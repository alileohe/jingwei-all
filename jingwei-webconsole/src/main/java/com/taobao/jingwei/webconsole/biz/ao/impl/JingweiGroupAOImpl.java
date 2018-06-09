package com.taobao.jingwei.webconsole.biz.ao.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import jodd.util.Wildcard;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.config.ConfigManager;
import com.taobao.jingwei.common.node.OperateNode;
import com.taobao.jingwei.common.node.OperateNode.OperateEnum;
import com.taobao.jingwei.common.node.StatusNode;
import com.taobao.jingwei.common.node.StatusNode.StatusEnum;
import com.taobao.jingwei.common.node.server.ServerNode;
import com.taobao.jingwei.server.node.GroupNode;
import com.taobao.jingwei.webconsole.biz.ao.JingweiGroupAO;
import com.taobao.jingwei.webconsole.biz.manager.JingweiZkConfigManager;
import com.taobao.jingwei.webconsole.common.JingweiWebConsoleConstance;
import com.taobao.jingwei.webconsole.model.JingweiAssembledGroup;
import com.taobao.jingwei.webconsole.model.JingweiAssembledGroup.GroupServerInfo;
import com.taobao.jingwei.webconsole.model.JingweiAssembledGroup.GroupTaskInfo;
import com.taobao.jingwei.webconsole.model.JingweiGroupCriteria;

public class JingweiGroupAOImpl implements JingweiGroupAO, JingWeiConstants, JingweiWebConsoleConstance {
	private Log log = LogFactory.getLog(this.getClass());

	@Autowired
	private JingweiZkConfigManager jwConfigManager;

	@Override
	public List<JingweiAssembledGroup> getJingweiAssembledGroups(JingweiGroupCriteria criteria, String zkKey) {
		String criteriaGroup = criteria.getGroupName();
		if (StringUtil.isBlank(criteriaGroup)) {
			criteriaGroup = new String("*");
		}

		Set<String> groupNames = this.getGroups(zkKey);

		List<JingweiAssembledGroup> jingweiAssembledGroups = new ArrayList<JingweiAssembledGroup>();

		Map<String, TreeSet<String>> serverGroups = this.getServerGroup(zkKey);

		// 迭代group
		for (String groupName : groupNames) {

			if (Wildcard.match(groupName, criteriaGroup)) {
				JingweiAssembledGroup jingweiAssembledGroup = new JingweiAssembledGroup();
				jingweiAssembledGroup.setGroupName(groupName);

				// group server infos
				Set<GroupServerInfo> groupServerInfos = this.getGroupServerInfos(serverGroups, groupName, zkKey);
				jingweiAssembledGroup.setGroupServerInfos(groupServerInfos);

				// group task infos
				Set<GroupTaskInfo> groupTaskInfos = this.getGroupTaskInfos(groupName, zkKey);
				jingweiAssembledGroup.setGroupTaskInfos(groupTaskInfos);

				// 是否支持批量修改
				GroupNode groupNode = null;
				try {
					groupNode = this.getGroupNode(groupName, zkKey);
					jingweiAssembledGroup.setSupportBatchModify(groupNode.getSupportBatchUpdate());
				} catch (JSONException e) {
					jingweiAssembledGroup.setSupportBatchModify(false);
					e.printStackTrace();
				}

				jingweiAssembledGroups.add(jingweiAssembledGroup);
			}
		}

		Collections.sort(jingweiAssembledGroups);

		return jingweiAssembledGroups;
	}

	@Override
	public Set<String> getGroups(String zkKey) {
		// path
		String path = new StringBuilder(JINGWEI_GROUP_ROOT_PATH).toString();

		// data
		return this.getJwConfigManager().getZkConfigManager(zkKey).getChildDatas(path, null).keySet();
	}

