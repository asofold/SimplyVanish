package me.asofold.bukkit.simplyvanish.api.hooks.impl;

import me.asofold.bukkit.simplyvanish.SimplyVanish;
import me.asofold.bukkit.simplyvanish.api.hooks.AbstractHook;
import me.asofold.bukkit.simplyvanish.api.hooks.HookListener;
import me.asofold.bukkit.simplyvanish.api.hooks.HookPurpose;
import me.asofold.bukkit.simplyvanish.api.hooks.util.HookPluginGetter;

import org.dynmap.bukkit.DynmapPlugin;

public class DynmapHook extends AbstractHook {
	
	private final HookPluginGetter<DynmapPlugin> getter;
	
	public DynmapHook(){
		getter = new HookPluginGetter<DynmapPlugin>("dynmap");
		if (getter.getPlugin() == null) throw new RuntimeException("Dynmap not found."); // To let it fail.
	}
	
	@Override
	public String getHookName() {
		return "dynmap";
	}

	@Override
	public HookPurpose[] getSupportedMethods() {
		return new HookPurpose[]{HookPurpose.LISTENER, HookPurpose.AFTER_REAPPEAR, HookPurpose.AFTER_VANISH};
	}

	@Override
	public HookListener getListener() {
		return getter;
	}

	@Override
	public void afterVanish(String playerName) {
		getter.getPlugin().setPlayerVisiblity(playerName, SimplyVanish.isVanished(playerName));
	}

	@Override
	public void afterReappear(String playerName) {
		getter.getPlugin().setPlayerVisiblity(playerName, SimplyVanish.isVanished(playerName));
	}

}
