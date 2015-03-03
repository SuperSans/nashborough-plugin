/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package to.us.nashboroughmc.nashboroughplugin;

import to.us.nashboroughmc.nashboroughplugin.listeners.ApplicationListener;
import org.bukkit.plugin.java.JavaPlugin;

public class NashboroughPlugin extends JavaPlugin {
    
    private ApplicationListener applicationListener;
    
    @Override 
    public void onEnable() {
        applicationListener = new ApplicationListener();
        getServer().getPluginManager().registerEvents(applicationListener, this);
        
        getCommand("apply").setExecutor(applicationListener);
        getCommand("reviewapps").setExecutor(applicationListener);
        getCommand("reviewapp").setExecutor(applicationListener);
    }
}
