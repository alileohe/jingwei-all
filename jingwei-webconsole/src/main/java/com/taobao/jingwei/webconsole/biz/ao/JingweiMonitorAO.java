package com.taobao.jingwei.webconsole.biz.ao;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.taobao.jingwei.common.node.monitor.MonitorParentNode;
import com.taobao.jingwei.common.node.monitor.MonitorTaskNode;
import com.taobao.jingwei.webconsole.model.JingweiAssembledMonitor;
import com.taobao.jingwei.webconsole.model.JingweiMonitorCriteria;

public interface JingweiMonitorAO {

	/**
	 * e.g. /jingwei/monitors/**monitor/tasks/**task�� /jingwei/monitors/**monitor/groups/**group
	 * ��ȡmonitor�б� �ں���monitor��ص�task�������б���monitor������״̬
	 * @param critera
	 * @param zkKey
	 * @return 
	 */
	List<JingweiAssembledMonitor> getMonitors(JingweiMonitorCriteria criteria, String zkKey);

	/**
	 * ��ȡmonitor�����б�
	 * @param zkKey
	 * @return
	 */
	Set<String> getMonitorNames(String zkKey);

	/**
	 * ��ȡָ���������ص�task������ e.g. /jingwei/tasks�ӽڵ�
	 * @param monitorName
	 * @param zkKey
	 * @return <code>null</code>��ʾʧ��
	 */
	Set<String> getTasks(String zkKey);

	/**
	 * ��ȡgroup������task������  /jingwei/groups/**group/tasks/**task
	 * @param groupName
	 * @param zkKey
	 * @return <code>null</code>��ʾʧ��
	 */
	Set<String> getGroupTasks(String groupName, String zkKey);

	/**
	 * ��ȡ����group  /jingwei/groups�ӽڵ�
	 * @param monitorName
	 * @param zkKey
	 * @return <code>null</code>��ʾʧ��
	 */
	Set<String> getGroups(String zkkey);

	/**
	 * ��ȡָ����������������Ϣ E.g. /jingwei/monitors/tasks/**task
	 * @param taskName
	 * @return <code>null</code>��ʾʧ��
	 */
	MonitorTaskNode getMonitorTaskNode(String taskName, String zkKey);

	/**
	 * ��ӻ��޸ļ�ض���
	 * @param monitorTaskNode
	 * @param zkKey
	 * @throws Exception 
	 */
	void addOrUpdateMonitorTaskNode(MonitorTaskNode monitorTaskNode, String zkKey) throws Exception;

	/**
	 * ɾ����ض���Ľڵ� e.g. /jingwei/monitors/tasks/**task
	 * @param taskName
	 * @param zkKey
	 */
	void deleteMonitorTaskNode(String taskName, String zkKey);

	/**
	 * ��ȡ�������е�task e.g. /jingwei/tasks���ӽڵ�
	 * @param zkKey
	 * @return 
	 */
	Set<String> getTaskNames(String zkKey);

	/**
	 * e.g. /jingwei/monitors/monitors�ӽڵ���д��task
	 * @param zkKey
	 * @return key monitorName�� value �� monitor��Ӧ��tasks
	 */
	Map<String, TreeSet<String>> getBusyTasks(String zkKey);

	/**
	 * e.g. /jingwei/monitors/monitors
	 * @param zkKey
	 * @return <code>null</code>û�����ݻ���ʧ��
	 */
	MonitorParentNode getGlobalConfig(String zkKey);

	/**
	 * e.g. /jingwei/monitors/monitors
	 * @param node
	 * @param zkKey
	 * @return
	 */
	boolean updateMonitorParentNode(MonitorParentNode node, String zkKey);

}
