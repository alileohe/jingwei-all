package com.taobao.jingwei.monitor.alert;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jm.msgcenter.MsgManager;
import com.taobao.jm.msgcenter.MsgManager.MsgType;
import com.taobao.jm.msgcenter.common.Result;

import java.text.MessageFormat;
import java.util.List;

public class AlertMsgManager {

	private static final String SOURCE_ID = "yugong*yugong";
	private static final String TEMPLATE_ID = "168849275";
	private static final String MESSAGE_TYPE_ID = "176904884";

	private static final String SUBJECT = "title:JingWei-Alert";
	private static final MessageFormat CONTENT_FORMAT = new MessageFormat("content:{0}");

	private final MsgManager msgManager;

	private final AlertConfig alertConfig;

	public AlertMsgManager(AlertConfig alertConfig) {
		this.alertConfig = alertConfig;
		msgManager = new MsgManager();
		msgManager.setServiceType(alertConfig.getServiceType());
		msgManager.setCustomServerHosts(alertConfig.getCustomServerHosts());
		msgManager.setServiceVersion(alertConfig.getServiceVersion());
		msgManager.setServiceGroup(alertConfig.getServiceGroup());
		msgManager.setConnectionTimeout(alertConfig.getConnectionTimeout());
		msgManager.setReceiveTimeout(alertConfig.getReceiveTimeout());
		msgManager.init();
	}

	public Result alertByWW(String user, String msg) {
		return sendAlertMsg(user, msg, MsgType.WANGWANG_TYPE);
	}

	public void alertByWW(List<String> users, String msg) {
		if (null != users && StringUtil.isNotBlank(msg)) {
			for (String user : users) {
				try {
					alertByWW(user, msg);
				} catch (Exception e) {
					continue;
				}
			}
		}
	}

	public Result alertByMobile(String mobile, String msg) {
		return sendAlertMsg(mobile, msg, MsgType.SMS_TYPE);
	}

	public void alertByMobile(List<String> mobiles, String msg) {
		if (null != mobiles && StringUtil.isNotBlank(msg)) {
			for (String mobile : mobiles) {
				try {
					alertByMobile(mobile, msg);
				} catch (Exception e) {
					continue;
				}
			}
		}
	}

	public Result alertByEmail(String email, String msg) {
		return sendAlertMsg(email, msg, MsgType.MAIL_TYPE);
	}

	public void alertByEmail(List<String> emails, String msg) {
		if (null != emails && !emails.isEmpty()) {
			for (String mail : emails) {
				try {
					alertByEmail(mail, msg);
				} catch (Exception e) {
					continue;
				}
			}
		}
	}

	private Result sendAlertMsg(String target, String content, String msgType) {
		Result result = Result.createResult();
		String msgStr = CONTENT_FORMAT.format(new String[] { content });
		result = msgManager.sendMsg(target, SUBJECT, msgStr, msgType, SOURCE_ID, TEMPLATE_ID, MESSAGE_TYPE_ID);
		return result;
	}

	public AlertConfig getAlertConfig() {
		return alertConfig;
	}

}