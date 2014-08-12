package me.asofold.bpl.simplyvanish.listeners;

import java.util.Iterator;

import me.asofold.bpl.simplyvanish.SimplyVanish;
import me.asofold.bpl.simplyvanish.SimplyVanishCore;
import me.asofold.bpl.simplyvanish.api.events.GetVanishConfigEvent;
import me.asofold.bpl.simplyvanish.api.events.SimplyVanishAtLoginEvent;
import me.asofold.bpl.simplyvanish.config.Settings;
import me.asofold.bpl.simplyvanish.config.VanishConfig;
import me.asofold.bpl.simplyvanish.util.HookUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;

public final class CoreListener implements Listener {
	private final SimplyVanishCore core;
	public CoreListener(SimplyVanishCore core){
		this.core = core;
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	final void onPlayerQuit(final PlayerQuitEvent event){
		if (onLeave(event.getPlayer().getName(), false, " quit.")) event.setQuitMessage(null);
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	final void onPlayerKick(final PlayerKickEvent event){
		if (onLeave(event.getPlayer().getName(), event.isCancelled(), " was kicked.")) event.setLeaveMessage(null);
	}
	
	/**
	 * For Quit / kick.
	 * @param name
	 * @param cancelled if event was cancelled
	 * @return If to clear the leave message.
	 */
	final boolean onLeave(final String name, boolean cancelled, final String action){
		final Settings settings = core.getSettings();
		if (settings.suppressQuitMessage && core.isVanished(name)){
			final boolean online = core.getVanishConfig(name, true).online.state;
			// TODO: Not sure about the notify flag here.
			if (settings.notifyState && !cancelled && !online){
				String msg = SimplyVanish.msgLabel+ChatColor.GREEN+name+ChatColor.GRAY+action;
				for (Player other : Bukkit.getServer().getOnlinePlayers()){
					if (core.hasPermission(other, settings.notifyStatePerm)) {
						other.sendMessage(msg);
					}
				}
			}
			return !online; // suppress in any case if vanished.
		}
		else return false;
	}
	

	

	
	@EventHandler(priority=EventPriority.HIGHEST)
	void onServerListPing(ServerListPingEvent event){
		Iterator<Player> it;
		try {
			it = event.iterator();
		} catch (UnsupportedOperationException e) {
			return;
		} catch (NoSuchMethodError e2) {
			return;
		}
		while (it.hasNext()) {
			final Player player = it.next();
			final VanishConfig cfg = core.getVanishConfig(player.getName(), false);
			if (cfg != null && cfg.vanished.state && !cfg.online.state) {
				// No catch here.
				it.remove();
			}
		}
	}
	

	
	@EventHandler(priority=EventPriority.HIGHEST)
	final void onPlayerJoin(final PlayerJoinEvent event){
		final Player player = event.getPlayer();
		final String playerName = player.getName();
		final Settings settings = core.getSettings();
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
		final HookUtil hookUtil = core.getHookUtil();
		if (doVanish){
			final SimplyVanishAtLoginEvent svEvent = new SimplyVanishAtLoginEvent(playerName, was, doVanish, auto);
			Bukkit.getServer().getPluginManager().callEvent(svEvent);
			if (svEvent.isCancelled()){
				// no update
				return;
			}
			doVanish = svEvent.getVisibleAfter();
			cfg.set("vanished", doVanish);
			if (doVanish) hookUtil.callBeforeVanish(playerName); // need to check again.
		}
		if (!core.updateVanishState(event.getPlayer())){
			// TODO: set doVanish ? remove from vanished ?
			return;
		}
		if (doVanish){
			hookUtil.callAfterVanish(playerName);	
			if (settings.suppressJoinMessage && cfg.vanished.state) {
				if (!cfg.online.state) {
					event.setJoinMessage(null);
				}
			}
			else if (!cfg.needsSave()) core.removeVanishedName(playerName);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	final void onGetVanishConfig(final GetVanishConfigEvent event){
		event.setVanishConfig(core.getVanishConfig(event.getPlayerName(), event.getCreate()));
	}
}
