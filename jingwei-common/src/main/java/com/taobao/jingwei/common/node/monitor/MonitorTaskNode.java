package com.taobao.jingwei.common.node.monitor;

import com.taobao.jingwei.common.JingWeiUtil;
import com.taobao.jingwei.common.node.AbstractNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * ��ָ��ʱ��Σ�ͳ�����ڣ��ڵ�ƽ���ӳ�ʱ�䳬��ָ����ֵ������TPS����ָ����ֵ������ø澯ģ�鱨��</p>
 * ��ӦZK�ڵ�/jingwei/monitors/**task/**task
 * 
 * @author shuohailhl
 * 
 */
public class MonitorTaskNode extends AbstractNode {
	private String monitorName;

	/** ��� �� ��ض���TASK������ */
	private final String taskName;

	/** ɨ������ʱ���ݵĲ�������, ��ӦZK�ڵ� /jingwei/tasks/**task/**host/stats, ���뵥λ */
	private long scanStatsPeriod = JingWeiUtil.DEFAULT_SCAN_STATS_PERIOD;

	/** ɨ����Ҫ��ʱ�����ڵ��ɨ������ , ɨ��ZK�ڵ�/jingwei/tasks/**task/**host/alarm, ���뵥λ */
	private long scanAlarmPeriod = JingWeiUtil.DEFAULT_SCAN_ALARM_PERIOD;

	/** ɨ�������ڵ��ɨ������ , ɨ��ZK�ڵ�/jingwei/tasks/**task/**host/heartBeat, ���뵥λ */
	private long scanHeartBeatPeriod = JingWeiUtil.DEFAULT_SCAN_HEARTBEAT_PERIOD;

	/** ���ű����� */
	private volatile List<String> smsAlertUsers = Collections.emptyList();

	/** ���������� */
	private volatile List<String> wwAlertUsers = Collections.emptyList();

	/** ɾ��������TPS������ֵ���򱨾� */
	private int tpsUpperDeleteThreshold;

	/** ���� */
	private boolean toggleTpsUpperDeleteThreshold = false;

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

	/** �������TPS��������ֵ�򱨾� */
	private int txTpsUpperThreshold;

	/** �������TPSС�ڸ���ֵ�򱨾� */
	private int txTpsLowerThreshold;

	/** ������ƽ���ӳ� */
	private long txMillisAvgLatencyThreshold;

	/** ��ֵ�澯�Ķ������� */
	private long thresholdFrozenPeriod = DEFAULT_THRESHOLD_FROZEN_PERIOD;

	/** alarm�ڵ�澯�Ķ������� */
	private long alarmFrozenPeriod = DEFAULT_ALARM_FROZEN_PERIOD;

	/** �����ڵ�澯�Ķ������� */
	private long heartBeatFrozenPeriod = DEFAULT_HEARTBEAT_FROZEN_PERIOD;

	/** λ���鶳������  */
	private long positionFrozenPeriod = DEFAULT_POSITION_FROZEN_PERIOD;

	/** λ��೤ʱ��û�仯������ֵ�Ÿ澯 ��Ĭ���ǰ��Сʱ */
	private long positionAlarmThreshold = DEFAULT_POSITION_NOT_CHANGE_PERIOD;

	/** λ��ʱ�������ػ�������ʱ�������澯 */
	private long positionDelayAlarmThreshold = DEFAULT_LANTENCY_ALARM_THRESHOULD;

	/** �Ƿ�����ʱ�澯����ww�Ͷ���  true ���澯��false�����澯ֻ����־*/
	private boolean alarmToggle = true;

	/** ��ֵ�澯����ww�Ͷ���  true ���澯��false�����澯ֻ����־*/
	private boolean thresholdToggle = true;

	/** �Ƿ�����ʱ�����Ƿ� true���澯��false�����澯ֻ����־*/
	private boolean heartBeatToggle = true;

	/** λ��û�б仯�澯 */
	private boolean positionToggle = true;

	/** �Ƿ�����ɨ����  */
	private boolean start = false;

	/** ���������Ƿ�running״̬ʱʹ�ã�ͬһ������ͬʱ�����ж��ٸ�����ʵ��ͬʱ���У�������single��Ĭ��ֻ��һ�����ǵ�������Ҫ���ø澯����������������������Ҫ�澯 */
	private int taskInstanceCount = 1;

