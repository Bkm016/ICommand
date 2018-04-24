package me.skymc.icommand.event;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import lombok.Getter;
import lombok.Setter;
import me.skymc.icommand.temp.ExecuteTempData;

/**
 * @author Bkm016
 * @since 2018-03-21
 */
public class ICommandItemLoadEvent extends Event implements Cancellable {
	
	private static final HandlerList handlers = new HandlerList();

	@Getter private Player player;
	@Getter private String type;
	@Getter private Event event;
	@Getter private HashMap<Integer, ExecuteTempData> items;
	@Getter @Setter private boolean isCancelled;
	
	/**
	 * 构造方法
	 */
	public ICommandItemLoadEvent(Player player, String type, Event event, HashMap<Integer, ExecuteTempData> items) {
		this.player = player;
		this.items = items;
		this.type = type;
		this.event = event;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
}
