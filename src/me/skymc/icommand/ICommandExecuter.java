/**
 * 
 */
package me.skymc.icommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map.Entry;
import javax.script.SimpleBindings;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.skymc.icommand.cooldown.ICommandCooldown;
import me.skymc.icommand.event.ICommandExecuteEvent;
import me.skymc.icommand.event.ICommandItemLoadEvent;
import me.skymc.icommand.item.ICommandItemAbstract;
import me.skymc.icommand.script.ScriptHandler;
import me.skymc.icommand.temp.ExecuteTempData;
import me.skymc.icommand.util.TimeUtils;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.inventory.ItemUtils;
import me.skymc.taboolib.message.MsgUtils;
import me.skymc.taboolib.other.NumberUtils;

/**
 * @author Bkm016
 * @since 2018-03-21
 */
public class ICommandExecuter {
	
	private static Pattern changePattern = Pattern.compile("\\[(\\S+)%\\]");
	
	/**
	 * 执行物品效果
	 * 
	 * @param player 玩家
	 * @param type 类型
	 */
	public static void execute(Player player, String type, Event event) {
		if (ICommandPlugin.getInst().getConfig().getString("Settings.DisableType").contains(type)) {
			return;
		}
		long time = System.nanoTime();
		ICommandItemLoadEvent itemLoadEvent = new ICommandItemLoadEvent(player, type, event, getEffectiveItems(player, type, event));
		if (!isEventEffective(itemLoadEvent)) {
			return;
		}
		if (ICommandPlugin.getInst().getConfig().getBoolean("DEBUG")) {
			MsgUtils.send(player, "本次物品读取耗时: &f" + ((System.nanoTime() - time)/1000000D) + "ms");
			time = System.nanoTime();
		}
		for (Entry<Integer, ExecuteTempData> entry : itemLoadEvent.getItems().entrySet()) {
			if (!entry.getValue().getItemType().onExecutePre(player, entry.getValue().getItemStack(), entry.getKey(), event, entry.getValue().getItemType())) {
				continue;
			}
			if (!isMeetRequire(entry.getValue())) {
				executeNotMeetRequireAction(entry.getValue());
				continue;
			}
			long cooldown = getCooldown(entry.getValue());
			if (cooldown > 0) {
				executeCooldownAction(entry.getValue(), cooldown);
				continue;
			}
			if (entry.getValue().getItemType().isConsume()) {
				takeAmount(player, entry.getKey(), entry.getValue().getItemType().getAmount());
			}
			if (!isItemInChange(entry.getValue())) {
				executeItemOutOfChangeAction(entry.getValue());
				continue;
			}
			if (event instanceof Cancellable && entry.getValue().getItemType().isCancelled()) {
				((Cancellable) event).setCancelled(entry.getValue().getItemType().isCancelled());
			}
			entry.getValue().getItemType().onExecutePost(player, entry.getValue().getItemStack(), entry.getKey(), event, entry.getValue().getItemType());
			if (ICommandPlugin.getInst().getConfig().getBoolean("DEBUG")) {
				MsgUtils.send(player, "本次事件触发耗时: &f" + ((System.nanoTime() - time)/1000000D) + "ms");
			}
			executeCommand(entry.getValue(), entry.getValue().getItemType().getCommands());
			runExecuteCode(entry.getValue());
			new ICommandExecuteEvent(player, type, event).callEvent();
		}
	}
	
	/**
	 * 获取物品执行列表
	 * 
	 * @param player 玩家
	 * @param type 类型
	 * @return {@link HashMap}
	 */
	@SuppressWarnings("deprecation")
	public static HashMap<Integer, ExecuteTempData> getEffectiveItems(Player player, String type, Event event) {
		HashMap<Integer, ExecuteTempData> itemQueue = new HashMap<>();
		List<Integer> disableSlot = new ArrayList<>();
		for (ICommandItemAbstract itemType : ICommandLoader.getItemTypes(type)) {
			for (Integer slot : itemType.getSlots()) {
				if (!disableSlot.contains(slot)) {
					ItemStack itemStack = player.getInventory().getItem(slot);
					if (hasLore(itemStack, itemType.getLore())) {
						if (!itemType.isConsume() || itemStack.getAmount() >= itemType.getAmount()) {
							itemQueue.put(slot, new ExecuteTempData(player, type, slot, itemStack, itemType, event));
							disableSlot.add(slot);
						}
					}
				}
			}
			if (itemType.isHeldItem() && !disableSlot.contains(player.getInventory().getHeldItemSlot())) {
				ItemStack itemStack = TabooLib.getVerint() > 10800 ? player.getInventory().getItemInMainHand() : player.getItemInHand();
				if (hasLore(itemStack, itemType.getLore())) {
					if (!itemType.isConsume() || itemStack.getAmount() >= itemType.getAmount()) {
						itemQueue.put(player.getInventory().getHeldItemSlot(), new ExecuteTempData(player, type, player.getInventory().getHeldItemSlot(), itemStack, itemType, event));
						disableSlot.add(player.getInventory().getHeldItemSlot());
					}
				}
			}
		}
		return itemQueue;
	}
	
