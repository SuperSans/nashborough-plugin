/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package to.us.nashboroughmc.nashboroughplugin.callbacks;

import org.bukkit.entity.Player;

/**
 *
 * @author Jacob
 */
public interface ApplicationInterface {
    
    public boolean handleMessage(Player player, String message);
}
