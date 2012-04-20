package me.asofold.bukkit.simplyvanish.api.hooks;

import me.asofold.bukkit.simplyvanish.config.VanishConfig;

/**
 * Might be useful if you only want to override few methods, does nothing by default.
 * @author mc_dev
 *
 */
public abstract class AbstractHook implements Hook {

	@Override
	public abstract String getHookName();

	@Override
	public abstract HookPurpose[] getSupportedMethods();

	@Override
	public HookListener getListener() {
		return null;
	}

	@Override
	public void beforeVanish(String playerName) {
	}

	@Override
	public void afterVanish(String playerName) {
	}

	@Override
	public void beforeReappear(String playerName) {
	}

	@Override
	public void afterReappear(String playerName) {
	}

	@Override
	public void beforeSetFlags(String playerName, VanishConfig oldCfg,
			VanishConfig newCfg) {
	}

	@Override
	public void afterSetFlags(String playerName) {
	}

}
