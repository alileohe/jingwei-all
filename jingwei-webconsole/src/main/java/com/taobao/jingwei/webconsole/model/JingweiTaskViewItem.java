package com.taobao.jingwei.webconsole.model;

import java.util.List;

public class JingweiTaskViewItem implements Comparable<JingweiTaskViewItem> {
	// ÈÎÎñÃû
	private String taskName;

	private List<JingweiAssembledServerTask> jingweiAssembledServerTasks;

	@Override
	public int compareTo(JingweiTaskViewItem o) {
		return this.taskName.compareTo(o.getTaskName());
	}

	public String getTaskName() {
		return taskName;
	}

	public List<JingweiAssembledServerTask> getJingweiAssembledServerTasks() {
		return jingweiAssembledServerTasks;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public void setJingweiAssembledServerTasks(
			List<JingweiAssembledServerTask> jingweiAssembledServerTasks) {
		this.jingweiAssembledServerTasks = jingweiAssembledServerTasks;
	}

}
