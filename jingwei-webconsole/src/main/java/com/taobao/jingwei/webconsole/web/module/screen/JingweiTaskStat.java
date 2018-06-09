package com.taobao.jingwei.webconsole.web.module.screen;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.taobao.jingwei.common.JingWeiUtil;
import com.taobao.jingwei.common.node.StatsNode;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode;
import com.taobao.jingwei.webconsole.biz.ao.JingweiTaskAO;
import com.taobao.jingwei.webconsole.biz.manager.JingweiZkConfigManager;
import com.taobao.jingwei.webconsole.common.JingweiWebConsoleConstance;

public class JingweiTaskStat {
	@Autowired
	private JingweiTaskAO jwTaskAO;

	public void execute(Context context, @Param(name = "taskId") String taskId,
			@Param(name = "hostName") String hostName, @Param(name = "host") String host) {
		StatsNode stats = jwTaskAO.getStatsInfo(taskId, hostName, host);
		if (stats != null) {
			context.put("statTime",
					JingWeiUtil.date2String(new Date(stats.getStatsTime()), JingweiWebConsoleConstance.DATE_FORMATOR));
			SyncTaskNode task = (SyncTaskNode) jwTaskAO.getTaskInfo(taskId, host);
			context.put(
					"nextTime",
					JingWeiUtil.date2String(new Date(stats.getStatsTime()
							+ task.getStatsPeriod()), JingweiWebConsoleConstance.DATE_FORMATOR));
			context.put("summaryPeriod", task.getSummaryPeriod());
			context.put("stats", stats);
		} else {
			context.put("error", "ŒﬁStats–≈œ¢£°");
		}
		context.put("zkHost", JingweiZkConfigManager.getHostName(Integer.parseInt(host)));
		context.put("taskId", taskId);
		context.put("hostName", hostName);
		context.put("host", host);
	}
}
