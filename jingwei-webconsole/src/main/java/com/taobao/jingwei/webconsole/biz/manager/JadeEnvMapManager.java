package com.taobao.jingwei.webconsole.biz.manager;

import java.util.HashMap;
import java.util.Map;

public class JadeEnvMapManager {
	/**
	 * #jade 1 : 二套环境
		jade.env.map=二套环境:1
		#jade 2 : 性能环境
		jade.env.map=性能环境:2
		#jade 3 : 日常环境
		jade.env.map=日常环境:3
		#jade 3 : 日常环境
		jade.env.map=开发环境:3
	 */
	// key 汉字；value 数值 （根据jade API接口）
	private static Map<String, Integer> jadeEnvMap = new HashMap<String, Integer>();

	public void init() {
		String[] maps = this.getJadeEnvList().split(",");

		for (String map : maps) {
			String[] entry = map.split(":");
			jadeEnvMap.put(entry[0], Integer.valueOf(entry[1]));
		}

	}

	/**
	 * 
	 * @param host 页面传的host 编号
	 * @return <code>null</code>表示获取jade环境失败
	 * 
	 */
	public static Integer getJadeEnvIdFromJwHostIndex(String host) {
		String jingweEnv = JingweiZkConfigManager.getHostName(Integer.valueOf(host));

		for (Map.Entry<String, Integer> entry : jadeEnvMap.entrySet()) {
			if (entry.getKey().equals(jingweEnv)) {
				return entry.getValue();
			}
		}

		return null;

	}

	// 二套环境:1|性能环境:2|
	// 竖线分隔的的字符串
	private String jadeEnvList;

	public String getJadeEnvList() {
		return jadeEnvList;
	}

	public void setJadeEnvList(String jadeEnvList) {
		this.jadeEnvList = jadeEnvList;
	}

	public static Map<String, Integer> getJadeEnvMap() {
		return jadeEnvMap;
	}

	public static void setJadeEnvMap(Map<String, Integer> jadeEnvMap) {
		JadeEnvMapManager.jadeEnvMap = jadeEnvMap;
	}

}
