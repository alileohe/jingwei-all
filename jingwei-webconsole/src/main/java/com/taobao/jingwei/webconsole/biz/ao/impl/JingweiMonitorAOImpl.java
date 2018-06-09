package com.taobao.jingwei.webconsole.biz.ao.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import jodd.util.StringUtil;
import jodd.util.Wildcard;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;

import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.TaskUtil;
import com.taobao.jingwei.common.config.ConfigManager;
import com.taobao.jingwei.common.node.monitor.MonitorNode;
import com.taobao.jingwei.common.node.monitor.MonitorParentNode;
import com.taobao.jingwei.common.node.monitor.MonitorTaskNode;
import com.taobao.jingwei.webconsole.biz.ao.JingweiMonitorAO;
import com.taobao.jingwei.webconsole.biz.manager.JingweiZkConfigManager;
import com.taobao.jingwei.webconsole.common.JingweiWebConsoleConstance;
import com.taobao.jingwei.webconsole.model.JingweiAssembledMonitor;
import com.taobao.jingwei.webconsole.model.JingweiMonitorCriteria;

public class JingweiMonitorAOImpl implements JingweiMonitorAO, JingweiWebConsoleConstance, JingWeiConstants {
	private static final Log log = LogFactory.getLog(JingweiMonitorAOImpl.JINGWEI_LOG);

	@Autowired
	private JingweiZkConfigManager jwConfigManager;

	/** 取出所有任务的是否被检测状态 */
	private String getTaskMonitorOperate(String taskName, String zkKey) {
		MonitorTaskNode monitorTaskNode = this.getMonitorTaskNode(taskName, zkKey);

		return this.getMonitorTaskOperateStr(monitorTaskNode);
	}

	private String getMonitorTaskOperateStr(MonitorTaskNode monitorTaskNode) {
		if (null == monitorTaskNode) {
			return JingweiAssembledMonitor.STOP;
		} else {
			if (monitorTaskNode.isStart()) {
				return JingweiAssembledMonitor.START;
			} else {
				return JingweiAssembledMonitor.STOP;
			}
		}
	}

	@Override
	public Set<String> getMonitorNames(String zkKey) {
		// 路径
		StringBuilder path = new StringBuilder(JINGWEI_MONITOR_ROOT_PATH).append(ZK_PATH_SEP).append(
				JINGWEI_MONITOR_MONITORS_NODE_NAME);

		// 数据
		return jwConfigManager.getZkConfigManager(zkKey).getChildDatas(path.toString(), null).keySet();
	}

	@Override
	public Set<String> getTasks(String zkKey) {
		// 路径
		StringBuilder path = new StringBuilder(JINGWEI_MONITOR_ROOT_PATH);
		path.append(ZK_PATH_SEP).append(JINGWEI_MONITOR_TASKS_NAME);

		return jwConfigManager.getZkConfigManager(zkKey).getChildDatas(path.toString(), null).keySet();
	}

	@Override
	public MonitorTaskNode getMonitorTaskNode(String taskName, String zkKey) {
		// 路径
		String path = this.getMonitorTaskNodePath(taskName);

		// 数据
		String data = jwConfigManager.getZkConfigManager(zkKey).getData(path.toString());

		if (StringUtil.isEmpty(data)) {
			return null;
		}

		MonitorTaskNode node = new MonitorTaskNode(taskName);

		try {
			node.jsonStringToNodeSelf(data);
		} catch (JSONException e) {
			log.error(e);
			return null;
		}

		return node;
	}

	@Override
	public void addOrUpdateMonitorTaskNode(MonitorTaskNode monitorTaskNode, String zkKey) throws Exception {
		String taskName = monitorTaskNode.getTaskName();

		// 路径
		String path = this.getMonitorTaskNodePath(taskName);

		// data
		String data;
		try {
			data = monitorTaskNode.toJSONString();
		} catch (JSONException e) {
			log.error(e);
			return;
		}

		jwConfigManager.getZkConfigManager(zkKey).publishOrUpdateData(path, data, true);

	}

