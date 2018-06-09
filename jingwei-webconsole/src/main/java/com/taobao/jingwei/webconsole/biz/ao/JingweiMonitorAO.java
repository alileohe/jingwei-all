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
	 * e.g. /jingwei/monitors/**monitor/tasks/**task和 /jingwei/monitors/**monitor/groups/**group
	 * 获取monitor列表 内涵被monitor监控的task的名字列表，和monitor的运行状态
	 * @param critera
	 * @param zkKey
	 * @return 
	 */
	List<JingweiAssembledMonitor> getMonitors(JingweiMonitorCriteria criteria, String zkKey);

	/**
	 * 获取monitor名字列表
	 * @param zkKey
	 * @return
	 */
	Set<String> getMonitorNames(String zkKey);

	/**
	 * 获取指定监控器监控的task的名字 e.g. /jingwei/tasks子节点
	 * @param monitorName
	 * @param zkKey
	 * @return <code>null</code>表示失败
	 */
	Set<String> getTasks(String zkKey);

	/**
	 * 获取group下所有task的名字  /jingwei/groups/**group/tasks/**task
	 * @param groupName
	 * @param zkKey
	 * @return <code>null</code>表示失败
	 */
	Set<String> getGroupTasks(String groupName, String zkKey);

	/**
	 * 获取所有group  /jingwei/groups子节点
	 * @param monitorName
	 * @param zkKey
	 * @return <code>null</code>表示失败
	 */
	Set<String> getGroups(String zkkey);

	/**
	 * 获取指定监控器监控配置信息 E.g. /jingwei/monitors/tasks/**task
	 * @param taskName
	 * @return <code>null</code>表示失败
	 */
	MonitorTaskNode getMonitorTaskNode(String taskName, String zkKey);

	/**
	 * 添加或修改监控对象
	 * @param monitorTaskNode
	 * @param zkKey
	 * @throws Exception 
	 */
	void addOrUpdateMonitorTaskNode(MonitorTaskNode monitorTaskNode, String zkKey) throws Exception;

	/**
	 * 删除监控对象的节点 e.g. /jingwei/monitors/tasks/**task
	 * @param taskName
	 * @param zkKey
	 */
	void deleteMonitorTaskNode(String taskName, String zkKey);

	/**
	 * 获取精卫所有的task e.g. /jingwei/tasks的子节点
	 * @param zkKey
	 * @return 
	 */
	Set<String> getTaskNames(String zkKey);

	/**
	 * e.g. /jingwei/monitors/monitors子节点上写的task
	 * @param zkKey
	 * @return key monitorName， value ： monitor对应的tasks
	 */
	Map<String, TreeSet<String>> getBusyTasks(String zkKey);

	/**
	 * e.g. /jingwei/monitors/monitors
	 * @param zkKey
	 * @return <code>null</code>没有数据或者失败
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
