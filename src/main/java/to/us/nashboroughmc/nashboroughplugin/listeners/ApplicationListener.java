/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package to.us.nashboroughmc.nashboroughplugin.listeners;

<<<<<<< HEAD
import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;

=======
>>>>>>> c882aed3b3537e669125bba5caca44816ef63994
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

<<<<<<< HEAD
=======
import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;

>>>>>>> c882aed3b3537e669125bba5caca44816ef63994
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

<<<<<<< HEAD
import to.us.nashboroughmc.nashboroughplugin.Utils;
=======
>>>>>>> c882aed3b3537e669125bba5caca44816ef63994
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
<<<<<<< HEAD
                Utils.send_message(player, "Applications awaiting review: " + applications.size());
                Utils.send_message(player, "Use \"/reviewapps\" to review them");
=======
                player.sendMessage("Applications awaiting review: " + applications.size());
                player.sendMessage("Use \"/reviewapps\" to review them");
>>>>>>> c882aed3b3537e669125bba5caca44816ef63994
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
                        
<<<<<<< HEAD
                        case "pending":  Utils.send_message(player, MESSAGE_PENDING);  break;
                        case "accepted": Utils.send_message(player, MESSAGE_ACCEPTED); break;
                        case "denied":   Utils.send_message(player, MESSAGE_DENIED);   break;
=======
                        case "pending":  player.sendMessage(MESSAGE_PENDING);  break;
                        case "accepted": player.sendMessage(MESSAGE_ACCEPTED); break;
                        case "denied":   player.sendMessage(MESSAGE_DENIED);   break;
