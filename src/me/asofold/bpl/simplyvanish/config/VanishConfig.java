package me.asofold.bpl.simplyvanish.config;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * As it seems this is needed :)
 * 
 * 
 * TODO: ?: use PriorityValue for this.
 * 
 * to add (5 places) : member, readFromArray, toLine, needsSave, getChanges
 * 
 * @author mc_dev
 *
 */
public class VanishConfig implements Cloneable {
	
	/**
	 * Map alias names to standard name.
	 */
	protected static Map<String, String> mappedFlags = new HashMap<String, String>();
	
	static{
		for (String[] c : new String[][]{
				{"vanished", "vanish", "van", "va"}, // Not used for vanished flag.
				{"pickup", "pick", "pic",  "pck", "pi"},
				{"drop", "dro", "drp", "dr"},
				{"damage", "dam", "dmg", "dm", "da"},
				{"target", "targ", "tar", "tgt", "tg", "ta"},
				{"interact", "inter", "int", "in" },
				{"attack", "attac", "att", "at"},
				{"bypass", "byp", "by", "bp"},
				{"cmd", "command", "cm"}, // The only flag that maps to something shorter.
				{"chat", "cha" ,"ch"},
				{"god", "gd", "go"}, // maybe not used as a flag
				{"tell", "tel", "tl", "te"},
		}){
			for (int i = 1; i < c.length; i++){
				mappedFlags.put(c[i] , c[0]);
			}
		}
	}
	
	/**
	 * Used by the interact listener for fake inventory opening.
	 */
	public boolean preventInventoryAction = false;
	
	/**
	 * Flag for indicating that this might get saved.<br>
	 * Handled not too strictly (used like: setFlags, then vanish, vanish checks this flag).
	 */
	public boolean changed = false;
	
	/**
	 * Mapping names to flags.
	 */
	final Map<String, Flag> flags = new LinkedHashMap<String, Flag>(); // takes more space but is the fastest way.
	
	// False flags:
	
	/**
	 * "god"-mode flag.
	 */
	public final Flag god = addFlag("god", false);
	
	/**
	 * Player is vanished.
	 */
	public final Flag vanished = addFlag("vanished", false);
	
	/**
	 * Player wants to be able to pick up items.
	 */
	public final Flag pickup = addFlag("pickup", false);
	
	/**
	 * Player wants to be able to drop items.
	 */
	public final Flag drop = addFlag("drop", false);
	
	/**
	 * Applies to potion effects and damage.
	 */
	public final Flag damage = addFlag("damage", false);
	
	/**
	 * Become target of mobs.
	 */
	public final Flag target = addFlag("target", false);
	
	/**
	 * Allow the player to attack, or inflict damage on others.
	 */
	public final Flag attack = addFlag("attack", false);
	
	/**
	 * Allow interaction with the rest of the world, like changing blocks, entities.
	 */
	public final Flag interact = addFlag("interact", false);
	
	/**
	 * Bypass flag for interact.
	 */
	public final Flag bypass = addFlag("bypass", false);
	
	/**
	 * Allow chat.
	 */
	public final Flag chat = addFlag("chat", false);
	
	/**
	 * Put through tell messages, even though cancelled.
	 */
	public final Flag tell = addFlag("tell", false);
	
	/**
	 * Allow blocked cmds (...).
	 */
	public final Flag cmd = addFlag("cmd", false);
	
	// True flags:
	
	/**
	 * Player does not want to see other vanished players.<br>
	 * (Though he potentially might y permission to.)
	 */
	public final Flag see = addFlag("see", true);
	
	/**
	 * Player wants auto-vanish to be set. If set to null, default configuration behavior is used.
	 */
	public final Flag auto = addFlag("auto", true);

	/**
	 * Notify ping (periodic reminder of being vanished).
	 */
	public final Flag ping = addFlag("ping", true);
	
	/**
	 * Notify the player (at all) about their own vanish state changes. If set to false, this will override ping.
	 */
	public final Flag notify = addFlag("notify", true);
	
	/**
	 * "I am online": If set, fake join/leave messages are suppressed and suppression of actual join/leave messages is bypassed.
	 */
	public final Flag online = addFlag("online", false);
	
	
	protected Flag addFlag(String name, boolean preset){
		if (flags.containsKey(name)) throw new IllegalArgumentException("Flags must be unique, got: "+name);
		else if (name == null) throw new IllegalArgumentException("Flag is null: "+name);
		Flag flag = new Flag(name, preset);
		flags.put(name, flag);
		return flag;
	}
	
