package com.taobao.jingwei.webconsole.model;

import java.util.Set;

import jodd.util.StringUtil;

/**
 * 
 * @desc ҳ��չʾ��Ϣgroup��
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Mar 17, 2013 4:35:30 PM
 * 
 */
public class JingweiAssembledGroup implements Comparable<JingweiAssembledGroup> {

	/** ͬ�������Ƿ�֧�������޸� */
	private boolean supportBatchModify = false;

	/** group name */
	private String groupName;

	/** group ��������Щserver �Լ�server������״̬ ���Ժ���ܻ���server�İ汾 */
	private Set<GroupServerInfo> groupServerInfos;

	/** group ������Щ�������������״̬��RUNNING��STANDBY���������ĸ�server�� */
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
	 * [server����server������״̬]
	 * 
	 * @author shuohailhl
	 * 
	 */
	public static class GroupServerInfo implements Comparable<GroupServerInfo> {
		/** server name */
		private final String serverName;

		/** server ������״̬ */
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
	 * [����������������״̬���������е�server]
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

		/** ����״̬ */
		private String status;

		/** �������ĸ�server�� */
		private String serverName;

		/** opetate ����״̬ */
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
