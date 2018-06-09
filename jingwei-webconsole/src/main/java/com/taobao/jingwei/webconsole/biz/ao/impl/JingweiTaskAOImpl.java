/**
 * 
 */
package com.taobao.jingwei.webconsole.biz.ao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.node.AbstractNode;
import com.taobao.jingwei.common.node.AlarmNode;
import com.taobao.jingwei.common.node.PositionNode;
import com.taobao.jingwei.common.node.StatsNode;
import com.taobao.jingwei.common.node.StatusNode;
import com.taobao.jingwei.common.node.StatusNode.StatusEnum;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode;
import com.taobao.jingwei.webconsole.biz.ao.JingweiTaskAO;
import com.taobao.jingwei.webconsole.biz.manager.JingweiZkConfigManager;
import com.taobao.jingwei.webconsole.common.JingweiWebConsoleConstance;
import com.taobao.jingwei.webconsole.model.JingWeiResult;
import com.taobao.jingwei.webconsole.model.JingweiAssembledTask;
import com.taobao.jingwei.webconsole.model.JingweiHost;
import com.taobao.jingwei.webconsole.model.JingweiTaskCriteria;

/**
 * @author qingren
 * 
 */
public class JingweiTaskAOImpl implements JingweiTaskAO, JingweiWebConsoleConstance, JingWeiConstants {
	private static final Log log = LogFactory.getLog(JINGWEI_LOG);

	@Autowired
	private JingweiZkConfigManager jwConfigManager;

	@Override
	public JingWeiResult addTaskInfo(AbstractNode taskNode, String zkKey) {
		JingWeiResult result = new JingWeiResult();
		try {
			String path = AbstractNode.JINGWEI_TASK_ROOT_PATH + "/" + taskNode.getName();
			jwConfigManager.getZkConfigManager(zkKey).publishData(path, taskNode.toJSONString(),
					taskNode.isPersistent());
			/* 创建hosts节点 */
			jwConfigManager.getZkConfigManager(zkKey).publishData(path + "/hosts", null, true);
		} catch (Exception e) {
			result.setSuccess(false);
			if (e instanceof ZkNodeExistsException) {
				result.setErrorCode(JingWeiResult.JINGWEI_ERROR_CODE_EXISTED);
			} else {
				result.setErrorCode(JingWeiResult.JINGWEI_ERROR_CODE_PATH_CREATE_FAILED);
			}
			result.setReplaceInfo(new String[] { taskNode.getName() });
			log.error(e);
		}
		return result;
	}

	@Override
	public JingWeiResult updateTaskInfo(AbstractNode taskNode, String zkKey) {
		JingWeiResult result = new JingWeiResult();
		String path = AbstractNode.JINGWEI_TASK_ROOT_PATH + "/" + taskNode.getName();
		try {
			jwConfigManager.getZkConfigManager(zkKey)
					.updateData(path, taskNode.toJSONString(), taskNode.isPersistent());
		} catch (Exception e) {
			result.setSuccess(false);
			if (e instanceof ZkNoNodeException) {
				result.setErrorCode(JingWeiResult.JINGWEI_ERROR_CODE_NOT_EXISTED);
			} else {
				result.setErrorCode(JingWeiResult.JINGWEI_ERROR_CODE_PATH_CREATE_FAILED);
			}
			result.setReplaceInfo(new String[] { path });
			log.error(e);
		}
		return result;
	}

	@Override
	public JingWeiResult deleteTaskInfo(String taskName, String zkKey) {
		JingWeiResult result = new JingWeiResult();
		String path = JINGWEI_TASK_ROOT_PATH + ZK_PATH_SEP + taskName;
		try {
			jwConfigManager.getZkConfigManager(zkKey).delete(path);
		} catch (Exception e) {
			result.setSuccess(false);
			if (e instanceof ZkNoNodeException) {
				result.setErrorCode(JingWeiResult.JINGWEI_ERROR_CODE_NOT_EXISTED);
			} else {
				result.setErrorCode(JingWeiResult.JINGWEI_ERROR_CODE_PATH_DELETE_FAILED);
			}
			result.setReplaceInfo(new String[] { path });
			log.error(e);
		}
		return result;
	}

