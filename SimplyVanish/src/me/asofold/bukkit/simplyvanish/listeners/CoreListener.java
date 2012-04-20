package me.asofold.bukkit.simplyvanish.listeners;

import me.asofold.bukkit.simplyvanish.SimplyVanish;
import me.asofold.bukkit.simplyvanish.SimplyVanishCore;
import me.asofold.bukkit.simplyvanish.api.events.SimplyVanishAtLoginEvent;
import me.asofold.bukkit.simplyvanish.config.Settings;
import me.asofold.bukkit.simplyvanish.config.VanishConfig;
import me.asofold.bukkit.simplyvanish.util.HookUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CoreListener implements Listener {
	private final SimplyVanishCore core;
	public CoreListener(SimplyVanishCore core){
		this.core = core;
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	void onPlayerQuit(PlayerQuitEvent event){
		if (onLeave(event.getPlayer().getName(), false, " quit.")) event.setQuitMessage(null);
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	void onPlayerKick(PlayerKickEvent event){
		if (onLeave(event.getPlayer().getName(), event.isCancelled(), " was kicked.")) event.setLeaveMessage(null);
	}
	
	/**
	 * For Quit / kick.
	 * @param name
	 * @param cancelled if event was cancelled
	 * @return If to clear the leave message.
	 */
	boolean onLeave(String name, boolean cancelled, String action){
		Settings settings = core.getSettings();
		if (settings.suppressQuitMessage && core.isVanished(name)){
			if (settings.notifyState && !cancelled){
				String msg = SimplyVanish.msgLabel+ChatColor.GREEN+name+ChatColor.GRAY+action;
				for (Player other : Bukkit.getServer().getOnlinePlayers()){
					if (core.hasPermission(other, settings.notifyStatePerm)) other.sendMessage(msg);
				}
			}
			return true; // suppress in any case if vanished.
		}
		else return false;
	}
	

	

	
//	@EventHandler(priority=EventPriority.HIGHEST)
//	void onServerListPing(ServerListPingEvent event){
//		// TODO: try reflection ??
//	}
	

	
	@EventHandler(priority=EventPriority.HIGHEST)
	void onPlayerJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();
		String playerName = player.getName();
		Settings settings = core.getSettings();
		VanishConfig cfg = core.getVanishConfig(playerName, false);
		boolean was = cfg != null && cfg.vanished.state;
		boolean auto = false; // Indicate if the player should be vanished due to auto-vanish.
		if ( settings.autoVanishUse && (cfg == null || cfg.auto.state) ) {
			if (core.hasPermission(player, settings.autoVanishPerm)){
				// permission given, do attempt to vanish
				auto = true;
				if (cfg == null) cfg = core.getVanishConfig(playerName, true);
			}
		}
		boolean doVanish = auto || was;
		HookUtil hookUtil = core.getHookUtil();
		if (doVanish){
			SimplyVanishAtLoginEvent svEvent = new SimplyVanishAtLoginEvent(playerName, was, doVanish, auto);
			Bukkit.getServer().getPluginManager().callEvent(svEvent);
			if (svEvent.isCancelled()){
				// no update
				return;
			}
			doVanish = svEvent.getVisibleAfter();
			cfg.set("vanished", doVanish);
			if (doVanish) hookUtil.callBeforeVanish(playerName); // need to check again.
		}
		core.updateVanishState(event.getPlayer()); // called in any case
		if (doVanish){
			hookUtil.callAfterVanish(playerName);	
			if ( settings.suppressJoinMessage && cfg.vanished.state){
				event.setJoinMessage(null);
			}
			else if (!cfg.needsSave()) core.removeVanishedName(playerName);
		}
	}
}
