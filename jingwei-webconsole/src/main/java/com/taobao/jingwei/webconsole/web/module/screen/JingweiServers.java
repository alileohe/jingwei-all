package com.taobao.jingwei.webconsole.web.module.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import jodd.util.Wildcard;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.config.ConfigManager;
import com.taobao.jingwei.common.node.StatusNode;
import com.taobao.jingwei.common.node.StatusNode.StatusEnum;
import com.taobao.jingwei.common.node.server.ServerTaskNode.TaskTypeEnum;
import com.taobao.jingwei.webconsole.biz.ao.JingweiGroupAO;
import com.taobao.jingwei.webconsole.biz.ao.JingweiServerAO;
import com.taobao.jingwei.webconsole.biz.manager.JingweiRightManeger;
import com.taobao.jingwei.webconsole.biz.manager.JingweiZkConfigManager;
import com.taobao.jingwei.webconsole.model.JingweiAssembledGroup.GroupTaskInfo;
import com.taobao.jingwei.webconsole.model.JingweiAssembledServer;
import com.taobao.jingwei.webconsole.model.JingweiServerCriteria;
import com.taobao.jingwei.webconsole.util.DataCacheType;
import com.taobao.jingwei.webconsole.util.EnvDataCache;
import com.taobao.jingwei.webconsole.util.PageFilter;
import com.taobao.jingwei.webconsole.util.PageUtil;
import com.taobao.jingwei.webconsole.web.filter.JingweiSecurityFilter;

public class JingweiServers {
	private Log log = LogFactory.getLog(JingweiServers.class);
	private static final String ONLY_GROUP = "ONLY_GROUP";
	private static final String NON_GROUP = "NON_GROUP";

	@Autowired
	private JingweiServerAO jwServerAO;

	@Autowired
	private JingweiGroupAO jwGroupAO;

	@Autowired
	private JingweiZkConfigManager jwConfigManager;
	@Autowired
	private EnvDataCache envDataCache;
	
	@Autowired
	private JingweiRightManeger jwRightManeger;

	@Autowired
	private HttpServletRequest request;

