package com.taobao.jingwei.webconsole.biz.ao;

import java.util.List;
import java.util.Set;

import com.taobao.jingwei.common.node.OperateNode;
import com.taobao.jingwei.common.node.OperateNode.OperateEnum;
import com.taobao.jingwei.common.node.StatusNode;
import com.taobao.jingwei.common.node.server.ServerNode;
import com.taobao.jingwei.common.node.server.ServerTaskNode;
import com.taobao.jingwei.webconsole.model.JingWeiResult;
import com.taobao.jingwei.webconsole.model.JingweiAssembledServer;
import com.taobao.jingwei.webconsole.model.JingweiServerCriteria;
import com.taobao.jingwei.webconsole.model.JingweiTaskViewItem;

public interface JingweiServerAO {
	/**
	 * 根据条件检索server
	 * @param criteria
	 * @param zkKey
	 * @return
	 */
	List<JingweiAssembledServer> getServers(JingweiServerCriteria criteria, String zkKey);

	List<JingweiAssembledServer> getServers(String zkKey);

	/**
	 * 获取所有的Buildin类型的任务名
	 * 
	 * @param zkKey
	 * @return
	 */
	Set<String> getBuildinTaskNames(String zkKey);

	/**
	 * 获取所有任务运行在哪些主机上的运行状态
	 * 
	 * @param zkKey
	 * @return
	 */
	List<JingweiTaskViewItem> getTasks(String zkKey);

	/**
	 * 获取所有task名<br>
	 * 此操作较为耗时, 需要缓存机制
	 * 
	 * @param zkKey
	 * @return
	 */
	Set<String> getTaskNames(String zkKey);

	/**
	 * 添加server节点，路径 E.g. /jingwei/servers/**server
	 * 
	 * @param serverNode
	 */
	JingWeiResult addServerNode(ServerNode serverNode, String zkKey);

	/**
	 * 删除serverNode，路径 E.g. /jingwei/servers/**server
	 * 
	 * @param serverName server name
	 */
	JingWeiResult deleteServerNode(String serverName, String zkKey);

	/**
	 * 获取所有ServerNode节点server name
	 */
	Set<String> getServerNames(String zkKey);

	/**
	 * 添加serverTaskNode，路径 E.g. /jingwei/servers/**server/tasks/**task
	 * 
	 * @param serverTaskNode
	 */
	JingWeiResult addServerTaskNode(ServerTaskNode serverTaskNode, String zkKey);

	/**
	 * 删除serverTaskNode，路径 E.g. /jingwei/servers/**server/tasks/**task
	 * 
	 * @param serverTaskNode
	 */
	JingWeiResult deleteServerTaskNode(ServerTaskNode serverTaskNode, String zkKey);

	/**
	 * 更新serverTaskNode，路径 E.g. /jingwei/servers/**server/tasks/**task
	 * 
	 * @param serverTaskNode
	 */
	JingWeiResult updateServerTaskNode(ServerTaskNode serverTaskNode, String zkKey);

	/**
	 * 获取server node节点下所有的ServerTaskNode, E.g./jingwei/servers/**server/tasks
	 * /jingwei/servers/**server/tasks的子节点
	 * 
	 * @param serverNode
	 */
	Set<ServerTaskNode> getServerTaskNodes(ServerNode serverNode, String zkKey);
	
	ServerTaskNode getServerTaskNode(String serverName, String taskName, String zkKey);

	/**
	 * 获取server node节点下所有的任务名, E.g./jingwei/servers/**server/tasks
	 * @param serverName
	 * @param zkKey
	 * @return
	 */
	Set<String> getTaskNames(String serverName, String zkKey);

	/**
	 * 获取serverTaskNode的op
	 * 
	 * @param serverTaskNode
	 * @return OperateNode <code>null</code>表示获取失败
	 */
	OperateNode getServerTaskOperate(ServerTaskNode serverTaskNode, String zkKey);

	/**
	 * 修改serverTaskNode对应的operate值
	 * 
	 * @param serverTaskNode
	 * @param operateEnum
	 */
	JingWeiResult updateServerTaskOperate(ServerTaskNode serverTaskNode, OperateEnum operateEnum, String zkKey);

	/**
	 * 获取任务运行状态 E.g. /jingwei/tasks/**task/hosts/**host/status
	 * 
	 * @param serverName
	 * @param taskName
	 * @return 对应的状态节点<code>null</code> 表示失败,或节点不存在
	 */
	StatusNode getStatusNode(String serverName, String taskName, String zkKey);

	/**
	 * E.g. /jingwei/servers/**server/status
	 * 
	 * @param serverName
	 * @param zkKey
	 * @return 对应的状态节点<code>null</code> 表示失败,或节点不存在
	 */
	StatusNode getServerStatus(String serverName, String zkKey);

	/**
	 * 获取server上关联的group的名字
	 * @param serverName
	 * @param zkKey
	 * @return <code>empty set</code>如果不存在server节点或者节点上没有groups属性，或者groups属性为空 
	 */
	Set<String> getGroupNames(String serverName, String zkKey);

	/**
	 * 向server上关联的添加group名 E.g. /jingwei/servers/**server
	 * @param serverName
	 * @param groupName
	 * @param zkKey
	 */
	void addGroup(String serverName, Set<String> groupName, String zkKey);

	/**
	 * 向server上关联的删除group名 E.g. /jingwei/servers/**server
	 * @param serverName
	 * @param groupName
	 * @param zkKey
	 */
	void removeGroup(String serverName, String groupName, String zkKey);

	/**
	 * e.g. /jingwei/servers/**server的属性
	 * @param serverName
	 * @param zkKey
	 * @return  <code>null</code>表示zk异常，或json异常
	 */
	ServerNode getServerNode(String serverName, String zkKey);

	/**
	 * e.g. /jingwei/tasks/**task/t-locks子节点数量
	 * @param serverName
	 * @param taskName
	 * @param zkKey
	 * @return
	 */
	int getRunningTaskCount(String serverName, String taskName, String zkKey);

	
}
