package me.skymc.icommand.temp;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import lombok.Getter;
import me.skymc.icommand.item.ICommandItemAbstract;

/**
 * @author Bkm016
 * @since 2018-03-27
 */
public class ExecuteTempData {

	@Getter private Player player;
	@Getter private String type;
	@Getter private ItemStack itemStack;
	@Getter private ICommandItemAbstract itemType;
	@Getter private int slot;
	@Getter private Event event;
	
	public ExecuteTempData(Player player, String type, Integer slot, ItemStack itemStack, ICommandItemAbstract itemType, Event event) {
		this.player = player;
		this.type = type;
		this.itemStack = itemStack;
		this.itemType = itemType;
		this.slot = slot;
		this.event = event;
	}
	
}
