package com.taobao.jingwei.webconsole.web.module.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jodd.util.StringUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.node.OperateNode.OperateEnum;
import com.taobao.jingwei.common.node.StatusNode;
import com.taobao.jingwei.common.node.StatusNode.StatusEnum;
import com.taobao.jingwei.common.node.server.ServerTaskNode;
import com.taobao.jingwei.common.node.server.ServerTaskNode.PluginTaskTargetStateEnum;
import com.taobao.jingwei.common.node.server.ServerTaskNode.PluginTaskWorkStateEnum;
import com.taobao.jingwei.common.node.server.ServerTaskNode.TaskTypeEnum;
import com.taobao.jingwei.webconsole.biz.ao.JingweiGroupAO;
import com.taobao.jingwei.webconsole.biz.ao.JingweiServerAO;
import com.taobao.jingwei.webconsole.common.JingweiWebConsoleConstance;
import com.taobao.jingwei.webconsole.model.JingWeiResult;
import com.taobao.jingwei.webconsole.model.JingweiAssembledGroup.GroupTaskInfo;
import com.taobao.jingwei.webconsole.model.JingweiAssembledServerGroup;
import com.taobao.jingwei.webconsole.util.DataCacheType;
import com.taobao.jingwei.webconsole.util.EnvDataCache;
import com.taobao.jingwei.webconsole.util.PageFilter;

public class JingweiServerAction implements JingweiWebConsoleConstance, JingWeiConstants {
	private Log log = LogFactory.getLog(this.getClass());
	@Autowired
	private JingweiServerAO jwServerAO;

	@Autowired
	private JingweiGroupAO jwGroupAO;

	@Autowired
	private HttpServletResponse response;

	private static final String SEP_COMMA_NAME = ",";

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private EnvDataCache envDataCache;

	/**
	 * 启动任务
	 * 
	 * @param context
	 * @param navigator
	 * @param serverName
	 *            srever名
	 * @param taskName
	 *            任务名
	 * @param zkKey
	 *            zk key
	 */
	public void doStartTask(Context context, Navigator navigator, @Param(name = "serverName") String serverName,
			@Param(name = "taskName") String taskName, @Param(name = "host") String zkKey,
			@Param(name = "groupCriteria") String groupCriteria,
			@Param(name = "criteriaServerName") String criteriaServerName,
			@Param(name = "criteriaTaskName") String criteriaTaskName, @Param(name = "pageCount") String pageCount,
			@Param(name = "currentPage") String currentPage, @Param(name = "pageSizeInt") String pageSizeInt) {

		context.put("pageCount", pageCount);
		context.put("currentPage", currentPage);
		context.put("pageSizeInt", pageSizeInt);
		context.put("groupCriteria", groupCriteria);
		context.put("criteriaServerName", criteriaServerName);
		context.put("criteriaTaskName", criteriaTaskName);

		ServerTaskNode serverTaskNode = jwServerAO.getServerTaskNode(serverName, taskName, zkKey);
		serverTaskNode.setPluginTaskWorkStateEnum(PluginTaskWorkStateEnum.WORK_NORM_STATE);

		jwServerAO.updateServerTaskOperate(serverTaskNode, OperateEnum.NODE_START, zkKey);
		//navigator.redirectTo("jingweiModule").withTarget("jingweiServers.vm").withParameter("host", zkKey);
	}

	/**
	 * 停止任务
	 * 
	 * @param context
	 * @param navigator
	 */
	public void doStopTask(Context context, Navigator navigator, @Param(name = "serverName") String serverName,
			@Param(name = "taskName") String taskName, @Param(name = "host") String zkKey,
			@Param(name = "groupCriteria") String groupCriteria,
			@Param(name = "criteriaServerName") String criteriaServerName,
			@Param(name = "criteriaTaskName") String criteriaTaskName, @Param(name = "pageCount") String pageCount,
			@Param(name = "currentPage") String currentPage, @Param(name = "pageSizeInt") String pageSizeInt) {

		context.put("pageCount", pageCount);
		context.put("currentPage", currentPage);
		context.put("pageSizeInt", pageSizeInt);
		context.put("groupCriteria", groupCriteria);
		context.put("criteriaServerName", criteriaServerName);
		context.put("criteriaTaskName", criteriaTaskName);

		ServerTaskNode serverTaskNode = jwServerAO.getServerTaskNode(serverName, taskName, zkKey);

		jwServerAO.updateServerTaskOperate(serverTaskNode, OperateEnum.NODE_STOP, zkKey);
		//navigator.redirectTo("jingweiModule").withTarget("jingweiServers.vm").withParameter("host", zkKey);
	}

