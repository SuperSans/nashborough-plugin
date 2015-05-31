/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package to.us.nashboroughmc.nashboroughplugin;

import to.us.nashboroughmc.nashboroughplugin.listeners.ApplicationListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class NashboroughPlugin extends JavaPlugin {
    
    private ApplicationListener applicationListener;
    
    @Override 
    public void onEnable() {
        applicationListener = new ApplicationListener();
        getServer().getPluginManager().registerEvents(applicationListener, this);
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