	/**
	 * Read flags from an array from start index on.
	 * @param parts
	 * @param startIndex
	 * @return
	 */
	public void readFromArray(String[] parts, int startIndex, boolean allowAll){
		for (int i = startIndex; i<parts.length; i++){
			String part = parts[i];
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
			else if (s.startsWith("*")){
				// (ignore these)
				continue;
			}
			else state = true;
			
			s = getMappedFlagName(s);
			if ( !allowAll && (s.equals("vanished") || s.equals("god"))  ) continue;
			
			Flag flag = flags.get(s);
			if (flag==null) continue; // should not happen by contract.
			if (state != flag.state){
				flag.state = state;
				changed = true;
			}
		}
	}

	/**
	 * Add all flags with a space in front of each, i.e. starting with a space.<br>
	 * Only adds the necessary ones.
	 * @return
	 */
	public String toLine(){
		StringBuilder out = new StringBuilder();
		for (Flag flag:flags.values()){
			if (flag.state == flag.preset) continue;
			out.append(" ");
			out.append(flag.toLine());
		}
		return out.toString();
	}
	
	/**
	 * Convenience method to save some space.<br>
	 * NOTE: This has nothing to do with the changed flag !
	 * @return
	 */
	public boolean needsSave(){
		for ( Flag flag : flags.values()){
			if (flag.preset != flag.state) return true;
		}
		return false;
	}

	/**
	 * Return mapped name or the name itself.<br>
	 * NOTE: Might get refactored to return some set or an array (sets of flags). 
	 * @param input must be lower-case.
	 * @return
	 */
	public static String getMappedFlagName(String input) {
		if (input.isEmpty()) return input;
		char c = input.charAt(0);
		if ( input.length()>1 && (c == '+' || c == '-' || c == '*')) input = input.substring(1);
		String mapped = mappedFlags.get(input);
		if (mapped == null) return input;
		else return mapped;
	}

	/**
	 * Return changed flags (including +/-).<br>
	 * This will also treat not present or newly added flags as changed and add their state. It always get the flags added according to "to", rather. If "to2 does not have that flag then it is "-".<br>
	 * (Maybe this is set up too general for the moment, but i do have some mix-ins in mind.)
	 * @param from
	 * @param to
	 * @return
	 */
	public static List<String> getChanges(VanishConfig from, VanishConfig to) {
		List<String> out = new LinkedList<String>();
		for (String name : from.flags.keySet()){
			Flag fromFlag = from.flags.get(name);
			Flag toFlag = to.flags.get(name);
			if (toFlag == null){
				out.add("-"+name);
				continue;
			}
			if (fromFlag.state != toFlag.state) out.add(toFlag.toLine());
		}
		for (String name : to.flags.keySet()){
			if(!from.flags.containsKey(name)) out.add(to.flags.get(name).toLine());
		}
		return out;
	}
	
	/**
	 * Delegate to getChanges(this, other).
	 * @param possiblyChanged
	 * @return
	 */
	public List<String> getChanges(VanishConfig possiblyChanged){
		return getChanges(this, possiblyChanged);
	}
	
	public boolean get(String name){
		return flags.get(name).state;
	}
	
	/**
	 * For setting existing flags, will fail if flag does not exist.
	 * @param flag
	 * @param state
	 */
	public void set(Flag flag, boolean state){
		set(flag.name, state);
	}
	
	/**
	 * For setting existing flags, will fail if flag does not exist.
	 * @param name
	 * @param state
	 */
	public void set(String name, boolean state){
		Flag flag = flags.get(name);
		if (flag.state != state){
			flag.state = state;
			changed = true;
		}
	}
	
	public void setAll(VanishConfig other){
		for (Entry<String, Flag> entry : other.flags.entrySet()){
			String n = entry.getKey();
			Flag flag = entry.getValue();
			if (has(n)) set(n, flag.state);
			else flags.put(n, flag.clone());
		}
	}
	
	public boolean has(String name){
		return flags.containsKey(name);
	}
	
	/**
	 * Clones but sets changed to true.
	 */
	public VanishConfig clone(){
		VanishConfig cfg = new VanishConfig();
		cfg.setAll(this);
		cfg.changed =  true;
		cfg.preventInventoryAction = preventInventoryAction;
		return cfg;
	}

	public List<Flag> getAllFlags() {
		List<Flag> all = new LinkedList<Flag>();
		all.addAll(flags.values());
		return all;
	}
	
}
