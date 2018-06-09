package com.taobao.jingwei.webconsole.model;

public class JingweiMonitorCriteria {
	private String taskName;
	private String monitorName;
	private String status;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTaskName() {
		return taskName;
	}

	public String getMonitorName() {
		return monitorName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public void setMonitorName(String monitorName) {
		this.monitorName = monitorName;
	}

}
