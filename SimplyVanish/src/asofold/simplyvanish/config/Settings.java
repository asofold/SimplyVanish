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
	public long pingPeriod = 30000;

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
	 * @param path 
	 */
	public void applyConfig(CompatConfig config, Path path) {
		Settings ref = new Settings();
		// Exp workaround.
		expThreshold = config.getDouble(path.expThreshold, ref.expThreshold);
		expEnabled = config.getBoolean(path.expEnabled, ref.expEnabled) && config.getBoolean(path.expWorkaround+path.sep+"active", true);
		expKillDist = config.getDouble(path.expKillDist, ref.expKillDist);
		expTeleDist = config.getDouble(path.expTeleDist, ref.expTeleDist);
		expVelocity = config.getDouble(path.expVelocity, ref.expVelocity);
		// suppress mesages:
		suppressJoinMessage = config.getBoolean(path.suppressJoinMessage, ref.suppressJoinMessage);
		suppressQuitMessage  = config.getBoolean(path.suppressQuitMessage, ref.suppressQuitMessage);
		// fake messages:
		sendFakeMessages = config.getBoolean(path.sendFakeMessages, ref.sendFakeMessages);
		fakeJoinMessage = Utils.withChatColors(config.getString(path.fakeJoinMessage, ref.fakeJoinMessage));
		fakeQuitMessage = Utils.withChatColors(config.getString(path.fakeQuitMessage, ref.fakeQuitMessage));
		// notify changing vanish stats
		notifyState = config.getBoolean(path.notifyStateEnabled, ref.notifyState);
		notifyStatePerm = config.getString(path.notifyStatePerm, ref.notifyStatePerm);
		// notify ping
		pingEnabled = config.getBoolean(path.pingEnabled, ref.pingEnabled);
		pingPeriod = config.getLong(path.pingPeriod, ref.pingPeriod/1000) * 1000; // in seconds
		if (pingPeriod<=0) pingEnabled = false;
		// command aliases: see SimplyVanish plugin.
		saveVanished = config.getBoolean(path.saveVanished, ref.saveVanished);
		saveVanishedAlways = config.getBoolean(path.saveVanishedAlways, ref.saveVanishedAlways);
		saveVanishedInterval = config.getLong(path.saveVanishedInterval, ref.saveVanishedInterval/60000)*60000;
		
		autoVanishUse = config.getBoolean("auto-vanish.use", ref.autoVanishUse);
		autoVanishPerm = config.getString("auto-vanish.permission", ref.autoVanishPerm);
		
		panicKickAll = config.getBoolean(path.panicKickAll, ref.panicKickAll);
		panicKickInvolved =  config.getBoolean(path.panicKickInvolved, ref.panicKickInvolved);
		panicKickMessage = config.getString(path.panicKickMessage, ref.panicKickMessage);
		
		panicMessage = config.getString(path.panicMessage, "§a[SimplyVanish] §eAdmin notice: check the logs.");
		panicMessageTargets = config.getString(path.panicMessageTargets, "ops");
		
		panicRunCommand = config.getBoolean(path.panicRunCommand, false);
		panicCommand = config.getString(path.panicCommand, "");
		
		noAbort = config.getBoolean(path.noAbort, ref.noAbort);
		
		allowOps = config.getBoolean(path.allowOps, Settings.allowOps);
		superperms = config.getBoolean(path.superperms, Settings.superperms);
		fakePermissions.clear();
		Collection<String> keys = config.getStringKeys("permissions"+path.sep+"players");
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
	
	public static MemoryConfiguration getDefaultConfig(Path path){
		MemoryConfiguration defaults = new MemoryConfiguration();
		Settings ref = new Settings();
		// exp workaround:
		defaults.set(path.expEnabled, ref.expEnabled);
		defaults.set(path.expThreshold, ref.expThreshold);
		defaults.set(path.expTeleDist, ref.expTeleDist);
		defaults.set(path.expKillDist, ref.expKillDist);
		defaults.set(path.expVelocity, ref.expVelocity);
		// supress messages:
		defaults.set(path.suppressJoinMessage, ref.suppressJoinMessage);
		defaults.set(path.suppressQuitMessage, ref.suppressQuitMessage);
		// messages:
		defaults.set(path.sendFakeMessages, ref.sendFakeMessages);
		defaults.set(path.fakeJoinMessage, ref.fakeJoinMessage);
		defaults.set(path.fakeQuitMessage, ref.fakeQuitMessage);
		defaults.set(path.notifyStateEnabled, ref.notifyState);
		defaults.set(path.notifyStatePerm, ref.notifyStatePerm);
		defaults.set(path.pingEnabled, ref.pingEnabled);
		defaults.set(path.pingPeriod, ref.pingPeriod/1000); // seconds
		// commands:
		for ( String cmd : SimplyVanish.baseLabels){
			defaults.set("commands"+path.sep+cmd+path.sep+"aliases", new LinkedList<String>());
		}
//		defaults.set("server-ping.subtract-vanished", false); // TODO: Feature request pending ...
		defaults.set(path.saveVanished, ref.saveVanished); // TODO: load/save vanished players.
		defaults.set(path.saveVanishedAlways, ref.saveVanishedAlways); // TODO: load/save vanished players.
		defaults.set(path.saveVanishedInterval, ref.saveVanishedInterval/60000); // minutes
		
		defaults.set(path.autoVanishUse, ref.autoVanishUse);
		defaults.set(path.autoVanishPerm, ref.autoVanishPerm);
		defaults.set(path.noAbort, ref.noAbort);
		defaults.set(path.allowOps, Settings.defaultAllowOps);
		defaults.set(path.superperms, Settings.defaultSuperperms);
		for ( String p : defaultFakePermissions){
			defaults.set("permissions"+path.sep+"players"+path.sep+p, new LinkedList<String>());
		}
		return defaults;
	}
}
