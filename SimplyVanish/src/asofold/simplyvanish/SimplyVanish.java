package asofold.simplyvanish;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import asofold.simplyvanish.api.hooks.Hook;
import asofold.simplyvanish.command.SimplyVanishCommand;
import asofold.simplyvanish.config.Path;
import asofold.simplyvanish.config.Settings;
import asofold.simplyvanish.config.VanishConfig;
import asofold.simplyvanish.config.compatlayer.CompatConfig;
import asofold.simplyvanish.config.compatlayer.CompatConfigFactory;
import asofold.simplyvanish.stats.Stats;
import asofold.simplyvanish.util.Utils;

/**
 * Example plugin for the vanish API as of CB 1914 !
 * Vanish + God mode + No Target + No pickup.
 * @author mc_dev
 *
 */
public class SimplyVanish extends JavaPlugin {
	
	static final SimplyVanishCore core = new SimplyVanishCore();

	public static final String[] baseLabels = new String[]{
		"vanish", "reappear", "tvanish", "simplyvanish","vanished", "vanflag",
	};
	
	public static final String cmdNoOpArg = "??NOOP??";
	public static final String cmdNoOp = "simplyvanish "+cmdNoOpArg;
	
	public static final String msgLabel = ChatColor.GOLD+"[SimplyVanish]"+ChatColor.GRAY+" ";
	public static final String msgStillInvisible =  SimplyVanish.msgLabel+ChatColor.GRAY+"You are still "+ChatColor.GREEN+"invisible"+ChatColor.GRAY+" to normal players.";
	public static final String msgNowInvisible = SimplyVanish.msgLabel+ChatColor.GRAY+"You are now "+ChatColor.GREEN+"invisible"+ChatColor.GRAY+" to normal players.";
	public static final String msgNotifyPing = SimplyVanish.msgLabel+ChatColor.GRAY+"You are "+ChatColor.GREEN+"invisible"+ChatColor.GRAY+", right now.";
	public static final String msgNoFlags = SimplyVanish.msgLabel+ChatColor.GRAY+"Flags are at default values.";

	public static final Stats stats = new Stats(msgLabel.trim()+"[STATS]");
	public static final Integer statsUpdateVanishState = stats.getNewId("UpdateVanishState");
	public static final Integer statsVanish= stats.getNewId("Vanish");
	public static final Integer statsReappear= stats.getNewId("Reappear");
	public static final Integer statsSetFlags = stats.getNewId("SetFlags");
	public static final Integer statsSave = stats.getNewId("SaveData");;
	static{
		stats.setLogStats(false);
	}
	
	SimplyVanishCommand cmdExe;
	
	/**
	 * Constructor: set some defualt configuration values.
	 */
	public SimplyVanish(){
		cmdExe = new SimplyVanishCommand(core);
	}
	
	@Override
	public void onDisable() {
		if (core.settings.saveVanished) core.saveVanished();
		core.setEnabled(false);
		core.setPlugin(null);
		// TODO: maybe let all players see each other again?
		System.out.println("[SimplyVanish] Disabled.");
	}

	@Override
	public void onEnable() {
		core.setPlugin(this);
		core.setVanishedFile(new File(getDataFolder(), "vanished.dat"));
		removeAllHooks();
		// load settings
		loadSettings(); // will also load vanished players
		// just in case quadratic time checking:
		for ( Player player : getServer().getOnlinePlayers()){
			core.updateVanishState(player);
		}
		// register events:
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(core, this);
		pm.registerEvents(cmdExe.aliasManager, this);
		// finished enabling.
		core.setEnabled(true);
		core.addStandardHooks();
		System.out.println("[SimplyVanish] Enabled");
	}

