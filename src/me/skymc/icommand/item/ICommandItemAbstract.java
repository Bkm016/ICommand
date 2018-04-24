/**
 * 
 */
package me.skymc.icommand.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.script.CompiledScript;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import lombok.Getter;
import me.skymc.icommand.ICommandPlugin;
import me.skymc.icommand.script.ScriptHandler;
import me.skymc.taboolib.message.MsgUtils;

/**
 * @author Bkm016
 * @since 2018-03-21
 */
public abstract class ICommandItemAbstract {
	
	@Getter private int priority;
	@Getter private String id;
	@Getter private String lore;
	@Getter private String[] type;
	@Getter private List<String> commands;
	@Getter private boolean cancelled;
	// ����
	@Getter private boolean consume;
	@Getter private int amount;
	// ����
	@Getter private double change;
	@Getter private String changeMessage;
	// λ��
	@Getter private List<Integer> slots;
	@Getter private boolean heldItem;
	// ��ȴ
	@Getter private int cooldown;
	@Getter private String cooldownMessage;
	@Getter private List<String> cooldownCommands;
	// ����
	@Getter private CompiledScript require;
	@Getter private String requireMessage;
	@Getter private List<String> requireCommands;
	// �ű�
	@Getter private CompiledScript executeCode;
	@Getter private HashMap<String, Object> executeCodeParameter;
	// ������
	@Getter private boolean loaded;
	
	public ICommandItemAbstract(ConfigurationSection section) {
		try {
			id = section.getName();
			lore = section.contains("Lore") ? section.getString("Lore") : id;
			type = section.getString("Type").split("\\|");
			consume = section.getBoolean("Consume.Enable");
			amount = section.getInt("Consume.Amount");
			cancelled = section.getBoolean("Cancelled");
			cooldown = section.getInt("Cooldown") * 1000;
			priority = section.getInt("Priority");
			heldItem = section.contains("EnableHeld") ? section.getBoolean("EnableHeld") : true;
			slots = section.contains("EnableSlot") ? section.getIntegerList("EnableSlot") : new ArrayList<>();
			commands = section.contains("Commands") ? section.getStringList("Commands") : null;
			change = section.contains("Change") ? section.getDouble("Change") : 0D;
			changeMessage = section.contains("ChangeMessage") ? section.getString("ChangeMessage") : null;
			cooldownMessage = section.contains("CooldownMessage") ? section.getString("CooldownMessage") : null;
			cooldownCommands = section.contains("CooldownCommands") ? section.getStringList("CooldownCommands") : null;
			require = section.contains("Require") ? ScriptHandler.compile(section.getString("Require")) : null;
			requireMessage = section.contains("RequireMessage") ? section.getString("RequireMessage") : null;
			requireCommands = section.contains("RequireCommands") ? section.getStringList("RequireCommands") : null;
			if (section.contains("ExecuteCode")) {
				String args = section.getString("ExecuteCode").replace("\n", " ");
				String scrptName = ScriptHandler.getScriptNameInExecuteCode(args);
				if (scrptName == null) {
					executeCode = ScriptHandler.compile(section.getString("ExecuteCode"));
				} else if ((executeCode = ScriptHandler.getScripts().get(scrptName)) == null) {
					MsgUtils.warn("�ű��ļ� &4" + scrptName + " &c������", ICommandPlugin.getInst());
				} else {
					executeCodeParameter = ScriptHandler.getParametersInExecuteCode(args);
					if (ICommandPlugin.getInst().getConfig().getBoolean("DEBUG")) {
						MsgUtils.send("�ű��ļ� &f" + scrptName + " &7���� &f" + executeCodeParameter.size() + " &7�����", ICommandPlugin.getInst());
						for (Entry<String, Object> entry : executeCodeParameter.entrySet()) {
							MsgUtils.send(" - KEY: &f" + entry.getKey() + " &7VALUE: &f" + entry.getValue(), ICommandPlugin.getInst());
						}
					}
				}
			}
			loaded = true;
		} catch (Exception e) {
			MsgUtils.warn("��Ʒ����ʧ��: &4" + section.getName(), ICommandPlugin.getInst());
			MsgUtils.warn("ʧ��ԭ��: &4" + e.getMessage(), ICommandPlugin.getInst());
			loaded = false;
		}
	}
	
	public HashMap<String, String> onPlaceholderHook(Player player, ItemStack item, Integer slot, Event event) {
		return new HashMap<>();
	}
	
	public boolean onExecutePre(Player player, ItemStack item, Integer slot, Event event, ICommandItemAbstract type) {
		return true;
	}
	
	public void onExecutePost(Player player, ItemStack item, Integer slot, Event event, ICommandItemAbstract type) {}
}
