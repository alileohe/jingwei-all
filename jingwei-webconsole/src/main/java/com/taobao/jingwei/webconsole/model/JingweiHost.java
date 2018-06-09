package com.taobao.jingwei.webconsole.model;

import com.taobao.jingwei.common.node.StatsNode;
import com.taobao.jingwei.common.node.StatusNode;

/**
 * 用于存放stats,status,alarm节点信息
 * 
 * @author qingren
 * 
 */
public class JingweiHost {
	private String name;

	// TODO:alarm节点未知
	private StatsNode stats;
	private StatusNode status;

	public StatsNode getStats() {
		return stats;
	}

	public void setStats(StatsNode stats) {
		this.stats = stats;
	}

	public StatusNode getStatus() {
		return status;
	}

	public void setStatus(StatusNode status) {
		this.status = status;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
