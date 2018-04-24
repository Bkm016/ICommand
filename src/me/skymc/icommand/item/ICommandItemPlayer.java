package me.skymc.icommand.item;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import me.skymc.icommand.ICommandLoader;
import me.skymc.icommand.ICommandPlugin;

/**
 * @author Bkm016
 * @since 2018-03-24
 */
public class ICommandItemPlayer extends ICommandItemAbstract {
	
	@Getter private List<String> targetCause;
	@Getter private List<String> targetState;
	@Getter private String chatFormat;
	@Getter private String chatMessage;
	@Getter private boolean sneaking;

	public ICommandItemPlayer(ConfigurationSection section) {
		super(section);
		targetCause = section.contains("Target.Cause") ? section.getStringList("Target.Cause") : null;
		targetState = section.contains("Target.State") ? section.getStringList("Target.State") : null;
		chatFormat = section.contains("Format") ? section.getString("Format") : null;
		chatMessage = section.contains("Message") ? section.getString("Message") : null;
		sneaking = section.getBoolean("Sneaking");
	}
		
	@Override
	public boolean onExecutePre(Player player, ItemStack item, Integer slot, Event event, ICommandItemAbstract type) {
		if (event instanceof PlayerTeleportEvent) {
			PlayerTeleportEvent e = (PlayerTeleportEvent) event;
			return ICommandLoader.isTargetContains(targetCause, e.getCause().name());
		}
		else if (event instanceof PlayerToggleSneakEvent) {
			return sneaking == player.isSneaking();
		}
		else if (event instanceof PlayerFishEvent) {
			PlayerFishEvent e = (PlayerFishEvent) event;
			return ICommandLoader.isTargetContains(targetState, e.getState().name());
		}
		return true;
	}
	
	@Override
	public void onExecutePost(Player player, ItemStack item, Integer slot, Event event, ICommandItemAbstract type) {
		if (event instanceof AsyncPlayerChatEvent) {
			AsyncPlayerChatEvent e = (AsyncPlayerChatEvent) event;
			if (chatFormat != null) {
				if (chatFormat != null) {
					if (ICommandPlugin.isPlaceholderAPI()) {
						e.setFormat(PlaceholderAPI.setPlaceholders(player, chatFormat.replace("$format", e.getFormat())));
					} else {
						e.setFormat(chatFormat.replace("$format", e.getFormat()));
					}
				}
			}
			if (chatMessage != null) {
				if (ICommandPlugin.isPlaceholderAPI()) {
					e.setMessage(PlaceholderAPI.setPlaceholders(player, chatMessage.replace("$message", e.getMessage())));
				} else {
					e.setMessage(chatMessage.replace("$message", e.getMessage()));
				}
			}
		}
	}
}