	@Override
	public Set<GroupTaskInfo> getGroupTaskInfos(String groupName, String zkKey) {
		Set<String> tasks = this.getTasks(groupName, zkKey);

		Set<GroupTaskInfo> groupTaskInfos = new TreeSet<GroupTaskInfo>();

		for (String taskName : tasks) {

			// 运行task的server
			Set<String> serverNames = this.getRunServers(taskName, zkKey);
			if (serverNames == null || serverNames.isEmpty()) {
				GroupTaskInfo groupTaskInfo = new GroupTaskInfo(taskName);
				groupTaskInfo.setServerName(DEFAULT_SHOW_STATUS);
				groupTaskInfo.setStatus(DEFAULT_SHOW_STATUS);
				String operate = getOperate(groupName, taskName, zkKey);
				groupTaskInfo.setOperate(operate);
				groupTaskInfos.add(groupTaskInfo);
			} else {
				for (String serverName : serverNames) {
					GroupTaskInfo groupTaskInfo = new GroupTaskInfo(taskName);
					groupTaskInfo.setServerName(serverName);
					groupTaskInfo.setStatus(StatusEnum.RUNNING.getStatusString().toUpperCase());
					String operate = getOperate(groupName, taskName, zkKey);
					groupTaskInfo.setOperate(operate);
					groupTaskInfos.add(groupTaskInfo);
				}
			}
		}

		return groupTaskInfos;
	}

	@Override
	public Set<GroupTaskInfo> getGroupTaskInfos(String server, String groupName, String zkKey) {
		Set<String> tasks = this.getTasks(groupName, zkKey);

		Set<GroupTaskInfo> groupTaskInfos = new TreeSet<GroupTaskInfo>();

		for (String taskName : tasks) {
			// 运行task的server
			Set<String> serverNames = this.getRunServers(taskName, zkKey);
			if (serverNames == null || serverNames.isEmpty()) {
				GroupTaskInfo groupTaskInfo = new GroupTaskInfo(taskName);
				groupTaskInfo.setServerName(DEFAULT_SHOW_STATUS);
				groupTaskInfo.setStatus(DEFAULT_SHOW_STATUS);
				String operate = getOperate(groupName, taskName, zkKey);
				groupTaskInfo.setOperate(operate);
				groupTaskInfos.add(groupTaskInfo);
			} else {
				for (String serverName : serverNames) {
					GroupTaskInfo groupTaskInfo = new GroupTaskInfo(taskName);
					groupTaskInfo.setServerName(serverName);
					groupTaskInfo.setStatus(StatusEnum.RUNNING.getStatusString().toUpperCase());
					String operate = getOperate(groupName, taskName, zkKey);
					groupTaskInfo.setOperate(operate);
					groupTaskInfos.add(groupTaskInfo);
				}
			}
		}

		return groupTaskInfos;
	}

	// private String getRunServer(String server, String taskName, String zkKey) {
	// StringBuilder path = new StringBuilder(JingWeiConstants.JINGWEI_TASK_ROOT_PATH);
	//
	// path.append(JingWeiConstants.ZK_PATH_SEP).append(taskName);
	// path.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_TASK_HOST_NODE);
	//
	// // 获取任务运行的host
	// Set<String> hosts = this.jwConfigManager.getZkConfigManager(zkKey).getChildDatas(path.toString(), null)
	// .keySet();
	//
	// if (hosts.isEmpty()) {
	// return null;
	// }
	//
	// for (String hostName : hosts) {
	// try {
	// if (this.isRunning(this.jwConfigManager.getZkConfigManager(zkKey), taskName, hostName)) {
	// if (server.equals(hostName)) {
	// return hostName;
	// }
	// }
	// } catch (JSONException e) {
	// log.error(e);
	// }
	// }
	//
	// return null;
	// }

	@Override
	public Set<GroupServerInfo> getGroupServerInfos(Map<String, TreeSet<String>> serverGroups, String groupName,
			String zkKey) {

		ConfigManager configManager = this.getJwConfigManager().getZkConfigManager(zkKey);
		// 所有的server

		Set<GroupServerInfo> groupServerInfos = new TreeSet<GroupServerInfo>();
		TreeSet<String> serverNames = serverGroups.get(groupName);
		if (serverNames == null) {
			serverNames = new TreeSet<String>();
			serverGroups.put(groupName, serverNames);
		}
		for (String serverName : serverNames) {
			GroupServerInfo groupServerInfo = new GroupServerInfo(serverName);
			groupServerInfo.setStatus(this.getServerStatus(configManager, serverName));

			groupServerInfos.add(groupServerInfo);
		}

		return groupServerInfos;
	}

	public JingweiZkConfigManager getJwConfigManager() {
		return jwConfigManager;
	}

