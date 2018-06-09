package com.taobao.jingwei.monitor.util;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.TaskUtil;
import com.taobao.jingwei.common.config.ConfigManager;
import com.taobao.jingwei.common.node.OperateNode;
import com.taobao.jingwei.common.node.OperateNode.OperateEnum;
import com.taobao.jingwei.common.node.StatusNode;
import com.taobao.jingwei.common.node.monitor.MonitorNode;
import com.taobao.jingwei.common.node.monitor.MonitorParentNode;
import com.taobao.jingwei.common.node.monitor.MonitorTaskNode;
import com.taobao.jingwei.monitor.alert.AlertMsgManager;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @desc ������
 * 
 * @author <a href="mailto:shuohai.lhl@taobao.com">shuohailhl</a>
 * 
 * @date 2012-1-12����11:32:27
 */
final public class MonitorUtil implements JingWeiConstants {
	private static Logger log = LoggerFactory.getLogger(MonitorUtil.class);

	/** �澯����, ����һ�ٸ��澯������������ */
	private static final LinkedBlockingDeque<AlarmContext> alarmContext = new LinkedBlockingDeque<MonitorUtil.AlarmContext>(
			1000);

	private static final MonitorParentNode globalConfig = new MonitorParentNode();

	static {
		new AlarmSender(alarmContext).start();
	}

	private MonitorUtil() {
	}

	public static class AlarmContext {

		final private AlertMsgManager alertMsgManager;
		final private MonitorTaskNode monitorTaskNode;
		final private String msg;

		public AlarmContext(AlertMsgManager alertMsgManager, MonitorTaskNode monitorTaskNode, String msg) {
			this.alertMsgManager = alertMsgManager;
			this.monitorTaskNode = monitorTaskNode;
			this.msg = msg;
		}

		public AlertMsgManager getAlertMsgManager() {
			return alertMsgManager;
		}

		public MonitorTaskNode getMonitorTaskNode() {
			return monitorTaskNode;
		}

		public String getMsg() {
			return msg;
		}
	}

	/**
	 * WW�����ţ���־ �澯��Ϣ
	 * 
	 * @param alertMsgManager
	 * @param monitorTaskNode
	 * @param msg
	 */
	public static synchronized void wwAndSmsAlert(AlertMsgManager alertMsgManager, MonitorTaskNode monitorTaskNode,
			String msg) {
		AlarmContext context = new AlarmContext(alertMsgManager, monitorTaskNode, msg);
		try {
			MonitorUtil.log.warn("[jwm] add new alarm to queue : " + monitorTaskNode.getTaskName() + " ," + msg);
			MonitorUtil.alarmContext.add(context);
		} catch (Throwable e) {
			MonitorUtil.log.error("[jwm] add new alarm context error!", e);
		}
	}

