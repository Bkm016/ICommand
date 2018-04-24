package me.skymc.icommand.script;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

import org.bukkit.configuration.ConfigurationSection;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import lombok.Getter;
import me.skymc.icommand.ICommandPlugin;
import me.skymc.taboolib.message.MsgUtils;

/**
 * @author Bkm016
 * @since 2018-03-24
 */
public class ScriptHandler {
	
	@Getter private static boolean enableScirptEngine;
	@Getter private static ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
	@Getter private static ScriptEngine scriptEngine;
	@Getter private static HashMap<String, CompiledScript> scripts = new HashMap<>();
	@Getter private static Pattern parameterPattern = Pattern.compile("-parameter:(\\S+)\\((\\S+):(\\S+)\\)");
	
	public static void main(String[] args) {
		String value = "-parameter:icommand(me.skymc.icommand.ICommand)";
		Matcher matcher = parameterPattern.matcher(value);
		if (matcher.find()) {
			System.out.println(matcher.group(1));
			System.out.println(matcher.group(2));
		} else {
			System.err.println("error");
		}
	}
	
	private ScriptHandler() {}
	
	public static void init() {
		if (enableScirptEngine = ICommandPlugin.getInst().getConfig().getBoolean("Settings.EnableNashorn")) {
	        NashornScriptEngineFactory factory = null;
	        for (ScriptEngineFactory factorie : scriptEngineManager.getEngineFactories()) {
	            if (factorie.getEngineName().equalsIgnoreCase("Oracle Nashorn")) {
	                factory = (NashornScriptEngineFactory) factorie;
	                break;
	            }
	        }
	        scriptEngine = Objects.requireNonNull(factory).getScriptEngine(new String[] {"-doe", "--global-per-engine"});
	        MsgUtils.send("已启用 &fNashorn &7驱动, 脚本功能开启.", ICommandPlugin.getInst());
		} else {
	        MsgUtils.send("已禁用 &fNashorn &7驱动, 脚本功能关闭.", ICommandPlugin.getInst());
		}
	}
	
	public static void reloadScripts() {
		File file = new File(ICommandPlugin.getInst().getDataFolder(), "Scripts");
		if (!file.exists()) {
			file.mkdirs();
		}
		scripts.clear();
		long time = System.currentTimeMillis();
		for (File scriptFile : file.listFiles()) {
			if (scriptFile.getName().endsWith(".js")) {
				CompiledScript script = compile(scriptFile);
				if (script != null) {
					scripts.put(scriptFile.getName().split("\\.")[0], script);
				}
			}
		}
		MsgUtils.send("载入 &f" + scripts.size() + " &7项脚本文件, 耗时: &f" + (System.currentTimeMillis() - time) + "ms", ICommandPlugin.getInst());
	}
	
	/**
	 * 编译表达式
	 * 
	 * @param script 表达式
	 * @return {@link CompiledScript}
	 */
	public static CompiledScript compile(String script) {
		try {
			Compilable Compilable = (Compilable) scriptEngine;
			return Compilable.compile(script);
		} catch (Exception e) {
			MsgUtils.warn("表达式 &4" + script + " &c编译失败", ICommandPlugin.getInst());
			return null;
		}
	}
	
	/**
	 * 编译脚本文件
	 * 
	 * @param scriptFile 文件
	 * @return {@link CompiledScript}
	 */
	public static CompiledScript compile(File scriptFile) {
		try {
			Compilable Compilable = (Compilable) scriptEngine;
			return Compilable.compile(new InputStreamReader(new FileInputStream(scriptFile), Charset.forName("UTF-8")));
		} catch (Exception e) {
			MsgUtils.warn("脚本 &4" + scriptFile.getName() + " &c编译失败", ICommandPlugin.getInst());
			return null;
		}
	}
	
	public static SimpleBindings formatBindings(ConfigurationSection section, String[] keys, Object[] values) {
		SimpleBindings simpleBindings = new SimpleBindings();
		for (int i = 0; i < keys.length; i++) {
			if (section.getBoolean(keys[i] + ".Enable")) {
				try {	
					simpleBindings.put(section.getString(keys[i] + ".Key"), values[i]);
				} catch (Exception e) {
					MsgUtils.warn("参数传递失败: &4" + keys[i]);
				}
			}
		}
		return simpleBindings;
	}
	
	public static String getScriptNameInExecuteCode(String script) {
		return script.contains("-script:") ? script.split("-script:")[1].split(" ")[0] : null;
	}
	
	public static HashMap<String, Object> getParametersInExecuteCode(String script) {
		HashMap<String, Object> parameters = new HashMap<>();
		for (String args : script.split(" ")) {
			Matcher matcher = parameterPattern.matcher(args);
			if (matcher.find()) {
				try {
					Object parameter = null;
					if (matcher.group(2).equals("Class")) {
						parameter = Class.forName(matcher.group(3));
					} else if (matcher.group(2).equals("String")) {
						parameter = String.valueOf(matcher.group(3));
					} else {
						Class<?> type = Class.forName(matcher.group(2));
						Method valueOfMethod = type.getMethod("valueOf", String.class);
						parameter = valueOfMethod.invoke(null, matcher.group(3));
					}
					parameters.put(matcher.group(1), parameter);
				} catch (Exception e) {
					MsgUtils.warn("参数解析错误: &4" + args);
					e.printStackTrace();
				}
			}
		}
		return parameters;
	}
}
