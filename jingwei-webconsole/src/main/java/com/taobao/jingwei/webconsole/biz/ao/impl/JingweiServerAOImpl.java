package com.taobao.jingwei.webconsole.biz.ao.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import jodd.util.Wildcard;

import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.node.OperateNode;
import com.taobao.jingwei.common.node.OperateNode.OperateEnum;
import com.taobao.jingwei.common.node.StatusNode;
import com.taobao.jingwei.common.node.server.ServerNode;
import com.taobao.jingwei.common.node.server.ServerTaskNode;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode;
import com.taobao.jingwei.common.node.type.ApplierType;
import com.taobao.jingwei.common.node.type.ExtractorType;
import com.taobao.jingwei.webconsole.biz.ao.JingweiGroupAO;
import com.taobao.jingwei.webconsole.biz.ao.JingweiServerAO;
import com.taobao.jingwei.webconsole.biz.manager.JingweiZkConfigManager;
import com.taobao.jingwei.webconsole.common.JingweiWebConsoleConstance;
import com.taobao.jingwei.webconsole.model.JingWeiResult;
import com.taobao.jingwei.webconsole.model.JingweiAssembledServer;
import com.taobao.jingwei.webconsole.model.JingweiAssembledServerTask;
import com.taobao.jingwei.webconsole.model.JingweiServerCriteria;
import com.taobao.jingwei.webconsole.model.JingweiTaskViewItem;
import com.taobao.jingwei.webconsole.util.DataCacheMaintain;
import com.taobao.jingwei.webconsole.util.DataCacheType;

public class JingweiServerAOImpl implements JingweiServerAO, JingweiWebConsoleConstance, JingWeiConstants {
	private static final Log log = LogFactory.getLog(JingweiServerAOImpl.JINGWEI_LOG);

	@Autowired
	private JingweiZkConfigManager jwConfigManager;

	@Autowired
	private JingweiGroupAO jwGroupAO;
	
	@Autowired
	private DataCacheMaintain jingweiDataCache;
	
	@Override
	public List<JingweiAssembledServer> getServers(String zkKey) {
		List<JingweiAssembledServer> list = new ArrayList<JingweiAssembledServer>();
		Set<String> serverNames = this.getServerNames(zkKey);

		for (String server : serverNames) {
			// 过滤掉server node是null的情况
			if (server.equals("null")) {
				continue;
			}
			JingweiAssembledServer as = new JingweiAssembledServer();
			ServerNode serverNode = new ServerNode();
			serverNode.setName(server);

			Set<ServerTaskNode> serverTaskNodes = this.getServerTaskNodes(serverNode, zkKey);

			List<JingweiAssembledServerTask> astList = new ArrayList<JingweiAssembledServerTask>();

			for (ServerTaskNode serverTaskNode : serverTaskNodes) {
				astList.add(this.getJingweiAssembledServerTask(serverTaskNode, zkKey));
			}

			StatusNode status = this.getServerStatus(server, zkKey);
			if (null == status) {
				as.setStatus(JingweiWebConsoleConstance.DEFAULT_SHOW_STATUS);
			} else {
				as.setStatus(status.getStatusEnum().getStatusString().toUpperCase());
			}

			as.setServerName(server);

			Collections.sort(astList);
			as.setJingweiAssembledServerTasks(astList);

			list.add(as);
		}

		Collections.sort(list);
		return list;
	}

	@Override
	public List<JingweiTaskViewItem> getTasks(String zkKey) {

		Set<String> taskNames = this.getTaskNames(zkKey);

		List<JingweiTaskViewItem> jingweiTaskViewItems = new ArrayList<JingweiTaskViewItem>();

		for (String taskName : taskNames) {
			JingweiTaskViewItem item = new JingweiTaskViewItem();
			item.setTaskName(taskName);

			List<JingweiAssembledServerTask> jingweiAssembledServerTasks = new ArrayList<JingweiAssembledServerTask>();

			// 获取task在哪些server上运行 e.g. /jingwei/tasks/**task/hosts的子节点
			Set<String> hostNames = this.getTaskHostNames(taskName, zkKey);

			for (String hostName : hostNames) {
				ServerTaskNode serverTaskNode = this.getServerTaskNode(hostName, taskName, zkKey);
				if (null != serverTaskNode) {
					jingweiAssembledServerTasks.add(this.getJingweiAssembledServerTask(serverTaskNode, zkKey));
				}
			}

			item.setJingweiAssembledServerTasks(jingweiAssembledServerTasks);

			jingweiTaskViewItems.add(item);
		}

		return jingweiTaskViewItems;
	}

