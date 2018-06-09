package com.taobao.jingwei.webconsole.web.module.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import jodd.util.Wildcard;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.webconsole.biz.ao.JingweiTaskAO;
import com.taobao.jingwei.webconsole.biz.manager.JingweiRightManeger;
import com.taobao.jingwei.webconsole.biz.manager.JingweiZkConfigManager;
import com.taobao.jingwei.webconsole.common.JingweiTypeHelper;
import com.taobao.jingwei.webconsole.model.JingweiAssembledTask;
import com.taobao.jingwei.webconsole.model.JingweiInfoHolder;
import com.taobao.jingwei.webconsole.model.JingweiTaskCriteria;
import com.taobao.jingwei.webconsole.util.DataCacheType;
import com.taobao.jingwei.webconsole.util.EnvDataCache;
import com.taobao.jingwei.webconsole.util.PageFilter;
import com.taobao.jingwei.webconsole.util.PageUtil;
import com.taobao.jingwei.webconsole.web.filter.JingweiSecurityFilter;

public class JingweiTasks {

	private static Log log = LogFactory.getLog(JingweiTasks.class);
	@Autowired
	private JingweiTaskAO jwTaskAO;

	@Autowired
	private EnvDataCache envDataCache;

	@Autowired
	private JingweiRightManeger jwRightManeger;

	@Autowired
	private HttpServletRequest request;

	public void execute(Context context, @Param(name = "host") String host,
			@Param(name = "extractorType") String extractorType, @Param(name = "sTaskId") String sTaskId,
			@Param(name = "applierType") String applierType, @Param(name = "hostName") String hostName,
			@Param(name = "runStatus") String runStatus, @Param(name = "taskName") String taskName,
			@Param(name = "delHost") String delHost, @Param(name = "page") String page,
			@Param(name = "pageSize") String pageSize) {
		JingweiTaskCriteria criteria = new JingweiTaskCriteria();
		criteria.setTaskId(sTaskId);
		criteria.setHostName(hostName);
		criteria.setExtractorType(StringUtil.isNotBlank(extractorType) ? Integer.parseInt(extractorType) : null);
		criteria.setApplierType(StringUtil.isNotBlank(applierType) ? Integer.parseInt(applierType) : null);
		criteria.setRunStatus(runStatus);
		if (StringUtil.isNotBlank(delHost) && StringUtil.isNotBlank(taskName)) {
			jwTaskAO.delHost(taskName, delHost, host);
		}
		int currentPage;
		if (StringUtils.isNumeric(page)) {
			currentPage = StringUtils.isBlank(page) ? 1 : Integer.parseInt(page);
		} else {
			currentPage = 1;
		}

		int pageSizeInt = StringUtils.isBlank(pageSize) ? PageUtil.DEFAULT_PAGE_SIZE : Integer.parseInt(pageSize);
		Map<String, JingweiAssembledTask> tasks;
		List<String> taskNameList = envDataCache.getZkPathCache(host).get(
				DataCacheType.JingweiAssembledTask.toString(), new PageFilter(criteria) {
					@Override
					public boolean filter(Object target) {
						try {
							JingweiTaskCriteria src = (JingweiTaskCriteria) this.getSrc();
							String taskName = (String) target;
							if (StringUtils.isBlank(src.getTaskId())) {
								return true;
							} else {
								return Wildcard.match(taskName, src.getTaskId());
							}
						} catch (Exception e) {
							log.error("page filter error, target must istanceof JingweiAssembledTask, return false!", e);
							return false;
						}
					}
				});

		//如果多条件过滤,就先查询 在分页了 否则先分页再查询
		boolean hasMoreCondition = !StringUtils.isBlank(extractorType) || !StringUtils.isBlank(applierType)
				|| !StringUtils.isBlank(hostName) || !StringUtils.isBlank(runStatus);
		if (hasMoreCondition) {

			Map<String, JingweiAssembledTask> taskAll = new HashMap<String, JingweiAssembledTask>();

			for (String filterTaskName : taskNameList) {
				criteria.setTaskId(filterTaskName);
				taskAll.putAll(jwTaskAO.getTasks(criteria, host));
			}
			tasks = (Map<String, JingweiAssembledTask>) PageUtil.pagingMap(pageSizeInt, currentPage, taskAll);

			context.put("pageCount",
					(taskAll.size() % pageSizeInt != 0 ? taskAll.size() / pageSizeInt + 1 : taskAll.size()
							/ pageSizeInt));
		} else {

			List<String> pagedTaskNameList = PageUtil.pagingList(pageSizeInt, currentPage, taskNameList);

			tasks = new HashMap<String, JingweiAssembledTask>();

			for (String taskName1 : pagedTaskNameList) {
				criteria.setTaskId(taskName1);
				tasks.putAll(jwTaskAO.getTasks(criteria, host));
			}
			context.put("pageCount", (taskNameList.size() % pageSizeInt != 0 ? taskNameList.size() / pageSizeInt + 1
					: taskNameList.size() / pageSizeInt));

		}

		JingweiInfoHolder.taskInfo.putAll(tasks);

		context.put("assembledTasks", new ArrayList<JingweiAssembledTask>(tasks.values()));
		context.put("currentPage", currentPage);
		context.put("pageSizeInt", pageSizeInt);

		context.put("host", StringUtil.isNotBlank(host) ? host : JingweiZkConfigManager.getDefaultKey());
		criteria.setTaskId(sTaskId);
		context.put("criteria", criteria);

		context.put("hosts", JingweiZkConfigManager.getKeys());
		context.put("extractorType", JingweiTypeHelper.getExtractorType());
		context.put("applierType", JingweiTypeHelper.getApplierType());
		context.put("statusType", JingweiTypeHelper.getStatusType());

		String loginNickName = (String) request.getAttribute(JingweiSecurityFilter.NICK_NAME_PARAM);
		if (jwRightManeger.getSuperUserSet().contains(loginNickName)) {
			context.put("withRights", true);
		}
	}

}
