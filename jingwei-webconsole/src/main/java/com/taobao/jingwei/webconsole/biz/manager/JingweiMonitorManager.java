package com.taobao.jingwei.webconsole.biz.manager;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.taobao.jingwei.common.config.impl.zk.ZkConfigManager;
import com.taobao.jingwei.monitor.alert.AlertConfig;
import com.taobao.jingwei.monitor.alert.AlertMsgManager;
import com.taobao.jingwei.monitor.conf.MonitorConfig;
import com.taobao.jingwei.monitor.core.MonitorCoreThread;

/**
 * @desc 加载monitor的管理类
 * 
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Jul 17, 2012 8:58:06 AM
 */

public class JingweiMonitorManager {
	/**  是否自动加载monitor */
	private boolean loadMonitor = true;

	/** alert configs */
	private String serviceType = "custom";
	private String serviceVersion = "1.0.0";
	private String serviceGroup = "HSF";
	private String customServerHosts = "110.75.2.144:9999,110.75.27.72:9999";
	private String connectionTimeout;
	private String receiveTimeout;
	private String alertUsers;
	private String smsNumbers;

	/** @see com.taobao.jingwei.webconsole.biz.manager.JingweiZkConfigManager */
	@Autowired
	private JingweiZkConfigManager jwConfigManager;

	private AlertMsgManager alertMsgManager;

	private MonitorConfig monitorConfig;

	public void init() {
		if (this.loadMonitor) {
			this.monitorConfig = MonitorConfig.getMonitorConfig();

			// init alert manager
			AlertConfig alertConfig = new AlertConfig();
			alertConfig.setServiceType(this.getServiceType());
			alertConfig.setCustomServerHosts(this.getCustomServerHosts());
			alertConfig.setServiceVersion(this.getServiceVersion());
			alertConfig.setServiceGroup(this.getServiceGroup());
			alertConfig.setAlertUsers(this.getAlertUsers());
			alertConfig.setSmsNumbers(this.getSmsNumbers());

			alertConfig.setConnectionTimeout(Long.valueOf(this.getConnectionTimeout()));
			alertConfig.setReceiveTimeout(Long.valueOf(this.getReceiveTimeout()));
			this.alertMsgManager = new AlertMsgManager(alertConfig);

			this.startMonitor();
		}
	}

	/**
	 * 启动监控
	 */
	private void startMonitor() {
		Set<String> keys = JingweiZkConfigManager.getKeys();
		for (String key : keys) {
			ZkConfigManager configManager = jwConfigManager.getZkConfigManager(key);

			// start monitor thread
			MonitorCoreThread agentCoreThread = new MonitorCoreThread(configManager, alertMsgManager, monitorConfig);
			new Thread(agentCoreThread).start();
		}
	}

	public boolean isLoadMonitor() {
		return loadMonitor;
	}

	public void setLoadMonitor(boolean loadMonitor) {
		this.loadMonitor = loadMonitor;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getServiceVersion() {
		return serviceVersion;
	}

	public void setServiceVersion(String serviceVersion) {
		this.serviceVersion = serviceVersion;
	}

	public String getServiceGroup() {
		return serviceGroup;
	}

	public void setServiceGroup(String serviceGroup) {
		this.serviceGroup = serviceGroup;
	}

	public String getCustomServerHosts() {
		return customServerHosts;
	}

	public void setCustomServerHosts(String customServerHosts) {
		this.customServerHosts = customServerHosts;
	}

	public String getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(String connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public String getReceiveTimeout() {
		return receiveTimeout;
	}

	public void setReceiveTimeout(String receiveTimeout) {
		this.receiveTimeout = receiveTimeout;
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

	public JingweiZkConfigManager getJwConfigManager() {
		return jwConfigManager;
	}

	public void setJwConfigManager(JingweiZkConfigManager jwConfigManager) {
		this.jwConfigManager = jwConfigManager;
	}

	public String getAlertUsers() {
		return alertUsers;
	}

	public void setAlertUsers(String alertUsers) {
		this.alertUsers = alertUsers;
	}

	public String getSmsNumbers() {
		return smsNumbers;
	}

	public void setSmsNumbers(String smsNumbers) {
		this.smsNumbers = smsNumbers;
	}

}
