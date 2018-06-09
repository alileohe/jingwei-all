/**
 * 
 */
package com.taobao.jingwei.webconsole.biz.ao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;

import com.taobao.jingwei.common.node.AbstractNode;
import com.taobao.jingwei.common.node.AlarmNode;
import com.taobao.jingwei.common.node.PositionNode;
import com.taobao.jingwei.common.node.StatsNode;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode;
import com.taobao.jingwei.webconsole.model.JingWeiResult;
import com.taobao.jingwei.webconsole.model.JingweiAssembledTask;
import com.taobao.jingwei.webconsole.model.JingweiTaskCriteria;

/**
 * @author qingren
 * 
 */
public interface JingweiTaskAO {
	/**
	 * ����task��Ϣ
	 * 
	 * @param taskNode
	 * @return
	 */
	public JingWeiResult addTaskInfo(AbstractNode taskNode, String zkKey);

	/**
	 * �޸�task��Ϣ
	 * 
	 * @param taskNode
	 * @return
	 */
	public JingWeiResult updateTaskInfo(AbstractNode taskNode, String zkKey);

	/**
	 * ɾ��task��Ϣ
	 * 
	 * @param taskName
	 * @return
	 */
	public JingWeiResult deleteTaskInfo(String taskName, String zkKey);

	/**
	 * ��ȡ����task��Ϣ
	 * 
	 * @param taskName
	 * @return
	 */
	public SyncTaskNode getTaskInfo(String taskName, String zkKey);

	/**
	 * ��ȡtask�б���Ϣ
	 * 
	 * @return
	 */
	public Map<String/* task name */, JingweiAssembledTask> getTasks(JingweiTaskCriteria criteria, String zkKey);

	/**
	 * ��ȡ���񼯺�
	 * 
	 * @return
	 */
	public Set<String> getTaskSet(JingweiTaskCriteria criteria, String zkKey);

	/**
	 * ������������ȡStats��Ϣ
	 * 
	 * @param taskName
	 *            ������
	 * @param hostName
	 *            ������
	 * @return StatsNode
	 */
	public StatsNode getStatsInfo(String taskName, String hostName, String zkKey);

	/**
	 * ����task name��ȡ�����������쳣��Ϣ
	 * 
	 * @param taskName
	 *            ������
	 * @return
	 */
	public Map<String/* host name */, AlarmNode> getAlarmInfo(String taskName, String zkKey);

	/**
	 * ��ȡtasks/[task_name]/last_commit�ڵ�
	 * 
	 * @param taskId
	 * @return
	 */
	public PositionNode getLastCommit(String taskId, String zkKey);

	/**
	 * ��ȡtasks/[task_name]/last_commit/* �ڵ�
	 * 
	 * @param taskId
	 * @return
	 */
	public List<PositionNode> getLastCommits(String taskId, String zkKey);

	/**
	 * ����λ��
	 * 
	 * @param taskId
	 * @param zkKey
	 * @param value
	 * @throws Exception
	 * @throws JSONException
	 */
	public void updateLastCommit(String taskId, String zkKey, String value) throws JSONException, Exception;

	/**
	 * ����������Ƿ������е�����
	 * 
	 * @param taskId
	 * @param zkKey
	 * @return
	 */
	public boolean hasRunningHost(String taskId, String zkKey);

	/**
	 * ɾ������
	 * 
	 * @param taskName
	 * @param hostName
	 * @param zkKey
	 */
	public void delHost(String taskName, String hostName, String zkKey);

}