>>>>>>> c882aed3b3537e669125bba5caca44816ef63994
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
            	for (Player reviewer : reviewingPlayers.keySet()){
            		reviewers.add(reviewer); //TODO: Add if statement to check if they're already reviewing
            	}
                if(reviewers.size() == 1) {
<<<<<<< HEAD
                	Utils.send_message(player, reviewers.get(0).getDisplayName() + " is also reviewing applications at the moment.");
                	Utils.send_message(player, " ");
                }
                else if (reviewers.size() == 2){
                	Utils.send_message(player, reviewers.get(0).getDisplayName() + " and " + reviewers.get(1).getDisplayName() + " are also reviewing applications at the moment.");
                	Utils.send_message(player, " ");
=======
                	player.sendMessage(reviewers.get(0).getDisplayName() + " is also reviewing applications at the moment.");
                	player.sendMessage(" ");
                }
                else if (reviewers.size() == 2){
                	player.sendMessage(reviewers.get(0).getDisplayName() + " and " + reviewers.get(1).getDisplayName() + " are also reviewing applications at the moment.");
                	player.sendMessage(" ");
>>>>>>> c882aed3b3537e669125bba5caca44816ef63994
                }
                else if (reviewers.size() > 2){
                	String message = "";
                	for (int i = 0; i < reviewers.size() - 2; i++){
                		message += reviewers.get(i).getDisplayName() + ", ";
                	}
                	message += "and " + reviewers.get(reviewers.size()-1).getDisplayName() + " are also reviewing applications at the moment.";
<<<<<<< HEAD
                	Utils.send_message(player, message);
                	Utils.send_message(player, " ");
=======
                	player.sendMessage(message);
                	player.sendMessage(" ");
>>>>>>> c882aed3b3537e669125bba5caca44816ef63994
                }
                
                pendingApplications = getPendingApplications();
                
                
                if(pendingApplications.size() > 0) {
                	Object[] entries = pendingApplications.keySet().toArray();
                	Application app = (Application) pendingApplications.get(entries[0]);
                    reviewingPlayers.put(player, app.getUUID());
                    displayApplication(player, app);
                    
                } else {
<<<<<<< HEAD
                    Utils.send_message(player, "There are no pending applications at this time.");
=======
                    player.sendMessage("There are no pending applications at this time.");
>>>>>>> c882aed3b3537e669125bba5caca44816ef63994
                    pendingApplications = null;
                }
                    
                break;
                
            default:
                return false;
        }
        
        return true;
    }
    
    private void displayApplication(Player player, Application application) {
        
<<<<<<< HEAD
        Utils.send_message(player, "Name: "       + application.getUsername());
        Utils.send_message(player, "Age: "        + application.getAge());
        Utils.send_message(player, "Experience: " + application.getExperience());
        Utils.send_message(player, "Country: "    + application.getCountry());
        Utils.send_message(player, "Album: "      + application.getAlbum());
        
        Utils.send_message(player, "Type \"accept\", \"deny\", or \"cancel.\"");
=======
        player.sendMessage("Name: "       + application.getUsername());
        player.sendMessage("Age: "        + application.getAge());
        player.sendMessage("Experience: " + application.getExperience());
        player.sendMessage("Country: "    + application.getCountry());
        player.sendMessage("Album: "      + application.getAlbum());
        
        player.sendMessage("Type \"accept\", \"deny\", or \"cancel.\"");
>>>>>>> c882aed3b3537e669125bba5caca44816ef63994
    }
    
    private boolean handleMessage(Player player, String message) {
        
        getLogger().info("0");
        
        if(reviewingPlayers.containsKey(player)) {
            getLogger().info("1");
            UUID applicationUUID = reviewingPlayers.get(player);
            if (pendingApplications.containsKey(applicationUUID)){
            	Application application = pendingApplications.get(applicationUUID);
                Player applicant = getServer().getPlayer(application.getUUID());
                getLogger().info("2");
                switch(message.toLowerCase()) {
                    case "accept":
                        application.setState("accepted");
                        if(applicant != null && applicant.isOnline()) {
<<<<<<< HEAD
                            Utils.send_message(applicant, MESSAGE_ACCEPTED);
                        }
                    break;
                        
                    case "deny":
                        application.setState("denied");
                        if(applicant != null && applicant.isOnline()) {
                            Utils.send_message(applicant, MESSAGE_DENIED);
                        }
=======
                            applicant.sendMessage(MESSAGE_ACCEPTED);
                        }
                        pendingApplications.remove(applicationUUID);
                    break;
                        
                    case "deny":
                    	//TODO: Ban the player with message
                        application.setState("denied");
                        if(applicant != null && applicant.isOnline()) {
                            applicant.sendMessage(MESSAGE_DENIED);
                        }
                        pendingApplications.remove(applicationUUID);
>>>>>>> c882aed3b3537e669125bba5caca44816ef63994
                    break;
                        
                    case "cancel":
                        reviewingPlayers.remove(player);
                        return true;
                        
                    default: return false;
                }
            } else {
<<<<<<< HEAD
            	Utils.send_message(player, "This application has been processed by another player.");
=======
            	player.sendMessage("This application has been processed by another player.");
            	reviewingPlayers.remove(player);
            	return true;
>>>>>>> c882aed3b3537e669125bba5caca44816ef63994
            }
            
            
            if(pendingApplications.size() == 0) {
<<<<<<< HEAD
                Utils.send_message(player, "That's all for now! Thank you.");
                reviewingPlayers.remove(player);
            } else {
                Utils.send_message(player, (pendingApplications.size()-1) + " applications remaining.");
=======
                player.sendMessage("That's all for now! Thank you.");
                reviewingPlayers.remove(player);
            } else {
                player.sendMessage((pendingApplications.size()-1) + " applications remaining.");
>>>>>>> c882aed3b3537e669125bba5caca44816ef63994
            }
            
            return true;
        }
        
        //Else, look for this players application
        for(Application application : applications) {  //TODO: This is iterated through EVERY TIME SOMEONE CHATS
            
            if(application.getUsername().equals(player.getDisplayName())) {
                switch(application.getState()) {
                    case "started":
<<<<<<< HEAD
                        Utils.send_message(player, "Thank you for choosing Nashborough!");
                        Utils.send_message(player, "Which country to do you live in?");
=======
                        player.sendMessage("Thank you for choosing Nashborough!");
                        player.sendMessage("Which country to do you live in?");
>>>>>>> c882aed3b3537e669125bba5caca44816ef63994
                        application.setState("country");
                        break;
                        
                    case "country":
                        application.setCountry(message);
<<<<<<< HEAD
                        Utils.send_message(player, "What is your age?");
=======
                        player.sendMessage("What is your age?");
>>>>>>> c882aed3b3537e669125bba5caca44816ef63994
                        application.setState("age");
                        break;
                        
                    case "age":
                        application.setAge(message);
<<<<<<< HEAD
                        Utils.send_message(player, "How long have you been playing Minecraft for?");
=======
                        player.sendMessage("How long have you been playing Minecraft for?");
>>>>>>> c882aed3b3537e669125bba5caca44816ef63994
                        application.setState("experience");
                        break;
                        
                    case "experience":
                        application.setExperience(message);
<<<<<<< HEAD
                        Utils.send_message(player, "If you would like, provide us with a link to an album of your previous builds.");
=======
                        player.sendMessage("If you would like, provide us with a link to an album of your previous builds.");
>>>>>>> c882aed3b3537e669125bba5caca44816ef63994
                        application.setState("album");
                        break;
                        
                    case "album":
                        application.setAlbum(message);
                        application.submit();
<<<<<<< HEAD
                        Utils.send_message(player, "That's all! We'll get to your application as soon as possible.");
=======
                        player.sendMessage("That's all! We'll get to your application as soon as possible.");
>>>>>>> c882aed3b3537e669125bba5caca44816ef63994
                        application.setState("pending");
                        
                        for(Player p : getServer().getOnlinePlayers()) {
                            if(p.isOp()) {
<<<<<<< HEAD
                                Utils.send_message(p, "A new application was submitted!");
                                Utils.send_message(p, "Applications awaiting review: " + getPendingApplications().size());
=======
                                p.sendMessage("A new application was submitted!");
                                p.sendMessage("Applications awaiting review: " + getPendingApplications().size());
>>>>>>> c882aed3b3537e669125bba5caca44816ef63994
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
<<<<<<< HEAD
     
    	
        /*BufferedReader reader = null;
        
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
                        applications.add(new Application(Application.State.PENDING));
                        applications.get(appIndex).setUUID(java.util.UUID.fromString(line));
                        break;
                        
                    case USERNAME:   applications.get(appIndex).setUsername(line);   break;
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
        }*/
=======
>>>>>>> c882aed3b3537e669125bba5caca44816ef63994
    }
}
