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
	 * ��ȡ����Ʒ���͵���ȴʱ��
	 * 
	 * @param player ���
	 * @param item ��Ʒ
	 * @return
	 */
	public long getCooldown(Player player, ICommandItemAbstract item) {
		FileConfiguration configuration = PlayerDataManager.getPlayerData(player);
		// ���û����
		if (!configuration.contains("ICommand.cooldown." + item.getId())) {
			configuration.set("ICommand.cooldown." + item.getId(), System.currentTimeMillis() + item.getCooldown());
			return 0;
		} else {
			// ��ȡʱ���
			long time = configuration.getLong("ICommand.cooldown." + item.getId()) - System.currentTimeMillis();
			if (time <= 0) {
				configuration.set("ICommand.cooldown." + item.getId(), System.currentTimeMillis() + item.getCooldown());
			}
			return time < 0 ? 0 : time;
		}
	}
	
	/**
	 * ˢ����ȴʱ��
	 * 
	 * @param player ���
	 * @param type ����
	 */
	public void updateCooldown(Player player, String type) {
		PlayerDataManager.getPlayerData(player).set("ICommand.cooldown." + type, null);
	}
}