	/**
	 * 删除任务
	 * 
	 * @param context
	 * @param navigator
	 */
	public void doDeleteTask(Context context, Navigator navigator, @Param(name = "serverName") String serverName,
			@Param(name = "taskName") String taskName, @Param(name = "host") String zkKey,
			@Param(name = "groupCriteria") String groupCriteria,
			@Param(name = "criteriaServerName") String criteriaServerName,
			@Param(name = "criteriaTaskName") String criteriaTaskName, @Param(name = "pageCount") String pageCount,
			@Param(name = "currentPage") String currentPage, @Param(name = "pageSizeInt") String pageSizeInt) {

		context.put("pageCount", pageCount);
		context.put("currentPage", currentPage);
		context.put("pageSizeInt", pageSizeInt);
		context.put("groupCriteria", groupCriteria);
		context.put("criteriaServerName", criteriaServerName);
		context.put("criteriaTaskName", criteriaTaskName);

		ServerTaskNode serverTaskNode = jwServerAO.getServerTaskNode(serverName, taskName, zkKey);

		jwServerAO.deleteServerTaskNode(serverTaskNode, zkKey);
		//navigator.redirectTo("jingweiModule").withTarget("jingweiServers.vm").withParameter("host", zkKey);
	}

	/**
	 * 
	 * @param context
	 * @param navigator
	 */
	public void doDeployTask(Context context, Navigator navigator, @Param(name = "serverName") String serverName,
			@Param(name = "targets") String taskNames, @Param(name = "host") String zkKey,
			@Param(name = "groupCriteria") String groupCriteria,
			@Param(name = "criteriaServerName") String criteriaServerName,
			@Param(name = "criteriaTaskName") String criteriaTaskName, @Param(name = "pageCount") String pageCount,
			@Param(name = "currentPage") String currentPage, @Param(name = "pageSizeInt") String pageSizeInt) {

		context.put("pageCount", pageCount);
		context.put("currentPage", currentPage);
		context.put("pageSizeInt", pageSizeInt);
		context.put("groupCriteria", groupCriteria);
		context.put("criteriaServerName", criteriaServerName);
		context.put("criteriaTaskName", criteriaTaskName);

		String tasks[] = taskNames.split(SEP_COMMA_NAME);
		// 错误提示：server task node已经存在
		List<String> messages = new ArrayList<String>();
		for (String taskName : tasks) {
			ServerTaskNode serverTaskNode = new ServerTaskNode();

			// 任务名
			serverTaskNode.setName(taskName);
			serverTaskNode.setPluginTaskTargetStateEnum(PluginTaskTargetStateEnum.TARGET_NORM_STATE);
			serverTaskNode.setPluginTaskWorkStateEnum(PluginTaskWorkStateEnum.WORK_NORM_STATE);
			serverTaskNode.setServerName(serverName);
			serverTaskNode.setTaskName(taskName);
			serverTaskNode.setTaskType(TaskTypeEnum.BUILDIN);

			JingWeiResult result = jwServerAO.addServerTaskNode(serverTaskNode, zkKey);
			if (StringUtil.isNotEmpty(result.getMessage())) {
				messages.add(result.getMessage());
			}
		}
		context.put("messages", messages);

	}

	public void doDeleteServer(Context context, Navigator navigator, @Param(name = "serverName") String serverName,
			@Param(name = "host") String zkKey) {
		jwServerAO.deleteServerNode(serverName, zkKey);
		navigator.redirectTo("jingweiModule").withTarget("jingweiServers.vm").withParameter("host", zkKey);
	}

	/**
	 * 获取要选择的任务
	 * @param context
	 * @param navigator
	 * @param host
	 * @param serverName
	 */
	public void doGetWaitDeployTasks(Context context, Navigator navigator, @Param(name = "host") String host,
			@Param(name = "serverName") String serverName) {

		context.put("serverName", serverName);

		Set<String> serverTasks = jwServerAO.getTaskNames(serverName, host);

		context.put("serverTasks", serverTasks);

		// 任务列表
		Set<String> tasks = jwServerAO.getBuildinTaskNames(host);

		tasks.removeAll(serverTasks);

		context.put("waitDeployTasks", tasks);

		navigator.redirectTo("jingweiModule").withTarget("jingweiServers.vm").withParameter("host", host);
	}

