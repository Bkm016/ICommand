package me.skymc.icommand;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import lombok.Getter;
import me.skymc.icommand.command.CommandHandler;
import me.skymc.icommand.item.ICommandItemDamage;
import me.skymc.icommand.item.ICommandItemInteract;
import me.skymc.icommand.item.ICommandItemPlayer;
import me.skymc.icommand.listener.ListenerPlayer;
import me.skymc.icommand.listener.ListenerPlayer_v1_9;
import me.skymc.icommand.script.ScriptHandler;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.fileutils.ConfigUtils;
import me.skymc.taboolib.message.MsgUtils;
import me.skymc.taboolib.string.language2.Language2;

/**
 * @author Bkm016
 * @since 2018-03-21
 */
public class ICommandPlugin extends JavaPlugin {
	
	@Getter private static Plugin inst;
	@Getter private static Language2 language;
	@Getter private static boolean isPlaceholderAPI;
	@Getter private FileConfiguration config;
	
	@Override
	public void reloadConfig() {
		config = ConfigUtils.saveDefaultConfig(this, "config.yml");
		try {
			language = new Language2(config.getString("Settings.Language"), this);
		} catch (Exception e) {
			language = new Language2(this);
		}
	}
	
	@Override
	public void onEnable() {
		inst = this;
		reloadConfig();
		
		Bukkit.getPluginCommand("icommand").setExecutor(new CommandHandler());
		
		ICommandLoader.registerItemType(ICommandItemDamage.class, "ATTACK", "DAMAGED_OTHER", "DAMAGED_ENTITY", "DAMAGED_BLOCK");
		ICommandLoader.registerItemType(ICommandItemInteract.class, "RIGHT_CLICK_AIR", "RIGHT_CLICK_BLOCK", "LEFT_CLICK_BLOCK", "PHYSICAL", "RIGHT_CLICK_ENTITY", "BLOCK_BREAK", "BLOCK_PLACE");
		ICommandLoader.registerItemType(ICommandItemPlayer.class, "PLAYER_TELEPORT", "PLAYER_CHAT", "PLAYER_SNEAK", "PLAYER_FISH");
		ICommandLoader.createFolder();
		ICommandLoader.reloadPriority();
		
		registerListener();
		ScriptHandler.init();
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				ScriptHandler.reloadScripts();
				ICommandLoader.reloadItems();
				isPlaceholderAPI = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
			}
		}.runTask(this);
	}
	
	@Override
	public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);
		HandlerList.unregisterAll(this);
	}
	
	@SuppressWarnings("unchecked")
	private void registerListener() {
		List<Listener> listeners = new ArrayList<>();
		listeners.add(new ListenerPlayer());
		if (TabooLib.getVerint() > 10800) {
			listeners.add(new ListenerPlayer_v1_9());
		}
		for (Listener listener : listeners) {
			for (Method method : listener.getClass().getMethods()) {
				if (method.isAnnotationPresent(EventHandler.class)) {
					EventHandler eventHandler = method.getAnnotation(EventHandler.class);
					try {
						InvocationHandler invocationHandler = Proxy.getInvocationHandler(eventHandler);
						Field declaredField = invocationHandler.getClass().getDeclaredField("memberValues"); 
						declaredField.setAccessible(true);
						Map<String, Object> memberValues = (Map<String, Object>) declaredField.get(invocationHandler);
						memberValues.put("priority", ICommandLoader.getEventPriority(method.getName()));
						if (getConfig().getBoolean("DEBUG")) {
							MsgUtils.send("监听器 &f" + method.getName() + "&7 当前优先级: &f" + eventHandler.priority(), this);
						}
					} catch (Exception e) {
						MsgUtils.warn("监听器优先级更改失败: &4" + e.getMessage(), this);
					}
				}
			}	
			Bukkit.getPluginManager().registerEvents(listener, this);
		}
	}
}
