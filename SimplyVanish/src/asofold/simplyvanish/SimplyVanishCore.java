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
		Player player = event.getPlayer();
		if (settings.autoVanishUse){
			if (Utils.hasPermission(player, settings.autoVanishPerm)) vanished.add(player.getName().toLowerCase());
		}
		updateVanishState(event.getPlayer());
		if ( settings.suppressJoinMessage && vanished.contains(player.getName().toLowerCase())){
			event.setJoinMessage(null);
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	void onPlayerQuit(PlayerQuitEvent event){
		if ( settings.suppressQuitMessage && vanished.contains(event.getPlayer().getName().toLowerCase())){
			event.setQuitMessage(null);
			if (settings.notifyState){
				String msg = SimplyVanish.label+ChatColor.GREEN+event.getPlayer().getName()+ChatColor.GRAY+" quit.";
				for (Player other : Bukkit.getServer().getOnlinePlayers()){
					if ( Utils.hasPermission(other, settings.notifyStatePerm)) other.sendMessage(msg);
				}
			}
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	void onPlayerKick(PlayerKickEvent event){
		// (still set if cancelled)
		if ( settings.suppressQuitMessage && vanished.contains(event.getPlayer().getName().toLowerCase())){
			event.setLeaveMessage(null);
			if (settings.notifyState && !event.isCancelled()){
				String msg = SimplyVanish.label+ChatColor.GREEN+event.getPlayer().getName()+ChatColor.GRAY+" was kicked.";
				for (Player other : Bukkit.getServer().getOnlinePlayers()){
					if ( Utils.hasPermission(other, settings.notifyStatePerm)) other.sendMessage(msg);
				}
			}
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
		if ( (dx == 0.0) && (dz == 0.0)){
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
		String name = player.getName();
		boolean was = !vanished.add(name.toLowerCase());
		String msg = null;
		if (settings.sendFakeMessages && !settings.fakeQuitMessage.isEmpty()){
			msg = settings.fakeQuitMessage.replaceAll("%name", name);
			msg = msg.replaceAll("%displayname", player.getDisplayName());
		}
		for ( Player other : Bukkit.getServer().getOnlinePlayers()){
			if (other.getName().equals(name)) continue;
			if ( other.canSee(player)){
				// (only consider a changed canSee state)
				if (settings.notifyState && Utils.hasPermission(other, settings.notifyStatePerm)){
					if (!was) other.sendMessage(SimplyVanish.label+ChatColor.GREEN+name+ChatColor.GRAY+" vanished.");
					if (!Utils.hasPermission(other, "simplyvanish.see-all")) hidePlayer(player, other);
				} else if (!Utils.hasPermission(other, "simplyvanish.see-all")){
					hidePlayer(player, other);
					if (msg != null) other.sendMessage(msg);
				}
			} else if (!was && settings.notifyState && Utils.hasPermission(other, settings.notifyStatePerm)){
				other.sendMessage(SimplyVanish.label+ChatColor.GREEN+name+ChatColor.GRAY+" vanished.");
			}
		}
		player.sendMessage(SimplyVanish.label+ChatColor.GRAY+"You are "+(was?"still":"now")+" "+ChatColor.GREEN+"invisible"+ChatColor.GRAY+" to normal players!");

	}

	/**
	 * Adjust state of player to not vanished.
	 * @param player
	 */
	public void onReappear(Player player) {
		String name = player.getName();
		boolean was = vanished.remove(name.toLowerCase());
		String msg = null;
		if (settings.sendFakeMessages && !settings.fakeJoinMessage.isEmpty()){
			msg = settings.fakeJoinMessage.replaceAll("%name", name);
			msg = msg.replaceAll("%displayname", player.getDisplayName());
		}
		for ( Player other : Bukkit.getServer().getOnlinePlayers()){
			if (other.getName().equals(name)) continue;
			if (!other.canSee(player)){
				// (only consider a changed canSee state)
				showPlayer(player, other);
				if (settings.notifyState && Utils.hasPermission(other, settings.notifyStatePerm)){
					other.sendMessage(SimplyVanish.label+ChatColor.RED+name+ChatColor.GRAY+" reappeared.");
				} else if (!Utils.hasPermission(other, "simplyvanish.see-all")){
					if (msg != null) other.sendMessage(msg);
				}
			} 
			else if (was && settings.notifyState && Utils.hasPermission(other, settings.notifyStatePerm)){
				other.sendMessage(SimplyVanish.label+ChatColor.RED+name+ChatColor.GRAY+" reappeared.");
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
		if (vanished.remove(lcName)) onVanish(player); // remove: people will get notified.
		else{
			for (Player other : server.getOnlinePlayers()){
				if ( !other.canSee(player)) showPlayer(player, other);
				// TODO: maybe message here too ?
			}
		}
		// Show or hide other vanished players:
		if ( !Utils.hasPermission(player, "simplyvanish.see-all")){
			for (String name : vanished){
				if ( name.equals(playerName)) continue;
				Player other = server.getPlayerExact(name);
				if ( other != null){
					if ( player.canSee(other)) hidePlayer(other, player);
				}
			}
		} else{
			for (String name : vanished){
				if ( name.equals(lcName)) continue;
				Player other = server.getPlayerExact(name);
				if ( other != null){
					if (!player.canSee(other)) showPlayer(other, player);
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
	
	/**
	 * Delegating method, for the case of other things to be checked.
	 * @param player The player to show.
	 * @param canSee 
	 */
	void showPlayer(Player player, Player canSee){
		if (!Utils.checkOnline(player, "showPlayer")||!Utils.checkOnline(canSee, "showPlayer")) return;
		try{
			canSee.showPlayer(player);
		} catch(Throwable t){
			Utils.severe("showPlayer failed (show "+player.getName()+" to "+canSee.getName()+"): "+t.getMessage());
			t.printStackTrace();
			onPanic(new Player[]{player, canSee});
		}
	}
	
	/**
	 * Delegating method, for the case of other things to be checked.
	 * @param player The player to hide.
	 * @param canNotSee
	 */
	void hidePlayer(Player player, Player canNotSee){
		if (!Utils.checkOnline(player, "hidePlayer")||!Utils.checkOnline(canNotSee, "hidePlayer")) return;
		try{
			canNotSee.hidePlayer(player);
		} catch ( Throwable t){
			Utils.severe("hidePlayer failed (hide "+player.getName()+" from "+canNotSee.getName()+"): "+t.getMessage());
			t.printStackTrace();
			onPanic(new Player[]{player, canNotSee});
		}
	}
	
	void onPanic(Player[] involved){
		Server server = Bukkit.getServer();
		if ( settings.panicKickAll){
			for ( Player player :  server.getOnlinePlayers()){
				try{
					player.kickPlayer(settings.panicKickMessage);
				} catch (Throwable t){
					// ignore
				}
			}
		} 
		else if (settings.panicKickInvolved){
			for ( Player player : involved){
				try{
					player.kickPlayer(settings.panicKickMessage);
				} catch (Throwable t){
					// ignore
				}
			}
		}
		try{
			Utils.sendToTargets(settings.panicMessage, settings.panicMessageTargets);
		} catch ( Throwable t){
			Utils.warn("[Panic] Failed to send to: "+settings.panicMessageTargets+" ("+t.getMessage()+")");
			t.printStackTrace();
		}
		if (settings.panicRunCommand && !"".equals(settings.panicCommand)){
			try{
				server.dispatchCommand(server.getConsoleSender(), settings.panicCommand);
			} catch (Throwable t){
				Utils.warn("[Panic] Failed to dispathc command: "+settings.panicCommand+" ("+t.getMessage()+")");
				t.printStackTrace();
			}
		}
	}

}
