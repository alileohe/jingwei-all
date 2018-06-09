package com.taobao.jingwei.webconsole.biz.manager.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiUtil;
import com.taobao.jingwei.webconsole.biz.manager.JingweiChartManager;
import com.taobao.jingwei.webconsole.biz.manager.JingweiZkConfigManager;
import com.taobao.jingwei.webconsole.common.HttpHelper;
import com.taobao.jingwei.webconsole.common.JingweiWebConsoleConstance;
import com.taobao.jingwei.webconsole.model.JingweiChartType;
import com.taobao.jingwei.webconsole.model.JingweiHttpCondition;
import com.taobao.jingwei.webconsole.model.JingweiStatConstant;
import com.taobao.jingwei.webconsole.model.chart.JingweiChart;
import com.taobao.jingwei.webconsole.model.chart.JingweiChartDataItem;
import com.taobao.jingwei.webconsole.model.chart.JingweiChartDataset;

public class JingweiChartManagerTLogImpl implements JingweiChartManager {
	private static final Log log = LogFactory.getLog(JingweiWebConsoleConstance.JINGWEI_LOG);
	private static final int HTTP_TIMEOUT = 60000;

	@Override
	public JingweiChart createStatCountChart(JingweiHttpCondition cd) {
		JingweiChart chart = new JingweiChart();
		chart.setCaption(JingweiStatConstant.getStatNameByKey(cd.getKey3()));
		chart.setxAxisName("时间");
		chart.setyAxisName("数量");

		Map<String, String> params = new HashMap<String, String>(6);
		if (StringUtil.isNotBlank(cd.getKey1())) {
			params.put("key1", cd.getKey1());
		}
		if (StringUtil.isNotBlank(cd.getKey2())) {
			params.put("key2", cd.getKey2());
		}
		params.put("key3", cd.getKey3());
		if (StringUtil.isNotBlank(cd.getInterval())) {
			params.put("interval", cd.getInterval());
		}
		params.put("startTime", cd.getStartTimestamp() + "");
		params.put("endTime", cd.getEndTimestamp() + "");

		if (cd.getChartType() == JingweiChartType.BAR.getType()) {
			fillBarData(params, chart, cd, false);
		} else if (cd.getChartType() == JingweiChartType.LINE.getType()) {
			fillLineData(params, chart, cd);
		} else {
			fillPieData(params, chart, cd);
		}
		chart.setChartType(cd.getChartType());
		return chart;
	}

	private void fillBarData(Map<String, String> params, JingweiChart chart, JingweiHttpCondition cd, boolean isDelay) {
		try {
			long s = System.currentTimeMillis();
			String content = HttpHelper.sendGet(JingweiZkConfigManager.getStatServer(cd.getStatServer()), params,
					cd.getTimeout() == 0 ? HTTP_TIMEOUT : cd.getTimeout());
			log.info("Retriever Data from TLog cost time: " + (System.currentTimeMillis() - s) + "ms");
			log.debug(content);
			if (StringUtil.isBlank(content) || "no data no this condition\r\n".equals(content)) {
				throw new NullPointerException("没有查找到统计数据！");
			}
			JSONObject datas = new JSONObject(content);
			Iterator<?> iter = datas.keys();
			while (iter.hasNext()) {
				String key = iter.next().toString();
				JingweiChartDataset ds = new JingweiChartDataset();
				ds.setSeriesName(JingweiStatConstant.getStatNameByKey(key));
				JSONArray sData = datas.getJSONArray(key);
				int aLen = sData.length();
				List<JingweiChartDataItem> data = new LinkedList<JingweiChartDataItem>();
				for (int i = 0; i < aLen; i++) {
					JSONObject d = sData.getJSONObject(i);
					String time = d.get("time").toString();
					String formated = isDelay ? time : formatTime(time, cd.getInterval());
					chart.addCategory(formated, "");

					JingweiChartDataItem item = new JingweiChartDataItem();
					item.setX(d.get("value").toString());
					data.add(item);
				}
				ds.setData(data);
				chart.addDataset(ds);
			}
		} catch (Exception e) {
			log.warn("获取远程数据异常", e);
		}
	}

