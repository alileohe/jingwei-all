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
	 * ��ȡ����group��Ϣ e.g. /jingwei/groups
	 * 
	 * @param criteria
	 * @param zkKey
	 * @return <code>empty set</code> ���û�в鵽���
	 */
	List<JingweiAssembledGroup> getJingweiAssembledGroups(JingweiGroupCriteria criteria, String zkKey);

	/**
	 * ��ȡ���е�group e.g. /jingwei/groups
	 * 
	 * @param zkKey
	 * @return <code>empty set</code> ���û�в鵽���
	 */
	Set<String> getGroups(String zkKey);

	/**
	 * group�ڵ�һ�����������״̬ e.g. /jingwei/groups/**group/tasks/**task
	 * 
	 * @param groupName group name
	 * @return {@link GroupTaskInfo}
	 */
	Set<GroupTaskInfo> getGroupTaskInfos(String groupName, String zkKey);

	/**
	 * group�ڵ�һ�����������״̬ e.g. /jingwei/groups/**group/tasks/**task
	 * 
	 * @param groupName group name
	 * @return {@link GroupTaskInfo}
	 */
	Set<GroupTaskInfo> getGroupTaskInfos(String serverName, String groupName, String zkKey);

	/**
	 * group�ڵ�һ��server������״̬ e.g. /jingwei/servers/**server������
	 * 
	 * @param groupName group name
	 * @return {@link GroupServerInfo}
	 */
	Set<GroupServerInfo> getGroupServerInfos(Map<String, TreeSet<String>> serverGroups, String groupName, String zkKey);

	/**
	 * zk��һ��group��������Щserver��
	 * 
	 * @param zkKey
	 * @return ���û������group����<code>empty map</code>
	 */
	Map<String, TreeSet<String>> getServerGroup(String zkKey);

	/**
	 * ���group e.g. /jingwei/groups/**group
	 * 
	 * @param groupName
	 * @param zkKey
	 * @throws Exception �ڵ㴴��ʧ��
	 */
	void addGroup(String groupName, String zkKey) throws Exception;

	/**
	 * ɾ��group e.g. /jingwei/groups/**group
	 * 
	 * @param groupName
	 * @param zkKey
	 * @throws Exception ɾ���ڵ�ʧ��
	 */
	void removeGroup(String groupName, String zkKey) throws Exception;

	/**
	 * ���ٵ�������ֹͣ�����ӵ�����Ҫ����
	 * 
	 * @param groupName
	 * @param taskNames
	 * @param zkKey
	 * @throws Exception ��ӻ�ɾ��ʧ��
	 */
	void updateTaskSetting(String groupName, Set<String> taskNames, String zkKey) throws Exception;

	/**
	 * �޸�server�ڵ������
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
	 * @return group�ж��ٸ�task �յļ������groupû��task
	 */
	Set<String> getTasks(String groupName, String zkKey);

	/**
	 * e.g. /jingwei/tasks�ӽڵ�
	 * 
	 * @param zkKey
	 * @return <code>empty set</code> ���û�в鵽���
	 */
	Set<String> getTasks(String zkKey);

	/**
	 * ��������group��task
	 * 
	 * @param zkKey
	 * @return
	 */
	Set<String> getAllGroupTasks(String zkKey);

	/**
	 * ɾ������ e.g. /jingwei/groups/**group/tasks/**task
	 * 
	 * @param groupName
	 * @param taskName
	 * @param zkKey
	 * @throws Exception ɾ��ʧ��
	 */
	void removeTask(String groupName, String taskName, String zkKey) throws Exception;

	/**
	 * ������� e.g. /jingwei/groups/**group/tasks/**task
	 * 
	 * @param groupName
	 * @param taskName
	 * @param zkKey
	 * @throws Exception ���ʧ��
	 */
	void addTask(String groupName, String taskName, String zkKey) throws Exception;

	/**
	 * ����ֹͣtask e.g. /jingwei/groups/**group/tasks/**task/operate
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
	 * �����Ƿ�֧�������޸ĵ����� /jingwei/groups/**group
	 * 
	 * @param GroupNode ����group node
	 * @param zkKey zkֵ
	 * @throws Exception
	 * @throws JSONException
	 */
	void updateGroupNode(String groupName, GroupNode groupNode, String zkKey) throws JSONException, Exception;

	/**
	 * ��ȡ����group node�ڵ�
	 * 
	 * @param groupName ����group name
	 * @param zkKey zkֵ
	 * @return
	 * @throws JSONException
	 */
	GroupNode getGroupNode(String groupName, String zkKey) throws JSONException;

}
