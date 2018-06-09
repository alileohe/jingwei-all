package com.taobao.jingwei.server;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.config.ConfigManager;
import com.taobao.jingwei.common.config.impl.zk.ZkConfig;
import com.taobao.jingwei.common.config.impl.zk.ZkConfigManager;
import com.taobao.jingwei.server.config.ServerConfig;
import com.taobao.jingwei.server.core.ServerCoreThread;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;

public class JingweiServerMain {
	private static Log logger = LogFactory.getLog(JingweiServerMain.class);

	private ConfigManager configManager;

	private ServerConfig serverConfig;

	/**
	 * @param args [0] �����ļ�������·��
	 */
	public static void main(String[] args) {

		JingweiServerMain jingweiServerMain = new JingweiServerMain();
		if (!jingweiServerMain.checkAndParserBootPram(args)) {
			logger.error("[jingwei server] check parameter error!");
			return;
		}

		ConfigManager configManager = jingweiServerMain.getConfigManager();
		ServerConfig serverConfig = jingweiServerMain.getServerConfig();

		// �����������ͨ�������������߳�
		ServerCoreThread serverCoreThread = new ServerCoreThread(configManager, serverConfig);
		new Thread(serverCoreThread).start();

		// ��ʼ�����ڵȴ�
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			logger.error("[jingwei server] Main Thread InterruptedException", e);
			Runtime.getRuntime().halt(-1);
		}
	}

	public boolean checkAndParserBootPram(String[] args) {
		boolean checkRet = false;
		if (args.length < 1) {
			logger.error("[jingwei server] please set conf file path!");
			return checkRet;
		}
		String confPath = args[0];
		if (StringUtil.isBlank(confPath)) {
			logger.error("[jingwei server] please set conf file path!");
			return checkRet;
		}
		// ƴװ������server�������ļ�·��
		File configFile = new File(confPath);
		if (!configFile.exists()) {
			logger.error("[jingwei server] server configFile not exists!");
			return checkRet;
		}
		// ��ʼ���������
		serverConfig = ServerConfig.getServerConfigFromFile(confPath);
		if (serverConfig == null) {
			logger.error("[jingwei server] get local host name error!");
			return checkRet;
		}

		// ��ʼ��ZK������
		ZkConfig zkConfig = ZkConfig.getZkConfigFromFile(confPath);
		if (null == zkConfig) {
			logger.error("[jingwei server]zk config Error please check!");
			return checkRet;
		}
		ZkConfigManager zkConfigManager = new ZkConfigManager();
		zkConfigManager.setZkConfig(zkConfig);
		zkConfigManager.init();
		this.setConfigManager(zkConfigManager);

		return true;
	}

	public ConfigManager getConfigManager() {
		return configManager;
	}

	public void setConfigManager(ConfigManager configManager) {
		this.configManager = configManager;
	}

	public ServerConfig getServerConfig() {
		return serverConfig;
	}

	public void setServerConfig(ServerConfig serverConfig) {
		this.serverConfig = serverConfig;
	}
}
