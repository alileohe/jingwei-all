package com.taobao.jingwei.webconsole.web.module.screen;

import java.util.Calendar;
import java.util.Date;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Params;
import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiUtil;
import com.taobao.jingwei.webconsole.biz.manager.JingweiZkConfigManager;
import com.taobao.jingwei.webconsole.common.JingweiWebConsoleConstance;
import com.taobao.jingwei.webconsole.model.JingweiHttpCondition;
import com.taobao.jingwei.webconsole.model.JingweiStatConstant;

public class JingweiStatChart {

	public void execute(Context context, @Params JingweiHttpCondition cd) {
		long now = System.currentTimeMillis();

		if (StringUtil.isBlank(cd.getStartTime())) {
			String date = JingWeiUtil.date2String(new Date(now), JingweiWebConsoleConstance.DATE_SHORT_FORMATOR);
			cd.setStartTime(date);
			cd.setEndTime(date);
			cd.setInterval("10m");
		}
		 
		String max = JingWeiUtil.date2String(new Date(now), JingweiWebConsoleConstance.DATE_SHORT_FORMATOR);

		cd.setWeek(Calendar.getInstance().get(Calendar.WEEK_OF_YEAR));

		context.put("max", max);

		context.put("condition", cd);
		context.put("key", JingweiStatConstant.getStatNameByKey(cd.getKey3()));
		context.put("host", cd.getStatServer());
		context.put("hostName", JingweiZkConfigManager.getHostName(cd.getStatServer()));
	}
}
