/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package to.us.nashboroughmc.nashboroughplugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import to.us.nashboroughmc.nashboroughplugin.callbacks.ApplicationInterface;

/**
 *
 * @author Jacob
 */
public class ApplicationListener implements Listener {
    
    private final ApplicationInterface applicationInterface;
    
    public ApplicationListener(ApplicationInterface applicationInterface) {
        this.applicationInterface = applicationInterface;
    }
    
    @EventHandler
    public void onChatEvent(AsyncPlayerChatEvent ev) {
        Player player  = ev.getPlayer();
        String message = ev.getMessage();
        
        //Cancel message if it is handled
        if(applicationInterface.handleMessage(player, message)) {
            ev.setCancelled(true);
        }
    }
}
