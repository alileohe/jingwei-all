package com.taobao.jingwei.monitor.core;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.config.ConfigManager;
import com.taobao.jingwei.monitor.listener.MonitorTaskScheduler;
import com.taobao.jingwei.monitor.util.MonitorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

abstract public class AbstractScanWorkerChain extends Thread {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	protected final MonitorTaskScheduler scheduler;

	protected final String taskName;

	protected final ConfigManager configManager;

	/** ����task������ */
	public ThreadLocal<List<String>> runningHostNames = new ThreadLocal<List<String>>();

	public AbstractScanWorkerChain(MonitorTaskScheduler monitorTaskScheduler) {
		this.scheduler = monitorTaskScheduler;
		this.taskName = this.scheduler.getMonitorTaskNode().getTaskName();
		this.configManager = this.scheduler.getConfigManager();
		this.setName(this.getClass().getName());
	}

	@Override
	public void run() {
		// ������õ�MonitorTaskNode����start״̬ ��ɨ��task����״̬
		if (!this.scheduler.getStart()) {
			if (logger.isInfoEnabled()) {
				logger.info("[jw monitor]" + taskName + " monitor toggle is stop!");
			}
			return;
		}

		this.check();
	}

	private void check() {
		String groupName = this.scheduler.getGroupName();
		if (StringUtil.isNotBlank(groupName)) {
			// ���������start״̬�򷵻�
			if (!MonitorUtil.isGroupTaskStartOp(configManager, groupName, taskName)) {
				return;
			}
		}

		List<String> runningHostList = MonitorUtil.getRunningHosts(scheduler.getConfigManager(), taskName);

		// ��������Ѿ����е������ڲ�ͬ�����ϵ�����ʵ����
		for (String hostName : runningHostList) {
			if (logger.isInfoEnabled()) {
				logger.info("[jwm]" + taskName + " running hosts " + runningHostList);
			}
			this.scan(hostName, runningHostList);
		}

		this.taskInstanceError(null, runningHostList);
	}

	abstract protected void scan(String hostName, List<String> hostNames);

	abstract protected void taskInstanceError(String hostName, List<String> hostNames);

	public MonitorTaskScheduler getScheduler() {
		return scheduler;
	}

	public String getTaskName() {
		return taskName;
	}

	public ConfigManager getConfigManager() {
		return configManager;
	}

	public ThreadLocal<List<String>> getRunningHostNames() {
		return runningHostNames;
	}

	public void setRunningHostNames(ThreadLocal<List<String>> runningHostNames) {
		this.runningHostNames = runningHostNames;
	}
}
