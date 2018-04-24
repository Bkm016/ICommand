package me.skymc.icommand.command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import me.skymc.icommand.ICommandLoader;
import me.skymc.icommand.ICommandPlugin;
import me.skymc.icommand.cooldown.ICommandCooldown;
import me.skymc.icommand.script.ScriptHandler;

/**
 * @author Bkm016
 * @since 2018-03-23
 */
public class CommandHandler implements CommandExecutor {
	
	private HashMap<String, Class<?>> enums = new HashMap<>(); 
	
	public CommandHandler() {
		enums.put("DamageCause", DamageCause.class);
		enums.put("TeleportCause", TeleportCause.class);
		enums.put("FishState", State.class);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
		if (args.length == 0) {
			ICommandPlugin.getLanguage().get("COMMAND-HELP").send(sender);
		}
		else if (args[0].equalsIgnoreCase("reload")) {
			if (args.length != 2) {
				ICommandPlugin.getLanguage().get("COMMAND-EMPTY").send(sender);
				return true;
			}
			long time = System.currentTimeMillis();
			if (args[1].equalsIgnoreCase("all") || args[1].equalsIgnoreCase("config")) {
				ICommandPlugin.getInst().reloadConfig();
			}
			if (args[1].equalsIgnoreCase("all") || args[1].equalsIgnoreCase("script")) {
				ScriptHandler.reloadScripts();
			}
			if (args[1].equalsIgnoreCase("all") || args[1].equalsIgnoreCase("item")) {
				ICommandLoader.reloadItems();
			}
			ICommandPlugin.getLanguage().get("COMMAND-RELOAD", String.valueOf(System.currentTimeMillis() - time)).send(sender);
		}
		else if (args[0].equalsIgnoreCase("update")) {
			if (args.length != 3) {
				ICommandPlugin.getLanguage().get("COMMAND-EMPTY").send(sender);
				return true;
			}
			Player player = Bukkit.getPlayerExact(args[1]);
			if (player == null) {
				ICommandPlugin.getLanguage().get("COMMAND-PLAYER-OFFLINE", args[1]).send(sender);
				return true;
			}
			ICommandCooldown.getInst().updateCooldown(player, args[2]);
			ICommandPlugin.getLanguage().get("COMMAND-UPDATE", args[1], args[2]).send(sender);
		}
		else if (args[0].equalsIgnoreCase("enums")) {
			if (args.length < 2) {
				ICommandPlugin.getLanguage().get("COMMAND-EMPTY").send(sender);
			}
			else if (args[1].equalsIgnoreCase("list")) {
				ICommandPlugin.getLanguage().get("COMMAND-ENUMS-LIST", enums.keySet().toString()).send(sender);
			}
			else if (args[1].equalsIgnoreCase("info")) {
				enumInfo(sender, args);
			}
			else {
				ICommandPlugin.getLanguage().get("COMMAND-EMPTY").send(sender);
			}
		}
		return true;
	}
	
	private void enumInfo(CommandSender sender, String[] args) {
		if (args.length != 3) {
			ICommandPlugin.getLanguage().get("COMMAND-EMPTY").send(sender);
		}
		else if (!enums.containsKey(args[2])) {
			ICommandPlugin.getLanguage().get("COMMAND-ENUMS-NOTFOUND", args[2]).send(sender);
		}
		else {
			List<?> enumsList = Arrays.asList(enums.get(args[2]).getEnumConstants());
			ICommandPlugin.getLanguage().get("COMMAND-ENUMS-INFO", enumsList.toString()).send(sender);
		}
	}
}
