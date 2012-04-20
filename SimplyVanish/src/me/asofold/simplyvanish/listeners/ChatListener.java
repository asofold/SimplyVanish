package me.asofold.simplyvanish.listeners;

import me.asofold.simplyvanish.SimplyVanish;
import me.asofold.simplyvanish.SimplyVanishCore;
import me.asofold.simplyvanish.config.VanishConfig;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

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
	
	@EventHandler(priority=EventPriority.LOW)
	public void onChat(PlayerChatEvent event){
		if (event.isCancelled()) return;
		// Just prevent accidental chat.
		if (shouldCancelChat(event.getPlayer().getName())){
			event.setCancelled(true);
			msgCancel(event.getPlayer());
		}
	}
	
	@EventHandler(priority=EventPriority.LOW)
	void onCommandPreprocess(PlayerCommandPreprocessEvent event){
		if (event.isCancelled()) return;
		final String cmd = event.getMessage().trim().split(" ", 2)[0].toLowerCase();
		// TODO: maybe find the fastest way to do this !
		// TODO: check for blocked commands !
		// Just prevent accidental chat.
		if ( cmd.equals("/me ") && shouldCancelChat(event.getPlayer().getName())){
			event.setCancelled(true);
			msgCancel(event.getPlayer());
		}
	}

	private void msgCancel(Player player) {
		player.sendMessage(SimplyVanish.msgLabel+ChatColor.RED+"Disabled! Use /vanflag +chat or /reappear!");
	}
}
