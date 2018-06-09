package com.taobao.jingwei.webconsole.model;

import java.util.HashMap;
import java.util.Map;

import com.taobao.jingwei.common.node.StatsNode;

public class JingweiStatConstant {
	private static Map<String, String> STAT_KEY_NAME_MAPPING = new HashMap<String, String>();

	static {
		STAT_KEY_NAME_MAPPING.put(StatsNode.LAST_PERIOD_INSERT_COUNT_KEY,
				"���ڲ����¼���");
		STAT_KEY_NAME_MAPPING.put(StatsNode.LAST_PERIOD_UPDATE_COUNT_KEY,
				"���ڸ����¼���");
		STAT_KEY_NAME_MAPPING.put(StatsNode.LAST_PERIOD_DELETE_COUNT_KEY,
				"���ڄh���¼���");
		STAT_KEY_NAME_MAPPING.put(StatsNode.LAST_PERIOD_INSERT_COUNT_KEY + ","
				+ StatsNode.LAST_PERIOD_UPDATE_COUNT_KEY + ","
				+ StatsNode.LAST_PERIOD_DELETE_COUNT_KEY, "�����¼���");

		STAT_KEY_NAME_MAPPING.put(
				StatsNode.LAST_PERIOD_INSERT_EXCEPTION_COUNT_KEY, "���ڲ����쳣��");
		STAT_KEY_NAME_MAPPING.put(
				StatsNode.LAST_PERIOD_UPDATE_EXCEPTION_COUNT_KEY, "���ڸ����쳣��");
		STAT_KEY_NAME_MAPPING.put(
				StatsNode.LAST_PERIOD_DELETE_EXCEPTION_COUNT_KEY, "����ɾ���쳣��");
		STAT_KEY_NAME_MAPPING.put(
				StatsNode.LAST_PERIOD_INSERT_EXCEPTION_COUNT_KEY + ","
						+ StatsNode.LAST_PERIOD_UPDATE_EXCEPTION_COUNT_KEY
						+ ","
						+ StatsNode.LAST_PERIOD_DELETE_EXCEPTION_COUNT_KEY,
				"�����쳣��");

		STAT_KEY_NAME_MAPPING
				.put(StatsNode.LAST_PERIOD_INSERT_TPS_KEY, "����TPS");
		STAT_KEY_NAME_MAPPING
				.put(StatsNode.LAST_PERIOD_UPDATE_TPS_KEY, "����TPS");
		STAT_KEY_NAME_MAPPING
				.put(StatsNode.LAST_PERIOD_DELETE_TPS_KEY, "ɾ��TPS");
		STAT_KEY_NAME_MAPPING.put(StatsNode.LAST_PERIOD_INSERT_TPS_KEY + ","
				+ StatsNode.LAST_PERIOD_UPDATE_TPS_KEY + ","
				+ StatsNode.LAST_PERIOD_DELETE_TPS_KEY, "TPS");

		STAT_KEY_NAME_MAPPING.put(StatsNode.TODAY_INSERT_COUNT_KEY, "���ղ����¼���");
		STAT_KEY_NAME_MAPPING.put(StatsNode.TODAY_UPDATE_COUNT_KEY, "���ո����¼���");
		STAT_KEY_NAME_MAPPING.put(StatsNode.TODAY_DELETE_COUNT_KEY, "����ɾ���¼���");
		STAT_KEY_NAME_MAPPING.put(StatsNode.TODAY_INSERT_COUNT_KEY + ","
				+ StatsNode.TODAY_UPDATE_COUNT_KEY + ","
				+ StatsNode.TODAY_DELETE_COUNT_KEY, "�����¼���");

		STAT_KEY_NAME_MAPPING.put(StatsNode.TODAY_INSERT_EXCEPTION_COUNT_KEY,
				"���ղ����쳣��");
		STAT_KEY_NAME_MAPPING.put(StatsNode.TODAY_UPDATE_EXCEPTION_COUNT_KEY,
				"���ո����쳣��");
		STAT_KEY_NAME_MAPPING.put(StatsNode.TODAY_DELETE_EXCEPTION_COUNT_KEY,
				"����ɾ���쳣��");
		STAT_KEY_NAME_MAPPING.put(StatsNode.TODAY_INSERT_EXCEPTION_COUNT_KEY
				+ "," + StatsNode.TODAY_UPDATE_EXCEPTION_COUNT_KEY + ","
				+ StatsNode.TODAY_DELETE_EXCEPTION_COUNT_KEY, "�����쳣��");

		STAT_KEY_NAME_MAPPING.put(StatsNode.LAST_PERIOD_AVG_INSERT_DELAY_KEY,
				"ƽ�������ӳ�");
		STAT_KEY_NAME_MAPPING.put(StatsNode.LAST_PERIOD_AVG_UPDATE_DELAY_KEY,
				"ƽ�������ӳ�");
		STAT_KEY_NAME_MAPPING.put(StatsNode.LAST_PERIOD_AVG_DELETE_DELAY_KEY,
				"ƽ��ɾ���ӳ�");
		STAT_KEY_NAME_MAPPING.put(
				StatsNode.LAST_PERIOD_AVG_EXTRACTOR_DELAY_KEY, "ƽ��Extractor�ӳ�");
		STAT_KEY_NAME_MAPPING.put(StatsNode.LAST_PERIOD_AVG_INSERT_DELAY_KEY
				+ "," + StatsNode.LAST_PERIOD_AVG_UPDATE_DELAY_KEY + ","
				+ StatsNode.LAST_PERIOD_AVG_DELETE_DELAY_KEY + ","
				+ StatsNode.LAST_PERIOD_AVG_EXTRACTOR_DELAY_KEY, "ƽ���ӳ�");
	}

	public static String getStatNameByKey(String key) {
		return STAT_KEY_NAME_MAPPING.get(key);
	}
}
