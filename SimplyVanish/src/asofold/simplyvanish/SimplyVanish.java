package asofold.simplyvanish;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Example plugin for the vanish API as of CB 1914 !
 * Vanish + God mode + No Target + No pickup.
 * @author mc_dev
 *
 */
public class SimplyVanish extends JavaPlugin implements Listener {
	static SimplyVanish instance = null;
	/**
	 * Vanished players.
	 */
	Set<String> vanished = new HashSet<String>();

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		
		if ( !(sender instanceof Player)){
			sender.sendMessage("[SimplyVanish] Commands are only available for players!");
		}
		Player player = (Player) sender;
		if ( label.equalsIgnoreCase("vanish")){
			if ( !hasPermission(player, "simplyvanish.vanish")){
				player.sendMessage(ChatColor.DARK_RED+"[SimplyVanish] No permission.");
				return false;
			}
			vanish(player);
			return true;
		} else if (label.equalsIgnoreCase("reappear")){
			if ( !hasPermission(player, "simplyvanish.vanish")){
				player.sendMessage(ChatColor.DARK_RED+"[SimplyVanish] No permission.");
				return false;
			}
			reappear(player);
			return true;
		}
		return false;
	}

	@Override
	public void onDisable() {
		instance = null;
		super.onDisable();
		System.out.println("[SimplyVanish] Disabled.");
	}

	@Override
	public void onEnable() {
		// just in case quadratic time checking:
		for ( Player player : getServer().getOnlinePlayers()){
			updateVanishState(player);
		}
		getServer().getPluginManager().registerEvents(this, this);
		System.out.println("[SimplyVanish] Enabled");
		super.onEnable();
		instance = this;
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	void onPlayerJoin( PlayerJoinEvent event){
		updateVanishState(event.getPlayer());
	}
	
	@EventHandler(priority=EventPriority.LOW)
	void onEntityTarget(EntityTargetEvent event){
		if ( event.isCancelled() ) return;
		Entity target = event.getTarget();
		if (!(target instanceof Player)) return;
		String playerName = ((Player) target).getName();
		if ( vanished.contains(playerName)) event.setTarget(null);
	}
	
	@EventHandler(priority=EventPriority.LOW)
	void onEntityDamage(EntityDamageEvent event){
		if ( event.isCancelled() ) return;
		Entity entity = event.getEntity();
		if (!(entity instanceof Player)) return;
		String playerName = ((Player) entity).getName();
		if ( !vanished.contains(playerName)) return;
		event.setCancelled(true);
		if ( entity.getFireTicks()>0) entity.setFireTicks(0);
	}
	
	@EventHandler(priority=EventPriority.LOW)
	void onItemPickUp(PlayerPickupItemEvent event){
		if ( event.isCancelled() ) return;
		Player player = event.getPlayer();
		if ( !vanished.contains(player.getName())) return;
		event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.LOW)
	void onItemDrop(PlayerDropItemEvent event){
		if ( event.isCancelled() ) return;
		Player player = event.getPlayer();
		if ( !vanished.contains(player.getName())) return;
		event.setCancelled(true);
	}
	
	public void vanish(Player player) {
		vanished.add(player.getName());
		for ( Player other : getServer().getOnlinePlayers()){
			if (!other.equals(player) && other.canSee(player)){
				if ( !hasPermission(other, "simplyvanish.see-all")) other.hidePlayer(player);
			}
		}
		player.sendMessage(ChatColor.GOLD+"[SimplyVanish] "+ChatColor.GRAY+"You are now "+ChatColor.GREEN+"invisible"+ChatColor.GRAY+" to normal players!");

	}

	public void reappear(Player player) {
		vanished.remove(player.getName());
		for ( Player other : getServer().getOnlinePlayers()){
			if (!other.equals(player) && !other.canSee(player)){
				other.showPlayer(player);
			}
		}
		player.sendMessage(ChatColor.GOLD+"[SimplyVanish] "+ChatColor.GRAY+"You are now "+ChatColor.RED+"visible"+ChatColor.GRAY+" to everyone!");
	}
	
	/**
	 * Heavy update for who can see this player and whom this player can see.
	 * @param player
	 */
	public void updateVanishState(Player player){
		String playerName = player.getName();
		Server server = getServer();
		// Show to or hide from online players:
		if ( vanished.contains(playerName)) vanish(player);
		else{
			for (Player other : server.getOnlinePlayers()){
				if ( !other.canSee(player)) other.showPlayer(player);
			}
		}
		// Show or hide other vanished players:
		if ( !hasPermission(player, "simplyvanish.see-all")){
			for (String name : vanished){
				if ( name.equals(playerName)) continue;
				Player other = server.getPlayerExact(name);
				if ( other != null){
					if ( player.canSee(other)) player.hidePlayer(other);
				}
			}
		} else{
			for (String name : vanished){
				if ( name.equals(playerName)) continue;
				Player other = server.getPlayerExact(name);
				if ( other != null){
					if ( !player.canSee(other)) player.showPlayer(other);
				}
			}
		}
	}

	/**
	 * Simplistic: Ops have permissions always, others get checked (superperms).
	 * @param player
	 * @param perm
	 * @return
	 */
	boolean hasPermission(Player player, String perm) {
		return player.isOp() || player.hasPermission(perm);
	}
	
	/**
	 * API
	 * @param playerName Exact player name.
	 * @return
	 */
	public static boolean isVanished(String playerName){
		if ( instance == null ) return false;
		else return instance.vanished.contains(playerName);
	}
	
	/**
	 * API
	 * @param player 
	 * @return
	 */
	public static boolean isVanished(Player player){
		if ( instance == null ) return false;
		else return instance.vanished.contains(player.getName());
	}
	
	/**
	 * API
	 * NOTE: It returns the internally used HashSet instance, do not manipulate it, do not iterate in an asynchronous task or thread.
	 * @return
	 */
	public static Set<String> getVanishedPlayers(){
		if ( instance == null ) return new HashSet<String>();
		else return instance.vanished;
	}

}
