package me.asofold.bpl.simplyvanish.listeners;

import me.asofold.bpl.simplyvanish.SimplyVanish;
import me.asofold.bpl.simplyvanish.SimplyVanishCore;
import me.asofold.bpl.simplyvanish.config.Settings;
import me.asofold.bpl.simplyvanish.config.VanishConfig;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * Chat + commands !
 * @author mc_dev
 *
 */
public final class ChatListener implements Listener {
	
	private final SimplyVanishCore core;
	
	public ChatListener(final SimplyVanishCore core){
		this.core = core;
	}
	
//	private final boolean shouldCancelChat(final String name) {
//		final VanishConfig cfg = core.getVanishConfig(name, false);
//		return shouldCancelChat(name, cfg);
//	}
	
	private static final boolean shouldCancelChat(final String name, final VanishConfig cfg) {
		if (cfg == null) return false;
		if (!cfg.vanished.state || cfg.chat.state) return false;
		return true;
	}
	
	/**
	 * 
	 * @param name Player name
	 * @param cmd command (lower case, trim).
	 * @return
	 */
	private final boolean shouldCancelCmd(final String name, final String cmd) {
		final VanishConfig cfg = core.getVanishConfig(name, false);
		if (cfg == null) return false;
		if (!cfg.vanished.state) return false;
		if (!cfg.cmd.state){
			final Settings settings = core.getSettings();
			final boolean contains = settings.cmdCommands.contains(cmd);
			if (settings.cmdWhitelist) return !contains;
			else return contains;
		}
		else return false;
	}
	
	@EventHandler(priority=EventPriority.LOW)
	final void onChat(final AsyncPlayerChatEvent event){
		if (event.isCancelled()) return;
		// Just prevent accidental chat.
		final Player player = event.getPlayer();
		final String playerName = player.getName();
		if (shouldCancelChat(playerName, SimplyVanish.getVanishConfig(playerName, false))){
			event.setCancelled(true);
			player.sendMessage(SimplyVanish.msgLabel+ChatColor.RED+"Disabled! (/vanflag +chat or /reappear)");
		}
	}
	
	@EventHandler(priority=EventPriority.LOW)
	final void onCommandPreprocess(final PlayerCommandPreprocessEvent event){
		if (event.isCancelled()) return;
		String cmd = event.getMessage().trim().split(" ", 2)[0].toLowerCase();
		if (cmd.length()>1) cmd = cmd.substring(1);
		// TODO: maybe find the fastest way to do this !
		final Player player = event.getPlayer();
		if (shouldCancelCmd(player.getName(), cmd)){
			event.setCancelled(true);
			player.sendMessage(SimplyVanish.msgLabel+ChatColor.RED+"Disabled! (/vanflag +cmd or /reappear)");
		}
	}
}
