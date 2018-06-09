package com.taobao.jingwei.server.util;

import java.util.HashSet;
import java.util.Set;

/**
 * ����bulidin��target���������
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com">shuohailhl</a>
 * 
 * @date 2011-12-31����3:51:14
 */
public class BothTaskSet {
	/** �������� */
	private final Set<String> buildinTasks = new HashSet<String>();

	/** �������� */
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
