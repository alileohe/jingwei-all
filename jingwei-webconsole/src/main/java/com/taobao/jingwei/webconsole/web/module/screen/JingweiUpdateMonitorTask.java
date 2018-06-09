package com.taobao.jingwei.webconsole.web.module.screen;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.turbine.dataresolver.Params;
import com.taobao.jingwei.common.node.monitor.MonitorTaskNode;
import com.taobao.jingwei.webconsole.biz.ao.JingweiMonitorAO;
import com.taobao.jingwei.webconsole.biz.manager.JingweiZkConfigManager;
import com.taobao.jingwei.webconsole.model.JingweiMonitorTask;

public class JingweiUpdateMonitorTask {
	@Autowired
	private JingweiMonitorAO jwMonitorAO;

	public void execute(Context context, @Param(name = "host") String host,
			@Param(name = "monitorName") String monitorName, @Param(name = "taskName") String taskName,
			@Params JingweiMonitorTask jmt, @Param(name = "isGroup") boolean isGroup) {
		context.put("host", host);
		String hostName = JingweiZkConfigManager.getHostName(Integer.valueOf(host));
		context.put("hostName", hostName);

		MonitorTaskNode monitorTaskNode = null;

		monitorTaskNode = jwMonitorAO.getMonitorTaskNode(taskName, host);

		JingweiMonitorTask JingweiMonitorTask = new JingweiMonitorTask(monitorTaskNode);
		context.put("JingweiMonitorTask", JingweiMonitorTask);
		context.put("isGroupBa", isGroup);
	}
}