	/**
	 * e.g. /jingwei/monitors/tasks/**task
	 * @param taskName
	 * @return
	 */
	private String getMonitorTaskNodePath(String taskName) {
		// 路径
		StringBuilder path = new StringBuilder(JINGWEI_MONITOR_ROOT_PATH);
		path.append(ZK_PATH_SEP).append(JINGWEI_MONITOR_TASKS_NAME);
		path.append(ZK_PATH_SEP).append(taskName);

		return path.toString();
	}

	@Override
	public void deleteMonitorTaskNode(String taskName, String zkKey) {
		// 路径
		String path = this.getMonitorTaskNodePath(taskName);

		try {
			jwConfigManager.getZkConfigManager(zkKey).delete(path);
		} catch (Exception e) {
			log.error(e);
		}
	}

	@Override
	public List<JingweiAssembledMonitor> getMonitors(JingweiMonitorCriteria criteria, String zkKey) {

		Set<String> tasks = this.getTasks(zkKey);

		List<JingweiAssembledMonitor> jingweiAssembledMonitors = new ArrayList<JingweiAssembledMonitor>();

		ConfigManager configManager = jwConfigManager.getZkConfigManager(zkKey);

		Set<String> allGroupTasks = getAllGroupTasks(configManager);

		Map<String, TreeSet<String>> map = this.getBusyTasks(zkKey);

		String criteriaTaskName = criteria.getTaskName();
		if (StringUtil.isBlank(criteriaTaskName)) {
			criteriaTaskName = "*";
		}

		// start还是stop
		String criteriaStatus = criteria.getStatus();

		for (String task : tasks) {

			if (!Wildcard.match(task, criteriaTaskName)) {
				continue;
			}

			MonitorTaskNode monitorTaskNode = this.getMonitorTaskNode(task, zkKey);

			if (monitorTaskNode == null) {
				continue;
			}

			if (allGroupTasks.contains(task) && !monitorTaskNode.isGroup()) {
				continue;
			}

			JingweiAssembledMonitor monitorTask = null;
			if (monitorTaskNode.isGroup()) {
				monitorTask = new JingweiAssembledMonitor(task, true);
			} else {
				monitorTask = new JingweiAssembledMonitor(task, false);
			}

			// start / stop
			String operateStr = this.getTaskMonitorOperate(task, zkKey);
			monitorTask.setOperate(operateStr);

			// monitor name
			for (String monitorName : map.keySet()) {

				if (map.get(monitorName).contains(task)) {
					monitorTask.setMonitorName(monitorName);
				}
			}

			if (criteriaStatus == null || criteriaStatus.equals("start") && monitorTaskNode.isStart()
					|| criteriaStatus.equals("stop") && !monitorTaskNode.isStart()) {
				jingweiAssembledMonitors.add(monitorTask);
			}

		}

		List<JingweiAssembledMonitor> hits = new ArrayList<JingweiAssembledMonitor>(jingweiAssembledMonitors);

		List<JingweiAssembledMonitor> afterTestHits = new ArrayList<JingweiAssembledMonitor>(jingweiAssembledMonitors);

		for (JingweiAssembledMonitor monitorTemp : hits) {
			boolean start = false;
			boolean isBuildin = false;
			String name = monitorTemp.getName();
			if (monitorTemp.isGroup()) {
				Set<String> groupTasks = getGroupTasks(name, zkKey);
				for (String taskName : groupTasks) {
					try {
						if (TaskUtil.isBuildinTaskType(configManager, name)) {
							isBuildin = true;
						}
					} catch (Exception e) {
						isBuildin = false;
					}
					if (JingweiMonitorUtil.isGroupTaskStart(configManager, name, taskName)) {
						start = true;
						break;
					}
				}
			} else {
				if (JingweiMonitorUtil.isTaskHasStartAtServer(name, configManager)) {
					start = true;
				}

				try {
					if (TaskUtil.isBuildinTaskType(configManager, name)) {
						isBuildin = true;
					}
				} catch (Exception e) {
					isBuildin = false;
				}
			}

			if (!start && isBuildin) {
				//monitorTemp.setMonitorName("");
			} else {
				afterTestHits.add(monitorTemp);
			}

		}

		Collections.sort(hits);
		return hits;
	}

