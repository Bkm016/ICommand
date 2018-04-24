package me.skymc.icommand.util;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Bkm016
 * @since 2018-03-22
 */
public class TimeUtils {

	/**
	 * 获取详细时间
	 * 
	 * @param seconds 秒
	 * @return {@link HashMap}
	 */
	public static HashMap<String, Long> formatTime(long seconds) {
	    HashMap<String, Long> map = new HashMap<>();
	    map.put("day", TimeUnit.SECONDS.toDays(seconds));
	    map.put("hour", TimeUnit.SECONDS.toHours(seconds) - map.get("day") * 24L);
	    map.put("minute", TimeUnit.SECONDS.toMinutes(seconds) - TimeUnit.SECONDS.toHours(seconds) * 60L);
	    map.put("second", TimeUnit.SECONDS.toSeconds(seconds) - TimeUnit.SECONDS.toMinutes(seconds) * 60L);
	    return map;
	}
}