	/**
	 * ��ȡstatus�ڵ�path
	 * 
	 * @param taskName ������
	 * @param hostName ������
	 * @return �����ڵ�zk·�� e.g /jingwei-v2/tasks/**task/hosts/**host/status
	 */
	public static String getStatusNodePath(String taskName, String hostName) {
		StringBuilder sb = new StringBuilder(JingWeiConstants.JINGWEI_TASK_ROOT_PATH);

		sb.append(JingWeiConstants.ZK_PATH_SEP).append(taskName);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_TASK_HOST_NODE);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(hostName);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_STATUS_NODE_NAME);

		return sb.toString();
	}

	/**
	 * ��ȡ�澯zk·��
	 * 
	 * @param taskName ������
	 * @param hostName ������
	 * @return �澯zk·�� e.g /jingwei-v2/tasks/**task/hosts/**host/alarm
	 */
	public static String getAlarmIdOrPath(String taskName, String hostName) {
		StringBuilder sb = new StringBuilder(JingWeiConstants.JINGWEI_TASK_ROOT_PATH);

		sb.append(JingWeiConstants.ZK_PATH_SEP).append(taskName);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_TASK_HOST_NODE);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(hostName);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_SCAN_ALARM_NODE);

		return sb.toString();
	}

	/**
	 * ��ȡ�����ڵ�path
	 * 
	 * @param taskName ������
	 * @param hostName ������
	 * @return �����ڵ�zk·�� e.g /jingwei-v2/tasks/**task/hosts/**host/heartBeat
	 */
	public static String getHeartBeatNodePath(String taskName, String hostName) {
		StringBuilder sb = new StringBuilder(JingWeiConstants.JINGWEI_TASK_ROOT_PATH);

		sb.append(JingWeiConstants.ZK_PATH_SEP).append(taskName);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_TASK_HOST_NODE);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(hostName);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_HOST_HEART_BEAT_NODE_NAME);

		return sb.toString();
	}

	/**
	 * ��ȡtask��host���ڵ�zk·��
	 * 
	 * @param taskName ������
	 * @return host���ڵ�zk ·�� e.g /jingwei-v2/tasks/**task/hosts
	 */
	public static String getTaskHostPath(String taskName) {
		StringBuilder sb = new StringBuilder(JingWeiConstants.JINGWEI_TASK_ROOT_PATH);

		sb.append(JingWeiConstants.ZK_PATH_SEP).append(taskName);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_TASK_HOST_NODE);

		return sb.toString();
	}

	/**
	 * ֻ�е�status��RUNNING��ʱ�� �ŷ���true��standby�Ͳ�����status�ڵ㶼����false; /jingwei-v2/tasks/**task/hosts/**host/status
	 * 
	 * @param configManager
	 * @param taskName
	 * @param hostName
	 * @return
	 */
	public static boolean checkRunning(ConfigManager configManager, String taskName, String hostName) {
		// ��ȡstatus�ڵ� �������running�ͱ���
		String statusPath = MonitorUtil.getStatusNodePath(taskName, hostName);

		// status�ڵ�
		String statusData = configManager.getData(statusPath);

		boolean isRunning = true;

		if (StringUtil.isBlank(statusData)) {
			isRunning = false;
		} else {
			StatusNode statusNode = new StatusNode();
			try {
				statusNode.jsonStringToNodeSelf(statusData);
			} catch (JSONException e) {
				log.error("[jwm] get status state for task: " + taskName + ", host: " + hostName
						+ ", status data to json string error : " + statusPath, e);
				return false;
			}

			if (StatusNode.StatusEnum.STANDBY == statusNode.getStatusEnum()) {
				return false;
			}

			if (StatusNode.StatusEnum.RUNNING != statusNode.getStatusEnum()) {
				isRunning = false;
			} else {
				isRunning = true;
			}
		}

		return isRunning;
	}

	/**
	 * ��ȡ/jingwei/tasks/**task/hosts������ host_name/status��running��host���ֵ��б�
	 * 
	 * @param configManager
	 * @param taskName
	 * @return
	 */
	public static List<String> getRunningHosts(ConfigManager configManager, String taskName) {
		String hostsPath = MonitorUtil.getTaskHostPath(taskName);
		Map<String, String> childs = configManager.getChildDatas(hostsPath, null);
		Set<String> hostNames = childs.keySet();

		if (hostNames.isEmpty()) {
			return Collections.emptyList();
		}

		List<String> runningHostList = new ArrayList<String>();

		for (String hostName : hostNames) {
			if (MonitorUtil.checkRunning(configManager, taskName, hostName)) {
				runningHostList.add(hostName);
			}
		}

		return runningHostList;
	}

	/**
	 * 
	 * @param configManager
	 * @param taskName
	 * @return ָ����task���е�ʵ���������ǵ��������ͬʱ���кö��
	 */
	public static int runningInstanceCount(ConfigManager configManager, String taskName) {
		String hostsPath = MonitorUtil.getTaskHostPath(taskName);
		Map<String, String> childs = configManager.getChildDatas(hostsPath, null);
		Set<String> hostNames = childs.keySet();

		if (hostNames.isEmpty()) {
			return 0;
		}

		int count = 0;
		for (String hostName : hostNames) {

			boolean running = MonitorUtil.checkRunning(configManager, taskName, hostName);

			if (running) {
				count++;
			}
		}

		return count;
	}

	/**
	 * // ���task��op�Ƿ�start����start�ż��� /jingwei/groups/**group/tasks/**task/operate
	 * 
	 * @param configManager
	 * @param groupName
	 * @param taskName
	 * @return ֻ����start�ŷ���<code>true</code>�����򷵻�<code>false</code>
	 */
	public static boolean isGroupTaskStartOp(ConfigManager configManager, String groupName, String taskName) {
		// ���task��op�Ƿ�start����start�ż��� /jingwei/groups/**group/tasks/**task/operate
		String path = GroupUtil.getGroupOpPath(groupName, taskName);

		String data = configManager.getData(path);

		if (StringUtil.isBlank(data)) {
			return false;
		}

		OperateNode opNode = new OperateNode();
		try {
			opNode.jsonStringToNodeSelf(data);
		} catch (JSONException e) {
			log.equals(e);
			return false;
		}

		if (opNode.getOperateEnum() == OperateEnum.NODE_STOP) {
			return false;
		}

		return true;
	}

	/**
	 * e.g. /jingwei/monitors/monitors/**monitor
	 * 
	 * @return
	 */
	public static String getMonitorSelfPath(String monitorName) {
		StringBuilder sb = new StringBuilder(JINGWEI_MONITOR_ROOT_PATH);
		sb.append(ZK_PATH_SEP).append(JINGWEI_MONITOR_MONITORS_NODE_NAME);
		sb.append(ZK_PATH_SEP).append(monitorName);

		return sb.toString();
	}

	/**
	 * e.g. /jingwei/monitors/monitors
	 * 
	 * @return
	 */
	public static String getMonitorMonitorsPath() {
		StringBuilder sb = new StringBuilder(JINGWEI_MONITOR_ROOT_PATH);
		sb.append(ZK_PATH_SEP).append(JINGWEI_MONITOR_MONITORS_NODE_NAME);

		return sb.toString();
	}

	/**
	 * e.g. /jingwei/monitors/tasks/**task
	 * 
	 * @return
	 */
	public static String getMonitorTaskPath(String taskName) {
		StringBuilder sb = new StringBuilder(JINGWEI_MONITOR_ROOT_PATH);
		sb.append(ZK_PATH_SEP).append(JINGWEI_MONITOR_TASKS_NAME);
		sb.append(ZK_PATH_SEP).append(taskName);

		return sb.toString();
	}

	/**
	 * e.g. /jingwei/monitors/tasks
	 * 
	 * @return
	 */
	public static String getMonitorTasksPath() {
		StringBuilder sb = new StringBuilder(JINGWEI_MONITOR_ROOT_PATH);
		sb.append(ZK_PATH_SEP).append(JINGWEI_MONITOR_TASKS_NAME);

		return sb.toString();
	}

	/***
	 * /jingwei/monitors/tasks/**task��Ӧ�Ľڵ�
	 * 
	 * @param taskName
	 * @param jsonStr
	 * @return <code>null</code>��ʾ�쳣
	 */
	public static MonitorTaskNode getMonitorTaskNode(String taskName, String jsonStr) {
		MonitorTaskNode monitorTaskNode = new MonitorTaskNode(taskName);
		try {
			monitorTaskNode.jsonStringToNodeSelf(jsonStr);

			return monitorTaskNode;
		} catch (JSONException e) {
			log.error("[jwm] MonitorTaskNode json string convert error, failed add monitor configration to mem ", e);
			return null;
		}
	}

	/**
	 * e.g. /jingwei/monitors/tasks �����task����group����taskData���ų�task��ֻ�в�����group���͵�task�ŷ���taskdata��
	 * 
	 * @param configManager
	 * @return
	 */
	public static BothMap getTaskGroupData(ConfigManager configManager) {
		String path = MonitorUtil.getMonitorTasksPath();

		Map<String, String> taskData = new HashMap<String, String>();
		Map<String, String> groupData = new HashMap<String, String>();

		Map<String, String> data = configManager.getChildDatas(path, null);

		for (Map.Entry<String, String> entry : data.entrySet()) {
			String taskName = entry.getKey();
			String jsonStr = entry.getValue();

			MonitorTaskNode monitorTaskNode = MonitorUtil.getMonitorTaskNode(taskName, jsonStr);

			if (null != monitorTaskNode) {
				if (monitorTaskNode.isGroup()) {
					groupData.put(taskName, jsonStr);
				} else {
					Set<String> allGroupTasks = getAllGroupTasks(configManager);
					if (!allGroupTasks.contains(taskName)) {
						taskData.put(taskName, jsonStr);
					}
				}
			}
		}

		BothMap bothMap = new BothMap();
		bothMap.setTaskData(taskData);
		bothMap.setGroupData(groupData);

		return bothMap;
	}

	/**
	 * �ǳ־ýڵ�e.g. /jingwei/monitors/monitors/**monitor
	 * 
	 * @param configManager
	 * @param taskName
	 * @param monitorName
	 * @throws Exception
	 */
	public static void publishMonitorAppendTask(ConfigManager configManager, List<String> tasks, String monitorName)
			throws Exception {
		String path = MonitorUtil.getMonitorSelfPath(monitorName);
		MonitorNode monitorNode = new MonitorNode();

		monitorNode.setTasks(tasks);
		configManager.publishOrUpdateData(path, monitorNode.toJSONString(), false);
	}

	/**
	 * ��ȡgroup�����е�task����/jingwei/groups/**group/tasks/task
	 * 
	 * @param configManager
	 * @param groupName
	 * @return
	 */
	public static Set<String> getGroupTasks(ConfigManager configManager, String groupName) {
		// path
		String path = new StringBuilder(JINGWEI_GROUP_ROOT_PATH).append(ZK_PATH_SEP).append(groupName)
				.append(ZK_PATH_SEP).append(JINGWEI_GROUP_TASKS_NAME).toString();

		return configManager.getChildDatas(path, null).keySet();
	}

	/**
	 * ��ȡ���е�group����/jingwei/groups/**group
	 * 
	 * @param configManager
	 * @return
	 */
	public static Set<String> getGroups(ConfigManager configManager) {
		// path
		String path = new StringBuilder(JINGWEI_GROUP_ROOT_PATH).toString();

		return configManager.getChildDatas(path, null).keySet();
	}

	/**
	 * ��ȡ���е�task����/jingwei/tasks/**task
	 * 
	 * @param configManager
	 * @return
	 */
	public static Set<String> getTasks(ConfigManager configManager) {
		// path
		String path = new StringBuilder(JINGWEI_TASK_ROOT_PATH).toString();

		return configManager.getChildDatas(path, null).keySet();
	}

	/**
	 * e.g. /jingwei/monitors/tasks/**task
	 * 
	 * @param configManager
	 */
	public static void addOrUpdateMonitorTaskNode(ConfigManager configManager, MonitorTaskNode monitorTaskNode) {
		String taskName = monitorTaskNode.getTaskName();

		// ·��
		String path = MonitorUtil.getMonitorTaskNodePath(taskName);

		// data
		String data;
		try {
			data = monitorTaskNode.toJSONString();
			configManager.publishOrUpdateData(path, data, true);
		} catch (Exception e) {
			log.error(e.toString());
			return;
		}
	}

	/**
	 * e.g. /jingwei/monitors/tasks/**task
	 * 
	 * @param taskName
	 * @return
	 */
	private static String getMonitorTaskNodePath(String taskName) {
		// ·��
		StringBuilder path = new StringBuilder(JINGWEI_MONITOR_ROOT_PATH);
		path.append(ZK_PATH_SEP).append(JINGWEI_MONITOR_TASKS_NAME);
		path.append(ZK_PATH_SEP).append(taskName);

		return path.toString();
	}

	/**
	 * e.g. /jingwei/monitors/tasks/**task
	 * 
	 * @param taskName
	 * @return
	 */
	public static void deleteMonitorTaskNode(ConfigManager configManager, String taskName) {
		// ·��
		String path = MonitorUtil.getMonitorTaskNodePath(taskName);
		try {
			configManager.delete(path);
		} catch (Exception e) {
			log.error(e.toString() + "taskName:" + taskName);
		}
	}

	/**
	 * ��ȡ���е�task����/jingwei/groups/**group/tasks/**task
	 * 
	 * @param configManager
	 * @return
	 */
	public static Set<String> getAllGroupTasks(ConfigManager configManager) {
		Set<String> tasks = new TreeSet<String>();
		Set<String> groups = getGroups(configManager);

		for (String groupName : groups) {
			Set<String> groupTasks = getGroupTasks(configManager, groupName);
			tasks.addAll(groupTasks);
		}

		return tasks;
	}

	public static Set<String> getSetFromCommaStr(String commaStr) {

		if (commaStr == null || commaStr.equals(StringUtil.EMPTY_STRING)) {
			return Collections.emptySet();
		}

		String[] tasks = commaStr.split(",");

		Set<String> set = new TreeSet<String>();
		for (String task : tasks) {
			set.add(task);
		}

		return set;
	}

	/**
	 * ��������̨��������start��״̬ -- taskName : {host1, host2}
	 */
	public static Map<String, TreeSet<String>> taskStartHosts(ConfigManager configManager, Set<String> taskNames) {
		if (taskNames.isEmpty()) {
			return Collections.emptyMap();
		}

		Set<String> hosts = configManager.getChildDatas(JINGWEI_SERVER_ROOT_PATH, null).keySet();

		Map<String, TreeSet<String>> map = new HashMap<String, TreeSet<String>>();
		for (String host : hosts) {
			for (String taskName : taskNames) {
				if (TaskUtil.isServerTaskStartOp(configManager, host, taskName)) {
					if (map.containsKey(taskName)) {
						map.get(taskName).add(host);
					} else {
						map.put(taskName, new TreeSet<String>());
					}
				}
			}
		}

		return map;
	}

	public static MonitorParentNode getGlobalConfig(ConfigManager configManager) {
		String path = MonitorParentNode.getPath();
		String data = configManager.getData(path);
		if (StringUtil.isBlank(data)) {
			return null;
		}

		MonitorParentNode node = new MonitorParentNode();
		try {
			node.jsonStringToNodeSelf(data);
			return node;
		} catch (JSONException e) {
			log.error("[jwm]get monitor global config error!", e);
			return null;
		}
	}

	public static MonitorParentNode getGlobalconfig() {
		return globalConfig;
	}
}
