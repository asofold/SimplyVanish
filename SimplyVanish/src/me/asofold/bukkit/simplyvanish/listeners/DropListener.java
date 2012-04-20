package me.asofold.bukkit.simplyvanish.listeners;

import me.asofold.bukkit.simplyvanish.SimplyVanishCore;
import me.asofold.bukkit.simplyvanish.config.VanishConfig;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class DropListener implements Listener {
	
	private final SimplyVanishCore core;
	
	public DropListener(SimplyVanishCore core){
		this.core = core;
	}
	
	@EventHandler(priority=EventPriority.LOW)
	void onItemDrop(PlayerDropItemEvent event){
		if ( event.isCancelled() ) return;
		Player player = event.getPlayer();
		VanishConfig cfg = core.getVanishConfig(player.getName(), false);
		if (cfg == null) return;
		if (!cfg.vanished.state) return;
		if (!cfg.drop.state) event.setCancelled(true);
	}
	
}
