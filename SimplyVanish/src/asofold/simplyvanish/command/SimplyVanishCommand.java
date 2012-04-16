package asofold.simplyvanish.command;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import asofold.simplyvanish.SimplyVanish;
import asofold.simplyvanish.SimplyVanishCore;
import asofold.simplyvanish.util.Utils;

public class SimplyVanishCommand{
	/**
	 * Map aliases to recognized labels.
	 */
	public Map<String, String> commandAliases = new HashMap<String, String>();
	private SimplyVanishCore core;
	
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

	public boolean onCommand(CommandSender sender, Command command, String label,
			String[] args) {
		SimplyVanish plugin = core.getPlugin();
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
			if (!SimplyVanish.setVanished((Player) sender, true)) Utils.send(sender, SimplyVanish.msgLabel+ChatColor.RED+"Action was prevented by hooks.");
			return true;
		} 
		else if ( label.equals("vanish") && len==1 ){
			if ( !Utils.checkPerm(sender, "simplyvanish.vanish.other")) return true;
			// Make sure the other player is vanished...
			String name = args[0].trim();
			if (hasFlags) core.setFlags(name, args, len, sender, false, true, false);
			if (SimplyVanish.setVanished(name, true)) Utils.send(sender, SimplyVanish.msgLabel + "Vanish player: "+name);
			else Utils.send(sender, SimplyVanish.msgLabel+ChatColor.RED+"Action was prevented by hooks.");
			return true;
		} 
		else if (label.equals("reappear") && len==0 ){
			if ( !Utils.checkPlayer(sender)) return true;
			if ( !Utils.checkPerm(sender, "simplyvanish.vanish.self") && !Utils.checkPerm(sender, "simplyvanish.reappear.self")) return true;
			// Let the player be seen...
			if (hasFlags) core.setFlags(((Player) sender).getName(), args, len, sender, false, false, false);
			if (!SimplyVanish.setVanished((Player) sender, false)) Utils.send(sender, SimplyVanish.msgLabel+ChatColor.RED+"Action was prevented by hooks.");
			return true;
		} 
		else if ( label.equals("reappear") && len==1 ){
			if ( !Utils.checkPerm(sender, "simplyvanish.vanish.other") && !Utils.checkPerm(sender, "simplyvanish.reappear.other")) return true;
			// Make sure the other player is shown...
			String name = args[0].trim();
			if (hasFlags) core.setFlags(name, args, len, sender, false, true, false);
			if (SimplyVanish.setVanished(name, false)) Utils.send(sender, SimplyVanish.msgLabel + "Show player: "+name);
			else Utils.send(sender, SimplyVanish.msgLabel+ChatColor.RED+"Action was prevented by hooks.");
			return true;
		} 
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
			
			if (label.equals("simplyvanish")){
				if (!hasFlags && len==1 && args[0].equalsIgnoreCase("reload")){
					if ( !Utils.checkPerm(sender, "simplyvanish.reload")) return true;
					plugin.loadSettings();
					Utils.send(sender, SimplyVanish.msgLabel + ChatColor.YELLOW+"Settings reloaded.");
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
					Utils.send(sender, SimplyVanish.stats.getStatsStr(true));
					return true;
				} 
				else if (len==2 && args[0].equalsIgnoreCase("stats") && args[1].equalsIgnoreCase("reset")){
					if ( !Utils.checkPerm(sender, "simplyvanish.stats.reset")) return true;
					SimplyVanish.stats.clear();
					Utils.send(sender, SimplyVanish.msgLabel+"Stats reset.");
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
		Utils.send(sender, SimplyVanish.msgLabel + ChatColor.DARK_RED+"Unrecognized command or number of arguments.");
		return false;
	}

}
