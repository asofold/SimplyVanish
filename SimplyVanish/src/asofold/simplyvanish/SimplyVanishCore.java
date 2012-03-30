package asofold.simplyvanish;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.util.Vector;

import asofold.simplyvanish.config.Settings;

/**
 * Core methods for vanish/reappear.
 * @author mc_dev
 *
 */
public class SimplyVanishCore implements Listener{
	/**
	 * Same as in SimplyVanish.
	 */
	final Set<String> vanished = new HashSet<String>();
	
	/**
	 * Flag for if the plugin is enabled.
	 */
	boolean enabled = false;
	
	Settings settings = new Settings();


	@EventHandler(priority=EventPriority.HIGHEST)
	void onPlayerJoin( PlayerJoinEvent event){
		updateVanishState(event.getPlayer());
		if ( settings.suppressJoinMessage && vanished.contains(event.getPlayer().getName().toLowerCase())){
			event.setJoinMessage(null);
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	void onPlayerQuit(PlayerQuitEvent event){
		if ( settings.suppressQuitMessage && vanished.contains(event.getPlayer().getName().toLowerCase())){
			event.setQuitMessage(null);
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	void onPlayerKick(PlayerKickEvent event){
		// (still set if cancelled)
		if ( settings.suppressQuitMessage && vanished.contains(event.getPlayer().getName().toLowerCase())){
			event.setLeaveMessage(null);
		}
	}
	
	@EventHandler(priority=EventPriority.LOW)
	void onEntityTarget(EntityTargetEvent event){
		if ( event.isCancelled() ) return;
		Entity target = event.getTarget();
		if (!(target instanceof Player)) return;
		String playerName = ((Player) target).getName();
		if ( vanished.contains(playerName.toLowerCase())){
			event.setTarget(null);
			if ( settings.expEnabled){
				Entity entity = event.getEntity();
				if ( entity instanceof ExperienceOrb){
					repellExpOrb((Player) target, (ExperienceOrb) entity);
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	void onServerListPing(ServerListPingEvent event){
		// TODO: try reflection ??
	}
	
	/**
	 * Attempt some workaround for experience orbs:
	 * prevent it getting near the player.
	 * @param target
	 * @param entity
	 */
	void repellExpOrb(Player player, ExperienceOrb orb) {
		Location pLoc = player.getLocation();
		Location oLoc = orb.getLocation();
		Vector dir = oLoc.toVector().subtract(pLoc.toVector());
		double dx = Math.abs(dir.getX());
		double dz = Math.abs(dir.getZ());
		if ( (dx == 0) && (dz == 0)){
			// Special case probably never happens
			dir.setX(0.001);
		}
		if ((dx < settings.expThreshold) && (dz < settings.expThreshold)){
			Vector nDir = dir.normalize();
			Vector newV = nDir.clone().multiply(settings.expVelocity);
			newV.setY(0);
			orb.setVelocity(newV);
			if ((dx < settings.expTeleDist) && (dz < settings.expTeleDist)){
				// maybe oLoc
				orb.teleport(oLoc.clone().add(nDir.multiply(settings.expTeleDist)), TeleportCause.PLUGIN);
			} 
			if ((dx < settings.expKillDist) && (dz < settings.expKillDist)){
				orb.remove();
			} 
		} 
	}

	@EventHandler(priority=EventPriority.LOW)
	void onEntityDamage(EntityDamageEvent event){
		if ( event.isCancelled() ) return;
		Entity entity = event.getEntity();
		if (!(entity instanceof Player)) return;
		String playerName = ((Player) entity).getName();
		if ( !vanished.contains(playerName.toLowerCase())) return;
		event.setCancelled(true);
		if ( entity.getFireTicks()>0) entity.setFireTicks(0);
	}
	
	@EventHandler(priority=EventPriority.LOW)
	void onItemPickUp(PlayerPickupItemEvent event){
		if ( event.isCancelled() ) return;
		Player player = event.getPlayer();
		if ( !vanished.contains(player.getName().toLowerCase())) return;
		event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.LOW)
	void onItemDrop(PlayerDropItemEvent event){
		if ( event.isCancelled() ) return;
		Player player = event.getPlayer();
		if ( !vanished.contains(player.getName().toLowerCase())) return;
		event.setCancelled(true);
	}

	/**
	 * Only has relevance for static access by Plugin.
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	/**
	 * Only for static access by plugin.
	 * @return
	 */
	public boolean isEnabled(){
		return enabled;
	}
	
	public void setSettings(Settings settings){
		this.settings = settings;
	}
	
	/**
	 * Adjust state of player to vanished.
	 * @param player
	 */
	public void onVanish(Player player) {
		boolean was = !vanished.add(player.getName().toLowerCase());
		for ( Player other : Bukkit.getServer().getOnlinePlayers()){
			if (!other.equals(player) && other.canSee(player)){
				if ( !Utils.hasPermission(other, "simplyvanish.see-all")) other.hidePlayer(player);
			}
		}
		player.sendMessage(SimplyVanish.label+ChatColor.GRAY+"You are "+(was?"still":"now")+" "+ChatColor.GREEN+"invisible"+ChatColor.GRAY+" to normal players!");

	}

	/**
	 * Adjust state of player to not vanished.
	 * @param player
	 */
	public void onReappear(Player player) {
		boolean was = vanished.remove(player.getName().toLowerCase());
		for ( Player other : Bukkit.getServer().getOnlinePlayers()){
			if (!other.equals(player) && !other.canSee(player)){
				other.showPlayer(player);
			}
		}
		player.sendMessage(SimplyVanish.label+ChatColor.GRAY+"You are "+(was?"still":"now")+" "+ChatColor.RED+"visible"+ChatColor.GRAY+" to everyone!");
	}
	
	/**
	 * Heavy update for who can see this player and whom this player can see.
	 * @param player
	 */
	public void updateVanishState(Player player){
		String playerName = player.getName();
		String lcName = playerName.toLowerCase();
		Server server = Bukkit.getServer();
		// Show to or hide from online players:
		if (vanished.contains(lcName)) onVanish(player);
		else{
			for (Player other : server.getOnlinePlayers()){
				if ( !other.canSee(player)) other.showPlayer(player);
			}
		}
		// Show or hide other vanished players:
		if ( !Utils.hasPermission(player, "simplyvanish.see-all")){
			for (String name : vanished){
				if ( name.equals(playerName)) continue;
				Player other = server.getPlayerExact(name);
				if ( other != null){
					if ( player.canSee(other)) player.hidePlayer(other);
				}
			}
		} else{
			for (String name : vanished){
				if ( name.equals(lcName)) continue;
				Player other = server.getPlayerExact(name);
				if ( other != null){
					if ( !player.canSee(other)) player.showPlayer(other);
				}
			}
		}
	}
	
	/**
	 * Unlikely that sorted is needed, but anyway.
	 * @return
	 */
	public List<String> getSortedVanished(){
		List<String> sorted = new ArrayList<String>(vanished.size());
		sorted.addAll(vanished);
		Collections.sort(sorted);
		return sorted;
	}
	


}