	/** ȷ���Ƿ���group���� */
	private boolean isGroup = false;

	/** �����澯���� */
	private boolean isWwToggle = true;

	/** ���Ÿ澯���� */
	private boolean isSmToggle = true;

	private static final String START_KEY = "start";
	private static final String TASK_INSTANCE_COUNT = "taskInstanceCount";
	private static final String IS_GROUP = "isGroup";
	private static final String POSITION_FROZEN_PERIOD = "positionFrozenPeriod";
	private static final String POSITION_ALARM_THRESHOLD = "positionAlarmThreshold";
	private static final String POSITION_TOGGLE = "positionToggle";
	private static final String POSITION_DELAY_ALARM_THRESHOLD = "positionDelayAlarmThreshold";
	private static final String IS_WW_TOGGLE = "isWwToggle";
	private static final String IS_SM_TOGGLE = "isSmToggle";

	public MonitorTaskNode(String taskName) {
		this.taskName = taskName;
		super.setName(taskName);
	}

	public MonitorTaskNode(String monitorName, String taskName) {
		this.taskName = taskName;
		this.monitorName = monitorName;
		super.setName(taskName);
	}

	@Override
	public boolean isPersistent() {
		return true;
	}

	@Override
	public String getDataIdOrNodePath() {

		StringBuilder sb = new StringBuilder(JINGWEI_MONITOR_ROOT_PATH);

		if (isGroup) {
			sb.append(ZK_PATH_SEP).append(JINGWEI_MONITOR_GROUPS_NAME);
		} else {
			sb.append(ZK_PATH_SEP).append(JINGWEI_MONITOR_TASKS_NAME);
		}

		sb.append(ZK_PATH_SEP).append(this.taskName);

		return sb.toString();

	}

