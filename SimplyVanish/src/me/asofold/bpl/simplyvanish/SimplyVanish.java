package me.asofold.bpl.simplyvanish;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import me.asofold.bpl.simplyvanish.api.events.GetVanishConfigEvent;
import me.asofold.bpl.simplyvanish.api.hooks.Hook;
import me.asofold.bpl.simplyvanish.command.SimplyVanishCommand;
import me.asofold.bpl.simplyvanish.config.Path;
import me.asofold.bpl.simplyvanish.config.Settings;
import me.asofold.bpl.simplyvanish.config.VanishConfig;
import me.asofold.bpl.simplyvanish.config.compatlayer.CompatConfig;
import me.asofold.bpl.simplyvanish.config.compatlayer.CompatConfigFactory;
import me.asofold.bpl.simplyvanish.listeners.AttackListener;
import me.asofold.bpl.simplyvanish.listeners.ChatListener;
import me.asofold.bpl.simplyvanish.listeners.CoreListener;
import me.asofold.bpl.simplyvanish.listeners.DamageListener;
import me.asofold.bpl.simplyvanish.listeners.DropListener;
import me.asofold.bpl.simplyvanish.listeners.InteractListener;
import me.asofold.bpl.simplyvanish.listeners.PickupListener;
import me.asofold.bpl.simplyvanish.listeners.TargetListener;
import me.asofold.bpl.simplyvanish.stats.Stats;
import me.asofold.bpl.simplyvanish.util.Utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;


/**
 * Example plugin for the vanish API as of CB 1914 !
 * Vanish + God mode + No Target + No pickup.
 * @author mc_dev
 *
 */
public class SimplyVanish extends JavaPlugin {
	
	static final SimplyVanishCore core = new SimplyVanishCore();

	public static final String cmdNoOpArg = "??NOOP??";
	public static final String cmdNoOp = "simplyvanish "+cmdNoOpArg;
	
