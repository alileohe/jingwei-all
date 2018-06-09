package com.taobao.jingwei.monitor.core;

import com.taobao.jingwei.common.JingWeiUtil;
import com.taobao.jingwei.common.config.ConfigManager;
import com.taobao.jingwei.common.node.monitor.MonitorParentNode;
import com.taobao.jingwei.common.node.monitor.MonitorTaskNode;
import com.taobao.jingwei.monitor.listener.MonitorTaskScheduler;
import com.taobao.jingwei.monitor.util.BothMap;
import com.taobao.jingwei.monitor.util.GroupUtil;
import com.taobao.jingwei.monitor.util.MonitorUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @desc 1 ɨ��/jingwei/monitors/tasks/�������ӽڵ��Ӧ������ʱ����1��֪ͨcore���ɾ������2���޸ļ�����ò���
 *       2 ɨ��/jingwei/monitors/monitorsi�ڵ㣬��ʱ����ȫ�ָ澯����
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Apr 27, 2012 5:03:45 PM
 */

public class MonitorTaskScanner implements Runnable {
	private Log log = LogFactory.getLog(this.getClass());

	private final MonitorCoreThread monitorCoreThread;

	private final ConfigManager configManager;

	public MonitorTaskScanner(MonitorCoreThread monitorCoreThread) {
		this.monitorCoreThread = monitorCoreThread;
		this.configManager = this.monitorCoreThread.getConfigManager();
	}

	@Override
	public void run() {
		// ����ȫ������
		this.updateGlobalConfig();

		BothMap bothMap = MonitorUtil.getTaskGroupData(this.monitorCoreThread.getConfigManager());
		synchronized (this.monitorCoreThread.getLock()) {

			this.processGroups(bothMap.getGroupData());
			this.processTasks(bothMap.getTaskData());

			this.findNewTask(bothMap.getTaskData().keySet());
			this.findNewGroup(bothMap.getGroupData().keySet());
		}
	}

	/**
	 * ����ɨ����
	 * @param tasks
	 */
	private void processTasks(Map<String, String> tasks) {

		Set<String> orignTasks = this.monitorCoreThread.getMonitorTasks().keySet();

		for (String taskName : orignTasks) {

			if (tasks.containsKey(taskName)) {
				String configData = tasks.get(taskName);
				this.monitorCoreThread.getMonitorTasks().get(taskName).handleData(null, configData);
			}
		}

		Set<String> currentTasks = tasks.keySet();

		// ���¼�ز���
		Set<String> adds = JingWeiUtil.getAddedElement(currentTasks, orignTasks);
		Set<String> deletes = JingWeiUtil.getDeletedElements(currentTasks, orignTasks);

		Set<String> allGroupTasks = MonitorUtil.getAllGroupTasks(configManager);

		for (String taskName : adds) {
			if (allGroupTasks.contains(taskName)) {
				continue;
			}

			this.monitorCoreThread.addMonitorScheduler(taskName, tasks.get(taskName));
		}

		for (String taskName : deletes) {
			this.monitorCoreThread.deleteMonitorScheduler(taskName);
		}
	}

	/***
	 * ��ɨ����
	 * @param groups
	 */
	private void processGroups(Map<String, String> groups) {

		Set<String> orignGroups = this.monitorCoreThread.getOrignMonitorGroups().keySet();

		for (String groupName : orignGroups) {

			if (groups.containsKey(groupName)) {
				String configData = groups.get(groupName);

				HashMap<String, MonitorTaskScheduler> groupTasksSchedulers = this.monitorCoreThread
						.getOrignMonitorGroups().get(groupName);

				for (MonitorTaskScheduler scheduler : groupTasksSchedulers.values()) {
					scheduler.handleData(null, configData);
				}
			}
		}

		Set<String> addGroups = JingWeiUtil.getAddedElement(groups.keySet(), orignGroups);
		Set<String> deleteGroups = JingWeiUtil.getDeletedElements(groups.keySet(), orignGroups);

		for (Map.Entry<String, String> group : groups.entrySet()) {
			this.checkGroupTasks(group.getKey(), group.getValue());
		}

		for (String groupName : addGroups) {
			this.monitorCoreThread.addGroupMonitor(groupName, groups.get(groupName));
		}

		for (String groupName : deleteGroups) {
			log.warn("[jingwei monitor] detected delete groups : " + addGroups);
			this.monitorCoreThread.deleteGroup(groupName);
		}
	}

