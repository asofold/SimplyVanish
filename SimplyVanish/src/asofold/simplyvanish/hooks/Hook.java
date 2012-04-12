package asofold.simplyvanish.hooks;

import asofold.simplyvanish.config.VanishConfig;

/**
 * For hooks to add for vanish.<br>
 * Players might not be online or even might not exist at all.<br>
 * boolean results will lead to SimplyVanish not performing the action/change.<br>
 * The listeners will be registered with SimplyVanish as plugin.
 * @author mc_dev
 *
 */
public interface Hook {
	
	/**
	 * Identifier.
	 * @return
	 */
	public String getHookName();
	
	/**
	 * 
	 * @return null if all are supported.
	 */
	public HookPurpose[] getSupportedMethods();
	/**
	 * Get an event listener.
	 * @return null if not desired.
	 */
	public HookListener getListener();
	
	
	/**
	 * Executed before any player vanishes or logs in vanished.
	 * @param playerName
	 * @return If to allow to vanish.
	 */
	public boolean beforeVanish(String playerName);
	
	/**
	 * Executed after a player vanishes or logs in vanished.
	 * @param playerName
	 */
	public void afterVanish(String playerName);
	
	/**
	 * Executed before a player reappears.
	 * @param playerName
	 * @return If to reappear.
	 */
	public boolean beforeReappear(String playerName);
	
	/**
	 * Executed after a player reappears.
	 * @param playerName
	 */
	public void afterReappear(String playerName);
	
	/**
	 * 
	 * @param playerName
	 * @param oldCfg (clone)
	 * @param newCfg (clone)
	 * @return
	 */
	public boolean beforeSetFlags(String playerName, VanishConfig oldCfg, VanishConfig newCfg);
	
	/**
	 * 
	 * @param playerName
	 * @return
	 */
	public boolean afterSetFlags(String playerName);
	
}
