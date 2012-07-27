package me.asofold.bpl.simplyvanish.api.hooks;

import org.bukkit.event.Listener;

public interface HookListener extends Listener {
	
	/**
	 * 
	 * @return If successful - false if impossible or not supported. May throw exceptions to indicate the same.
	 */
	public boolean unregisterEvents();
}
