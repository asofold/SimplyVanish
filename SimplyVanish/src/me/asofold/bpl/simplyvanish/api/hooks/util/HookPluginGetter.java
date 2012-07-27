package me.asofold.bpl.simplyvanish.api.hooks.util;

import me.asofold.bpl.simplyvanish.api.hooks.HookListener;

import org.bukkit.plugin.Plugin;

public class HookPluginGetter<T extends Plugin> extends PluginGetter<T> implements HookListener{

	public HookPluginGetter(String pluginName) {
		super(pluginName);
	}

	@Override
	public boolean unregisterEvents() {
		// Override once it exists in bukkit.
		return false;
	}
}
