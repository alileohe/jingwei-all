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

	/** ��� �� ��ض���TASK������ */
	private String taskName;

	/** ɨ������ʱ���ݵĲ�������, ��ӦZK�ڵ� /jingwei/tasks/**task/**host/stats, ���뵥λ */
	private long scanStatsPeriod;

	/** ɨ����Ҫ��ʱ�����ڵ��ɨ������ , ɨ��ZK�ڵ�/jingwei/tasks/**task/**host/alarm, ���뵥λ */
	private long scanAlarmPeriod;

	/** ɨ�������ڵ��ɨ������ , ɨ��ZK�ڵ�/jingwei/tasks/**task/**host/heartBeat, ���뵥λ */
	private long scanHeartBeatPeriod;

	/** ���ű����� */
	private String smsAlertUsers = "";

	/** ���������� */
	private String wwAlertUsers = "";

	/** ɾ��������TPS������ֵ���򱨾� */
	private int tpsUpperDeleteThreshold;

	/** ���� */
	private boolean toggleTpsUpperDeleteThreshold;

	/** ���������TPS������ֵ���򱨾� */
	private int tpsUpperInsertThreshold;

	/** ���� */
	private boolean toggleTpsUpperInsertThreshold;

	/** ���²�����TPS������ֵ���򱨾� */
	private int tpsUpperUpdateThreshold;

	/** ���� */
	private boolean toggleTpsUpperUpdateThreshold;

	/** ɾ��������TPS������ֵ���򱨾� */
	private int tpsLowerDeleteThreshold;

	/** ���� */
	private boolean toggleTpsLowerDeleteThreshold;

	/** ���������TPS���ڳ�����ֵ���򱨾� */
	private int tpsLowerInsertThreshold;

	/** ���� */
	private boolean toggleTpsLowerInsertThreshold;

	/** ���²�����TPS���ڳ�����ֵ���򱨾� */
	private int tpsLowerUpdateThreshold;

	/** ���� */
	private boolean toggleTpsLowerUpdateThreshold;

	/** ����delete�¼��ӳٳ���ָ����ֵ�򱨾����Ժ���Ϊ��λ */
	private long deleteDelayThreshold;

	/** ���� */
	private boolean toggleDeleteDelayThreshold;

	/** ����insert�¼��ӳٳ���ָ����ֵ�򱨾����Ժ���Ϊ��λ */
	private long insertDelayThreshold;

	/** ���� */
	private boolean toggleInsertDelayThreshold;

	/** ����update�¼��ӳٳ���ָ����ֵ�򱨾� */
	private long updateDelayThreshold;

	/** ���� */
	private boolean toggleUpdateDelayThreshold;

	/** ����delete�¼��쳣����ָ����ֵ�򱨾� */
	private long deleteExceptionThreshold;

	/** ���� */
	private boolean toggleDeleteExceptionThreshold;

	/** ����delete�¼��쳣����ָ����ֵ�򱨾� */
	private long insertExceptionThreshold;

	/** ���� */
	private boolean toggleInsertExceptionThreshold;

	/** ����delete�¼��쳣����ָ����ֵ�򱨾� */
	private long updateExceptionThreshold;

	/** ���� */
	private boolean toggleUpdateExceptionThreshold;

	/** extractor�ӳ�ʱ�䳬������ֵ�򱨾� */
	private long extractorDelayThreshold;

	/** ���� */
	private boolean toggleExtractorDelayThreshold;

	/** ��ֵ�澯�Ķ������� */
	private long thresholdFrozenPeriod;

	/** alarm�ڵ�澯�Ķ������� */
	private long alarmFrozenPeriod;

	/** �����ڵ�澯�Ķ������� */
	private long heartBeatFrozenPeriod;

	/** �Ƿ�����ʱ�澯����ww�Ͷ���  true ���澯��false�����澯ֻ����־*/
	private boolean alarmToggle;

	/** ��ֵ�澯����ww�Ͷ���  true ���澯��false�����澯ֻ����־*/
	private boolean thresholdToggle;

	/** �Ƿ�����ʱ�����Ƿ� true���澯��false�����澯ֻ����־*/
	private boolean heartBeatToggle;

	/** ���ʵ���� */
	private int taskInstanceCount;

	/** λ���鶳������  */
	private long positionFrozenPeriod;

	/** �ӳ�ʱ�䳬����ֵ�Ÿ澯 ��Ĭ���ǰ��Сʱ */
	private long positionAlarmThreshold;

	/** λ��û�б仯�澯 */
	private boolean positionToggle = true;

	/** λ��ʱ��������ٲŸ澯 */
	private long positionDelayAlarmThreshold;

	/** �����澯���� */
	private boolean isWwToggle = true;

	/** ���Ÿ澯���� */
	private boolean isSmToggle = true;

	public JingweiMonitorTask() {
	}

	public JingweiMonitorTask(MonitorTaskNode monitorTakNode) {
		this.setTaskName(monitorTakNode.getTaskName());

		// ͳ����������
		this.setScanStatsPeriod(monitorTakNode.getScanStatsPeriod());
		this.setThresholdFrozenPeriod(monitorTakNode.getThresholdFrozenPeriod());
		this.setThresholdToggle(monitorTakNode.isThresholdToggle());

		// �澯�������
		this.setScanAlarmPeriod(monitorTakNode.getScanAlarmPeriod());
		this.setAlarmFrozenPeriod(monitorTakNode.getAlarmFrozenPeriod());
		this.setAlarmToggle(monitorTakNode.isAlarmToggle());

		// �����������
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

		// �澯֪ͨ��
		String smsAlert = JingweiMonitorTask.getStringFromList(monitorTakNode.getSmsAlertUsers());
		this.setSmsAlertUsers(smsAlert);

		String wwAlert = JingweiMonitorTask.getStringFromList(monitorTakNode.getWwAlertUsers());
		this.setWwAlertUsers(wwAlert);

		// ʵ������
		this.setTaskInstanceCount(monitorTakNode.getTaskInstanceCount());

		// λ��
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
	 * �����ַ��������б�
	 * @param content
	 * @param sep
	 * @return  empty list ����ַ���Ϊ��
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

		// ɨ��ͳ����Ϣ������
		monitorTaskNode.setScanStatsPeriod(scanStatsPeriod);
		monitorTaskNode.setThresholdFrozenPeriod(thresholdFrozenPeriod);
		monitorTaskNode.setThresholdToggle(thresholdToggle);

		// ɨ��澯�ڵ������
		monitorTaskNode.setScanAlarmPeriod(scanAlarmPeriod);
		monitorTaskNode.setAlarmFrozenPeriod(alarmFrozenPeriod);
		monitorTaskNode.setAlarmToggle(alarmToggle);

		// ɨ�������ڵ������
		monitorTaskNode.setScanHeartBeatPeriod(scanHeartBeatPeriod);
		monitorTaskNode.setHeartBeatFrozenPeriod(heartBeatFrozenPeriod);
		monitorTaskNode.setHeartBeatToggle(heartBeatToggle);

		// ͳ����ֵ TPS
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

		// ͳ����ֵ �ӳ�
		monitorTaskNode.setInsertDelayThreshold(insertDelayThreshold);
		monitorTaskNode.setToggleInsertDelayThreshold(toggleInsertDelayThreshold);
		monitorTaskNode.setUpdateDelayThreshold(updateDelayThreshold);
		monitorTaskNode.setToggleUpdateDelayThreshold(toggleUpdateDelayThreshold);
		monitorTaskNode.setDeleteDelayThreshold(deleteDelayThreshold);
		monitorTaskNode.setToggleDeleteDelayThreshold(toggleDeleteDelayThreshold);
		monitorTaskNode.setExtractorDelayThreshold(extractorDelayThreshold);
		monitorTaskNode.setToggleExtractorDelayThreshold(toggleExtractorDelayThreshold);

		// ͳ����ֵ �쳣
		monitorTaskNode.setInsertExceptionThreshold(insertExceptionThreshold);
		monitorTaskNode.setToggleInsertExceptionThreshold(toggleInsertExceptionThreshold);
		monitorTaskNode.setUpdateExceptionThreshold(updateExceptionThreshold);
		monitorTaskNode.setToggleUpdateExceptionThreshold(toggleUpdateExceptionThreshold);
		monitorTaskNode.setDeleteExceptionThreshold(deleteExceptionThreshold);
		monitorTaskNode.setToggleDeleteExceptionThreshold(toggleDeleteExceptionThreshold);

		monitorTaskNode.setTaskInstanceCount(this.taskInstanceCount);

		// λ��
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
