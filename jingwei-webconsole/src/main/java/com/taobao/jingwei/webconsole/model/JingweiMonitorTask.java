package com.taobao.jingwei.webconsole.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jodd.util.StringUtil;

import com.taobao.jingwei.common.node.monitor.MonitorTaskNode;

public class JingweiMonitorTask {
	public static final String SEP_COMMA = ",";

	/** monitorName */
	private String monitorName;

	/** 监控 器 监控对象TASK的名字 */
	private String taskName;

	/** 扫描运行时数据的采样周期, 对应ZK节点 /jingwei/tasks/**task/**host/stats, 毫秒单位 */
	private long scanStatsPeriod;

	/** 扫描需要及时报警节点的扫描周期 , 扫描ZK节点/jingwei/tasks/**task/**host/alarm, 毫秒单位 */
	private long scanAlarmPeriod;

	/** 扫描心跳节点的扫描周期 , 扫描ZK节点/jingwei/tasks/**task/**host/heartBeat, 毫秒单位 */
	private long scanHeartBeatPeriod;

	/** 短信报警人 */
	private String smsAlertUsers = "";

	/** 旺旺报警人 */
	private String wwAlertUsers = "";

	/** 删除操作的TPS超过阈值，则报警 */
	private int tpsUpperDeleteThreshold;

	/** 开关 */
	private boolean toggleTpsUpperDeleteThreshold;

	/** 插入操作的TPS超过阈值，则报警 */
	private int tpsUpperInsertThreshold;

	/** 开关 */
	private boolean toggleTpsUpperInsertThreshold;

	/** 更新操作的TPS超过阈值，则报警 */
	private int tpsUpperUpdateThreshold;

	/** 开关 */
	private boolean toggleTpsUpperUpdateThreshold;

	/** 删除操作的TPS低于阈值，则报警 */
	private int tpsLowerDeleteThreshold;

	/** 开关 */
	private boolean toggleTpsLowerDeleteThreshold;

	/** 插入操作的TPS低于超过阈值，则报警 */
	private int tpsLowerInsertThreshold;

	/** 开关 */
	private boolean toggleTpsLowerInsertThreshold;

	/** 更新操作的TPS低于超过阈值，则报警 */
	private int tpsLowerUpdateThreshold;

	/** 开关 */
	private boolean toggleTpsLowerUpdateThreshold;

	/** 处理delete事件延迟超过指定阈值则报警，以毫秒为单位 */
	private long deleteDelayThreshold;

	/** 开关 */
	private boolean toggleDeleteDelayThreshold;

	/** 处理insert事件延迟超过指定阈值则报警，以毫秒为单位 */
	private long insertDelayThreshold;

	/** 开关 */
	private boolean toggleInsertDelayThreshold;

	/** 处理update事件延迟超过指定阈值则报警 */
	private long updateDelayThreshold;

	/** 开关 */
	private boolean toggleUpdateDelayThreshold;

	/** 处理delete事件异常超过指定阈值则报警 */
	private long deleteExceptionThreshold;

	/** 开关 */
	private boolean toggleDeleteExceptionThreshold;

	/** 处理delete事件异常超过指定阈值则报警 */
	private long insertExceptionThreshold;

	/** 开关 */
	private boolean toggleInsertExceptionThreshold;

	/** 处理delete事件异常超过指定阈值则报警 */
	private long updateExceptionThreshold;

	/** 开关 */
	private boolean toggleUpdateExceptionThreshold;

	/** extractor延迟时间超过该阈值则报警 */
	private long extractorDelayThreshold;

	/** 开关 */
	private boolean toggleExtractorDelayThreshold;

	/** 阀值告警的冻结周期 */
	private long thresholdFrozenPeriod;

	/** alarm节点告警的冻结周期 */
	private long alarmFrozenPeriod;

	/** 心跳节点告警的冻结周期 */
	private long heartBeatFrozenPeriod;

	/** 是否发运行时告警搭配ww和短信  true 发告警；false不发告警只记日志*/
	private boolean alarmToggle;

	/** 阈值告警开关ww和短信  true 发告警；false不发告警只记日志*/
	private boolean thresholdToggle;

	/** 是否发运行时心跳是否发 true发告警；false不发告警只记日志*/
	private boolean heartBeatToggle;

	/** 最低实例数 */
	private int taskInstanceCount;

	/** 位点检查冻结周期  */
	private long positionFrozenPeriod;

