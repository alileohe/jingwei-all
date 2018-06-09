package com.taobao.jingwei.monitor;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.config.ConfigManager;
import com.taobao.jingwei.common.config.impl.zk.ZkConfig;
import com.taobao.jingwei.common.config.impl.zk.ZkConfigManager;
import com.taobao.jingwei.monitor.alert.AlertConfig;
import com.taobao.jingwei.monitor.alert.AlertMsgManager;
import com.taobao.jingwei.monitor.conf.MonitorConfig;
import com.taobao.jingwei.monitor.core.MonitorCoreThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class JingweiMonitorMain {
	private final static Logger logger = LoggerFactory.getLogger(JingweiMonitorMain.class);

	private ConfigManager configManager;

	private AlertMsgManager alertMsgManager;

	private MonitorConfig monitorConfig;

	/**
	 * @param args [0] �����ļ�������·��
	 */
	public static void main(String[] args) {

		JingweiMonitorMain jingweiMonitorMain = new JingweiMonitorMain();
		if (!jingweiMonitorMain.checkAndParserBootPram(args)) {
			logger.error("[jingwei monitor] check parameter from conf file error!");
			return;
		}
		ConfigManager configManager = jingweiMonitorMain.getConfigManager();

		AlertMsgManager monitorMsgManager = jingweiMonitorMain.getAlertMsgManager();
		MonitorConfig monitorConfig = jingweiMonitorMain.getMonitorConfig();

		// �����������ͨ�������������߳�
		MonitorCoreThread agentCoreThread = new MonitorCoreThread(configManager, monitorMsgManager, monitorConfig);
		new Thread(agentCoreThread).start();

		// ��ʼ�����ڵȴ�
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			logger.error("[jingwei monitor] Main Thread InterruptedException", e);
			Runtime.getRuntime().halt(-1);
		}
	}

	public boolean checkAndParserBootPram(String[] args) {
		boolean checkRet = false;
		if (args.length < 1) {
			logger.error("[jingwei monitor] please set conf file path!");
			return checkRet;
		}
		String confPath = args[0];
		if (StringUtil.isBlank(confPath)) {
			logger.error("[jingwei monitor] please set conf file path!");
			return checkRet;
		}
		// ƴװ������monitor�������ļ�·��
		File configFile = new File(confPath);
		if (!configFile.exists()) {
			logger.error("[jingwei monitor] monitor configFile not exists!");
			return checkRet;
		}

		// ��ʼ���������
		monitorConfig = MonitorConfig.getMonitorConfig();
		if (null == monitorConfig) {
			logger.error("[jingwei monitor] can not get local host name!");
			return checkRet;
		}

		// ��ʼ��ZK������
		ZkConfig zkConfig = ZkConfig.getZkConfigFromFile(confPath);
		if (null == zkConfig) {
			logger.error("[jingwei monitor]zk config Error please check!");
			return checkRet;
		}
		ZkConfigManager zkConfigManager = new ZkConfigManager();
		zkConfigManager.setZkConfig(zkConfig);
		zkConfigManager.init();
		this.setConfigManager(zkConfigManager);

		// ��ʼ���澯������
		AlertConfig alertConfig = AlertConfig.getAlertConfigFromFile(confPath);
		if (null == alertConfig) {
			logger.error("[jingwei monitor]alert config Error please check!");
			return checkRet;
		}
		this.alertMsgManager = new AlertMsgManager(alertConfig);

		return true;
	}

	public ConfigManager getConfigManager() {
		return configManager;
	}

	public void setConfigManager(ConfigManager configManager) {
		this.configManager = configManager;
	}

	public AlertMsgManager getAlertMsgManager() {
		return alertMsgManager;
	}

	public void setAlertMsgManager(AlertMsgManager alertMsgManager) {
		this.alertMsgManager = alertMsgManager;
	}

	public MonitorConfig getMonitorConfig() {
		return monitorConfig;
	}

	public void setMonitorConfig(MonitorConfig monitorConfig) {
		this.monitorConfig = monitorConfig;
	}
}
