package to.us.nashboroughmc.nashboroughplugin;

import org.bukkit.entity.Player;

public class Utils {
	
	private static final String CODE_PINK = "\u00a7d";
	
	public static void send_message(Player player, String message) {
		
		player.sendMessage(CODE_PINK + message);
	}
}
	