	@Override
	public JingWeiResult addServerNode(ServerNode serverNode, String zkKey) {
		JingWeiResult result = new JingWeiResult();

		// 发布数据的路径
		String path = serverNode.getDataIdOrNodePath();

		try {
			// 创建server节点 E.g. /jingwei/servers/**server
			jwConfigManager.getZkConfigManager(zkKey).publishData(path, serverNode.toJSONString(),
					serverNode.isPersistent());
		} catch (Exception e) {
			result.setSuccess(false);
			if (e instanceof ZkNodeExistsException) {
				result.setErrorCode(JingWeiResult.JINGWEI_ERROR_CODE_EXISTED);
			} else {
				result.setErrorCode(JingWeiResult.JINGWEI_ERROR_CODE_PATH_CREATE_FAILED);
			}
			result.setReplaceInfo(new String[] { serverNode.getName() });
			log.error(e);
		}

		return result;
	}

	@Override
	public JingWeiResult deleteServerNode(String serverName, String zkKey) {
		JingWeiResult result = new JingWeiResult();

		// 路径
		String path = ServerNode.getDataIdOrPathFromServerName(serverName);

		try {
			// 删除server节点 E.g. /jingwei/servers/**server
			jwConfigManager.getZkConfigManager(zkKey).delete(path.toString());
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorCode(JingWeiResult.JINGWEI_ERROR_CODE_PATH_DELETE_FAILED);
			result.setReplaceInfo(new String[] { serverName });
			log.error(e);
		}

		return result;
	}

	@Override
	public Set<String> getServerNames(String zkKey) {
		// 数据的路径E.g. /jingwei/servers
		String path = JingWeiConstants.JINGWEI_SERVER_ROOT_PATH;

		// 获取子节点 E.g. /jingwei/servers
		Map<String, String> data = jwConfigManager.getZkConfigManager(zkKey).getChildDatas(path.toString(), null);

		return data.keySet();
	}

	@Override
	public JingWeiResult addServerTaskNode(ServerTaskNode serverTaskNode, String zkKey) {
		JingWeiResult result = new JingWeiResult();

		// 路径 E.g. /jingwei/servers/**server/tasks/**task
		String path = ServerTaskNode.getDataIdOrNodePathByServerTaskName(serverTaskNode.getServerName(),
				serverTaskNode.getTaskName());

		try {
			// 数据
			String data = serverTaskNode.toJSONString();

			// 创建server节点 E.g. /jingwei/servers/**server
			// 注意：不能重复创建ServerTaskNode类型的节点
			jwConfigManager.getZkConfigManager(zkKey).publishData(path, data, true);
		} catch (Exception e) {
			result.setSuccess(false);
			if (e instanceof ZkNodeExistsException) {
				result.setErrorCode(JingWeiResult.JINGWEI_ERROR_SERVER_TASK_EXIST);
			} else {
				result.setErrorCode(JingWeiResult.JINGWEI_ERROR_CODE_PATH_CREATE_FAILED);
			}
			result.setReplaceInfo(new String[] { serverTaskNode.getServerName(), serverTaskNode.getTaskName() });

			log.error(e);
		}

		return result;
	}

	@Override
	public JingWeiResult deleteServerTaskNode(ServerTaskNode serverTaskNode, String zkKey) {
		JingWeiResult result = new JingWeiResult();
		try {
			// 路径
			String path = serverTaskNode.getDataIdOrNodePath();

			// 删除server节点 E.g. /jingwei/servers/**server
			jwConfigManager.getZkConfigManager(zkKey).delete(path);
		} catch (Exception e) {
			result.setSuccess(false);

			result.setErrorCode(JingWeiResult.JINGWEI_ERROR_CODE_PATH_DELETE_FAILED);

			result.setReplaceInfo(new String[] { serverTaskNode.getServerName(), serverTaskNode.getTaskName() });

			log.error(e);
		}

		return result;
	}