	@Override
	protected void specilizeAttributeToJsonObject(JSONObject jsonObject) throws JSONException {
		jsonObject.put("taskName", this.getTaskName());

		jsonObject.put("scanStatsPeriod", this.getScanStatsPeriod());
		jsonObject.put("scanAlarmPeriod", this.getScanAlarmPeriod());
		jsonObject.put("scanHeartBeatPeriod", this.getScanHeartBeatPeriod());

		jsonObject.put("smsAlertUsers", this.smsAlertUsers != null ? new JSONArray(this.smsAlertUsers)
				: new JSONArray());
		jsonObject.put("wwAlertUsers", this.wwAlertUsers != null ? new JSONArray(this.wwAlertUsers) : new JSONArray());

		jsonObject.put("tpsUpperDeleteThreshold", this.getTpsUpperDeleteThreshold());
		jsonObject.put("tpsUpperInsertThreshold", this.getTpsUpperInsertThreshold());
		jsonObject.put("tpsUpperUpdateThreshold", this.getTpsUpperUpdateThreshold());
		jsonObject.put("tpsLowerDeleteThreshold", this.getTpsLowerDeleteThreshold());
		jsonObject.put("tpsLowerInsertThreshold", this.getTpsLowerInsertThreshold());
		jsonObject.put("tpsLowerUpdateThreshold", this.getTpsLowerUpdateThreshold());
		jsonObject.put("deleteDelayThreshold", this.getDeleteDelayThreshold());
		jsonObject.put("insertDelayThreshold", this.getInsertDelayThreshold());
		jsonObject.put("updateDelayThreshold", this.getUpdateDelayThreshold());
		jsonObject.put("deleteExceptionThreshold", this.getDeleteExceptionThreshold());
		jsonObject.put("insertExceptionThreshold", this.getInsertExceptionThreshold());
		jsonObject.put("updateExceptionThreshold", this.getUpdateExceptionThreshold());

		// extractor��tx
		jsonObject.put("extractorDelayThreshold", this.getExtractorDelayThreshold());
		jsonObject.put("txTpsUpperThreshold", this.getTxTpsUpperThreshold());
		jsonObject.put("txTpsLowerThreshold", this.getTxTpsLowerThreshold());
		jsonObject.put("txMillisAvgLatencyThreshold", this.getTxMillisAvgLatencyThreshold());

		// ��ֵ�������ں�alarm��������
		jsonObject.put("alarmToggle", this.isAlarmToggle());
		jsonObject.put("heartBeatToggle", this.isHeartBeatToggle());
		jsonObject.put("thresholdToggle", this.isThresholdToggle());

		jsonObject.put("alarmFrozenPeriod", this.getAlarmFrozenPeriod());
		jsonObject.put("heartBeatFrozenPeriod", this.getHeartBeatFrozenPeriod());
		jsonObject.put("thresholdFrozenPeriod", this.getThresholdFrozenPeriod());

		// ϸ��Ŀ���-�ӳ�
		jsonObject.put("toggleDeleteDelayThreshold", this.isToggleDeleteDelayThreshold());
		jsonObject.put("toggleInsertDelayThreshold", this.isToggleInsertDelayThreshold());
		jsonObject.put("toggleUpdateDelayThreshold", this.isToggleUpdateDelayThreshold());
		jsonObject.put("toggleExtractorDelayThreshold", this.isToggleExtractorDelayThreshold());

		// ϸ��Ŀ���-TPS
		jsonObject.put("toggleTpsLowerDeleteThreshold", this.isToggleTpsLowerDeleteThreshold());
		jsonObject.put("toggleTpsUpperDeleteThreshold", this.isToggleTpsUpperDeleteThreshold());
		jsonObject.put("toggleTpsLowerUpdateThreshold", this.isToggleTpsLowerUpdateThreshold());
		jsonObject.put("toggleTpsUpperUpdateThreshold", this.isToggleTpsUpperUpdateThreshold());
		jsonObject.put("toggleTpsLowerInsertThreshold", this.isToggleTpsLowerInsertThreshold());
		jsonObject.put("toggleTpsUpperInsertThreshold", this.isToggleTpsUpperInsertThreshold());

		// ϸ��Ŀ���-�쳣
		jsonObject.put("toggleDeleteExceptionThreshold", this.isToggleDeleteExceptionThreshold());
		jsonObject.put("toggleUpdateExceptionThreshold", this.isToggleUpdateExceptionThreshold());
		jsonObject.put("toggleInsertExceptionThreshold", this.isToggleInsertExceptionThreshold());

		// �Ƿ�����
		jsonObject.put(START_KEY, this.isStart());

		// ����澯����
		jsonObject.put(TASK_INSTANCE_COUNT, this.getTaskInstanceCount());

		// group���͵�����
		jsonObject.put(IS_GROUP, this.isGroup());

		// λ��澯��������
		jsonObject.put(POSITION_FROZEN_PERIOD, this.getPositionFrozenPeriod());

		// λ��೤ʱ�䲻�仯�Ÿ澯���뵥λ
		jsonObject.put(POSITION_ALARM_THRESHOLD, this.getPositionAlarmThreshold());

		// λ�㲻�仯�澯����
		jsonObject.put(POSITION_TOGGLE, this.isPositionToggle());

		// λ��ʱ��������ٲŸ澯
		jsonObject.put(POSITION_DELAY_ALARM_THRESHOLD, this.getPositionDelayAlarmThreshold());

		// �����澯����
		jsonObject.put(IS_WW_TOGGLE, this.isWwToggle());

		// ���ſ���
		jsonObject.put(IS_SM_TOGGLE, this.isSmToggle());
	}

