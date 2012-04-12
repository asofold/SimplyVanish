package asofold.simplyvanish;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import asofold.simplyvanish.command.LightCommands;
import asofold.simplyvanish.config.Settings;
import asofold.simplyvanish.config.VanishConfig;
import asofold.simplyvanish.hooks.Hook;
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
	
	private static final String cmdNoOpArg = "??NOOP??";
	private static final String cmdNoOp = "simplyvanish "+cmdNoOpArg;
	
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
	

	
	
	Configuration defaults;
	
	/**
	 * Map aliases to recognized labels.
	 */
	Map<String, String> commandAliases = new HashMap<String, String>();
	
	/**
	 *  Dynamic "fake" commands.
	 */
	LightCommands aliasManager = new LightCommands();
	
	/**
	 * Constructor: set some defualt configuration values.
	 */
	public SimplyVanish(){
		defaults = Settings.getDefaultConfig();
	}
	
	@Override
	public void onDisable() {
		if (core.settings.saveVanished) core.saveVanished();
		core.setEnabled(false);
		// TODO: maybe let all players see each other again?
		System.out.println("[SimplyVanish] Disabled.");
	}

	@Override
	public void onEnable() {
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
		pm.registerEvents(aliasManager, this);
		// finished enabling.
		core.setEnabled(true);
		System.out.println("[SimplyVanish] Enabled");
	}

	/**
	 * Force reloading the config.
	 */
	public void loadSettings() {
		BukkitScheduler sched = getServer().getScheduler();
		sched.cancelTasks(this);
		reloadConfig();
		Configuration config = getConfig();
		boolean changed = Utils.forceDefaults(defaults, config);
		Settings settings = new Settings();
		settings.applyConfig(config);
		core.setSettings(settings);
		registerCommandAliases(config);
		if (changed) saveConfig(); // TODO: maybe check for changes, somehow ?
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
	
	void registerCommandAliases(Configuration config) {
		aliasManager.cmdNoOp =  SimplyVanish.cmdNoOp; //  hack :)
		// Register aliases from configuration ("fake"). 
		aliasManager.clear();
		for ( String cmd : SimplyVanish.baseLabels){
			// TODO: only register the needed aliases.
			cmd = cmd.trim().toLowerCase();
			List<String> mapped = config.getStringList("commands."+cmd+".aliases");
			if ( mapped == null || mapped.isEmpty()) continue;
			List<String> needed = new LinkedList<String>(); // those that need to be registered.
			for (String alias : mapped){
				Command ref = getCommand(alias);
				if (ref==null){
					needed.add(alias);
				}
				else if (ref.getLabel().equalsIgnoreCase(cmd)){
					// already mapped to that command.
					continue;
				}
				else needed.add(alias);
			}
			if (needed.isEmpty()) continue;
			// register with wrong(!) label:
			if (!aliasManager.registerCommand(cmd, needed, this)){
				// TODO: log maybe
			}
			if (getCommand(cmd) != null) aliasManager.removeAlias(cmd); // the command is registered already.
			for ( String alias: needed){
				alias = alias.trim().toLowerCase();
				commandAliases.put(alias, cmd);
			}
		
		}
		
		// Register aliases for commands from plugin.yml:
		for ( String cmd : SimplyVanish.baseLabels){
			cmd = cmd.trim().toLowerCase();
			PluginCommand command = getCommand(cmd);
			if (command == null) continue;
			List<String> aliases = command.getAliases();
			if ( aliases == null) continue;
			for ( String alias: aliases){
				commandAliases.put(alias.trim().toLowerCase(), cmd);
			}
		}
	}
	
	/**
	 * Get standardized lower-case label, possibly mapped from an alias.
	 * @param label
	 * @return
	 */
	String getMappedCommandLabel(String label){
		label = label.toLowerCase();
		String mapped = commandAliases.get(label);
		if (mapped == null) return label;
		else return mapped;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		label = getMappedCommandLabel(label);
		int len = args.length;
		boolean hasFlags = false;
		for ( int i=args.length-1; i>=0; i--){
			if (args[i].startsWith("+") || args[i].startsWith("-") || args [i].startsWith("*")){
				len --;
				hasFlags = true;
			} 
			else break;
		}
		
		if ( label.equals("vanish") && len==0 ){
			if ( !Utils.checkPlayer(sender)) return true;
			if ( !Utils.checkPerm(sender, "simplyvanish.vanish.self")) return true;
			// Make sure the player is vanished...
			if (hasFlags) core.setFlags(((Player) sender).getName(), args, len, sender, false, false, false);
			if (!setVanished((Player) sender, true)) Utils.send(sender, SimplyVanish.msgLabel+ChatColor.RED+"Action was prevented by hooks.");
			return true;
		} 
		else if ( label.equals("vanish") && len==1 ){
			if ( !Utils.checkPerm(sender, "simplyvanish.vanish.other")) return true;
			// Make sure the other player is vanished...
			String name = args[0].trim();
			if (hasFlags) core.setFlags(name, args, len, sender, false, true, false);
			if (setVanished(name, true)) Utils.send(sender, msgLabel + "Vanish player: "+name);
			else Utils.send(sender, SimplyVanish.msgLabel+ChatColor.RED+"Action was prevented by hooks.");
			return true;
		} 
		else if (label.equals("reappear") && len==0 ){
			if ( !Utils.checkPlayer(sender)) return true;
			if ( !Utils.checkPerm(sender, "simplyvanish.vanish.self") && !Utils.checkPerm(sender, "simplyvanish.reappear.self")) return true;
			// Let the player be seen...
			if (hasFlags) core.setFlags(((Player) sender).getName(), args, len, sender, false, false, false);
			if (!setVanished((Player) sender, false)) Utils.send(sender, SimplyVanish.msgLabel+ChatColor.RED+"Action was prevented by hooks.");
			return true;
		} 
		else if ( label.equals("reappear") && len==1 ){
			if ( !Utils.checkPerm(sender, "simplyvanish.vanish.other") && !Utils.checkPerm(sender, "simplyvanish.reappear.other")) return true;
			// Make sure the other player is shown...
			String name = args[0].trim();
			if (hasFlags) core.setFlags(name, args, len, sender, false, true, false);
			if (setVanished(name, false)) Utils.send(sender, msgLabel + "Show player: "+name);
			else Utils.send(sender, SimplyVanish.msgLabel+ChatColor.RED+"Action was prevented by hooks.");
			return true;
		} 
		else if ( label.equals("tvanish") && len==0 ){
			if ( !Utils.checkPlayer(sender)) return true;
			Player player = (Player) sender;
			if ( !Utils.checkPerm(sender, "simplyvanish.vanish.self")) return true;
			if (hasFlags) core.setFlags(player.getName(), args, len, sender, false, false, false);
			if (!setVanished(player, !isVanished(player))) Utils.send(sender, SimplyVanish.msgLabel+ChatColor.RED+"Action was prevented by hooks.");
			return true;
		}
		else if (label.equals("vanished")){
			if ( !Utils.checkPerm(sender, "simplyvanish.vanished")) return true;
			Utils.send(sender, core.getVanishedMessage());
			return true;
		} 
		else if ( label.equals("simplyvanish") || label.equals("vanflag")){
			
			if (label.equals("simplyvanish")){
				if (!hasFlags && len==1 && args[0].equalsIgnoreCase("reload")){
					if ( !Utils.checkPerm(sender, "simplyvanish.reload")) return true;
					loadSettings();
					Utils.send(sender, msgLabel + ChatColor.YELLOW+"Settings reloaded.");
					return true;
				}
				else if (!hasFlags && len==1 && args[0].equalsIgnoreCase("drop")){
					if ( !Utils.checkPerm(sender, "simplyvanish.cmd.drop")) return true;
					if (!Utils.checkPlayer(sender)) return true;
					Utils.dropItemInHand((Player) sender);
					return true;
				}
				else if (len==1 && args[0].equals(SimplyVanish.cmdNoOpArg)) return true;
				else if (len==1 && args[0].equalsIgnoreCase("stats")){
					if ( !Utils.checkPerm(sender, "simplyvanish.stats.display")) return true;
					Utils.send(sender, stats.getStatsStr(true));
					return true;
				} else if (len==2 && args[0].equalsIgnoreCase("stats") && args[1].equalsIgnoreCase("reset")){
					if ( !Utils.checkPerm(sender, "simplyvanish.stats.reset")) return true;
					stats.clear();
					Utils.send(sender, msgLabel+"Stats reset.");
					return true;
				}
			}
			
			if (hasFlags && len == 0){
				if (!Utils.checkPlayer(sender)) return true;
				core.setFlags(((Player)sender).getName(), args, len, sender, false, false, true);
				if (Utils.hasPermission(sender, "simplyvanish.flags.display.self")) core.onShowFlags((Player) sender, null);
				return true;
			} 
			else if (len == 0){
				if (!Utils.checkPlayer(sender)) return true;
				if (Utils.hasPermission(sender, "simplyvanish.flags.display.self")) core.onShowFlags((Player) sender, null);
				else sender.sendMessage(SimplyVanish.msgLabel+ChatColor.RED+"You do not have permission to display flags.");
				return true;
			} 
			else if (hasFlags && len==1){
				core.setFlags(args[0], args, len, sender, false, true, true);
				if (Utils.hasPermission(sender, "simplyvanish.flags.display.other")) core.onShowFlags(sender, args[0]);
				return true;
			}
			else if (len==1){
				if (Utils.hasPermission(sender, "simplyvanish.flags.display.other")) core.onShowFlags(sender, args[0]);
				else sender.sendMessage(SimplyVanish.msgLabel+ChatColor.RED+"You do not have permission to display flags of others.");
				return true;
			}
		}
		Utils.send(sender, msgLabel + ChatColor.DARK_RED+"Unrecognized command or number of arguments.");
		return false;
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
	public static boolean  setVanished(String playerName, boolean vanished){
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
	 * This actually will create a new config and apply changes from the given one.
	 * @param playerName
	 * @param cfg
	 * @param update
	 */
	public static void setVanishConfig(String playerName, VanishConfig cfg, boolean update){
		core.setVanishedConfig(playerName, cfg, update, false);
	}
	
	/**
	 * Set the VanishConfig for the player, with optional notifications, if the player is online.<br>
	 * This actually will create a new config and apply changes from the given one.
	 * @param playerName
	 * @param cfg
	 * @param update
	 * @param message
	 */
	public static void setVanishConfig(String playerName, VanishConfig cfg, boolean update, boolean message){
		core.setVanishedConfig(playerName, cfg, update, message);
	}
	
	/**
	 * Force an update of who sees who for this player, without notification.
	 * @param player
	 */
	public static void updateVanishState(Player player){
		core.updateVanishState(player, false); // Mind the difference of flag to core.updateVanishState(Player).
	}
	
	/**
	 * Force an update of who sees who for this player, with optional notification messages.
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
		return core.addHook(hook);
	}
	
	/**
	 * API
	 * @param hook
	 * @return If one was already present.
	 */
	public static boolean removeHook(Hook hook){
		return core.removeHook(hook);
	}
	
	/**
	 * API
	 * @param hookName
	 * @return If one was already present.
	 */
	public static boolean removeHook(String hookName){
		return core.removeHook(hookName);
	}
	
	/**
	 * API
	 */
	public static void removeAllHooks(){
		core.removeAllHooks();
	}

}
