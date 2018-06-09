package com.taobao.jingwei.server.util;

import java.util.HashSet;
import java.util.Set;

/**
 * 保存bulidin和target类的任务名
 * 
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com">shuohailhl</a>
 * 
 * @date 2011-12-31下午3:51:14
 */
public class BothTaskSet {
	/** 内置任务 */
	private final Set<String> buildinTasks = new HashSet<String>();

	/** 定制任务 */
	private final Set<String> customerTasks = new HashSet<String>();

	public void addBuildinTask(String taskName) {
		this.buildinTasks.add(taskName);
	}

	public void addCustomerTask(String taskName) {
		this.customerTasks.add(taskName);
	}

	public Set<String> getBuildinTasks() {
		return buildinTasks;
	}

	public Set<String> getCustomerTasks() {
		return customerTasks;
	}

}