	/** 延迟时间超过阀值才告警 ，默认是半个小时 */
	private long positionAlarmThreshold;

	/** 位点没有变化告警 */
	private boolean positionToggle = true;

	/** 位点时间戳落后多少才告警 */
	private long positionDelayAlarmThreshold;

	/** 旺旺告警开关 */
	private boolean isWwToggle = true;

	/** 短信告警开关 */
	private boolean isSmToggle = true;

	public JingweiMonitorTask() {
	}

	public JingweiMonitorTask(MonitorTaskNode monitorTakNode) {
		this.setTaskName(monitorTakNode.getTaskName());

		// 统计数据周期
		this.setScanStatsPeriod(monitorTakNode.getScanStatsPeriod());
		this.setThresholdFrozenPeriod(monitorTakNode.getThresholdFrozenPeriod());
		this.setThresholdToggle(monitorTakNode.isThresholdToggle());

		// 告警监控周期
		this.setScanAlarmPeriod(monitorTakNode.getScanAlarmPeriod());
		this.setAlarmFrozenPeriod(monitorTakNode.getAlarmFrozenPeriod());
		this.setAlarmToggle(monitorTakNode.isAlarmToggle());

		// 心跳监控周期
		this.setScanHeartBeatPeriod(monitorTakNode.getScanHeartBeatPeriod());
		this.setHeartBeatFrozenPeriod(monitorTakNode.getHeartBeatFrozenPeriod());
		this.setHeartBeatToggle(monitorTakNode.isHeartBeatToggle());

		// TPS
		this.setTpsUpperDeleteThreshold(monitorTakNode.getTpsUpperDeleteThreshold());
		this.setToggleTpsUpperDeleteThreshold(monitorTakNode.isToggleTpsUpperDeleteThreshold());
		this.setTpsLowerDeleteThreshold(monitorTakNode.getTpsLowerDeleteThreshold());
		this.setToggleTpsLowerDeleteThreshold(monitorTakNode.isToggleTpsLowerDeleteThreshold());

		this.setTpsUpperInsertThreshold(monitorTakNode.getTpsUpperInsertThreshold());
		this.setToggleTpsUpperInsertThreshold(monitorTakNode.isToggleTpsUpperInsertThreshold());
		this.setTpsLowerInsertThreshold(monitorTakNode.getTpsLowerInsertThreshold());
		this.setToggleTpsLowerInsertThreshold(monitorTakNode.isToggleTpsLowerInsertThreshold());

		this.setTpsUpperUpdateThreshold(monitorTakNode.getTpsUpperUpdateThreshold());
		this.setToggleTpsUpperUpdateThreshold(monitorTakNode.isToggleTpsUpperUpdateThreshold());
		this.setTpsLowerUpdateThreshold(monitorTakNode.getTpsLowerUpdateThreshold());
		this.setToggleTpsLowerUpdateThreshold(monitorTakNode.isToggleTpsLowerUpdateThreshold());

		// delay
		this.setInsertDelayThreshold(monitorTakNode.getInsertDelayThreshold());
		this.setToggleInsertDelayThreshold(monitorTakNode.isToggleInsertDelayThreshold());
		this.setDeleteDelayThreshold(monitorTakNode.getDeleteDelayThreshold());
		this.setToggleDeleteDelayThreshold(monitorTakNode.isToggleDeleteDelayThreshold());
		this.setUpdateDelayThreshold(monitorTakNode.getUpdateDelayThreshold());
		this.setToggleUpdateDelayThreshold(monitorTakNode.isToggleUpdateDelayThreshold());
		this.setExtractorDelayThreshold(monitorTakNode.getExtractorDelayThreshold());
		this.setToggleExtractorDelayThreshold(monitorTakNode.isToggleExtractorDelayThreshold());

		// exception
		this.setDeleteExceptionThreshold(monitorTakNode.getDeleteExceptionThreshold());
		this.setToggleDeleteExceptionThreshold(monitorTakNode.isToggleDeleteExceptionThreshold());
		this.setInsertExceptionThreshold(monitorTakNode.getInsertExceptionThreshold());
		this.setToggleInsertExceptionThreshold(monitorTakNode.isToggleInsertExceptionThreshold());
		this.setUpdateExceptionThreshold(monitorTakNode.getUpdateExceptionThreshold());
		this.setToggleUpdateExceptionThreshold(monitorTakNode.isToggleUpdateExceptionThreshold());

		// 告警通知人
		String smsAlert = JingweiMonitorTask.getStringFromList(monitorTakNode.getSmsAlertUsers());
		this.setSmsAlertUsers(smsAlert);

		String wwAlert = JingweiMonitorTask.getStringFromList(monitorTakNode.getWwAlertUsers());
		this.setWwAlertUsers(wwAlert);

		// 实例数量
		this.setTaskInstanceCount(monitorTakNode.getTaskInstanceCount());

		// 位点
		this.setPositionAlarmThreshold(monitorTakNode.getPositionAlarmThreshold());
		this.setPositionFrozenPeriod(monitorTakNode.getPositionFrozenPeriod());
		this.setPositionToggle(monitorTakNode.isPositionToggle());

		this.setPositionDelayAlarmThreshold(monitorTakNode.getPositionDelayAlarmThreshold());
		
		this.setWwToggle(monitorTakNode.isWwToggle());
		this.setSmToggle(monitorTakNode.isSmToggle());
	}

