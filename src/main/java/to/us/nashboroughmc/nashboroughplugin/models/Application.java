/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package to.us.nashboroughmc.nashboroughplugin.models;

<<<<<<< HEAD
import java.io.BufferedWriter;
=======
>>>>>>> c882aed3b3537e669125bba5caca44816ef63994
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Jacob and Greg
 */
public class Application {
    
<<<<<<< HEAD
    public static enum State {
        STARTED, PENDING, DENIED, ACCEPTED, COUNTRY, AGE, EXPERIENCE, ALBUM
    }
    
=======
>>>>>>> c882aed3b3537e669125bba5caca44816ef63994
    private String state;
    private UUID   uuid;
    private String username;
    private String age;
    private String country;
    private String experience;
    private String album;
    
    public Application(String string) {
        this.state = string;
    }
    
    public Application(Player player) {
        this.uuid     = player.getUniqueId();
        this.username = player.getDisplayName();
        
        this.state = "started";
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

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }
    
    @SuppressWarnings("unchecked")
	public void submit() {
    	JSONParser parser = new JSONParser();
    	JSONObject jsonObject = null;
    	try {
			jsonObject = (JSONObject) parser.parse(new FileReader("applications.json"));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		};
    	JSONObject obj = new JSONObject();
    	obj.put("UUID", getUUID().toString());
    	obj.put("username", getUsername());
    	obj.put("country", getCountry());
    	obj.put("age", getAge());
    	obj.put("experience", getExperience());
    	obj.put("album", getAlbum());
<<<<<<< HEAD
=======
    	obj.put("state",getState());
>>>>>>> c882aed3b3537e669125bba5caca44816ef63994
    	jsonObject.put(getUUID().toString(),obj);
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
<<<<<<< HEAD
        /*BufferedWriter writer = null;
        
        
        try {
            File applicationsFile = new File("pending_applications.txt");
            
            writer = new BufferedWriter(new FileWriter(applicationsFile, true));
            
            writeLine(writer, uuid.toString());
            writeLine(writer, username);
            writeLine(writer, country);
            writeLine(writer, age);
            writeLine(writer, experience);
            writeLine(writer, album);
            
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch(Exception e) {
            }
        }
    }
    
    private void writeLine(BufferedWriter writer, String text) throws IOException {
        writer.write(text);
        writer.newLine();
    }*/
=======
>>>>>>> c882aed3b3537e669125bba5caca44816ef63994
}
