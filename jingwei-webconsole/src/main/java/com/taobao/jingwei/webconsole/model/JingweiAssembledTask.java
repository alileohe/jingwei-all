/**
 * 
 */
package com.taobao.jingwei.webconsole.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.taobao.jingwei.common.node.AbstractNode;

/**
 * task展示信息封装类
 * 
 * @author qingren
 * 
 */
public class JingweiAssembledTask {
	private AbstractNode task;
	private List<JingweiHost> hosts;
	private String lastCommit;
	private boolean canModify;

	public AbstractNode getTask() {
		return task;
	}

	public void setTask(AbstractNode task) {
		this.task = task;
	}

	public List<JingweiHost> getHosts() {
		return hosts;
	}

	public void setHosts(List<JingweiHost> hosts) {
		this.hosts = hosts;
	}

	public String getLastCommit() {
		return lastCommit;
	}

	public void setLastCommit(String lastCommit) {
		this.lastCommit = lastCommit;
	}

	public void setLastCommit(long time) {
		Date date = new Date(time);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		this.lastCommit = sdf.format(date);
	}

	public boolean isCanModify() {
		return canModify;
	}

	public void setCanModify(boolean canModify) {
		this.canModify = canModify;
	}

}
