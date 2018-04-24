package me.skymc.icommand.item;

import java.util.HashMap;
import java.util.List;
import javax.script.CompiledScript;
import javax.script.SimpleBindings;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import lombok.Getter;
import me.skymc.icommand.ICommandLoader;
import me.skymc.icommand.ICommandPlugin;
import me.skymc.icommand.script.ScriptHandler;
import me.skymc.icommand.util.EntityUtils;
import me.skymc.taboolib.damage.GetDamager;
import me.skymc.taboolib.message.MsgUtils;

/**
 * @author Bkm016
 * @since 2018-03-21
 */
public class ICommandItemDamage extends ICommandItemAbstract {
	
	@Getter private CompiledScript damage;
	@Getter private List<String> targetType;
	@Getter private List<String> targetName;
	@Getter private List<String> targetCause;
	@Getter private List<String> targetBlock;
	
	public ICommandItemDamage(ConfigurationSection section) {
		super(section);
		targetType = section.contains("Target.Type") ? section.getStringList("Target.Type") : null;
		targetName = section.contains("Target.Name") ? section.getStringList("Target.Name") : null;
		targetCause = section.contains("Target.Cause") ? section.getStringList("Target.Cause") : null;
		targetBlock = section.contains("Target.Block") ? section.getStringList("Target.Block") : null;
		damage = section.contains("Damage") ? ScriptHandler.compile(section.getString("Damage")) : null;
	}
	
	@Override
	public boolean onExecutePre(Player player, ItemStack item, Integer slot, Event event, ICommandItemAbstract type) {
		if (event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
			if (ICommandLoader.arrayContains(type.getType(), "ATTACK")) {
				return ICommandLoader.isTargetContains(targetType, e.getEntityType().name()) 
						&& ICommandLoader.isTargetContains(targetName, e.getEntity().getName())
						&& ICommandLoader.isTargetContains(targetCause, e.getCause().name());
			}
			else if (ICommandLoader.arrayContains(type.getType(), "DAMAGED_ENTITY")) {
				LivingEntity attacker = EntityUtils.getDamager(e);
				if (attacker != null) {
					return ICommandLoader.isTargetContains(targetType, attacker.getType().name()) 
							&& ICommandLoader.isTargetContains(targetName, attacker.getName())
							&& ICommandLoader.isTargetContains(targetCause, e.getCause().name());
				}
			}
		}
		else if (event instanceof EntityDamageByBlockEvent) {
			EntityDamageByBlockEvent e = (EntityDamageByBlockEvent) event;
			if (ICommandLoader.arrayContains(type.getType(), "DAMAGED_BLOCK")) {
				return ICommandLoader.isTargetContains(targetCause, e.getCause().name())
						&& ICommandLoader.isTargetContains(targetBlock, getBlockFullName(e.getDamager()));
			}
		}
		else if (event instanceof EntityDamageEvent) {
			EntityDamageEvent e = (EntityDamageEvent) event;
			if (ICommandLoader.arrayContains(type.getType(), "DAMAGED_OTHER")) {
				return ICommandLoader.isTargetContains(targetCause, e.getCause().name());
			}
		}
		return true;
	}
	
	@Override
	public void onExecutePost(Player player, ItemStack item, Integer slot, Event event, ICommandItemAbstract type) {
		if (event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
			if (damage != null) {
				if (!ScriptHandler.isEnableScirptEngine()) {
					MsgUtils.warn("您的服务器没有 &4Nashorn &c驱动或已禁用, 无法使用 &4Damage &c功能", ICommandPlugin.getInst());
					return;
				}
				try {
					SimpleBindings simpleBindings = ScriptHandler.formatBindings(
							ICommandPlugin.getInst().getConfig().getConfigurationSection("TransmissionParameter.Damage"),
							new String[] {"Player", "Event", "Type", "ItemSlot", "ItemStack"}, 
							new Object[] {player, event, type, slot, item});
					e.setDamage(Double.valueOf(String.valueOf(damage.eval(simpleBindings))));
				} catch (Exception err) {
					MsgUtils.warn("类型 &4" + type.getId() + "&c 的 &4Damage&c 代码执行异常");
					return;
				}
			}
		}
	}
	
	@Override
	public HashMap<String, String> onPlaceholderHook(Player player, ItemStack item, Integer slot, Event event) {
		HashMap<String, String> map = new HashMap<>();
		if (event instanceof EntityDamageByEntityEvent) {
			map.put("$damage", String.valueOf(((EntityDamageByEntityEvent) event).getDamage()));
			map.put("$victim", ((EntityDamageByEntityEvent) event).getEntity().getName());
			Player attacker = GetDamager.get((EntityDamageByEntityEvent) event);
			if (attacker != null) {
				map.put("$attacker", attacker.getName());
			}
		}
		else if (event instanceof EntityDamageByBlockEvent) {
			map.put("$damage", String.valueOf(((EntityDamageByBlockEvent) event).getDamage()));
		}
		return map;
	}
	
	@SuppressWarnings("deprecation")
	private String[] getBlockFullName(Block block) {
		return new String[] { block.getType().name(), block.getType().name() + ":" + block.getData() };
	}
}