	public void execute(
			Context context,
			@Param(name = "host") String host,
			@Param(name = "criteriaServerName") String criteriaServerName,
			@Param(name = "criteriaTaskName") String criteriaTaskName,
			@Param(name = "serverStatus") String serverStatus,
			@Param(name = "taskStatus") String taskStatus,
			@Param(name = "groupCriteria") String groupCriteria,
			@Param(name = "taskType") String taskType,
			@Param(name = "page") String page,
			@Param(name = "pageSize") String pageSize) {

		JingweiServerCriteria criteria = new JingweiServerCriteria();

		criteria.setServerName(criteriaServerName);

		criteria.setTaskName(criteriaTaskName);

		criteria.setServerStatus(serverStatus);

		criteria.setTaskType(taskType);

		criteria.setTaskStatus(taskStatus);
		criteria.setGroupType(groupCriteria);
		
		if (StringUtils.isNotBlank((String)context.get("groupCriteria"))) {
			criteria.setGroupType((String)context.get("groupCriteria"));
		}
		
		if (StringUtils.isNotBlank((String)context.get("criteriaServerName"))) {
			criteria.setServerName((String)context.get("criteriaServerName"));
		}
		
		if (StringUtils.isNotBlank((String)context.get("criteriaTaskName"))) {
			criteria.setTaskName((String)context.get("criteriaTaskName"));
		}

		
		String zkKey = StringUtil.isNotBlank(host) ? host
				: JingweiZkConfigManager.getDefaultKey();
		context.put("host", zkKey);
		int currentPage;
		if(StringUtils.isNumeric(page)){
			currentPage = StringUtils.isBlank(page) ? 1 : Integer.parseInt(page);
		}else{
			currentPage = 1;
		}
		
		
		int pageSizeInt = StringUtils.isBlank(pageSize) ? PageUtil.DEFAULT_PAGE_SIZE : Integer.parseInt(pageSize);
		
		if (StringUtils.isNotBlank((String)context.get("currentPage"))) {
			currentPage = Integer.valueOf((String)context.get("currentPage"));
		}
		
		
		if (StringUtils.isNotBlank((String)context.get("pageSizeInt"))) {
			pageSizeInt = Integer.valueOf((String)context.get("pageSizeInt"));
		}
		
		List<JingweiAssembledServer> servers;
		
		List<String> serverNameList = envDataCache.getZkPathCache(host).get(
				DataCacheType.JingweiAssembledServer.toString(),
				new PageFilter(criteria) {
					@Override
					public boolean filter(Object target) {
						try {
							JingweiServerCriteria src = (JingweiServerCriteria) this
									.getSrc();
							String serverName = (String) target;
							if (StringUtils.isBlank(src.getServerName())) {
								return true;
							} else {
								return Wildcard.match(serverName,
										src.getServerName());
							}
						} catch (Exception e) {
							log.error("page filter error, target must istanceof JingweiAssembledTask, return false!");
							e.printStackTrace();
							return false;
						}
					}
				});
		boolean hasMoreCondition = !StringUtils.isBlank(criteriaServerName)||!StringUtils.isBlank(serverStatus)
									||!StringUtils.isBlank(taskStatus)||!StringUtils.isBlank(groupCriteria)
									||!StringUtils.isBlank(taskType);
		if(hasMoreCondition){
			 List<JingweiAssembledServer> serverAll = new ArrayList<JingweiAssembledServer>();
			 
			 for (String serverName1 : serverNameList) {
				criteria.setServerName(serverName1);
				serverAll.addAll(jwServerAO.getServers(criteria, zkKey));
			}
			 
			 servers = PageUtil.pagingList(pageSizeInt, currentPage, serverAll);
			 context.put("pageCount", (serverAll.size()
						% pageSizeInt != 0 ? serverAll.size()
						/ pageSizeInt + 1 : serverAll.size()
						/ pageSizeInt));
		}else{
			
			
			List<String> pagedServerNameList =PageUtil.pagingList(pageSizeInt,currentPage,serverNameList); 
			
			servers = new ArrayList<JingweiAssembledServer>();
			for (String serverName1 : pagedServerNameList) {
				criteria.setServerName(serverName1);
				servers.addAll(jwServerAO.getServers(criteria, zkKey));
			}
			context.put("pageCount", (serverNameList.size()
					% pageSizeInt != 0 ? serverNameList.size()
					/ pageSizeInt + 1 : serverNameList.size()
					/ pageSizeInt));
		}

		List<JingweiAssembledServer> filteredServers = new ArrayList<JingweiAssembledServer>();

		// group 里面含有多少个task， 一台机器上有多少个executor
		// TODO 这一段语义不清，不理解
		for (JingweiAssembledServer jingweiAssembledServer : servers) {

			Set<String> groupsNames = jwServerAO.getGroupNames(
					jingweiAssembledServer.getServerName(), zkKey);

			Map<String, TreeSet<String>> groupRunningTasks = new HashMap<String, TreeSet<String>>();
			for (String groupName : groupsNames) {
				Set<GroupTaskInfo> groupTaskInfos = jwGroupAO
						.getGroupTaskInfos(
								jingweiAssembledServer.getServerName(),
								groupName, zkKey);

				TreeSet<String> runningTasks = new TreeSet<String>();

				groupRunningTasks.put(groupName, runningTasks);

				// executor count
				for (GroupTaskInfo groupTaskInfo : groupTaskInfos) {
					if (groupTaskInfo.getServerName().equals(
							jingweiAssembledServer.getServerName())
							&& groupTaskInfo.getStatus().equalsIgnoreCase(
									StatusEnum.RUNNING.getStatusString())) {
						groupRunningTasks.get(groupName).add(
								groupTaskInfo.getTaskName());
					}
				}
			}

			/*int occupiedExecutorCount = this.getOldOccupiedExecutorCount(
					jingweiAssembledServer.getServerName(), host);
			if (occupiedExecutorCount == 0) {
				occupiedExecutorCount = this.getOccupiedExecutorCount(
						jingweiAssembledServer.getServerName(), host);
			}

			jingweiAssembledServer
					.setOccupiedExecutorCount(occupiedExecutorCount);*/
			jingweiAssembledServer.setGroups(groupRunningTasks);
			if (StringUtil.isBlank(groupCriteria)) {
				filteredServers.add(jingweiAssembledServer);

			} else if (groupCriteria.equalsIgnoreCase(ONLY_GROUP)) {
				if (!jingweiAssembledServer.getGroups().isEmpty()) {
					jingweiAssembledServer.setJingweiAssembledServerTasks(null);
					filteredServers.add(jingweiAssembledServer);
				}

			} else if (groupCriteria.equalsIgnoreCase(NON_GROUP)) {

				jingweiAssembledServer.setGroups(null);
				filteredServers.add(jingweiAssembledServer);
			}

		}

		
		context.put("currentPage", currentPage);
		context.put("pageSizeInt", pageSizeInt);
		context.put("servers", filteredServers);
		criteria.setServerName(criteriaServerName);
		context.put("criteria", criteria);

		// server status
		Set<String> serverStatues = new HashSet<String>();
		serverStatues.add(StatusEnum.RUNNING.getStatusString().toUpperCase());

		// server status
		Set<String> taskStatues = new HashSet<String>();
		taskStatues.add(StatusEnum.RUNNING.getStatusString().toUpperCase());
		taskStatues.add(StatusEnum.STANDBY.getStatusString().toUpperCase());

		context.put("serverStatues", serverStatues);

		context.put("taskStatues", taskStatues);

		// group 过滤条件
		Set<String> groupCriterias = new HashSet<String>();
		groupCriterias.add(ONLY_GROUP);
		groupCriterias.add(NON_GROUP);
		context.put("groupCriterias", groupCriterias);
		context.put("groupCriteriaServerName", criteriaServerName);

		// customer buildin类型分类
		Set<String> buildCustomerCriterias = new HashSet<String>();
		buildCustomerCriterias.add(TaskTypeEnum.BUILDIN.getTaskTypeString()
				.toUpperCase());
		buildCustomerCriterias.add(TaskTypeEnum.CUSTOMER.getTaskTypeString()
				.toUpperCase());
		context.put("buildCustomerCriterias", buildCustomerCriterias);
		
		String loginNickName = (String) request.getAttribute(JingweiSecurityFilter.NICK_NAME_PARAM);
		if (jwRightManeger.getSuperUserSet().contains(loginNickName)) {
			context.put("withRights", true);
		}
	}

