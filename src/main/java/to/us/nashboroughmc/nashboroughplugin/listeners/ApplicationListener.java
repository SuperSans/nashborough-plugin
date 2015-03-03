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
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import to.us.nashboroughmc.nashboroughplugin.Utils;
import to.us.nashboroughmc.nashboroughplugin.models.Application;

/**
 *
 * @author Jacob
 */
public class ApplicationListener implements CommandExecutor, Listener {
    
    private static final int UUID       = 0;
    private static final int USERNAME   = 1;
    private static final int COUNTRY    = 2;
    private static final int AGE        = 3;
    private static final int EXPERIENCE = 4;
    private static final int ALBUM      = 5;
    private static final int INFORMED   = 6;
    private static final int STATE      = 7;
    private static final int NEWLINE    = 8;
    
    private static final String COMMAND_APPLY       = "apply";
    private static final String COMMAND_REVIEW_APPS = "reviewapps";
    private static final String COMMAND_REVIEW_APP  = "reviewapp";
    
    private static final String MESSAGE_NO_APP   = "Looks like you haven't filled out an application yet! To start on your path to membership, use /apply to tell us about yourself.";
    private static final String MESSAGE_PENDING  = "Looks like you have an application pending. We will get to it as soon as possible.";
    private static final String MESSAGE_ACCEPTED = "Congratulations! Your application accepted.";
    private static final String MESSAGE_DENIED   = "Sorry, your application was denied.";
    
    private final List<Application> applications;
    private final List<Application> pendingApplications;
    
