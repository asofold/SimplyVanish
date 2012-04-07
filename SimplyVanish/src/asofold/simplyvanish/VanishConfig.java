package asofold.simplyvanish;

/**
 * As it seems this is needed :)
 * 
 * Current design:
 * if in vanished: player is vanished.
 * if in parked: player is not vanished.
 * 
 * @author mc_dev
 *
 */
public class VanishConfig {
	
	/**
	 * Player wants to be able to pick up items.
	 */
	public boolean pickup = false;
	
	/**
	 * Player wants to be able to drop items.
	 */
	public boolean drop = false;
	
	/**
	 * Applies to potion effects and damage.
	 */
	public boolean damage = false;
	
	/**
	 * Player does not want to see other vanished players.<br>
	 * (Though he potentially might y permission to.)
	 */
	public boolean nosee = false;
	
	/**
	 * Player wants to repell experience orbs [might not work well].
	 */
	public boolean repellExp = false;
	
}