	public String getMonitorName() {
		return monitorName;
	}

	public String getTaskName() {
		return taskName;
	}

	public long getScanStatsPeriod() {
		return scanStatsPeriod;
	}

	public long getScanAlarmPeriod() {
		return scanAlarmPeriod;
	}

	public long getScanHeartBeatPeriod() {
		return scanHeartBeatPeriod;
	}

	public String getSmsAlertUsers() {
		return smsAlertUsers;
	}

	public String getWwAlertUsers() {
		return wwAlertUsers;
	}

	public void setSmsAlertUsers(String smsAlertUsers) {
		this.smsAlertUsers = smsAlertUsers;
	}

	public void setWwAlertUsers(String wwAlertUsers) {
		this.wwAlertUsers = wwAlertUsers;
	}

	public int getTpsUpperDeleteThreshold() {
		return tpsUpperDeleteThreshold;
	}

	public boolean isToggleTpsUpperDeleteThreshold() {
		return toggleTpsUpperDeleteThreshold;
	}

	public int getTpsUpperInsertThreshold() {
		return tpsUpperInsertThreshold;
	}

	public boolean isToggleTpsUpperInsertThreshold() {
		return toggleTpsUpperInsertThreshold;
	}

	public int getTpsUpperUpdateThreshold() {
		return tpsUpperUpdateThreshold;
	}

	public boolean isToggleTpsUpperUpdateThreshold() {
		return toggleTpsUpperUpdateThreshold;
	}

	public int getTpsLowerDeleteThreshold() {
		return tpsLowerDeleteThreshold;
	}

	public boolean isToggleTpsLowerDeleteThreshold() {
		return toggleTpsLowerDeleteThreshold;
	}

	public int getTpsLowerInsertThreshold() {
		return tpsLowerInsertThreshold;
	}

	public boolean isToggleTpsLowerInsertThreshold() {
		return toggleTpsLowerInsertThreshold;
	}

	public int getTpsLowerUpdateThreshold() {
		return tpsLowerUpdateThreshold;
	}

	public boolean isToggleTpsLowerUpdateThreshold() {
		return toggleTpsLowerUpdateThreshold;
	}

	public long getDeleteDelayThreshold() {
		return deleteDelayThreshold;
	}

	public boolean isToggleDeleteDelayThreshold() {
		return toggleDeleteDelayThreshold;
	}

	public long getInsertDelayThreshold() {
		return insertDelayThreshold;
	}

	public boolean isToggleInsertDelayThreshold() {
		return toggleInsertDelayThreshold;
	}

	public long getUpdateDelayThreshold() {
		return updateDelayThreshold;
	}

	public boolean isToggleUpdateDelayThreshold() {
		return toggleUpdateDelayThreshold;
	}

	public long getDeleteExceptionThreshold() {
		return deleteExceptionThreshold;
	}

	public boolean isToggleDeleteExceptionThreshold() {
		return toggleDeleteExceptionThreshold;
	}

	public long getInsertExceptionThreshold() {
		return insertExceptionThreshold;
	}

	public boolean isToggleInsertExceptionThreshold() {
		return toggleInsertExceptionThreshold;
	}

	public long getUpdateExceptionThreshold() {
		return updateExceptionThreshold;
	}

	public boolean isToggleUpdateExceptionThreshold() {
		return toggleUpdateExceptionThreshold;
	}

	public long getExtractorDelayThreshold() {
		return extractorDelayThreshold;
	}

