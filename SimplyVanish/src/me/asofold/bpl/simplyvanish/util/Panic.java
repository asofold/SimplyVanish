package me.asofold.bpl.simplyvanish.util;

import me.asofold.bpl.simplyvanish.SimplyVanish;
import me.asofold.bpl.simplyvanish.config.Settings;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;


public class Panic {

	/**
	 * Do online checking and also check settings if to continue.
	 * @param player1 The player to be shown or hidden.
	 * @param player2
	 * @param tag
	 * @return true if to continue false if to abort.
	 */
	public static boolean checkInvolved(Player player1, Player player2, String tag, boolean noAbort){
		boolean inconsistent = false;
		if (!Utils.checkOnline(player1, tag)) inconsistent = true;
		if (!Utils.checkOnline(player2, tag)) inconsistent = true;
		if (noAbort){
			return true;
		} else if (inconsistent){
			try{
				player1.sendMessage(SimplyVanish.msgLabel+ChatColor.RED+"Warning: Could not use "+tag+" to player: "+player2.getName());
			} catch (Throwable t){	
			}
		}
		return !inconsistent; // "true = continue = not inconsistent"
	}

	public static void onPanic(Settings settings, Player[] involved){
		Server server = Bukkit.getServer();
		if ( settings.panicKickAll){
			for ( Player player :  server.getOnlinePlayers()){
				try{
					player.kickPlayer(settings.panicKickMessage);
				} catch (Throwable t){
					// ignore
				}
			}
		} 
		else if (settings.panicKickInvolved){
			for ( Player player : involved){
				try{
					player.kickPlayer(settings.panicKickMessage);
				} catch (Throwable t){
					// ignore
				}
			}
		}
		try{
			Utils.sendToTargets(settings.panicMessage, settings.panicMessageTargets);
		} catch ( Throwable t){
			Utils.warn("[Panic] Failed to send to: "+settings.panicMessageTargets+" ("+t.getMessage()+")");
			t.printStackTrace();
		}
		if (settings.panicRunCommand && !"".equals(settings.panicCommand)){
			try{
				server.dispatchCommand(server.getConsoleSender(), settings.panicCommand);
			} catch (Throwable t){
				Utils.warn("[Panic] Failed to dispathc command: "+settings.panicCommand+" ("+t.getMessage()+")");
				t.printStackTrace();
			}
		}
	}

}
