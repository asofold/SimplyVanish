package me.asofold.bpl.simplyvanish.command;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

/**
 * Stand-alone class for handling fake commands, to allow for ingame registration of new aliases and handling.<br>
 * "Lightweight commands" : checking PlayerCommandPreProcessEvent, if no command is available, check map and delegate.<br>
 * Uses trim/lowercase internally, a lot.<br>
 * Registers at highest priority currently.
 * @author mc_dev
 *
 */
public class LightCommands implements Listener {
	 
	public static class LightCommand extends Command{
		private CommandExecutor exe = null;
		public LightCommand(String name, String description,
				String usageMessage, List<String> aliases) {
			super(name, description, usageMessage, aliases);
		}
		@Override
		public boolean execute(CommandSender sender, String label, String[] args) {
			if (exe == null) return false;
			else return exe.onCommand(sender, this, label, args);
		}
		public void setExecutor(CommandExecutor commandExecutor){
			this.exe = commandExecutor;
		}
		public CommandExecutor getExecutor(){
			return exe;
		}
	}
	
	/**
	 * If set all whitespace only parts will get removed before sending the command.
	 */
	public boolean aggressiveTrim = true;
	
	/**
	 * Set command/message get set to this if handled by LightCommands.
	 */
	public String cmdNoOp = null;
	
	Map<String, LightCommand> commandMap = new HashMap<String, LightCommand>();
	
	
	public boolean registerCommand(String label, Collection<String> aliases, CommandExecutor commandExecutor){
		return registerCommand(label, null, null, aliases, commandExecutor);
	}
 	
	public boolean registerCommand(String label, String description, String usage, Collection<String> aliases, CommandExecutor commandExecutor){
		label = label.trim().toLowerCase();
		List<String> aliasList = new LinkedList<String>();
		for ( String alias : aliases){
			aliasList.add(alias.trim().toLowerCase());
		}
		if (commandMap.containsKey(label)) return false;
		LightCommand cmd = new LightCommand(label, description, usage, aliasList);
		cmd.setExecutor(commandExecutor);
		cmd.setUsage(usage);
		cmd.setDescription(description);
		this.commandMap.put(label, cmd);
		for (String alias : aliasList){
			if (!commandMap.containsKey(alias)) commandMap.put(alias, cmd);
		}
		return true;
	}
	
	/**
	 * Remove the binding for the alias or command, but not the other aliases / command.
	 * @param label
	 * @return
	 */
	public boolean removeAlias(String label){
		LightCommand cmd = commandMap.remove(label.trim().toLowerCase());
		if (cmd==null) return false;
		else return true;
	}
	
	/**
	 * Remove command and aliases for the given label.
	 * Does not work if label is an alias (!).
	 * @param label
	 * @return
	 */
	public boolean removeCommand(String label){
		label = label.trim().toLowerCase();
		LightCommand cmd = commandMap.get(label);
		if (cmd == null) return false;
		if (!label.equals(cmd.getLabel())) return false;
		commandMap.remove(label);
		for (String alias : cmd.getAliases()){
			LightCommand candidate = commandMap.get(alias.trim().toLowerCase());
			if (candidate == null) continue;
			if (!label.equals(candidate.getLabel())) continue;
			commandMap.remove(alias);
		}
		return true;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onConsoleTryCommand(ServerCommandEvent event){
		String msg = event.getCommand();
		CommandSender sender = event.getSender();
		if (processCommand(sender, msg)) event.setCommand(cmdNoOp); // TODO
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerTryCommand(PlayerCommandPreprocessEvent event){
		if ( event.isCancelled()) return;
		CommandSender sender = event.getPlayer();
		String msg = event.getMessage();
		if (msg == null) return;
		msg = msg.trim();
		if (msg.startsWith("/")){
			if (msg.length()>1) msg = msg.substring(1);
			else msg = "";
		}
		if (processCommand(sender, msg)){
			event.setCancelled(true);
			event.setMessage(cmdNoOp);
		}
	}
	
	/**
	 * 
	 * @param sender
	 * @param msg
	 * @return
	 */
	public boolean processCommand(CommandSender sender, String msg){
		if (msg == null) return false;
		if (sender == null) return false;
		String[] split = msg.split(" ");
		List<String> valid = new LinkedList<String>();
		String label = null;
		LightCommand command = null;
		for (String part : split){
			if (aggressiveTrim){
				part = part.trim();
				if (part.isEmpty()) continue;
			}
			if (label == null){
				label = part;
				// ensure quick return. 
				command = commandMap.get(label.trim().toLowerCase());
				if (command == null) return false;
			}
			else valid.add(part);
		}
		if (label == null) return false;
		String[] args = new String[valid.size()];
		if (args.length>0) valid.toArray(args);
		boolean res = command.execute(sender, label, args);
		if (res == false){
			String usage = command.getUsage();
			if (usage != null){
				if (!usage.isEmpty()){
					if (sender instanceof Player) sender.sendMessage(usage);
					else sender.sendMessage(ChatColor.stripColor(usage));
				}
			}
		}
		return true;
	}

	public void clear() {
		commandMap.clear();
	}
}