	@Override
	public SyncTaskNode getTaskInfo(String taskName, String zkKey) {
		String data = jwConfigManager.getZkConfigManager(zkKey).getData(
				AbstractNode.JINGWEI_TASK_ROOT_PATH + "/" + taskName);
		SyncTaskNode node = new SyncTaskNode();
		if (StringUtil.isNotBlank(data)) {
			try {
				node.jsonStringToNodeSelf(data);
				// if (node.getExtractorType().getType() == ExtractorType.BINLOG_EXTRACTOR.getType()) {
				// JSONObject obj = new JSONObject(node.getExtractorData());
				// node.setExtractorData(obj.getString("extractorData"));
				// }
			} catch (JSONException e) {
				node.setName(taskName);
				node.setDesc(NOT_JINGWEI_TASK);
				log.warn(e);
			}
		}
		return node;
	}

	@Override
	public PositionNode getLastCommit(String taskId, String zkKey) {
		// TODO:增加last_commit数据监听
		String lastCommit = jwConfigManager.getZkConfigManager(zkKey).getData(
				JINGWEI_TASK_ROOT_PATH + ZK_PATH_SEP + taskId + ZK_PATH_SEP + JINGWEI_TASK_POSITION_NODE_NAME);
		PositionNode position = new PositionNode();
		if (StringUtil.isNotBlank(lastCommit)) {
			try {
				position.jsonStringToNodeSelf(lastCommit);
			} catch (JSONException e1) {
				log.warn(e1);
			}
		}
		return position;
	}

	@Override
	public List<PositionNode> getLastCommits(String taskId, String zkKey) {
		List<PositionNode> list = new ArrayList<PositionNode>();
		Map<String, String> lastCommits = jwConfigManager.getZkConfigManager(zkKey).getChildDatas(
				JINGWEI_TASK_ROOT_PATH + ZK_PATH_SEP + taskId + ZK_PATH_SEP + JINGWEI_TASK_POSITION_NODE_NAME, null);
		if (lastCommits != null) {
			for (Entry<String, String> entry : lastCommits.entrySet()) {
				PositionNode position = new PositionNode();
				try {
					position.jsonStringToNodeSelf(entry.getValue());
				} catch (JSONException e) {
					log.warn("转换lastComit/*节点失败", e);
				}
				list.add(position);
			}
		}
		return list;
	}

