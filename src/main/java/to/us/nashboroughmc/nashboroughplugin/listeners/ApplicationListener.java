/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package to.us.nashboroughmc.nashboroughplugin.listeners;

import static org.bukkit.Bukkit.getServer;

import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import to.us.nashboroughmc.nashboroughplugin.NashboroughPlugin;
import to.us.nashboroughmc.nashboroughplugin.Utils;
import to.us.nashboroughmc.nashboroughplugin.models.Application;
import to.us.nashboroughmc.nashboroughplugin.models.Build;

/**
 *
 * @author Jacob and Greg
 */
public class ApplicationListener implements Listener {
    
    private static final String COMMAND_APPLY       = "apply";
    private static final String COMMAND_REVIEW_APPS = "reviewapps";
    private static final String COMMAND_SUBMIT_BUILD= "submitbuild";
    private static final String COMMAND_REVIEW_LIST = "reviewlist";
    private static final String COMMAND_REVIEW_LOG  = "reviewlog";
    private static final String COMMAND_REVIEW_BUILD= "reviewbuild";
    private static final String COMMAND_APPROVE_BUILD= "approvebuild";
    private static final String COMMAND_REJECT_BUILD= "rejectbuild";
    
    
    private static final String MESSAGE_PENDING   = "Looks like you have an application pending. We will get to it as soon as possible.";
    private static final String MESSAGE_ACCEPTED  = "Congratulations! Your application has been accepted.";
    private static final String MESSAGE_DENIED    = "Sorry, your application was denied.";
    private static final String MESSAGE_SELECTING = "Now that your preliminary application has been accepted, you must choose where to build your home.";
    private static final String MESSAGE_BUILDING  = "You're in the building stage of your membership. We have given you some basic supplies to assist you. When your survival build is completed, use the /submitbuild command and a moderator will review your build.";
    
    private static final String WEST_AUTUMNPORT_INFO   = "Autumnport: West of the coastal town, Autumnport.";
    private static final int[]  WEST_AUTUMNPORT_COORDS = {106, 69, -498};
    
    private static final String FELLFRIN_INFO   = "Fellfrin: A survival-styled village featuring cabins and cottages.";
    private static final int[]  FELLFRIN_COORDS = {-224, 72, -311};
    
    private static final String SORRENTO_INFO   = "Sorrento: A suburb connecting Autumnport and Nashborough.";
    private static final int[]  SORRENTO_COORDS = {525, 72, -321};
    
    private static final String WEST_NASH_INFO   = "West Nashborough: An underdeveloped suburb in need of development.";
    private static final int[]  WEST_NASH_COORDS = {-245, 64, 145};
    
    private static final String SOUTH_NASH_INFO   = "South Nashborough: An underdeveloped suburb with a farming community south of it.";
    private static final int[]  SOUTH_NASH_COORDS = {-65, 67, 429};
    
    private static final ItemStack[] items = {
    	new ItemStack(Material.BREAD, 64), 
    	new ItemStack(Material.BED, 3), 
    	new ItemStack(Material.IRON_AXE, 1), 
    	new ItemStack(Material.IRON_SPADE, 1),
    	new ItemStack(Material.IRON_PICKAXE, 1),
    	new ItemStack(Material.IRON_SPADE, 1),
    	new ItemStack(Material.WOOL, 16),
    	new ItemStack(Material.LOG, 256),
    	new ItemStack(Material.BRICK, 128),
    	new ItemStack(Material.SAND, 64),
    	new ItemStack(Material.COAL, 64),
    	new ItemStack(Material.TORCH, 32),
    };
    
    
    private final HashMap<UUID, Application> applications;
    private HashMap<UUID, Application> pendingApplications;
    private HashMap<Player, UUID> reviewingPlayers = new HashMap<Player, UUID>();
    private HashMap<Player, UUID> selectingPlayers = new HashMap<Player, UUID>();
    
    //IMPORTED FROM BUILD REVIEW PLUGIN
    public static ArrayList<String> submittedPlayerList = new ArrayList<String>();
	public static HashMap<String, Build> submittedBuilds = new HashMap<String, Build>();
	public static HashMap<String, Build> reviewedBuilds = new HashMap<String, Build>();
	
	public static String approvalMessage = "Your build has been approved!";
	public static String rejectionMessage = "Your build has not been approved at this time."
			+ " The mods encourage you to keep working on it and resubmit it at a later time.";
    //
    
