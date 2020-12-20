/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package to.us.nashboroughmc.nashboroughplugin.models;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

import org.bukkit.entity.Player;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;

import to.us.nashboroughmc.nashboroughplugin.NashboroughPlugin;

/**
 *
 * @author Jacob and Greg
 */
public class Application {
    private String state;
    private UUID   uuid;
    private String username;
    private String regionId;
    
    public Application(String string) {
        this.state = string;
    }
    
    public Application(Player player) {
        this.uuid     = player.getUniqueId();
        this.username = player.getDisplayName();
    }

    public String getState() {
        return state;
    }

    public void setState(String string) {
        this.state = string;
    }
    
    public UUID getUUID() {
        return uuid;
    }
    
    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

	public String getRegionId() {
		return regionId;
	}

	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	public boolean isComplete() {
    	return state.equals("completed") || state.equals("approved");
	}

	@SuppressWarnings("unchecked")
	public void submit() {
    	JsonParser parser = new JsonParser();
    	JsonObject jsonObject = null;
    	try {
			jsonObject = (JsonObject) parser.parse(new FileReader(NashboroughPlugin.APPLICATION_JSON_PATH));
		} catch (IOException | JsonParseException e) {
			e.printStackTrace();
		};
    	JsonObject obj = new JsonObject();
    	obj.addProperty("UUID", getUUID().toString());
    	obj.addProperty("username", getUsername());
    	obj.addProperty("state",getState());
    	jsonObject.add(getUUID().toString(),obj);
    	FileWriter file;
		try {
			file = new FileWriter(NashboroughPlugin.APPLICATION_JSON_PATH);
			file.write(jsonObject.toString());
    		file.flush();
    		file.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
    }
    
    public void changeFileState(){
    	final String StateUUID = getUUID().toString();
    	new Thread(new Runnable(){

			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				JsonParser parser = new JsonParser();
		    	JsonObject jsonObject = null;
		    	try {
					jsonObject = (JsonObject) parser.parse(new FileReader(NashboroughPlugin.APPLICATION_JSON_PATH));
				} catch (IOException | JsonParseException e) {
					e.printStackTrace();
				};
				JsonObject playerobj = (JsonObject) jsonObject.get(getUUID().toString());
				playerobj.addProperty("state", state);
				playerobj.addProperty("regionId", regionId);
				jsonObject.add(getUUID().toString(), playerobj);
				FileWriter file;
				try {
					file = new FileWriter(NashboroughPlugin.APPLICATION_JSON_PATH);
					file.write(jsonObject.toString());
		    		file.flush();
		    		file.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
			}
        	
        }).start();
    }
}