	@Override
	public Set<String> getTaskNames(String zkKey) {
		// 路径
		String path = JINGWEI_TASK_ROOT_PATH;

		return jwConfigManager.getZkConfigManager(zkKey).getChildDatas(path, null).keySet();

	}

	/**
	 * e.g. /jingwei/groups/**group/tasks
	 * 
	 * @return
	 */
	@Override
	public Set<String> getGroupTasks(String groupName, String zkKey) {

		// path
		StringBuilder sb = new StringBuilder(JingWeiConstants.JINGWEI_GROUP_ROOT_PATH);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(groupName);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_GROUP_TASKS_NAME);

		// data
		Set<String> tasks = jwConfigManager.getZkConfigManager(zkKey).getChildDatas(sb.toString(), null).keySet();

		return tasks;
	}

	@Override
	public Set<String> getGroups(String zkKey) {

		StringBuilder sb = new StringBuilder(JingWeiConstants.JINGWEI_MONITOR_ROOT_PATH);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_MONITOR_GROUPS_NAME);

		Set<String> groups = this.jwConfigManager.getZkConfigManager(zkKey).getChildDatas(sb.toString(), null).keySet();
		return groups;
	}

	@Override
	public Map<String, TreeSet<String>> getBusyTasks(String zkKey) {
		Map<String, TreeSet<String>> map = new HashMap<String, TreeSet<String>>();

		String path = this.getMonitorMonitorsPath();
		Map<String, String> monitors = this.jwConfigManager.getZkConfigManager(zkKey).getChildDatas(path, null);

		for (Map.Entry<String, String> entry : monitors.entrySet()) {
			String monitorName = entry.getKey();
			String jsonStr = entry.getValue();

			MonitorNode monitorNode = new MonitorNode();
			try {
				monitorNode.jsonStringToNodeSelf(jsonStr);

				TreeSet<String> set = new TreeSet<String>(monitorNode.getTasks());

				map.put(monitorName, set);

			} catch (JSONException e) {
				log.error(e);
			}
		}

		return map;
	}

	/**
	 * e.g. /jingwei/monitors/monitors/**monitor
	 * @return
	 */
	public String getMonitorSelfPath(String monitorName) {
		StringBuilder sb = new StringBuilder(JINGWEI_MONITOR_ROOT_PATH);
		sb.append(ZK_PATH_SEP).append(JINGWEI_MONITOR_MONITORS_NODE_NAME);
		sb.append(ZK_PATH_SEP).append(monitorName);

		return sb.toString();
	}

	/**
	 * e.g. /jingwei/monitors/monitors
	 * @return
	 */
	public String getMonitorMonitorsPath() {
		StringBuilder sb = new StringBuilder(JINGWEI_MONITOR_ROOT_PATH);
		sb.append(ZK_PATH_SEP).append(JINGWEI_MONITOR_MONITORS_NODE_NAME);

		return sb.toString();
	}

	/**
	 * 获取group下所有的task名，/jingwei/groups/**group/tasks/task
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
	 * 获取所有的group名，/jingwei/groups/**group
	 * @param configManager
	 * @return
	 */
	public static Set<String> getGroups(ConfigManager configManager) {
		// path
		String path = new StringBuilder(JINGWEI_GROUP_ROOT_PATH).toString();

		return configManager.getChildDatas(path, null).keySet();
	}

	/**
	 * 获取所有的task名，/jingwei/groups/**group/tasks/**task
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

	@Override
	public MonitorParentNode getGlobalConfig(String zkKey) {
		String path = MonitorParentNode.getPath();
		String data = this.jwConfigManager.getZkConfigManager(zkKey).getData(path);
		if (StringUtil.isBlank(data)) {
			return null;
		}

		MonitorParentNode node = new MonitorParentNode();
		try {
			node.jsonStringToNodeSelf(data);
			return node;
		} catch (JSONException e) {
			log.error("get monitor global config error!", e);
			return null;
		}
	}

	@Override
	public boolean updateMonitorParentNode(MonitorParentNode node, String zkKey) {
		String path = MonitorParentNode.getPath();
		try {
			this.jwConfigManager.getZkConfigManager(zkKey).publishOrUpdateData(path, node.toJSONString(), true);
			return true;
		} catch (Exception e) {
			log.error("update global config error!", e);
			return false;
		}
	}
}
