package me.asofold.bukkit.simplyvanish.api.hooks;

import org.bukkit.entity.Player;

import me.asofold.bukkit.simplyvanish.SimplyVanish;
import me.asofold.bukkit.simplyvanish.config.VanishConfig;

/**
 * Might be useful if you only want to override few methods, does nothing by default.
 * @author mc_dev
 *
 */
public abstract class AbstractHook implements Hook {
	
	protected int hookId = SimplyVanish.getNewHookId();

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

	@Override
	public boolean allowUpdateVanishState(Player player, int hookId, boolean isAllowed) {
		return true;
	}
}
