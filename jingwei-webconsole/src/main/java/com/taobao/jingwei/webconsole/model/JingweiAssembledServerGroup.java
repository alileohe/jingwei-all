package com.taobao.jingwei.webconsole.model;

import java.util.Map;
import java.util.TreeSet;

import com.taobao.jingwei.webconsole.model.JingweiAssembledGroup.GroupTaskInfo;

public class JingweiAssembledServerGroup implements Comparable<JingweiAssembledServerGroup> {
	/** server name */
	private final String serverName;

	/** server 的状态*/
	private String status;

	/** server 中有多少个group */
	private Map<String, TreeSet<GroupTaskInfo>> groups;

	public Map<String, TreeSet<GroupTaskInfo>> getGroups() {
		return groups;
	}

	public void setGroups(Map<String, TreeSet<GroupTaskInfo>> groups) {
		this.groups = groups;
	}

	public JingweiAssembledServerGroup(String serverName) {
		this.serverName = serverName;
	}

	public String getServerName() {
		return serverName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}


	@Override
	public int compareTo(JingweiAssembledServerGroup o) {
		return this.getServerName().compareTo(o.getServerName());
	}
}
