package asofold.simplyvanish;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import asofold.simplyvanish.config.Settings;

/**
 * Example plugin for the vanish API as of CB 1914 !
 * Vanish + God mode + No Target + No pickup.
 * @author mc_dev
 *
 */
public class SimplyVanish extends JavaPlugin {
	
	static final SimplyVanishCore core = new SimplyVanishCore();

	public static final String msgLabel = ChatColor.GOLD+"[SimplyVanish] ";
	
	public static final String[] baseLabels = new String[]{
		"vanish", "reappear", "tvanish", "simplyvanish","vanished",
	};
	
	Configuration defaults;
	
	/**
	 * Map aliases to recognized labels.
	 */
	Map<String, String> commandAliases = new HashMap<String, String>();
	
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
		// load settings
		loadSettings(); // will also load vanished players
		// just in case quadratic time checking:
		for ( Player player : getServer().getOnlinePlayers()){
			core.updateVanishState(player);
		}
		// register events:
		getServer().getPluginManager().registerEvents(core, this);
		// finished enabling.
		core.setEnabled(true);
		System.out.println("[SimplyVanish] Enabled");
	}

	/**
	 * Force reloading the config.
	 */
	public void loadSettings() {
		reloadConfig();
		Configuration config = getConfig();
		Utils.forceDefaults(defaults, config);
		Settings settings = new Settings();
		settings.applyConfig(config);
		core.setSettings(settings);
		registerCommandAliases(config);
		saveConfig(); // TODO: maybe check for changes, somehow ?
		if (settings.saveVanished) core.loadVanished();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		label = getMappedCommandLabel(label);
		int len = args.length;
		boolean hasFlags = false;
		for ( int i=args.length-1; i>=0; i--){
			if (args[i].startsWith("+") || args[i].startsWith("-")){
				len --;
				hasFlags = true;
			} 
			else break;
		}
		
		if (label.equals("nosee") && len==0){
			// TODO: EXPERIMENTAL ADDITION
			// toggle for oneself.
			if ( !Utils.checkPlayer(sender)) return true;
			if ( !Utils.checkPerm(sender, "simplyvanish.see-all")) return true;
			core.onToggleNosee((Player) sender);
			return true;
		}
		else if ( label.equals("vanish") && len==0 ){
			if ( !Utils.checkPlayer(sender)) return true;
			if ( !Utils.checkPerm(sender, "simplyvanish.vanish.self")) return true;
			// Make sure the player is vanished...
			if (hasFlags){
				if (Utils.hasPermission(sender, "simplyvanish.flags.set.self")) core.setFlags(((Player) sender).getName(), args, 0);
				else sender.sendMessage(SimplyVanish.msgLabel+ChatColor.RED+"You do not have permission to set flags.");
			}
			core.onVanish((Player) sender);
			return true;
		} 
		else if ( label.equals("vanish") && len==1 ){
			if ( !Utils.checkPerm(sender, "simplyvanish.vanish.other")) return true;
			// Make sure the other player is vanished...
			String name = args[0].trim();
			if (hasFlags){
				if (Utils.hasPermission(sender, "simplyvanish.flags.set.self")) core.setFlags(name, args, 1);
				else sender.sendMessage(SimplyVanish.msgLabel+ChatColor.RED+"You do not have permission to set flags for others.");
			}
			setVanished(name, true);
			Utils.send(sender, msgLabel + "Vanish player: "+name);
			return true;
		} 
		else if (label.equals("reappear") && len==0 ){
			if ( !Utils.checkPlayer(sender)) return true;
			if ( !Utils.checkPerm(sender, "simplyvanish.vanish.self")) return true;
			// Let the player be seen...
			if (hasFlags){
				if (Utils.hasPermission(sender, "simplyvanish.flags.set.self")) core.setFlags(((Player) sender).getName(), args, 0);
				else sender.sendMessage(SimplyVanish.msgLabel+ChatColor.RED+"You do not have permission to set flags.");
			}
			core.onReappear((Player) sender);
			return true;
		} 
		else if ( label.equals("reappear") && len==1 ){
			if ( !Utils.checkPerm(sender, "simplyvanish.vanish.other")) return true;
			// Make sure the other player is shown...
			String name = args[0].trim();
			if (hasFlags){
				if (Utils.hasPermission(sender, "simplyvanish.flags.set.other")) core.setFlags(name, args, 1);
				else sender.sendMessage(SimplyVanish.msgLabel+ChatColor.RED+"You do not have permission to set flags.");
			}
			setVanished(name, false);
			Utils.send(sender, msgLabel + "Show player: "+name);
			return true;
		} 
		else if ( label.equals("tvanish") && len==0 ){
			if ( !Utils.checkPlayer(sender)) return true;
			Player player = (Player) sender;
			if ( !Utils.checkPerm(sender, "simplyvanish.vanish.self")) return true;
			if (hasFlags){
				if (Utils.hasPermission(sender, "simplyvanish.flags.set.self")) core.setFlags(player.getName(), args, 0);
				else sender.sendMessage(SimplyVanish.msgLabel+ChatColor.RED+"You do not have permission to set flags.");
			}
			setVanished(player, !isVanished(player));
			return true;
		}
		else if (label.equals("vanished")){
			if ( !Utils.checkPerm(sender, "simplyvanish.vanished")) return true;
			Utils.send(sender, core.getVanishedMessage());
			return true;
		} 
		else if ( label.equals("simplyvanish")){
			if (len==1 && args[0].equalsIgnoreCase("reload")){
				if ( !Utils.checkPerm(sender, "simplyvanish.reload")) return true;
				loadSettings();
				Utils.send(sender, msgLabel + "Settings reloaded.");
				return true;
			}
			else if (len==1 && args[0].equalsIgnoreCase("drop")){
				if ( !Utils.checkPerm(sender, "simplyvanish.cmd.drop")) return true;
				if (!Utils.checkPlayer(sender)) return true;
				Utils.dropItemInHand((Player) sender);
				return true;
			}
			else if (hasFlags && len == 0){
				if (!Utils.checkPlayer(sender)) return true;
				if (Utils.hasPermission(sender, "simplyvanish.flags.set.self")) core.setFlags(((Player)sender).getName(), args, 0);
				else sender.sendMessage(SimplyVanish.msgLabel+ChatColor.RED+"You do not have permission to set flags.");
				return true;
			} 
			else if (len == 0){
				if (!Utils.checkPlayer(sender)) return true;
				if (Utils.hasPermission(sender, "simplyvanish.flags.display.self")) core.onShowFlags((Player) sender, null);
				else sender.sendMessage(SimplyVanish.msgLabel+ChatColor.RED+"You do not have permission to display flags.");
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
	 * @deprecated Use setVanished(player, true)
	 * @param player
	 */
	public void vanish(Player player){
		setVanished(player, true);
	}
	
	/**
	 * @deprecated Use setVanished(player, false)
	 * @param player
	 */
	public void reappear(Player player){
		setVanished(player, false);
	}
	
	/**
	 * API
	 * @param player
	 * @param vanished true=vanish, false=reappear
	 */
	public static void setVanished(Player player, boolean vanished){
		if (!core.isEnabled()) return;
		core.setVanished(player.getName(), vanished);
	}
	
	/**
	 * API
	 * @param playerName
	 * @param vanished
	 */
	public static void setVanished(String playerName, boolean vanished){
		if (!core.isEnabled()) return;
		core.setVanished(playerName, vanished);
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
	
	void registerCommandAliases(Configuration config) {
		// OLD ATTEMPT TO REGISTER DYNAMICALLY COMMENTED OUT:
//for ( String cmd : SimplyVanish.baseLabels){
//	List<String> mapped = config.getStringList("commands."+cmd+".aliases");
//	if ( mapped == null || mapped.isEmpty()) continue;
//	for ( String alias: mapped){
//		commandAliases.put(alias.trim().toLowerCase(), cmd);
//	}
//	ArrayList<String> aliases = new ArrayList<String>(mapped.size());
//	aliases.addAll(mapped); // TEST
//	PluginCommand command = plugin.getCommand(cmd);
//	try{
//		command.unregister(cmap);
//		command.setAliases(aliases);
//		command.register(cmap);
//		for (String alias : aliases){
//			PluginCommand aliasCommand = plugin.getCommand(alias);
//			if ( aliasCommand == null ) plugin.getServer().getLogger().warning("[SimplyVanish] Failed to set up command alias for '"+cmd+"': "+alias);
//			else aliasCommand.setExecutor(plugin);
//		}
//	} catch (Throwable t){
//		plugin.getServer().getLogger().severe("[SimplyVanish] Failed to register command aliases for '"+cmd+"': "+t.getMessage());
//		t.printStackTrace();
//	}
//	command.setExecutor(plugin);
//}
		// JUST TO REGISTER ALIASES FOR onCommand:
		for ( String label : SimplyVanish.baseLabels){
			PluginCommand command = getCommand(label);
			List<String> aliases = command.getAliases();
			if ( aliases == null) continue;
			for ( String alias: aliases){
				commandAliases.put(alias.trim().toLowerCase(), label.toLowerCase());
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

}
