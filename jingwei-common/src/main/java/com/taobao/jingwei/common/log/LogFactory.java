package com.taobao.jingwei.common.log;

import org.apache.commons.logging.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author <a href="mailto:shuohai.lhl@taobao.com">shuohailhl</a>
 * 
 * @date 2011-12-12����6:29:30
 */
public class LogFactory {

	/** key : LogType; value : Log according to the key type */
	private final static Map<LogType, Log> logs = new HashMap<LogType, Log>(LogType.values().length);

	/** key : LogType; value : Log configration according to the key type */
	private final static Map<LogType, JingweiLogConfig> configs = new HashMap<LogType, JingweiLogConfig>(
			LogType.values().length);

	public static Log getLog(LogType type, String name) {

		if (logs.get(type) == null) {
			logs.put(type, new JingweiLogImpl(type, configs.get(type), name));
		}

		return logs.get(type);
	}

	public static void setCommitLogConfig(JingweiLogConfig logConfig) {
		configs.put(LogType.COMMIT, logConfig);
	}

	public static void setStatsLogConfig(JingweiLogConfig logConfig) {
		configs.put(LogType.STATS, logConfig);
	}
}