	private static boolean isEventEffective(ICommandItemLoadEvent itemLoadEvent) {
		return !(itemLoadEvent.getItems().size() == 0 || !itemLoadEvent.callEvent());
	}
	
	private static boolean hasLore(ItemStack item, String lore) {
		if (ItemUtils.isNull(item) || !item.getItemMeta().hasLore()) {
			return false;
		}
		for (String _lore : item.getItemMeta().getLore()) {
			if (_lore.contains(lore)) {
				return true;
			}
		}
		return false;
	}
	
	private static void takeAmount(Player player, Integer slot, Integer amount) {
		ItemStack itemStack = player.getInventory().getItem(slot);
		if (itemStack.getAmount() > amount) {
			itemStack.setAmount(itemStack.getAmount() - amount);
		} else {
			player.getInventory().setItem(slot, null);
		}
	}
	
	private static String placeholderHook(String command, HashMap<String, String> placeholder) {
		if (placeholder != null) {
			for (Entry<String, String> entry : placeholder.entrySet()) {
				command = command.replace(entry.getKey(), entry.getValue());
			}
		}
		return command;
	}
	
	private static boolean isCommandInChange(String command) {
		Matcher matcher = changePattern.matcher(command);
		if (matcher.find()) {
			Double change = 0D;
			try {
				change = Double.valueOf(matcher.group(1).toString());
				if (NumberUtils.getRand().nextDouble() < change / 100D) {
					return true;
				}
			} catch (Exception e) {
				MsgUtils.warn("几率识别错误 &4" + command, ICommandPlugin.getInst());
			}
		}
		return false;
	}
	
	private static String[] getEffectiveCommands(String command) {
		return command.replaceAll("\\[(\\S+)%\\]", "").split("\\|\\|");
	}
	
	private static void executeCommand(ExecuteTempData data, List<String> commands) {
		if (commands == null || commands.size() == 0) {
			return;
		}
		HashMap<String, String> placeholder = data.getItemType().onPlaceholderHook(data.getPlayer(), data.getItemStack(), data.getSlot(), data.getEvent());
		if (placeholder == null) {
			MsgUtils.warn("类型 &4" + data.getType() + "&c 的 &4onPlaceholderHook&c 方法返回参数异常", ICommandPlugin.getInst());
		}
		int delay = 0;
		for (String command : commands) {
			if (isCommandInChange(command)) {
				for (String subCommand : getEffectiveCommands(command)) {
					if (subCommand.startsWith("[delay] ")) {
						delay += NumberUtils.getInteger(command.substring("[delay] ".length()));
					} else {
						try {
							executeSubCommand(data.getPlayer(), placeholderHook(command, placeholder).replace("$player", data.getPlayer().getName()), delay);
						} catch (Exception e) {
							MsgUtils.warn("执行命令出错: &4" + data.getItemType().getId(), ICommandPlugin.getInst());
							MsgUtils.warn("出错原因: &4" + e.getMessage(), ICommandPlugin.getInst());
						}
					}
				}
			}
		}
	}
	
