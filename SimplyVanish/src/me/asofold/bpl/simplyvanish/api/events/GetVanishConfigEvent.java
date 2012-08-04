package me.asofold.bpl.simplyvanish.api.events;

import me.asofold.bpl.simplyvanish.config.VanishConfig;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fire this event to get a VanishConfig, it will be set at lowest level.<br>
 * However some methods of VanishConfig might not be thread safe (static),
 * though should remain unchanged usually.
 * @author mc_dev
 *
 */
public class GetVanishConfigEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	
	private final String playerName;
	
	private boolean create;

	private VanishConfig cfg = null;
 
	
	public GetVanishConfigEvent(String playerName, boolean create){
		this.playerName = playerName;
		this.create = create;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	/**
	 * Must have :_) ...
	 * @return
	 */
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	public String getPlayerName(){
		return playerName;
	}
	
	public boolean getCreate(){
		return create;
	}
	
	/**
	 * Sets cfg.clone() (!).
	 * @param cfg
	 */
	public void setVanishConfig(VanishConfig cfg){
		if (cfg == null) this.cfg = null;
		else this.cfg = cfg.clone();
	}
	
	public VanishConfig getVanishConfig(){
		return cfg;
	}

}
