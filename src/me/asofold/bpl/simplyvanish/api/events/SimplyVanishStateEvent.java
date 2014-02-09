package me.asofold.bpl.simplyvanish.api.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event is called when setVanished is used internally (on commands and API usage) or on player logins (see NOTE below), it is not called for updateVanishState, thus can be bypassed, also by API calls (!).<br>
 * If this event is canceled, no state updating will be performed, to just prevent state changes use the setVisibleAfter method.<br>
 * It is not sure that this event means a state change from visible to invisible or vice-versa, it could also be a forced state update.<br>
 * NOTE: This could also be a SimplyVanishAtLoginEvent which extends SimplyVanishStateEvent. 
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
	
	/**
	 * Convenience method.
	 * @return
	 */
	public Player getPlayer(){
		return Bukkit.getServer().getPlayerExact(playerName);
	}
	
	public boolean isPlayerOnline(){
		final Player player = getPlayer();
		if (player == null) return false;
		return player.isOnline();
	}

}
