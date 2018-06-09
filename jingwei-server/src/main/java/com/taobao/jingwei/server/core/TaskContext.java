package com.taobao.jingwei.server.core;

import com.taobao.jingwei.common.config.ConfigDataListener;

/**
 * @desc task���������ģ���ǰ����op���Ժ���ܰ���status
 * 
 * @author <a href="mailto:shuohai.lhl@taobao.com">˷�� shuohailhl</a>
 * 
 * @date 2011-12-7����4:18:30
 */
public class TaskContext {
	private final String taskName;

	private final ConfigDataListener taskOperateListener;

	public TaskContext(String taskName, ConfigDataListener opListener) {
		this.taskName = taskName;
		this.taskOperateListener = opListener;
	}

	public ConfigDataListener getTaskOperateListener() {
		return taskOperateListener;
	}

	public String getTaskName() {
		return taskName;
	}

}
