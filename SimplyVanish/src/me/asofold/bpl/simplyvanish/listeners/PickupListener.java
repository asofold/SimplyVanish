package me.asofold.bpl.simplyvanish.listeners;

import me.asofold.bpl.simplyvanish.SimplyVanishCore;
import me.asofold.bpl.simplyvanish.config.VanishConfig;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;

public final class PickupListener implements Listener {
	private final SimplyVanishCore core;
	public PickupListener(final SimplyVanishCore core){
		this.core = core;
	}
	@EventHandler(priority=EventPriority.LOW)
	final void onItemPickUp(final PlayerPickupItemEvent event){
		if ( event.isCancelled() ) return;
		final Player player = event.getPlayer();
		final VanishConfig cfg = core.getVanishConfig(player.getName(), false);
		if (cfg == null) return;
		if (!cfg.vanished.state) return;
		if (!cfg.pickup.state) event.setCancelled(true);
	}
}