	/**
	 * 获取在指定的server上运行的Group类型的Task到JSON response
	 * @param context
	 * @param host zkKey
	 * @param serverName server name
	 * @param groupName group name
	 */
	public void doGetRunningTaskAtServer(Context context, @Param(name = "host") String host,
			@Param(name = "serverName") String serverName, @Param(name = "groupName") String groupName) {
		Set<GroupTaskInfo> groupTaskInfos = jwGroupAO.getGroupTaskInfos(groupName, host);

		JSONArray array = new JSONArray();
		int index = 0;

		try {
			for (GroupTaskInfo groupTaskInfo : groupTaskInfos) {
				if (groupTaskInfo.getServerName().equals(serverName)
						&& groupTaskInfo.getStatus().equalsIgnoreCase(StatusEnum.RUNNING.getStatusString())) {

					array.put(index, groupTaskInfo.getTaskName());
					index = index + 1;

				}
			}

			JSONObject runningTasks = new JSONObject();

			runningTasks.put("runningTasks", array);

			writeJsonToResponse(runningTasks);
		} catch (JSONException e) {
			log.error(e.getStackTrace());
		}

	}

	/**
	 * 获取以某种前缀开头的servers
	 * @param context
	 * @param host
	 * @param word 前缀
	 */ 
	public void doGetPromptServers(Context context, @Param(name = "host") String host,
			@Param(name = "word") String word) {
		// 改用新缓存
		List<String> list = envDataCache.getZkPathCache(host).get(
				DataCacheType.JingweiAssembledServer.toString(), new PageFilter(null) {
					@Override
					public boolean filter(Object target) {
						return true;
					}
				});

		List<String> resultList = new ArrayList<String>();

		if (StringUtil.isNotBlank(word)) {
			for (String name : list) {
				if (name.startsWith(word)) {
					resultList.add(name);
				}
			}
		} else {
			resultList.addAll(list);
		}

		Collections.sort(resultList);
		
		JSONObject jsonObj = new JSONObject();

		try {
			jsonObj.put("candidates", resultList);
		} catch (JSONException e) {
			log.error(e.getStackTrace());
		}

		this.writeJsonToResponse(jsonObj);
	}

	public void doGetServerGroups(Context context, Navigator navigator, @Param(name = "host") String host,
			@Param(name = "serverName") String serverName) {

		JSONObject serverGroupViews = new JSONObject();

		JSONArray array = new JSONArray();

		Set<String> servers = jwServerAO.getServerNames(host);

		if (StringUtil.isNotBlank(serverName)) {
			if (servers.contains(serverName)) {
				servers = new HashSet<String>();
				servers.add(serverName);
			} else {
				this.writeJsonToResponse(serverGroupViews);
				return;
			}
		}

		try {
			serverGroupViews.put("rows", array);

			int index = 0;
			// 向server一列填充内容
			for (String server : servers) {
				JSONObject row = new JSONObject();
				row.put("server_name", server);

				Set<String> groups = jwServerAO.getGroupNames(server, host);

				row.put("group_names", groups);

				array.put(index, row);

				index = index + 1;
			}

			writeJsonToResponse(serverGroupViews);
		} catch (JSONException e) {
			log.error(e.getStackTrace());
		}
	}

