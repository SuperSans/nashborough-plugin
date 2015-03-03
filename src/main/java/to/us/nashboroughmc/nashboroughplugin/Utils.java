/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package to.us.nashboroughmc.nashboroughplugin;

import org.bukkit.command.CommandSender;

/**
 *
 * @author Jacob
 */
public class Utils {
    
    private static final String MESSAGE_COLOR_PINK = "\u00A7d";
    
    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(MESSAGE_COLOR_PINK + message);
    }
}
