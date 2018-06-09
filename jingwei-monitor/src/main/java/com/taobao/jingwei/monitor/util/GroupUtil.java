package com.taobao.jingwei.monitor.util;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.config.ConfigManager;
import com.taobao.jingwei.common.node.OperateNode;
import com.taobao.jingwei.common.node.OperateNode.OperateEnum;
import com.taobao.jingwei.common.node.StatusNode;
import com.taobao.jingwei.common.node.StatusNode.StatusEnum;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

import java.util.Set;

/**
* @desc ������
* @author <a href="mailto:shuohai.lhl@taobao.com">˷�� shuohailhl</a>
* @date 2012-3-2����2:29:55
*/
public final class GroupUtil {
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
	public static boolean checkTaskOpetate(ConfigManager configManager, String groupName, String taskName)
			throws JSONException, Exception {
		// path
		String path = GroupUtil.getGroupOpPath(groupName, taskName);

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
	public static String getGroupOpPath(String groupName, String taskName) {
		StringBuilder path = new StringBuilder(JingWeiConstants.JINGWEI_GROUP_ROOT_PATH);

		path.append(JingWeiConstants.ZK_PATH_SEP).append(groupName);
		path.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_GROUP_TASKS_NAME);
		path.append(JingWeiConstants.ZK_PATH_SEP).append(taskName);
		path.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_OPERATE_NODE_NAME);

		return path.toString();
	}

	/**
	 *  e.g. /jingwei/groups/**group/tasks/**task/status
	 * @param groupName ����
	 * @param taskName ������
	 * @return op�ڵ�·��
	 */
	public static String getGroupStatusPath(String groupName, String taskName) {
		StringBuilder path = new StringBuilder(JingWeiConstants.JINGWEI_GROUP_ROOT_PATH);

		path.append(JingWeiConstants.ZK_PATH_SEP).append(groupName);
		path.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_GROUP_TASKS_NAME);
		path.append(JingWeiConstants.ZK_PATH_SEP).append(taskName);
		path.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_STATUS_NODE_NAME);

		return path.toString();
	}

	/***
	 * �ж�executor�Ƿ����status�ڵ㣬e.g. /jingwei/groups/**group/tasks/**task/status
	 * @param groupName  ����
	 * @param taskName ������
	 * @return <code>true</code>  ���ڣ�<code>false</code>������
	 */
	public static boolean existExecutorStatus(ConfigManager configManager, String groupName, String taskName) {
		String path = getGroupStatusPath(groupName, taskName);

		return configManager.exists(path);
	}

	/**
	 * e.g. /jingwei/groups/**group/tasks
	 * @return
	 */
	public static Set<String> getGroupTasksFromZk(ConfigManager configManager, String groupName) {
		//path
		StringBuilder sb = new StringBuilder(JingWeiConstants.JINGWEI_GROUP_ROOT_PATH);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(groupName);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_GROUP_TASKS_NAME);

		// data
		Set<String> tasks = configManager.getChildDatas(sb.toString(), null).keySet();

		return tasks;
	}
}
