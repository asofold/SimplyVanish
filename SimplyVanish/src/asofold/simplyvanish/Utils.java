package asofold.simplyvanish;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

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
	
	/**
	 * Compatibility method.
	 * @param input
	 * @return
	 */
	public static String withChatColors(String input) {
        char[] chars = input.toCharArray();
        for (int i = 0; i < chars.length - 1; i++) {
            if ((chars[i] == '&' || chars[i]=='§') && ("0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(chars[i+1]) >= 0)) {
                chars[i] = ChatColor.COLOR_CHAR;
                chars[i+1] = Character.toLowerCase(chars[i+1]);
            }
        }
        return new String(chars);
    }
	
	public static final boolean checkOnline(final Player player){
		return checkOnline( player.getName());
	}
	
	public static final boolean checkOnline(final String name){
		final Player player = Bukkit.getServer().getPlayerExact(name);
		return player != null;
	}
	
	/**
	 * Check and log warning message.
	 * @param player
	 * @param tag
	 * @return
	 */
	public static final boolean checkOnline(final Player player, final String tag){
		final boolean res = checkOnline(player);
		warn("["+tag+"] Player is not online, though should: "+player.getName());
		return res;
	}
	
	public static final void warn(final String msg){
		Bukkit.getServer().getLogger().warning("[SimplyVanish] "+msg);
	}
	
	public static final void severe(final String msg){
		Bukkit.getServer().getLogger().severe("[SimplyVanish] "+msg);
	}

	public static void sendToTargets(String msg,
			String targetSpec) {
		// check targets:
		List<Player> players = new LinkedList<Player>();
		Player[] online = Bukkit.getServer().getOnlinePlayers();
		for ( String x : targetSpec.split(",")){
			String targets = x.trim();
			if ( targets.equalsIgnoreCase("ops") || targets.equalsIgnoreCase("operators")){
				for (Player player : online){
					if (player.isOp()) players.add(player);
				}
			}
			else if (targets.equalsIgnoreCase("all") || targets.equalsIgnoreCase("everyone") || (targets.equalsIgnoreCase("everybody"))){
				for (Player player : online){
					players.add(player);
				}
			} 
			else if (targets.toLowerCase().startsWith("permission:") && targets.length()>11){
				String perm = targets.substring(11).trim();
				for (Player player : online){
					if (Utils.hasPermission(player, perm)) players.add(player);
				}
			}
		}
		for ( Player player : players){
			player.sendMessage(msg);
		}
	}

	public static void dropItemInHand(Player player) {
		ItemStack stack = player.getItemInHand();
		if ( stack == null) return;
		if(stack.getType() == Material.AIR) return;
		ItemStack newStack = stack.clone();
		Item item = player.getWorld().dropItem(player.getLocation().add(new Vector(0.0, 1.0, 0.0)), newStack);
		item.setVelocity(player.getLocation().getDirection().normalize().multiply(0.05));
		if ( item != null && !item.isDead()) player.setItemInHand(null);
	}
}