	@Override
	protected void jsonObjectToSpecilizeAttribute(JSONObject jsonObject) throws JSONException {

		this.scanStatsPeriod = jsonObject.getLong("scanStatsPeriod");
		this.scanAlarmPeriod = jsonObject.getLong("scanAlarmPeriod");
		this.scanHeartBeatPeriod = jsonObject.getLong("scanHeartBeatPeriod");

		this.setSmsAlertUsers(JingWeiUtil.<String> jsonArray2List(jsonObject.getJSONArray("smsAlertUsers")));
		this.setWwAlertUsers(JingWeiUtil.<String> jsonArray2List(jsonObject.getJSONArray("wwAlertUsers")));

		this.tpsUpperDeleteThreshold = jsonObject.getInt("tpsUpperDeleteThreshold");
		this.tpsUpperInsertThreshold = jsonObject.getInt("tpsUpperInsertThreshold");
		this.tpsUpperUpdateThreshold = jsonObject.getInt("tpsUpperUpdateThreshold");
		this.tpsLowerDeleteThreshold = jsonObject.getInt("tpsLowerDeleteThreshold");
		this.tpsLowerInsertThreshold = jsonObject.getInt("tpsLowerInsertThreshold");
		this.tpsLowerUpdateThreshold = jsonObject.getInt("tpsLowerUpdateThreshold");
		this.deleteDelayThreshold = jsonObject.getLong("deleteDelayThreshold");
		this.insertDelayThreshold = jsonObject.getLong("insertDelayThreshold");
		this.updateDelayThreshold = jsonObject.getLong("updateDelayThreshold");
		this.deleteExceptionThreshold = jsonObject.getLong("deleteExceptionThreshold");
		this.insertExceptionThreshold = jsonObject.getLong("insertExceptionThreshold");
		this.updateExceptionThreshold = jsonObject.getLong("updateExceptionThreshold");

		// extractor��tx
		this.setExtractorDelayThreshold(jsonObject.getLong("extractorDelayThreshold"));
		this.setTxTpsUpperThreshold(jsonObject.getInt("txTpsUpperThreshold"));
		this.setTxTpsLowerThreshold(jsonObject.getInt("txTpsLowerThreshold"));
		this.setTxMillisAvgLatencyThreshold(jsonObject.getLong("txMillisAvgLatencyThreshold"));

		// ��ֵ�������ں�alarm��������
		this.setAlarmToggle(jsonObject.getBoolean("alarmToggle"));
		this.setHeartBeatToggle(jsonObject.getBoolean("heartBeatToggle"));
		this.setThresholdToggle(jsonObject.getBoolean("thresholdToggle"));

		this.setAlarmFrozenPeriod(jsonObject.getLong("alarmFrozenPeriod"));
		this.setHeartBeatFrozenPeriod(jsonObject.getLong("heartBeatFrozenPeriod"));
		this.setThresholdFrozenPeriod(jsonObject.getLong("thresholdFrozenPeriod"));

		// ϸ��Ŀ���-�ӳ�
		this.setToggleDeleteDelayThreshold(jsonObject.getBoolean("toggleDeleteDelayThreshold"));
		this.setToggleUpdateDelayThreshold(jsonObject.getBoolean("toggleUpdateDelayThreshold"));
		this.setToggleInsertDelayThreshold(jsonObject.getBoolean("toggleInsertDelayThreshold"));
		this.setToggleExtractorDelayThreshold(jsonObject.getBoolean("toggleExtractorDelayThreshold"));

		// ϸ��Ŀ���-TPS
		this.setToggleTpsLowerDeleteThreshold(jsonObject.getBoolean("toggleTpsLowerDeleteThreshold"));
		this.setToggleTpsUpperDeleteThreshold(jsonObject.getBoolean("toggleTpsUpperDeleteThreshold"));
		this.setToggleTpsLowerUpdateThreshold(jsonObject.getBoolean("toggleTpsLowerUpdateThreshold"));
		this.setToggleTpsUpperUpdateThreshold(jsonObject.getBoolean("toggleTpsUpperUpdateThreshold"));
		this.setToggleTpsLowerInsertThreshold(jsonObject.getBoolean("toggleTpsLowerInsertThreshold"));
		this.setToggleTpsUpperInsertThreshold(jsonObject.getBoolean("toggleTpsUpperInsertThreshold"));

		// ϸ��Ŀ���-�쳣
		this.setToggleDeleteExceptionThreshold(jsonObject.getBoolean("toggleDeleteExceptionThreshold"));
		this.setToggleUpdateExceptionThreshold(jsonObject.getBoolean("toggleUpdateExceptionThreshold"));
		this.setToggleInsertExceptionThreshold(jsonObject.getBoolean("toggleInsertExceptionThreshold"));

		// �Ƿ�����
		if (jsonObject.has(START_KEY)) {
			this.setStart(jsonObject.getBoolean(START_KEY));
		}

		// ����ʵ�������澯
		if (jsonObject.has(TASK_INSTANCE_COUNT)) {
			this.setTaskInstanceCount(jsonObject.getInt(TASK_INSTANCE_COUNT));
		}

		// group����
		if (jsonObject.has(IS_GROUP)) {
			this.setGroup(jsonObject.getBoolean(IS_GROUP));
		}

		// position��������
		if (jsonObject.has(POSITION_FROZEN_PERIOD)) {
			this.setPositionFrozenPeriod(jsonObject.getLong(POSITION_FROZEN_PERIOD));
		}

		//  λ�������ٲŸ澯
		if (jsonObject.has(POSITION_ALARM_THRESHOLD)) {
			this.setPositionAlarmThreshold(jsonObject.getLong(POSITION_ALARM_THRESHOLD));
		}

		// λ��澯����
		if (jsonObject.has(POSITION_TOGGLE)) {
			this.setPositionToggle(jsonObject.getBoolean(POSITION_TOGGLE));
		}

		// λ���ӳٶ��ٲŸ澯
		if (jsonObject.has(POSITION_DELAY_ALARM_THRESHOLD)) {
			this.setPositionDelayAlarmThreshold(jsonObject.getLong(POSITION_DELAY_ALARM_THRESHOLD));
		}

		// �����澯����
		if (jsonObject.has(IS_WW_TOGGLE)) {
			this.setWwToggle(jsonObject.getBoolean(IS_WW_TOGGLE));
		}

		// ���Ÿ澯����
		if (jsonObject.has(IS_SM_TOGGLE)) {
			this.setSmToggle(jsonObject.getBoolean(IS_SM_TOGGLE));
		}
	}

