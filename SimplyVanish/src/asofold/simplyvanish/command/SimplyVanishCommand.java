package asofold.simplyvanish.command;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import asofold.simplyvanish.SimplyVanish;
import asofold.simplyvanish.SimplyVanishCore;
import asofold.simplyvanish.config.Flag;
import asofold.simplyvanish.config.Path;
import asofold.simplyvanish.config.VanishConfig;
import asofold.simplyvanish.config.compatlayer.CompatConfig;
import asofold.simplyvanish.util.Utils;

public class SimplyVanishCommand{
	
	private SimplyVanishCore core;
	
	/**
	 *  Dynamic "fake" commands.
	 */
	public LightCommands aliasManager = new LightCommands();
	
	/**
	 * Map aliases to recognized labels.
	 */
	public Map<String, String> commandAliases = new HashMap<String, String>();
	
	
	public SimplyVanishCommand(SimplyVanishCore core) {
		this.core = core;
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
	
	public void registerCommandAliases(CompatConfig config, Path path) {
		SimplyVanish plugin = core.getPlugin();
		aliasManager.cmdNoOp =  SimplyVanish.cmdNoOp; //  hack :)
		// Register aliases from configuration ("fake"). 
		aliasManager.clear();
		for ( String cmd : SimplyVanish.baseLabels){
			// TODO: only register the needed aliases.
			cmd = cmd.trim().toLowerCase();
			List<String> mapped = config.getStringList("commands"+path.sep+cmd+path.sep+"aliases", null);
			if ( mapped == null || mapped.isEmpty()) continue;
			List<String> needed = new LinkedList<String>(); // those that need to be registered.
			for (String alias : mapped){
				Command ref = plugin.getCommand(alias);
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
			if (!aliasManager.registerCommand(cmd, needed, plugin)){
				// TODO: log maybe
			}
			Command ref = plugin.getCommand(cmd) ;
			if ( ref != null){
				aliasManager.removeAlias(cmd); // the command is registered already.
				for (String alias : ref.getAliases() ) {
					aliasManager.removeAlias(alias); // TODO ?
				}
			}
			for ( String alias: needed){
				alias = alias.trim().toLowerCase();
				commandAliases.put(alias, cmd);
			}
		
		}
		
		// Register aliases for commands from plugin.yml:
		for ( String cmd : SimplyVanish.baseLabels){
			cmd = cmd.trim().toLowerCase();
			PluginCommand command = plugin.getCommand(cmd);
			if (command == null) continue;
			List<String> aliases = command.getAliases();
			if ( aliases == null) continue;
			for ( String alias: aliases){
				commandAliases.put(alias.trim().toLowerCase(), cmd);
			}
		}
	}

	public boolean onCommand(CommandSender sender, Command command, String label,
			String[] args) {
//		SimplyVanish plugin = core.getPlugin();
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
		if ( label.equals("vanish")) return vanishCommand(sender, args, len, hasFlags);
		else if (label .equals("reappear")) return reappearCommand(sender, args, len, hasFlags);
		else if ( label.equals("tvanish") && len==0 ){
			if ( !Utils.checkPlayer(sender)) return true;
			Player player = (Player) sender;
			if ( !Utils.checkPerm(sender, "simplyvanish.vanish.self")) return true;
			if (hasFlags) core.setFlags(player.getName(), args, len, sender, false, false, false);
			if (!SimplyVanish.setVanished(player, !SimplyVanish.isVanished(player))) Utils.send(sender, SimplyVanish.msgLabel+ChatColor.RED+"Action was prevented by hooks.");
			return true;
		}
		else if (label.equals("vanished")){
			if ( !Utils.checkPerm(sender, "simplyvanish.vanished")) return true;
			Utils.send(sender, core.getVanishedMessage());
			return true;
		} 
		else if ( label.equals("simplyvanish") || label.equals("vanflag")){
			if (label.equals("simplyvanish") && !hasFlags) return rootCommand(sender, args);
			else return flagCommand(sender, args, len, hasFlags);
		}
		return unrecognized(sender);
	}

	private boolean flagCommand(CommandSender sender, String[] args, int len,
			boolean hasFlags) {
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
		return unrecognized(sender);
	}

	private boolean reappearCommand(CommandSender sender, String[] args, int len,
			boolean hasFlags) {
		if ( len==0 ){
			if ( !Utils.checkPlayer(sender)) return true;
			if ( !Utils.hasPermission(sender, "simplyvanish.vanish.self") && !Utils.hasPermission(sender, "simplyvanish.reappear.self")) return Utils.noPerm(sender);
			// Let the player be seen...
			if (hasFlags) core.setFlags(((Player) sender).getName(), args, len, sender, false, false, false);
			if (!SimplyVanish.setVanished((Player) sender, false)) Utils.send(sender, SimplyVanish.msgLabel+ChatColor.RED+"Action was prevented by hooks.");
			return true;
		} 
		else if ( len==1 ){
			if ( !Utils.hasPermission(sender, "simplyvanish.vanish.other") && !Utils.hasPermission(sender, "simplyvanish.reappear.other")) return Utils.noPerm(sender);
			// Make sure the other player is shown...
			String name = args[0].trim();
			if (hasFlags) core.setFlags(name, args, len, sender, false, true, false);
			if (SimplyVanish.setVanished(name, false)) Utils.send(sender, SimplyVanish.msgLabel + "Show player: "+name);
			else Utils.send(sender, SimplyVanish.msgLabel+ChatColor.RED+"Action was prevented by hooks.");
			return true;
		} 
		return unrecognized(sender);
	}

	private boolean vanishCommand(CommandSender sender, String[] args, int len,
			boolean hasFlags) {
		if ( len==0 ){
			if ( !Utils.checkPlayer(sender)) return true;
			if ( !Utils.checkPerm(sender, "simplyvanish.vanish.self")) return true;
			// Make sure the player is vanished...
			if (hasFlags) core.setFlags(((Player) sender).getName(), args, len, sender, false, false, false);
			if (!SimplyVanish.setVanished((Player) sender, true)) Utils.send(sender, SimplyVanish.msgLabel+ChatColor.RED+"Action was prevented by hooks.");
			return true;
		} 
		else if ( len==1 ){
			if ( !Utils.checkPerm(sender, "simplyvanish.vanish.other")) return true;
			// Make sure the other player is vanished...
			String name = args[0].trim();
			if (hasFlags) core.setFlags(name, args, len, sender, false, true, false);
			if (SimplyVanish.setVanished(name, true)) Utils.send(sender, SimplyVanish.msgLabel + "Vanish player: "+name);
			else Utils.send(sender, SimplyVanish.msgLabel+ChatColor.RED+"Action was prevented by hooks.");
			return true;
		} 
		return unrecognized(sender);
	}

	/**
	 * No flags.
	 * @param sender
	 * @param command
	 * @param label Expected to be mapped already.
	 * @param args
	 * @return
	 */
	private boolean rootCommand(CommandSender sender, String[] args) {
		SimplyVanish plugin = core.getPlugin();
		int len = args.length;
		if (len==1 && args[0].equalsIgnoreCase("reload")){
			if ( !Utils.checkPerm(sender, "simplyvanish.reload")) return true;
			plugin.loadSettings();
			Utils.send(sender, SimplyVanish.msgLabel + ChatColor.YELLOW+"Settings reloaded.");
			return true;
		}
		else if (len==1 && args[0].equalsIgnoreCase("drop")){
			if ( !Utils.checkPerm(sender, "simplyvanish.cmd.drop")) return true;
			if (!Utils.checkPlayer(sender)) return true;
			Utils.dropItemInHand((Player) sender);
			return true;
		}
		else if (len==1 && args[0].equals(SimplyVanish.cmdNoOpArg)) return true;
		else if (len==1 && args[0].equalsIgnoreCase("stats")){
			if ( !Utils.checkPerm(sender, "simplyvanish.stats.display")) return true;
			Utils.send(sender, SimplyVanish.stats.getStatsStr(true));
			return true;
		} 
		else if (len==2 && args[0].equalsIgnoreCase("stats") && args[1].equalsIgnoreCase("reset")){
			if ( !Utils.checkPerm(sender, "simplyvanish.stats.reset")) return true;
			SimplyVanish.stats.clear();
			Utils.send(sender, SimplyVanish.msgLabel+"Stats reset.");
			return true;
		}
		else if (len == 1 && args[0].equalsIgnoreCase("flags")){
			if (!Utils.hasPermission(sender, "simplyvanish.flags.display.self") && !Utils.hasPermission(sender, "simplyvanish.flags.display.other")) return Utils.noPerm(sender);
			VanishConfig cfg = new VanishConfig();
			StringBuilder b = new StringBuilder();
			for (Flag flag : cfg.getAllFlags()){
				b.append(" "+Flag.fs(flag.preset)+flag.name);
			}
			Utils.send(sender, SimplyVanish.msgLabel + ChatColor.GRAY+"All default flags: "+ChatColor.YELLOW+b.toString());
			return true;
		}
		return unrecognized(sender);
		
	}

	/**
	 * Message and return false.
	 * @param sender
	 * @return
	 */
	public static boolean unrecognized(CommandSender sender) {
		Utils.send(sender, SimplyVanish.msgLabel + ChatColor.DARK_RED+"Unrecognized command or number of arguments.");
		return false;
	}

}