	private void fillLineData(Map<String, String> params, JingweiChart chart, JingweiHttpCondition cd) {
		try {
			long s = System.currentTimeMillis();
			String content = HttpHelper.sendGet(JingweiZkConfigManager.getStatServer(cd.getStatServer()), params,
					cd.getTimeout() == 0 ? HTTP_TIMEOUT : cd.getTimeout());
			log.info("Retriever Data from TLog cost time: " + (System.currentTimeMillis() - s) + "ms");
			if (StringUtil.isBlank(content) || "no data no this condition\r\n".equals(content)) {
				throw new NullPointerException("没有查找到统计数据！" + content);
			}
			JSONObject datas = new JSONObject(content);
			Iterator<?> iter = datas.keys();
			long max = Long.MIN_VALUE;
			long min = Long.MAX_VALUE;
			while (iter.hasNext()) {
				String key = iter.next().toString();
				JingweiChartDataset ds = new JingweiChartDataset();
				ds.setSeriesName(JingweiStatConstant.getStatNameByKey(key));
				JSONArray sData = datas.getJSONArray(key);
				int aLen = sData.length();
				List<JingweiChartDataItem> data = new LinkedList<JingweiChartDataItem>();
				for (int i = 0; i < aLen; i++) {
					JSONObject d = sData.getJSONObject(i);
					String time = d.get("time").toString();
					long t = JingWeiUtil.string2Date(time, JingweiWebConsoleConstance.DATE_FORMATOR).getTime();
					JingweiChartDataItem item = new JingweiChartDataItem();
					item.setLabel(time);
					item.setX(t + "");
					// 10 * 60 * 1000 / cd.getSummaryPeriod()
					// 统计间隔为10分钟转换为毫秒，除以统计周期
					long base = cd.getSummaryPeriod() > 0 ? 10 * 60 * 1000 / cd.getSummaryPeriod() : 1;
					item.setY((Long.parseLong(d.get("value").toString()) / base) + "");

					data.add(item);
					if (t > max) {
						max = t;
					}
					if (t < min) {
						min = t;
					}

					chart.addCategory(formatTime(time, cd.getInterval()), t + "");
				}
				ds.setData(data);
				chart.addDataset(ds);
				chart.setMax(max == Long.MAX_VALUE ? "0" : max + "");
				chart.setMin(min == Long.MIN_VALUE ? "0" : min + "");
			}
		} catch (Exception e) {
			log.warn("获取远程数据异常", e);
		}
	}

	private String formatTime(String time, String interval) {
		String formated = "";
		if (interval.indexOf("s") > -1) {
			formated = time.substring(14, time.length());
		} else if (interval.indexOf("m") > -1 || interval.indexOf("h") > -1) {
			formated = time.substring(11, 16);
		} else if (interval.indexOf("d") > -1) {
			formated = time.substring(0, 10);
		}
		return formated;
	}

	private void fillPieData(Map<String, String> params, JingweiChart chart, JingweiHttpCondition cd) {
		try {
			long s = System.currentTimeMillis();
			String content = HttpHelper.sendGet(JingweiZkConfigManager.getStatServer(cd.getStatServer()), params,
					cd.getTimeout() == 0 ? HTTP_TIMEOUT : cd.getTimeout());
			log.info("Retriever Data from TLog cost time: " + (System.currentTimeMillis() - s) + "ms");
			if (StringUtil.isBlank(content) || "no data no this condition\r\n".equals(content)) {
				throw new NullPointerException("没有查找到统计数据！");
			}
			JSONObject datas = new JSONObject(content);
			Iterator<?> iter = datas.keys();
			while (iter.hasNext()) {
				String key = iter.next().toString();
				JingweiChartDataset ds = new JingweiChartDataset();
				ds.setSeriesName(JingweiStatConstant.getStatNameByKey(key));
				JSONArray sData = datas.getJSONArray(key);
				int aLen = sData.length();
				List<JingweiChartDataItem> data = new LinkedList<JingweiChartDataItem>();
				for (int i = 0; i < aLen; i++) {
					JSONObject d = sData.getJSONObject(i);
					String time = d.get("time").toString();
					JingweiChartDataItem item = new JingweiChartDataItem();
					item.setX(time);
					item.setY(d.get("value").toString());
					data.add(item);
				}
				ds.setData(data);
				chart.addDataset(ds);
			}
		} catch (Exception e) {
			log.warn("获取远程数据异常", e);
		}
	}
}
