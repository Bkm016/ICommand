package me.skymc.icommand.util;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * @author Bkm016
 * @since 2018-03-22
 */
public class EntityUtils {
	
	/**
	 * 获取伤害来源
	 * 
	 * @param e 伤害事件
	 * @return {@link LivingEntity}}
	 */
	public static LivingEntity getDamager(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof LivingEntity) {
			return (LivingEntity) e.getDamager();
		}
		if (e.getDamager() instanceof Projectile && ((Projectile) e.getDamager()).getShooter() instanceof LivingEntity) {
			return (LivingEntity) ((Projectile) e.getDamager()).getShooter();
		} 
		return null;
	}
}
