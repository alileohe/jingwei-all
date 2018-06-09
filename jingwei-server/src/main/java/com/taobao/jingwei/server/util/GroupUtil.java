package com.taobao.jingwei.server.util;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.config.ConfigManager;
import com.taobao.jingwei.common.node.OperateNode;
import com.taobao.jingwei.common.node.OperateNode.OperateEnum;
import com.taobao.jingwei.common.node.StatusNode;
import com.taobao.jingwei.common.node.StatusNode.StatusEnum;
import com.taobao.jingwei.common.node.server.ServerNode;
import com.taobao.jingwei.server.group.CandidateTarget;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

import java.util.*;

/**
* @desc ������
* @author <a href="mailto:shuohai.lhl@taobao.com">˷�� shuohailhl</a>
* @date 2012-3-2����2:29:55
*/
public final class GroupUtil implements JingWeiConstants {
	private static Log log = LogFactory.getLog(GroupUtil.class);

	/**
	 * �ж��Ƿ����status�ڵ�
	 * @param configManager zk������
	 * @param taskName ������
	 * @param hostName ������
	 * @return <code>false</code> ������running״̬��<code>true</code>������running״̬  
	 * e.g.  /jingwei/tasks/**task/hosts/**host/status
	 * @throws JSONException 
	 */
	public static boolean isRunning(ConfigManager configManager, String taskName, String hostName) throws JSONException {
		// path
		StringBuilder sb = new StringBuilder(JingWeiConstants.JINGWEI_TASK_ROOT_PATH);

		sb.append(JingWeiConstants.ZK_PATH_SEP).append(taskName);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_TASK_HOST_NODE);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(hostName);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_STATUS_NODE_NAME);

		String path = sb.toString();

		//data
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

	public static void pause(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			log.error(e);
		}
	}

	/**
	 * ���task�����Ƿ�������״̬
	 * @param configManager
	 * @param taskName
	 * @return  ���ɨ�赽ָ����task��һ��host�ڵ��ϵ�satus���ǿգ�����<code>true</code>, 
	 * �������host�ڵ��status�������ڣ�����<code>false</code>  e.g. /jingwei/tasks/**task/hosts/**host/status
	 * @throws JSONException 
	 */
	public static boolean runningOnOneHost(ConfigManager configManager, String taskName) throws JSONException {

		StringBuilder path = new StringBuilder(JingWeiConstants.JINGWEI_TASK_ROOT_PATH);

		path.append(JingWeiConstants.ZK_PATH_SEP).append(taskName);
		path.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_TASK_HOST_NODE);

		// ��ȡ�������е�host
		Set<String> hosts = configManager.getChildDatas(path.toString(), null).keySet();

		if (hosts.isEmpty()) {
			return false;
		}

		for (String hostName : hosts) {
			if (GroupUtil.isRunning(configManager, taskName, hostName)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * �жϽڵ��Ƿ���� ������RUNNING, e.g. /jingwei/servers/**server/executors/**executor
	 * @param executorName ִ����������
	 * @return <code>true</code>���ڣ�<code>false</code>������
	 * @throws JSONException 
	 */
	public static boolean isTaskRunning(ConfigManager configManager, String taskName) throws JSONException {

		if (GroupUtil.runningOnOneHost(configManager, taskName)) {
			return true;
		}

		return false;
	}

	/**
	 * ���opetate�ڵ�ֹͣ������״̬;����ڵ㲻���ڣ��򴴽�ֹͣ״̬�Ľڵ� e.g. /jingwei/groups/**group/tasks/**task/operate
	 * @param configManager ���ù�����
	 * @param groupName ����
	 * @param taskName ������
	 * @return <code>true</code>��ʾstart״̬��<code>false</code>��ʾstop״̬
	 * @throws Exception дzk�쳣
	 * @throws JSONException jsonת���쳣 
	 */
	public static boolean isGroupTaskStart(ConfigManager configManager, String groupName, String taskName)
			throws JSONException, Exception {
		// path
		String path = GroupUtil.getGroupTaskOpPath(groupName, taskName);

		String data = configManager.getData(path);

		if (StringUtil.isBlank(data)) {
			OperateNode opNode = new OperateNode();
			opNode.setOperateEnum(OperateEnum.NODE_STOP);

			configManager.publishOrUpdateData(path, opNode.toJSONString(), true);

			return false;
		}

		OperateNode opNode = new OperateNode();
		opNode.jsonStringToNodeSelf(data);

		if (opNode.getOperateEnum() == OperateEnum.NODE_START) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 *  e.g. /jingwei/groups/**group/tasks/**task/operate
	 * @param groupName ����
	 * @param taskName ������
	 * @return op�ڵ�·��
	 */
	public static String getGroupTaskOpPath(String groupName, String taskName) {
		StringBuilder path = new StringBuilder(JingWeiConstants.JINGWEI_GROUP_ROOT_PATH);

		path.append(JingWeiConstants.ZK_PATH_SEP).append(groupName);
		path.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_GROUP_TASKS_NAME);
		path.append(JingWeiConstants.ZK_PATH_SEP).append(taskName);
		path.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_OPERATE_NODE_NAME);

		return path.toString();
	}

	/**
	 * ��������group��task������ op��start������
	 * @param configManager
	 * @return
	 */
	public static List<CandidateTarget> getCandidateTargets(ConfigManager configManager, String serverName) {
		Set<String> groupNames = GroupUtil.getGroups(configManager, serverName);

		List<CandidateTarget> candidateTargets = new ArrayList<CandidateTarget>();

		for (String groupName : groupNames) {
			Set<String> getGroupTasks = GroupUtil.getGroupTasks(configManager, groupName);
			for (String taskName : getGroupTasks) {
				try {
					if (GroupUtil.isGroupTaskStart(configManager, groupName, taskName)) {
						candidateTargets.add(new CandidateTarget(groupName, taskName));
					}
				} catch (Exception e) {
					log.error("[jingwei server] get group task op error : "
							+ GroupUtil.getGroupTaskOpPath(groupName, taskName));
					continue;
				}
			}
		}

		return candidateTargets;
	}

	/**
	 *  /jingwei/servers/**server�ڵ㣬group������
	 * @param configManager
	 * @param serverName
	 * @return
	 */
	public static Set<String> getGroups(ConfigManager configManager, String serverName) {
		String path = new StringBuilder(JINGWEI_SERVER_ROOT_PATH).append(ZK_PATH_SEP).append(serverName).toString();

		// data
		String data = configManager.getData(path);

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

	/**
	 * ��ȡgroup�����е�task����/jingwei/groups/**group/tasks/task
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
	 * ��ȡ���б�������group�����е�task����/jingwei/groups/**group/tasks/task
	 * @param configManager
	 * @param serverName
	 * @return
	 */
	public static Set<String> getServerGroupTasks(ConfigManager configManager, String serverName) {
		Set<String> allGroupTasks = new HashSet<String>();
		Set<String> allGroups = GroupUtil.getGroups(configManager, serverName);

		for (String groupName : allGroups) {
			allGroupTasks.addAll(GroupUtil.getGroupTasks(configManager, groupName));
		}

		return allGroupTasks;
	}

	/**
	 * e.g. /jingwei/tasks/**task/s-locks/lock_i
	 * @param configManager
	 * @param taskName
	 * @param index {1, 2, ...}
	 * @Param serverName
	 * @return 
	 * @throws Exception 
	 */
	public static boolean creatTaskServerEephemeralLock(ConfigManager configManager, String taskName, String index,
			String serverName) throws Exception {

		String path = GroupUtil.getTaskServerEephemeralLockPath(configManager, taskName, index);

		try {
			configManager.publishData(path.toString(), serverName, false);

			return true;
		} catch (Exception e) {
			if (e instanceof ZkNodeExistsException) {
				return false;
			} else {
				throw e;
			}

		}
	}

	/**
	 * e.g. /jingwei/tasks/**task/t-locks/lock1
	 * @param configManager
	 * @param groupName
	 * @param taskName
	 * @param index {1, 2, ...}
	 * @Param serverName
	 * @return 
	 * @throws Exception 
	 */
	public static boolean creatGroupTaskEephemeralLock(ConfigManager configManager, String taskName, String index,
			String serverName) throws Exception {

		String path = GroupUtil.getGroupTaskEephemeralLockPath(configManager, taskName, index);

		try {
			configManager.publishData(path.toString(), serverName, false);

			return true;
		} catch (Exception e) {
			if (e instanceof ZkNodeExistsException) {
				return false;
			} else {
				throw e;
			}

		}
	}

	/**
	 * ������������е���group��ʽ������task��
	 * ִ���������̽����󣬾�ɾ����Ӧ��/jingwei/tasks/**task/s-locks/lock_i
	 * 
	 * @param configManager
	 * @param taskName
	 * @param index
	 * @param serverName
	 * @throws Exception
	 */
	public static void deleteTaskServerEphemetalLock(ConfigManager configManager, String taskName, String index)
			throws Exception {
		String path = GroupUtil.getTaskServerEephemeralLockPath(configManager, taskName, index);

		configManager.delete(path);
	}

	/**
	 *   e.g. /jingwei/tasks/**task/s-locks/lock_i
	 * @param configManager
	 * @param taskName
	 * @param index
	 * @param serverName
	 * @return
	 */
	public static String getTaskServerEephemeralLockPath(ConfigManager configManager, String taskName, String index) {
		StringBuilder path = new StringBuilder(JINGWEI_TASK_ROOT_PATH);

		path.append(ZK_PATH_SEP).append(taskName);
		path.append(ZK_PATH_SEP).append(JINGWEI_GROUP_TASK_SERVER_LOCKS_NODE_NAME);
		path.append(ZK_PATH_SEP).append(JINGWEI_GROUP_TASK_SERVER_LOCK_NODE_PREFIX);
		path.append(index);

		return path.toString();
	}

	/**
	 *   e.g. /jingwei/tasks/**task/t-locks/lock1
	 * @param configManager
	 * @param groupName
	 * @param taskName
	 * @param index
	 * @param serverName
	 * @return
	 */
	public static String getGroupTaskEephemeralLockPath(ConfigManager configManager, String taskName, String index) {
		StringBuilder path = new StringBuilder(JINGWEI_TASK_ROOT_PATH);

		path.append(ZK_PATH_SEP).append(taskName);
		path.append(ZK_PATH_SEP).append(JINGWEI_INSTANCE_TASK_LOCKS_NODE_NAME);
		path.append(ZK_PATH_SEP).append(JINGWEI_INSTANCE_TASK_LOCK_NODE_PREFIX);
		path.append(index);

		return path.toString();
	}

	/**
	 * 
	 * @param requiredTaskInstanceCount
	 * @param currentLocks
	 * @return
	 */
	public static Set<String> waitLockNames(int requiredTaskInstanceCount, Set<String> currentLocks) {
		Set<String> set = new HashSet<String>(requiredTaskInstanceCount);

		for (int i = 1; i <= requiredTaskInstanceCount; i++) {
			set.add(JINGWEI_INSTANCE_TASK_LOCK_NODE_PREFIX + i);
		}

		set.removeAll(currentLocks);

		return set;
	}

	public static int getIndexFromLockName(String lockName) {
		return Integer.valueOf(lockName.substring(JINGWEI_INSTANCE_TASK_LOCK_NODE_PREFIX.length()));
	}
}
