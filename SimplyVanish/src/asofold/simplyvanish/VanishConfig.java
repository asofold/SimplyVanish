package asofold.simplyvanish;

/**
 * As it seems this is needed :)
 * 
 * Current design:
 * if in vanished: player is vanished.
 * if in parked: player is not vanished.
 * 
 * TODO: use PriorityValue for this.
 * 
 * @author mc_dev
 *
 */
public class VanishConfig {
	
	/**
	 * Player is vanished.
	 */
	public boolean vanished = false;
	
	/**
	 * Player does not want to see other vanished players.<br>
	 * (Though he potentially might y permission to.)
	 */
	public boolean see = true;
	
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
	 * Player wants auto-vanish to be set. If set to null, default configuration behavior is used.
	 */
	public Boolean auto = null;
	
	/**
	 * Read flags from an array from start index on.
	 * @param parts
	 * @param startIndex
	 * @return
	 */
	public void readFromArray(String[] parts, int startIndex, boolean allowVanish){
		for ( String part : parts){
			String s = part.trim().toLowerCase();
			if (s.isEmpty() || s.length()<2) continue;
			boolean state = false;
			if (s.startsWith("+")){
				state = true;
				s = s.substring(1);
			}
			else if (s.startsWith("-")){
				state = false;
				s = s.substring(1);
			}
			else state = true;
			if ( s.equals("vanish") || s.equals("vanished")){
				if (allowVanish) vanished = state;
			}
			else if (s.equals("see")) see = state;
			else if (s.equals("pickup") || s.equals("pick")) pickup = state;
			else if (s.equals("drop")) drop = state;
			else if (s.equals("damage") || s.equals("dam") || s.equals("dmg")) damage = state; 
			else if (s.equals("auto")) auto = state;
		}
	}

	/**
	 * Add all flags with a space in front of each, i.e. starting with a space.<br>
	 * Only adds the necessary ones.
	 * @return
	 */
	public String toLine(){
		StringBuilder out = new StringBuilder();
		if (vanished) out.append(" +vanished");
		if (!see) out.append(" -see");
		if (pickup) out.append(" +pickup");
		if (drop) out.append(" +drop");
		if (damage) out.append(" +damage");
		if (auto != null) out.append(" "+(auto?"+":"-")+"auto");
		return out.toString();
	}
	
	/**
	 * Convenience method to save some space.
	 * @return
	 */
	public boolean needsSave(){
		return vanished || !see  || pickup || drop || damage || auto!=null;
	}
	
}
