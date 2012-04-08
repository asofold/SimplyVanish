package asofold.simplyvanish;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;

import asofold.simplyvanish.config.Settings;

/**
 * Core methods for vanish/reappear.
 * @author mc_dev
 *
 */
public class SimplyVanishCore implements Listener{
	
	/**
	 * Vanished players.
	 */
	private final Map<String, VanishConfig> vanishConfigs = new HashMap<String, VanishConfig>();
	
	/**
	 * Flag for if the plugin is enabled.
	 */
	boolean enabled = false;
	
	Settings settings = new Settings();
	
	/*
	 * File to save vanished players to.
	 */
	private File vanishedFile = null;


	@EventHandler(priority=EventPriority.HIGHEST)
	void onPlayerJoin( PlayerJoinEvent event){
		Player player = event.getPlayer();
		String playerName = player.getName();
		VanishConfig cfg = getVanishConfig(playerName);
		boolean auto = false;
		if (cfg.auto == null){
			if (settings.autoVanishUse) auto = true;
		} 
		else if (cfg.auto) auto = true;
		if (auto){
			if (Utils.hasPermission(player, settings.autoVanishPerm)) addVanishedName(playerName);
		}
		updateVanishState(event.getPlayer());
		if ( settings.suppressJoinMessage && isVanished(playerName)){
			event.setJoinMessage(null);
		}
	}
	
