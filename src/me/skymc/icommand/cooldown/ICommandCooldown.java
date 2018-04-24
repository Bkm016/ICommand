package me.skymc.icommand.cooldown;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import me.skymc.icommand.ICommandPlugin;
import me.skymc.icommand.item.ICommandItemAbstract;
import me.skymc.taboolib.database.PlayerDataManager;

/**
 * @author Bkm016
 * @since 2018-03-22
 */
public class ICommandCooldown implements Listener {
	
	private static ICommandCooldown inst;
	
	private ICommandCooldown () {
		Bukkit.getPluginManager().registerEvents(this, ICommandPlugin.getInst());
	}
	
	public static ICommandCooldown getInst() {
		if (inst == null) {
			synchronized (ICommandCooldown.class) {
				if (inst == null) {
					inst = new ICommandCooldown();
				}
			}
		}
		return inst;
	}
	
	/**
	 * 获取该物品类型的冷却时间
	 * 
	 * @param player 玩家
	 * @param item 物品
	 * @return
	 */
	public long getCooldown(Player player, ICommandItemAbstract item) {
		FileConfiguration configuration = PlayerDataManager.getPlayerData(player);
		// 如果没数据
		if (!configuration.contains("ICommand.cooldown." + item.getId())) {
			configuration.set("ICommand.cooldown." + item.getId(), System.currentTimeMillis() + item.getCooldown());
			return 0;
		} else {
			// 获取时间差
			long time = configuration.getLong("ICommand.cooldown." + item.getId()) - System.currentTimeMillis();
			if (time <= 0) {
				configuration.set("ICommand.cooldown." + item.getId(), System.currentTimeMillis() + item.getCooldown());
			}
			return time < 0 ? 0 : time;
		}
	}
	
	/**
	 * 刷新冷却时间
	 * 
	 * @param player 玩家
	 * @param type 类型
	 */
	public void updateCooldown(Player player, String type) {
		PlayerDataManager.getPlayerData(player).set("ICommand.cooldown." + type, null);
	}
}