	public boolean isToggleExtractorDelayThreshold() {
		return toggleExtractorDelayThreshold;
	}

	public long getThresholdFrozenPeriod() {
		return thresholdFrozenPeriod;
	}

	public long getAlarmFrozenPeriod() {
		return alarmFrozenPeriod;
	}

	public long getHeartBeatFrozenPeriod() {
		return heartBeatFrozenPeriod;
	}

	public boolean isAlarmToggle() {
		return alarmToggle;
	}

	public boolean isThresholdToggle() {
		return thresholdToggle;
	}

	public boolean isHeartBeatToggle() {
		return heartBeatToggle;
	}

	public void setMonitorName(String monitorName) {
		this.monitorName = monitorName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public void setScanStatsPeriod(long scanStatsPeriod) {
		this.scanStatsPeriod = scanStatsPeriod;
	}

	public void setScanAlarmPeriod(long scanAlarmPeriod) {
		this.scanAlarmPeriod = scanAlarmPeriod;
	}

	public void setScanHeartBeatPeriod(long scanHeartBeatPeriod) {
		this.scanHeartBeatPeriod = scanHeartBeatPeriod;
	}

	public void setTpsUpperDeleteThreshold(int tpsUpperDeleteThreshold) {
		this.tpsUpperDeleteThreshold = tpsUpperDeleteThreshold;
	}

	public void setToggleTpsUpperDeleteThreshold(boolean toggleTpsUpperDeleteThreshold) {
		this.toggleTpsUpperDeleteThreshold = toggleTpsUpperDeleteThreshold;
	}

	public void setTpsUpperInsertThreshold(int tpsUpperInsertThreshold) {
		this.tpsUpperInsertThreshold = tpsUpperInsertThreshold;
	}

	public void setToggleTpsUpperInsertThreshold(boolean toggleTpsUpperInsertThreshold) {
		this.toggleTpsUpperInsertThreshold = toggleTpsUpperInsertThreshold;
	}

	public void setTpsUpperUpdateThreshold(int tpsUpperUpdateThreshold) {
		this.tpsUpperUpdateThreshold = tpsUpperUpdateThreshold;
	}

	public void setToggleTpsUpperUpdateThreshold(boolean toggleTpsUpperUpdateThreshold) {
		this.toggleTpsUpperUpdateThreshold = toggleTpsUpperUpdateThreshold;
	}

	public void setTpsLowerDeleteThreshold(int tpsLowerDeleteThreshold) {
		this.tpsLowerDeleteThreshold = tpsLowerDeleteThreshold;
	}

	public void setToggleTpsLowerDeleteThreshold(boolean toggleTpsLowerDeleteThreshold) {
		this.toggleTpsLowerDeleteThreshold = toggleTpsLowerDeleteThreshold;
	}

	public void setTpsLowerInsertThreshold(int tpsLowerInsertThreshold) {
		this.tpsLowerInsertThreshold = tpsLowerInsertThreshold;
	}

	public void setToggleTpsLowerInsertThreshold(boolean toggleTpsLowerInsertThreshold) {
		this.toggleTpsLowerInsertThreshold = toggleTpsLowerInsertThreshold;
	}

	public void setTpsLowerUpdateThreshold(int tpsLowerUpdateThreshold) {
		this.tpsLowerUpdateThreshold = tpsLowerUpdateThreshold;
	}

	public void setToggleTpsLowerUpdateThreshold(boolean toggleTpsLowerUpdateThreshold) {
		this.toggleTpsLowerUpdateThreshold = toggleTpsLowerUpdateThreshold;
	}

	public void setDeleteDelayThreshold(long deleteDelayThreshold) {
		this.deleteDelayThreshold = deleteDelayThreshold;
	}

	public void setToggleDeleteDelayThreshold(boolean toggleDeleteDelayThreshold) {
		this.toggleDeleteDelayThreshold = toggleDeleteDelayThreshold;
	}

	public void setInsertDelayThreshold(long insertDelayThreshold) {
		this.insertDelayThreshold = insertDelayThreshold;
	}

	public void setToggleInsertDelayThreshold(boolean toggleInsertDelayThreshold) {
		this.toggleInsertDelayThreshold = toggleInsertDelayThreshold;
	}

	public void setUpdateDelayThreshold(long updateDelayThreshold) {
		this.updateDelayThreshold = updateDelayThreshold;
	}

	public void setToggleUpdateDelayThreshold(boolean toggleUpdateDelayThreshold) {
		this.toggleUpdateDelayThreshold = toggleUpdateDelayThreshold;
	}

	public void setDeleteExceptionThreshold(long deleteExceptionThreshold) {
		this.deleteExceptionThreshold = deleteExceptionThreshold;
	}

	public void setToggleDeleteExceptionThreshold(boolean toggleDeleteExceptionThreshold) {
		this.toggleDeleteExceptionThreshold = toggleDeleteExceptionThreshold;
	}

	public void setInsertExceptionThreshold(long insertExceptionThreshold) {
		this.insertExceptionThreshold = insertExceptionThreshold;
	}

	public void setToggleInsertExceptionThreshold(boolean toggleInsertExceptionThreshold) {
		this.toggleInsertExceptionThreshold = toggleInsertExceptionThreshold;
	}

	public void setUpdateExceptionThreshold(long updateExceptionThreshold) {
		this.updateExceptionThreshold = updateExceptionThreshold;
	}

	public void setToggleUpdateExceptionThreshold(boolean toggleUpdateExceptionThreshold) {
		this.toggleUpdateExceptionThreshold = toggleUpdateExceptionThreshold;
	}

	public void setExtractorDelayThreshold(long extractorDelayThreshold) {
		this.extractorDelayThreshold = extractorDelayThreshold;
	}

	public void setToggleExtractorDelayThreshold(boolean toggleExtractorDelayThreshold) {
		this.toggleExtractorDelayThreshold = toggleExtractorDelayThreshold;
	}

	public void setThresholdFrozenPeriod(long thresholdFrozenPeriod) {
		this.thresholdFrozenPeriod = thresholdFrozenPeriod;
	}

	public void setAlarmFrozenPeriod(long alarmFrozenPeriod) {
		this.alarmFrozenPeriod = alarmFrozenPeriod;
	}

	public void setHeartBeatFrozenPeriod(long heartBeatFrozenPeriod) {
		this.heartBeatFrozenPeriod = heartBeatFrozenPeriod;
	}

	public void setAlarmToggle(boolean alarmToggle) {
		this.alarmToggle = alarmToggle;
	}

	public void setThresholdToggle(boolean thresholdToggle) {
		this.thresholdToggle = thresholdToggle;
	}

	public void setHeartBeatToggle(boolean heartBeatToggle) {
		this.heartBeatToggle = heartBeatToggle;
	}

	public static String getStringFromList(List<String> list) {

		if (list.isEmpty()) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		for (String e : list) {
			sb.append(e);
			sb.append(SEP_COMMA);
		}

		String content = sb.toString();

		return content.substring(0, content.length() - 1);
	}

	/**
	 * 根据字符串返回列表
	 * @param content
	 * @param sep
	 * @return  empty list 如果字符串为空
	 */
	public static List<String> getStringList(String content, String sep) {
		if (StringUtil.isBlank(content)) {
			return Collections.emptyList();
		}

		String[] list = content.split(sep);

		List<String> trimedList = new ArrayList<String>();

		for (String e : list) {
			trimedList.add(e.trim());
		}

		return trimedList;
	}

	public MonitorTaskNode getMonitorTaskNode() {
		MonitorTaskNode monitorTaskNode = new MonitorTaskNode(taskName);

		// 扫描统计信息的周期
		monitorTaskNode.setScanStatsPeriod(scanStatsPeriod);
		monitorTaskNode.setThresholdFrozenPeriod(thresholdFrozenPeriod);
		monitorTaskNode.setThresholdToggle(thresholdToggle);

		// 扫描告警节点的周期
		monitorTaskNode.setScanAlarmPeriod(scanAlarmPeriod);
		monitorTaskNode.setAlarmFrozenPeriod(alarmFrozenPeriod);
		monitorTaskNode.setAlarmToggle(alarmToggle);

		// 扫描心跳节点的周期
		monitorTaskNode.setScanHeartBeatPeriod(scanHeartBeatPeriod);
		monitorTaskNode.setHeartBeatFrozenPeriod(heartBeatFrozenPeriod);
		monitorTaskNode.setHeartBeatToggle(heartBeatToggle);

		// 统计阈值 TPS
		monitorTaskNode.setTpsUpperInsertThreshold(tpsUpperInsertThreshold);
		monitorTaskNode.setToggleTpsUpperInsertThreshold(toggleTpsUpperInsertThreshold);
		monitorTaskNode.setTpsLowerInsertThreshold(tpsLowerInsertThreshold);
		monitorTaskNode.setToggleTpsLowerInsertThreshold(toggleTpsLowerInsertThreshold);
		monitorTaskNode.setTpsUpperUpdateThreshold(tpsUpperUpdateThreshold);
		monitorTaskNode.setToggleTpsUpperUpdateThreshold(toggleTpsUpperUpdateThreshold);
		monitorTaskNode.setTpsLowerUpdateThreshold(tpsLowerUpdateThreshold);
		monitorTaskNode.setToggleTpsLowerUpdateThreshold(toggleTpsLowerUpdateThreshold);
		monitorTaskNode.setTpsUpperDeleteThreshold(tpsUpperDeleteThreshold);
		monitorTaskNode.setToggleTpsUpperDeleteThreshold(toggleTpsUpperDeleteThreshold);
		monitorTaskNode.setTpsLowerDeleteThreshold(tpsLowerDeleteThreshold);
		monitorTaskNode.setToggleTpsLowerDeleteThreshold(toggleTpsLowerDeleteThreshold);

		// 统计阈值 延迟
		monitorTaskNode.setInsertDelayThreshold(insertDelayThreshold);
		monitorTaskNode.setToggleInsertDelayThreshold(toggleInsertDelayThreshold);
		monitorTaskNode.setUpdateDelayThreshold(updateDelayThreshold);
		monitorTaskNode.setToggleUpdateDelayThreshold(toggleUpdateDelayThreshold);
		monitorTaskNode.setDeleteDelayThreshold(deleteDelayThreshold);
		monitorTaskNode.setToggleDeleteDelayThreshold(toggleDeleteDelayThreshold);
		monitorTaskNode.setExtractorDelayThreshold(extractorDelayThreshold);
		monitorTaskNode.setToggleExtractorDelayThreshold(toggleExtractorDelayThreshold);

		// 统计阈值 异常
		monitorTaskNode.setInsertExceptionThreshold(insertExceptionThreshold);
		monitorTaskNode.setToggleInsertExceptionThreshold(toggleInsertExceptionThreshold);
		monitorTaskNode.setUpdateExceptionThreshold(updateExceptionThreshold);
		monitorTaskNode.setToggleUpdateExceptionThreshold(toggleUpdateExceptionThreshold);
		monitorTaskNode.setDeleteExceptionThreshold(deleteExceptionThreshold);
		monitorTaskNode.setToggleDeleteExceptionThreshold(toggleDeleteExceptionThreshold);

		monitorTaskNode.setTaskInstanceCount(this.taskInstanceCount);

		// 位点
		monitorTaskNode.setPositionAlarmThreshold(this.positionAlarmThreshold);
		monitorTaskNode.setPositionFrozenPeriod(this.positionFrozenPeriod);
		monitorTaskNode.setPositionToggle(this.positionToggle);
		monitorTaskNode.setPositionDelayAlarmThreshold(this.positionDelayAlarmThreshold);

		return monitorTaskNode;
	}

	public int getTaskInstanceCount() {
		return taskInstanceCount;
	}

	public void setTaskInstanceCount(int taskInstanceCount) {
		this.taskInstanceCount = taskInstanceCount;
	}

	public long getPositionFrozenPeriod() {
		return positionFrozenPeriod;
	}

	public void setPositionFrozenPeriod(long positionFrozenPeriod) {
		this.positionFrozenPeriod = positionFrozenPeriod;
	}

	public long getPositionAlarmThreshold() {
		return positionAlarmThreshold;
	}

	public void setPositionAlarmThreshold(long positionAlarmThreshold) {
		this.positionAlarmThreshold = positionAlarmThreshold;
	}

	public boolean isPositionToggle() {
		return positionToggle;
	}

	public void setPositionToggle(boolean positionToggle) {
		this.positionToggle = positionToggle;
	}

	public long getPositionDelayAlarmThreshold() {
		return positionDelayAlarmThreshold;
	}

	public void setPositionDelayAlarmThreshold(long positionDelayAlarmThreshold) {
		this.positionDelayAlarmThreshold = positionDelayAlarmThreshold;
	}

	public boolean isWwToggle() {
		return isWwToggle;
	}

	public void setWwToggle(boolean isWwToggle) {
		this.isWwToggle = isWwToggle;
	}

	public boolean isSmToggle() {
		return isSmToggle;
	}

	public void setSmToggle(boolean isSmToggle) {
		this.isSmToggle = isSmToggle;
	}
}