	@Override
	public JingWeiResult updateServerTaskNode(ServerTaskNode serverTaskNode, String zkKey) {
		JingWeiResult result = new JingWeiResult();

		// 路径 E.g. /jingwei/servers/**server/tasks/**task
		String path = serverTaskNode.getDataIdOrNodePath();

		try {
			// 数据
			String data = serverTaskNode.toJSONString();

			// 创建server节点 E.g. /jingwei/servers/**server
			jwConfigManager.getZkConfigManager(zkKey).updateData(path, data, true);
		} catch (Exception e) {
			result.setSuccess(false);
			if (e instanceof ZkNoNodeException) {
				result.setErrorCode(JingWeiResult.JINGWEI_ERROR_CODE_NOT_EXISTED);
			} else {
				result.setErrorCode(JingWeiResult.JINGWEI_ERROR_CODE_PATH_CREATE_FAILED);
			}

			result.setReplaceInfo(new String[] { serverTaskNode.getServerName(), serverTaskNode.getTaskName() });

			log.error(e);
		}

		return result;
	}

	@Override
	public Set<ServerTaskNode> getServerTaskNodes(ServerNode serverNode, String zkKey) {
		// 数据的路径E.g. /jingwei/servers/**server/tasks

		StringBuilder sb = new StringBuilder(serverNode.getDataIdOrNodePath());
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_SERVER_TASKS_NAME);
		String path = sb.toString();

		// 获取子节点 E.g. /jingwei/servers/**server/tasks
		Map<String, String> data = jwConfigManager.getZkConfigManager(zkKey).getChildDatas(path.toString(), null);

		Set<ServerTaskNode> serverTaskNodes = new HashSet<ServerTaskNode>();

		for (String taskName : data.keySet()) {
			ServerTaskNode node = new ServerTaskNode();
			try {
				node.jsonStringToNodeSelf(data.get(taskName));
			} catch (JSONException e) {
				log.error("json string to node attr error! ", e);
				continue;
			} catch (Exception e) {
				log.error("get server task node error! ", e);
				continue;
			}
			serverTaskNodes.add(node);
		}

