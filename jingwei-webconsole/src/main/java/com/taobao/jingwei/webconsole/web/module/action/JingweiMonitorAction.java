package com.taobao.jingwei.webconsole.web.module.action;

import java.util.Set;

import jodd.util.StringUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.node.monitor.MonitorParentNode;
import com.taobao.jingwei.common.node.monitor.MonitorTaskNode;
import com.taobao.jingwei.webconsole.biz.ao.JingweiMonitorAO;
import com.taobao.jingwei.webconsole.biz.ao.impl.JingweiMonitorAOImpl;
import com.taobao.jingwei.webconsole.model.JingweiMonitorTask;

public class JingweiMonitorAction implements JingWeiConstants {
	private static final Log log = LogFactory.getLog(JingweiMonitorAOImpl.JINGWEI_LOG);

	/** ������������������ָ��� */
	private static final String COMMA_SEP = ",";

	@Autowired
	private JingweiMonitorAO jwMonitorAO;

	public void doAddMonitorTask(Context context, Navigator navigator, @Param(name = "host") String zkKey,
			@Param(name = "taskName") String taskName, @Param(name = "scanStatsPeriod") int scanStatsPeriod,
			@Param(name = "thresholdFrozenPeriod") int thresholdFrozenPeriod,
			@Param(name = "thresholdToggle") boolean thresholdToggle,
			@Param(name = "scanAlarmPeriod") int scanAlarmPeriod,
			@Param(name = "alarmFrozenPeriod") int alarmFrozenPeriod, @Param(name = "alarmToggle") boolean alarmToggle,
			@Param(name = "scanHeartBeatPeriod") int scanHeartBeatPeriod,
			@Param(name = "heartBeatFrozenPeriod") int heartBeatFrozenPeriod,
			@Param(name = "heartBeatToggle") boolean heartBeatToggle,
			@Param(name = "tpsUpperInsertThreshold") int tpsUpperInsertThreshold,
			@Param(name = "toggleTpsUpperInsertThreshold") boolean toggleTpsUpperInsertThreshold,
			@Param(name = "tpsLowerInsertThreshold") int tpsLowerInsertThreshold,
			@Param(name = "toggleTpsLowerInsertThreshold") boolean toggleTpsLowerInsertThreshold,
			@Param(name = "tpsUpperUpdateThreshold") int tpsUpperUpdateThreshold,
			@Param(name = "toggleTpsUpperUpdateThreshold") boolean toggleTpsUpperUpdateThreshold,
			@Param(name = "tpsLowerUpdateThreshold") int tpsLowerUpdateThreshold,
			@Param(name = "toggleTpsLowerUpdateThreshold") boolean toggleTpsLowerUpdateThreshold,
			@Param(name = "tpsUpperDeleteThreshold") int tpsUpperDeleteThreshold,
			@Param(name = "toggleTpsUpperDeleteThreshold") boolean toggleTpsUpperDeleteThreshold,
			@Param(name = "tpsLowerDeleteThreshold") int tpsLowerDeleteThreshold,
			@Param(name = "toggleTpsLowerDeleteThreshold") boolean toggleTpsLowerDeleteThreshold,
			@Param(name = "insertDelayThreshold") int insertDelayThreshold,
			@Param(name = "toggleInsertDelayThreshold") boolean toggleInsertDelayThreshold,
			@Param(name = "updateDelayThreshold") int updateDelayThreshold,
			@Param(name = "toggleUpdateDelayThreshold") boolean toggleUpdateDelayThreshold,
			@Param(name = "deleteDelayThreshold") int deleteDelayThreshold,
			@Param(name = "toggleDeleteDelayThreshold") boolean toggleDeleteDelayThreshold,
			@Param(name = "extractorDelayThreshold") int extractorDelayThreshold,
			@Param(name = "toggleExtractorDelayThreshold") boolean toggleExtractorDelayThreshold,
			@Param(name = "insertExceptionThreshold") int insertExceptionThreshold,
			@Param(name = "toggleInsertExceptionThreshold") boolean toggleInsertExceptionThreshold,
			@Param(name = "updateExceptionThreshold") int updateExceptionThreshold,
			@Param(name = "toggleUpdateExceptionThreshold") boolean toggleUpdateExceptionThreshold,
			@Param(name = "deleteExceptionThreshold") int deleteExceptionThreshold,
			@Param(name = "toggleDeleteExceptionThreshold") boolean toggleDeleteExceptionThreshold,
			@Param(name = "smsAlertUsers") String smsAlertUsers, @Param(name = "wwAlertUsers") String wwAlertUsers,
			@Param(name = "isGroup") boolean isGroup, @Param(name = "taskInstanceCount") int taskInstanceCount) {

		if (StringUtil.isBlank(taskName)) {
			context.put("messages", "������Ϊ�գ���������������");
			return;
		}

		String[] tasks = taskName.split(COMMA_SEP);

		for (String task : tasks) {

			MonitorTaskNode monitorTaskNode = new MonitorTaskNode(task);

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

			// �߾�֪ͨ��
			monitorTaskNode.setSmsAlertUsers(JingweiMonitorTask.getStringList(smsAlertUsers,
					JingweiMonitorTask.SEP_COMMA));
			monitorTaskNode.setWwAlertUsers(JingweiMonitorTask
					.getStringList(wwAlertUsers, JingweiMonitorTask.SEP_COMMA));

			monitorTaskNode.setTaskInstanceCount(taskInstanceCount);

			MonitorTaskNode node = jwMonitorAO.getMonitorTaskNode(taskName, zkKey);
			if (null != node) {
				context.put("messages", "���ʧ��,�����Ѿ����ڣ�");
				return;
			}

			try {

				if (isGroup) {
					monitorTaskNode.setGroup(true);
				}

				jwMonitorAO.addOrUpdateMonitorTaskNode(monitorTaskNode, zkKey);

			} catch (Exception e) {
				context.put("messages", "���ʧ�ܣ�");
				e.printStackTrace();
				return;
			}
			context.put("messages", "��ӳɹ���");
		}
	}