	public static final String msgLabel = ChatColor.GOLD+"[SimplyVanish]"+ChatColor.GRAY+" ";
	public static final String msgStillInvisible =  SimplyVanish.msgLabel+ChatColor.GRAY+"You are still "+ChatColor.GREEN+"invisible"+ChatColor.GRAY+" to normal players.";
	public static final String msgNowInvisible = SimplyVanish.msgLabel+ChatColor.GRAY+"You are now "+ChatColor.GREEN+"invisible"+ChatColor.GRAY+" to normal players.";
	public static final String msgNotifyPing = SimplyVanish.msgLabel+ChatColor.GRAY+"You are "+ChatColor.GREEN+"invisible"+ChatColor.GRAY+", right now.";
	public static final String msgDefaultFlags = SimplyVanish.msgLabel+ChatColor.GRAY+"Flags are at default values.";

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
		if (core.getSettings().saveVanished) core.doSaveVanished();
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
		Server server = getServer();
		// register events:
		PluginManager pm = server.getPluginManager();
		for ( Listener listener : new Listener[]{
				new AttackListener(core),
				new ChatListener(core),
				new CoreListener(core),
				new DamageListener(core),
				new DropListener(core),
				new InteractListener(core),
				new PickupListener(core),
				new TargetListener(core),	
		}){
			pm.registerEvents(listener, this);
		}
		pm.registerEvents(cmdExe.aliasManager, this);
		// finished enabling.
		core.setEnabled(true);
		core.addStandardHooks();
		// just in case quadratic time checking:
		try{
			updateAllPlayers();
		}
		catch(Throwable t){
			Utils.severe("Failed to update players in onEnable (scheduled for next tick), are you using reload?", t);
			getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				@Override
				public void run() {
					updateAllPlayers();
				}
			});
		}
		System.out.println("[SimplyVanish] Enabled");
	}
	
	/**
	 * Quadratic time.
	 */
	private void updateAllPlayers(){
		for ( Player player : getServer().getOnlinePlayers()){
			core.updateVanishState(player);
			// TODO: this remains a source of trouble when reloading !
		}
	}

	/**
	 * Force reloading the config.
	 */
	public void loadSettings() {
		Server server = getServer();
		BukkitScheduler sched = server.getScheduler();
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
					core.doSaveVanished();
				}
			}, period, period);
		}
		// Load plugins (permissions!):
		PluginManager pm = server.getPluginManager(); 
		for (String plgName : settings.loadPlugins){
			Plugin plg = pm.getPlugin(plgName);
			if (plg != null && !plg.isEnabled()) pm.enablePlugin(plg);
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
	 * Convenience method, get default VanishConfig (copy), for checking flags.<br>
	 * Later on this might be useful, because default flags might be configurable.
	 * @return
	 */
	public static VanishConfig getDefaultVanishConfig(){
		return  new VanishConfig();
	}
	
	/**
	 * The create flag does in this case not force to store the configuration internally. To force that you have to use setVanishConfig.
	 * API
	 * @param playerName
	 * @param create
	 * @return A clone of the VanishConfig.
	 */
	public static VanishConfig getVanishConfig(String playerName, boolean create){
		VanishConfig cfg = core.getVanishConfig(playerName, false);
		if (cfg == null){
			if (create) return getDefaultVanishConfig();
			else return null;
		}
		else return cfg.clone();
	}
	
	/**
	 * Set the VanishConfig for the player, silently (no notifications), issues saving the configs.<br>
	 * This actually will create a new config and apply changes from the given one.<br>
	 * If update is true, this will bypass hooks and events.
	 * @param playerName
	 * @param cfg
	 * @param update
	 */
	public static void setVanishConfig(String playerName, VanishConfig cfg, boolean update){
		core.setVanishConfig(playerName, cfg, update, false);
	}
	
	/**
	 * Set the VanishConfig for the player, with optional notifications, if the player is online, does issue saving the configs.<br>
	 * This actually will create a new config and apply changes from the given one.<br>
	 * If update is true, this will bypass hooks and events.
	 * @param playerName
	 * @param cfg
	 * @param update
	 * @param message
	 */
	public static void setVanishConfig(String playerName, VanishConfig cfg, boolean update, boolean message){
		core.setVanishConfig(playerName, cfg, update, message);
	}
	
	/**
	 * Force an update of who sees who for this player, without notification, as if SimplyVanish would acall it internally.<br>
	 * @param player
	 * @return false, if the action was prevented by a hook, true otherwise.
	 */
	public static boolean updateVanishState(Player player){
		return core.updateVanishState(player, false); // Mind the difference of flag to core.updateVanishState(Player).
	}
	
	/**
	 * Force an update of who sees who for this player, without notification.<br>
	 * @param player
	 * @param hookId To identify who calls this, 0 = as if SimplyVanish called it.
	 * @return false, if the action was prevented by a hook, true otherwise.
	 */
	public static boolean updateVanishState(Player player, int hookId){
		return core.updateVanishState(player, false, hookId);
	}
	
	/**
	 * Force an update of who sees who for this player, with optional notification messages, as if SimplyVanish would call it internally.<br>
	 * @param player
	 * @param message If to send notifications and state messages.
	 * @return false, if the action was prevented by a hook, true otherwise.
	 */
	public static boolean updateVanishState(Player player, boolean message){
		return core.updateVanishState(player, message);
	}
	
	/**
	 * Force an update of who sees who for this player, with optional notification messages.<br>
	 * @param player
	 * @param message
	 * @param hookId To identify who calls this, 0 = as if SimplyVanish called it.
	 * @return false, if the action was prevented by a hook, true otherwise.
	 */
	public static boolean updateVanishState(Player player, boolean message, int hookId){
		return core.updateVanishState(player, message, hookId);
	}
	
	/**
	 * Get a new hook id to be passed for certain calls, to allow knwing if your own code called updateVanishState.
	 * API
	 * @return
	 */
	public static int getNewHookId(){
		return core.getNewHookId();
	}
	
	/**
	 * API
	 * @param hook
	 * @return If one was already present.
	 */
	public static boolean addHook(Hook hook){
		return core.getHookUtil().addHook(hook);
	}
	
	/**
	 * Get a hook if registered, the name must exactly match.
	 * @param name
	 * @return Hook or null, if not registered.
	 */
	public static Hook getRegisteredHook(String name){
		return core.getHookUtil().getHook(name);
	}
	
	/**
	 * Listeners can not be removed yet.
	 * API
	 * @param hook
	 * @return If one was already present.
	 */
	public static boolean removeHook(Hook hook){
		return core.getHookUtil().removeHook(hook);
	}
	
	/**
	 * Listeners can not be removed yet.
	 * API
	 * @param hookName
	 * @return If one was already present.
	 */
	public static boolean removeHook(String hookName){
		return core.getHookUtil().removeHook(hookName);
	}
	
	/**
	 * Listeners can not be removed yet.
	 * API
	 */
	public static void removeAllHooks(){
		core.getHookUtil().removeAllHooks();
	}
	
	/**
	 * Respects allow-ops, superperms and fake-permissions configuration entries. 
	 * @param player
	 * @param 
	 * @return
	 */
	public static final boolean hasPermission(final CommandSender sender, final String perm) {
		return core.hasPermission(sender, perm);
	}
	
	/**
	 * Convenience method used internally.
	 * @return
	 */
	public static SimplyVanish getPluginInstance(){
		return core.getPlugin();
	}

	/**
	 * Get a thread safe copy of the VanishConfig for a player.<br>
	 * This method will synchronize into the main server thread with an event, 
	 * this take up to 50 milliseconds for processing, but it will return a copy
	 * of the VanishCOnfig instance for the player, exactly at a cxertain moment of time.<br>
	 * 
	 * NOTE: This probably mostly obsolete and will likely be removed for the use of synchronized maps, but it is interesting to see (once) to use events to get some object in a thread safe way.  
	 * @param playerName
	 * @param b
	 * @return
	 */
	public static VanishConfig getVanishConfigThreadSafe(String playerName, boolean create) {
		// bit hacky:
		GetVanishConfigEvent event = new GetVanishConfigEvent(playerName, create);
		Bukkit.getPluginManager().callEvent(event);
		return event.getVanishConfig();
	}
	
}
