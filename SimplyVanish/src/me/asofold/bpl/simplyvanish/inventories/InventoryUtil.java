package me.asofold.bpl.simplyvanish.inventories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import me.asofold.bpl.simplyvanish.SimplyVanish;
import me.asofold.bpl.simplyvanish.config.Settings;
import me.asofold.bpl.simplyvanish.config.VanishConfig;
import me.asofold.bpl.simplyvanish.util.Utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryUtil {
	
	/**
	 * Show inventory based on settings.
	 * @param viewer
	 * @param settings
	 */
	public static void showInventory(final CommandSender viewer, final VanishConfig cfg, final String playerName, final Settings settings){
		if (settings.allowRealPeek && viewer instanceof Player && SimplyVanish.hasPermission(viewer, "simplyvanish.inventories.peek.real")){
			final Player player = (Player) viewer;
			Bukkit.getScheduler().scheduleSyncDelayedTask(SimplyVanish.getPluginInstance(), new Runnable() {
				@Override
				public void run() {
					final Player other = Bukkit.getPlayerExact(playerName);
					if (other == null){
						Utils.send(viewer, SimplyVanish.msgLabel + ChatColor.RED + "Not available: " +playerName);
						return;
					}
					if (player.getOpenInventory() != null) player.closeInventory();
					final Inventory inv = other.getInventory();
					prepareInventoryOpen(player, inv, cfg); // TODO
					// TODO: trigger OpenInv if modifiable !
					player.openInventory(inv);
				}
			});
		}
		else{
			final Player other = Bukkit.getPlayerExact(playerName);
			if (other == null){
				Utils.send(viewer, SimplyVanish.msgLabel + ChatColor.RED + "Not available: " +playerName);
				return;
			}
			List<ItemStack> items = new LinkedList<ItemStack>();
			for (ItemStack stack : other.getInventory().getContents()){
				if (stack != null) items.add(stack);
			}
			StringBuilder b = new StringBuilder();
			b.append("Inventory(" + other.getName() + "): ");
			addItemDescr(items, b);
			viewer.sendMessage(b.toString());
		}
	}
	
	/**
	 * Set the preventInventoryAction flag according to permissions.
	 * @param player
	 * @param inventory
	 * @param cfg
	 */
	public static void prepareInventoryOpen(Player player, Inventory inventory, VanishConfig cfg) {
		if (SimplyVanish.hasPermission(player, "simplyvanish.inventories.manipulate")) cfg.preventInventoryAction = false;
		else if (inventory == player.getInventory()) cfg.preventInventoryAction = false;
		else cfg.preventInventoryAction = true;
	}
	
	/**
	 * Get String with sorted item descriptions.
	 * @param items
	 * @return
	 */
	public static String getItemDescr(Collection<ItemStack> items){
		StringBuilder builder = new StringBuilder();
		addItemDescr(items, builder);
		return builder.toString();
	}
	
	/**
	 * Add verbalized and sorted item descriptions.
	 * @param items
	 * @param builder
	 */
	public static void addItemDescr(Collection<ItemStack> items, StringBuilder builder) {
		if (items.isEmpty()) return;
		List<String> keys = new ArrayList<String>(items.size()); // will rather be shorter.
		Map<String, Integer>  dropList = new HashMap<String, Integer>();
		for ( ItemStack stack:items){
			if (stack == null) continue;
			if (stack.getTypeId() == 0) continue;
			int d ;
			if ( stack.getType().isBlock()) d = stack.getData().getData();
			else d = stack.getDurability();
			String key;
			key =  getShortestItemName(stack.getTypeId(), d).toLowerCase();
			Map<Enchantment, Integer> enchantments = stack.getEnchantments();
			if ( enchantments != null) {
				if ( !enchantments.isEmpty()){
					List<String> es = new ArrayList<String>(enchantments.size());
					for ( Enchantment e : enchantments.keySet()){
						es.add(e.getName()+"@"+enchantments.get(e));
					}
					Collections.sort(es);
					key+="(";
					for (String s : es){
						key +=s+",";
					}
					key+=")";
				}
			}
			Integer n = dropList.get(key);
			if ( n != null) dropList.put(key, n+stack.getAmount());
			else{
				dropList.put(key,  stack.getAmount());
				keys.add(key);
			}
		}
		Collections.sort(keys);
		for ( String key : keys){
			builder.append(key+" x"+dropList.get(key)+", ");
		}
	}

	public static final String getShortestItemName(final int id, final int data) {
		try{
			final Material mat = Material.getMaterial(id);
			if (mat == null){
				if (data == 0) return "" + id;
				else return "" + id + ":" + data;
			}
			if (data == 0) return mat.toString();
			else return mat.toString() + ":" + data;
		}
		catch (Throwable t){
			if (data == 0) return "" + id;
			else return "" + id + ":" + data;
		}
	}
	
}