    public ApplicationListener() {
        applications        = new ArrayList<>();
        pendingApplications = new ArrayList<>();
        
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
        
        Application application = getApplicationForPlayer(player);
        
        if(application == null) {
            Utils.sendMessage(player, MESSAGE_NO_APP);
            
        } else {
            switch(application.getState()) {
                case PENDING:
                    Utils.sendMessage(player, MESSAGE_PENDING);
                break;
                    
                case ACCEPTED:
                    if(!application.isInformed()) {
                        Utils.sendMessage(player, MESSAGE_ACCEPTED);
                        player.setGameMode(GameMode.SURVIVAL);
                    }
                break;
                    
                case DENIED:
                    if(!application.isInformed()) {
                        Utils.sendMessage(player, MESSAGE_DENIED);
                    }
                break;
            }
        }
        
        if(player.isOp()) {
            if(pendingApplications.size() > 0) {
                Utils.sendMessage(player, "Applications awaiting review: " + (pendingApplications.size()));
                Utils.sendMessage(player, "Use \"/reviewapps\" to review them");
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        switch(command.getName()) {
            case COMMAND_APPLY:
                if(sender instanceof Player) {
                    Player player = (Player)sender;
                    
                    Application application = getApplicationForPlayer(player);
            
                    if(application != null) {
                        switch(application.getState()) {

                            case PENDING:  Utils.sendMessage(player, MESSAGE_PENDING);  break;
                            case ACCEPTED: Utils.sendMessage(player, MESSAGE_ACCEPTED); break;
                            case DENIED:   Utils.sendMessage(player, MESSAGE_DENIED);   break;
                            default: handleMessage(player, null); break;
                        }

                    //Create a new application
                    } else {
                        applications.add(new Application(player));
                        handleMessage(player, null);
                    }
                } else {
                    Utils.sendMessage(sender, "Only players can apply.");
                }
                
                break;
                
            case COMMAND_REVIEW_APPS:
                if(pendingApplications.isEmpty()) {
                    Utils.sendMessage(sender, "There are no applications awaiting review.");
                    
                } else {
                    
                    for(Application pendingApplication : pendingApplications) {
                        Utils.sendMessage(sender, pendingApplication.getUsername());
                    }
                    
                    Utils.sendMessage(sender, "Use \"/reviewapp <username>\" to review an application.");
                }
                
                break;
                
            case COMMAND_REVIEW_APP:
                if(args.length > 0 && args.length < 3) {
                    Application application = getApplicationForUsername(args[0]);
                    
                    if(application == null) {
                        Utils.sendMessage(sender, "Could not find an application for that username");
                        
                    } else if(args.length == 1) {
                        displayApplication(sender, application);
                        Utils.sendMessage(sender, "Use \"/reviewapp <username> [verdict]\" to accept or deny this application.");
                
                    } else {
                        Player player = getServer().getPlayer(application.getUUID());
                        
                        switch(args[1]) {
                            case "accept":
                                application.setState(Application.State.ACCEPTED);
                                Utils.sendMessage(sender, args[0] + " has been accepted.");
                                
                                if(player != null && player.isOnline()) {
                                    Utils.sendMessage(player, MESSAGE_ACCEPTED);
                                    
                                    if(player.getGameMode().equals(GameMode.ADVENTURE)) {
                                        player.setGameMode(GameMode.SURVIVAL);
                                    }
                                    
                                    pendingApplications.remove(application);
                                    application.setIsInformed(true);
                                }
                                break;
                                
                            case "deny":
                                application.setState(Application.State.DENIED);
                                Utils.sendMessage(sender, args[0] + " has been denied");
                                
                                if(player != null && player.isOnline()) {
                                    Utils.sendMessage(player, MESSAGE_DENIED);
                                    
                                    pendingApplications.remove(application);
                                    application.setIsInformed(true);
                                }
                                break;
                                
                            default:
                                Utils.sendMessage(sender, "Use \"accept\" or \"deny.\"");
                                break;
                        }
                    }
                }
                break;
                
            default:
                return false;
        }
        
        return true;
    }
    
    private void displayApplication(CommandSender sender, Application application) {
        
        Utils.sendMessage(sender, "Name: "       + application.getUsername());
        Utils.sendMessage(sender, "Age: "        + application.getAge());
        Utils.sendMessage(sender, "Experience: " + application.getExperience());
        Utils.sendMessage(sender, "Country: "    + application.getCountry());
        Utils.sendMessage(sender, "Album: "      + application.getAlbum());
    }
    
    private boolean handleMessage(Player player, String message) {
        
        //Look for this players application
        for(Application application : applications) {
            
            if(application.getUsername().equals(player.getDisplayName())) {
                switch(application.getState()) {
                    case STARTED:
                        Utils.sendMessage(player, "Thank you for choosing Nashborough!");
                        Utils.sendMessage(player, "What country to do you live in?");
                        application.setState(Application.State.COUNTRY);
                        break;
                        
                    case COUNTRY:
                        application.setCountry(message);
                        player.sendMessage(message);
                        Utils.sendMessage(player, "How old are you?");
                        application.setState(Application.State.AGE);
                        break;
                        
                    case AGE:
                        application.setAge(message);
                        player.sendMessage(message);
                        Utils.sendMessage(player, "How long have you been playing Minecraft for?");
                        application.setState(Application.State.EXPERIENCE);
                        break;
                        
                    case EXPERIENCE:
                        application.setExperience(message);
                        player.sendMessage(message);
                        Utils.sendMessage(player, "If you would like, provide us with a link to an album of your previous builds.");
                        application.setState(Application.State.ALBUM);
                        break;
                        
                    case ALBUM:
                        application.setAlbum(message);
                        player.sendMessage(message);
                        application.setState(Application.State.PENDING);
                        application.submit();
                        Utils.sendMessage(player, "That's all! We'll get to your application as soon as possible.");
                        pendingApplications.add(application);
                        
                        for(Player op : getServer().getOnlinePlayers()) {
                            if(op.isOp()) {
                                Utils.sendMessage(op, "A new application was submitted!");
                                Utils.sendMessage(op, "Applications awaiting review: " + pendingApplications.size());
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
    
    private Application getApplicationForPlayer(Player player) {
        for(Application application: applications) {
            if(application.getUsername().equals(player.getDisplayName())) {
                return application;
            }
        }
        
        return null;
    }
    
    private Application getApplicationForUsername(String username) {
        for(Application application: applications) {
            if(application.getUsername().equalsIgnoreCase(username)) {
                return application;
            }
        }
        
        return null;
    }
    
    private void loadSavedApplications() {
        BufferedReader reader = null;
        
        try {
            File applicationsFile = new File("pending_applications.txt");
            
            if(!applicationsFile.exists()) {
                applicationsFile.createNewFile();
            }
            
            reader = new BufferedReader(new FileReader(applicationsFile));
            
            String line;
            int lineIndex = 0, appIndex = 0;
            while((line = reader.readLine()) != null) {
                switch(lineIndex++) {
                    case UUID:
                        applications.add(new Application());
                        applications.get(appIndex).setUUID(java.util.UUID.fromString(line));
                        break;
                        
                    case USERNAME:   applications.get(appIndex).setUsername(line);   break;
                    case COUNTRY:    applications.get(appIndex).setCountry(line);    break;
                    case AGE:        applications.get(appIndex).setAge(line);        break;
                    case EXPERIENCE: applications.get(appIndex).setExperience(line); break;
                    case ALBUM:      applications.get(appIndex).setAlbum(line);      break;
                    case INFORMED:   applications.get(appIndex).setIsInformed(Boolean.getBoolean(line)); break;
                        
                    case STATE:
                        Application application = applications.get(appIndex);
                        switch(line) {
                            case "pending":  application.setState(Application.State.PENDING);  break;
                            case "accepted": application.setState(Application.State.ACCEPTED); break;
                            case "denied":   application.setState(Application.State.DENIED);   break;
                            default:         application.setState(Application.State.STARTED);  break;
                        }
                    break;
                        
                    case NEWLINE:
                        appIndex++;
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
            
            for(Application application : applications) {
            if(application.getState() == Application.State.PENDING) {
                pendingApplications.add(application);
            }
        }
        }
    }
}