	public void doDeleteMonitorTask(Context context, Navigator navigator, @Param(name = "host") String zkKey,
			@Param(name = "taskName") String taskName) {
		jwMonitorAO.deleteMonitorTaskNode(taskName, zkKey);
		navigator.redirectTo("jingweiModule").withTarget("jingweiMonitors.vm").withParameter("host", zkKey);
	}

	public void doViewMonitorTask(Context context, Navigator navigator, @Param(name = "host") String zkKey,
			@Param(name = "monitorName") String monitorName, @Param(name = "taskName") String taskName) {
		jwMonitorAO.getMonitorTaskNode(taskName, zkKey);

		navigator.redirectTo("jingweiModule").withTarget("jingweiMonitors.vm").withParameter("host", zkKey);
	}

	public void doUpdateMonitorTask(Context context, Navigator navigator, @Param(name = "host") String zkKey,
			@Param(name = "taskName") String taskName, @Param(name = "scanStatsPeriod") int scanStatsPeriod,
			@Param(name = "thresholdFrozenPeriod") int thresholdFrozenPeriod,
			@Param(name = "thresholdToggle") boolean thresholdToggle,
			@Param(name = "scanAlarmPeriod") int scanAlarmPeriod,
			@Param(name = "alarmFrozenPeriod") int alarmFrozenPeriod, @Param(name = "alarmToggle") boolean alarmToggle,
			@Param(name = "scanHeartBeatPeriod") int scanHeartBeatPeriod,
			@Param(name = "heartBeatFrozenPeriod") int heartBeatFrozenPeriod,
			@Param(name = "heartBeatToggle") boolean heartBeatToggle,
			@Param(name = "tpsUpperInsertThreshold") int tpsUpperInsertThreshold,
			@Param(name = "toggleTpsUpperInsertThreshold") boolean toggleTpsUpperInsertThreshold,
			@Param(name = "tpsLowerInsertThreshold") int tpsLowerInsertThreshold,
			@Param(name = "toggleTpsLowerInsertThreshold") boolean toggleTpsLowerInsertThreshold,
			@Param(name = "tpsUpperUpdateThreshold") int tpsUpperUpdateThreshold,
			@Param(name = "toggleTpsUpperUpdateThreshold") boolean toggleTpsUpperUpdateThreshold,
			@Param(name = "tpsLowerUpdateThreshold") int tpsLowerUpdateThreshold,
			@Param(name = "toggleTpsLowerUpdateThreshold") boolean toggleTpsLowerUpdateThreshold,
			@Param(name = "tpsUpperDeleteThreshold") int tpsUpperDeleteThreshold,
			@Param(name = "toggleTpsUpperDeleteThreshold") boolean toggleTpsUpperDeleteThreshold,
			@Param(name = "tpsLowerDeleteThreshold") int tpsLowerDeleteThreshold,
			@Param(name = "toggleTpsLowerDeleteThreshold") boolean toggleTpsLowerDeleteThreshold,
			@Param(name = "insertDelayThreshold") int insertDelayThreshold,
			@Param(name = "toggleInsertDelayThreshold") boolean toggleInsertDelayThreshold,
			@Param(name = "updateDelayThreshold") int updateDelayThreshold,
			@Param(name = "toggleUpdateDelayThreshold") boolean toggleUpdateDelayThreshold,
			@Param(name = "deleteDelayThreshold") int deleteDelayThreshold,
			@Param(name = "toggleDeleteDelayThreshold") boolean toggleDeleteDelayThreshold,
			@Param(name = "extractorDelayThreshold") int extractorDelayThreshold,
			@Param(name = "toggleExtractorDelayThreshold") boolean toggleExtractorDelayThreshold,
			@Param(name = "insertExceptionThreshold") int insertExceptionThreshold,
			@Param(name = "toggleInsertExceptionThreshold") boolean toggleInsertExceptionThreshold,
			@Param(name = "updateExceptionThreshold") int updateExceptionThreshold,
			@Param(name = "toggleUpdateExceptionThreshold") boolean toggleUpdateExceptionThreshold,
			@Param(name = "deleteExceptionThreshold") int deleteExceptionThreshold,
			@Param(name = "toggleDeleteExceptionThreshold") boolean toggleDeleteExceptionThreshold,
			@Param(name = "smsAlertUsers") String smsAlertUsers, @Param(name = "wwAlertUsers") String wwAlertUsers,
			@Param(name = "isGroup") boolean isGroup, @Param(name = "taskInstanceCount") int taskInstanceCount,
			@Param(name = "positionAlarmThreshold") int positionAlarmThreshold,
			@Param(name = "positionFrozenPeriod") int positionFrozenPeriod,
			@Param(name = "positionToggle") boolean positionToggle,
			@Param(name = "positionDelayAlarmThreshold") int positionDelayAlarmThreshold,
			@Param(name = "isWwToggle") boolean isWwToggle, @Param(name = "isSmToggle") boolean isSmToggle) {
		MonitorTaskNode monitorTaskNode = new MonitorTaskNode(taskName);

		MonitorTaskNode zkNode = this.jwMonitorAO.getMonitorTaskNode(taskName, zkKey);

		if (null == zkNode) {
			context.put("messages", "�޸�ʧ�ܣ�");
			return;
		}

		// ��ʼ�رձ�־
		monitorTaskNode.setStart(zkNode.isStart());

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

		// �߾�֪ͨ��
		monitorTaskNode.setSmsAlertUsers(JingweiMonitorTask.getStringList(smsAlertUsers, JingweiMonitorTask.SEP_COMMA));
		monitorTaskNode.setWwAlertUsers(JingweiMonitorTask.getStringList(wwAlertUsers, JingweiMonitorTask.SEP_COMMA));

		monitorTaskNode.setTaskInstanceCount(taskInstanceCount);

		// λ��
		monitorTaskNode.setPositionAlarmThreshold(positionAlarmThreshold);
		monitorTaskNode.setPositionFrozenPeriod(positionFrozenPeriod);
		monitorTaskNode.setPositionToggle(positionToggle);
		monitorTaskNode.setPositionDelayAlarmThreshold(positionDelayAlarmThreshold);

		// ww sm�澯����
		monitorTaskNode.setWwToggle(isWwToggle);
		monitorTaskNode.setSmToggle(isSmToggle);

		try {

			if (isGroup) {
				monitorTaskNode.setGroup(true);
			}
			jwMonitorAO.addOrUpdateMonitorTaskNode(monitorTaskNode, zkKey);

		} catch (Exception e) {
			context.put("messages", "�޸�ʧ�ܣ�");
			log.error(e);
			return;
		}

		context.put("messages", "�޸ĳɹ���");
	}

