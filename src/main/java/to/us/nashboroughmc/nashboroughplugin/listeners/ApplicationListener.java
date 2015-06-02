/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package to.us.nashboroughmc.nashboroughplugin.listeners;

import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import to.us.nashboroughmc.nashboroughplugin.Utils;
import to.us.nashboroughmc.nashboroughplugin.models.Application;

/**
 *
 * @author Jacob and Greg
 */
public class ApplicationListener implements Listener {
    
    private static final String COMMAND_APPLY       = "apply";
    private static final String COMMAND_REVIEW_APPS = "reviewapps";
    
    private static final String MESSAGE_PENDING  = "Looks like you have an application pending. We will get to it as soon as possible.";
    private static final String MESSAGE_ACCEPTED = "Congratulations! Your application accepted.";
    private static final String MESSAGE_DENIED   = "Sorry, your application was denied.";
    
    private final List<Application> applications;
    private HashMap<UUID, Application> pendingApplications;
    private HashMap<Player, UUID> reviewingPlayers = new HashMap<Player, UUID>();
    
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
                Utils.send_message(player, "Applications awaiting review: " + applications.size());
                Utils.send_message(player, "Use \"/reviewapps\" to review them");
            }
        }
    }
    @EventHandler
    public void onQuitEvent(PlayerQuitEvent ev) {
        Player player = ev.getPlayer();
        if(player.isOp() && reviewingPlayers.containsKey(player)) {
            reviewingPlayers.remove(player);
        }
    }
    
    public boolean handleCommand(Player player, String command) {
        
        switch(command) {
            case COMMAND_APPLY:
                Application application = getApplicationForPlayer(player);
            
                if(application != null) {
                    switch(application.getState()) {
                        case "pending":  Utils.send_message(player, MESSAGE_PENDING);  break;
                        case "accepted": Utils.send_message(player, MESSAGE_ACCEPTED); break;
                        case "denied":   Utils.send_message(player, MESSAGE_DENIED);   break;
                        default: handleMessage(player, null); break;
                    }

                //Create a new application
                } else {
                    applications.add(new Application(player));
                    handleMessage(player, null);
                }
                
                break;
                
            case COMMAND_REVIEW_APPS:
            	ArrayList<Player> reviewers = new ArrayList<Player>();
            	if (reviewingPlayers.keySet().contains(player)){
            		reviewingPlayers.remove(player);
            	}
            	for (Player reviewer : reviewingPlayers.keySet()){
            		reviewers.add(reviewer);
            	}
                if(reviewers.size() == 1) {

                	Utils.send_message(player, reviewers.get(0).getDisplayName() + " is also reviewing applications at the moment.");
                	Utils.send_message(player, " ");
                }
                else if (reviewers.size() == 2){
                	Utils.send_message(player, reviewers.get(0).getDisplayName() + " and " + reviewers.get(1).getDisplayName() + " are also reviewing applications at the moment.");
                	Utils.send_message(player, " ");
                }
                else if (reviewers.size() > 2){
                	String message = "";
                	for (int i = 0; i < reviewers.size() - 2; i++){
                		message += reviewers.get(i).getDisplayName() + ", ";
                	}
                	message += "and " + reviewers.get(reviewers.size()-1).getDisplayName() + " are also reviewing applications at the moment.";

                	Utils.send_message(player, message);
                	Utils.send_message(player, " ");
                }
                
                pendingApplications = getPendingApplications();
                
                
                if(pendingApplications.size() > 0) {
                	Object[] entries = pendingApplications.keySet().toArray();
                	Application app = (Application) pendingApplications.get(entries[0]);
                    reviewingPlayers.put(player, app.getUUID());
                    displayApplication(player, app);
                    
                } else {
                    Utils.send_message(player, "There are no pending applications at this time.");
                    pendingApplications = null;
                }
                    
                break;
                
            default:
                return false;
        }
        
        return true;
    }
    
    private void displayApplication(Player player, Application application) {
        
        Utils.send_message(player, "Name: "       + application.getUsername());
        Utils.send_message(player, "Age: "        + application.getAge());
        Utils.send_message(player, "Experience: " + application.getExperience());
        Utils.send_message(player, "Country: "    + application.getCountry());
        Utils.send_message(player, "Album: "      + application.getAlbum());
        
        Utils.send_message(player, "Type \"accept\", \"deny\", or \"cancel.\"");
    }
    
    private boolean handleMessage(Player player, String message) {
        
        getLogger().info("0");
        
        if(reviewingPlayers.containsKey(player)) {
            getLogger().info("1");
            UUID applicationUUID = reviewingPlayers.get(player);
            final UUID StateUUID = applicationUUID;
            if (pendingApplications.containsKey(applicationUUID)){
            	Application application = pendingApplications.get(applicationUUID);
                Player applicant = getServer().getPlayer(application.getUUID());
                getLogger().info("2");
                switch(message.toLowerCase()) {
                    case "accept":
                        application.setState("accepted");
                        if(applicant != null && applicant.isOnline()) {
                            Utils.send_message(applicant, MESSAGE_ACCEPTED);
                        }
						
                        new Thread(new Runnable(){

							@SuppressWarnings("unchecked")
							@Override
							public void run() {
								JSONParser parser = new JSONParser();
						    	JSONObject jsonObject = null;
						    	try {
									jsonObject = (JSONObject) parser.parse(new FileReader("applications.json"));
								} catch (IOException | ParseException e) {
									e.printStackTrace();
								};
								JSONObject playerobj = (JSONObject) jsonObject.get(StateUUID);
								playerobj.put("state", "accepted");
								jsonObject.put(StateUUID, playerobj);
								FileWriter file;
								try {
									file = new FileWriter("applications.json");
									file.write(jsonObject.toJSONString());
						    		file.flush();
						    		file.close();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								
							}
                        	
                        });
                        pendingApplications.remove(applicationUUID);
                    break;
                        
                    case "deny":
                        application.setState("denied");
                        if(applicant != null && applicant.isOnline()) {
                            Utils.send_message(applicant, MESSAGE_DENIED);
                        }
                        new Thread(new Runnable(){

							@SuppressWarnings("unchecked")
							@Override
							public void run() {
								JSONParser parser = new JSONParser();
						    	JSONObject jsonObject = null;
						    	try {
									jsonObject = (JSONObject) parser.parse(new FileReader("applications.json"));
								} catch (IOException | ParseException e) {
									e.printStackTrace();
								};
								JSONObject playerobj = (JSONObject) jsonObject.get(StateUUID);
								playerobj.put("state", "denied");
								jsonObject.put(StateUUID, playerobj);
								FileWriter file;
								try {
									file = new FileWriter("applications.json");
									file.write(jsonObject.toJSONString());
						    		file.flush();
						    		file.close();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								
							}
                        	
                        });
                        pendingApplications.remove(applicationUUID);
                    break;
                        
                    case "cancel":
                        reviewingPlayers.remove(player);
                        return true;
                        
                    default: return false;
                }
            } else {
            	Utils.send_message(player, "This application has been processed by another player.");
            	reviewingPlayers.remove(player);
            	return true;
            }
            
            
            if(pendingApplications.size() == 0) {
                Utils.send_message(player, "That's all for now! Thank you.");
                reviewingPlayers.remove(player);
            } else {
                Utils.send_message(player, (pendingApplications.size()-1) + " applications remaining.");
                reviewingPlayers.remove(player);
            }
            
            return true;
        }
        
        //Else, look for this players application
        for(Application application : applications) {  //TODO: This is iterated through EVERY TIME SOMEONE CHATS. Should this be fixed?
            
            if(application.getUsername().equals(player.getDisplayName())) {
                switch(application.getState()) {
                    case "started":
                        Utils.send_message(player, "Thank you for choosing Nashborough!");
                        Utils.send_message(player, "Which country to do you live in?");
                        application.setState("country");
                        break;
                        
                    case "country":
                        application.setCountry(message);
                        Utils.send_message(player, "What is your age?");
                        application.setState("age");
                        break;
                        
                    case "age":
                        application.setAge(message);
                        Utils.send_message(player, "How long have you been playing Minecraft for?");
                        application.setState("experience");
                        break;
                        
                    case "experience":
                        application.setExperience(message);
                        Utils.send_message(player, "If you would like, provide us with a link to an album of your previous builds.");
                        player.sendMessage("If you would like, provide us with a link to an album of your previous builds.");
                        application.setState("album");
                        break;
                        
                    case "album":
                        application.setAlbum(message);
                        application.setState("pending");
                        application.submit();
                        Utils.send_message(player, "That's all! We'll get to your application as soon as possible.");
                        
                        for(Player p : getServer().getOnlinePlayers()) {
                            if(p.isOp()) {
                                Utils.send_message(p, "A new application was submitted!");
                                Utils.send_message(p, "Applications awaiting review: " + getPendingApplications().size());
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
    
    private HashMap<UUID, Application> getPendingApplications() {
        HashMap<UUID, Application> pApplications = new HashMap<UUID, Application>();
        for(Application application : applications) {
            if(application.getState() == "pending") {
                pApplications.put(application.getUUID(),application);
            }
        }
        
        return pApplications;
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
    	JSONParser parser = new JSONParser();
    	JSONObject jsonObject = null;
    	 
    	try {
    		Object obj = parser.parse(new FileReader("applications.json"));
    		jsonObject = (JSONObject) obj;
    	} catch (FileNotFoundException e) {
    		jsonObject = new JSONObject();
    		FileWriter file;
			try {
				file = new FileWriter("applications.json");
				file.write(jsonObject.toJSONString());
	    		file.flush();
	    		file.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
    		
     
    	} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
    	Object[] keys = jsonObject.keySet().toArray();

		for (Object UUID : keys){
			JSONObject player = (JSONObject) jsonObject.get(UUID);
			Application app = new Application((String)player.get("state"));
			app.setAge((String) player.get("age"));
			app.setAlbum((String)player.get("album"));
			app.setCountry((String)player.get("country"));
			app.setExperience((String)player.get("experience"));
			app.setUsername((String)player.get("username"));
			String string = (String) player.get("UUID");
			UUID uuid = java.util.UUID.fromString(string);
			app.setUUID(uuid);
			applications.add(app);
		}
    }
}