	/**
	 * Force reloading the config.
	 */
	public void loadSettings() {
		BukkitScheduler sched = getServer().getScheduler();
		sched.cancelTasks(this);
		CompatConfig config = CompatConfigFactory.getConfig(new File(getDataFolder(), "config.yml"));
		final Path path;
//		if (config.setPathSeparatorChar('/')){
//			path = new Path('/');
//		} else{
//			// This would  render some things inoperable (permissions with dot as keys).
			path = new Path('.');
//		}
	    boolean changed = false;
	    Settings settings = new Settings();
		try{
			config.load();
			changed = Settings.addDefaults(config, path);
			settings.applyConfig(config, path);
		} catch (Throwable t){
			Utils.severe("Failed to load the configuration, continue with default settings. ", t);
			settings = new Settings();
		}
		core.setSettings(settings);
		cmdExe.registerCommandAliases(config, path);
		if (changed) config.save(); // TODO: maybe check for changes, somehow ?
		if (settings.saveVanished) core.loadVanished();
		if (settings.pingEnabled){
			final long period = Math.max(settings.pingPeriod/50, 200);
			sched.scheduleSyncRepeatingTask(this, new Runnable(){
				@Override
				public void run() {
					core.onNotifyPing();
				}
			}, period, period);
		}
		if (settings.saveVanishedInterval > 0){
			final long period = settings.saveVanishedInterval/50;
			sched.scheduleSyncRepeatingTask(this, new Runnable(){
				@Override
				public void run() {
					core.saveVanished();
				}
			}, period, period);
		}
	}
	

	
	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		return cmdExe.onCommand(sender, command, label, args);
	}
	
	/**
	 * API
	 * @param player
	 * @param vanished true=vanish, false=reappear
	 */
	public static boolean setVanished(Player player, boolean vanished){
		if (!core.isEnabled()) return false;
		return core.setVanished(player.getName(), vanished);
	}
	
	/**
	 * API
	 * @param playerName
	 * @param vanished
	 */
	public static boolean setVanished(String playerName, boolean vanished){
		if (!core.isEnabled()) return false;
		return  core.setVanished(playerName, vanished);
	}
	
	/**
	 * API
	 * @param playerName Exact player name.
	 * @return
	 */
	public static boolean isVanished(String playerName){
		if (!core.isEnabled()) return false;
		else return core.isVanished(playerName);
	}
	
	/**
	 * API
	 * @param player 
	 * @return
	 */
	public static boolean isVanished(Player player){
		if (!core.isEnabled()) return false;
		else return core.isVanished(player.getName());
	}
	
	/**
	 * API
	 * Get a new Set containing the lower case names of Players to be vanished.<br>
	 * These are not necessarily online.<br>
	 * @deprecated The method signature will most likely change to Collection or List.
	 * @return
	 */
	public static Set<String> getVanishedPlayers(){
		if (!core.isEnabled()) return new HashSet<String>();
		else return core.getVanishedPlayers();
	}
	
	/**
	 * API
	 * @param playerName
	 * @param create
	 * @return A clone of the VanishConfig.
	 */
	public static VanishConfig getVanishConfig(String playerName, boolean create){
		VanishConfig cfg = core.getVanishConfig(playerName, create);
		if (cfg == null) return null;
		else return cfg.clone();
	}
	
	/**
	 * Set the VanishConfig for the player, silently (no notifications).<br>
	 * This actually will create a new config and apply changes from the given one.<br>
	 * If update is true, this will bypass hooks and events.
	 * @param playerName
	 * @param cfg
	 * @param update
	 */
	public static void setVanishConfig(String playerName, VanishConfig cfg, boolean update){
		core.setVanishedConfig(playerName, cfg, update, false);
	}
	
	/**
	 * Set the VanishConfig for the player, with optional notifications, if the player is online.<br>
	 * This actually will create a new config and apply changes from the given one.<br>
	 * If update is true, this will bypass hooks and events.
	 * @param playerName
	 * @param cfg
	 * @param update
	 * @param message
	 */
	public static void setVanishConfig(String playerName, VanishConfig cfg, boolean update, boolean message){
		core.setVanishedConfig(playerName, cfg, update, message);
	}
	
	/**
	 * Force an update of who sees who for this player, without notification.<br>
	 * This bypasses hooks and events.
	 * @param player
	 */
	public static void updateVanishState(Player player){
		core.updateVanishState(player, false); // Mind the difference of flag to core.updateVanishState(Player).
	}
	
	/**
	 * Force an update of who sees who for this player, with optional notification messages.<br>
	 * This bypasses hooks and events.
	 * @param player
	 * @param message If to send notifications and state messages.
	 */
	public static void updateVanishState(Player player, boolean message){
		core.updateVanishState(player, message);
	}
	
	/**
	 * API
	 * @param hook
	 * @return If one was already present.
	 */
	public static boolean addHook(Hook hook){
		return core.hookUtil.addHook(hook);
	}
	
	/**
	 * Listeners can not be removed yet.
	 * API
	 * @param hook
	 * @return If one was already present.
	 */
	public static boolean removeHook(Hook hook){
		return core.hookUtil.removeHook(hook);
	}
	
	/**
	 * Listeners can not be removed yet.
	 * API
	 * @param hookName
	 * @return If one was already present.
	 */
	public static boolean removeHook(String hookName){
		return core.hookUtil.removeHook(hookName);
	}
	
	/**
	 * Listeners can not be removed yet.
	 * API
	 */
	public static void removeAllHooks(){
		core.hookUtil.removeAllHooks();
	}
	
	/**
	 * Convenience method used internally.
	 * @return
	 */
	public static SimplyVanish getPluginInstance(){
		return core.getPlugin();
	}
	
}
