package to.us.nashboroughmc.nashboroughplugin.models;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;

import to.us.nashboroughmc.nashboroughplugin.NashboroughPlugin;

/**
 *
 * @author Greg
 */

public class Build {
	private String username;
	private UUID uuid;
	private Location location;
	private Date timestamp;
	private String state;
	private String reviewer;
	private boolean alerted = false;
	
	public Build(Player player){
		this.uuid = player.getUniqueId();
		this.username = player.getDisplayName();
	}
	public Build(String state){
		this.state = state;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public UUID getUUID() {
		return uuid;
	}
	
	public void setUUID(UUID uuid) {
		this.uuid = uuid;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	
	public String getState() {
		return state;
	}
	
	public void setState(String state) {
		this.state = state;
	}
	
	public String getReviewer() {
		return reviewer;
	}
	
	public void setReviewer(String reviewer) {
		this.reviewer = reviewer;
	}

	public boolean isAlerted() {
		return alerted;
	}
	public void setAlerted(boolean alerted) {
		this.alerted = alerted;
	}
	
	@SuppressWarnings("unchecked")
	public void submit() {
    	JsonParser parser = new JsonParser();
    	JsonObject jsonObject = null;
    	try {
			jsonObject = (JsonObject) parser.parse(new FileReader(NashboroughPlugin.SUBMITTED_BUILDS_JSON_PATH));
		} catch (IOException | JsonParseException e) {
			e.printStackTrace();
		};
    	JsonObject obj = new JsonObject();
    	obj.addProperty("UUID", getUUID().toString());
    	obj.addProperty("username", getUsername());
    	obj.addProperty("state",getState());
    	obj.addProperty("x", (int)getLocation().getX());
    	obj.addProperty("y", (int)getLocation().getY());
    	obj.addProperty("z", (int)getLocation().getZ());
    	obj.addProperty("timestamp", getTimestamp().toString());
    	obj.addProperty("reviewer", getReviewer());
    	obj.addProperty("alerted", isAlerted());
    	
    	jsonObject.add(getUUID().toString(), obj);
    	FileWriter file;
		try {
			file = new FileWriter(NashboroughPlugin.SUBMITTED_BUILDS_JSON_PATH);
			file.write(jsonObject.toString());
    		file.flush();
    		file.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
    }
	
	public void changeFileState(final String fileloc){
    	final String StateUUID = getUUID().toString();
    	new Thread(new Runnable(){

			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				JsonParser parser = new JsonParser();
		    	JsonObject jsonObject = null;
		    	JsonObject jsonObjectW = null;
		    	try {
		    		if (fileloc.equals("submitted")){
		    			jsonObject = (JsonObject) parser.parse(new FileReader(NashboroughPlugin.SUBMITTED_BUILDS_JSON_PATH));
		    			jsonObjectW = (JsonObject) parser.parse(new FileReader(NashboroughPlugin.APPROVED_BUILDS_JSON_PATH));
		    		}
		    		else if (fileloc.equals("reviewed")){
		    			jsonObject = (JsonObject) parser.parse(new FileReader(NashboroughPlugin.APPROVED_BUILDS_JSON_PATH));
		    		}
		    		
				} catch (IOException | JsonParseException e) {
					e.printStackTrace();
				};
				JsonObject playerobj = (JsonObject) jsonObject.get(StateUUID);
				playerobj.addProperty("state", getState());
				if (getState().equals("approved") || getState().equals("rejected")){
					playerobj.addProperty("timestamp", getTimestamp().toString());
					playerobj.addProperty("reviewer", getReviewer());
					playerobj.addProperty("alerted", isAlerted());
				}
				if (fileloc.equals("submitted")){
					jsonObjectW.add(StateUUID, playerobj);
				} else {
					jsonObject.add(StateUUID, playerobj);
				}
				FileWriter file;
				try {					
					file = new FileWriter(NashboroughPlugin.APPROVED_BUILDS_JSON_PATH);
					if (fileloc.equals("submitted")){
						file.write(jsonObjectW.toString());
					} else {
						file.write(jsonObject.toString());
					}
		    		file.flush();
		    		file.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				if (fileloc.equals("submitted")){
					jsonObject.remove(StateUUID);
					try {					
						file = new FileWriter(NashboroughPlugin.SUBMITTED_BUILDS_JSON_PATH);
						file.write(jsonObject.toString());
			    		file.flush();
			    		file.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
        	
        }).start();
    }
}
