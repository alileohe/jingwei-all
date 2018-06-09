package com.taobao.jingwei.monitor.core;

import com.taobao.jingwei.common.TaskUtil;
import com.taobao.jingwei.common.node.monitor.MonitorTaskNode;
import com.taobao.jingwei.monitor.listener.MonitorTaskScheduler;
import com.taobao.jingwei.monitor.util.MonitorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @desc 
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Aug 6, 2012 5:39:30 PM
 */

public class ScanRunningInstanceWorker extends AbstractScanWorker {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/** ������β����òŸ澯  */
	private final static int CONTINUE_COUNT = 3;

	private ThreadLocal<String> msg = new ThreadLocal<String>();

	public ScanRunningInstanceWorker(MonitorTaskScheduler scheduler, long tickTime) {
		super(scheduler, tickTime);
	}

	@Override
	protected long getConfigScanInterval() {
		return this.scheduler.getCurrentScanHeartBeatPeriod();
	}

	@Override
	protected AtomicLong getRunTimeTicker() {
		return this.scheduler.getRunningHeartBeatTicker();
	}

	@Override
	protected boolean findException(MonitorTaskNode monitorTaskNode, String hostName, List<String> hostNames) {
		logger.info("[jwm] check running count for " + taskName);
		// ʵ������ʵ������
		int realRinningSize = hostNames.size();
		int requiredRunningSize = this.scheduler.getMonitorTaskNode().getTaskInstanceCount();

		if (realRinningSize >= requiredRunningSize) {
			this.scheduler.getContinueCount().set(0);
			return false;
		}

		boolean isBuildinType = false;
		try {
			isBuildinType = TaskUtil.isBuildinTaskType(configManager, taskName);
		} catch (Exception e) {
			logger.error("[jwm] get task buildin/customer type error " + Arrays.deepToString(e.getStackTrace()));
			return false;
		}

		Set<String> set = new HashSet<String>();
		set.add(taskName);
		Collection<String> startHosts = MonitorUtil.taskStartHosts(configManager, set).get(taskName);

		if ((startHosts == null || startHosts.isEmpty()) && isBuildinType) {
			if (logger.isInfoEnabled()) {
				logger.info("[jw  monitor] all op is stop!");
			}
			return false;
		}

		String msg = "[jwm]" + taskName + " real: " + realRinningSize + ",required:" + requiredRunningSize;

		this.msg.set(msg);

		int newContinueCount = this.scheduler.getContinueCount().addAndGet(1);

		if (logger.isInfoEnabled()) {
			logger.info("[jwm] continue lack instance : " + newContinueCount + " for task " + taskName);
		}
		if (newContinueCount >= CONTINUE_COUNT) {

			this.scheduler.getContinueCount().set(0);
			return true;
		}

		return false;
	}

	@Override
	protected String getMsg() {
		String temp = this.msg.get();
		this.msg.remove();
		return temp;
	}

	@Override
	protected boolean isToggleOpened(MonitorTaskNode monitorTaskNode) {
		return this.scheduler.getCurrentHeartBeatToggle();
	}

	@Override
	protected boolean isBeyondFrozenTime(long current, MonitorTaskNode monitorTaskNode, String hostName) {
		return (current - this.scheduler.getLastInstanceAlarmTime()) > monitorTaskNode.getHeartBeatFrozenPeriod();
	}

	@Override
	protected void log(String msg) {
		logger.warn(msg);
	}

	@Override
	protected void flushLastAlarmTime(long tmsp, String hostName) {
		this.scheduler.setLastInstanceAlarmTime(tmsp);
	}
}
