package me.asofold.bpl.simplyvanish.api.events;

/**
 * This event is only called when a player was vanished before or is intended to be vanished.<br>
 * For more details see:
 * @see SimplyVanishStateEvent
 * @author mc_dev
 *
 */
public class SimplyVanishAtLoginEvent extends SimplyVanishStateEvent {

	private final boolean autoVanish;
	
	public SimplyVanishAtLoginEvent(String playerName, boolean visibleBefore,
			boolean visibleAfter, boolean autoVanish) {
		super(playerName, visibleBefore, visibleAfter);
		this.autoVanish = autoVanish;
	}
	
	public boolean getAutoVanish(){
		return autoVanish;
	}

}
