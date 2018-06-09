package com.taobao.jingwei.webconsole.web.module.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.TaskUtil;
import com.taobao.jingwei.common.config.ConfigManager;
import com.taobao.jingwei.common.node.OperateNode;
import com.taobao.jingwei.common.node.OperateNode.OperateEnum;
import com.taobao.jingwei.webconsole.biz.ao.JingweiGroupAO;
import com.taobao.jingwei.webconsole.biz.manager.JingweiZkConfigManager;

public class JingweiGroupAction implements JingWeiConstants {
	private Log log = LogFactory.getLog(this.getClass());

	private final static String SEP_COMMA = ",";
	@Autowired
	private JingweiGroupAO jwGroupAO;

	@Autowired
	private HttpServletResponse response;

	@Autowired
	private JingweiZkConfigManager jwConfigManager;

	/**
	 * ��ȡgroup��ѡ��task
	 * @param context
	 * @param host
	 * @param serverName
	 */
	public void doGetCandidateTasks(Context context, @Param(name = "host") String host,
			@Param(name = "groupName") String groupName) {
		// ���е�task
		Set<String> allTasks = jwGroupAO.getTasks(host);

		Set<String> allGroups = jwGroupAO.getGroups(host);

		for (String group : allGroups) {
			// ���group�Ѿ��е�task
			Set<String> groupTasks = jwGroupAO.getTasks(group, host);
			allTasks.removeAll(groupTasks);
		}

		// �����б�
		Set<String> candidates = allTasks;

		// �ų���start op��������Щ�����ܼ��뵽group���͵�������ȥ
		Set<String> startTasks = new HashSet<String>();

		for (String taskName : candidates) {

			Set<String> hosts = this.getTaskHosts(jwConfigManager.getZkConfigManager(host), taskName);

			for (String hostName : hosts) {
				boolean isStartOp = this.isServerTaskStartOp(jwConfigManager.getZkConfigManager(host), hostName,
						taskName);

				if (isStartOp) {
					startTasks.add(taskName);
					break;
				}
			}
		}

		candidates.removeAll(startTasks);

		List<String> list = new ArrayList<String>();
		list.addAll(candidates);
		Collections.sort(list);

		PrintWriter writer = null;
		JSONObject jsonObj = new JSONObject();

		JSONArray jsonArray = new JSONArray(list);

		try {
			jsonObj.put("candidates", jsonArray);
			writer = response.getWriter();
			response.setContentType("application/json;charset=utf-8");
			if (null != writer) {
				writer.write(jsonObj.toString());
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			if (null != writer) {
				writer.flush();
				writer.close();
			}
		}
	}

	/**
	 *  e.g. /jingwei/tasks/**task/t-locks�ӽڵ�
	 * @param configManager
	 * @param taskName
	 * @return 
	 */
	public Map<String, String> getTaskLocksCount(ConfigManager configManager, String taskName) {
		String path = this.getTaskLocksPath(taskName);
		return configManager.getChildDatas(path, null);
	}

	/**
	 * e.g. /jingwei/tasks/**task/t-locks�ӽڵ�
	 * @param groupName
	 * @param taskName
	 * @return
	 */
	private String getTaskLocksPath(String taskName) {
		StringBuilder path = new StringBuilder(JINGWEI_TASK_ROOT_PATH);

		path.append(ZK_PATH_SEP).append(taskName);
		path.append(ZK_PATH_SEP).append(JINGWEI_INSTANCE_TASK_LOCKS_NODE_NAME);

		return path.toString();
	}

	/**
	 * 	�������
	 * @param context
	 * @param host
	 * @param groupName
	 * @param tasks
	 */
	public void doAddTasks(Context context, @Param(name = "host") String host,
			@Param(name = "groupName") String groupName, @Param(name = "targets") String tasks) {

		String[] taskNames = tasks.split(SEP_COMMA);

		for (String taskName : taskNames) {
			try {
				jwGroupAO.addTask(groupName, taskName, host);
			} catch (Exception e) {
				log.error(e);
			}
		}

		context.remove("groupName");
	}

	/**
	 * 	�������
	 * @param context
	 * @param host
	 * @param groupName
	 * @param tasks
	 */
	public void doDeleteTask(Context context, @Param(name = "host") String host,
			@Param(name = "groupName") String groupName, @Param(name = "taskName") String taskName,
			@Param(name = "groupNameCriteria") String groupNameCriteria, @Param(name = "pageCount") String pageCount,
			@Param(name = "currentPage") String currentPage, @Param(name = "pageSizeInt") String pageSizeInt) {
		context.put("groupNameCriteria", groupNameCriteria);
		context.put("pageCount", pageCount);
		context.put("currentPage", currentPage);
		context.put("pageSizeInt", pageSizeInt);
		try {
			jwGroupAO.removeTask(groupName, taskName, host);
		} catch (Exception e) {
			log.error(e);
		}
	}

	/**
	 * 	���group
	 * @param context
	 * @param host
	 * @param groupName
	 */
	public void doAddGroup(Context context, @Param(name = "host") String host,
			@Param(name = "groupName") String groupName) {

		if (StringUtil.isBlank(groupName) || groupName.equals(DEFAULT_GROUP)) {
			context.put("messages", groupName + "(group����ʹ��DEFAULT��Ϊ��!)");
			return;
		}

		Set<String> tasks = TaskUtil.getAllTasks(jwConfigManager.getZkConfigManager(host));
		if (tasks.contains(groupName)) {
			context.put("messages", groupName + "(group����ʹ���Ѿ����ڵ�task����!)");
			return;
		}
		try {
			jwGroupAO.addGroup(groupName, host);
		} catch (Exception e) {
			if (e instanceof ZkNodeExistsException) {
				context.put("messages", groupName + " �Ѿ����ڣ�");
			}
			log.error(e);
		}
	}

	/**
	 * 	ɾ��group
	 * @param context
	 * @param host
	 * @param groupName

	 */
	public void doRemoveGroup(Context context, @Param(name = "host") String host,
			@Param(name = "groupName") String groupName) {
		try {
			jwGroupAO.removeGroup(groupName, host);
		} catch (Exception e) {
			log.error(e.getStackTrace());
		}
	}

	/**
	 * 	ɾ��group
	 * @param context
	 * @param host
	 * @param groupName

	 */
	public void doStartTask(Context context, @Param(name = "host") String host,
			@Param(name = "groupName") String groupName, @Param(name = "taskName") String taskName,
			@Param(name = "groupNameCriteria") String groupNameCriteria, @Param(name = "pageCount") String pageCount,
			@Param(name = "currentPage") String currentPage, @Param(name = "pageSizeInt") String pageSizeInt) {
		context.put("groupNameCriteria", groupNameCriteria);
		context.put("pageCount", pageCount);
		context.put("currentPage", currentPage);
		context.put("pageSizeInt", pageSizeInt);
		try {
			jwGroupAO.updateTaskOperate(groupName, taskName, OperateEnum.NODE_START, host);
		} catch (Exception e) {
			log.error(e.getStackTrace());
		}
	}

	/**
	 * 	ɾ��group
	 * @param context
	 * @param host
	 * @param groupName

	 */
	public void doStopTask(Context context, @Param(name = "host") String host,
			@Param(name = "groupName") String groupName, @Param(name = "taskName") String taskName,
			@Param(name = "groupNameCriteria") String groupNameCriteria, @Param(name = "pageCount") String pageCount,
			@Param(name = "currentPage") String currentPage, @Param(name = "pageSizeInt") String pageSizeInt) {
		context.put("groupNameCriteria", groupNameCriteria);
		context.put("pageCount", pageCount);
		context.put("currentPage", currentPage);
		context.put("pageSizeInt", pageSizeInt);
		try {
			jwGroupAO.updateTaskOperate(groupName, taskName, OperateEnum.NODE_STOP, host);
		} catch (Exception e) {
			log.error(e.getStackTrace());
		}
	}

	/**
	 *  e.g. /jingwei/servers/**server/tasks/**task/operate
	 * @param configManager
	 * @param serverName
	 * @param taskName
	 * @return ֻ��START�ŷ���true,���򷵻�false
	 */
	private boolean isServerTaskStartOp(ConfigManager configManager, String serverName, String taskName) {
		// path
		String path = this.getServerTaskOpPath(serverName, taskName);

		String data = configManager.getData(path);

		OperateNode opNode = new OperateNode();
		try {
			opNode.jsonStringToNodeSelf(data);
		} catch (JSONException e) {
			log.error(e);
			return false;
		}

		if (opNode.getOperateEnum() == OperateEnum.NODE_START) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * e.g. /jingwei/servers/**server/tasks/**task/operate
	 * @param taskName
	 * @param taskName
	 * @return
	 */
	private String getServerTaskOpPath(String serverName, String taskName) {
		StringBuilder sb = new StringBuilder(JINGWEI_SERVER_ROOT_PATH);
		sb.append(ZK_PATH_SEP).append(serverName);
		sb.append(ZK_PATH_SEP).append(JINGWEI_SERVER_TASKS_NAME);
		sb.append(ZK_PATH_SEP).append(taskName);
		sb.append(ZK_PATH_SEP).append(JINGWEI_OPERATE_NODE_NAME);

		return sb.toString();

	}

	/**
	 * ��ȡtask��host���ڵ�zk·��
	 * @param taskName ������
	 * @return host���ڵ�zk ·�� e.g /jingwei-v2/tasks/**task/hosts
	 */
	private String getTaskHostsPath(String taskName) {
		StringBuilder sb = new StringBuilder(JingWeiConstants.JINGWEI_TASK_ROOT_PATH);

		sb.append(JingWeiConstants.ZK_PATH_SEP).append(taskName);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_TASK_HOST_NODE);

		return sb.toString();
	}

	// e.g. ·�� e.g /jingwei-v2/tasks/**task/hosts�ӽڵ�
	private Set<String> getTaskHosts(ConfigManager configManager, String taskName) {
		String path = this.getTaskHostsPath(taskName);

		return configManager.getChildDatas(path, null).keySet();
	}
}
