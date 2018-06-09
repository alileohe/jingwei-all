package com.taobao.jingwei.webconsole.biz.ao.impl;

import java.util.Set;

import jodd.util.StringUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.TaskUtil;
import com.taobao.jingwei.common.config.ConfigManager;
import com.taobao.jingwei.common.node.OperateNode;
import com.taobao.jingwei.common.node.OperateNode.OperateEnum;
import com.taobao.jingwei.common.node.monitor.MonitorTaskNode;

/**
 * @desc 
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Jun 13, 2012 2:28:31 PM
 */

public class JingweiMonitorUtil implements JingWeiConstants {
	private static final Log log = LogFactory.getLog(JingweiMonitorUtil.class);

	/**
	 * e.g. /jingwei/monitors/monitors/**monitor
	 * @return
	 */
	public static String getMonitorSelfPath(String monitorName) {
		StringBuilder sb = new StringBuilder(JINGWEI_MONITOR_ROOT_PATH);
		sb.append(ZK_PATH_SEP).append(JINGWEI_MONITOR_MONITORS_NODE_NAME);
		sb.append(ZK_PATH_SEP).append(monitorName);

		return sb.toString();
	}

	/**
	 * e.g. /jingwei/monitors/tasks/**task
	 * @return
	 */
	public static String getMonitorTaskPath(String taskName) {
		StringBuilder sb = new StringBuilder(JINGWEI_MONITOR_ROOT_PATH);
		sb.append(ZK_PATH_SEP).append(JINGWEI_MONITOR_TASKS_NAME);
		sb.append(ZK_PATH_SEP).append(taskName);

		return sb.toString();
	}

	/**
	 * e.g. /jingwei/monitors/groups/**group/host ����
	 * @param configManager
	 * @param groupName
	 * @return <code>null</code> �쳣�򲻴���
	 */
	public static String getMonitorGroupHost(ConfigManager configManager, String groupName) {
		String path = JingweiMonitorUtil.getMonitorGroupHostPath(groupName);

		return configManager.getData(path);
	}

	/**
	 * e.g. /jingwei/monitors/tasks
	 * @return
	 */
	public static String getMonitorTasksPath() {
		StringBuilder sb = new StringBuilder(JINGWEI_MONITOR_ROOT_PATH);
		sb.append(ZK_PATH_SEP).append(JINGWEI_MONITOR_TASKS_NAME);

		return sb.toString();
	}

	/**
	 * e.g. /jingwei/monitors/groups
	 * @return
	 */
	public static String getMonitorGroupsPath() {
		StringBuilder sb = new StringBuilder(JINGWEI_MONITOR_ROOT_PATH);
		sb.append(ZK_PATH_SEP).append(JINGWEI_MONITOR_GROUPS_NAME);

		return sb.toString();
	}

	/**
	 * e.g. /jingwei/monitors/groups/**group
	 * @return
	 */
	public static String getMonitorGroupPath(String groupName) {
		StringBuilder sb = new StringBuilder(JINGWEI_MONITOR_ROOT_PATH);
		sb.append(ZK_PATH_SEP).append(JINGWEI_MONITOR_GROUPS_NAME);
		sb.append(ZK_PATH_SEP).append(groupName);

		return sb.toString();
	}

	/**
	 * e.g. /jingwei/monitors/groups/**group/host
	 * @return
	 */
	public static String getMonitorGroupHostPath(String groupName) {
		StringBuilder sb = new StringBuilder(JINGWEI_MONITOR_ROOT_PATH);
		sb.append(ZK_PATH_SEP).append(JINGWEI_MONITOR_GROUPS_NAME);
		sb.append(ZK_PATH_SEP).append(groupName);
		sb.append(ZK_PATH_SEP).append(JINGWEI_MONITOR_TASK_HOST_NAME);

		return sb.toString();
	}

	/***
	 * /jingwei/monitors/tasks/**task��Ӧ�Ľڵ�
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
			log.error(
					"[jingwei monitor] MonitorTaskNode json string convert error, failed add monitor configration to mem ",
					e);
			return null;
		}
	}

	/**
	 * ��ȡstatus�ڵ�path
	 * @param taskName ������
	 * @param hostName ������
	 * @return �����ڵ�zk·�� e.g /jingwei/servers/**server/tasks/**task/operate
	 */
	public static String getTaskOperateAtHostPath(String serverName, String taskName) {
		StringBuilder sb = new StringBuilder(JingWeiConstants.JINGWEI_SERVER_ROOT_PATH);

		sb.append(JingWeiConstants.ZK_PATH_SEP).append(serverName);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_SERVER_TASKS_NAME);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(taskName);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_OPERATE_NODE_NAME);

		return sb.toString();
	}

	public static boolean taskOperateIsStartAtHost(ConfigManager configManager, String serverName, String taskName) {
		String path = getTaskOperateAtHostPath(serverName, taskName);
		String data = configManager.getData(path);

		if (StringUtil.isBlank(data)) {
			return false;
		}

		OperateNode opNode = new OperateNode();

		try {
			opNode.jsonStringToNodeSelf(data);
		} catch (JSONException e) {
			return false;
		}

		if (opNode.getOperateEnum() == OperateEnum.NODE_START) {
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
	public static boolean isGroupTaskStart(ConfigManager configManager, String groupName, String taskName) {
		// path
		String path = getGroupTaskOpPath(groupName, taskName);

		String data = configManager.getData(path);

		if (StringUtil.isBlank(data)) {
			return false;
		}

		OperateNode opNode = new OperateNode();
		try {
			opNode.jsonStringToNodeSelf(data);
			if (opNode.getOperateEnum() == OperateEnum.NODE_START) {
				return true;
			} else {
				return false;
			}
		} catch (JSONException e) {
			return false;
		}
	}

	/**
	 * e.g /jingwei/servers/**server/tasks/**task/operate��,�������һ��������Ҫ
	 * @param taskName
	 * @param configManager
	 * @return
	 */
	public static boolean isTaskHasStartAtServer(String taskName, ConfigManager configManager) {
		//  e.g /jingwei-v2/tasks/**task/hosts
		String path = TaskUtil.getTaskHostPath(taskName);
		Set<String> hosts = configManager.getChildDatas(path, null).keySet();

		for (String host : hosts) {
			boolean startOp = JingweiMonitorUtil.taskOperateIsStartAtHost(configManager, host, taskName);

			if (startOp) {
				return true;
			}
		}

		return false;
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
}
