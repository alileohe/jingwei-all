package com.taobao.jingwei.webconsole.web.module.screen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jodd.util.Wildcard;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.node.monitor.MonitorParentNode;
import com.taobao.jingwei.webconsole.biz.ao.JingweiMonitorAO;
import com.taobao.jingwei.webconsole.biz.manager.JingweiZkConfigManager;
import com.taobao.jingwei.webconsole.model.JingweiAssembledMonitor;
import com.taobao.jingwei.webconsole.model.JingweiMonitorCriteria;
import com.taobao.jingwei.webconsole.util.DataCacheType;
import com.taobao.jingwei.webconsole.util.EnvDataCache;
import com.taobao.jingwei.webconsole.util.PageFilter;
import com.taobao.jingwei.webconsole.util.PageUtil;

public class JingweiMonitors {
	private static Log log = LogFactory.getLog(JingweiMonitors.class);

	@Autowired
	private JingweiMonitorAO jwMonitorAO;

	@Autowired
	private EnvDataCache envDataCache;

	public void execute(Context context, @Param(name = "host") String host,
			@Param(name = "taskStatus") String taskStatus, @Param(name = "taskNameCrireria") String taskNameCrireria,
			@Param(name = "page") String page, @Param(name = "pageSize") String pageSize) {

		JingweiMonitorCriteria criteria = new JingweiMonitorCriteria();

		criteria.setStatus(taskStatus);
		criteria.setTaskName(taskNameCrireria);

		String zkKey = StringUtil.isNotBlank(host) ? host : JingweiZkConfigManager.getDefaultKey();
		context.put("host", zkKey);

		int currentPage;
		if (StringUtils.isNumeric(page)) {
			currentPage = StringUtils.isBlank(page) ? 1 : Integer.parseInt(page);
		} else {
			currentPage = 1;
		}
		int pageSizeInt = StringUtils.isBlank(pageSize) ? PageUtil.DEFAULT_PAGE_SIZE : Integer.parseInt(pageSize);
		List<JingweiAssembledMonitor> monitors;

		List<String> taskNameList = envDataCache.getZkPathCache(host).get(
				DataCacheType.JingweiAssembledMonitor.toString(), new PageFilter(criteria) {
					@Override
					public boolean filter(Object target) {
						try {
							JingweiMonitorCriteria src = (JingweiMonitorCriteria) this.getSrc();
							String taskName = (String) target;
							if (StringUtils.isBlank(src.getTaskName())) {
								return true;
							} else {
								return Wildcard.match(taskName, src.getTaskName());
							}
						} catch (Exception e) {
							log.error("page filter error, target must istanceof JingweiAssembledTask, return false!", e);
							return false;
						}
					}
				});
		boolean hasMoreCondition = !StringUtils.isBlank(taskStatus);
		if (hasMoreCondition) {

			List<JingweiAssembledMonitor> monitorAll = new ArrayList<JingweiAssembledMonitor>();
			for (String taskName1 : taskNameList) {
				criteria.setTaskName(taskName1);
				monitorAll.addAll(jwMonitorAO.getMonitors(criteria, zkKey));
			}

			monitors = PageUtil.pagingList(pageSizeInt, currentPage, monitorAll);
			context.put("pageCount", (monitorAll.size() % pageSizeInt != 0 ? monitorAll.size() / pageSizeInt + 1
					: monitorAll.size() / pageSizeInt));
			context.put("criteria", criteria);

		} else {

			List<String> pagedMonitorNameList = PageUtil.pagingList(pageSizeInt, currentPage, taskNameList);

			monitors = new ArrayList<JingweiAssembledMonitor>();

			//List<JingweiAssembledMonitor> list = jwMonitorAO.getMonitors(criteria, zkKey);
			for (String monitorName1 : pagedMonitorNameList) {
				criteria.setTaskName(monitorName1);
				monitors.addAll(jwMonitorAO.getMonitors(criteria, zkKey));
			}
			context.put("pageCount", (taskNameList.size() % pageSizeInt != 0 ? taskNameList.size() / pageSizeInt + 1
					: taskNameList.size() / pageSizeInt));
			context.put("criteria", criteria);
		}

		context.put("currentPage", currentPage);
		context.put("pageSizeInt", pageSizeInt);
		context.put("monitors", monitors);
		criteria.setTaskName(taskNameCrireria);
		context.put("criteria", criteria);
		Set<String> acticveMonitors = jwMonitorAO.getMonitorNames(zkKey);

		context.put("acticveMonitors", acticveMonitors);

		Set<String> taskStatues = new HashSet<String>();
		taskStatues.add("start");
		taskStatues.add("stop");

		context.put("taskStatues", taskStatues);

		// global config
		MonitorParentNode globalConfig = jwMonitorAO.getGlobalConfig(zkKey);
		context.put("globalConfig", globalConfig);
	}
}
