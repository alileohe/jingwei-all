package com.taobao.jingwei.common;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.config.ConfigManager;
import com.taobao.jingwei.common.node.OperateNode;
import com.taobao.jingwei.common.node.OperateNode.OperateEnum;
import com.taobao.jingwei.common.node.PositionNode;
import com.taobao.jingwei.common.node.StatusNode;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode;
import com.taobao.jingwei.common.node.type.ApplierType;
import com.taobao.jingwei.common.node.type.ExtractorType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * @desc
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date May 25, 2012 10:30:04 AM
 */

final public class TaskUtil implements JingWeiConstants {
	private static Log log = LogFactory.getLog(TaskUtil.class);

	/**
	 * e.g /jingwei/tasks/task taskInstanceCount����
	 * 
	 * @return
	 */
	public static int getTaskInstanceCount(ConfigManager configManager, String taskName) {
		// ��ȡʧ�� ȡĬ��ֵ1
		int taskInstanceCount = 1;

		StringBuilder path = new StringBuilder(JINGWEI_TASK_ROOT_PATH);
		path.append(ZK_PATH_SEP).append(taskName);

		String data = configManager.getData(path.toString());

		if (StringUtil.isBlank(data)) {
			return taskInstanceCount;
		}

		SyncTaskNode taskNode = new SyncTaskNode();

		try {
			taskNode.jsonStringToNodeSelf(data);

			return taskNode.getTaskInstanceCount();

		} catch (JSONException e) {
			log.error("[jingwei server] get task instance count error : " + taskName, e);

			return taskInstanceCount;
		}

	}

	/**
	 * 
	 * @param configManager
	 * @param taskName
	 * @return ָ����task���е�ʵ���������ǵ��������ͬʱ���кö��
	 */
	public static int runningInstanceCount(ConfigManager configManager, String taskName) {
		String hostsPath = TaskUtil.getTaskHostPath(taskName);
		Map<String, String> childs = configManager.getChildDatas(hostsPath, null);
		Set<String> hostNames = childs.keySet();

		if (hostNames.isEmpty()) {
			return 0;
		}

		int count = 0;
		for (String hostName : hostNames) {

			boolean running = TaskUtil.checkRunning(configManager, taskName, hostName);

			if (running) {
				count++;
			}
		}

		return count;
	}

	/**
	 * e.g. /jingwei/tasks/**task/t-locks�ӽڵ�
	 * 
	 * @param configManager
	 * @param taskName
	 * @return
	 */
	public static Map<String, String> getTaskLocksCount(ConfigManager configManager, String taskName) {
		String path = TaskUtil.getTaskLocksPath(taskName);
		return configManager.getChildDatas(path, null);
	}

	/**
	 * e.g. /jingwei/tasks/**task/t-locks�ӽڵ�
	 * 
	 * @param groupName
	 * @param taskName
	 * @return
	 */
	private static String getTaskLocksPath(String taskName) {
		StringBuilder path = new StringBuilder(JINGWEI_TASK_ROOT_PATH);
		path.append(ZK_PATH_SEP).append(taskName);
		path.append(ZK_PATH_SEP).append(JINGWEI_INSTANCE_TASK_LOCKS_NODE_NAME);

		return path.toString();
	}

	/**
	 * ��ȡtask��host���ڵ�zk·��
	 * 
	 * @param taskName
	 *            ������
	 * @return host���ڵ�zk ·�� e.g /jingwei-v2/tasks/**task/hosts
	 */
	public static String getTaskHostPath(String taskName) {
		StringBuilder sb = new StringBuilder(JingWeiConstants.JINGWEI_TASK_ROOT_PATH);

		sb.append(JingWeiConstants.ZK_PATH_SEP).append(taskName);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_TASK_HOST_NODE);

