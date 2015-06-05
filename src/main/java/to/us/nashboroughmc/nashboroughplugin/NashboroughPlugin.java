/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package to.us.nashboroughmc.nashboroughplugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import to.us.nashboroughmc.nashboroughplugin.listeners.ApplicationListener;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;

public class NashboroughPlugin extends JavaPlugin {
    
    private ApplicationListener applicationListener;
    
    @Override 
    public void onEnable() {
        applicationListener = new ApplicationListener();
        getServer().getPluginManager().registerEvents(applicationListener, this);
        File applications = new File("applications.json");
        File submitted_builds = new File("submitted_builds.json");
        File accepted_builds = new File("accepted_builds.json");
        File accepted_applications = new File("accepted_applications.json"); //TODO: These two JSONs currently do anything. Do we actually need them?
        File denied_applications = new File("denied_applications.json");
        
        if(!applications.exists()) { //If applications.json does not exist
        	JSONObject jsonObject = new JSONObject();
			try {
				FileWriter file = new FileWriter("applications.json");
				file.write(jsonObject.toJSONString());
	    		file.flush();
	    		file.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
        } else {
        	BufferedReader br;
			try {
				br = new BufferedReader(new FileReader("applications.json"));
				if (br.readLine() == null) {
					JSONObject jsonObject = new JSONObject();
					FileWriter file = new FileWriter("applications.json");
					file.write(jsonObject.toJSONString());
		    		file.flush();
		    		file.close();
	        	}
			} catch (IOException e) {
				e.printStackTrace();
			}     
        	
        }
        if(!submitted_builds.exists()) {
        	JSONObject jsonObject = new JSONObject();
			try {
				FileWriter file = new FileWriter("submitted_builds.json");
				file.write(jsonObject.toJSONString());
	    		file.flush();
	    		file.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
        }
        if(!accepted_builds.exists()) {
        	JSONObject jsonObject = new JSONObject();
			try {
				FileWriter file = new FileWriter("accepted_builds.json");
				file.write(jsonObject.toJSONString());
	    		file.flush();
	    		file.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
        }
        if(!accepted_applications.exists()) {
        	JSONObject jsonObject = new JSONObject();
			try {
				FileWriter file = new FileWriter("accepted_applications.json");
				file.write(jsonObject.toJSONString());
	    		file.flush();
	    		file.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
        }
        if(!denied_applications.exists()) {
        	JSONObject jsonObject = new JSONObject();
			try {
				FileWriter file = new FileWriter("denied_applications.json");
				file.write(jsonObject.toJSONString());
	    		file.flush();
	    		file.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if(sender instanceof Player) {
            Player player = (Player)sender;
            
            if(applicationListener.handleCommand(player, command.getName())) {
                return true;
            }
        }
        
        return false;
    }
}