	public String getTaskName() {
		return taskName;
	}

	public long getDeleteDelayThreshold() {
		return deleteDelayThreshold;
	}

	public void setDeleteDelayThreshold(long deleteDelayThreshold) {
		this.deleteDelayThreshold = deleteDelayThreshold;
	}

	public long getInsertDelayThreshold() {
		return insertDelayThreshold;
	}

	public void setInsertDelayThreshold(long insertDelayThreshold) {
		this.insertDelayThreshold = insertDelayThreshold;
	}

	public long getUpdateDelayThreshold() {
		return updateDelayThreshold;
	}

	public void setUpdateDelayThreshold(long updateDelayThreshold) {
		this.updateDelayThreshold = updateDelayThreshold;
	}

	public long getDeleteExceptionThreshold() {
		return deleteExceptionThreshold;
	}

	public void setDeleteExceptionThreshold(long deleteExceptionThreshold) {
		this.deleteExceptionThreshold = deleteExceptionThreshold;
	}

	public long getInsertExceptionThreshold() {
		return insertExceptionThreshold;
	}

	public void setInsertExceptionThreshold(long insertExceptionThreshold) {
		this.insertExceptionThreshold = insertExceptionThreshold;
	}

	public long getUpdateExceptionThreshold() {
		return updateExceptionThreshold;
	}

	public void setUpdateExceptionThreshold(long updateExceptionThreshold) {
		this.updateExceptionThreshold = updateExceptionThreshold;
	}

	public List<String> getSmsAlertUsers() {
		return smsAlertUsers;
	}

	public void setSmsAlertUsers(List<String> smsAlertUsers) {
		this.smsAlertUsers = smsAlertUsers;
	}

	public List<String> getWwAlertUsers() {
		return wwAlertUsers;
	}

	public void setWwAlertUsers(List<String> wwAlertUsers) {
		this.wwAlertUsers = wwAlertUsers;
	}

	public int getTpsUpperDeleteThreshold() {
		return tpsUpperDeleteThreshold;
	}

	public void setTpsUpperDeleteThreshold(int tpsUpperDeleteThreshold) {
		this.tpsUpperDeleteThreshold = tpsUpperDeleteThreshold;
	}

	public int getTpsUpperInsertThreshold() {
		return tpsUpperInsertThreshold;
	}

	public void setTpsUpperInsertThreshold(int tpsUpperInsertThreshold) {
		this.tpsUpperInsertThreshold = tpsUpperInsertThreshold;
	}

	public int getTpsUpperUpdateThreshold() {
		return tpsUpperUpdateThreshold;
	}

	public void setTpsUpperUpdateThreshold(int tpsUpperUpdateThreshold) {
		this.tpsUpperUpdateThreshold = tpsUpperUpdateThreshold;
	}

	public int getTpsLowerDeleteThreshold() {
		return tpsLowerDeleteThreshold;
	}

	public void setTpsLowerDeleteThreshold(int tpsLowerDeleteThreshold) {
		this.tpsLowerDeleteThreshold = tpsLowerDeleteThreshold;
	}

