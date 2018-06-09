package com.taobao.jingwei.webconsole.web.module.screen;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Params;
import com.taobao.jingwei.common.JingWeiUtil;
import com.taobao.jingwei.webconsole.biz.manager.JingweiChartManager;
import com.taobao.jingwei.webconsole.common.JingweiWebConsoleConstance;
import com.taobao.jingwei.webconsole.model.JingweiChartType;
import com.taobao.jingwei.webconsole.model.JingweiHttpCondition;
import com.taobao.jingwei.webconsole.model.chart.JingweiChart;

public class JingweiStatChartData {
	@Autowired
	private JingweiChartManager jwChartManager;

	public void execute(Context context, @Params JingweiHttpCondition cd) {
		if (cd.getChartType() == JingweiChartType.BAR.getType()) {
			Calendar cal = Calendar.getInstance(Locale.CHINA);
			cal.set(Calendar.WEEK_OF_YEAR, cd.getWeek());
			cal.add(Calendar.DAY_OF_WEEK, -1);
			int today = cal.get(Calendar.DAY_OF_WEEK);
			cal.add(Calendar.DATE, 2 - today);
			cd.setStartTime(JingWeiUtil.date2String(cal.getTime(), JingweiWebConsoleConstance.DATE_SHORT_FORMATOR));
			cal.setTime(new Date(System.currentTimeMillis()));
			cal.set(Calendar.WEEK_OF_YEAR, cd.getWeek());
			cal.add(Calendar.DATE, 7 - today);
			cd.setEndTime(JingWeiUtil.date2String(cal.getTime(), JingweiWebConsoleConstance.DATE_SHORT_FORMATOR));
		}
		JingweiChart chart = jwChartManager.createStatCountChart(cd);
		context.put("cd", cd);
		context.put("data", chart);
	}
}