	@Override
	public Map<String, TreeSet<String>> getServerGroup(String zkKey) {
		ConfigManager configManager = this.getJwConfigManager().getZkConfigManager(zkKey);

		// 所有的server
		Set<String> serverNames = this.getServerNames(configManager);

		Map<String, TreeSet<String>> groupMapServers = new HashMap<String, TreeSet<String>>();

		for (String serverName : serverNames) {
			String path = new StringBuilder(JINGWEI_SERVER_ROOT_PATH).append(ZK_PATH_SEP).append(serverName).toString();

			// data
			String data = configManager.getData(path);

			if (StringUtil.isBlank(data)) {
				continue;
			}

			ServerNode serverNode = new ServerNode();
			try {
				serverNode.jsonStringToNodeSelf(data);
			} catch (JSONException e) {
				log.error(e);
				continue;
			}

			List<String> groups = serverNode.getGroups();

			for (String groupName : groups) {
				if (groupMapServers.containsKey(groupName)) {
					groupMapServers.get(groupName).add(serverName);
				} else {
					TreeSet<String> hosts = new TreeSet<String>();
					hosts.add(serverName);
					groupMapServers.put(groupName, hosts);
				}
			}
		}

		return groupMapServers;
	}

	@Override
	public Set<String> getTasks(String groupName, String zkKey) {
		// path
		String path = new StringBuilder(JINGWEI_GROUP_ROOT_PATH).append(ZK_PATH_SEP).append(groupName)
				.append(ZK_PATH_SEP).append(JINGWEI_GROUP_TASKS_NAME).toString();

		return this.getJwConfigManager().getZkConfigManager(zkKey).getChildDatas(path, null).keySet();
	}

	// /**
	// * @param taskName 任务名
	// * @param zkKey
	// * @return 运行这个任务的server名<code>null</code>表示没有机器运行这个任务
	// */
	// private String getRunServer(String taskName, String zkKey) {
	// StringBuilder path = new StringBuilder(JingWeiConstants.JINGWEI_TASK_ROOT_PATH);
	//
	// path.append(JingWeiConstants.ZK_PATH_SEP).append(taskName);
	// path.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_TASK_HOST_NODE);
	//
	// // 获取任务运行的host
	// Set<String> hosts = this.jwConfigManager.getZkConfigManager(zkKey).getChildDatas(path.toString(), null)
	// .keySet();
	//
	// if (hosts.isEmpty()) {
	// return null;
	// }
	//
	// for (String hostName : hosts) {
	// try {
	// if (this.isRunning(this.jwConfigManager.getZkConfigManager(zkKey), taskName, hostName)) {
	// return hostName;
	// }
	// } catch (JSONException e) {
	// log.error(e);
	// }
	// }
	//
	// return null;
	// }

	private Set<String> getRunServers(String taskName, String zkKey) {
		StringBuilder path = new StringBuilder(JingWeiConstants.JINGWEI_TASK_ROOT_PATH);

		path.append(JingWeiConstants.ZK_PATH_SEP).append(taskName);
		path.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_TASK_HOST_NODE);

		Set<String> servers = new TreeSet<String>();
		// 获取任务运行的host
		Set<String> hosts = this.jwConfigManager.getZkConfigManager(zkKey).getChildDatas(path.toString(), null)
				.keySet();

		if (hosts.isEmpty()) {
			return servers;
		}

		for (String hostName : hosts) {
			try {
				if (this.isRunning(this.jwConfigManager.getZkConfigManager(zkKey), taskName, hostName)) {
					servers.add(hostName);
				}
			} catch (JSONException e) {
				log.error(e);
			}
		}

