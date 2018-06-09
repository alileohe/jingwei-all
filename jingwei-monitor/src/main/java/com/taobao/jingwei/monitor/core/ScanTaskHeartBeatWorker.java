package com.taobao.jingwei.monitor.core;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.node.HeartbeatNode;
import com.taobao.jingwei.common.node.monitor.MonitorTaskNode;
import com.taobao.jingwei.monitor.listener.MonitorTaskScheduler;
import com.taobao.jingwei.monitor.util.MonitorUtil;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @desc ������ɨ��/jingwei/tasks/**task/**host/heartBeat�ڵ����ݣ�û������������ͨ����������
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date May 22, 2012 11:47:10 AM
 */

public class ScanTaskHeartBeatWorker extends AbstractScanWorker {
	private static Logger logger = LoggerFactory.getLogger(ScanTaskHeartBeatWorker.class);

	private SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/** ����5�� ����⵽û��heart beat�Ÿ澯*/
	private static final int HEART_BEAT_ALARM_LIMIT = 5;

	private final ConcurrentHashMap<String, RunningHeartCount> heartMemory;

	private ThreadLocal<RunningHeartCount> count = new ThreadLocal<RunningHeartCount>();

	private ThreadLocal<String> msg = new ThreadLocal<String>();

	public ScanTaskHeartBeatWorker(MonitorTaskScheduler scheduler, long tickTime) {
		super(scheduler, tickTime);
		this.heartMemory = scheduler.getHeartMemory();
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

		RunningHeartCount heatCount = this.heartMemory.get(hostName);

		this.count.set(heatCount);

		logger.warn("[jw monitor] start scan task heart beat " + taskName + " at host " + hostName);

		// ��ȡ��������
		String heartBeatDataIdOrPath = MonitorUtil.getHeartBeatNodePath(taskName, hostName);

		// �����ڵ�
		String heartBeatData = this.configManager.getData(heartBeatDataIdOrPath);

		if (StringUtil.isBlank(heartBeatData)) {
			logger.warn("[jingwei monitor]task: " + taskName + ", host: " + hostName + ", heart beat data is blank : ");
			return false;
		}

		HeartbeatNode heartBeatNode = new HeartbeatNode();
		try {
			heartBeatNode.jsonStringToNodeSelf(heartBeatData);
		} catch (JSONException e) {
			logger.error("[jingwei monitor] check heart beat for task: " + taskName + ", host: " + hostName
					+ ", heart beat data to json string error : " + heartBeatDataIdOrPath, e);
			return false;
		}

		long newHeartBeartTime = heartBeatNode.getTimestamp().getTime();

		// --��ʱ
		heartMemory.putIfAbsent(hostName, new RunningHeartCount());

		// ��һ�γ�ʼ��LastHeartBeatTime
		RunningHeartCount tt = heartMemory.get(hostName);

		if (tt == null ){
			return false;
		}
		Long lastHeartBeatTime = tt.getLastHeartBeatTime();
		if (lastHeartBeatTime == null) {
			return false;
		}
		if ( lastHeartBeatTime == 0L) {
			tt.setLastHeartBeatTime(newHeartBeartTime);
			return false;
		}

		long now = System.currentTimeMillis();
		String newDate = DATE_FORMAT.format(new Date(now));
		String lastDate = DATE_FORMAT.format(new Date(lastHeartBeatTime));

		logger.warn("[jwm] task : " + taskName + " heart beat last : " + lastDate + ", current : " + newDate);

		if (newHeartBeartTime == lastHeartBeatTime) {
			String msg = "[jwm]" + taskName + "," + hostName + ",heartbeat not change:" + lastDate;

			int heartBeartContinueCnt = heatCount.getHeartBeartContinueCnt().get();
			logger.warn(msg + "; not heart beat continue count : " + heartBeartContinueCnt);

			if (heartBeartContinueCnt < HEART_BEAT_ALARM_LIMIT) {
				heatCount.getHeartBeartContinueCnt().getAndIncrement();
			} else {
				heatCount.getHeartBeartContinueCnt().set(0);
				this.msg.set(msg);
				return true;
			}
		} else {
			heatCount.getHeartBeartContinueCnt().set(0);
		}

		heatCount.setLastHeartBeatTime(newHeartBeartTime);

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
		return scheduler.getCurrentHeartBeatToggle();
	}

	@Override
	protected boolean isBeyondFrozenTime(long current, MonitorTaskNode monitorTaskNode, String hostName) {
		long lastAlarmHeartBeatTime = this.count.get().getLastAlarmHeartBeatTime();
		if (current - lastAlarmHeartBeatTime > monitorTaskNode.getHeartBeatFrozenPeriod()) {
			return true;
		}
		return false;
	}

	@Override
	protected void log(String msg) {
		logger.warn(msg);
	}

	@Override
	protected void flushLastAlarmTime(long tmsp, String hostName) {
		this.count.get().setLastAlarmHeartBeatTime(tmsp);
	}
}
