package to.us.nashboroughmc.nashboroughplugin.listeners;

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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;
import net.alex9849.arm.events.PreBuyEvent;
import net.alex9849.arm.events.RegionEvent;
import net.alex9849.arm.events.RemoveRegionEvent;
import net.alex9849.arm.events.UnsellRegionEvent;
import net.alex9849.arm.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import to.us.nashboroughmc.nashboroughplugin.NashboroughPlugin;
import to.us.nashboroughmc.nashboroughplugin.Utils;
import to.us.nashboroughmc.nashboroughplugin.models.Application;
import to.us.nashboroughmc.nashboroughplugin.models.Build;

/**
 *
 * @author Jacob and Greg
 */
public class ApplicationListener implements Listener {
	private static final String COMMAND_START_BUILD = "startbuild";
	private static final String COMMAND_SUBMIT_BUILD= "submitbuild";
	private static final String COMMAND_REVIEW_LIST = "reviewlist";
	private static final String COMMAND_REVIEW_LOG  = "reviewlog";
	private static final String COMMAND_REVIEW_BUILD= "reviewbuild";
	private static final String COMMAND_APPROVE_BUILD= "approvebuild";
	private static final String COMMAND_REJECT_BUILD= "rejectbuild";

	private static final String MESSAGE_WELCOME = "Welcome to the Nashborough Server! Feel free to explore the city, and " +
			"when you're ready to get started, use the /startbuild command.";
	private static final String MESSAGE_PENDING   = "Looks like you have a build pending review. We will get to it as soon as possible.";
	private static final String MESSAGE_REGION_REMOVED = "Your region was removed or sold. Please pick a new plot to continue with your application.";
	private static final String MESSAGE_SELECTING = "You've entered the plot selection phase. Find an open plot for sale, then right click to claim the one you like.";
	private static final String MESSAGE_BUILDING  = "You're in the building stage of your membership. We use this to make sure you can be trusted and meet good building standards. When you complete your build, use /submitbuild so a moderator can review your build. Make sure your build matches its surroundings, but don't be afraid to be creative as well.";
	private static final String MESSAGE_BUILD_APPROVAL = "Your build has been approved! You can now build freely in Ashton and Durham as an entry-level builder until you're promoted to an advanced builder. To visit these cities, use /warp Durham or /warp Ashton.";
	private static final String MESSAGE_BUILD_REJECTION = "Your build has not been approved at this time."
			+ " The mods encourage you to keep working on it and resubmit it at a later time.";
    
    private final HashMap<UUID, Application> applications;
    private final HashMap<String, Application> applicationsByRegion;
    private final HashMap<String, Application> applicationsByName;
    private final NashboroughPlugin plugin;
    private final HashMap<Player, UUID> reviewingPlayers = new HashMap<Player, UUID>();

    public static ArrayList<String> submittedPlayerList = new ArrayList<String>();
	public static HashMap<String, Build> submittedBuilds = new HashMap<String, Build>();
	public static HashMap<String, Build> reviewedBuilds = new HashMap<String, Build>();
    
    public ApplicationListener(NashboroughPlugin plugin) {
    	this.plugin = plugin;
        applications = new HashMap<UUID, Application>();
		applicationsByRegion = new HashMap<String, Application>();
		applicationsByName = new HashMap<String, Application>();
        loadSavedApplications();
        loadSubmittedBuilds();
        loadApprovedBuilds();
    }
    
