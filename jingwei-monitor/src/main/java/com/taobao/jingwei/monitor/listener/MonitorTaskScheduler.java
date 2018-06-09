package com.taobao.jingwei.monitor.listener;

import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.config.ConfigDataListener;
import com.taobao.jingwei.common.config.ConfigManager;
import com.taobao.jingwei.common.node.monitor.MonitorTaskNode;
import com.taobao.jingwei.monitor.alert.AlertMsgManager;
import com.taobao.jingwei.monitor.checker.StatsShesholdChecker;
import com.taobao.jingwei.monitor.core.RunningHeartCount;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ����/jingwei/monitors/**monitor/**task�������ݱ仯
 * 
 * @author shuohailhl
 */
public class MonitorTaskScheduler extends ConfigDataListener implements JingWeiConstants {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/** �澯������ */
	private final AlertMsgManager alertMsgManager;

	/** ZK������ */
	private final ConfigManager configManager;

	/** ����Monitor����ֵ�������� */
	private volatile MonitorTaskNode monitorTaskNode;

	/** ������  */
	private final String taskName;

	/** �������ʱͳ�������Ƿ񳬹���ֵ */
	private final StatsShesholdChecker statsShesholdChecker;

	/** ��single�����Ѿ�������not running �澯������Ҫ��������� */
	private volatile Boolean isUnSingleTaskRunningAlerted = false;

	/** �����group���͵ģ���group���Բ�Ϊnull;�������group���ͣ��������Ϊnull */
	private String groupName;

	/* ===============================ɨ�����ʷ״̬==================================== */
	/** key : hostName, value : ��������澯��ʱ��  ���ڱ���Ƶ�������澯*/
	private final ConcurrentHashMap<String, Long> lastAlarmTime = new ConcurrentHashMap<String, Long>();

	/** ��¼running������״̬  */
	private final ConcurrentHashMap<String, RunningHeartCount> heartMemory = new ConcurrentHashMap<String, RunningHeartCount>();

	/** ʵ����Ŀ������������ */
	private final AtomicInteger continueCount = new AtomicInteger(0);

	/** ����ʵ������׼��������һ�θ澯ʱ��  */
	private long lastInstanceAlarmTime = 0L;

	/** ���һ��λ��澯ʱ�� */
	private long lastPositionAlarmTime = 0L;

	/** �ϴ��ύλ�� */
	private volatile String lastCommitPosition = "";

	private volatile long positionNotChangeTime = 0;

	private volatile boolean positionNotChange = false;

	private final AtomicLong runningHeartBeatTicker = new AtomicLong();

	private final AtomicLong positionTicker = new AtomicLong();

	private final AtomicLong alarmTicker = new AtomicLong();

	private final AtomicLong statsTicker = new AtomicLong();

	/*==============================================================================*/

	/** MonitorTaskNode��lock��ȡMonitorTaskNode������ֵ���� */
	private final Object nodeLock = new Object();

	public MonitorTaskScheduler(AlertMsgManager alertMsgManager, ConfigManager configManager, String monitorName,
			String taskName) {
		this.taskName = taskName;
		this.alertMsgManager = alertMsgManager;
		this.configManager = configManager;
		this.monitorTaskNode = new MonitorTaskNode(taskName);
		this.statsShesholdChecker = new StatsShesholdChecker(this);
	}

	public long getCurrentScanStatsPeriod() {
		return this.monitorTaskNode.getScanStatsPeriod();
	}

	public long getCurrentScanAlarmPeriod() {
		return this.monitorTaskNode.getScanAlarmPeriod();
	}

	public Long getCurrentScanHeartBeatPeriod() {
		return this.monitorTaskNode.getScanHeartBeatPeriod();
	}

	public AlertMsgManager getAlertMsgManager() {
		return alertMsgManager;
	}

	public ConfigManager getConfigManager() {
		return configManager;
	}

	public MonitorTaskNode getMonitorTaskNode() {
		return monitorTaskNode;
	}

	public Boolean getCurrentAlarmToggle() {
		return this.monitorTaskNode.isAlarmToggle();
	}

	public Boolean getCurrentHeartBeatToggle() {
		return this.monitorTaskNode.isHeartBeatToggle();
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public Boolean getStart() {
		return this.monitorTaskNode.isStart();
	}

	public StatsShesholdChecker getStatsShesholdChecker() {
		return statsShesholdChecker;
	}

	public Boolean getIsUnSingleTaskRunningAlerted() {
		return isUnSingleTaskRunningAlerted;
	}

	public void setIsUnSingleTaskRunningAlerted(Boolean isUnSingleTaskRunningAlerted) {
		this.isUnSingleTaskRunningAlerted = isUnSingleTaskRunningAlerted;
	}

	public ConcurrentHashMap<String, Long> getLastAlarmTime() {
		return lastAlarmTime;
	}

	public void setMonitorTaskNode(MonitorTaskNode monitorTaskNode) {
		this.monitorTaskNode = monitorTaskNode;
	}

	public AtomicLong getRunningHeartBeatTicker() {
		return runningHeartBeatTicker;
	}

	public AtomicLong getPositionTicker() {
		return positionTicker;
	}

	public AtomicLong getAlarmTicker() {
		return alarmTicker;
	}

	public AtomicLong getStatsTicker() {
		return statsTicker;
	}

	public long getLastInstanceAlarmTime() {
		return lastInstanceAlarmTime;
	}

	public void setLastInstanceAlarmTime(long lastInstanceAlarmTime) {
		this.lastInstanceAlarmTime = lastInstanceAlarmTime;
	}

	public String getTaskName() {
		return taskName;
	}

	public Object getNodeLock() {
		return nodeLock;
	}

	public AtomicInteger getContinueCount() {
		return continueCount;
	}

	public long getLastPositionAlarmTime() {
		return lastPositionAlarmTime;
	}

	public void setLastPositionAlarmTime(long lastPositionAlarmTime) {
		this.lastPositionAlarmTime = lastPositionAlarmTime;
	}

	public String getLastCommitPosition() {
		return lastCommitPosition;
	}

	public void setLastCommitPosition(String lastCommitPosition) {
		this.lastCommitPosition = lastCommitPosition;
	}

	public long getPositionNotChangeTime() {
		return positionNotChangeTime;
	}

	public void setPositionNotChangeTime(long positionNotChangeTime) {
		this.positionNotChangeTime = positionNotChangeTime;
	}

	public boolean isPositionNotChange() {
		return positionNotChange;
	}

	public void setPositionNotChange(boolean positionNotChange) {
		this.positionNotChange = positionNotChange;
	}

	@Override
	public void handleData(String dataIdOrPath, String data) {
		// /jingwei/monitors/tasks/**task
		if (null == data) {
			return;
		}

		MonitorTaskNode updateNode = new MonitorTaskNode(monitorTaskNode.getTaskName());
		// ��ֵ�޸ģ�ˢ���ڴ��MonitorThresholdNode
		try {
			updateNode.jsonStringToNodeSelf(data);
		} catch (JSONException e) {
			logger.error("[jingwei monitor] get jsonobj data to monitorThresholdNode  error!", e);
			return;
		}

		// ��������
		synchronized (this.getNodeLock()) {
			this.setMonitorTaskNode(updateNode);
		}
	}

	public ConcurrentHashMap<String, RunningHeartCount> getHeartMemory() {
		return heartMemory;
	}
}