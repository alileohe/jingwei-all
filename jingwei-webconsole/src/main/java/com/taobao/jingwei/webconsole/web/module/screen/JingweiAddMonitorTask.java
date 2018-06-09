package com.taobao.jingwei.webconsole.web.module.screen;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.taobao.jingwei.webconsole.biz.ao.JingweiGroupAO;
import com.taobao.jingwei.webconsole.biz.ao.JingweiMonitorAO;
import com.taobao.jingwei.webconsole.biz.manager.JingweiZkConfigManager;

public class JingweiAddMonitorTask {
	@Autowired
	private JingweiMonitorAO jwMonitorAO;

	@Autowired
	private JingweiGroupAO jwGroupAO;

	public void execute(Context context, @Param(name = "host") int host,
			@Param(name = "monitorName") String monitorName, @Param(name = "taskName") String taskName) {
		context.put("host", host);
		String hostName = JingweiZkConfigManager.getHostName(Integer.valueOf(host));
		context.put("hostName", hostName);
		context.put("monitorName", monitorName);
		context.put("taskName", taskName);

		Set<String> tasks = jwMonitorAO.getTaskNames(hostName);

		Set<String> monitoredTasks = jwMonitorAO.getTasks(hostName);

		tasks.removeAll(monitoredTasks);

		context.put("tasks", tasks);

		context.put("monitoredTasks", monitoredTasks);

		Set<String> allGroups = jwGroupAO.getGroups(String.valueOf(host));

		allGroups.removeAll(monitoredTasks);

		context.put("candidateGroups", allGroups);
	}
}
