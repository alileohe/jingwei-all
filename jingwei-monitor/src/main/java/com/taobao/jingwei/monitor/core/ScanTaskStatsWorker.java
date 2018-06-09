package com.taobao.jingwei.monitor.core;

import com.taobao.jingwei.common.node.monitor.MonitorTaskNode;
import com.taobao.jingwei.monitor.listener.MonitorTaskScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @desc ������ɨ��/jingwei/tasks/**task/**host/stats�ڵ�����,������ֵ�澯
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date May 22, 2012 11:21:31 AM
 */

public class ScanTaskStatsWorker extends AbstractScanWorker {
	private static Logger logger = LoggerFactory.getLogger(ScanTaskStatsWorker.class);

	public ScanTaskStatsWorker(MonitorTaskScheduler scheduler, long tickTime) {
		super(scheduler, tickTime);
	}

	@Override
	protected long getConfigScanInterval() {
		return this.getScheduler().getCurrentScanStatsPeriod();
	}

	@Override
	protected AtomicLong getRunTimeTicker() {
		return this.getScheduler().getStatsTicker();
	}

	@Override
	protected boolean findException(MonitorTaskNode monitorTaskNode, String hostName, List<String> hostNames) {
		logger.info("[jwm] check stats for " + taskName);
		this.getScheduler().getStatsShesholdChecker().checkStatsForAlert(monitorTaskNode, taskName, hostName);
		return false;
	}

	@Override
	protected String getMsg() {
		return null;
	}

	@Override
	protected boolean isToggleOpened(MonitorTaskNode monitorTaskNode) {
		return false;
	}

	@Override
	protected boolean isBeyondFrozenTime(long current, MonitorTaskNode monitorTaskNode, String hostName) {
		return false;
	}

	@Override
	protected void log(String msg) {

	}

	@Override
	protected void flushLastAlarmTime(long tmsp, String hostName) {

	}

}