package com.taobao.jingwei.webconsole.web.module.screen;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.taobao.jingwei.common.node.AlarmNode;
import com.taobao.jingwei.webconsole.biz.ao.JingweiTaskAO;
import com.taobao.jingwei.webconsole.common.JingweiWebConsoleConstance;

public class JingweiTaskAlarm {
	@Autowired
	private JingweiTaskAO jwTaskAO;

	public void execute(Context context, @Param(name = "taskId") String taskId,
			@Param(name = "host") String host) {
		Map<String, AlarmNode> alarms = jwTaskAO.getAlarmInfo(taskId, host);
		context.put("alarms", alarms);
		context.put("taskId", taskId);
		context.put("host", host);
	}
}
