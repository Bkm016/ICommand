package me.skymc.icommand.item;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import lombok.Getter;
import me.skymc.icommand.ICommandLoader;
import me.skymc.icommand.ICommandPlugin;
import me.skymc.taboolib.inventory.ItemUtils;
import me.skymc.taboolib.message.MsgUtils;
import me.skymc.taboolib.other.NumberUtils;

/**
 * @author Bkm016
 * @since 2018-03-23
 */
public class ICommandItemInteract extends ICommandItemAbstract {
	
	@Getter private List<String> targetBlock;
	@Getter private List<String> targetBlockFace;
	@Getter private List<String> targetBlockAgainst;
	@Getter private List<String> targetBlockDown;
	@Getter private List<String> targetBlockUp;
	@Getter private List<Location> targetLocation;
	@Getter private List<String> targetType;
	@Getter private List<String> targetName;
	@Getter private List<String> contents;
	
	public ICommandItemInteract(ConfigurationSection section) {
		super(section);
		targetBlock = section.contains("Target.Block") ? section.getStringList("Target.Block") : null;
		targetBlockFace = section.contains("Target.BlockFace") ? section.getStringList("Target.BlockFace") : null;
		targetBlockAgainst = section.contains("Target.BlockAgainst") ? section.getStringList("Target.BlockAgainst") : null;
		targetBlockDown = section.contains("Target.BlockDown") ? section.getStringList("Target.BlockDown") : null;
		targetBlockUp = section.contains("Target.BlockUp") ? section.getStringList("Target.BlockUp") : null;
		targetType = section.contains("Target.Type") ? section.getStringList("Target.Type") : null;
		targetName = section.contains("Target.Name") ? section.getStringList("Target.Name") : null;
		contents = section.contains("Contents") ? section.getStringList("Contents") : null;
		if (section.contains("Target.Location")) {
			targetLocation = new ArrayList<>();
			for (String preLocation : section.getStringList("Target.Location")) {
				try {
					targetLocation.add(new Location(Bukkit.getWorld(
							preLocation.split(":")[0]), 
							Double.valueOf(preLocation.split(":")[1].split(",")[0]),
							Double.valueOf(preLocation.split(":")[1].split(",")[1]),
							Double.valueOf(preLocation.split(":")[1].split(",")[2])));
				} catch (Exception e) {
					MsgUtils.warn("坐标载入失败: &4" + preLocation, ICommandPlugin.getInst());
					MsgUtils.warn("失败原因: &4" + e.getMessage(), ICommandPlugin.getInst());
				}
			}
		}
	}
	
	@Override
	public boolean onExecutePre(Player player, ItemStack item, Integer slot, Event event, ICommandItemAbstract type) {
		if (event instanceof PlayerInteractEvent) {
			PlayerInteractEvent e = (PlayerInteractEvent) event;
			/**
			 * 修复 MOJANG 的某个 BUG 设定
			 * 右键空气的时候如果消耗手中物品必须取消事件
			 */
			if (e.getAction() == Action.RIGHT_CLICK_AIR) {
				e.setCancelled(true);
			}
			else if (ICommandLoader.arrayContains(type.getType(), "RIGHT_CLICK_BLOCK") 
					|| ICommandLoader.arrayContains(type.getType(), "LEFT_CLICK_BLOCK")
					|| ICommandLoader.arrayContains(type.getType(), "PHYSICAL")) {
				return ICommandLoader.isTargetContains(targetBlock, getBlockFullName(e.getClickedBlock()))
						&& ICommandLoader.isTargetContains(targetBlockFace, e.getBlockFace().name())
						&& ICommandLoader.isTargetContains(targetLocation, e.getClickedBlock().getLocation());
			}
		}
		else if (event instanceof PlayerInteractEntityEvent) {
			PlayerInteractEntityEvent e = (PlayerInteractEntityEvent) event;
			return ICommandLoader.isTargetContains(targetType, e.getRightClicked().getType().name()) 
					&& ICommandLoader.isTargetContains(targetName, e.getRightClicked().getName());
		}
		else if (event instanceof BlockPlaceEvent) {
			BlockPlaceEvent e = (BlockPlaceEvent) event;
			return ICommandLoader.isTargetContains(targetBlock, getBlockFullName(e.getBlock()))
					&& ICommandLoader.isTargetContains(targetBlockAgainst, getBlockFullName(e.getBlockAgainst()))
					&& ICommandLoader.isTargetContains(targetBlockDown, getBlockFullName(e.getBlock().getRelative(BlockFace.DOWN)))
					&& ICommandLoader.isTargetContains(targetBlockUp, getBlockFullName(e.getBlock().getRelative(BlockFace.UP)))
					&& ICommandLoader.isTargetContains(targetLocation, e.getBlock().getLocation());
		}
		else if (event instanceof BlockBreakEvent) {
			BlockBreakEvent e = (BlockBreakEvent) event;
			return ICommandLoader.isTargetContains(targetBlock, getBlockFullName(e.getBlock()))
					&& ICommandLoader.isTargetContains(targetBlockDown, getBlockFullName(e.getBlock().getRelative(BlockFace.DOWN)))
					&& ICommandLoader.isTargetContains(targetBlockUp, getBlockFullName(e.getBlock().getRelative(BlockFace.UP)))
					&& ICommandLoader.isTargetContains(targetLocation, e.getBlock().getLocation());
		}
		return true;
	}

	@Override
	public void onExecutePost(Player player, ItemStack item, Integer slot, Event event, ICommandItemAbstract type) {
		if (event instanceof BlockPlaceEvent && contents != null) {
			BlockPlaceEvent e = (BlockPlaceEvent) event;
			new BukkitRunnable() {
				
				@Override
				public void run() {
					if (e.getBlock().getState() instanceof BlockState && e.getBlock().getState() instanceof InventoryHolder) {
						BlockState blockState = e.getBlock().getState();
						InventoryHolder holder = (InventoryHolder) e.getBlock().getState();
						for (String itemResult : contents) {
							try {
								ItemStack itemStack = ItemUtils.getCacheItem(itemResult.split(" ")[0]);
								if (itemStack != null && NumberUtils.getRand().nextDouble() <= Double.valueOf(itemResult.split(" ")[2])) {
									itemStack.setAmount(Integer.valueOf(itemResult.split(" ")[1]));
									holder.getInventory().addItem(itemStack);
									blockState.update();
								}
							} catch (Exception err) {
								MsgUtils.warn("类型 &4" + type.getId() + "&c 的 &4Contents&c 代码执行异常");
								MsgUtils.warn("原因: &4" + err.getMessage(), ICommandPlugin.getInst());
								return;
							}
						}
					}
				}
			}.runTask(ICommandPlugin.getInst());
		}
	}
	
	@SuppressWarnings("deprecation")
	private String[] getBlockFullName(Block block) {
		return block == null || block.getType().equals(Material.AIR) ? new String[] { "0", "0:0" } : new String[] { block.getType().name(), block.getType().name() + ":" + block.getData() };
	}
}
