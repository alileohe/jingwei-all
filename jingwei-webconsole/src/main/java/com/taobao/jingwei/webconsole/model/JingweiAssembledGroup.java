package com.taobao.jingwei.webconsole.model;

import java.util.Set;

import jodd.util.StringUtil;

/**
 * 
 * @desc 页面展示信息group的
 * 
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Mar 17, 2013 4:35:30 PM
 * 
 */
public class JingweiAssembledGroup implements Comparable<JingweiAssembledGroup> {

	/** 同组任务是否支持批量修改 */
	private boolean supportBatchModify = false;

	/** group name */
	private String groupName;

	/** group 分配了那些server 以及server的运行状态 ，以后可能会有server的版本 */
	private Set<GroupServerInfo> groupServerInfos;

	/** group 含有那些任务，任务的运行状态，RUNNING和STANDBY的任务在哪个server上 */
	private Set<GroupTaskInfo> groupTaskInfos;

	public String getGroupName() {
		return groupName;
	}

	public Set<GroupServerInfo> getGroupServerInfos() {
		return groupServerInfos;
	}

	public Set<GroupTaskInfo> getGroupTaskInfos() {
		return groupTaskInfos;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public void setGroupServerInfos(Set<GroupServerInfo> groupServerInfos) {
		this.groupServerInfos = groupServerInfos;
	}

	public void setGroupTaskInfos(Set<GroupTaskInfo> groupTaskInfos) {
		this.groupTaskInfos = groupTaskInfos;
	}

	public boolean isSupportBatchModify() {
		return supportBatchModify;
	}

	public void setSupportBatchModify(boolean supportBatchModify) {
		this.supportBatchModify = supportBatchModify;
	}

	/**
	 * [server名，server的运行状态]
	 * 
	 * @author shuohailhl
	 * 
	 */
	public static class GroupServerInfo implements Comparable<GroupServerInfo> {
		/** server name */
		private final String serverName;

		/** server 的运行状态 */
		private String status;

		public GroupServerInfo(String serverName) {
			this.serverName = serverName;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getServerName() {
			return serverName;
		}

		public String getStatus() {
			return status;
		}

		@Override
		public int compareTo(GroupServerInfo o) {

			return this.serverName.compareTo(o.getServerName());
		}
	}

	/**
	 * [任务名，任务运行状态，任务运行的server]
	 * 
	 * @author shuohailhl
	 * 
	 */
	public static class GroupTaskInfo implements Comparable<GroupTaskInfo> {
		/** task name */
		private String taskName;

		public void setTaskName(String taskName) {
			this.taskName = taskName;
		}

		/** 运行状态 */
		private String status;

		/** 运行在哪个server上 */
		private String serverName;

		/** opetate 操作状态 */
		private String operate;

		public String getOperate() {
			return operate;
		}

		public void setOperate(String operate) {
			this.operate = operate;
		}

		public GroupTaskInfo(String taskName) {
			this.taskName = taskName;
		}

		public String getTaskName() {
			return taskName;
		}

		public String getStatus() {
			return status;
		}

		public String getServerName() {
			return serverName;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public void setServerName(String serverName) {
			this.serverName = serverName;
		}

		@Override
		public int compareTo(GroupTaskInfo o) {
			if (this.getTaskName().equals(o.getTaskName())) {
				if (StringUtil.isNotBlank(this.serverName) && StringUtil.isNotBlank(o.getServerName())
						&& this.getServerName().equals(o.getServerName())) {
					return 0;
				} else {
					return -1;
				}

			} else {
				return -1;
			}
		}

	}

	@Override
	public int compareTo(JingweiAssembledGroup o) {
		return this.groupName.compareTo(o.getGroupName());
	}
}
