package me.asofold.bpl.simplyvanish.config;

public class Flag implements Cloneable{
	public final String name;
	public final boolean preset;
	public boolean state;
	
	public Flag(String name, boolean preset){
		this.name = name;
		this.preset = preset;
		state = preset;
	}
	
	/**
	 * Always returns "+/-name".
	 * @return
	 */
	public String toLine(){
		return fs(state)+name;
	}
	
	/**
	 * Flag state prefix.
	 * @param state
	 * @return
	 */
	public static String fs(boolean state){
		return state?"+":"-";
	}
	
	public Flag clone(){
		Flag flag = new Flag(name, preset);
		flag.state = state;
		return flag;
	}
}
