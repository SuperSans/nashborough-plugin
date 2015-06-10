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
	
	public void changeFileState(final String state){
    	final String StateUUID = getUUID().toString();
    	new Thread(new Runnable(){

			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				JSONParser parser = new JSONParser();
		    	JSONObject jsonObject = null;
		    	try {
					jsonObject = (JSONObject) parser.parse(new FileReader(NashboroughPlugin.SUBMITTED_BUILDS_JSON_PATH));
				} catch (IOException | ParseException e) {
					e.printStackTrace();
				};
				JSONObject playerobj = (JSONObject) jsonObject.get(StateUUID);
				playerobj.put("state", state);
				jsonObject.put(StateUUID, playerobj);
				FileWriter file;
				try {
					if (state.equals("approved")){
						file = new FileWriter(NashboroughPlugin.APPROVED_BUILDS_JSON_PATH);
					} else {
						file = new FileWriter(NashboroughPlugin.SUBMITTED_BUILDS_JSON_PATH);
					}
					file.write(jsonObject.toJSONString());
		    		file.flush();
		    		file.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
			}
        	
        }).start();
    }

}
