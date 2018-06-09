package com.taobao.jingwei.common.config.impl.zk;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiUtil;

import java.util.Map;

/**
 * ZKClint�����ö���
 * 
 * @author qihao
 *
 */
public class ZkConfig {

	private final static String ZK_CONFIG_SECTION_NAME = "ZooKeeper";

	/**
	 * ini�ļ�zk�ĵ�host��Ӧ��key,һ���Ӧ��ֵ��IP���ŷָ����ַ���
	 */
	private final static String ZK_CONFIG_HOSTS_KEY = "Hosts";

	/**
	 * ini�ļ�zk��sessionTimout��Ӧ��key,��λms
	 */
	private final static String ZK_CONFIG_SESSION_TIMEOUT_KEY = "SessionTimeOut";

	/**
	 * ini�ļ�zk��connectionTimeout��Ӧ��key,��λms
	 */
	private final static String ZK_CONFIG_CONNECTION_TIMEOUT_KEY = "ConnectionTimeout";

	/**
	 * ZK�ķ�������ַ�б����ŷָ�
	 */
	public String zkHosts;

	/**ZK��session��ʱ*/
	public int zkSessionTimeoutMs = 30000;

	/**
	 * ZK�����ӳ�ʱ
	 */
	public int zkConnectionTimeoutMs = 30000;

	private String zkConfigFile;

	public ZkConfig() {
	}

	public ZkConfig(String zkHosts) {
		this.zkHosts = zkHosts;
	}

	public String getZkHosts() {
		return zkHosts;
	}

	public void setZkHosts(String zkHosts) {
		this.zkHosts = zkHosts;
	}

	public int getZkSessionTimeoutMs() {
		return zkSessionTimeoutMs;
	}

	public void setZkSessionTimeoutMs(int zkSessionTimeoutMs) {
		this.zkSessionTimeoutMs = zkSessionTimeoutMs;
	}

	public int getZkConnectionTimeoutMs() {
		return zkConnectionTimeoutMs;
	}

	public void setZkConnectionTimeoutMs(int zkConnectionTimeoutMs) {
		this.zkConnectionTimeoutMs = zkConnectionTimeoutMs;
	}

	public static ZkConfig getZkConfigFromFile(String filePath) {
		ZkConfig zkConfig = null;
		Map<String, String> zkConfMap = JingWeiUtil.getIniValuesFromFile(filePath, ZK_CONFIG_SECTION_NAME, null);
		if (!zkConfMap.isEmpty()) {
			String hosts = zkConfMap.get(ZK_CONFIG_HOSTS_KEY);
			zkConfig = new ZkConfig(hosts);
			zkConfig.setZkConfigFile(filePath);
			String sessionTimout = zkConfMap.get(ZK_CONFIG_SESSION_TIMEOUT_KEY);
			if (StringUtil.isNotBlank(sessionTimout) && StringUtil.isNumeric(sessionTimout)) {
				zkConfig.setZkSessionTimeoutMs(Integer.valueOf(sessionTimout));
			}
			String connectionTimeout = zkConfMap.get(ZK_CONFIG_CONNECTION_TIMEOUT_KEY);
			if (StringUtil.isNotBlank(connectionTimeout) && StringUtil.isNumeric(connectionTimeout)) {
				zkConfig.setZkConnectionTimeoutMs(Integer.valueOf(connectionTimeout));
			}
		}
		return zkConfig;
	}

	public String getZkConfigFile() {
		return zkConfigFile;
	}

	public void setZkConfigFile(String zkConfigFile) {
		this.zkConfigFile = zkConfigFile;
	}
}