		return serverTaskNodes;
	}

	/**
	 * E.g. /jingwei/servers/**server/tasks/**task
	 * 
	 * @param serverName
	 * @param taskName
	 * @param zkKey
	 * @return <code>null</code>表示获取失败
	 */
	public ServerTaskNode getServerTaskNode(String serverName, String taskName, String zkKey) {

		// 路径
		String path = ServerTaskNode.getDataIdOrNodePathByServerTaskName(serverName, taskName);

		// 数据
		String data = jwConfigManager.getZkConfigManager(zkKey).getData(path);

		if (StringUtil.isBlank(data)) {
			return null;
		}

		ServerTaskNode node = new ServerTaskNode();

		try {
			node.jsonStringToNodeSelf(data);
		} catch (JSONException e) {
			log.error("json string to node attr error! ", e);
			return null;
		} catch (Exception e) {
			log.error("get server task node error! ", e);
			return null;
		}

		return node;
	}

	@Override
	public OperateNode getServerTaskOperate(ServerTaskNode serverTaskNode, String zkKey) {
		// 路径 E.g. /jingwei/servers/**server/tasks/**task
		String path = serverTaskNode.getDataIdOrNodePath();

		// 数据
		OperateNode opNode = new OperateNode();
		opNode.setOwnerDataIdOrPath(path);

		path = opNode.getDataIdOrNodePath();

		try {

			// 获取operate节点内容 E.g. /jingwei/servers/**server/tasks
			String data = jwConfigManager.getZkConfigManager(zkKey).getData(path);
			opNode.jsonStringToNodeSelf(data);
		} catch (Exception e) {
			log.error(e);
			return null;
		}

		return opNode;

	}

	@Override
	public JingWeiResult updateServerTaskOperate(ServerTaskNode serverTaskNode, OperateEnum operateEnum, String zkKey) {

		JingWeiResult result = new JingWeiResult();

		// 路径 E.g. /jingwei/servers/**server/tasks/**task/operate
		String path = serverTaskNode.getDataIdOrNodePath() + JingWeiConstants.ZK_PATH_SEP
				+ JingWeiConstants.JINGWEI_OPERATE_NODE_NAME;

		// 数据
		OperateNode opNode = new OperateNode();
		opNode.setOperateEnum(operateEnum);
		opNode.setName(JingWeiConstants.JINGWEI_OPERATE_NODE_NAME);

		try {
			
			// 更新serverTask节点的内容
			jwConfigManager.getZkConfigManager(zkKey).publishOrUpdateData(serverTaskNode.getDataIdOrNodePath(), serverTaskNode.toJSONString(), true);

			String data = opNode.toJSONString();

			// 修改operate节点内容 E.g. /jingwei/servers/**server/tasks/**task/operate
			jwConfigManager.getZkConfigManager(zkKey).publishOrUpdateData(path, data, true);
		} catch (Exception e) {
			result.setSuccess(false);

			result.setErrorCode(JingWeiResult.JINGWEI_ERROR_CODE_PATH_CREATE_FAILED);

			result.setReplaceInfo(new String[] { serverTaskNode.getServerName(), serverTaskNode.getTaskName() });

			log.error(e);
		}

		return result;
	}

	@Override
	public StatusNode getStatusNode(String serverName, String taskName, String zkKey) {
		StatusNode statusNode = new StatusNode();

		// 节点路径
		String path = this.getTaskStatusPath(serverName, taskName);

		// 数据
		String data = jwConfigManager.getZkConfigManager(zkKey).getData(path);

		if (StringUtil.isBlank(data)) {
			return null;
		}

		try {
			statusNode.jsonStringToNodeSelf(data);
		} catch (JSONException e) {
			log.error(e);
			return null;
		}

		return statusNode;
	}

	/**
	 * E.g. /jingwei/tasks/**task/hosts/**host/status
	 * 
	 * @param servreName
	 * @param taskName
	 * @return
	 */
	private String getTaskStatusPath(String serverName, String taskName) {
		StringBuilder sb = new StringBuilder(JingWeiConstants.JINGWEI_TASK_ROOT_PATH);

		sb.append(JingWeiConstants.ZK_PATH_SEP).append(taskName);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_TASK_HOST_NODE);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(serverName);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_STATUS_NODE_NAME);

		return sb.toString();
	}

	@Override
	public StatusNode getServerStatus(String serverName, String zkKey) {
		StringBuilder sb = new StringBuilder(JingWeiConstants.JINGWEI_SERVER_ROOT_PATH);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(serverName);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_STATUS_NODE_NAME);

		// 路径
		String path = sb.toString();

		// 数据
		String data = jwConfigManager.getZkConfigManager(zkKey).getData(path);

		if (StringUtil.isBlank(data)) {
			return null;
		}

		StatusNode statusNode = new StatusNode();
		try {
			statusNode.jsonStringToNodeSelf(data);
		} catch (JSONException e) {
			log.error(e);
			return null;
		}

		return statusNode;

	}

	private JingweiAssembledServerTask getJingweiAssembledServerTask(ServerTaskNode serverTaskNode, String zkKey) {
		JingweiAssembledServerTask ast = new JingweiAssembledServerTask();

		// 设置op值
		OperateNode opNode = this.getServerTaskOperate(serverTaskNode, zkKey);

		if (null == opNode) {
			ast.setOperate(OperateEnum.NODE_STOP.getOperateString());
		} else {
			// set op
			ast.setOperate(opNode.getOperateEnum().getOperateString());
		}

		// 设置status值
		String taskName = serverTaskNode.getTaskName();
		String serverName = serverTaskNode.getServerName();
		StatusNode statusNode = this.getStatusNode(serverName, taskName, zkKey);

		if (null == statusNode) {
			ast.setStatus(JingweiWebConsoleConstance.DEFAULT_SHOW_STATUS);
		} else {
			// set status
			ast.setStatus(statusNode.getStatusEnum().getStatusString().toUpperCase());
		}

		// 设置severTaskNode的值
		ast.setServerName(serverTaskNode.getServerName());
		ast.setTaskName(serverTaskNode.getTaskName());
		ast.setPluginTaskTargetState(serverTaskNode.getPluginTaskTargetStateEnum().toString());
		ast.setPluginTaskWorkState(serverTaskNode.getPluginTaskWorkStateEnum().toString());

		// 设置Task类型
		ast.setTaskType(serverTaskNode.getTaskType().getTaskTypeString().toUpperCase());

		return ast;
	}

	/**
	 * 获取 /jingwei/tasks/**task/hosts 的子节点名集合
	 * 
	 * @param taskName
	 * @param zkKey
	 * @return E.g. /jingwei/tasks/**task/hosts 的子节点名集合
	 */
	private Set<String> getTaskHostNames(String taskName, String zkKey) {
		// 路径
		StringBuilder path = new StringBuilder(JingWeiConstants.JINGWEI_TASK_ROOT_PATH);

		path.append(JingWeiConstants.ZK_PATH_SEP).append(taskName);
		path.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_TASK_HOST_NODE);

		// 数据
		return jwConfigManager.getZkConfigManager(zkKey).getChildDatas(path.toString(), null).keySet();
	}

	/**
	 * 获取zk上所有任务名称, 使用缓存, modified by leiwen.zh
	 * 
	 * @param zkKey
	 * @return
	 */
	@Override
	public Set<String> getTaskNames(String zkKey) {
	    Set<String> taskNameSet = new HashSet<String>();
	    // 先从缓存中取
	    List<String> taskNameList = this.jingweiDataCache.getEnvDataCache().getZkPathCache(zkKey).get(DataCacheType.JingweiAssembledTask.toString());
	    if(taskNameList != null) {
	        taskNameSet.addAll(taskNameList);
	    }
	    else {
	        // 缓存中没有, 从zk取, 并放入缓存
	        taskNameSet = jwConfigManager.getZkConfigManager(zkKey).getChildDatas(JingWeiConstants.JINGWEI_TASK_ROOT_PATH, null).keySet();
	        List<String> taskNameList2 = new ArrayList<String>();
	        taskNameList2.addAll(taskNameSet);
	        this.jingweiDataCache.getEnvDataCache().getZkPathCache(zkKey).put(DataCacheType.JingweiAssembledTask.toString(), taskNameList2);
	    }
	    
		return taskNameSet;
	}

	@Override
	public Set<String> getBuildinTaskNames(String zkKey) {
		// 路径
		String path = JingWeiConstants.JINGWEI_TASK_ROOT_PATH;

		// data
		Map<String, String> allTasks = jwConfigManager.getZkConfigManager(zkKey).getChildDatas(path, null);

		// 返回值
		Set<String> buildinTaskNames = new TreeSet<String>();

		for (String taskName : allTasks.keySet()) {
			SyncTaskNode syncTaskNode = new SyncTaskNode();
			String data = allTasks.get(taskName);
			if (StringUtil.isNotEmpty(data)) {
				try {
					syncTaskNode.jsonStringToNodeSelf(data);
				} catch (JSONException e) {
					log.error(e);
					continue;
				}
			}

			if (null != syncTaskNode.getApplierType() && null != syncTaskNode.getExtractorType()) {
				String appllierString = syncTaskNode.getApplierType().toString();
				String extractorString = syncTaskNode.getExtractorType().toString();
				if (!appllierString.equals(ApplierType.CUSTOM_APPLIER.toString())
						&& !extractorString.equals(ExtractorType.CUSTOM_EXTRACTOR.toString())) {
					buildinTaskNames.add(taskName);
				}
			}
		}

		return buildinTaskNames;
	}

	@Override
	public Set<String> getTaskNames(String serverName, String zkKey) {
		// 数据的路径E.g. /jingwei/servers/**server/tasks
		StringBuilder sb = new StringBuilder(JingWeiConstants.JINGWEI_SERVER_ROOT_PATH);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(serverName);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_SERVER_TASKS_NAME);
		String path = sb.toString();

		// 获取子节点 E.g. /jingwei/servers/**server/tasks
		return jwConfigManager.getZkConfigManager(zkKey).getChildDatas(path.toString(), null).keySet();
	}

	@Override
	public List<JingweiAssembledServer> getServers(JingweiServerCriteria criteria, String zkKey) {

		List<JingweiAssembledServer> list = new ArrayList<JingweiAssembledServer>();
		Set<String> serverNames = this.getServerNames(zkKey);

		String criteriaTaskName = criteria.getTaskName();
		if (StringUtil.isBlank(criteriaTaskName)) {
			criteriaTaskName = new String("*");
		}

		String criteriaServerName = criteria.getServerName();
		if (StringUtil.isBlank(criteriaServerName)) {
			criteriaServerName = new String("*");
		}

		// 包含serverName
		for (String server : serverNames) {

			if (Wildcard.match(server, criteriaServerName)) {
				JingweiAssembledServer as = new JingweiAssembledServer();
				ServerNode serverNode = new ServerNode();
				serverNode.setName(server);

				Set<ServerTaskNode> serverTaskNodes = this.getServerTaskNodes(serverNode, zkKey);

				List<JingweiAssembledServerTask> astList = new ArrayList<JingweiAssembledServerTask>();

				// 设置server属性
				ServerNode serverNodeReal = this.getServerNode(server, zkKey);
				if (null != serverNodeReal) {
					as.setExecutorCount(serverNodeReal.getExecutorCount());
					as.setVersion(serverNodeReal.getVersion());
					as.setUserName(serverNodeReal.getUserName());
				}

				for (ServerTaskNode serverTaskNode : serverTaskNodes) {

					// 过滤task name
					if (StringUtil.isNotBlank(criteriaTaskName)
							&& !Wildcard.match(serverTaskNode.getTaskName(), criteriaTaskName)) {
						continue;
					}

					JingweiAssembledServerTask jingweiAssembledServerTask = this.getJingweiAssembledServerTask(
							serverTaskNode, zkKey);

					// 过滤task status

					String criteriaTaskStatus = criteria.getTaskStatus();
					if (StringUtil.isNotBlank(criteriaTaskStatus)
							&& !criteriaTaskStatus.equals(jingweiAssembledServerTask.getStatus())) {
						continue;
					}

					// 过滤task type

					String criteriaTaskType = criteria.getTaskType();
					if (StringUtil.isNotBlank(criteriaTaskType)
							&& !criteriaTaskType.equals(jingweiAssembledServerTask.getTaskType())) {
						continue;
					}

					astList.add(jingweiAssembledServerTask);
				}

				StatusNode status = this.getServerStatus(server, zkKey);
				if (null == status) {
					as.setStatus(JingweiWebConsoleConstance.DEFAULT_SHOW_STATUS);
				} else {
					as.setStatus(status.getStatusEnum().getStatusString().toUpperCase());
				}

				// 过滤server status
				String criteriaServerStatus = criteria.getServerStatus();
				if (StringUtil.isNotBlank(criteriaServerStatus) && !criteriaServerStatus.equals(as.getStatus())) {
					continue;
				}

				as.setServerName(server);

				Collections.sort(astList);
				as.setJingweiAssembledServerTasks(astList);
				
				if ((as.getGroups() == null || as.getGroups().isEmpty()) && as.getJingweiAssembledServerTasks().isEmpty()) {
					as.setCanDelete(true);
				}

				list.add(as);
			}

		}

		List<JingweiAssembledServer> hits = new ArrayList<JingweiAssembledServer>();
		for (JingweiAssembledServer server : list) {
			if (!server.getJingweiAssembledServerTasks().isEmpty()) {
				hits.add(server);
			} else {
				if (StringUtil.isBlank(criteria.getTaskName()) && StringUtil.isBlank(criteria.getTaskStatus())
						&& StringUtil.isBlank(criteria.getServerStatus())) {
					hits.add(server);
				}
			}

			// 判断能否删除，如果有子节点就不能删除
		}

		// 如果group里含有任务，则不显示任务了
		Set<String> allGroupTasks = jwGroupAO.getAllGroupTasks(zkKey);
		for (JingweiAssembledServer server : hits) {
			List<JingweiAssembledServerTask> newTasks = new ArrayList<JingweiAssembledServerTask>();

			for (JingweiAssembledServerTask task : server.getJingweiAssembledServerTasks()) {
				if (!allGroupTasks.contains(task.getTaskName())) {
					newTasks.add(task);
				}
			}

			server.setJingweiAssembledServerTasks(newTasks);
		}

		Collections.sort(hits);
		return hits;
	}

	@Override
	public Set<String> getGroupNames(String serverName, String zkKey) {
		String path = new StringBuilder(JINGWEI_SERVER_ROOT_PATH).append(ZK_PATH_SEP).append(serverName).toString();

		// data
		String data = jwConfigManager.getZkConfigManager(zkKey).getData(path);

		if (StringUtil.isBlank(data)) {
			return Collections.emptySet();
		}

		ServerNode serverNode = new ServerNode();
		try {
			serverNode.jsonStringToNodeSelf(data);
		} catch (JSONException e) {
			log.error(e.getStackTrace());
			return Collections.emptySet();
		}

		List<String> groups = serverNode.getGroups();

		Set<String> groupSet = new TreeSet<String>();
		groupSet.addAll(groups);

		return groupSet;
	}

	@Override
	public void addGroup(String serverName, Set<String> groupNames, String zkKey) {
		Set<String> currentGroups = this.getGroupNames(serverName, zkKey);

		//currentGroups.addAll(groupNames);
		groupNames.addAll(currentGroups);

		this.updateGroup(serverName, groupNames, zkKey);

	}

	@Override
	public void removeGroup(String serverName, String groupName, String zkKey) {

		Set<String> currentGroups = this.getGroupNames(serverName, zkKey);
		currentGroups.remove(groupName);
		this.updateGroup(serverName, currentGroups, zkKey);
	}

	private void updateGroup(String serverName, Set<String> currentGroups, String zkKey) {
		// path
		String path = new StringBuilder(JINGWEI_SERVER_ROOT_PATH).append(ZK_PATH_SEP).append(serverName).toString();

		List<String> groups = new ArrayList<String>();
		groups.addAll(currentGroups);

		String data = jwConfigManager.getZkConfigManager(zkKey).getData(path);

		ServerNode serverNode = new ServerNode();
		try {
			if (StringUtil.isNotBlank(data)) {
				serverNode.jsonStringToNodeSelf(data);
			}

			serverNode.setGroups(groups);
			jwConfigManager.getZkConfigManager(zkKey).publishOrUpdateData(path, serverNode.toJSONString(), true);
		} catch (Exception e) {
			log.error(e.getStackTrace());
			return;
		}
	}

	@Override
	public ServerNode getServerNode(String serverName, String zkKey) {

		// path
		StringBuilder path = new StringBuilder(JINGWEI_SERVER_ROOT_PATH);
		path.append(ZK_PATH_SEP).append(serverName);
		path.append(ZK_PATH_SEP).append(JINGWEI_EXECUTORS_NAME);

		int executorCount = jwConfigManager.getZkConfigManager(zkKey).getChildDatas(path.toString(), null).keySet()
				.size();

		// path
		path = new StringBuilder(JINGWEI_SERVER_ROOT_PATH);
		path.append(ZK_PATH_SEP).append(serverName);

		ServerNode serverNode = new ServerNode();
		String data = jwConfigManager.getZkConfigManager(zkKey).getData(path.toString());

		try {
			serverNode.jsonStringToNodeSelf(data);

			if (executorCount != 0) {
				serverNode.setExecutorCount(executorCount);
			}

			return serverNode;
		} catch (JSONException e) {
			log.error(e);
			return null;
		}
	}

	@Override
	public int getRunningTaskCount(String serverName, String taskName, String zkKey) {
		StringBuilder sb = new StringBuilder(JINGWEI_TASK_ROOT_PATH);
		sb.append(ZK_PATH_SEP).append(taskName);
		sb.append(ZK_PATH_SEP).append(JINGWEI_INSTANCE_TASK_LOCKS_NODE_NAME);

		Map<String, String> data = jwConfigManager.getZkConfigManager(zkKey).getChildDatas(sb.toString(), null);

		if (data.values().contains(serverName)) {
			return 1;
		}

		return 0;
	}
}