	public int getTpsLowerInsertThreshold() {
		return tpsLowerInsertThreshold;
	}

	public void setTpsLowerInsertThreshold(int tpsLowerInsertThreshold) {
		this.tpsLowerInsertThreshold = tpsLowerInsertThreshold;
	}

	public int getTpsLowerUpdateThreshold() {
		return tpsLowerUpdateThreshold;
	}

	public void setTpsLowerUpdateThreshold(int tpsLowerUpdateThreshold) {
		this.tpsLowerUpdateThreshold = tpsLowerUpdateThreshold;
	}

	public long getScanAlarmPeriod() {
		return scanAlarmPeriod;
	}

	public void setScanAlarmPeriod(long scanAlarmPeriod) {
		this.scanAlarmPeriod = scanAlarmPeriod;
	}

	public long getExtractorDelayThreshold() {
		return extractorDelayThreshold;
	}

	public void setExtractorDelayThreshold(long extractorDelayThreshold) {
		this.extractorDelayThreshold = extractorDelayThreshold;
	}

	public int getTxTpsUpperThreshold() {
		return txTpsUpperThreshold;
	}

	public void setTxTpsUpperThreshold(int txTpsUpperThreshold) {
		this.txTpsUpperThreshold = txTpsUpperThreshold;
	}

	public int getTxTpsLowerThreshold() {
		return txTpsLowerThreshold;
	}

	public void setTxTpsLowerThreshold(int txTpsLowerThreshold) {
		this.txTpsLowerThreshold = txTpsLowerThreshold;
	}

	public long getTxMillisAvgLatencyThreshold() {
		return txMillisAvgLatencyThreshold;
	}

	public void setTxMillisAvgLatencyThreshold(long txMillisAvgLatencyThreshold) {
		this.txMillisAvgLatencyThreshold = txMillisAvgLatencyThreshold;
	}

	public long getScanHeartBeatPeriod() {
		return scanHeartBeatPeriod;
	}

	public void setScanHeartBeatPeriod(long scanHeartBeatPeriod) {
		this.scanHeartBeatPeriod = scanHeartBeatPeriod;
	}

	public long getThresholdFrozenPeriod() {
		return thresholdFrozenPeriod;
	}

	public void setThresholdFrozenPeriod(long thresholdFrozenPeriod) {
		this.thresholdFrozenPeriod = thresholdFrozenPeriod;
	}

	public boolean isAlarmToggle() {
		return alarmToggle;
	}

	public void setAlarmToggle(boolean alarmToggle) {
		this.alarmToggle = alarmToggle;
	}

	public long getScanStatsPeriod() {
		return scanStatsPeriod;
	}

	public void setScanStatsPeriod(long scanStatsPeriod) {
		this.scanStatsPeriod = scanStatsPeriod;
	}

	public boolean isHeartBeatToggle() {
		return heartBeatToggle;
	}

	public void setHeartBeatToggle(boolean heartBeatToggle) {
		this.heartBeatToggle = heartBeatToggle;
	}

	public long getAlarmFrozenPeriod() {
		return alarmFrozenPeriod;
	}

	public void setAlarmFrozenPeriod(long alarmFrozenPeriod) {
		this.alarmFrozenPeriod = alarmFrozenPeriod;
	}

	public long getHeartBeatFrozenPeriod() {
		return heartBeatFrozenPeriod;
	}

	public void setHeartBeatFrozenPeriod(long heartBeatFrozenPeriod) {
		this.heartBeatFrozenPeriod = heartBeatFrozenPeriod;
	}

	public boolean isThresholdToggle() {
		return thresholdToggle;
	}

	public void setThresholdToggle(boolean thresholdToggle) {
		this.thresholdToggle = thresholdToggle;
	}

	public boolean isToggleTpsUpperDeleteThreshold() {
		return toggleTpsUpperDeleteThreshold;
	}

	public void setToggleTpsUpperDeleteThreshold(boolean toggleTpsUpperDeleteThreshold) {
		this.toggleTpsUpperDeleteThreshold = toggleTpsUpperDeleteThreshold;
	}

	public boolean isToggleTpsUpperInsertThreshold() {
		return toggleTpsUpperInsertThreshold;
	}

