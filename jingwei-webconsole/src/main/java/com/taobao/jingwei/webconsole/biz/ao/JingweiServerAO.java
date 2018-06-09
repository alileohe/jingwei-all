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
	 * ������������server
	 * @param criteria
	 * @param zkKey
	 * @return
	 */
	List<JingweiAssembledServer> getServers(JingweiServerCriteria criteria, String zkKey);

	List<JingweiAssembledServer> getServers(String zkKey);

	/**
	 * ��ȡ���е�Buildin���͵�������
	 * 
	 * @param zkKey
	 * @return
	 */
	Set<String> getBuildinTaskNames(String zkKey);

	/**
	 * ��ȡ����������������Щ�����ϵ�����״̬
	 * 
	 * @param zkKey
	 * @return
	 */
	List<JingweiTaskViewItem> getTasks(String zkKey);

	/**
	 * ��ȡ����task��<br>
	 * �˲�����Ϊ��ʱ, ��Ҫ�������
	 * 
	 * @param zkKey
	 * @return
	 */
	Set<String> getTaskNames(String zkKey);

	/**
	 * ���server�ڵ㣬·�� E.g. /jingwei/servers/**server
	 * 
	 * @param serverNode
	 */
	JingWeiResult addServerNode(ServerNode serverNode, String zkKey);

	/**
	 * ɾ��serverNode��·�� E.g. /jingwei/servers/**server
	 * 
	 * @param serverName server name
	 */
	JingWeiResult deleteServerNode(String serverName, String zkKey);

	/**
	 * ��ȡ����ServerNode�ڵ�server name
	 */
	Set<String> getServerNames(String zkKey);

	/**
	 * ���serverTaskNode��·�� E.g. /jingwei/servers/**server/tasks/**task
	 * 
	 * @param serverTaskNode
	 */
	JingWeiResult addServerTaskNode(ServerTaskNode serverTaskNode, String zkKey);

	/**
	 * ɾ��serverTaskNode��·�� E.g. /jingwei/servers/**server/tasks/**task
	 * 
	 * @param serverTaskNode
	 */
	JingWeiResult deleteServerTaskNode(ServerTaskNode serverTaskNode, String zkKey);

	/**
	 * ����serverTaskNode��·�� E.g. /jingwei/servers/**server/tasks/**task
	 * 
	 * @param serverTaskNode
	 */
	JingWeiResult updateServerTaskNode(ServerTaskNode serverTaskNode, String zkKey);

	/**
	 * ��ȡserver node�ڵ������е�ServerTaskNode, E.g./jingwei/servers/**server/tasks
	 * /jingwei/servers/**server/tasks���ӽڵ�
	 * 
	 * @param serverNode
	 */
	Set<ServerTaskNode> getServerTaskNodes(ServerNode serverNode, String zkKey);
	
	ServerTaskNode getServerTaskNode(String serverName, String taskName, String zkKey);

	/**
	 * ��ȡserver node�ڵ������е�������, E.g./jingwei/servers/**server/tasks
	 * @param serverName
	 * @param zkKey
	 * @return
	 */
	Set<String> getTaskNames(String serverName, String zkKey);

	/**
	 * ��ȡserverTaskNode��op
	 * 
	 * @param serverTaskNode
	 * @return OperateNode <code>null</code>��ʾ��ȡʧ��
	 */
	OperateNode getServerTaskOperate(ServerTaskNode serverTaskNode, String zkKey);

	/**
	 * �޸�serverTaskNode��Ӧ��operateֵ
	 * 
	 * @param serverTaskNode
	 * @param operateEnum
	 */
	JingWeiResult updateServerTaskOperate(ServerTaskNode serverTaskNode, OperateEnum operateEnum, String zkKey);

	/**
	 * ��ȡ��������״̬ E.g. /jingwei/tasks/**task/hosts/**host/status
	 * 
	 * @param serverName
	 * @param taskName
	 * @return ��Ӧ��״̬�ڵ�<code>null</code> ��ʾʧ��,��ڵ㲻����
	 */
	StatusNode getStatusNode(String serverName, String taskName, String zkKey);

	/**
	 * E.g. /jingwei/servers/**server/status
	 * 
	 * @param serverName
	 * @param zkKey
	 * @return ��Ӧ��״̬�ڵ�<code>null</code> ��ʾʧ��,��ڵ㲻����
	 */
	StatusNode getServerStatus(String serverName, String zkKey);

	/**
	 * ��ȡserver�Ϲ�����group������
	 * @param serverName
	 * @param zkKey
	 * @return <code>empty set</code>���������server�ڵ���߽ڵ���û��groups���ԣ�����groups����Ϊ�� 
	 */
	Set<String> getGroupNames(String serverName, String zkKey);

	/**
	 * ��server�Ϲ��������group�� E.g. /jingwei/servers/**server
	 * @param serverName
	 * @param groupName
	 * @param zkKey
	 */
	void addGroup(String serverName, Set<String> groupName, String zkKey);

	/**
	 * ��server�Ϲ�����ɾ��group�� E.g. /jingwei/servers/**server
	 * @param serverName
	 * @param groupName
	 * @param zkKey
	 */
	void removeGroup(String serverName, String groupName, String zkKey);

	/**
	 * e.g. /jingwei/servers/**server������
	 * @param serverName
	 * @param zkKey
	 * @return  <code>null</code>��ʾzk�쳣����json�쳣
	 */
	ServerNode getServerNode(String serverName, String zkKey);

	/**
	 * e.g. /jingwei/tasks/**task/t-locks�ӽڵ�����
	 * @param serverName
	 * @param taskName
	 * @param zkKey
	 * @return
	 */
	int getRunningTaskCount(String serverName, String taskName, String zkKey);

	
}
