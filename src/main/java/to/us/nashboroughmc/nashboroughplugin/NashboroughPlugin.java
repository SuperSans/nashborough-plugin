/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package to.us.nashboroughmc.nashboroughplugin;

import to.us.nashboroughmc.nashboroughplugin.models.Application;
import to.us.nashboroughmc.nashboroughplugin.listeners.ApplicationListener;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import to.us.nashboroughmc.nashboroughplugin.callbacks.ApplicationInterface;

public class NashboroughPlugin extends JavaPlugin implements ApplicationInterface {
    
    private ApplicationListener applicationListener;
    private List<Application>   applications;
    
    @Override 
    public void onEnable() {
        applicationListener = new ApplicationListener(this);
        getServer().getPluginManager().registerEvents(applicationListener, this);
        
        applications = new ArrayList<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if(sender instanceof Player && command.getName().equalsIgnoreCase("apply")) {
            Player player = (Player)sender;
            
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
            
            return true;
        }
        
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
    
    @Override
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
}
