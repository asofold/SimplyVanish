package me.asofold.bpl.simplyvanish.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.asofold.bpl.simplyvanish.command.SimplyVanishCommand;
import me.asofold.bpl.simplyvanish.config.compatlayer.CompatConfig;
import me.asofold.bpl.simplyvanish.config.compatlayer.CompatConfigFactory;
import me.asofold.bpl.simplyvanish.config.compatlayer.ConfigUtil;
import me.asofold.bpl.simplyvanish.util.Utils;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;



public class Settings {
	
	// static
	public static final String[][] presetPermSets = new String[][]{
		{"all"},
		{"vanish.self", "reappear.self", "flags.display.self", "flags.set.self.drop"},
	};
	
	/**
	 * Some bypass blocks typical for inspection and use:
	 */
	public static final int[] presetBypassBlocks = new int[]{
		23, // dispenser
		54,  // chest
		61, 62, // furnace
		58, // crafting table
		116, // Enchantment table
		117, // Brewing stand
		118, // Cauldron
	};
	
	public static final EntityType[] presetBypassEntities = new EntityType[]{
		EntityType.MINECART,
	};

	public static final String[] defaultFLagCmds = new String[]{
		"me", "tell", "vantell", "tellvan",
	};

	private static final String[][] defaultFlagSets = new String[][]{
		{"cc", "+cmd +chat"},
		{"cl", "*clear"},
		{"act", "+damage +attack +target +interact +pickup +drop"}, 
	};
	
	
	// non static:
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
	public String panicMessage = "�a[SimplyVanish] �eAdmin notice: check the logs.";
	public String panicMessageTargets = "ops";
	public boolean panicRunCommand = false;
	public String panicCommand = "";
	
	public boolean saveVanished = true;
	public boolean saveVanishedAlways = true;
	/**
	 * Stored in milliseconds, read from config in minutes.
	 */
	public long saveVanishedInterval = 0; 
	/**
	 * Stored in milliseconds, read from config in SECONDS.
	 */
	public long saveVanishedDelay = 10000;
	
	public boolean autoVanishUse = false;
	public String autoVanishPerm = "simplyvanish.auto-vanish";
	
	public boolean noAbort = false;
	
	public boolean pingEnabled = false;
	/**
	 * Stored in milliseconds, read from config as seconds.
	 */
	public long pingPeriod = 30000;

	public boolean allowOps = true;

	public boolean superperms = true;

	/**
	 * All lower-case: Player -> permissions.
	 */
	public final Map<String, Set<String>> fakePermissions = new HashMap<String, Set<String>>(); 
	
	public boolean addExtendedConfiguration = true;
	
	public final Set<Integer> bypassBlocks = new HashSet<Integer>();
	public final Set<EntityType> bypassEntities = new HashSet<EntityType>();
	
	public boolean bypassIgnorePermissions = true;
	
	
	public boolean cmdWhitelist = false;
	
	public final Set<String> cmdCommands = new HashSet<String>();
	
	public final Map<String, String[]> flagSets = new HashMap<String, String[]>();
	
	public List<String> loadPlugins = new LinkedList<String>();
	
	/**
	 * If to log vantell messages.
	 */
	public boolean logVantell = false;
	
	public boolean mirrorVantell = true;
	
	public boolean allowRealPeek = true;
	
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
		// suppress messages:
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
		saveVanished = config.getBoolean(path.saveVanishedEnabled, ref.saveVanished);
		saveVanishedAlways = config.getBoolean(path.saveVanishedAlways, ref.saveVanishedAlways);
		saveVanishedInterval = config.getLong(path.saveVanishedInterval, ref.saveVanishedInterval/60000)*60000;
		saveVanishedDelay = config.getLong(path.saveVanishedDelay, ref.saveVanishedDelay/1000) * 1000;
		
		autoVanishUse = config.getBoolean("auto-vanish.use", ref.autoVanishUse);
		autoVanishPerm = config.getString("auto-vanish.permission", ref.autoVanishPerm);
		
		panicKickAll = config.getBoolean(path.panicKickAll, ref.panicKickAll);
		panicKickInvolved =  config.getBoolean(path.panicKickInvolved, ref.panicKickInvolved);
		panicKickMessage = config.getString(path.panicKickMessage, ref.panicKickMessage);
		
		panicMessage = config.getString(path.panicMessage, "�a[SimplyVanish] �eAdmin notice: check the logs.");
		panicMessageTargets = config.getString(path.panicMessageTargets, "ops");
		
