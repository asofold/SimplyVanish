package asofold.simplyvanish;

import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;

/**
 * Some static methods for more generic purpose.
 * @author mc_dev
 *
 */
public class Utils {
	
	/**
	 * Simplistic: Ops have permissions always, others get checked (superperms).
	 * @param player
	 * @param perm
	 * @return
	 */
	public static boolean hasPermission(CommandSender sender, String perm) {
		return sender.isOp() || sender.hasPermission(perm);
	}

	/**
	 * Check, message on failure.
	 * @param sender
	 * @param perm
	 * @return
	 */
	public static boolean checkPerm(CommandSender sender, String perm){
		if ( !hasPermission(sender, perm)){
			sender.sendMessage(ChatColor.DARK_RED+"[SimplyVanish] No permission.");
			return false;
		}
		return true;
	}

	/**
	 * Check if is a player, message if not.
	 * @param sender
	 * @return
	 */
	public static boolean checkPlayer(CommandSender sender){
		if ( sender instanceof Player) return true;
		sender.sendMessage("[SimplyVanish] This is only available for players!");
		return false;
	}
	
	public static void forceDefaults(Configuration defaults, Configuration config){
		Map<String ,Object> all = defaults.getValues(true);
		for ( String path : all.keySet()){
			if ( !config.contains(path)) config.set(path, defaults.get(path));
		}
	}

}
