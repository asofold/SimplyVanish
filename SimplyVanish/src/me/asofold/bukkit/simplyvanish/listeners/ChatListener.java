package me.asofold.bukkit.simplyvanish.listeners;

import me.asofold.bukkit.simplyvanish.SimplyVanish;
import me.asofold.bukkit.simplyvanish.SimplyVanishCore;
import me.asofold.bukkit.simplyvanish.config.Settings;
import me.asofold.bukkit.simplyvanish.config.VanishConfig;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * Chat + commands !
 * @author mc_dev
 *
 */
public class ChatListener implements Listener {
	
	private final SimplyVanishCore core;
	
	public ChatListener(SimplyVanishCore core){
		this.core = core;
	}
	
	private final boolean shouldCancelChat(final String name) {
		final VanishConfig cfg = core.getVanishConfig(name, false);
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
	private final boolean shouldCancelCmd(final String name, String cmd) {
		final VanishConfig cfg = core.getVanishConfig(name, false);
		if (cfg == null) return false;
		if (!cfg.vanished.state) return false;
		if (!cfg.cmd.state){
			Settings settings = core.getSettings();
			final boolean contains = settings.cmdCommands.contains(cmd);
			if (settings.cmdWhitelist) return !contains;
			else return contains;
		}
		else return false;
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void onChat(PlayerChatEvent event){
		if (event.isCancelled()) return;
		// Just prevent accidental chat.
		final Player player = event.getPlayer();
		if (shouldCancelChat(player.getName())){
			event.setCancelled(true);
			msgCancel(player);
		}
	}
	
	@EventHandler(priority=EventPriority.LOW)
	void onCommandPreprocess(PlayerCommandPreprocessEvent event){
		if (event.isCancelled()) return;
		String cmd = event.getMessage().trim().split(" ", 2)[0].toLowerCase();
		if (cmd.length()>1) cmd = cmd.substring(1);
		// TODO: maybe find the fastest way to do this !
		final Player player = event.getPlayer();
		if (shouldCancelCmd(player.getName(), cmd)){
			event.setCancelled(true);
			msgCancel(player);
		}
	}

	private void msgCancel(Player player) {
		player.sendMessage(SimplyVanish.msgLabel+ChatColor.RED+"Disabled! (Flags: +chat or +cmd, or /reappear)");
	}
}
