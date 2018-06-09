package com.taobao.jingwei.webconsole.biz.manager;

import java.util.HashMap;
import java.util.Map;

public class JadeEnvMapManager {
	/**
	 * #jade 1 : ���׻���
		jade.env.map=���׻���:1
		#jade 2 : ���ܻ���
		jade.env.map=���ܻ���:2
		#jade 3 : �ճ�����
		jade.env.map=�ճ�����:3
		#jade 3 : �ճ�����
		jade.env.map=��������:3
	 */
	// key ���֣�value ��ֵ ������jade API�ӿڣ�
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
	 * @param host ҳ�洫��host ���
	 * @return <code>null</code>��ʾ��ȡjade����ʧ��
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

	// ���׻���:1|���ܻ���:2|
	// ���߷ָ��ĵ��ַ���
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