	@Override
	public Map<String/* task name */, JingweiAssembledTask> getTasks(JingweiTaskCriteria criteria, String zkKey) {
		Map<String, JingweiAssembledTask> assembledTasks = new LinkedHashMap<String, JingweiAssembledTask>();
		Map<String/* child name */, String/* data String */> childMap = jwConfigManager.getZkConfigManager(zkKey)
				.getChildDatas(JINGWEI_TASK_ROOT_PATH, criteria.getTaskId());

		// 装配task信息
		for (Entry<String, String> entry : childMap.entrySet()) {
			String taskPath = "";
			SyncTaskNode task = new SyncTaskNode();
			try {
				task.jsonStringToNodeSelf(entry.getValue());
				taskPath = task.getDataIdOrNodePath();
			} catch (JSONException e) {
				task.setName(entry.getKey());
				taskPath = JINGWEI_TASK_ROOT_PATH + ZK_PATH_SEP
						+ (StringUtil.isNotBlank(task.getName()) ? task.getName() : "null");
				task.setDesc(NOT_JINGWEI_TASK);
			}

			// 过滤Extractor类型
			Integer extractorType = criteria.getExtractorType();
			if (extractorType == null) {
				// no op
			} else {
				if (criteria.getExtractorType() != null
						&& (task.getExtractorType() == null || task.getExtractorType().getType() != extractorType)) {
					continue;
				}
			}

			// 过滤Applier类型
			if (criteria.getApplierType() != null
					&& (task.getApplierType() == null || task.getApplierType().getType() != criteria.getApplierType())) {
				continue;
			}

			// TODO:task加数据监听
			JingweiAssembledTask assembledTask = new JingweiAssembledTask();
			assembledTask.setTask(task);

			// 取hosts列表
			// TODO:hosts加子节点监听
			String hostsPath = new StringBuilder(taskPath).append(ZK_PATH_SEP).append(JINGWEI_TASK_HOST_NODE)
					.toString();
			Map<String, String> hostsData = jwConfigManager.getZkConfigManager(zkKey).getChildDatas(hostsPath,
					criteria.getHostName());
			List<JingweiHost> hosts = new ArrayList<JingweiHost>();
			// 过滤运行状态

			boolean isCheckStatus = StringUtil.isNotBlank(criteria.getRunStatus());
			boolean isCheckHost = StringUtil.isNotBlank(criteria.getHostName());
			/* 如果有RUNNING或STANDBY状态的主机，则不允许修改和删除 */
			boolean canModify = true;
			if (!hostsData.isEmpty()) {
				for (String hostName : hostsData.keySet()) {
					// stats 节点、alarm节点数据等用到时主动去取
					StringBuilder hostPath = new StringBuilder().append(hostsPath).append(ZK_PATH_SEP).append(hostName)
							.append(ZK_PATH_SEP);
					JingweiHost host = new JingweiHost();
					host.setName(hostName);

					// TODO:增加status数据监听
					String statusStr = jwConfigManager.getZkConfigManager(zkKey).getData(
							hostPath.append(JINGWEI_STATUS_NODE_NAME).toString());
					StatusNode status = null;
					if (StringUtil.isNotBlank(statusStr)) {
						status = new StatusNode();
						try {
							status.jsonStringToNodeSelf(statusStr);
							host.setStatus(status);
							canModify = !StatusEnum.RUNNING.name().equalsIgnoreCase(status.getStatusEnum().name())
									&& !StatusEnum.STANDBY.name().equalsIgnoreCase(status.getStatusEnum().name());
						} catch (JSONException e) {
							// 忽略非法数据
							log.warn(e);
						}
					}

					// 过滤运行状态
					if (isCheckStatus) {
						if (status == null || !criteria.getRunStatus().equals(status.getStatusEnum().getStatusString())) {
							continue;
						}
					}

					hosts.add(host);
				}
			}
			assembledTask.setCanModify(canModify);
			// 过滤主机名
			if (isCheckHost && hosts.isEmpty()) {
				continue;
			}
			// 过滤运行状态
			if (isCheckStatus && hosts.isEmpty()) {
				continue;
			}
			assembledTask.setHosts(hosts);
			assembledTasks.put(entry.getKey(), assembledTask);
		}

		return assembledTasks;
	}

	@Override
	public StatsNode getStatsInfo(String taskName, String hostName, String zkKey) {
		StatsNode node = null;
		String data = jwConfigManager.getZkConfigManager(zkKey).getData(StatsNode.getNodeIdOrPath(taskName, hostName));
		if (StringUtil.isNotBlank(data)) {
			node = new StatsNode(taskName, hostName);
			try {
				node.jsonStringToNodeSelf(data);
			} catch (JSONException e) {
				log.warn(e);
			}
		}
		return node;
	}

