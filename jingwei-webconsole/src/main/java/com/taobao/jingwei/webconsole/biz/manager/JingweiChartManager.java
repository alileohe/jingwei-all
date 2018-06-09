package com.taobao.jingwei.webconsole.biz.manager;

import com.taobao.jingwei.webconsole.model.JingweiHttpCondition;
import com.taobao.jingwei.webconsole.model.chart.JingweiChart;

public interface JingweiChartManager {
	public JingweiChart createStatCountChart(JingweiHttpCondition condition);
}