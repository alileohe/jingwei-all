package com.taobao.jingwei.monitor.util;

import java.util.Map;

/**
 * @desc 
 * 
 * @author Ë·º£ <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Jun 13, 2012 7:35:23 PM
 */

public class BothMap {
	private Map<String, String> taskData;
	private Map<String, String> groupData;

	public Map<String, String> getTaskData() {
		return taskData;
	}

	public void setTaskData(Map<String, String> taskData) {
		this.taskData = taskData;
	}

	public Map<String, String> getGroupData() {
		return groupData;
	}

	public void setGroupData(Map<String, String> groupData) {
		this.groupData = groupData;
	}
}
