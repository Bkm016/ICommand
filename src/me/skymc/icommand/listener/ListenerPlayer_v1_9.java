package me.skymc.icommand.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import me.skymc.icommand.ICommandExecuter;

/**
 * @author Bkm016
 * @since 2018-03-26
 */
public class ListenerPlayer_v1_9 implements Listener {
	
	@EventHandler (priority = EventPriority.NORMAL)
	public static void PLAYER(PlayerSwapHandItemsEvent e) {
		if (!e.isCancelled()) {
			ICommandExecuter.execute(e.getPlayer(), "PLAYER_SWAPHAND", e);
		}
	}

}