	public void doSelectMonitorTasks(Context context, @Param(name = "monitorName") String monitorName,
			@Param(name = "host") String zkKey) {

		Set<String> tasks = jwMonitorAO.getTaskNames(zkKey);

		Set<String> monitoredTasks = jwMonitorAO.getTasks(zkKey);

		tasks.removeAll(monitoredTasks);

		context.put("tasks", tasks);

		context.put("monitoredTasks", monitoredTasks);

		context.put("monitorName", monitorName);
	}

	public void doStartMonitorTask(Context context, Navigator navigator, @Param(name = "host") String zkKey,
			@Param(name = "taskName") String taskName, @Param(name = "monitorName") String monitorName,
			@Param(name = "taskStatus") String taskStatus, @Param(name = "taskNameCrireria") String taskNameCrireria,
			@Param(name = "page") String page, @Param(name = "pageSize") String pageSize) {
		this.updateTaskMonitorNodeStart(taskName, true, zkKey);
	}

	public void doStopMonitorTask(Context context, Navigator navigator, @Param(name = "host") String zkKey,
			@Param(name = "taskName") String taskName, @Param(name = "monitorName") String monitorName,
			@Param(name = "taskStatus") String taskStatus, @Param(name = "taskNameCrireria") String taskNameCrireria,
			@Param(name = "page") String page, @Param(name = "pageSize") String pageSize) {
		this.updateTaskMonitorNodeStart(taskName, false, zkKey);
	}

	public void doUpdateGlobalConfig(Context context, Navigator navigator, @Param(name = "host") String zkKey,
			@Param(name = "isWwToggle") boolean isWwToggle, @Param(name = "isSmToggle") boolean isSmToggle) {
		MonitorParentNode node = new MonitorParentNode();
		node.setWwToggle(isWwToggle);
		node.setSmToggle(isSmToggle);

		boolean success = this.jwMonitorAO.updateMonitorParentNode(node, zkKey);

		if (!success) {
			context.put("messages", "�޸�ʧ�ܣ�");
		}
	}

	private void updateTaskMonitorNodeStart(String taskName, boolean start, String zkKey) {
		MonitorTaskNode monitorTaskNode = jwMonitorAO.getMonitorTaskNode(taskName, zkKey);
		monitorTaskNode.setStart(start);

		try {
			jwMonitorAO.addOrUpdateMonitorTaskNode(monitorTaskNode, zkKey);
		} catch (Exception e) {
			log.error(e);
			return;
		}
	}

}
