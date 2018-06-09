package com.taobao.jingwei.common.log;

import com.alibaba.common.lang.StringUtil;

/**
 * This class neumerate log types jingwei use.
 * 
 * @author <a href="mailto:shuohai.lhl@taobao.com">shuohailhl</a>
 * @date 2011-12-6����2:29:55
 */
public enum LogType {
	COMMIT("jingwei-commit"), //./logs, 

	STATS("jingwei-stats");// user.dir/logs

	private String typeName;

	private LogType(String typeName) {
		this.typeName = typeName;
	}

	public String getTypeName() {
		return typeName;
	}

	/**����typeString��ȡtask��enum����
	 * @param typeString
	 * @return
	 */
	public static LogType getLogTypeByTypeString(String typeString) {
		if (StringUtil.isBlank(typeString)) {
			return null;
		}
		LogType type = null;
		for (LogType typeEnum : LogType.values()) {
			if (StringUtil.equals(typeEnum.getTypeName(), typeString)) {
				type = typeEnum;
				break;
			}
		}
		return type;
	}
}