		return sb.toString();
	}

	/**
	 * ֻ�е�status��RUNNING��ʱ�� �ŷ���true��standby�Ͳ�����status�ڵ㶼����false;
	 * /jingwei-v2/tasks/**task/hosts/**host/status
	 * 
	 * @param configManager
	 * @param taskName
	 * @param hostName
	 * @param times
	 *            ������ɴβŶ϶�task�ҵ���
	 * @param interval
	 *            ����ʱ����
	 */
	public static boolean checkRunning(ConfigManager configManager, String taskName, String hostName, int times,
			int interval) {

		if (times < 1) {
			throw new IllegalArgumentException("times should greater than 1.");
		}

		if (interval < 0) {
			throw new IllegalArgumentException("interval should be positive integer.");
		}

		for (int i = 0; i < times; i++) {
			if (TaskUtil.checkRunning(configManager, taskName, hostName)) {
				return true;
			}

			if (i == times - 1) {
				break;
			}

			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				log.error("[jingwei server] interrupt exception check runnning", e);
			}
		}

		return false;
	}

	/**
	 * ֻ�е�status��RUNNING��ʱ�� �ŷ���true��standby�Ͳ�����status�ڵ㶼����false;
	 * /jingwei-v2/tasks/**task/hosts/**host/status
	 * 
	 * @param configManager
	 * @param taskName
	 * @param hostName
	 * @return
	 */
	public static boolean checkRunning(ConfigManager configManager, String taskName, String hostName) {
		// ��ȡstatus�ڵ� �������running�ͱ���
		String statusPath = TaskUtil.getStatusNodePath(taskName, hostName);

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
				log.error("[jingwei monitor] get status state for task: " + taskName + ", host: " + hostName
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
	 * ��ȡstatus�ڵ�path
	 * 
	 * @param taskName
	 *            ������
	 * @param hostName
	 *            ������
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

	public static boolean isBuildinTaskType(ConfigManager configManager, String taskName) throws Exception {
		String path = JINGWEI_TASK_ROOT_PATH + ZK_PATH_SEP + taskName;

		String data = configManager.getData(path);

		SyncTaskNode taskNode = new SyncTaskNode();

		taskNode.jsonStringToNodeSelf(data);

		if (taskNode.getExtractorType() == ExtractorType.CUSTOM_EXTRACTOR
				|| taskNode.getApplierType() == ApplierType.CUSTOM_APPLIER) {
			return false;
		} else {
			return true;
		}
	}

	public static boolean shouldRunMoreInstance(ConfigManager configManager, String taskName) {
		// ��ȡ����ʵ�����������û�дﵽ����������
		int requiredTaskInstanceCount = TaskUtil.getTaskInstanceCount(configManager, taskName);

		// �Ѿ������ĸ������������е�task��ʵ������
		Map<String, String> lockTasks = TaskUtil.getTaskLocksCount(configManager, taskName);

		if (requiredTaskInstanceCount == lockTasks.size()) {
			return false;
		}

		return true;
	}

	/**
	 * e.g. /jingwei/servers/**server/tasks/**task/operate
	 * 
	 * @param taskName
	 * @param taskName
	 * @return
	 */
	public static String getServerTaskOpPath(String serverName, String taskName) {
		StringBuilder sb = new StringBuilder(JINGWEI_SERVER_ROOT_PATH);
		sb.append(ZK_PATH_SEP).append(serverName);
		sb.append(ZK_PATH_SEP).append(JINGWEI_SERVER_TASKS_NAME);
		sb.append(ZK_PATH_SEP).append(taskName);
		sb.append(ZK_PATH_SEP).append(JINGWEI_OPERATE_NODE_NAME);

		return sb.toString();

	}

	/**
	 * e.g. /jingwei/servers/**server/tasks/**task/operate
	 * 
	 * @param configManager
	 * @param serverName
	 * @param taskName
	 * @return ֻ��START�ŷ���true,���򷵻�false
	 */
	public static boolean isServerTaskStartOp(ConfigManager configManager, String serverName, String taskName) {
		// path
		String path = TaskUtil.getServerTaskOpPath(serverName, taskName);

		String data = configManager.getData(path);

		OperateNode opNode = new OperateNode();
		try {
			opNode.jsonStringToNodeSelf(data);
		} catch (JSONException e) {
			//log.error(e);
			return false;
		}

		if (opNode.getOperateEnum() == OperateEnum.NODE_START) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * e.g./jingwei/tasks/**task/lastComit
	 * 
	 * @param configManager
	 * @param taskName
	 * @return ����ڵ㲻���ڣ����߽����쳣������zk�쳣����<code>null</code>
	 */
	public static PositionNode getPositionNode(ConfigManager configManager, String taskName) {
		String path = TaskUtil.getTaskPositionPath(taskName);

		String data = configManager.getData(path);

		if (StringUtil.isBlank(data)) {
			log.warn("position node is null " + taskName);
			return null;
		}

		PositionNode positionNode = new PositionNode();

		try {
			positionNode.jsonStringToNodeSelf(data);

			return positionNode;
		} catch (JSONException e) {
			log.error(e);
			return null;
		}
	}

	/**
	 * e.g./jingwei/tasks/**task/lastComit
	 * 
	 * @param taskName
	 * @return
	 */
	public static String getTaskPositionPath(String taskName) {
		StringBuilder sb = new StringBuilder(JINGWEI_TASK_ROOT_PATH);
		sb.append(ZK_PATH_SEP).append(taskName);
		sb.append(ZK_PATH_SEP).append(JINGWEI_TASK_POSITION_NODE_NAME);

		return sb.toString();
	}

	/**
	 * e.g. /jingwei/tasks/**task/lastComit/index
	 * 
	 * @return
	 */
	public static String getSpecTaskComitNodePath(String taskName, int index) {
		StringBuilder sb = new StringBuilder(TaskUtil.getTaskPositionPath(taskName));
		sb.append(ZK_PATH_SEP).append(index);

		return sb.toString();
	}

	public static Set<String> getAllTasks(ConfigManager configManager) {
		String path = JINGWEI_TASK_ROOT_PATH;
		return configManager.getChildDatas(path, null).keySet();
	}

	/**
	 * ��ȡ����ڵ㣬 /jingwei/tasks/**task
	 * @param configManager
	 * @param taskName
	 * @return <code>null</code>��ȡʧ�ܻ�ڵ㲻����
	 */
	public static SyncTaskNode getSyncTaskNode(ConfigManager configManager, String taskName) {
		StringBuilder path = new StringBuilder(JINGWEI_TASK_ROOT_PATH);
		path.append(ZK_PATH_SEP).append(taskName);

		String data = configManager.getData(path.toString());

		if (StringUtil.isBlank(data)) {
			return null;
		}

		SyncTaskNode taskNode = new SyncTaskNode();

		try {
			taskNode.jsonStringToNodeSelf(data);

			return taskNode;
		} catch (Exception e) {
			log.error("[jingwei server] get sync task node error!" + Arrays.deepToString(e.getStackTrace()), e);
		}
		return null;
	}

}