		return servers;
	}

	/**
	 * e.g. /jingwei/tasks/**task/t-locks子节点
	 * 
	 * @param configManager
	 * @param taskName
	 * @return
	 */
	public Map<String, String> groupTaskLocksCount(ConfigManager configManager, String taskName) {
		String path = this.getTaskLocksPath(taskName);
		return configManager.getChildDatas(path, null);
	}

	/**
	 * e.g. /jingwei/tasks/**task/t-locks子节点
	 * 
	 * @param groupName
	 * @param taskName
	 * @return
	 */
	private String getTaskLocksPath(String taskName) {
		StringBuilder path = new StringBuilder(JINGWEI_TASK_ROOT_PATH);

		path.append(ZK_PATH_SEP).append(taskName);
		path.append(ZK_PATH_SEP).append(JINGWEI_INSTANCE_TASK_LOCKS_NODE_NAME);

		return path.toString();
	}

	/**
	 * 判断是否存在status节点
	 * 
	 * @param configManager zk管理器
	 * @param taskName 任务名
	 * @param hostName 主机名
	 * @return <code>false</code> 任务不是running状态，<code>true</code>任务是running状态 e.g. /jingwei/tasks/**task/hosts/**host/status
	 * @throws JSONException
	 */
	private boolean isRunning(ConfigManager configManager, String taskName, String hostName) throws JSONException {
		// path
		StringBuilder sb = new StringBuilder(JingWeiConstants.JINGWEI_TASK_ROOT_PATH);

		sb.append(JingWeiConstants.ZK_PATH_SEP).append(taskName);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_TASK_HOST_NODE);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(hostName);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_STATUS_NODE_NAME);

		String path = sb.toString();

		// data
		String data = configManager.getData(path);

		if (StringUtil.isBlank(data)) {
			return false;
		}

		StatusNode statusNode = new StatusNode();
		statusNode.jsonStringToNodeSelf(data);

		if (statusNode.getStatusEnum() == StatusEnum.RUNNING) {
			return true;
		}

		return false;
	}

	/**
	 * 获取zk所有的server名
	 * 
	 * @param zkKey
	 * @return <code>empty set</code>如果zk上没有server
	 */
	private Set<String> getServerNames(ConfigManager configManager) {
		// path
		String path = new StringBuilder(JingWeiConstants.JINGWEI_SERVER_ROOT_PATH).toString();

		// data
		return configManager.getChildDatas(path, null).keySet();
	}

	private String getServerStatus(ConfigManager configManager, String serverName) {
		// path
		String path = new StringBuilder(JINGWEI_SERVER_ROOT_PATH).append(ZK_PATH_SEP).append(serverName)
				.append(ZK_PATH_SEP).append(JINGWEI_STATUS_NODE_NAME).toString();

		boolean exists = configManager.exists(path);
		if (exists) {
			return StatusEnum.RUNNING.getStatusString().toUpperCase();
		} else {
			return DEFAULT_SHOW_STATUS;
		}
	}

	@Override
	public void addGroup(String groupName, String zkKey) throws Exception {
		// path
		String path = new StringBuilder(JINGWEI_GROUP_ROOT_PATH).append(ZK_PATH_SEP).append(groupName).toString();

		// 创建节点
		this.getJwConfigManager().getZkConfigManager(zkKey).publishOrUpdateData(path, null, true);
	}

	@Override
	public void removeGroup(String groupName, String zkKey) throws Exception {
		// path
		String path = new StringBuilder(JINGWEI_GROUP_ROOT_PATH).append(ZK_PATH_SEP).append(groupName).toString();

		// 删除节点
		this.getJwConfigManager().getZkConfigManager(zkKey).delete(path);
	}

	@Override
	public void updateTaskSetting(String groupName, Set<String> taskNames, String zkKey) throws Exception {

		// 当前的task
		Set<String> currentTasks = this.getTasks(groupName, zkKey);

		// 增加的task
		for (String taskName : currentTasks) {
			if (!taskNames.contains(taskName)) {
				// 添加 e.g. /jingwei/groups/**group/tasks/**task
				addTask(groupName, taskName, zkKey);
			}
		}

		// 减少的task
		for (String taskName : taskNames) {
			if (!currentTasks.contains(taskName)) {
				// 删除 e.g. /jingwei/groups/**group/tasks/**task
				// path
				removeTask(groupName, taskName, zkKey);
			}
		}
	}

	@Override
	public void updateHostSetting(String groupName, Set<String> hostNames, String zkKey) {
		// too boring, imp by server modlue
	}

	/**
	 * e.g. /jingwei/groups/**group/tasks/**task
	 * 
	 * @param taskName
	 * @return
	 */
	private String getGroupTaskPath(String groupName, String taskName) {
		String path = new StringBuilder(JINGWEI_GROUP_ROOT_PATH).append(ZK_PATH_SEP).append(groupName)
				.append(ZK_PATH_SEP).append(JINGWEI_GROUP_TASKS_NAME).append(ZK_PATH_SEP).append(taskName).toString();
		return path;
	}

	/**
	 * e.g. /jingwei/groups/**group/tasks/**task/operate
	 * 
	 * @return
	 */
	private String getOperate(String groupName, String taskName, String zkKey) {
		String parentPath = this.getGroupTaskPath(groupName, taskName);
		String path = new StringBuilder(parentPath).append(ZK_PATH_SEP).append(JINGWEI_OPERATE_NODE_NAME).toString();

		String data = this.getJwConfigManager().getZkConfigManager(zkKey).getData(path);

		if (StringUtil.isBlank(data)) {
			return DEFAULT_SHOW_STATUS;
		}

		OperateNode opNode = new OperateNode();
		try {
			opNode.jsonStringToNodeSelf(data);
		} catch (JSONException e) {
			log.error(e);
			return DEFAULT_SHOW_STATUS;
		}

		if (opNode.getOperateEnum() != null) {
			return opNode.getOperateEnum().getOperateString().toUpperCase();
		} else {
			return DEFAULT_SHOW_STATUS;
		}

	}

	@Override
	public Set<String> getTasks(String zkKey) {
		// path
		String path = new StringBuilder(JINGWEI_TASK_ROOT_PATH).toString();

		return this.getJwConfigManager().getZkConfigManager(zkKey).getChildDatas(path, null).keySet();
	}

	@Override
	public void removeTask(String groupName, String taskName, String zkKey) throws Exception {
		// 添加 e.g. /jingwei/groups/**group/tasks/**task
		// path
		String path = this.getGroupTaskPath(groupName, taskName);

		this.getJwConfigManager().getZkConfigManager(zkKey).delete(path);

	}

	@Override
	public void addTask(String groupName, String taskName, String zkKey) throws Exception {
		// path
		String path = this.getGroupTaskPath(groupName, taskName);

		this.getJwConfigManager().getZkConfigManager(zkKey).publishOrUpdateData(path, null, true);
	}

	@Override
	public void updateTaskOperate(String groupName, String taskName, OperateEnum operateEnum, String zkKey)
			throws JSONException, Exception {
		// path
		String parent = this.getGroupTaskPath(groupName, taskName);

		String path = new StringBuilder(parent).append(ZK_PATH_SEP).append(JINGWEI_OPERATE_NODE_NAME).toString();

		// data
		OperateNode opNode = new OperateNode();
		opNode.setOperateEnum(operateEnum);

		this.getJwConfigManager().getZkConfigManager(zkKey).publishOrUpdateData(path, opNode.toJSONString(), true);

	}

	@Override
	public Set<String> getAllGroupTasks(String zkKey) {
		Set<String> tasks = new TreeSet<String>();
		Set<String> groups = this.getGroups(zkKey);

		for (String groupName : groups) {
			tasks.addAll(this.getTasks(groupName, zkKey));
		}

		return tasks;
	}

	@Override
	public void updateGroupNode(String groupName, GroupNode groupNode, String zkKey) throws JSONException, Exception {
		// path
		String path = GroupNode.getDataIdOrNodePath(groupName);

		this.getJwConfigManager().getZkConfigManager(zkKey).publishOrUpdateData(path, groupNode.toJSONString(), true);
	}

	// @Override
	// public List<JingweiAssembledGroup> getGroupsByName(
	// List<String> groupNameList, String zkKey) {
	//
	// List<JingweiAssembledGroup> jingweiAssembledGroups = new ArrayList<JingweiAssembledGroup>();
	// Map<String, TreeSet<String>> serverGroups = this.getServerGroup(zkKey);
	// // 迭代group
	// for (String groupName : groupNameList) {
	//
	// JingweiAssembledGroup jingweiAssembledGroup = new JingweiAssembledGroup();
	// jingweiAssembledGroup.setGroupName(groupName);
	//
	// // group server infos
	// Set<GroupServerInfo> groupServerInfos = this.getGroupServerInfos(serverGroups, groupName, zkKey);
	// jingweiAssembledGroup.setGroupServerInfos(groupServerInfos);
	//
	// // group task infos
	// Set<GroupTaskInfo> groupTaskInfos = this.getGroupTaskInfos(groupName, zkKey);
	// jingweiAssembledGroup.setGroupTaskInfos(groupTaskInfos);
	//
	// jingweiAssembledGroups.add(jingweiAssembledGroup);
	//
	// }
	// Collections.sort(jingweiAssembledGroups);
	// return jingweiAssembledGroups;
	// }

	@Override
	public GroupNode getGroupNode(String groupName, String zkKey) throws JSONException {
		// path
		String path = GroupNode.getDataIdOrNodePath(groupName);

		String data = this.getJwConfigManager().getZkConfigManager(zkKey).getData(path);

		GroupNode node = new GroupNode();
		// 兼容原来没有内容的情况
		if (StringUtil.isBlank(data)) {
			return node;
		}

		node.jsonStringToNodeSelf(data);

		return node;
	}
}
