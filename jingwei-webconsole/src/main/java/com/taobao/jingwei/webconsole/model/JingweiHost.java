package com.taobao.jingwei.webconsole.model;

import com.taobao.jingwei.common.node.StatsNode;
import com.taobao.jingwei.common.node.StatusNode;

/**
 * ���ڴ��stats,status,alarm�ڵ���Ϣ
 * 
 * @author qingren
 * 
 */
public class JingweiHost {
	private String name;

	// TODO:alarm�ڵ�δ֪
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