    public ApplicationListener() {
        applications = new HashMap<UUID, Application>();
        loadSavedApplications();
        pendingApplications = getPendingApplications();
        loadSubmittedBuilds();
        loadApprovedBuilds();
        
    }
    
    @EventHandler
    public void onChatEvent(AsyncPlayerChatEvent ev) {
        Player player  = ev.getPlayer();
        String message = ev.getMessage();
        for (Player onlineplayer : Bukkit.getServer().getOnlinePlayers()){
        	if (applications.containsKey(onlineplayer.getUniqueId())){
        		Application application = applications.get(onlineplayer.getUniqueId());
        		String[] states = {"pending","accepted","completed","selecting","building"};
        		if (!ArrayUtils.contains(states, application.getState())){
        			ev.getRecipients().remove(onlineplayer);
        		}
        	} else {
        		ev.getRecipients().remove(onlineplayer);
        	}
        }
        
        //Cancel message if it is handled
        if(handleMessage(player, message)) {
            ev.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onJoinEvent(PlayerJoinEvent ev) {
        Player player = ev.getPlayer();
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
                Utils.send_message(player, "Applications awaiting review: " + pendingApplications.size());
                Utils.send_message(player, "Use \"/reviewapps\" to review them");
            }
        }
        
        if(player.isOp() && !submittedPlayerList.isEmpty()){ //Imported from Build Review Plugin
			player.sendMessage(ChatColor.DARK_AQUA+"[MOD MESSAGE] "+ChatColor.AQUA+"New builds submitted for approval. Use /reviewlist");
		}
		if(reviewedBuilds.containsKey(player.getDisplayName())){
			if(!reviewedBuilds.get(player.getDisplayName()).isAlerted()){
				if (reviewedBuilds.get(player.getDisplayName()).getState().equals("approved")){
					updateToCreative(player);
				} else if(reviewedBuilds.get(player.getDisplayName()).getState().equals("rejected")){
					player.sendMessage(ChatColor.DARK_AQUA+"[MESSAGE FROM THE MODS] "+ChatColor.AQUA+rejectionMessage);
				}
				reviewedBuilds.get(player.getDisplayName()).setAlerted(true);
				reviewedBuilds.get(player.getDisplayName()).changeFileState("reviewed");
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
    
    @EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		if(event.getPlayer().getGameMode() == GameMode.ADVENTURE){
			if(event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK){
				event.setCancelled(true);
				event.getPlayer().sendMessage(ChatColor.RED+"You do not have permission for that. Apply to become an official member to build.");
			}
		}
	}
    
    public boolean handleCommand(Player player, String command, String[] args) {
    	
    	String playerName;
    	Player reviewer;
    	Date reviewDate;
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
            	ArrayList<Player> app_reviewers = new ArrayList<Player>();
            	if (reviewingPlayers.keySet().contains(player)){
            		reviewingPlayers.remove(player);
            	}
            	for (Player reviewingplayer : reviewingPlayers.keySet()){
            		app_reviewers.add(reviewingplayer);
            	}
                if(app_reviewers.size() == 1) {

                	Utils.send_message(player, app_reviewers.get(0).getDisplayName() + " is also reviewing applications at the moment.");
                	Utils.send_message(player, " ");
                }
                else if (app_reviewers.size() == 2){
                	Utils.send_message(player, app_reviewers.get(0).getDisplayName() + " and " + app_reviewers.get(1).getDisplayName() + " are also reviewing applications at the moment.");
                	Utils.send_message(player, " ");
                }
                else if (app_reviewers.size() > 2){
                	String message = "";
                	for (int i = 0; i < app_reviewers.size() - 2; i++){
                		message += app_reviewers.get(i).getDisplayName() + ", ";
                	}
                	message += "and " + app_reviewers.get(app_reviewers.size()-1).getDisplayName() + " are also reviewing applications at the moment.";

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
            case COMMAND_SUBMIT_BUILD:
    			if(!submittedPlayerList.contains(player.getDisplayName())){
    				submittedPlayerList.add(player.getDisplayName());
    			}
    			Build build = new Build(player);
    			build.setLocation(player.getLocation());
    			build.setReviewer("None");
    			build.setState("submitted");
    			build.setTimestamp(new Date());
    			build.setUsername(player.getDisplayName());
    			build.setUUID(player.getUniqueId());
    			build.submit();
    			submittedBuilds.put(player.getDisplayName(), build);
    			player.sendMessage(ChatColor.AQUA+"Your build has been submitted for approval");
    			for(int i = 0; i < Bukkit.getServer().getOnlinePlayers().length; i++){
    				if(Bukkit.getServer().getOnlinePlayers()[i].isOp()){
    					Bukkit.getServer().getOnlinePlayers()[i].sendMessage(ChatColor.DARK_AQUA+"[MOD MESSAGE] "
    							+ChatColor.AQUA+"New builds submitted for approval. Use /reviewlist");
    				}
    			}
    			
    			break;
            case COMMAND_REVIEW_LIST:
            	player.sendMessage(ChatColor.WHITE+"----- "+ChatColor.DARK_AQUA+"Builds Submitted for Review"+ChatColor.WHITE+" -----");
    			if(submittedBuilds.isEmpty()){
    				player.sendMessage(ChatColor.AQUA+" No builds");
    			}else{
	    			for(int i = 0; i < submittedPlayerList.size(); i++){
	    				player.sendMessage(ChatColor.WHITE+" - "+ChatColor.AQUA+submittedPlayerList.get(i));
	    			}
    			}
    			
    			break;
    			
            case COMMAND_REVIEW_LOG:
            	Object[] review_log = reviewedBuilds.values().toArray();
            	Arrays.sort(review_log, new Comparator<Object>(){
            	    public int compare(Object b1, Object b2) {
            	        return ((Build)b1).getTimestamp().compareTo(((Build)b2).getTimestamp());
            	    }
            	});
            	if(reviewedBuilds.isEmpty()){
    				player.sendMessage(ChatColor.WHITE+"----- "+ChatColor.DARK_AQUA+"Builds Reviewed (page 0/0)"+ChatColor.WHITE+" -----");
    				player.sendMessage(ChatColor.AQUA+" No builds");
    			}else{
    				String state;
    				if(args.length == 0){
    					Bukkit.getLogger().info(Integer.toString(reviewedBuilds.size()));
    					player.sendMessage(ChatColor.WHITE+"----- "+ChatColor.DARK_AQUA+"Builds Reviewed (page 1/"+(int)(Math.ceil((double)reviewedBuilds.size()/3))+")"+ChatColor.WHITE+" -----");
		    			for(int i = reviewedBuilds.size()-1; i >= ((reviewedBuilds.size()-3) + Math.abs(reviewedBuilds.size()-3))/2; i--){
		    				Build ind_build = (Build) review_log[i];
		    				if (ind_build.getState().equals("approved")){
		    					state = " approved ";
		    				} else {
		    					state = " rejected ";
		    				}
		    				player.sendMessage(ChatColor.WHITE+" - "+ChatColor.AQUA+ind_build.getReviewer()+state+ind_build.getUsername()+"'s build on "+ind_build.getTimestamp().toString()); //TODO: FIX FORMATTING
		    			}
    				}else{
    					int pageNum = Integer.parseInt(args[0]);
    					player.sendMessage(ChatColor.WHITE+"----- "+ChatColor.DARK_AQUA+"Builds Reviewed (page "+pageNum+"/"+(int)(Math.ceil((double)reviewedBuilds.size()/3))+")"+ChatColor.WHITE+" -----");
    					
    					for(int i = (reviewedBuilds.size()-1)-((pageNum-1)*3); i >= ((((reviewedBuilds.size()-3)-((pageNum-1)*3)) + Math.abs((reviewedBuilds.size()-3)-((pageNum-1)*3)))/2); i--){
    						Build ind_build = (Build) review_log[i];
		    				if (ind_build.getState().equals("completed") || ind_build.getState().equals("approved")){
		    					state = " approved ";
		    				} else {
		    					state = " rejected ";
		    				}
		    				player.sendMessage(ChatColor.WHITE+" - "+ChatColor.AQUA+ind_build.getReviewer()+state+ind_build.getUsername()+"'s build on "+ind_build.getTimestamp().toString());
		    			}
    				}
    			}
            	
            	break;
            case COMMAND_REVIEW_BUILD: 
            	if(args.length != 2){
    				player.sendMessage(ChatColor.RED+ "Usage: /reviewbuild [player name] [-i]");
    				return true;
    			}
    			playerName = args[0];
    			reviewer = (Player) player;
				if(submittedBuilds.containsKey(playerName)){
					if(args[1].equalsIgnoreCase("i")){
						if(isOnline(playerName)){
							player.sendMessage(ChatColor.LIGHT_PURPLE+" "+ChatColor.ITALIC+"You are now hidden from "+playerName);
							getPlayer(playerName).hidePlayer(reviewer);
						}
					}
					reviewer.teleport(submittedBuilds.get(playerName).getLocation());
    				player.sendMessage(ChatColor.DARK_AQUA+playerName+"'s Build");
				}else{
    				player.sendMessage(ChatColor.RED+ "Player does not exist.");
    				return true;
    			}		
    			break;
            
            case COMMAND_APPROVE_BUILD: 
            	if(args.length == 0){
    				player.sendMessage(ChatColor.RED+ "Usage: /approvebuild [player name]");
    				return true;
    			}
    			playerName = args[0];
    			reviewer = (Player) player;
    			reviewDate = new Date();
    			if(submittedBuilds.containsKey(playerName)){
    				player.sendMessage(ChatColor.DARK_AQUA+playerName+"'s build was approved");
    				Build approved_build = submittedBuilds.get(playerName);
    				approved_build.setState("approved");
    				approved_build.setReviewer(player.getDisplayName());
    				approved_build.setTimestamp(new Date());
    				submittedBuilds.remove(playerName);
    				submittedPlayerList.remove(playerName);
    				if(isOnline(playerName)){
    					Player online_player = getPlayer(playerName);
    					approved_build.setAlerted(true);
    					updateToCreative(online_player);
    					online_player.showPlayer(reviewer);
    					player.sendMessage(ChatColor.LIGHT_PURPLE+" "+ChatColor.ITALIC+"You are now visible to "+playerName);
    				}
    				approved_build.changeFileState("submitted");
    				reviewedBuilds.put(playerName, approved_build);
    				return true;
    			}else{
    				player.sendMessage(ChatColor.RED+ "Player does not exist.");
    				return true;
    			}
            case COMMAND_REJECT_BUILD:
            	if(args.length == 0){
    				player.sendMessage(ChatColor.RED+ "Usage: /rejectbuild [player name]");
    				return true;
    			}
    			playerName = args[0];
    			reviewer = (Player) player;
    			reviewDate = new Date();
    			if(submittedBuilds.containsKey(playerName)){
    				player.sendMessage(ChatColor.DARK_AQUA+playerName+"'s build was denied.");
    				Build rejected_build = submittedBuilds.get(playerName);
    				rejected_build.setState("rejected");
    				rejected_build.setTimestamp(new Date());
    				rejected_build.setReviewer(player.getDisplayName());
					submittedBuilds.remove(playerName);
    				submittedPlayerList.remove(playerName);
    				if(isOnline(playerName)){
    					Player online_player = getPlayer(playerName);
    					rejected_build.setAlerted(true);
    					online_player.sendMessage(ChatColor.DARK_AQUA+"[MESSAGE FROM THE MODS] "+ChatColor.AQUA+rejectionMessage);
    					online_player.showPlayer(reviewer);    					
    					player.sendMessage(ChatColor.LIGHT_PURPLE+" "+ChatColor.ITALIC+"You are now visible to "+playerName);
    				}
    				rejected_build.changeFileState("submitted");
    				reviewedBuilds.put(playerName, rejected_build);
    				return true;
    			}else{
    				player.sendMessage(ChatColor.RED+ "Player does not exist.");
    				return true;
    			}

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
        
        if(reviewingPlayers.containsKey(player)) {
            UUID applicationUUID = reviewingPlayers.get(player);
            if (pendingApplications.containsKey(applicationUUID)){
            	Application application = pendingApplications.get(applicationUUID);
                Player applicant = getServer().getPlayer(application.getUUID());
                switch(message.toLowerCase()) {
                    case "accept":
                        application.setState("accepted");
                        application.changeFileState();
                        if(applicant != null && applicant.isOnline()) {
                            alertAcceptance(applicant);
                        }
                        Bukkit.broadcastMessage(ChatColor.AQUA+application.getUsername()+" has been accepted to the server! The world rejoices.");
                        pendingApplications.remove(applicationUUID);
                        break;
                        
                    case "deny":
                        application.setState("denied");
                        application.changeFileState();
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
        if(applications.containsKey(player.getUniqueId())){
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
        
        //Messege cancelled, as the player has yet to apply
        Utils.send_message(player, "You must apply first in order to chat.");
        return true;
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
    		Object obj = parser.parse(new FileReader(NashboroughPlugin.APPLICATION_JSON_PATH));
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
    
    private void loadSubmittedBuilds() {
    	JSONParser parser = new JSONParser();
    	JSONObject jsonObject = null;
    	 
    	try {
    		Object obj = parser.parse(new FileReader(NashboroughPlugin.SUBMITTED_BUILDS_JSON_PATH));
    		jsonObject = (JSONObject) obj;
    	} catch (IOException|ParseException e) {
    		e.printStackTrace();
    	}
    	Object[] keys = jsonObject.keySet().toArray();

		for (Object uuid : keys){
			JSONObject build_obj = (JSONObject) jsonObject.get(uuid);
			Build build = new Build((String)build_obj.get("state"));
			Location loc = new Location(Bukkit.getWorld("world"),((Long)build_obj.get("x")).intValue(),((Long)build_obj.get("y")).intValue(),((Long)build_obj.get("z")).intValue());
			SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
			Date date = new Date();
			try {
				date = formatter.parse((String)build_obj.get("timestamp"));
			} catch (java.text.ParseException e) {
				e.printStackTrace();
			}
			
			build.setUUID((UUID)UUID.fromString((String)build_obj.get("UUID")));
			build.setLocation(loc);
			build.setReviewer((String)build_obj.get("reviewer"));
			build.setTimestamp(date);
			build.setUsername((String)build_obj.get("username"));
			
			submittedBuilds.put((String) (String)build_obj.get("username"), build);
			if (build.getState().equals("submitted")){
				submittedPlayerList.add((String) (String)build_obj.get("username"));
			}
			
		}
    }
    
    private void loadApprovedBuilds() { 
    	JSONParser parser = new JSONParser();
    	JSONObject jsonObject = null;
    	 
    	try {
    		Object obj = parser.parse(new FileReader(NashboroughPlugin.APPROVED_BUILDS_JSON_PATH));
    		jsonObject = (JSONObject) obj;
    	} catch (IOException|ParseException e) {
    		e.printStackTrace();
    	}
    	Object[] keys = jsonObject.keySet().toArray();

		for (Object uuid : keys){
			JSONObject build_obj = (JSONObject) jsonObject.get(uuid);
			Build build = new Build((String)build_obj.get("state"));
			Location loc = new Location(Bukkit.getWorld("world"),((Long)build_obj.get("x")).intValue(),((Long)build_obj.get("y")).intValue(),((Long)build_obj.get("z")).intValue());
			SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
			Date date = new Date();
			try {
				date = formatter.parse((String)build_obj.get("timestamp"));
			} catch (java.text.ParseException e) {
				e.printStackTrace();
			}
			
			build.setUUID((UUID)UUID.fromString((String)build_obj.get("UUID")));
			build.setLocation(loc);
			build.setReviewer((String)build_obj.get("reviewer"));
			build.setTimestamp(date);
			build.setUsername((String)build_obj.get("username"));
			build.setAlerted((boolean)build_obj.get("alerted"));
			
			reviewedBuilds.put((String) build_obj.get("username"), build);
			
		}
    }
    private void alertAcceptance(final Player applicant){
    	Utils.send_message(applicant, MESSAGE_ACCEPTED);
    	
        Application application = applications.get(applicant.getUniqueId());
        application.setState("selecting");
        application.changeFileState();
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
        application.changeFileState();
    	Bukkit.getScheduler().runTask(getServer().getPluginManager().getPlugin("NashboroughPlugin"), new Runnable() {
        	  public void run() {
        		applicant.setGameMode(GameMode.SURVIVAL);
        		applicant.getInventory().addItem(items);
        	  }
        	});
    }
    
    private void updateToCreative(final Player player){
    	player.sendMessage(ChatColor.DARK_AQUA+"[MESSAGE FROM THE MODS] "+ChatColor.AQUA+approvalMessage);
		Application app = applications.get(player.getUniqueId());
		app.setState("completed");
		app.changeFileState();
		Bukkit.getScheduler().runTask(getServer().getPluginManager().getPlugin("NashboroughPlugin"), new Runnable() {
        	  public void run() {
        		player.setGameMode(GameMode.CREATIVE);
        	  }
        	});
    }
    
    private boolean isOnline(String playername){
    	for (Player player : Bukkit.getServer().getOnlinePlayers()){
    		if (player.getDisplayName().equals(playername)){
    			return true;
    		}
    	}
    	return false;
    }
    private Player getPlayer(String playername){
    	for (Player player : Bukkit.getServer().getOnlinePlayers()){
    		if (player.getDisplayName().equals(playername)){
    			return player;
    		}
    	}
    	return null;
    }
}