		panicRunCommand = config.getBoolean(path.panicRunCommand, false);
		panicCommand = config.getString(path.panicCommand, "");
		
		noAbort = config.getBoolean(path.noAbort, ref.noAbort);
		addExtendedConfiguration = config.getBoolean(path.addExtended, ref.addExtendedConfiguration);
		
		allowOps = config.getBoolean(path.allowOps, ref.allowOps);
		superperms = config.getBoolean(path.superperms, ref.superperms);
		
		// Bypasses:
		bypassIgnorePermissions = config.getBoolean(path.flagsBypassIgnorePermissions, ref.bypassIgnorePermissions);
		bypassBlocks.clear();
		bypassBlocks.addAll(getIdList(config.getStringList(path.flagsBypassBlocks, null)));
		bypassEntities.clear();
		bypassEntities.addAll(getEntityList(config.getStringList(path.flagsBypassEntities, null)));
		
		loadPlugins.clear();
		loadPlugins.addAll(config.getStringList(path.loadPlugins, new LinkedList<String>()));
		
		logVantell = config.getBoolean(path.cmdVantellLog, ref.logVantell);
		mirrorVantell = config.getBoolean(path.cmdVantellMirror, ref.mirrorVantell);
		
		allowRealPeek = config.getBoolean(path.allowRealPeek, ref.allowRealPeek);
		
		// cmd flag:
		cmdWhitelist = config.getBoolean(path.flagsCmdWhitelist, ref.cmdWhitelist);
		cmdCommands.clear();
		List<String> cmds = config.getStringList(path.flagsCmdCommands, null);
		if (cmds != null){
			for (String cmd : cmds){
				cmd = cmd.trim().toLowerCase();
				if (cmd.isEmpty()) continue;
				else cmdCommands.add(cmd);
			}
		}
		
		List<String> flagSetNames = config.getStringKeys(path.flagSets);
		flagSets.clear();
		for (String key : flagSetNames){
			String flags = config.getString(path.flagSets+"."+key);
			if (flags == null) continue;
			String lcKey = key.trim().toLowerCase();
			flagSets.put(lcKey, flags.split(" "));
		}
		
		// Command aliases: are set in another place !
		
