package com.taobao.jingwei.webconsole.model;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class JingweiAssembledServer implements Comparable<JingweiAssembledServer> {
	private String serverName;
	private List<JingweiAssembledServerTask> jingweiAssembledServerTasks;
	private String status;
	/** 如果有任务或者group在这个server上，该节点就不能删除 */
	private Boolean canDelete;

	private Map<String, TreeSet<String>> groups;

	private int executorCount;

	private int occupiedExecutorCount;

	/** server 的版本*/
	private String version;

	/** 运行server的用户 */
	private String userName;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getOccupiedExecutorCount() {
		return occupiedExecutorCount;
	}

	public void setOccupiedExecutorCount(int occupiedExecutorCount) {
		this.occupiedExecutorCount = occupiedExecutorCount;
	}

	public int getExecutorCount() {
		return executorCount;
	}

	public void setExecutorCount(int executorCount) {
		this.executorCount = executorCount;
	}

	public Boolean getCanDelete() {
		return canDelete;
	}

	public void setCanDelete(Boolean canDelete) {
		this.canDelete = canDelete;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<JingweiAssembledServerTask> getJingweiAssembledServerTasks() {
		return jingweiAssembledServerTasks;
	}

	public void setJingweiAssembledServerTasks(List<JingweiAssembledServerTask> jingweiAssembledServerTasks) {
		this.jingweiAssembledServerTasks = jingweiAssembledServerTasks;
	}

	public Map<String, TreeSet<String>> getGroups() {
		return groups;
	}

	public void setGroups(Map<String, TreeSet<String>> groups) {
		this.groups = groups;
	}

	@Override
	public int compareTo(JingweiAssembledServer o) {
		return this.serverName.compareTo(o.getServerName());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (this == obj) {
			return true;
		}

		if (!(obj instanceof JingweiAssembledServer)) {
			return false;
		}

		JingweiAssembledServer other = (JingweiAssembledServer) obj;

		if (other.getServerName() == null) {
			return false;
		}

		return this.serverName.equals(other.getServerName());
	}

	@Override
	public int hashCode() {
		return this.serverName.hashCode();
	}

}
