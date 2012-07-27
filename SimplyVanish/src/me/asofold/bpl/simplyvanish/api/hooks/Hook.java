package me.asofold.bpl.simplyvanish.api.hooks;

import me.asofold.bpl.simplyvanish.config.VanishConfig;

import org.bukkit.entity.Player;

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
	 */
	public void beforeVanish(String playerName);
	
	/**
	 * Executed after a player vanishes or logs in vanished.
	 * @param playerName
	 */
	public void afterVanish(String playerName);
	
	/**
	 * Executed before a player reappears.
	 * @param playerName
	 */
	public void beforeReappear(String playerName);
	
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
	 */
	public void beforeSetFlags(String playerName, VanishConfig oldCfg, VanishConfig newCfg);
	
	/**
	 * 
	 * @param playerName
	 */
	public void afterSetFlags(String playerName);
	
	/**
	 * Called on updateVanishState.<br>
	 * All hooks will be called, even if a hook returrns false (!), see isAllowed if it was cancelled.
	 * @param player
	 * @param hookId The caller of updateVanishState, 0 = SimplyVanish (or an API call not specifying a hookId).
	 * @param isAllowed Will be set to false and stay false, if one hook returns false on allowUpdateVanishState.
	 * @return If false is returned, an update will not be performed. 
	 */
	public boolean allowUpdateVanishState(Player player, int hookId, boolean isAllowed);
	
	/**
	 * 
	 * @param player
	 * @param canSee
	 * @param isAllowed
	 * @return
	 */
	public boolean allowShow(Player player, Player canSee, boolean isAllowed);
	
	/**
	 * 
	 * @param player
	 * @param canNotSee
	 * @param isAllowed
	 * @return
	 */
	public boolean allowHide(Player player, Player canNotSee, boolean isAllowed);
	
}
