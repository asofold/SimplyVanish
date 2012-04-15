package asofold.simplyvanish.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.MemoryConfiguration;

import asofold.simplyvanish.SimplyVanish;
import asofold.simplyvanish.config.compatlayer.CompatConfig;
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

	public static boolean allowOps = true;

	public static boolean superperms = true;

	/**
	 * All lower-case: Player -> permissions.
	 */
	public static final Map<String, Set<String>> fakePermissions = new HashMap<String, Set<String>>(); 
	
	public static final String[] defaultFakePermissions = new String[]{
		"all", "vanish.self",
	};

	public static final boolean defaultAllowOps = true;
	public static final boolean defaultSuperperms = true;
	
	/**
	 * Adjust internal settings to the given configuration.
	 * TODO: put this to plugin / some settings helper
	 * @param config
	 */
	public void applyConfig(CompatConfig config) {
		Settings ref = new Settings();
		// Exp workaround.
		expThreshold = config.getDouble("pickup.exp.workaround.distance.threshold", ref.expThreshold);
		expEnabled = config.getBoolean("pickup.exp.workaround.enabled", ref.expEnabled) && config.getBoolean("pickup.exp.workaround.active", true);
		expKillDist = config.getDouble("pickup.exp.workaround.distance.remove", ref.expKillDist);
		expTeleDist = config.getDouble("pickup.exp.workaround.distance.teleport", ref.expTeleDist);
		expVelocity = config.getDouble("pickup.exp.workaround.velocity", ref.expVelocity);
		// suppress mesages:
		suppressJoinMessage = config.getBoolean("messages.suppress.join", ref.suppressJoinMessage);
		suppressQuitMessage  = config.getBoolean("messages.suppress.quit", ref.suppressQuitMessage);
		// fake messages:
		sendFakeMessages = config.getBoolean("messages.fake.enabled", ref.sendFakeMessages);
		fakeJoinMessage = Utils.withChatColors(config.getString("messages.fake.join", ref.fakeJoinMessage));
		fakeQuitMessage = Utils.withChatColors(config.getString("messages.fake.quit", ref.fakeQuitMessage));
		// notify changing vanish stats
		notifyState = config.getBoolean("messages.notify.state.enabled", ref.notifyState);
		notifyStatePerm = config.getString("messages.notify.state.permission", ref.notifyStatePerm);
		// notify ping
		pingEnabled = config.getBoolean("messages.notify.ping.enabled", ref.pingEnabled);
		pingPeriod = config.getLong("messages.notify.ping.period", 0L) * 1000; // in seconds
		if (pingPeriod<=0) pingEnabled = false;
		// command aliases: see SimplyVanish plugin.
		saveVanished = config.getBoolean("save-vanished", ref.saveVanished);
		saveVanishedAlways = config.getBoolean("save-vanished-always", ref.saveVanishedAlways);
		saveVanishedInterval = config.getLong("save-vanished-interval", 0L)*60000;
		
		autoVanishUse = config.getBoolean("auto-vanish.use", ref.autoVanishUse);
		autoVanishPerm = config.getString("auto-vanish.permission", ref.autoVanishPerm);
		
		panicKickAll = config.getBoolean("panic.kick-all", false);
		panicKickInvolved =  config.getBoolean("panic.kick-involved", false);
		panicKickMessage = config.getString("panic.kick-message","[ERROR] Please log in again, contact staff.");
		
		panicMessage = config.getString("panic.message", "§a[SimplyVanish] §eAdmin notice: check the logs.");
		panicMessageTargets = config.getString("panic.message-targets", "ops");
		
		panicRunCommand = config.getBoolean("panic.run-command", false);
		panicCommand = config.getString("panic.command", "");
		
		noAbort = config.getBoolean("no-abort", ref.noAbort);
		
		allowOps = config.getBoolean("permissions.allow-ops", Settings.allowOps);
		superperms = config.getBoolean("permissions.superperms", Settings.superperms);
		fakePermissions.clear();
		Collection<String> keys = config.getStringKeys("permissions.players");
		if (keys != null){
			for (String perm : keys){
				System.out.println("key "+perm);
				List<String> players = config.getStringList("permissions.players."+perm, null);
				if (players == null) continue;
				for ( String player : players){
					System.out.println("player "+player);
					Set<String> perms = fakePermissions.get(player.trim().toLowerCase());
					if(perms == null){
						perms = new HashSet<String>();
						fakePermissions.put(player.trim().toLowerCase(), perms);
					}
					String part = perm.trim().toLowerCase();
					if ( part.startsWith("simplyvanish.")) perms.add(part);
					else perms.add("simplyvanish."+part);
				}
			}
		}
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
		// commands:
		for ( String cmd : SimplyVanish.baseLabels){
			defaults.set("commands."+cmd+".aliases", new LinkedList<String>());
		}
//		defaults.set("server-ping.subtract-vanished", false); // TODO: Feature request pending ...
		defaults.set("save-vanished", ref.saveVanished); // TODO: load/save vanished players.
		defaults.set("save-vanished-always", ref.saveVanishedAlways); // TODO: load/save vanished players.
		defaults.set("save-vanished-interval", ref.saveVanishedInterval/60000); // minutes
		
		defaults.set("auto-vanish.use", ref.autoVanishUse);
		defaults.set("auto-vanish.permission", ref.autoVanishPerm);
		defaults.set("no-abort", ref.noAbort);
		defaults.set("permissions.allow-ops", Settings.defaultAllowOps);
		defaults.set("permissions.superperms", Settings.defaultSuperperms);
		for ( String p : defaultFakePermissions){
			defaults.set("permissions.players."+p, new LinkedList<String>());
		}
		return defaults;
	}
}
