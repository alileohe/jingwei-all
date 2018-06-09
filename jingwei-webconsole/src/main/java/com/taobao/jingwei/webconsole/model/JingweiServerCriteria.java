package com.taobao.jingwei.webconsole.model;

public class JingweiServerCriteria {

	private String taskName;
	private String serverName;
	private String taskStatus;
	private String serverStatus;
	private String groupType;
	private String taskType;

	public String getTaskType() {
		return taskType;
	}

	public void setTaskType(String taskType) {
		this.taskType = taskType;
	}

	public String getGroupType() {
		return groupType;
	}

	public void setGroupType(String groupType) {
		this.groupType = groupType;
	}

	public String getTaskStatus() {
		return taskStatus;
	}

	public String getServerStatus() {
		return serverStatus;
	}

	public void setTaskStatus(String taskStatus) {
		this.taskStatus = taskStatus;
	}

	public void setServerStatus(String serverStatus) {
		this.serverStatus = serverStatus;
	}

	public String getTaskName() {
		return taskName;
	}

	public String getServerName() {
		return serverName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
}
