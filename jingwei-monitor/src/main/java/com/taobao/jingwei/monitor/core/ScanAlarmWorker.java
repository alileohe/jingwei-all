package com.taobao.jingwei.monitor.core;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.node.AlarmNode;
import com.taobao.jingwei.common.node.monitor.MonitorTaskNode;
import com.taobao.jingwei.monitor.listener.MonitorTaskScheduler;
import com.taobao.jingwei.monitor.util.MonitorUtil;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @desc ������ɨ��/jingwei/tasks/**task/**host/alarm�ڵ㣬��ȡ��������1 �澯 2 ��ոýڵ������
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date May 22, 2012 11:27:45 AM
 */

public class ScanAlarmWorker extends AbstractScanWorker {
	private static Logger logger = LoggerFactory.getLogger(ScanAlarmWorker.class);

	/** �澯��Ϣ�ضϳ��� */
	private final static int ALARM_LENGTH = 140;

	private final ConcurrentHashMap<String, Long> lastAlarmTime;

	private ThreadLocal<String> msg = new ThreadLocal<String>();

	public ScanAlarmWorker(MonitorTaskScheduler scheduler, long tickTime) {
		super(scheduler, tickTime);
		this.lastAlarmTime = scheduler.getLastAlarmTime();
	}

	public ConcurrentHashMap<String, Long> getLastAlarmTime() {
		return lastAlarmTime;
	}

	@Override
	protected long getConfigScanInterval() {
		return this.scheduler.getCurrentScanAlarmPeriod();
	}

	@Override
	protected AtomicLong getRunTimeTicker() {
		return this.scheduler.getAlarmTicker();
	}

	@Override
	protected boolean findException(MonitorTaskNode monitorTaskNode, String hostName, List<String> hostNames) {
		this.lastAlarmTime.putIfAbsent(hostName, 0L);
		logger.warn("[jwm] task : " + taskName + "\t" + hostName + " check alarm!");

		String alarmDataIdOrPath = MonitorUtil.getAlarmIdOrPath(taskName, hostName);
		String alarmData = this.configManager.getData(alarmDataIdOrPath);

		if (StringUtil.isBlank(alarmData)) {
			if (logger.isInfoEnabled()) {
				logger.info("[jwm] no alarm" + alarmDataIdOrPath + " !");
			}
			return false;
		}

		AlarmNode alarmNode = new AlarmNode();
		try {
			alarmNode.jsonStringToNodeSelf(alarmData);
		} catch (JSONException e) {
			logger.error("[jwm] json string to alarm node error!", e);
			return false;
		}

		// �����澯���ݺ���սڵ��ϵ�����
		try {
			this.configManager.updateData(alarmDataIdOrPath, StringUtil.EMPTY_STRING, false);
		} catch (Exception e) {
			logger.error("[jwm]clear alarm data on zk path " + alarmDataIdOrPath + " error!", e);
			return false;
		}

		String alarmMsg = "[jwm]" + taskName + "," + hostName + "," + alarmNode.getMessage()
				+ alarmNode.getStackTrace();

		String msg = "";
		if (alarmMsg.length() < ALARM_LENGTH) {
			msg = alarmMsg;
		} else {
			msg = alarmMsg.substring(0, ALARM_LENGTH);
		}

		this.msg.set(msg);
		return true;
	}

	@Override
	protected String getMsg() {
		String temp = this.msg.get();
		this.msg.remove();
		return temp;
	}

	@Override
	protected boolean isToggleOpened(MonitorTaskNode monitorTaskNode) {
		return monitorTaskNode.isAlarmToggle();
	}

	@Override
	protected boolean isBeyondFrozenTime(long current, MonitorTaskNode monitorTaskNode, String hostName) {
		ConcurrentHashMap<String, Long> lastAlarmTimes = this.scheduler.getLastAlarmTime();
		if (lastAlarmTimes != null) {
			Long lastAlarmTime = lastAlarmTimes.get(hostName);
			if (null != lastAlarmTime) {
				return (current - lastAlarmTime) > monitorTaskNode.getAlarmFrozenPeriod();
			}

		}

		return false;
	}

	@Override
	protected void log(String msg) {
		logger.warn(msg);
	}

	@Override
	protected void flushLastAlarmTime(long tmsp, String hostName) {
		this.scheduler.getLastAlarmTime().put(hostName, tmsp);
	}

}
