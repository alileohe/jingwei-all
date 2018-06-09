package com.taobao.jingwei.webconsole.model;

import java.util.HashMap;
import java.util.Map;

import com.taobao.jingwei.common.node.StatsNode;

public class JingweiStatConstant {
	private static Map<String, String> STAT_KEY_NAME_MAPPING = new HashMap<String, String>();

	static {
		STAT_KEY_NAME_MAPPING.put(StatsNode.LAST_PERIOD_INSERT_COUNT_KEY,
				"周期插入事件数");
		STAT_KEY_NAME_MAPPING.put(StatsNode.LAST_PERIOD_UPDATE_COUNT_KEY,
				"周期更新事件数");
		STAT_KEY_NAME_MAPPING.put(StatsNode.LAST_PERIOD_DELETE_COUNT_KEY,
				"周期刪除事件数");
		STAT_KEY_NAME_MAPPING.put(StatsNode.LAST_PERIOD_INSERT_COUNT_KEY + ","
				+ StatsNode.LAST_PERIOD_UPDATE_COUNT_KEY + ","
				+ StatsNode.LAST_PERIOD_DELETE_COUNT_KEY, "周期事件数");

		STAT_KEY_NAME_MAPPING.put(
				StatsNode.LAST_PERIOD_INSERT_EXCEPTION_COUNT_KEY, "周期插入异常数");
		STAT_KEY_NAME_MAPPING.put(
				StatsNode.LAST_PERIOD_UPDATE_EXCEPTION_COUNT_KEY, "周期更新异常数");
		STAT_KEY_NAME_MAPPING.put(
				StatsNode.LAST_PERIOD_DELETE_EXCEPTION_COUNT_KEY, "周期删除异常数");
		STAT_KEY_NAME_MAPPING.put(
				StatsNode.LAST_PERIOD_INSERT_EXCEPTION_COUNT_KEY + ","
						+ StatsNode.LAST_PERIOD_UPDATE_EXCEPTION_COUNT_KEY
						+ ","
						+ StatsNode.LAST_PERIOD_DELETE_EXCEPTION_COUNT_KEY,
				"周期异常数");

		STAT_KEY_NAME_MAPPING
				.put(StatsNode.LAST_PERIOD_INSERT_TPS_KEY, "插入TPS");
		STAT_KEY_NAME_MAPPING
				.put(StatsNode.LAST_PERIOD_UPDATE_TPS_KEY, "更新TPS");
		STAT_KEY_NAME_MAPPING
				.put(StatsNode.LAST_PERIOD_DELETE_TPS_KEY, "删除TPS");
		STAT_KEY_NAME_MAPPING.put(StatsNode.LAST_PERIOD_INSERT_TPS_KEY + ","
				+ StatsNode.LAST_PERIOD_UPDATE_TPS_KEY + ","
				+ StatsNode.LAST_PERIOD_DELETE_TPS_KEY, "TPS");

		STAT_KEY_NAME_MAPPING.put(StatsNode.TODAY_INSERT_COUNT_KEY, "今日插入事件数");
		STAT_KEY_NAME_MAPPING.put(StatsNode.TODAY_UPDATE_COUNT_KEY, "今日更新事件数");
		STAT_KEY_NAME_MAPPING.put(StatsNode.TODAY_DELETE_COUNT_KEY, "今日删除事件数");
		STAT_KEY_NAME_MAPPING.put(StatsNode.TODAY_INSERT_COUNT_KEY + ","
				+ StatsNode.TODAY_UPDATE_COUNT_KEY + ","
				+ StatsNode.TODAY_DELETE_COUNT_KEY, "今日事件数");

		STAT_KEY_NAME_MAPPING.put(StatsNode.TODAY_INSERT_EXCEPTION_COUNT_KEY,
				"今日插入异常数");
		STAT_KEY_NAME_MAPPING.put(StatsNode.TODAY_UPDATE_EXCEPTION_COUNT_KEY,
				"今日更新异常数");
		STAT_KEY_NAME_MAPPING.put(StatsNode.TODAY_DELETE_EXCEPTION_COUNT_KEY,
				"今日删除异常数");
		STAT_KEY_NAME_MAPPING.put(StatsNode.TODAY_INSERT_EXCEPTION_COUNT_KEY
				+ "," + StatsNode.TODAY_UPDATE_EXCEPTION_COUNT_KEY + ","
				+ StatsNode.TODAY_DELETE_EXCEPTION_COUNT_KEY, "今日异常数");

		STAT_KEY_NAME_MAPPING.put(StatsNode.LAST_PERIOD_AVG_INSERT_DELAY_KEY,
				"平均插入延迟");
		STAT_KEY_NAME_MAPPING.put(StatsNode.LAST_PERIOD_AVG_UPDATE_DELAY_KEY,
				"平均更新延迟");
		STAT_KEY_NAME_MAPPING.put(StatsNode.LAST_PERIOD_AVG_DELETE_DELAY_KEY,
				"平均删除延迟");
		STAT_KEY_NAME_MAPPING.put(
				StatsNode.LAST_PERIOD_AVG_EXTRACTOR_DELAY_KEY, "平均Extractor延迟");
		STAT_KEY_NAME_MAPPING.put(StatsNode.LAST_PERIOD_AVG_INSERT_DELAY_KEY
				+ "," + StatsNode.LAST_PERIOD_AVG_UPDATE_DELAY_KEY + ","
				+ StatsNode.LAST_PERIOD_AVG_DELETE_DELAY_KEY + ","
				+ StatsNode.LAST_PERIOD_AVG_EXTRACTOR_DELAY_KEY, "平均延迟");
	}

	public static String getStatNameByKey(String key) {
		return STAT_KEY_NAME_MAPPING.get(key);
	}
}