	/**
	 * һ��group����ӻ����������
	 * @param groupName
	 */
	private void checkGroupTasks(String groupName, String jsonStr) {

		if (!this.monitorCoreThread.getOrignMonitorGroups().containsKey(groupName)) {
			return;
		}

		Set<String> orignTasks = this.monitorCoreThread.getOrignMonitorGroups().get(groupName).keySet();
		Set<String> currentTasks = GroupUtil.getGroupTasksFromZk(this.monitorCoreThread.getConfigManager(), groupName);

		// ���¼�ز���
		Set<String> adds = JingWeiUtil.getAddedElement(currentTasks, orignTasks);
		Set<String> deletes = JingWeiUtil.getDeletedElements(currentTasks, orignTasks);

		for (String taskName : adds) {
			log.warn("[jingwei monitor] detected add tasks : " + adds + " for group : " + groupName);
			this.monitorCoreThread.addGroupMonitorTask(groupName, taskName, jsonStr);
		}

		for (String taskName : deletes) {
			log.warn("[jingwei monitor] detected delete tasks : " + deletes + " for group : " + groupName);
			this.monitorCoreThread.deleteGroupMonitorTask(groupName, taskName);
		}
	}

	private void findNewTask(Set<String> monitorTasks) {
		// e.g. /jingwei/tasks
		Set<String> currentTasks = MonitorUtil.getTasks(this.configManager);

		currentTasks.removeAll(MonitorUtil.getAllGroupTasks(configManager));

		Set<String> orignTasks = monitorTasks;

		// ����������ɾ��monitortask�ڵ�
		Set<String> adds = JingWeiUtil.getAddedElement(currentTasks, orignTasks);
		Set<String> deletes = JingWeiUtil.getDeletedElements(currentTasks, orignTasks);

		// д�ڵ� /jingwei/monitors/tasks/**task
		for (String taskName : adds) {
			log.warn("[jingwei monitor] detected create new task node  : " + taskName);
			MonitorTaskNode monitorTaskNode = new MonitorTaskNode(taskName);
			MonitorUtil.addOrUpdateMonitorTaskNode(configManager, monitorTaskNode);
			log.warn("[jingwei monitor] detected create new monitor task node  : " + taskName);
		}

		// ɾ���ڵ� /jingwei/monitors/tasks/**task
		for (String taskName : deletes) {
			log.warn("[jingwei monitor] detected delete task node  : " + taskName);
			MonitorUtil.deleteMonitorTaskNode(configManager, taskName);
			log.warn("[jingwei monitor] detected delete monitor task node  : " + taskName);
		}
	}

	private void findNewGroup(Set<String> monitorGroups) {
		Set<String> orignGroups = monitorGroups;
		Set<String> groups = MonitorUtil.getGroups(configManager);

		Set<String> addGroups = JingWeiUtil.getAddedElement(groups, orignGroups);
		Set<String> deleteGroups = JingWeiUtil.getDeletedElements(groups, orignGroups);

		for (String groupName : addGroups) {
			log.warn("[jingwei monitor] detected create new group node  : " + groupName);
			MonitorTaskNode monitorTaskNode = new MonitorTaskNode(groupName);
			monitorTaskNode.setGroup(true);
			MonitorUtil.addOrUpdateMonitorTaskNode(configManager, monitorTaskNode);
			log.warn("[jingwei monitor] detected create new monitor group node  : " + groupName);
		}

		for (String groupName : deleteGroups) {
			log.warn("[jingwei monitor] detected create new group node  : " + groupName);
			MonitorUtil.deleteMonitorTaskNode(configManager, groupName);
			log.warn("[jingwei monitor] detected delete monitor group node  : " + groupName);
		}
	}

	public MonitorCoreThread getMonitorCoreThread() {
		return monitorCoreThread;
	}

	private void updateGlobalConfig() {
		MonitorParentNode monitorParentNode = MonitorUtil.getGlobalConfig(configManager);
		if (null != monitorParentNode) {
			MonitorParentNode currentGlobalConfig = MonitorUtil.getGlobalconfig();
			currentGlobalConfig.setSmToggle(monitorParentNode.isSmToggle());
			currentGlobalConfig.setWwToggle(monitorParentNode.isWwToggle());
		}
	}

}
