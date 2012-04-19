package me.asofold.simplyvanish.listeners;

import me.asofold.simplyvanish.SimplyVanishCore;
import me.asofold.simplyvanish.config.VanishConfig;

import org.bukkit.event.Listener;

public class InteractListener implements Listener {
	private final SimplyVanishCore core;
	public InteractListener(SimplyVanishCore core){
		this.core = core;
	}
	
	private final boolean shouldCancel(final String name) {
		final VanishConfig cfg = core.getVanishConfig(name, false);
		if (cfg == null) return false;
		if (!cfg.vanished.state || cfg.interact.state) return false;
		return true;
	}
	
	
}
