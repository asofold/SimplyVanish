package asofold.simplyvanish.hooks;

import asofold.simplyvanish.config.VanishConfig;

/**
 * Might be useful if you only want to override few methods, preset behavior is to allow everything.
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
	public boolean beforeVanish(String playerName) {
		return true;
	}

	@Override
	public void afterVanish(String playerName) {
	}

	@Override
	public boolean beforeReappear(String playerName) {
		return true;
	}

	@Override
	public void afterReappear(String playerName) {
	}

	@Override
	public boolean beforeSetFlags(String playerName, VanishConfig oldCfg,
			VanishConfig newCfg) {
		return true;
	}

	@Override
	public void afterSetFlags(String playerName) {
	}

}
