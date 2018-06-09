package com.taobao.jingwei.monitor.alert;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**   
 * <p>description:<p> 
 *
 * @{#} AlertConfig.java Create on Jun 16, 2011 10:52:42 AM   
 *   
 * Copyright (c) 2011 by qihao.
 *
 *@author <a href="mailto:qihao@taobao.com">qihao</a> 
 *@version 1.0   
 */
public class AlertConfig {
	/**
	 * service�ĵ�ַ��ȡ��ʽ��֧��2�ַ�ʽ
	 * 1:configServer��ʽ��������Ĭ��ʹ�ø÷�ʽ������ʹ��MsgConstants.SERVER_SERVER_LIST_TYPE����
	 * 2:�Զ����������ַ�б�ʽ������ʹ��MsgConstants.CUSTOM_SERVER_LIST_TYPE ����
	 */
	private String serviceType;

	/**
	 * configServer��ʽ�£�ָ������汾��������Ĭ��1.0.0
	 */
	private String serviceVersion;

	/**
	 * configServer��ʽ�µ�group��һ���������ã�Ĭ��HSF��
	 */
	private String serviceGroup;

	/**
	 * �Զ�������ַ�б�ʹ�á������ָ�Զ���ģʽ�¸�����
	 * �����Ϊ�ա����磺10.232.10.142:8047,10.232.10.143:8047
	 * 
	 */
	private String customServerHosts;

	/**
	 * WSDL�������ӳ�ʱ
	 */
	private long connectionTimeout = 2000L;

	/**
	 * WSDL���ý��ܳ�ʱ
	 */
	private long receiveTimeout = 10000L;

	private List<String> alertUsers = new ArrayList<String>();

	private List<String> smsNumbers = new ArrayList<String>();

	private List<String> mailUsers = new ArrayList<String>();

	private final static String ALERT_SECTION_NAME = "Alert";
	private final static String ALERT_SERVICE_TYPE_KEY = "ServiceType";
	private final static String ALERT_CUSTOM_SERVER_HOSTS_KEY = "CustomServerHosts";
	private final static String ALERT_SERVICE_VERSION_KEY = "ServiceVersion";
	private final static String ALERT_SERVICE_GROUP_KEY = "ServiceGroup";
	private final static String ALERT_CONNECTION_TIMEOUT_KEY = "ConnectionTimeout";
	private final static String ALERT_RECEIVE_TIMEOUT_KEY = "ReceiveTimeout";
	private final static String ALERT_GLOBA_SMS_KEY = "GlobaSmsUsers";
	private final static String ALERT_GLOBA_WW_KEY = "GlobaWwUsers";
	private final static String ALERT_GLOBA_MAIL_KEY = "GlobaMailUsers";

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

	public long getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(long connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public long getReceiveTimeout() {
		return receiveTimeout;
	}

	public void setReceiveTimeout(long receiveTimeout) {
		this.receiveTimeout = receiveTimeout;
	}

	public List<String> getAlertUsers() {
		return alertUsers;
	}

	public void setAlertUsers(String users) {
		String[] userNames = StringUtil.split(users, ",");
		for (String user : userNames) {
			this.alertUsers.add(user);
		}
	}

	public List<String> getSmsNumbers() {
		return smsNumbers;
	}

	public void setSmsNumbers(String smsNumbers) {
		String[] mobiles = StringUtil.split(smsNumbers, ",");
		for (String num : mobiles) {
			this.smsNumbers.add(num);
		}
	}

	public List<String> getMailUsers() {
		return mailUsers;
	}

	public void setMailUsers(String mailUsers) {
		String[] mails = StringUtil.split(mailUsers, ",");
		for (String mail : mails) {
			this.mailUsers.add(mail);
		}
	}

	public static AlertConfig getAlertConfigFromFile(String filePath) {
		AlertConfig alertConfig = null;
		Map<String, String> alertConfMap = JingWeiUtil.getIniValuesFromFile(filePath, ALERT_SECTION_NAME, null);
		if (!alertConfMap.isEmpty()) {
			alertConfig = new AlertConfig();
			alertConfig.setServiceType(StringUtil.defaultIfBlank(alertConfMap.get(ALERT_SERVICE_TYPE_KEY)));
			alertConfig
					.setCustomServerHosts(StringUtil.defaultIfBlank(alertConfMap.get(ALERT_CUSTOM_SERVER_HOSTS_KEY)));
			alertConfig.setServiceVersion(StringUtil.defaultIfBlank(alertConfMap.get(ALERT_SERVICE_VERSION_KEY)));
			alertConfig.setServiceGroup(StringUtil.defaultIfBlank(alertConfMap.get(ALERT_SERVICE_GROUP_KEY)));

			String conTimeOutStr = StringUtil.defaultIfBlank(alertConfMap.get(ALERT_CONNECTION_TIMEOUT_KEY));
			if (StringUtil.isNotBlank(conTimeOutStr) && StringUtil.isNumeric(conTimeOutStr)) {
				alertConfig.setConnectionTimeout(Long.valueOf(conTimeOutStr));
			}
			String receiveTimeoutStr = StringUtil.defaultIfBlank(alertConfMap.get(ALERT_RECEIVE_TIMEOUT_KEY));
			if (StringUtil.isNotBlank(receiveTimeoutStr) && StringUtil.isNumeric(receiveTimeoutStr)) {
				alertConfig.setReceiveTimeout(Long.valueOf(receiveTimeoutStr));
			}
			String alertUses = StringUtil.defaultIfBlank((String) alertConfMap.get(ALERT_GLOBA_WW_KEY));
			if (StringUtil.isNotBlank(alertUses)) {
				alertConfig.setAlertUsers(alertUses);
			}
			String alertSms = StringUtil.defaultIfBlank(alertConfMap.get(ALERT_GLOBA_SMS_KEY));
			if (StringUtil.isNotBlank(alertSms)) {
				alertConfig.setSmsNumbers(alertSms);
			}
			String mailUsers = StringUtil.defaultIfBlank(alertConfMap.get(ALERT_GLOBA_MAIL_KEY));
			if (StringUtil.isNotBlank(mailUsers)) {
				alertConfig.setMailUsers(mailUsers);
			}
		}
		return alertConfig;
	}
}