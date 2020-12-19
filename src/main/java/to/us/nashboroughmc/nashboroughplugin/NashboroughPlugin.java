package to.us.nashboroughmc.nashboroughplugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import to.us.nashboroughmc.nashboroughplugin.listeners.ApplicationListener;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import com.google.gson.JsonObject;


public class NashboroughPlugin extends JavaPlugin {
    
    private ApplicationListener applicationListener;
    public static final String APPLICATION_JSON_PATH = "plugins/ApplicationPlugin/applications.json";
    public static final String SUBMITTED_BUILDS_JSON_PATH = "plugins/ApplicationPlugin/submitted_builds.json";
    public static final String APPROVED_BUILDS_JSON_PATH = "plugins/ApplicationPlugin/approved_builds.json";
    public static final String CITIES_JSON_PATH = "plugins/ApplicationPlugin/cities.json";
    
    @Override 
    public void onEnable() {
        
        
        File directory = new File("plugins/ApplicationPlugin");
		if (!directory.exists()){
			new File("plugins/ApplicationPlugin").mkdir();
			Bukkit.getLogger().info("Creating new directory");
		}
		
        File applications = new File(APPLICATION_JSON_PATH);
        File submitted_builds = new File(SUBMITTED_BUILDS_JSON_PATH);
        File approved_builds = new File(APPROVED_BUILDS_JSON_PATH);
        File cities = new File(CITIES_JSON_PATH);
        
        if(!applications.exists()) { //If applications.json does not exist
        	JsonObject jsonObject = new JsonObject();
			try {
				FileWriter file = new FileWriter(APPLICATION_JSON_PATH);
				file.write(jsonObject.toString());
	    		file.flush();
	    		file.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
        }
        if(!submitted_builds.exists()) {
        	JsonObject jsonObject = new JsonObject();
			try {
				FileWriter file = new FileWriter(SUBMITTED_BUILDS_JSON_PATH);
				file.write(jsonObject.toString());
	    		file.flush();
	    		file.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
        }
        if(!approved_builds.exists()) {
        	JsonObject jsonObject = new JsonObject();
			try {
				FileWriter file = new FileWriter(APPROVED_BUILDS_JSON_PATH);
				file.write(jsonObject.toString());
	    		file.flush();
	    		file.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
        }
        if(!approved_builds.exists()) {
        	JsonObject jsonObject = new JsonObject();
			try {
				FileWriter file = new FileWriter(CITIES_JSON_PATH);
				file.write(jsonObject.toString());
	    		file.flush();
	    		file.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
        }
        
        applicationListener = new ApplicationListener();
        getServer().getPluginManager().registerEvents(applicationListener, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if(sender instanceof Player) {
            Player player = (Player)sender;
            
            if(applicationListener.handleCommand(player, command.getName(), args)) {
                return true;
            }
        }
        
        return false;
    }
}
