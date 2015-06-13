/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package to.us.nashboroughmc.nashboroughplugin.models;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
    	JSONParser parser = new JSONParser();
    	JSONObject jsonObject = null;
    	try {
			jsonObject = (JSONObject) parser.parse(new FileReader(NashboroughPlugin.SUBMITTED_BUILDS_JSON_PATH));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		};
    	JSONObject obj = new JSONObject();
    	obj.put("UUID", getUUID().toString());
    	obj.put("username", getUsername());
    	obj.put("state",getState());
    	obj.put("x", (int)getLocation().getX());
    	obj.put("y", (int)getLocation().getY());
    	obj.put("z", (int)getLocation().getZ());
    	obj.put("timestamp", getTimestamp().toString());
    	obj.put("reviewer", getReviewer());
    	obj.put("alerted", isAlerted());
    	
    	jsonObject.put(getUUID().toString(),obj);
    	FileWriter file;
		try {
			file = new FileWriter(NashboroughPlugin.SUBMITTED_BUILDS_JSON_PATH);
			file.write(jsonObject.toJSONString());
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
				JSONParser parser = new JSONParser();
		    	JSONObject jsonObject = null;
		    	JSONObject jsonObjectW = null;
		    	try {
		    		if (fileloc.equals("submitted")){
		    			jsonObject = (JSONObject) parser.parse(new FileReader(NashboroughPlugin.SUBMITTED_BUILDS_JSON_PATH));
		    			jsonObjectW = (JSONObject) parser.parse(new FileReader(NashboroughPlugin.APPROVED_BUILDS_JSON_PATH));
		    		}
		    		else if (fileloc.equals("reviewed")){
		    			jsonObject = (JSONObject) parser.parse(new FileReader(NashboroughPlugin.APPROVED_BUILDS_JSON_PATH));
		    		}
		    		
				} catch (IOException | ParseException e) {
					e.printStackTrace();
				};
				JSONObject playerobj = (JSONObject) jsonObject.get(StateUUID);
				playerobj.put("state", getState());
				if (getState().equals("approved") || getState().equals("rejected")){
					playerobj.put("timestamp", getTimestamp().toString());
					playerobj.put("reviewer", getReviewer());
					playerobj.put("alerted", isAlerted());
				}
				if (fileloc.equals("submitted")){
					jsonObjectW.put(StateUUID, playerobj);
				} else {
					jsonObject.put(StateUUID, playerobj);
				}
				FileWriter file;
				try {					
					file = new FileWriter(NashboroughPlugin.APPROVED_BUILDS_JSON_PATH);
					if (fileloc.equals("submitted")){
						file.write(jsonObjectW.toJSONString());
					} else {
						file.write(jsonObject.toJSONString());
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
						file.write(jsonObject.toJSONString());
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