	@Override
	public Map<String, AlarmNode> getAlarmInfo(String taskName, String zkKey) {
		Map<String, AlarmNode> map = new LinkedHashMap<String, AlarmNode>();
		String hostsPath = AlarmNode.JINGWEI_TASK_ROOT_PATH + ZK_PATH_SEP + taskName + ZK_PATH_SEP + "hosts";
		Map<String, String> hosts = jwConfigManager.getZkConfigManager(zkKey).getChildDatas(hostsPath, null);
		for (String host : hosts.keySet()) {
			String alarmPath = hostsPath + ZK_PATH_SEP + host + ZK_PATH_SEP + "alarm";
			String data = jwConfigManager.getZkConfigManager(zkKey).getData(alarmPath);
			if (StringUtil.isNotBlank(data)) {
				AlarmNode node = new AlarmNode();
				try {
					node.jsonStringToNodeSelf(data);
				} catch (JSONException e) {
					log.warn(e);
				}
				map.put(host, node);
			}
		}
		return map;
	}

	@Override
	public void updateLastCommit(String taskId, String zkKey, String value) throws JSONException, Exception {
		SyncTaskNode taskNode = new SyncTaskNode();
		taskNode.setName(taskId);
		PositionNode position = new PositionNode();
		position.setTimestamp(new Date());
		position.setPosition(StringUtils.isBlank(value) ? "" : value);
		position.setOwnerDataIdOrPath(taskNode.getDataIdOrNodePath());

		jwConfigManager.getZkConfigManager(zkKey).publishOrUpdateData(position.getDataIdOrNodePath(),
				position.toJSONString(), position.isPersistent());

	}

	@Override
	public boolean hasRunningHost(String taskId, String zkKey) {
		boolean running = false;

		String hostsPath = JINGWEI_TASK_ROOT_PATH + ZK_PATH_SEP + taskId + ZK_PATH_SEP + JINGWEI_TASK_HOST_NODE;
		Map<String, String> hostsData = jwConfigManager.getZkConfigManager(zkKey).getChildDatas(hostsPath, null);
		for (String hostName : hostsData.keySet()) {
			String statsPath = hostsPath + ZK_PATH_SEP + hostName + ZK_PATH_SEP + JINGWEI_STATUS_NODE_NAME;
			String statusStr = jwConfigManager.getZkConfigManager(zkKey).getData(statsPath);
			StatusNode status = null;
			if (StringUtil.isNotBlank(statusStr)) {
				status = new StatusNode();
				try {
					status.jsonStringToNodeSelf(statusStr);
					if (StatusEnum.RUNNING.equals(status.getStatusEnum())) {
						running = true;
						break;
					}
				} catch (JSONException e) {
					// 忽略非法数据
					log.warn(e);
				}
			}
		}
		return running;
	}

	@Override
	public void delHost(String taskName, String hostName, String zkKey) {
		try {
			String node = JINGWEI_TASK_ROOT_PATH + ZK_PATH_SEP + taskName + ZK_PATH_SEP + JINGWEI_TASK_HOST_NODE
					+ ZK_PATH_SEP + hostName;
			// 递归删除HOST为hostName下的所有节点
			clearTask(node, zkKey);
		} catch (Exception e) {
			log.error("删除HOST节点失败", e);
		}
	}

	private void clearTask(String node, String zkKey) throws Exception {
		Map<String, String> cd = jwConfigManager.getZkConfigManager(zkKey).getChildDatas(node, null);
		if (!cd.isEmpty()) {
			for (Entry<String, String> entry : cd.entrySet()) {
				clearTask(node + JingWeiConstants.ZK_PATH_SEP + entry.getKey(), zkKey);
			}
		}
		jwConfigManager.getZkConfigManager(zkKey).delete(node);
		log.info("node deleted: " + node);
	}

	@Override
	public Set<String> getTaskSet(JingweiTaskCriteria criteria, String zkKey) {
		Map<String/* child name */, String/* data String */> childMap = jwConfigManager.getZkConfigManager(zkKey)
				.getChildDatas(JINGWEI_TASK_ROOT_PATH, criteria.getTaskId());

		return new TreeSet<String>(childMap.keySet());
	}
}
