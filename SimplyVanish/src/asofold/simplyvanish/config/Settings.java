package asofold.simplyvanish.config;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.MemoryConfiguration;

import asofold.simplyvanish.util.Utils;


public class Settings {
	/**
	 * exp-workaround
	 */
	public double expThreshold = 3.0;
	
	/**
	 * exp-workaround
	 */
	public double expTeleDist = 1.0;
	
	/**
	 * exp-workaround
	 */
	public double expKillDist = 0.5;
	
	/**
	 * exp-workaround
	 */
	public double expVelocity = 0.3;
	
	/**
	 * Exp workaround
	 */
	public boolean expEnabled = true;

	public boolean suppressJoinMessage = false;
	public boolean suppressQuitMessage = false;

	public boolean sendFakeMessages = false;
	public String fakeJoinMessage = "&e%name joined the game.";
	public String fakeQuitMessage = "&e%name left the game.";

	public boolean notifyState = false;
	public String notifyStatePerm = "simplyvanish.see-all";
	
	public boolean panicKickAll = false;
	public boolean panicKickInvolved = false;
	public String panicKickMessage = "[ERROR] Please log in again, contact staff.";
	public String panicMessage = "§a[SimplyVanish] §eAdmin notice: check the logs.";
	public String panicMessageTargets = "ops";
	public boolean panicRunCommand = false;
	public String panicCommand = "";
	
	public boolean saveVanished = true;
	public boolean saveVanishedAlways = true;
	/**
	 * Stored in milliseconds, read from config in minutes.
	 */
	public long saveVanishedInterval = 0; 
	
	public boolean autoVanishUse = false;
	public String autoVanishPerm = "simplyvanish.auto-vanish";
	
	public boolean noAbort = false;
	
	public boolean pingEnabled = false;
	/**
	 * Stored in milliseconds, read from config as seconds.
	 */
	public long pingPeriod = 60000; 
	
	
	
	/**
	 * Adjust internal settings to the given configuration.
	 * TODO: put this to plugin / some settings helper
	 * @param config
	 */
	public void applyConfig(Configuration config) {
		// Exp workaround.
		expThreshold = config.getDouble("pickup.exp.workaround.distance.threshold");
		expEnabled = config.getBoolean("pickup.exp.workaround.enabled") && config.getBoolean("pickup.exp.workaround.active", true);
		expKillDist = config.getDouble("pickup.exp.workaround.distance.remove");
		expTeleDist = config.getDouble("pickup.exp.workaround.distance.teleport");
		expVelocity = config.getDouble("pickup.exp.workaround.velocity");
		// suppress mesages:
		suppressJoinMessage = config.getBoolean("messages.suppress.join");
		suppressQuitMessage  = config.getBoolean("messages.suppress.quit");
		// fake messages:
		sendFakeMessages = config.getBoolean("messages.fake.enabled");
		fakeJoinMessage = Utils.withChatColors(config.getString("messages.fake.join"));
		fakeQuitMessage = Utils.withChatColors(config.getString("messages.fake.quit"));
		// notify changing vanish stats
		notifyState = config.getBoolean("messages.notify.state.enabled");
		notifyStatePerm = config.getString("messages.notify.state.permission");
		// notify ping
		pingEnabled = config.getBoolean("messages.notify.ping.enabled");
		pingPeriod = config.getLong("messages.notify.ping.period", 0) * 1000; // in seconds
		if (pingPeriod<=0) pingEnabled = false;
		// command aliases: see SimplyVanish plugin.
		saveVanished = config.getBoolean("save-vanished");
		saveVanishedAlways = config.getBoolean("save-vanished-always");
		saveVanishedInterval = config.getInt("save-vanished-interval", 0)*60000;
		
		autoVanishUse = config.getBoolean("auto-vanish.use");
		autoVanishPerm = config.getString("auto-vanish.permission");
		
		panicKickAll = config.getBoolean("panic.kick-all", false);
		panicKickInvolved =  config.getBoolean("panic.kick-involved", false);
		panicKickMessage = config.getString("panic.kick-message","[ERROR] Please log in again, contact staff.");
		
		panicMessage = config.getString("panic.message", "§a[SimplyVanish] §eAdmin notice: check the logs.");
		panicMessageTargets = config.getString("panic.message-targets", "ops");
		
		panicRunCommand = config.getBoolean("panic.run-command", false);
		panicCommand = config.getString("panic.command", "");
		
		noAbort = config.getBoolean("no-abort");
	}
	
	public static MemoryConfiguration getDefaultConfig(){
		MemoryConfiguration defaults = new MemoryConfiguration();
		Settings ref = new Settings();
		// exp workaround:
		defaults.set("pickup.exp.workaround.enabled", ref.expEnabled);
		defaults.set("pickup.exp.workaround.distance.threshold", ref.expThreshold);
		defaults.set("pickup.exp.workaround.distance.teleport", ref.expTeleDist);
		defaults.set("pickup.exp.workaround.distance.remove", ref.expKillDist);
		defaults.set("pickup.exp.workaround.velocity", ref.expVelocity);
		// supress messages:
		defaults.set("messages.suppress.join", ref.suppressJoinMessage);
		defaults.set("messages.suppress.quit", ref.suppressQuitMessage);
		// messages:
		defaults.set("messages.fake.enabled", ref.sendFakeMessages);
		defaults.set("messages.fake.join", ref.fakeJoinMessage);
		defaults.set("messages.fake.quit", ref.fakeQuitMessage);
		defaults.set("messages.notify.state.enabled", ref.notifyState);
		defaults.set("messages.notify.state.permission", ref.notifyStatePerm);
		defaults.set("messages.notify.ping.enabled", ref.pingEnabled);
		defaults.set("messages.notify.ping.period", ref.pingPeriod/1000); // seconds
//		// commands:
//		for ( String cmd : SimplyVanish.baseLabels){
//			defaults.set("commands."+cmd+".aliases", new LinkedList<String>());
//		}
//		defaults.set("server-ping.subtract-vanished", false); // TODO: Feature request pending ...
		defaults.set("save-vanished", ref.saveVanished); // TODO: load/save vanished players.
		defaults.set("save-vanished-always", ref.saveVanishedAlways); // TODO: load/save vanished players.
		defaults.set("save-vanished-interval", ref.saveVanishedInterval/60000); // minutes
		
		defaults.set("auto-vanish.use", ref.autoVanishUse);
		defaults.set("auto-vanish.permission", ref.autoVanishPerm);
		defaults.set("no-abort", ref.noAbort);
		return defaults;
	}
}