		// Fake permissions:
		fakePermissions.clear();
		String inUse = "";
		Collection<String> keys = config.getStringKeys(path.permSets);
		if (keys != null){
			for (String setName : keys){
				final String base = path.permSets + path.sep + setName + path.sep;
				List<String> perms = config.getStringList(base + path.keyPerms);
				List<String> players = config.getStringList(base + path.keyPlayers);
				if (perms == null || players == null || perms.isEmpty()){
					Utils.warn("Missing entries in fake permissions set: "+setName);
					continue;
				}
				if (players.isEmpty()) continue; // just skip;
				for ( String n : players){
					inUse += " "+n;
					String lcn = n.trim().toLowerCase();
					Set<String> permSet = fakePermissions.get(lcn);
					if (permSet == null){
						permSet = new HashSet<String>();
						fakePermissions.put(lcn, permSet);
					}
					for ( String p : perms){
						String part = p.trim().toLowerCase();
						if ( part.startsWith("simplyvanish.")) permSet.add(part);
						else permSet.add("simplyvanish."+part);
					}
				}
			}
		}
		if (!inUse.isEmpty()) Utils.warn("Fake permissions in use for: "+inUse);
	}
	
	private List<EntityType> getEntityList(List<String> entries) {
		List<EntityType> out = new LinkedList<EntityType>();
		if (entries == null) return out;
		for (String entry : entries){
			EntityType type = null;
			try{
				type = EntityType.valueOf(entry.trim().toUpperCase().replace(" ", "_"));
			} catch (Throwable t){
			}
			if (type != null) out.add(type);
			else Utils.warn("Unrecognized entity definition: "+entry);
		}
		return out;
	}

	private List<Integer> getIdList(List<String> blocks) {
		List<Integer> out = new LinkedList<Integer>();
		if ( blocks == null) return out;
		for ( String entry : blocks){
			Material mat = null;
			try{
				mat = Material.matchMaterial(entry.trim().toUpperCase());
			} 
			catch (Throwable t){
			}
			if (mat != null){
				out.add(mat.getId());
				continue;
			}
			try{
				mat = Material.getMaterial(Integer.parseInt(entry.trim()));
			}
			catch (Throwable t){	
			}
			if (mat != null){
				out.add(mat.getId());
				continue;
			}
			Utils.warn("Unrecognized block definition: "+entry);
		}
		return out;
	}

	/**
	 * Only contain values that are safe to add if the key is not present.
	 * @param path
	 * @return
	 */
	public static CompatConfig getSimpleDefaultConfig(Path path){
		CompatConfig defaults = CompatConfigFactory.getConfig(null);
		defaults.setPathSeparatorChar(path.sep);
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
//		defaults.set("server-ping.subtract-vanished", false); // TODO: Feature request pending ...
		defaults.set(path.saveVanishedEnabled, ref.saveVanished); // TODO: load/save vanished players.
		defaults.set(path.saveVanishedAlways, ref.saveVanishedAlways); // TODO: load/save vanished players.
		defaults.set(path.saveVanishedInterval, ref.saveVanishedInterval/60000); // minutes
		defaults.set(path.saveVanishedDelay, ref.saveVanishedDelay/1000); // SECONDS
		
		defaults.set(path.autoVanishUse, ref.autoVanishUse);
		defaults.set(path.autoVanishPerm, ref.autoVanishPerm);
		defaults.set(path.noAbort, ref.noAbort);
		defaults.set(path.allowOps, ref.allowOps);
		defaults.set(path.superperms, ref.superperms);
		
		defaults.set(path.addExtended, ref.addExtendedConfiguration);
		
		defaults.set(path.flagsBypassIgnorePermissions, ref.bypassIgnorePermissions);
		
		defaults.set(path.flagsCmdWhitelist, ref.cmdWhitelist);
		List<String> cmds = new LinkedList<String>();
		for (String cmd: defaultFLagCmds){
			cmds.add(cmd);
		}
		defaults.set(path.flagsCmdCommands, cmds);
		
		defaults.set(path.loadPlugins, ref.loadPlugins);
		
		defaults.set(path.cmdVantellLog, ref.logVantell);
		defaults.set(path.cmdVantellMirror, ref.mirrorVantell);
		
		defaults.set(path.allowRealPeek, ref.allowRealPeek);
		
		// Sets are not added, for they can interfere.
		
		return defaults;
	}

	public static boolean addDefaults(CompatConfig config, Path path) {
		boolean changed  = false;
		// Add more complex defaults:
		if (!config.contains(path.flagsBypass)){
			List<String> blocks = new LinkedList<String>();
			for (Integer id : presetBypassBlocks){
				blocks.add(id.toString());
			}
			config.set(path.flagsBypassBlocks, blocks);
			List<String> entities = new LinkedList<String>();
			for (EntityType entity : presetBypassEntities){
				entities.add(entity.toString());
			}
			config.set(path.flagsBypassEntities, entities);
			changed = true;
		}
		for (String p : path.deprecated){
			if (config.contains(p)){
				config.remove(p);
				changed = true;
			}
		}
		// Add simple default entries:
		changed |= ConfigUtil.forceDefaults(getSimpleDefaultConfig(path), config);
		// Add more complex defaults:
		if (!config.contains(path.flagsBypassBlocks)) config.set(path.flagsBypassBlocks, new LinkedList<String>());
		if (!config.contains(path.flagsBypassEntities)) config.set(path.flagsBypassEntities, new LinkedList<String>());	
		
		// Return if no extended entries desired:
		if (!config.getBoolean(path.addExtended, true)) return changed;
		// Fake permissions example entries:
		if (!config.contains(path.permSets)){
			final String base = path.permSets + path.sep + "set";
			int i = 0;
			for (String[] perms : presetPermSets){
				i ++;
				List<String> entries = new LinkedList<String>();
				for ( String e : perms){
					entries.add(e);
				}
				final String prefix =  base + i + path.sep;
				config.set( prefix + path.keyPerms, entries);
				config.set( prefix + path.keyPlayers, new LinkedList<String>());
			}
			changed = true;
		}
		// Command aliases:
		for ( String cmd : SimplyVanishCommand.baseLabels){
			String p = "commands"+path.sep+cmd+path.sep+"aliases";
			if (!config.contains(p)){
				config.set(p, new LinkedList<String>());
				changed = true;
			}
		}
		
		if (!config.contains(path.flagSets)){
			for (String[] entry : defaultFlagSets){
				config.set(path.flagSets + path.sep + entry[0], entry[1]);
			}
			changed = true;
		}
		return changed;
	}
	
}
