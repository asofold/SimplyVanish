package asofold.simplyvanish.hooks;

import java.util.Collection;

import org.bukkit.event.Listener;

/**
 * For hooks to add for vanish.<br>
 * Players might not be online or even might not exist at all.
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
	public HookSupport[] getSupportedMethods();
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
	
	public boolean beforeSetFlags(String playerName, Collection<String> changes);
	
	public boolean afterSetFlags(String playerName);
	
}