	@SuppressWarnings("unused")
	private int getOccupiedExecutorCount(String serverName, String zkKey) {
		Set<String> tasks = jwServerAO.getTaskNames(zkKey);

		int occupiedExecutorCount = 0;

		for (String taskName : tasks) {
			int oneTaskOccupied = jwServerAO.getRunningTaskCount(serverName,
					taskName, zkKey);

			occupiedExecutorCount += oneTaskOccupied;
		}

		return occupiedExecutorCount;
	}

	// 兼容老版本，一个server上运行的task从/tasks/**task/hosts/**host/status获取
	@SuppressWarnings("unused")
	private int getOldOccupiedExecutorCount(String serverName, String zkKey) {
		Set<String> tasks = jwServerAO.getTaskNames(zkKey);

		int occupiedExecutorCount = 0;

		for (String taskName : tasks) {
			try {
				if (isRunning(jwConfigManager.getZkConfigManager(zkKey),
						taskName, serverName)) {
					occupiedExecutorCount++;
				}
			} catch (JSONException e) {
				log.error(e);
			}
		}

		return occupiedExecutorCount;
	}

	/**
	 * 判断是否存在status节点
	 * 
	 * @param configManager
	 *            zk管理器
	 * @param taskName
	 *            任务名
	 * @param hostName
	 *            主机名
	 * @return <code>false</code> 任务不是running状态，<code>true</code>任务是running状态
	 *         e.g. /jingwei/tasks/**task/hosts/**host/status
	 * @throws JSONException
	 */
	public static boolean isRunning(ConfigManager configManager,
			String taskName, String hostName) throws JSONException {
		// path
		StringBuilder sb = new StringBuilder(
				JingWeiConstants.JINGWEI_TASK_ROOT_PATH);

		sb.append(JingWeiConstants.ZK_PATH_SEP).append(taskName);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(
				JingWeiConstants.JINGWEI_TASK_HOST_NODE);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(hostName);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(
				JingWeiConstants.JINGWEI_STATUS_NODE_NAME);

		String path = sb.toString();

		// data
		String data = configManager.getData(path);

		if (StringUtil.isBlank(data)) {
			return false;
		}

		StatusNode statusNode = new StatusNode();
		statusNode.jsonStringToNodeSelf(data);

		if (statusNode.getStatusEnum() == StatusEnum.RUNNING) {
			return true;
		}

		return false;
	}

}
