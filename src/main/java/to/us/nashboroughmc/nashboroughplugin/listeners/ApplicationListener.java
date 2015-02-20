/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package to.us.nashboroughmc.nashboroughplugin.listeners;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import static org.bukkit.Bukkit.getServer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import to.us.nashboroughmc.nashboroughplugin.models.Application;

/**
 *
 * @author Jacob
 */
public class ApplicationListener implements Listener {
    
    private static final int USERNAME   = 0;
    private static final int COUNTRY    = 1;
    private static final int AGE        = 2;
    private static final int EXPERIENCE = 3;
    private static final int ALBUM      = 4;
    
    private static final String COMMAND_APPLY = "apply";
    
    private final List<Application> applications;
    
    public ApplicationListener() {
        applications = new ArrayList<>();
        
        loadSavedApplications();
    }
    
    @EventHandler
    public void onChatEvent(AsyncPlayerChatEvent ev) {
        Player player  = ev.getPlayer();
        String message = ev.getMessage();
        
        //Cancel message if it is handled
        if(handleMessage(player, message)) {
            ev.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onJoinEvent(PlayerJoinEvent ev) {
        Player player = ev.getPlayer();
        
        if(player.isOp()) {
            if(applications.size() > 0) {
                player.sendMessage("Applications awaiting review: " + applications.size());
                player.sendMessage("Use \"/reviewapps\" to review them");
            }
        }
    }
    
    public boolean handleCommand(Player player, String command) {
        
        switch(command) {
            case COMMAND_APPLY:
                Application application = getApplicationForPlayer(player);
            
                if(application != null) {
                    switch(application.getState()) {
                        default:
                            handleMessage(player, null);
                            break;

                        case PENDING:
                            player.sendMessage("Looks like you have an application pending. We will get to it as soon as possible.");
                            break;

                        case ACCEPTED:
                            player.sendMessage("Congratulations! You were accepted.");
                            break;

                        case DENIED:
                            player.sendMessage("Sorry, your application was denied.");
                            break;
                    }

                //Create a new application
                } else {
                    applications.add(new Application(player));
                    handleMessage(player, null);
                }
                
                break;
                
            default:
                return false;
        }
        
        return true;
    }
    
    public boolean handleMessage(Player player, String message) {
        
        for(Application application : applications) {
            
            if(application.getUsername().equals(player.getDisplayName())) {
                switch(application.getState()) {
                    case STARTED:
                        player.sendMessage("Thank you for choosing Nashborough!");
                        player.sendMessage("What country to do you live in?");
                        application.setState(Application.State.COUNTRY);
                        break;
                        
                    case COUNTRY:
                        application.setCountry(message);
                        player.sendMessage("How old are you?");
                        application.setState(Application.State.AGE);
                        break;
                        
                    case AGE:
                        application.setAge(message);
                        player.sendMessage("How long have you been playing Minecraft for?");
                        application.setState(Application.State.EXPERIENCE);
                        break;
                        
                    case EXPERIENCE:
                        application.setExperience(message);
                        player.sendMessage("If you would like, provide us with a link to an album of your previous builds.");
                        application.setState(Application.State.ALBUM);
                        break;
                        
                    case ALBUM:
                        application.setAlbum(message);
                        application.submit();
                        player.sendMessage("That's all! We'll get to your application as soon as possible.");
                        application.setState(Application.State.PENDING);
                        
                        for(Player p : getServer().getOnlinePlayers()) {
                            if(p.isOp()) {
                                p.sendMessage("A new application was submitted!");
                                p.sendMessage("Applications awaiting review: " + getPendingApplications().size());
                            }
                        }
                        
                        break;
                        
                    //Application is pending, denied, or accepted
                    default:
                        return false;
                }
                
                //Message handled
                return true;
            }
        }
        
        //Message unhandled
        return false;
    }
    
    private List<Application> getPendingApplications() {
        List<Application> pendingApplications = new ArrayList<>();
        
        int i = 0;
        for(Application application : applications) {
            if(application.getState() == Application.State.PENDING) {
                pendingApplications.add(application);
            }
        }
        
        return pendingApplications;
    }
    
    private Application getApplicationForPlayer(Player player) {
        for(Application application: applications) {
            if(application.getUsername().equals(player.getDisplayName())) {
                return application;
            }
        }
        
        return null;
    }
    
    private void loadSavedApplications() {
        BufferedReader reader = null;
        
        try {
            File applicationsFile = new File("pending_applications.txt");
            
            reader = new BufferedReader(new FileReader(applicationsFile));
            
            String line;
            int lineIndex = 0, appIndex = 0;
            while((line = reader.readLine()) != null) {
                switch(lineIndex++) {
                    case USERNAME:
                        applications.add(new Application(Application.State.PENDING));
                        applications.get(appIndex).setUsername(line);
                        break;
                        
                    case COUNTRY:    applications.get(appIndex).setCountry(line);    break;
                    case AGE:        applications.get(appIndex).setAge(line);        break;
                    case EXPERIENCE: applications.get(appIndex).setExperience(line); break;
                        
                    case ALBUM:
                        applications.get(appIndex++).setAlbum(line);
                        lineIndex = 0;
                        break;
                }
            }
            
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch(Exception e) {
                
            }
        }
    }
}
