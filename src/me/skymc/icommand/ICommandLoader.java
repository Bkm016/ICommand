package me.skymc.icommand;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventPriority;

import me.skymc.icommand.item.ICommandItemAbstract;
import me.skymc.icommand.item.ICommandItemBase;
import me.skymc.taboolib.fileutils.ConfigUtils;
import me.skymc.taboolib.message.MsgUtils;

/**
 * @author Bkm016
 * @since 2018-03-21
 */
public class ICommandLoader {
	
	private static File itemFolder;
	private static List<ICommandItemAbstract> items = new ArrayList<>();
	private static HashMap<String[], Class<?>> types = new HashMap<>();
	private static HashMap<String, EventPriority> eventPriority = new HashMap<>();
	
	private ICommandLoader() {}
	
	/**
	 * 注册物品类型
	 * 
	 * @param name 名称
	 * @param clazz 地址
	 */
	public static void registerItemType(Class<?> clazz, String... name) {
		types.put(name, clazz);
	}
		
	/**
	 * 载入物品目录
	 */
	public static void createFolder() {
		itemFolder = new File(ICommandPlugin.getInst().getDataFolder(), "Items");
		if (!itemFolder.exists()) {
			ICommandPlugin.getInst().saveResource("Items/Example.yml", true);
		}
	}
	
	/**
	 * 获取指定物品实例
	 * 
	 * @param type 类型
	 * @return {@link List}
	 */
	public static List<ICommandItemAbstract> getItemTypes(String type) {
		List<ICommandItemAbstract> _items = new ArrayList<>();
		for (ICommandItemAbstract _item : items) {
			List<String> types = Arrays.asList(_item.getType());
			if (types.contains(type)) {
				_items.add(_item);
			}
		}
		return _items;
	}
	
	/**
	 * 载入监听器优先级
	 */
	public static void reloadPriority() {
		for (String type : ICommandPlugin.getInst().getConfig().getConfigurationSection("EventPriority").getKeys(false)) {
			try {
				eventPriority.put(type, EventPriority.valueOf(ICommandPlugin.getInst().getConfig().getString("EventPriority." + type)));
			} catch (Exception e) {
				eventPriority.put(type, EventPriority.NORMAL);
				MsgUtils.warn("监听器优先级识别异常: &4" + type, ICommandPlugin.getInst());
			}
		}
	}
	
	/**
	 * 载入所有物品
	 */
	public static void reloadItems() {
		long time = System.currentTimeMillis();
		// 清理物品
		items.clear();
		// 载入物品
		for (File file : itemFolder.listFiles()) {
			FileConfiguration conf = ConfigUtils.load(ICommandPlugin.getInst(), file);
			for (String name : conf.getConfigurationSection("").getKeys(false)) {
				// 是否禁用
				if (!name.startsWith("-")) {
					ICommandItemAbstract item = null;
					ConfigurationSection section = conf.getConfigurationSection(name);
					try {
						for (String[] typeNames : types.keySet()) {
							if (arrayContains(typeNames, section.getString("Type").split("\\|")[0])) {
								Class<?> type = types.get(typeNames);
								item = (ICommandItemAbstract) type.getConstructor(ConfigurationSection.class).newInstance(section);
							}
						}
					} catch (Exception e) {
						MsgUtils.warn("载入 &4" + name + " &c出现异常, 检查类型是否正确");
					}
					if (item == null) {
						item = new ICommandItemBase(section);
					}
					if (item.isLoaded()) {
						items.add(item);
					}
				}
			}
		}
		// 排序
		items.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
		// 提示
		MsgUtils.send("载入 &f" + items.size() + " &7项物品类型, 耗时: &f" + (System.currentTimeMillis() - time) + "ms", ICommandPlugin.getInst());
	}
	
	/**
	 * 获取监听器优先级
	 * 
	 * @param name 名称
	 * @return {@link EventPriority}
	 */
	public static EventPriority getEventPriority(String name) {
		if (eventPriority.containsKey(name)) {
			return eventPriority.get(name);
		}
		return EventPriority.NORMAL;
	}
	
	public static <T> boolean arrayContains(T[] arrays, Object element) {
		for (T obj : arrays) {
			if (obj.equals(element)) {
				return true;
			}
		}
		return false;
	}
	
	public static <T> boolean isTargetContains(List<T> targetList, T[] target) {
		if (targetList != null) {
			for (Object targetValue : targetList) {
				if (arrayContains(target, targetValue)) {
					return true;
				}	
			}
			return false;
		} else {
			return true;
		}
	}
	
	public static <T> boolean isTargetContains(List<T> targetList, T target) {
		if (targetList != null) {
			for (Object targetValue : targetList) {
				if (target.equals(targetValue)) {
					return true;
				}	
			}
			return false;
		} else {
			return true;
		}
	}
}
