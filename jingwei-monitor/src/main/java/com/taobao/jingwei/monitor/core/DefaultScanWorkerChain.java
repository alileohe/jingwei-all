package com.taobao.jingwei.monitor.core;

import com.taobao.jingwei.monitor.listener.MonitorTaskScheduler;
import com.taobao.jingwei.monitor.util.MonitorConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @desc 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * @date Aug 1, 2012 4:38:24 PM
 */

public class DefaultScanWorkerChain extends AbstractScanWorkerChain {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private final ScanTaskHeartBeatWorker scanTaskHeartBeatWorker;

	private final ScanAlarmWorker scanAlarmWorker;

	private final BinlogPositionScannWorker binlogPositionScanner;

	private final ScanTaskStatsWorker scanTaskStatsWorker;

	private final ScanRunningInstanceWorker scanRunningInstanceWorker;

	public DefaultScanWorkerChain(MonitorTaskScheduler monitorTaskScheduler) {
		this(monitorTaskScheduler, MonitorConst.DEFAULT_TICK_TIME);
	}

	public DefaultScanWorkerChain(MonitorTaskScheduler monitorTaskScheduler, long tickTime) {
		super(monitorTaskScheduler);
		this.scanTaskHeartBeatWorker = new ScanTaskHeartBeatWorker(monitorTaskScheduler, tickTime);
		this.scanAlarmWorker = new ScanAlarmWorker(monitorTaskScheduler, tickTime);
		this.binlogPositionScanner = new BinlogPositionScannWorker(monitorTaskScheduler, tickTime);
		this.scanTaskStatsWorker = new ScanTaskStatsWorker(monitorTaskScheduler, tickTime);
		this.scanRunningInstanceWorker = new ScanRunningInstanceWorker(monitorTaskScheduler, tickTime);
	}

	public ScanTaskHeartBeatWorker getScanTaskHeartBeatWorker() {
		return scanTaskHeartBeatWorker;
	}

	@Override
	protected void scan(String hostName, List<String> hostNames) {
		// �������
		this.scanTaskHeartBeatWorker.check(hostName, hostNames);

		// ���澯
		this.scanAlarmWorker.check(hostName, hostNames);

		// ���λ���ӳ�
		this.binlogPositionScanner.check(hostName, hostNames);

		// ���ҵ��澯
		this.scanTaskStatsWorker.check(hostName, hostNames);
	}

	@Override
	protected void taskInstanceError(String hostName, List<String> runningHostList) {
		if (logger.isInfoEnabled()) {
			logger.info("[jwm] start scan if running instance error " + " for task : " + taskName);
		}
		this.scanRunningInstanceWorker.check(hostName, runningHostList);
	}
}
