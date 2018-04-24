package me.skymc.icommand.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import lombok.Getter;

/**
 * @author Bkm016
 * @since 2018-03-21
 */
public class ICommandExecuteEvent extends Event {
	
	private static final HandlerList handlers = new HandlerList();

	@Getter private Player player;
	@Getter private String type;
	@Getter private Event event;
	
	/**
	 * 构造方法
	 */
	public ICommandExecuteEvent(Player player, String type, Event event) {
		this.player = player;
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