	/**
	 * 
	 * @param jsonObj
	 */
	private void writeJsonToResponse(JSONObject jsonObj) {
		PrintWriter writer = null;
		try {
			writer = response.getWriter();
			response.setContentType("application/json;charset=utf-8");
			if (null != writer) {
				writer.write(jsonObj.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != writer) {
				writer.flush();
				writer.close();
			}
		}
	}

	public void doGetServerGroupInfo(Context context, @Param(name = "host") String host,
			@Param(name = "groupCriteriaServerName") String serverName) {

		context.put("selected_tab", 2);

		Set<JingweiAssembledServerGroup> serverGroups = new TreeSet<JingweiAssembledServerGroup>();

		Set<String> serverNames = jwServerAO.getServerNames(host);

		if (StringUtil.isNotBlank(serverName)) {
			if (serverNames.contains(serverName)) {
				serverNames = new HashSet<String>();
				serverNames.add(serverName);
			} else {
				context.put("server_groups", serverGroups);
				return;
			}
		}

		for (String server : serverNames) {
			JingweiAssembledServerGroup serverGroup = new JingweiAssembledServerGroup(server);

			StatusNode status = jwServerAO.getServerStatus(server, host);

			if (status == null) {
				serverGroup.setStatus(DEFAULT_SHOW_STATUS);
			} else {
				serverGroup.setStatus(StatusEnum.RUNNING.getStatusString().toUpperCase());
			}

			Set<String> groupNames = jwServerAO.getGroupNames(server, host);

			Map<String, TreeSet<GroupTaskInfo>> groupMapTasks = new TreeMap<String, TreeSet<GroupTaskInfo>>();

			for (String groupName : groupNames) {
				TreeSet<GroupTaskInfo> groupTaskInfos = (TreeSet<GroupTaskInfo>) jwGroupAO.getGroupTaskInfos(groupName,
						host);
				groupMapTasks.put(groupName, groupTaskInfos);
			}

			serverGroup.setGroups(groupMapTasks);

			serverGroups.add(serverGroup);
		}

		context.put("server_groups", serverGroups);
		//context.put("groupCriteriaServerName", serverName);
	}

	/**
	 * 获取server上可以添加的group
	 * @param context
	 * @param host
	 * @param groupName
	 */
	public void doGetCandidateGroups(Context context, @Param(name = "host") String host,
			@Param(name = "serverName") String serverName) {
		// 所有的group
		Set<String> allGroups = jwGroupAO.getGroups(host);

		// servers上的group
		Set<String> serverGroups = jwServerAO.getGroupNames(serverName, host);

		allGroups.removeAll(serverGroups);

		JSONObject jsonObj = new JSONObject();

		try {
			jsonObj.put("candidate_groups", allGroups);
		} catch (JSONException e) {
			log.error(e.getStackTrace());
		}

		this.writeJsonToResponse(jsonObj);
	}

	public void doAddGroups(Context context, @Param(name = "host") String host,
			@Param(name = "serverName") String serverName, @Param(name = "targets") String groups,
			@Param(name = "groupCriteria") String groupCriteria,
			@Param(name = "criteriaServerName") String criteriaServerName,
			@Param(name = "criteriaTaskName") String criteriaTaskName, @Param(name = "pageCount") String pageCount,
			@Param(name = "currentPage") String currentPage, @Param(name = "pageSizeInt") String pageSizeInt) {

		context.put("pageCount", pageCount);
		context.put("currentPage", currentPage);
		context.put("pageSizeInt", pageSizeInt);
		context.put("groupCriteria", groupCriteria);
		context.put("criteriaServerName", criteriaServerName);
		context.put("criteriaTaskName", criteriaTaskName);
		String[] groupNames = groups.split(SEP_COMMA_NAME);
		Set<String> groupSet = new HashSet<String>();

		for (String group : groupNames) {
			groupSet.add(group);
		}

		jwServerAO.addGroup(serverName, groupSet, host);
		this.doGetServerGroupInfo(context, host, serverName);
	}

	public void doRemoveGroup(Context context, @Param(name = "host") String host,
			@Param(name = "serverName") String serverName, @Param(name = "groupName") String groupName,
			@Param(name = "groupCriteria") String groupCriteria,
			@Param(name = "criteriaServerName") String criteriaServerName,
			@Param(name = "criteriaTaskName") String criteriaTaskName, @Param(name = "pageCount") String pageCount,
			@Param(name = "currentPage") String currentPage, @Param(name = "pageSizeInt") String pageSizeInt) {

		context.put("pageCount", pageCount);
		context.put("currentPage", currentPage);
		context.put("pageSizeInt", pageSizeInt);
		context.put("groupCriteria", groupCriteria);
		context.put("criteriaServerName", criteriaServerName);
		context.put("criteriaTaskName", criteriaTaskName);

		jwServerAO.removeGroup(serverName, groupName, host);
		this.doGetServerGroupInfo(context, host, serverName);
	}
}
