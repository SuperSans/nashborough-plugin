/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package to.us.nashboroughmc.nashboroughplugin.listeners;

import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
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
    
    private static final String MESSAGE_PENDING   = "Looks like you have an application pending. We will get to it as soon as possible.";
    private static final String MESSAGE_ACCEPTED  = "Congratulations! Your application accepted.";
    private static final String MESSAGE_DENIED    = "Sorry, your application was denied.";
    private static final String MESSAGE_SELECTING = "Now that your preliminary application has been accepted, you must choose where to build your home.";
    private static final String MESSAGE_BUILDING  = "You're in the building stage of your membership. When your survival build is completed, use the /submitbuild command and a moderator will review your build.";
    
    private static final String WEST_AUTUMNPORT_INFO   = "Autumnport: West of the coastal town, Autumnport. Style: Wooden, Stone";
    private static final int[]  WEST_AUTUMNPORT_COORDS = {106, 69, -498};
    
    private static final String FELLFRIN_INFO   = "Fellfrin: A survival-styled village featuring cabins and cottages. Style: Wooden";
    private static final int[]  FELLFRIN_COORDS = {-224, 72, -311};
    
    private static final String SORRENTO_INFO   = "Sorrento: A suburn connecting Autumnport and Nashborough. Style: Wooden, Stone";
    private static final int[]  SORRENTO_COORDS = {525, 72, -321};
    
    private static final String WEST_NASH_INFO   = "West Nashborough: An underdeveloped suburb in need of a community. Style: Wooden, Brick, Stone";
    private static final int[]  WEST_NASH_COORDS = {-245, 64, 145};
    
    private static final String SOUTH_NASH_INFO   = "South Nashborough: An underdeveloped suburb with a farming community a few hundred meters south of it. Style: Wooden, Brick, Stone";
    private static final int[]  SOUTH_NASH_COORDS = {-65, 67, 429};
    
    private final HashMap<UUID, Application> applications;
    private HashMap<UUID, Application> pendingApplications;
    private HashMap<Player, UUID> reviewingPlayers = new HashMap<Player, UUID>();
    private HashMap<Player, UUID> selectingPlayers = new HashMap<Player, UUID>();
    public ApplicationListener() {
        applications = new HashMap<UUID, Application>();
        loadSavedApplications();
        pendingApplications = getPendingApplications();
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
        Player player = ev.getPlayer(); //TODO: Handle whether or not a player has applied. Should we make 2 separate JSONs for denied and accepted applications as well?
        if (applications.containsKey(player.getUniqueId())){
        	Application application = applications.get(player.getUniqueId());
        	switch(application.getState()){
        	case "pending"	:  	Utils.send_message(player, MESSAGE_PENDING);  break;
        	case "accepted"	:	alertAcceptance(player);  break;
        	case "selecting": 	messageLocationOptions(player); break;
        	case "building"	: 	Utils.send_message(player, MESSAGE_BUILDING);  break;
        	}
        }
        else{
        	Utils.send_message(player, "Welcome to the Nashborough Server! Feel free to explore the city, and when you're ready to get started, use the /apply command.");
        }
        
        if(player.isOp()) {
            if(pendingApplications.size() > 0) {
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
                        case "pending":   Utils.send_message(player, MESSAGE_PENDING);  break;
                        case "accepted":  Utils.send_message(player, MESSAGE_ACCEPTED); break;
                        case "denied":    Utils.send_message(player, MESSAGE_DENIED);   break;
                        case "selecting": messageLocationOptions(player);   break;
                        case "building":  Utils.send_message(player, MESSAGE_BUILDING);   break;
                        default: handleMessage(player, null); break;
                    }

                //Create a new application
                } else {
                    applications.put(player.getUniqueId(),new Application(player));
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
        
    	Utils.send_message(player, " ");
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
            if (pendingApplications.containsKey(applicationUUID)){
            	Application application = pendingApplications.get(applicationUUID);
                Player applicant = getServer().getPlayer(application.getUUID());
                getLogger().info("2");
                switch(message.toLowerCase()) {
                    case "accept":
                        application.setState("accepted");
                        application.changeFileState("accepted");
                        if(applicant != null && applicant.isOnline()) {
                            alertAcceptance(applicant);
                        }
                        pendingApplications.remove(applicationUUID);
                        break;
                        
                    case "deny":
                        application.setState("denied");
                        application.changeFileState("denied");
                        if(applicant != null && applicant.isOnline()) {
                            Utils.send_message(applicant, MESSAGE_DENIED);
                        }
                        BanList banlist = Bukkit.getBanList(BanList.Type.NAME);
                        banlist.addBan(applicant.getDisplayName(), "Your application has been rejected. We wish you luck in your server search!", null, "Application Plugin");
                        final Player p = applicant;
                        if (applicant.isOnline()){
	                        Bukkit.getScheduler().runTask(getServer().getPluginManager().getPlugin("NashboroughPlugin"), new Runnable() {
	                        	  public void run() {
	                        		  p.kickPlayer("Your application has been rejected. We wish you luck in your server search!");
	                        	  }
	                        	});
                        }
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
                Utils.send_message(player, (pendingApplications.size()) + " application(s) remaining.");
                reviewingPlayers.remove(player);
            }
            
            return true;
        }
        
        //Else, look for this players application
        if(applications.containsKey(player.getUniqueId())){ //TODO: This is iterated through EVERY TIME SOMEONE CHATS. Should this be fixed?
        	Application application = applications.get(player.getUniqueId());
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
                
                case "selecting":
	                	switch(message.toLowerCase()){
	                	case "autumnport":  player.teleport(new Location(Bukkit.getWorld("world"), WEST_AUTUMNPORT_COORDS[0], WEST_AUTUMNPORT_COORDS[1], WEST_AUTUMNPORT_COORDS[2])); 
	                		updateToBuilder(player);
	                		break;
	                	case "fellfrin": player.teleport(new Location(Bukkit.getWorld("world"), FELLFRIN_COORDS[0], FELLFRIN_COORDS[1], FELLFRIN_COORDS[2]));
	                		//TODO: Give kits here
	                		updateToBuilder(player);
	                		break;
	                	case "sorrento": player.teleport(new Location(Bukkit.getWorld("world"), SORRENTO_COORDS[0], SORRENTO_COORDS[1], SORRENTO_COORDS[2]));  
	                		updateToBuilder(player);
	                		break;
	                	case "west nashborough": player.teleport(new Location(Bukkit.getWorld("world"), WEST_NASH_COORDS[0], WEST_NASH_COORDS[1], WEST_NASH_COORDS[2]));  
	                		updateToBuilder(player);
	                		break;
	                	case "south nashborough": player.teleport(new Location(Bukkit.getWorld("world"), SOUTH_NASH_COORDS[0], SOUTH_NASH_COORDS[1], SOUTH_NASH_COORDS[2]));  
	                		updateToBuilder(player);
	                		break;
	                	default: return false;
                		
                	}
                	
                	break;
                    
                //Application is pending, denied, or accepted
                default:
                    return false;
            }
            
            //Message handled
            return true;
        }
        
        //Message unhandled
        return false;
    }
    
    private HashMap<UUID, Application> getPendingApplications() {
        HashMap<UUID, Application> pApplications = new HashMap<UUID, Application>();
        for (Map.Entry<UUID, Application> entry : applications.entrySet()) {
            if (entry.getValue().getState().equals("pending")){
            	pApplications.put(entry.getValue().getUUID(),entry.getValue());
            }
        }        
        return pApplications;
    }
    
    private Application getApplicationForPlayer(Player player) {
        return applications.get(player.getUniqueId());
    }
    
    private void loadSavedApplications() {
    	JSONParser parser = new JSONParser();
    	JSONObject jsonObject = null;
    	 
    	try {
    		Object obj = parser.parse(new FileReader("applications.json"));
    		jsonObject = (JSONObject) obj;
    	} catch (IOException|ParseException e) {
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
			applications.put(uuid,app);
		}
    }
    
    private void alertAcceptance(final Player applicant){
    	Utils.send_message(applicant, MESSAGE_ACCEPTED);
    	
        Application application = applications.get(applicant.getUniqueId());
        Utils.send_message(applicant, "");
        Utils.send_message(applicant, application.getCountry());
        Utils.send_message(applicant, "");
        application.setState("selecting");
        application.changeFileState("selecting");
        messageLocationOptions(applicant);
    }
    
    private void messageLocationOptions(Player player){
    	selectingPlayers.put(player, player.getUniqueId()); 
    	Utils.send_message(player, MESSAGE_SELECTING);
    	Utils.send_message(player, "Here are your options. To select one, type the name of the location in chat:"); 
    	Utils.send_message(player, " ");
    	Utils.send_message(player, WEST_AUTUMNPORT_INFO);
    	Utils.send_message(player, FELLFRIN_INFO);
    	Utils.send_message(player, SORRENTO_INFO);
    	Utils.send_message(player, WEST_NASH_INFO);
    	Utils.send_message(player, SOUTH_NASH_INFO);
    }
    
    private void updateToBuilder(final Player applicant){
    	Utils.send_message(applicant, MESSAGE_BUILDING);
    	Application application = applications.get(applicant.getUniqueId());
    	application.setState("building");
        application.changeFileState("building");
    	Bukkit.getScheduler().runTask(getServer().getPluginManager().getPlugin("NashboroughPlugin"), new Runnable() {
        	  public void run() {
        		applicant.setGameMode(GameMode.SURVIVAL);
        	  }
        	});
    }
}