	/**
	 * Save vanished names to file, does NOT update states (!).
	 */
	public void saveVanished(){
		File file = getVanishedFile();
		if ( file==null){
			Utils.warn("Can not save vanished players: File is not set.");
			return;
		}
		if (!createFile(file, "vanished players")) return;
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter( new FileWriter(file));
			writer.write("\n"); // to write something at least.
			for (String n : vanishConfigs.keySet()){
				VanishConfig cfg = getVanishConfig(n);
				if (cfg.needsSave()){
					writer.write(n);
					writer.write(cfg.toLine());
					writer.write("\n");
				}
			}
		} 
		catch (IOException e) {
			Utils.warn("Can not save vanished players: "+e.getMessage());
		}
		finally{
			if ( writer != null)
				try {
					writer.close();
				} catch (IOException e) {
				}
		}
	}
	
	/**
	 * Load vanished names from file.<br>
	 *  This does not update vanished states!
	 */
	public void loadVanished(){
		File file = getVanishedFile();
		if ( file == null){
			Utils.warn("Can not load vanished players: File is not set.");
			return;
		}
		if (!createFile(file, "vanished players")) return;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader( new FileReader(file));
			String line = "";
			while ( line != null){
				String n = line.trim().toLowerCase();
				if (!n.isEmpty()){
					if (n.startsWith("nosee:") && n.length()>6){
						// kept for compatibility:
						n = n.substring(7).trim();
						if (n.isEmpty()) continue;
						getVanishConfig(n).see = false;
						
					}
					else{
						String[] split = n.split(" ");
						n = split[0].trim().toLowerCase();
						if (n.isEmpty()) continue;
						VanishConfig cfg = getVanishConfig(n);
						cfg.readFromArray(split, 1, true);
					}
				}
				line = reader.readLine();
			}
		} 
		catch (IOException e) {
			Utils.warn("Can not load vanished players: "+e.getMessage());
		}
		finally{
			if ( reader != null)
				try {
					reader.close();
				} catch (IOException e) {
				}
		}
	}
	
	/**
	 * Create if not exists.
	 * @param file
	 * @param tag
	 * @return if exists now.
	 */
	public boolean createFile(File file, String tag){
		if ( !file.exists() ){
			try {
				if ( file.createNewFile()) return true;
				else{
					Utils.warn("Could not create "+tag+" file.");
				}
			} catch (IOException e) {
				Utils.warn("Could not create "+tag+" file: "+e.getMessage());
				return false;
			}
		}
		return true;
	}
	
	public File getVanishedFile(){
		return vanishedFile;
	}
	
	public void setVanishedFile(File file) {
		vanishedFile = file;
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	void onPlayerQuit(PlayerQuitEvent event){
		Player player = event.getPlayer();
		String name = player.getName();
		if ( settings.suppressQuitMessage && isVanished(name)){
			event.setQuitMessage(null);
			if (settings.notifyState){
				String msg = SimplyVanish.msgLabel+ChatColor.GREEN+name+ChatColor.GRAY+" quit.";
				for (Player other : Bukkit.getServer().getOnlinePlayers()){
					if ( Utils.hasPermission(other, settings.notifyStatePerm)) other.sendMessage(msg);
				}
			}
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	void onPlayerKick(PlayerKickEvent event){
		// (still set if cancelled)
		Player player = event.getPlayer();
		String name = player.getName();
		if ( settings.suppressQuitMessage && isVanished(name)){
			event.setLeaveMessage(null);
			if (settings.notifyState && !event.isCancelled()){
				String msg = SimplyVanish.msgLabel+ChatColor.GREEN+name+ChatColor.GRAY+" was kicked.";
				for (Player other : Bukkit.getServer().getOnlinePlayers()){
					if ( Utils.hasPermission(other, settings.notifyStatePerm)) other.sendMessage(msg);
				}
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOW)
	final void onEntityTarget(final EntityTargetEvent event){
		if ( event.isCancelled() ) return;
		final Entity target = event.getTarget();
		if (!(target instanceof Player)) return;
		final String playerName = ((Player) target).getName();
		final VanishConfig cfg = vanishConfigs.get(playerName.toLowerCase());
		if (cfg == null) return;
		if (cfg.vanished){
			if (settings.expEnabled && !cfg.pickup){
				Entity entity = event.getEntity();
				if ( entity instanceof ExperienceOrb){
					repellExpOrb((Player) target, (ExperienceOrb) entity);
					event.setCancelled(true);
					event.setTarget(null);
					return;
				}
			}
			if (!cfg.target) event.setTarget(null);
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	void onPotionSplash(PotionSplashEvent event){
		try{
			final List<Entity> rem = new LinkedList<Entity>();
			final Collection<LivingEntity> affected = event.getAffectedEntities();
			for ( LivingEntity entity : affected){
				if (entity instanceof Player ){
					String playerName = ((Player) entity).getName();
					VanishConfig cfg = vanishConfigs.get(playerName.toLowerCase());
					if (cfg == null) continue;
					if (cfg.vanished){
						if (!cfg.damage) rem.add(entity);
					}
				}
			}
			if (!rem.isEmpty()) affected.removeAll(rem);
		} catch(Throwable t){
			// ignore (fast addition.)
		}
	}
	
//	@EventHandler(priority=EventPriority.HIGHEST)
//	void onServerListPing(ServerListPingEvent event){
//		// TODO: try reflection ??
//	}
	
	/**
	 * Attempt some workaround for experience orbs:
	 * prevent it getting near the player.
	 * @param target
	 * @param entity
	 */
	void repellExpOrb(Player player, ExperienceOrb orb) {
		Location pLoc = player.getLocation();
		Location oLoc = orb.getLocation();
		Vector dir = oLoc.toVector().subtract(pLoc.toVector());
		double dx = Math.abs(dir.getX());
		double dz = Math.abs(dir.getZ());
		if ( (dx == 0.0) && (dz == 0.0)){
			// Special case probably never happens
			dir.setX(0.001);
		}
		if ((dx < settings.expThreshold) && (dz < settings.expThreshold)){
			Vector nDir = dir.normalize();
			Vector newV = nDir.clone().multiply(settings.expVelocity);
			newV.setY(0);
			orb.setVelocity(newV);
			if ((dx < settings.expTeleDist) && (dz < settings.expTeleDist)){
				// maybe oLoc
				orb.teleport(oLoc.clone().add(nDir.multiply(settings.expTeleDist)), TeleportCause.PLUGIN);
			} 
			if ((dx < settings.expKillDist) && (dz < settings.expKillDist)){
				orb.remove();
			} 
		} 
	}

	@EventHandler(priority=EventPriority.LOW)
	final void onEntityDamage(final EntityDamageEvent event){
		if ( event.isCancelled() ) return;
		final Entity entity = event.getEntity();
		if (!(entity instanceof Player)) return;
		final String playerName = ((Player) entity).getName();
		final VanishConfig cfg = vanishConfigs.get(playerName.toLowerCase());
		if (cfg == null) return;
		if (!cfg.vanished || cfg.damage) return;
		event.setCancelled(true);
		if ( entity.getFireTicks()>0) entity.setFireTicks(0);
	}
	
	@EventHandler(priority=EventPriority.LOW)
	void onItemPickUp(PlayerPickupItemEvent event){
		if ( event.isCancelled() ) return;
		Player player = event.getPlayer();
		VanishConfig cfg = vanishConfigs.get(player.getName().toLowerCase());
		if (cfg == null) return;
		if (!cfg.vanished) return;
		if (!cfg.pickup) event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.LOW)
	void onItemDrop(PlayerDropItemEvent event){
		if ( event.isCancelled() ) return;
		Player player = event.getPlayer();
		VanishConfig cfg = vanishConfigs.get(player.getName().toLowerCase());
		if (cfg == null) return;
		if (!cfg.vanished) return;
		if (!cfg.drop) event.setCancelled(true);
	}

	/**
	 * Only has relevance for static access by Plugin.
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	/**
	 * Only for static access by plugin.
	 * @return
	 */
	public boolean isEnabled(){
		return enabled;
	}
	
	public void setSettings(Settings settings){
		this.settings = settings;
	}
	
	/**
	 * Adjust state of player to vanished, message player.
	 * @param player
	 */
	public void onVanish(Player player) {
		onVanish(player, true);
	}
	
	/**
	 * Adjust state of player to vanished.
	 * @param player
	 * @param message If to message players.
	 */
	public void onVanish(Player player, boolean message) {
		String name = player.getName();
		boolean was = !addVanishedName(name);
		String msg = null;
		if (settings.sendFakeMessages && !settings.fakeQuitMessage.isEmpty()){
			msg = settings.fakeQuitMessage.replaceAll("%name", name);
			msg = msg.replaceAll("%displayname", player.getDisplayName());
		}
		for ( Player other : Bukkit.getServer().getOnlinePlayers()){
			if (other.getName().equals(name)) continue;
			if ( other.canSee(player)){
				// (only consider a changed canSee state)
				if (settings.notifyState && Utils.hasPermission(other, settings.notifyStatePerm)){
					if (!was){
						if (message) other.sendMessage(SimplyVanish.msgLabel+ChatColor.GREEN+name+ChatColor.GRAY+" vanished.");
					}
					if (!shouldSeeVanished(other)) hidePlayer(player, other);
				} else if (!shouldSeeVanished(other)){
					hidePlayer(player, other);
					if (msg != null) other.sendMessage(msg);
				}
			} else if (!was && settings.notifyState && Utils.hasPermission(other, settings.notifyStatePerm)){
				if (message) other.sendMessage(SimplyVanish.msgLabel+ChatColor.GREEN+name+ChatColor.GRAY+" vanished.");
			}
		}
		if (message) player.sendMessage(was?SimplyVanish.msgStillInvisible:SimplyVanish.msgNowInvisible);
	}

	/**
	 * Central access point for checking if player has permission and wants to see vanished players.
	 * @param player
	 * @return
	 */
	public  boolean shouldSeeVanished(Player player) {
		VanishConfig cfg = vanishConfigs.get(player.getName().toLowerCase());
		if(cfg!=null){
			if (!cfg.see) return false;
		}
		return Utils.hasPermission(player, "simplyvanish.see-all"); 
	}

	/**
	 * Adjust state of player to not vanished.
	 * @param player
	 */
	public void onReappear(Player player) {
		String name = player.getName();
		boolean was = removeVanishedName(name);
		String msg = null;
		if (settings.sendFakeMessages && !settings.fakeJoinMessage.isEmpty()){
			msg = settings.fakeJoinMessage.replaceAll("%name", name);
			msg = msg.replaceAll("%displayname", player.getDisplayName());
		}
		for ( Player other : Bukkit.getServer().getOnlinePlayers()){
			if (other.getName().equals(name)) continue;
			if (!other.canSee(player)){
				// (only consider a changed canSee state)
				showPlayer(player, other);
				if (settings.notifyState && Utils.hasPermission(other, settings.notifyStatePerm)){
					other.sendMessage(SimplyVanish.msgLabel+ChatColor.RED+name+ChatColor.GRAY+" reappeared.");
				} else if (!shouldSeeVanished(other)){
					if (msg != null) other.sendMessage(msg);
				}
			} 
			else if (was && settings.notifyState && Utils.hasPermission(other, settings.notifyStatePerm)){
				other.sendMessage(SimplyVanish.msgLabel+ChatColor.RED+name+ChatColor.GRAY+" reappeared.");
			}
		}
		player.sendMessage(SimplyVanish.msgLabel+ChatColor.GRAY+"You are "+(was?"still":"now")+" "+ChatColor.RED+"visible"+ChatColor.GRAY+" to everyone!");
	}
	
	/**
	 * Heavy update for who can see this player and whom this player can see.
	 * @param player
	 */
	public void updateVanishState(Player player){
		updateVanishState(player, true);
	}
	
	/**
	 * Heavy update for who can see this player and whom this player can see and other way round.
	 * @param player
	 * @param message If to message the player.
	 */
	public void updateVanishState(Player player, boolean message){
		String playerName = player.getName();
		String lcName = playerName.toLowerCase();
		Server server = Bukkit.getServer();
		Player[] players = server.getOnlinePlayers();
		boolean shouldSee = shouldSeeVanished(player);
		boolean was = removeVanishedName(lcName);
		// Show or hide other players to player:
		for (Player other : players){
			if (shouldSee||!isVanished(other.getName())){
				if (!player.canSee(other)) showPlayer(other, player);
			} 
			else if (player.canSee(other)) hidePlayer(other, player);
			if (!was && !other.canSee(player)) showPlayer(player, other);   
			
		}
		if (was) onVanish(player, message); // remove: a) do not save 2x b) people will get notified.		
	}
	
	/**
	 * Unlikely that sorted is needed, but anyway.
	 * @return
	 */
	public List<String> getSortedVanished(){
		Collection<String> vanished = getVanishedPlayers();
		List<String> sorted = new ArrayList<String>(vanished.size());
		sorted.addAll(vanished);
		Collections.sort(sorted);
		return sorted;
	}
	
	/**
	 * Show player to canSee.
	 * Delegating method, for the case of other things to be checked.
	 * @param player The player to show.
	 * @param canSee 
	 */
	void showPlayer(Player player, Player canSee){
		if (!checkInvolved(player, canSee, "showPlayer")) return;
		try{
			canSee.showPlayer(player);
		} catch(Throwable t){
			Utils.severe("showPlayer failed (show "+player.getName()+" to "+canSee.getName()+"): "+t.getMessage());
			t.printStackTrace();
			onPanic(new Player[]{player, canSee});
		}
	}
	
	/**
	 * Hide player from canNotSee.
	 * Delegating method, for the case of other things to be checked.
	 * @param player The player to hide.
	 * @param canNotSee
	 */
	void hidePlayer(Player player, Player canNotSee){
		if (!checkInvolved(player, canNotSee, "hidePlayer")) return;
		try{
			canNotSee.hidePlayer(player);
		} catch ( Throwable t){
			Utils.severe("hidePlayer failed (hide "+player.getName()+" from "+canNotSee.getName()+"): "+t.getMessage());
			t.printStackTrace();
			onPanic(new Player[]{player, canNotSee});
		}
	}
	
	/**
	 * Do online checking and also check settings if to continue.
	 * @param player1 The player to be shown or hidden.
	 * @param player2
	 * @param tag
	 * @return true if to continue false if to abort.
	 */
	boolean checkInvolved(Player player1, Player player2, String tag){
		boolean inconsistent = false;
		if (!Utils.checkOnline(player1, tag)) inconsistent = true;
		if (!Utils.checkOnline(player2, tag)) inconsistent = true;
		if (settings.noAbort){
			return true;
		} else if (inconsistent){
			try{
				player1.sendMessage(SimplyVanish.msgLabel+ChatColor.RED+"Warning: Could not use "+tag+" to player: "+player2.getName());
			} catch (Throwable t){	
			}
		}
		return !inconsistent; // "true = continue = not inconsistent"
	}
	
	void onPanic(Player[] involved){
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

	public boolean addVanishedName(String name) {
		VanishConfig cfg = getVanishConfig(name);
		if (!cfg.vanished){
			cfg.vanished = true;
			if (settings.saveVanishedAlways) saveVanished();
			return true;
		}
		else return false;
	}

	/**
	 * 
	 * @param name
	 * @return If the player was vanished.
	 */
	public boolean removeVanishedName(String name) {
		VanishConfig cfg = vanishConfigs.get(name.toLowerCase());
		if (cfg==null) return false;
		if (cfg.vanished){
			cfg.vanished = false;
			if (!cfg.needsSave()) vanishConfigs.remove(name.toLowerCase());
			if (settings.saveVanishedAlways) saveVanished();
			return true;
		}
		else return false;
	}

	public String getVanishedMessage() {
		List<String> sorted = getSortedVanished();
		StringBuilder builder = new StringBuilder();
		builder.append(ChatColor.GOLD+"[VANISHED]");
		Server server = Bukkit.getServer();
		boolean found = false;
		for ( String n : sorted){
			Player player = server.getPlayerExact(n);
			VanishConfig cfg = vanishConfigs.get(n);
			if (!cfg.vanished) continue;
			found = true;
			boolean isNosee = !cfg.see; // is lower case
			if ( player == null ){
				builder.append(" "+ChatColor.GRAY+"("+n+")");
				if (isNosee) builder.append(ChatColor.DARK_RED+"[NOSEE]");
			}
			else{
				builder.append(" "+ChatColor.GREEN+player.getName());
				if (!Utils.hasPermission(player, "simplyvanish.see-all")) builder.append(ChatColor.DARK_RED+"[CANTSEE]");
				else if (isNosee) builder.append(ChatColor.RED+"[NOSEE]");
			}
		}
		if (!found) builder.append(" "+ChatColor.DARK_GRAY+"<none>");
		return builder.toString();
	}
	
	/**
	 * Get a VanishConfig, create it if necessary.<br>
	 * (Might be from vanished, parked, or new thus put to parked).
	 * @param playerName
	 * @return
	 */
	public VanishConfig getVanishConfig(String playerName){
		playerName = playerName.toLowerCase();
		VanishConfig cfg = vanishConfigs.get(playerName);
		if (cfg != null) return cfg;
		cfg = new VanishConfig();
		vanishConfigs.put(playerName, cfg);
		return cfg;
	}

	public boolean isVanished(final String playerName) {
		final VanishConfig cfg = vanishConfigs.get(playerName.toLowerCase());
		if (cfg == null) return false;
		else return cfg.vanished;
	}

	public void setVanished(String playerName, boolean vanished) {
		Player player = Bukkit.getServer().getPlayerExact(playerName);
		if (player != null){
			// The simple part.
			if ( vanished) onVanish(player, true);
			else onReappear(player);
			return;
		}
		// The less simple part.
		if (vanished) addVanishedName(playerName);
		else if (removeVanishedName(playerName)) return;
		else{
			// Expensive part:
			String match = null;
			for (String n : vanishConfigs.keySet()){
				if ( n.equalsIgnoreCase(playerName)){
					match = n;
					break;
				}
			}
			if ( match != null) removeVanishedName(match);
		}
	}

	/**
	 * Lower case names.<br>
	 * Currently iterates over all VanishConfig entries.
	 * @return
	 */
	public Set<String> getVanishedPlayers() {
		Set<String> out = new HashSet<String>();
		for (Entry<String, VanishConfig> entry : vanishConfigs.entrySet()){
			if (entry.getValue().vanished) out.add(entry.getKey());
		}
		return out;
	}

	/**
	 * Only set the flags, no save.
	 * @param name
	 * @param args
	 * @param startIndex
	 */
	public void setFlags(String name, String[] args, int startIndex) {
		VanishConfig cfg = getVanishConfig(name);
		boolean hasClearFlag = false;
		for ( int i = startIndex; i<args.length; i++){
			if ( args[i].trim().toLowerCase().equals("*clear")){
				hasClearFlag = true;
				break; // currently break.
			}
		}
		if (hasClearFlag){
			final VanishConfig ncfg = new VanishConfig();
			ncfg.vanished = cfg.vanished;
			vanishConfigs.put(name.trim().toLowerCase(), ncfg);
			cfg = ncfg;
		}
		cfg.readFromArray(args, startIndex, false);
	}

	public void onShowFlags(CommandSender sender, String name) {
		if ( name == null) name = sender.getName();
		name = name.toLowerCase();
		VanishConfig cfg = vanishConfigs.get(name);
		if (cfg==null) return;
		sender.sendMessage(SimplyVanish.msgLabel+ChatColor.GRAY+"Flags("+name+"): "+cfg.toLine());
	}

	public void onNotifyPing() {
		if (!settings.pingEnabled) return;
		for ( final Entry<String, VanishConfig> entry : vanishConfigs.entrySet()){
			final Player player = Bukkit.getPlayerExact(entry.getKey());
			if (player==null) continue;
			final VanishConfig cfg = entry.getValue();
			if (!cfg.vanished) continue;
			if (!cfg.ping) continue;
			player.sendMessage(SimplyVanish.msgNotifyPing);
		}
	}


}
