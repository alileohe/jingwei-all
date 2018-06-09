package com.taobao.jingwei.webconsole.biz.ao;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONException;

import com.taobao.jingwei.common.node.OperateNode.OperateEnum;
import com.taobao.jingwei.server.node.GroupNode;
import com.taobao.jingwei.webconsole.model.JingweiAssembledGroup;
import com.taobao.jingwei.webconsole.model.JingweiAssembledGroup.GroupServerInfo;
import com.taobao.jingwei.webconsole.model.JingweiAssembledGroup.GroupTaskInfo;
import com.taobao.jingwei.webconsole.model.JingweiGroupCriteria;

public interface JingweiGroupAO {
	/**
	 * 获取复合group信息 e.g. /jingwei/groups
	 * 
	 * @param criteria
	 * @param zkKey
	 * @return <code>empty set</code> 如果没有查到结果
	 */
	List<JingweiAssembledGroup> getJingweiAssembledGroups(JingweiGroupCriteria criteria, String zkKey);

	/**
	 * 获取所有的group e.g. /jingwei/groups
	 * 
	 * @param zkKey
	 * @return <code>empty set</code> 如果没有查到结果
	 */
	Set<String> getGroups(String zkKey);

	/**
	 * group内的一个任务的运行状态 e.g. /jingwei/groups/**group/tasks/**task
	 * 
	 * @param groupName group name
	 * @return {@link GroupTaskInfo}
	 */
	Set<GroupTaskInfo> getGroupTaskInfos(String groupName, String zkKey);

	/**
	 * group内的一个任务的运行状态 e.g. /jingwei/groups/**group/tasks/**task
	 * 
	 * @param groupName group name
	 * @return {@link GroupTaskInfo}
	 */
	Set<GroupTaskInfo> getGroupTaskInfos(String serverName, String groupName, String zkKey);

	/**
	 * group内的一个server的运行状态 e.g. /jingwei/servers/**server的属性
	 * 
	 * @param groupName group name
	 * @return {@link GroupServerInfo}
	 */
	Set<GroupServerInfo> getGroupServerInfos(Map<String, TreeSet<String>> serverGroups, String groupName, String zkKey);

	/**
	 * zk上一个group运行在那些server上
	 * 
	 * @param zkKey
	 * @return 如果没有配置group返回<code>empty map</code>
	 */
	Map<String, TreeSet<String>> getServerGroup(String zkKey);

	/**
	 * 添加group e.g. /jingwei/groups/**group
	 * 
	 * @param groupName
	 * @param zkKey
	 * @throws Exception 节点创建失败
	 */
	void addGroup(String groupName, String zkKey) throws Exception;

	/**
	 * 删除group e.g. /jingwei/groups/**group
	 * 
	 * @param groupName
	 * @param zkKey
	 * @throws Exception 删除节点失败
	 */
	void removeGroup(String groupName, String zkKey) throws Exception;

	/**
	 * 减少的任务则停止，增加的任务要启动
	 * 
	 * @param groupName
	 * @param taskNames
	 * @param zkKey
	 * @throws Exception 添加或删除失败
	 */
	void updateTaskSetting(String groupName, Set<String> taskNames, String zkKey) throws Exception;

	/**
	 * 修改server节点的内容
	 * 
	 * @param groupName
	 * @param hostNames
	 * @param zkKey
	 */
	void updateHostSetting(String groupName, Set<String> hostNames, String zkKey);

	/**
	 * e.g. /jingwei/groups/**group/tasks
	 * 
	 * @param groupName
	 * @param zkKey
	 * @return group有多少个task 空的集合如果group没有task
	 */
	Set<String> getTasks(String groupName, String zkKey);

	/**
	 * e.g. /jingwei/tasks子节点
	 * 
	 * @param zkKey
	 * @return <code>empty set</code> 如果没有查到结果
	 */
	Set<String> getTasks(String zkKey);

	/**
	 * 返回所有group的task
	 * 
	 * @param zkKey
	 * @return
	 */
	Set<String> getAllGroupTasks(String zkKey);

	/**
	 * 删除任务 e.g. /jingwei/groups/**group/tasks/**task
	 * 
	 * @param groupName
	 * @param taskName
	 * @param zkKey
	 * @throws Exception 删除失败
	 */
	void removeTask(String groupName, String taskName, String zkKey) throws Exception;

	/**
	 * 添加任务 e.g. /jingwei/groups/**group/tasks/**task
	 * 
	 * @param groupName
	 * @param taskName
	 * @param zkKey
	 * @throws Exception 添加失败
	 */
	void addTask(String groupName, String taskName, String zkKey) throws Exception;

	/**
	 * 启动停止task e.g. /jingwei/groups/**group/tasks/**task/operate
	 * 
	 * @param groupName
	 * @param taskName
	 * @param zkKey
	 * @throws Exception
	 * @throws JSONException
	 */
	void updateTaskOperate(String groupName, String taskName, OperateEnum operateEnum, String zkKey)
			throws JSONException, Exception;

	/**
	 * 更改是否支持批量修改的属性 /jingwei/groups/**group
	 * 
	 * @param GroupNode 精卫group node
	 * @param zkKey zk值
	 * @throws Exception
	 * @throws JSONException
	 */
	void updateGroupNode(String groupName, GroupNode groupNode, String zkKey) throws JSONException, Exception;

	/**
	 * 获取精卫group node节点
	 * 
	 * @param groupName 精卫group name
	 * @param zkKey zk值
	 * @return
	 * @throws JSONException
	 */
	GroupNode getGroupNode(String groupName, String zkKey) throws JSONException;

}
