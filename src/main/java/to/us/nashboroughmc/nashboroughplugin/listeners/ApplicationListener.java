/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package to.us.nashboroughmc.nashboroughplugin.listeners;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import static org.bukkit.Bukkit.getLogger;
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
import static to.us.nashboroughmc.nashboroughplugin.models.Application.State.AGE;
import static to.us.nashboroughmc.nashboroughplugin.models.Application.State.ALBUM;
import static to.us.nashboroughmc.nashboroughplugin.models.Application.State.COUNTRY;
import static to.us.nashboroughmc.nashboroughplugin.models.Application.State.EXPERIENCE;

/**
 *
 * @author Jacob
 */
public class ApplicationListener implements CommandExecutor, Listener {
    
    private static final String FILE_PENDING_APPLICATIONS = "pending_applications.txt";
    private static final String FILE_REVIEWED_APPLICATIONS = "reviewed_applications.txt";
    
    private static final String COMMAND_APPLY       = "apply";
    private static final String COMMAND_REVIEW_APPS = "reviewapps";
    
    private static final String MESSAGE_NO_APP   = "Looks like you haven't filled out an application yet! To start on your path to membership, use /apply to tell us about yourself.";
    private static final String MESSAGE_PENDING  = "Looks like you have an application pending. We will get to it as soon as possible.";
    private static final String MESSAGE_ACCEPTED = "Congratulations! Your application accepted.";
    private static final String MESSAGE_DENIED   = "Sorry, your application was denied.";
    
    private final List<Application> pendingApplications;
    private final List<Application> reviewedApplications;
    
    public ApplicationListener() {
        
        pendingApplications  = new ArrayList<>();
        reviewedApplications = new ArrayList<>();
        
        loadPendingApplications();
        loadReviewedApplications();
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
                    if(!application.isInformed()) {
                        Utils.sendMessage(player, MESSAGE_PENDING);
                    }
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
                            
                        default: getLogger().info("null");
                    }
                        
                    } else {
                        pendingApplications.add(new Application(player));
                        handleMessage(player, null);
                    }
                    
                } else {
                    Utils.sendMessage(sender, "Only players can apply.");
                }
                
                break;
                
            case COMMAND_REVIEW_APPS:
                
                switch(args.length) {
                    case 0: 
                        for(Application application : pendingApplications) {
                            Utils.sendMessage(sender, application.getUsername());
                        }
                    break;
                        
                    case 1:
                    case 2:
                        Application application = getApplicationForUsername(args[0]);
                        
                        if(application != null) {
                            if(args.length == 1) {
                                displayApplication(sender, application);
                            } else {
                                
                                switch(args[1]) {
                                    case "accept": 
                                }
                            }
                            
                        } else {
                            Utils.sendMessage(sender, "Could not find an application for " + args[0]);
                        }
                    break;
                        
                    default:
                        Utils.sendMessage(sender, "Usage: /reviewapps [player] [verdict]");
                    break;
                }
                
                break;
                
            default:
                return false;
        }
        
        return true;
    }
    
    private void displayApplication(CommandSender sender, Application application) {
        
        Utils.sendMessage(sender, "Username: "       + application.getUsername());
        Utils.sendMessage(sender, "Age: "        + application.getAge());
        Utils.sendMessage(sender, "Experience: " + application.getExperience());
        Utils.sendMessage(sender, "Country: "    + application.getCountry());
        Utils.sendMessage(sender, "Album: "      + application.getAlbum());
        
        SimpleDateFormat timeFormat = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a z");
        
        Utils.sendMessage(sender, "Time Applied: " + timeFormat.format(application.getTimeApplied()));
    }
    
    private boolean handleMessage(Player player, String message) {
        
        //Look for this players application
        for(Application application : pendingApplications) {
            
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
                        Utils.sendMessage(player, "That's all! We'll get to your application as soon as possible.");
                        application.setState(Application.State.PENDING);
                        
                        application.setTimeApplied(new Date());
                        savePendingApplications();
                        
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
        for(Application application : reviewedApplications) {
            if(application.getUsername().equals(player.getDisplayName())) {
                return application;
            }
        }
        
        for(Application application : pendingApplications) {
            if(application.getUsername().equals(player.getDisplayName())) {
                return application;
            }
        }
        
        return null;
    }
    
    private Application getApplicationForUsername(String username) {
        for(Application application : reviewedApplications) {
            if(application.getUsername().equalsIgnoreCase(username)) {
                return application;
            }
        }
        
        for(Application application : pendingApplications) {
            if(application.getUsername().equalsIgnoreCase(username)) {
                return application;
            }
        }
        
        return null;
    }
    
    private void savePendingApplications() {
        BufferedWriter writer = null;
        
        try {
            File applicationsFile = new File(FILE_PENDING_APPLICATIONS);
            if(!applicationsFile.exists()) {
                applicationsFile.createNewFile();
            }
            
            writer = new BufferedWriter(new FileWriter(applicationsFile, false));
            
            for(Application application : pendingApplications) {
                
                writeLine(writer, application.getUUID().toString());
                writeLine(writer, application.getUsername());
                writeLine(writer, application.getCountry());
                writeLine(writer, application.getAge());
                writeLine(writer, application.getExperience());
                writeLine(writer, application.getAlbum());
                writeLine(writer, new SimpleDateFormat().format(application.getTimeApplied()));
                writer.newLine();
            }
            
            writer.close();
            
        } catch (Exception e) {
            
        }
    }
    
    private void loadPendingApplications() {
        
        try {
            File applicationsFile = new File(FILE_PENDING_APPLICATIONS);
            if(!applicationsFile.exists()) {
                applicationsFile.createNewFile();
            }
            
            BufferedReader reader = new BufferedReader(new FileReader(applicationsFile));
            
            String line;
            Application application;
            while((line = reader.readLine()) != null) {
                application = new Application();
                
                application.setUUID(UUID.fromString(line));
                application.setUsername(reader.readLine());
                application.setCountry(reader.readLine());
                application.setAge(reader.readLine());
                application.setExperience(reader.readLine());
                application.setAlbum(reader.readLine());
                application.setTimeApplied(new SimpleDateFormat().parse(reader.readLine()));
                application.setState(Application.State.PENDING);
                
                pendingApplications.add(application);
            }
            reader.close();
            
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private void saveReviewedApplications() {
        
    }
    
    private void loadReviewedApplications() {
        BufferedReader reader = null;
        
        try {
            File applicationsFile = new File(FILE_REVIEWED_APPLICATIONS);
            if(!applicationsFile.exists()) {
                applicationsFile.createNewFile();
            }
            
            reader = new BufferedReader(new FileReader(applicationsFile));
            
            String line;
            Application application;
            while((line = reader.readLine()) != null) {
                application = new Application();
                
                application.setUUID(UUID.fromString(line));
                application.setUsername(reader.readLine());
                application.setCountry(reader.readLine());
                application.setAge(reader.readLine());
                application.setExperience(reader.readLine());
                application.setAlbum(reader.readLine());
                application.setState(Application.State.PENDING);
                
                reviewedApplications.add(application);
            }
            
            reader.close();
            
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private void writeLine(BufferedWriter writer, String text) throws IOException {
        writer.write(text);
        writer.newLine();
    }
}
