/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package to.us.nashboroughmc.nashboroughplugin.models;

import java.time.LocalDateTime;
import java.util.Date;
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
    
    private Date timeApplied;
    private Date timeReviewed;
    private String  reviewer;
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

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Date getTimeApplied() {
        return timeApplied;
    }

    public void setTimeApplied(Date timeApplied) {
        this.timeApplied = timeApplied;
    }

    public Date getTimeReviewed() {
        return timeReviewed;
    }

    public void setTimeReviewed(Date timeReviewed) {
        this.timeReviewed = timeReviewed;
    }

    public String getReviewer() {
        return reviewer;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }
    
    
    
    public void setIsInformed(boolean isInformed) {
        this.isInformed = isInformed;
    }
    
    public boolean isInformed() {
        return isInformed;
    }
}
