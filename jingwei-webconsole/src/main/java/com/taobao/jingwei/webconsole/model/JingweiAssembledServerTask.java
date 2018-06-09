package com.taobao.jingwei.webconsole.model;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.node.OperateNode.OperateEnum;
import com.taobao.jingwei.common.node.server.ServerTaskNode.TaskTypeEnum;

public class JingweiAssembledServerTask implements
		Comparable<JingweiAssembledServerTask> {
	// E.g /jingwei/servers/**server
	private String serverName;

	private String taskName;

	private String taskType;

	private String pluginTaskTargetState;

	private String pluginTaskWorkState;

	// E.g /jingwei/servers/**server/tasks/task/operate
	private String operate;

	// E.g. /jingwei/tasks/**task/hosts/**host/status
	private String status;

	private static final String OP_STOP = "STOP";

	private static final String OP_START = "START";
	
	private String group;
	
	public static final String GROUP_TYPE = "GROUP";

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getTaskType() {
		return taskType;
	}

	public void setTaskType(String taskType) {
		this.taskType = taskType;
	}

	public String getPluginTaskTargetState() {
		if (TaskTypeEnum.BUILDIN.toString()
				.equalsIgnoreCase(this.getTaskType())) {
			return StringUtil.EMPTY_STRING;
		}

		return pluginTaskTargetState;
	}

	public void setPluginTaskTargetState(String pluginTaskTargetState) {
		this.pluginTaskTargetState = pluginTaskTargetState;
	}

	public String getPluginTaskWorkState() {
		if (TaskTypeEnum.BUILDIN.toString()
				.equalsIgnoreCase(this.getTaskType())) {
			return StringUtil.EMPTY_STRING;
		}

		return pluginTaskWorkState;
	}

	public void setPluginTaskWorkState(String pluginTaskWorkState) {
		this.pluginTaskWorkState = pluginTaskWorkState;
	}

	public String getOperate() {
		if (StringUtil.equalsIgnoreCase(operate,
				OperateEnum.NODE_START.getOperateString())) {
			return OP_START;
		} else {
			return OP_STOP;
		}
	}
	
	public void setOperate(String operate) {
		this.operate = operate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public int compareTo(JingweiAssembledServerTask o) {
		return this.taskName.compareTo(o.getTaskName());
	}
	
	@Override
	public boolean equals(Object obj){
		if (obj == null) {
			return false;
		}
		
		if (this == obj) {
			return true;
		}
		
		if (!(obj instanceof JingweiAssembledServerTask)) {
			return false;
		}
		
		JingweiAssembledServerTask other =(JingweiAssembledServerTask)obj;
		
		if (other.getTaskName() == null) {
			return false;
		}
		
		return this.taskName.equals(other.getServerName());
	}
	
	@Override
	public int hashCode(){
		return this.taskName.hashCode();
	}
	
	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

}
