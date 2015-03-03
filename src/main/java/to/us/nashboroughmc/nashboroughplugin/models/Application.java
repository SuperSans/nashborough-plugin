/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package to.us.nashboroughmc.nashboroughplugin.models;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import org.bukkit.entity.Player;

/**
 *
 * @author Jacob
 */
public class Application {
    
    public static enum State {
        STARTED, PENDING, DENIED, ACCEPTED, COUNTRY, AGE, EXPERIENCE, ALBUM
    }
    
    private State  state;
    private UUID   uuid;
    private String username;
    private String age;
    private String country;
    private String experience;
    private String album;
    private boolean isInformed;
    
    public Application() {}
    
    public Application(Player player) {
        
        this.uuid       = player.getUniqueId();
        this.username   = player.getDisplayName();
        this.state      = State.STARTED;
        this.isInformed = false;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
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
    
    public void setIsInformed(boolean isInformed) {
        this.isInformed = isInformed;
    }
    
    public boolean isInformed() {
        return isInformed;
    }
    
    public void submit() {
        BufferedWriter writer = null;
        
        try {
            File applicationsFile = new File("pending_applications.txt");
            
            writer = new BufferedWriter(new FileWriter(applicationsFile, true));
            
            writeLine(writer, uuid.toString());
            writeLine(writer, username);
            writeLine(writer, country);
            writeLine(writer, age);
            writeLine(writer, experience);
            writeLine(writer, album);
            writeLine(writer, String.valueOf(isInformed));
            
            switch(state) {
                case PENDING:  writeLine(writer, "pending");  break;
                case ACCEPTED: writeLine(writer, "accepted"); break;
                case DENIED:   writeLine(writer, "denied");   break;
                default:       writeLine(writer, "default");  break;
            }
            
            writer.newLine();
            
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
    }
}
