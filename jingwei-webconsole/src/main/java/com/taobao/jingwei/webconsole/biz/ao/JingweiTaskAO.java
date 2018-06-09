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
	 * 新增task信息
	 * 
	 * @param taskNode
	 * @return
	 */
	public JingWeiResult addTaskInfo(AbstractNode taskNode, String zkKey);

	/**
	 * 修改task信息
	 * 
	 * @param taskNode
	 * @return
	 */
	public JingWeiResult updateTaskInfo(AbstractNode taskNode, String zkKey);

	/**
	 * 删除task信息
	 * 
	 * @param taskName
	 * @return
	 */
	public JingWeiResult deleteTaskInfo(String taskName, String zkKey);

	/**
	 * 获取单个task信息
	 * 
	 * @param taskName
	 * @return
	 */
	public SyncTaskNode getTaskInfo(String taskName, String zkKey);

	/**
	 * 获取task列表信息
	 * 
	 * @return
	 */
	public Map<String/* task name */, JingweiAssembledTask> getTasks(JingweiTaskCriteria criteria, String zkKey);

	/**
	 * 获取任务集合
	 * 
	 * @return
	 */
	public Set<String> getTaskSet(JingweiTaskCriteria criteria, String zkKey);

	/**
	 * 根据任务名获取Stats信息
	 * 
	 * @param taskName
	 *            任务名
	 * @param hostName
	 *            主机名
	 * @return StatsNode
	 */
	public StatsNode getStatsInfo(String taskName, String hostName, String zkKey);

	/**
	 * 根据task name获取所有主机的异常消息
	 * 
	 * @param taskName
	 *            任务名
	 * @return
	 */
	public Map<String/* host name */, AlarmNode> getAlarmInfo(String taskName, String zkKey);

	/**
	 * 获取tasks/[task_name]/last_commit节点
	 * 
	 * @param taskId
	 * @return
	 */
	public PositionNode getLastCommit(String taskId, String zkKey);

	/**
	 * 获取tasks/[task_name]/last_commit/* 节点
	 * 
	 * @param taskId
	 * @return
	 */
	public List<PositionNode> getLastCommits(String taskId, String zkKey);

	/**
	 * 更新位点
	 * 
	 * @param taskId
	 * @param zkKey
	 * @param value
	 * @throws Exception
	 * @throws JSONException
	 */
	public void updateLastCommit(String taskId, String zkKey, String value) throws JSONException, Exception;

	/**
	 * 检查任务下是否有运行的主机
	 * 
	 * @param taskId
	 * @param zkKey
	 * @return
	 */
	public boolean hasRunningHost(String taskId, String zkKey);

	/**
	 * 删除主机
	 * 
	 * @param taskName
	 * @param hostName
	 * @param zkKey
	 */
	public void delHost(String taskName, String hostName, String zkKey);

}