	public void setToggleTpsUpperInsertThreshold(boolean toggleTpsUpperInsertThreshold) {
		this.toggleTpsUpperInsertThreshold = toggleTpsUpperInsertThreshold;
	}

	public boolean isToggleTpsUpperUpdateThreshold() {
		return toggleTpsUpperUpdateThreshold;
	}

	public void setToggleTpsUpperUpdateThreshold(boolean toggleTpsUpperUpdateThreshold) {
		this.toggleTpsUpperUpdateThreshold = toggleTpsUpperUpdateThreshold;
	}

	public boolean isToggleTpsLowerDeleteThreshold() {
		return toggleTpsLowerDeleteThreshold;
	}

	public void setToggleTpsLowerDeleteThreshold(boolean toggleTpsLowerDeleteThreshold) {
		this.toggleTpsLowerDeleteThreshold = toggleTpsLowerDeleteThreshold;
	}

	public boolean isToggleTpsLowerInsertThreshold() {
		return toggleTpsLowerInsertThreshold;
	}

	public void setToggleTpsLowerInsertThreshold(boolean toggleTpsLowerInsertThreshold) {
		this.toggleTpsLowerInsertThreshold = toggleTpsLowerInsertThreshold;
	}

	public boolean isToggleTpsLowerUpdateThreshold() {
		return toggleTpsLowerUpdateThreshold;
	}

	public void setToggleTpsLowerUpdateThreshold(boolean toggleTpsLowerUpdateThreshold) {
		this.toggleTpsLowerUpdateThreshold = toggleTpsLowerUpdateThreshold;
	}

	public boolean isToggleDeleteDelayThreshold() {
		return toggleDeleteDelayThreshold;
	}

	public void setToggleDeleteDelayThreshold(boolean toggleDeleteDelayThreshold) {
		this.toggleDeleteDelayThreshold = toggleDeleteDelayThreshold;
	}

	public boolean isToggleInsertDelayThreshold() {
		return toggleInsertDelayThreshold;
	}

	public void setToggleInsertDelayThreshold(boolean toggleInsertDelayThreshold) {
		this.toggleInsertDelayThreshold = toggleInsertDelayThreshold;
	}

	public boolean isToggleUpdateDelayThreshold() {
		return toggleUpdateDelayThreshold;
	}

	public void setToggleUpdateDelayThreshold(boolean toggleUpdateDelayThreshold) {
		this.toggleUpdateDelayThreshold = toggleUpdateDelayThreshold;
	}

	public boolean isToggleDeleteExceptionThreshold() {
		return toggleDeleteExceptionThreshold;
	}

	public void setToggleDeleteExceptionThreshold(boolean toggleDeleteExceptionThreshold) {
		this.toggleDeleteExceptionThreshold = toggleDeleteExceptionThreshold;
	}

	public boolean isToggleInsertExceptionThreshold() {
		return toggleInsertExceptionThreshold;
	}

	public void setToggleInsertExceptionThreshold(boolean toggleInsertExceptionThreshold) {
		this.toggleInsertExceptionThreshold = toggleInsertExceptionThreshold;
	}

	public boolean isToggleUpdateExceptionThreshold() {
		return toggleUpdateExceptionThreshold;
	}

	public void setToggleUpdateExceptionThreshold(boolean toggleUpdateExceptionThreshold) {
		this.toggleUpdateExceptionThreshold = toggleUpdateExceptionThreshold;
	}

	public boolean isToggleExtractorDelayThreshold() {
		return toggleExtractorDelayThreshold;
	}

	public void setToggleExtractorDelayThreshold(boolean toggleExtractorDelayThreshold) {
		this.toggleExtractorDelayThreshold = toggleExtractorDelayThreshold;
	}

	public boolean isStart() {
		return start;
	}

	public void setStart(boolean start) {
		this.start = start;
	}

	public int getTaskInstanceCount() {
		return taskInstanceCount;
	}

	public void setTaskInstanceCount(int taskInstanceCount) {
		this.taskInstanceCount = taskInstanceCount;
	}

	public boolean isGroup() {
		return isGroup;
	}

	public void setGroup(boolean isGroup) {
		this.isGroup = isGroup;
	}

	public String getMonitorName() {
		return monitorName;
	}

	public void setMonitorName(String monitorName) {
		this.monitorName = monitorName;
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
