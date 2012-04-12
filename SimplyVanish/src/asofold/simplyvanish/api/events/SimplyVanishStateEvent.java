package asofold.simplyvanish.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event is called when setVanished is used (on commands and API usage),<br>
 * it is not called for updateVanishState (!).<br>
 * NOTE: This could also be a SimplyVanishAtLoginEvent . 
 * @author mc_dev
 *
 */
public class SimplyVanishStateEvent extends Event implements SimplyVanishEvent{
	
	private static final HandlerList handlers = new HandlerList();
	
	private final String playerName;
	private final boolean visibleBefore;
	
	private boolean cancelled = false;
	
	private boolean visibleAfter;
	
	
	public SimplyVanishStateEvent(String playerName, boolean visibleBefore, boolean visibleAfter){
		this.visibleBefore = visibleBefore;
		this.visibleAfter = visibleAfter;
		this.playerName = playerName;
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
	   
	public boolean getVisibleBefore(){
		return visibleBefore;
	}
	
	public boolean getVisibleAfter(){
		return visibleAfter;
	}
	
	/**
	 * This forces a state, if you cancel the event, it may not be certain if the player will be visible or not.
	 * @param visible
	 */
	public void setVisibleAfter(boolean visible){
		visibleAfter = visible;
	}
	
	public String getPlayerName(){
		return playerName;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		cancelled = cancel;
	}

}