	private static void executeSubCommand(Player player, String command, Integer delay) {
		new BukkitRunnable() {
			
			@Override
			public void run() {
				if (command.startsWith("[player] ")) {
					player.chat("/" + command.substring("[player] ".length()));
				} else if (command.startsWith("[op]")) {
					boolean isOp = player.isOp();
					player.setOp(true);
					player.chat("/" + command.substring("[op] ".length()));
					player.setOp(isOp);
				} else if (command.startsWith("[console] ")) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.substring("[console] ".length()));
				} else {
					MsgUtils.warn("错误的指令格式 &4" + command, ICommandPlugin.getInst());
				}
			}
		}.runTaskLater(ICommandPlugin.getInst(), delay);
	}
	
	private static void runExecuteCode(ExecuteTempData data) {
		if (data.getItemType().getExecuteCode() != null) {
			long time = System.nanoTime();
			if (!ScriptHandler.isEnableScirptEngine()) {
				MsgUtils.warn("您的服务器没有 &4Nashorn &c驱动或已禁用, 无法使用 &4ExecuteCode &c功能", ICommandPlugin.getInst());
				return;
			}
			SimpleBindings simpleBindings = ScriptHandler.formatBindings(
					ICommandPlugin.getInst().getConfig().getConfigurationSection("TransmissionParameter.ExecuteCode"),
					new String[] {"Player", "Event", "Type", "ItemSlot", "ItemStack", "Plugin"}, 
					new Object[] {data.getPlayer(), data.getEvent(), data.getItemType(), data.getSlot(), data.getItemStack(), ICommandPlugin.getInst()});
			if (data.getItemType().getExecuteCodeParameter() != null) {
				for (Entry<String, Object> entry : data.getItemType().getExecuteCodeParameter().entrySet()) {
					simpleBindings.put(entry.getKey(), entry.getValue());
				}
			}
			try {
				data.getItemType().getExecuteCode().eval(simpleBindings);
			} catch (Exception e) {
				MsgUtils.warn("类型 &4" + data.getItemType().getId() + "&c 的 &4ExecuteCode&c 代码执行异常", ICommandPlugin.getInst());
				MsgUtils.warn("错误内容: &4" + e.getMessage(), ICommandPlugin.getInst());
			}
			if (ICommandPlugin.getInst().getConfig().getBoolean("DEBUG")) {
				MsgUtils.send(data.getPlayer(), "本次 &fExecuteCode &7运行耗时: &f" + ((System.nanoTime() - time)/1000000D) + "ms");
			}
		}
	}
	
	private static boolean isMeetRequire(ExecuteTempData data) {
		if (data.getItemType().getRequire() != null) {
			if (!ScriptHandler.isEnableScirptEngine()) {
				MsgUtils.warn("您的服务器没有 &4Nashorn &c驱动或已禁用, 无法使用 &4Require &c功能", ICommandPlugin.getInst());
				return false;
			}
			SimpleBindings simpleBindings = ScriptHandler.formatBindings(
					ICommandPlugin.getInst().getConfig().getConfigurationSection("TransmissionParameter.Require"),
					new String[] {"Player", "Event", "Type", "ItemSlot", "ItemStack"},
					new Object[] {data.getPlayer(), data.getEvent(), data.getItemType(), data.getSlot(), data.getItemStack()});
			try {
				return Boolean.valueOf(String.valueOf(data.getItemType().getRequire().eval(simpleBindings)));
			} catch (Exception e) {
				MsgUtils.warn("类型 &4" + data.getItemType().getId() + "&c 的 &4Require&c 代码执行异常", ICommandPlugin.getInst());
				return false;
			}
		} else {
			return true;
		}
	}
	
	private static void executeNotMeetRequireAction(ExecuteTempData data) {
		if (data.getItemType().getRequireMessage() != null) {
			data.getPlayer().sendMessage(data.getItemType().getRequireMessage().replace("$player", data.getPlayer().getName()).replace("&", "§"));
		}
		executeCommand(data, data.getItemType().getRequireCommands());
	}
	
	private static long getCooldown(ExecuteTempData data) {
		return data.getItemType().getCooldown() > 0 ? ICommandCooldown.getInst().getCooldown(data.getPlayer(), data.getItemType()) : 0;
	}
	
	private static void executeCooldownAction(ExecuteTempData data, long cooldown) {
		if (data.getItemType().getCooldownMessage() != null) {
			HashMap<String, Long> timeMap = TimeUtils.formatTime(cooldown/1000L);
			data.getPlayer().sendMessage(data.getItemType().getCooldownMessage()
					.replace("$second", String.valueOf(timeMap.get("second")))
					.replace("$minute", String.valueOf(timeMap.get("minute")))
					.replace("$hour", String.valueOf(timeMap.get("hour")))
					.replace("$day", String.valueOf(timeMap.get("day")))
					.replace("&", "§")
					);
		}
		executeCommand(data, data.getItemType().getCooldownCommands());
	}
	
	private static boolean isItemInChange(ExecuteTempData data) {
		return data.getItemType().getChange() == 0 || NumberUtils.getRand().nextDouble() <= data.getItemType().getChange();
	}
	
	private static void executeItemOutOfChangeAction(ExecuteTempData data) {
		if (data.getItemType().getChangeMessage() != null) {
			data.getPlayer().sendMessage(data.getItemType().getChangeMessage().replace("$player", data.getPlayer().getName()).replace("&", "§"));
		}
	}
}