    @EventHandler
    public void onJoinEvent(PlayerJoinEvent ev) {
        Player player = ev.getPlayer();
        if (!player.isOp()) {
			if (applications.containsKey(player.getUniqueId())) {
				Application application = applications.get(player.getUniqueId());
				switch (application.getState()) {
					case "pending":
						Utils.send_message(player, MESSAGE_PENDING);
						break;
					case "selecting":
						Utils.send_message(player, MESSAGE_SELECTING);
						break;
					case "building":
						Utils.send_message(player, MESSAGE_BUILDING);
						break;
				}
			} else {
				Utils.send_message(player, MESSAGE_WELCOME);
			}
		}

        // Send mods update messages on login.
        if (player.isOp()) {
			if (!submittedPlayerList.isEmpty()) {
				player.sendMessage(ChatColor.DARK_AQUA+"[MOD MESSAGE] "+ChatColor.AQUA+"New builds submitted for approval. Use /reviewlist");
			}
        }

		if (reviewedBuilds.containsKey(player.getDisplayName())){
			if (!reviewedBuilds.get(player.getDisplayName()).isAlerted()){
				if (reviewedBuilds.get(player.getDisplayName()).getState().equals("approved")){
					updateToCreative(player);
				} else if (reviewedBuilds.get(player.getDisplayName()).getState().equals("rejected")){
					player.sendMessage(ChatColor.DARK_AQUA+"[MESSAGE FROM THE MODS] "+ChatColor.AQUA+ MESSAGE_BUILD_REJECTION);
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
	public void onBuyRegionEvent(PreBuyEvent event) {
    	// Get the player's application, set the region ID, then set the application state to building.
    	Player player = event.getBuyer();
    	Region region = event.getRegion();
    	Application application = getApplicationForPlayer(player);
    	if (application == null || !application.getState().equals("selecting")) {
    		return;
		}
    	application.setRegionId(region.getRegion().getId());
		updateToBuilder(player);
		applicationsByRegion.put(application.getRegionId(), application);
	}

	@EventHandler
	public void onUnsellRegionEvent(UnsellRegionEvent event) {
    	handleRegionRemoveEvent(event);
	}

	@EventHandler
	public void onRemoveRegionEvent(RemoveRegionEvent event) {
    	handleRegionRemoveEvent(event);
	}

	private void handleRegionRemoveEvent(RegionEvent event) {
    	// Get the application the region is associated with.
		Application application = getApplicationForRegion(event.getRegion().getRegion().getId());
		if (application == null || application.isComplete()) {
			return;
		}
		applicationsByRegion.remove(application.getRegionId());
		application.setRegionId(null);
		application.setState("selecting");
		// Also make sure there are no submitted builds for review.
		if (submittedBuilds.containsKey(application.getUsername())) {
			Build build = submittedBuilds.get(application.getUsername());
			build.retract();
			submittedBuilds.remove(application.getUsername());
		}
		if(isOnline(application.getUsername())){
			Player onlinePlayer = getPlayer(application.getUsername());
			Utils.send_message(onlinePlayer, MESSAGE_REGION_REMOVED);
		}
		application.changeFileState();
	}

	public boolean handleCommand(Player player, String command, String[] args) {
    	String playerName;
    	Player reviewer;
    	Application application;
        switch(command) {
			case COMMAND_START_BUILD:
				application = getApplicationForPlayer(player);
				if(application != null) {
					switch(application.getState()) {
						case "pending":
							Utils.send_message(player, MESSAGE_PENDING);
							break;
						case "selecting":
							Utils.send_message(player, MESSAGE_SELECTING);
							break;
						case "building":
							Utils.send_message(player, MESSAGE_BUILDING);
							break;
					}
				} else {
					// Create a new application, set player into selecting state.
					application = new Application(player);
					applications.put(player.getUniqueId(), application);
					applicationsByName.put(player.getDisplayName(), application);
					application.setState("selecting");
					application.submit();
					int x = plugin.getConfig().getInt("location-x");
					int y = plugin.getConfig().getInt("location-y");
					int z = plugin.getConfig().getInt("location-z");
					player.teleport(new Location(Bukkit.getWorld("world"), x, y, z));
					Utils.send_message(player, MESSAGE_SELECTING);
				}
				break;
            case COMMAND_SUBMIT_BUILD:
				application = getApplicationForPlayer(player);
				if (application.getRegionId() == null) {
					player.sendMessage(ChatColor.AQUA+"You still haven't selected a plot for your build.");
					break;
				}
				if(!submittedPlayerList.contains(player.getDisplayName())){
					submittedPlayerList.add(player.getDisplayName());
				}
    			application.setState("pending");
    			application.changeFileState();
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
    			for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
					if (onlinePlayer.isOp()){
						onlinePlayer.sendMessage(ChatColor.DARK_AQUA+"[MOD MESSAGE] "
								+ChatColor.AQUA+"New builds submitted for approval. Use /reviewlist");
					}
				}
    			break;
            case COMMAND_REVIEW_LIST:
            	player.sendMessage(ChatColor.WHITE+"----- "+ChatColor.DARK_AQUA+"Builds Submitted for Review"+ChatColor.WHITE+" -----");
    			if(submittedBuilds.isEmpty()){
    				player.sendMessage(ChatColor.AQUA+" No builds");
    			}else {
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
    			reviewer = player;
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
    			reviewer = player;
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
					Bukkit.broadcastMessage(ChatColor.AQUA+playerName+" has been accepted to the server! The world rejoices.");
    				approved_build.changeFileState("submitted");
    				reviewedBuilds.put(playerName, approved_build);
					addPlayerToEntryLevel(playerName);
    				return true;
    			} else {
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
    			if(submittedBuilds.containsKey(playerName)){
					application = getApplicationForPlayerName(playerName);
					application.setState("building");
					application.changeFileState();
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
    					online_player.sendMessage(ChatColor.DARK_AQUA+"[MESSAGE FROM THE MODS] "+ChatColor.AQUA+MESSAGE_BUILD_REJECTION);
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

    private Application getApplicationForPlayer(Player player) {
        return applications.get(player.getUniqueId());
    }

    private Application getApplicationForPlayerName(String playerName) {
    	return applicationsByName.get(playerName);
	}

    private Application getApplicationForRegion(String regionId) {
    	return applicationsByRegion.get(regionId);
	}

    private void loadSavedApplications() {
    	JsonParser parser = new JsonParser();
    	JsonObject jsonObject = null;

    	try {
    		Object obj = parser.parse(new FileReader(NashboroughPlugin.APPLICATION_JSON_PATH));
    		jsonObject = (JsonObject) obj;
    	} catch (IOException|JsonParseException e) {
    		e.printStackTrace();
    	}

		for (Map.Entry<String, JsonElement> entry: jsonObject.entrySet()) {
			String id = entry.getKey();
			JsonObject player = jsonObject.getAsJsonObject(id);
			Application app = new Application(player.get("state").getAsString());
			app.setUsername(player.get("username").getAsString());
			if (!player.get("regionId").isJsonNull()) {
				app.setRegionId(player.get("regionId").getAsString());
			}
			String string = player.get("UUID").getAsString();
			UUID uuid = java.util.UUID.fromString(string);
			app.setUUID(uuid);
			applications.put(uuid, app);
			applicationsByRegion.put(app.getRegionId(), app);
			applicationsByName.put(app.getUsername(), app);
		}
    }
    
    private void loadSubmittedBuilds() {
    	JsonParser parser = new JsonParser();
    	JsonObject jsonObject = null;
    	 
    	try {
    		Object obj = parser.parse(new FileReader(NashboroughPlugin.SUBMITTED_BUILDS_JSON_PATH));
    		jsonObject = (JsonObject) obj;
    	} catch (IOException|JsonParseException e) {
    		e.printStackTrace();
    	}

		for (Map.Entry<String, JsonElement> entry: jsonObject.entrySet()) {
			String uuid = entry.getKey();
			JsonObject build_obj = (JsonObject) jsonObject.get(uuid);
			Build build = new Build(build_obj.get("state").getAsString());
			Location loc = new Location(
					Bukkit.getWorld("world"),
					(build_obj.get("x").getAsLong()),
					(build_obj.get("y").getAsLong()),
					(build_obj.get("z").getAsLong()));
			SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
			Date date = new Date();
			try {
				date = formatter.parse(build_obj.get("timestamp").getAsString());
			} catch (java.text.ParseException e) {
				e.printStackTrace();
			}
			
			build.setUUID(UUID.fromString(build_obj.get("UUID").getAsString()));
			build.setLocation(loc);
			build.setReviewer(build_obj.get("reviewer").getAsString());
			build.setTimestamp(date);
			build.setUsername(build_obj.get("username").getAsString());
			
			submittedBuilds.put(build_obj.get("username").getAsString(), build);
			if (build.getState().equals("submitted")){
				submittedPlayerList.add(build_obj.get("username").getAsString());
			}
			
		}
    }
    
    private void loadApprovedBuilds() { 
    	JsonParser parser = new JsonParser();
    	JsonObject jsonObject = null;
    	 
    	try {
    		Object obj = parser.parse(new FileReader(NashboroughPlugin.APPROVED_BUILDS_JSON_PATH));
    		jsonObject = (JsonObject) obj;
    	} catch (IOException| JsonParseException e) {
    		e.printStackTrace();
    	}

		for (Map.Entry<String, JsonElement> entry: jsonObject.entrySet()) {
			String uuid = entry.getKey();
			JsonObject build_obj = (JsonObject) jsonObject.get(uuid);
			Build build = new Build(build_obj.get("state").getAsString());
			Location loc = new Location(
					Bukkit.getWorld("world"),
					(build_obj.get("x").getAsLong()),
					(build_obj.get("y").getAsLong()),
					(build_obj.get("z").getAsLong()));
			SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
			Date date = new Date();
			try {
				date = formatter.parse(build_obj.get("timestamp").getAsString());
			} catch (java.text.ParseException e) {
				e.printStackTrace();
			}
			
			build.setUUID(UUID.fromString(build_obj.get("UUID").getAsString()));
			build.setLocation(loc);
			build.setReviewer(build_obj.get("reviewer").getAsString());
			build.setTimestamp(date);
			build.setUsername(build_obj.get("username").getAsString());
			build.setAlerted(build_obj.get("alerted").getAsBoolean());
			
			reviewedBuilds.put(build_obj.get("username").getAsString(), build);
			
		}
    }
    
    private void updateToBuilder(final Player applicant){
    	Utils.send_message(applicant, MESSAGE_BUILDING);
    	Application application = applications.get(applicant.getUniqueId());
    	application.setState("building");
        application.changeFileState();
    }
    
    private void updateToCreative(final Player player){
    	player.sendMessage(ChatColor.DARK_AQUA+"[MESSAGE FROM THE MODS] "+ChatColor.AQUA+MESSAGE_BUILD_APPROVAL);
		Application app = applications.get(player.getUniqueId());
		app.setState("completed");
		app.changeFileState();
    }

    private void addPlayerToEntryLevel(String playerName) {
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + playerName + " parent add entry-builders");
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
