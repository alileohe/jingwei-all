package com.taobao.jingwei.monitor.util;

import com.taobao.jingwei.common.node.monitor.MonitorTaskNode;
import com.taobao.jingwei.monitor.alert.AlertMsgManager;
import com.taobao.jingwei.monitor.util.MonitorUtil.AlarmContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @desc
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Apr 17, 2013 4:52:05 PM
 * 
 */
class AlarmSender extends Thread {

	/** ������ʼʱ�� */
	private static final int START_WORK_TIME = 9;

	/** ��������ʱ�� */
	private static final int END_WORK_TIME = 18;
	private Logger logger = LoggerFactory.getLogger(AlarmSender.class);
	private LinkedBlockingDeque<AlarmContext> alarmContext;

	public AlarmSender(LinkedBlockingDeque<AlarmContext> alarmContext) {
		this.alarmContext = alarmContext;
		this.setName("alarm-sender");
	}

	@Override
	public void run() {
		while (true) {
			try {
				logger.warn("[jwm] try to take context:");
				AlarmContext context = alarmContext.take();
				logger.warn("[jwm] taked alarm context : " + context.getMsg());

				AlertMsgManager alertMsgManager = context.getAlertMsgManager();
				MonitorTaskNode monitorTaskNode = context.getMonitorTaskNode();
				String msg = context.getMsg();

				// ���Ÿ澯
				if (MonitorUtil.getGlobalconfig().isSmToggle()) {

					if (monitorTaskNode.isSmToggle()) {
						logger.warn("[jwm]send alarm by sm : " + msg);
						this.alertBySm(alertMsgManager, monitorTaskNode, msg);
					} else {
						logger.warn("[jwm]task sms toggle is closed!");
					}
				} else {
					logger.warn("[jwm]global sms toggle is closed!");
				}

				// �����澯
				if (MonitorUtil.getGlobalconfig().isWwToggle()) {
					if (monitorTaskNode.isWwToggle()) {
						logger.warn("[jwm]send alarm by ww : " + msg);
						this.alertByWw(alertMsgManager, monitorTaskNode, msg);
					} else {
						logger.warn("[jwm]task ww toggle is closed!");
					}
				} else {
					logger.warn("[jwm]global ww toggle is closed!");
				}

			} catch (Throwable e) {
				logger.error("[jwm]send alarm error!", e);
			}
		}
	}

	private synchronized void alertByWw(AlertMsgManager alertMsgManager, MonitorTaskNode monitorTaskNode, String msg)
			throws InterruptedException {

		logger.warn("[jwm] send ww now is workday time" + msg);
		List<String> list = new ArrayList<String>();
		Set<String> set = new HashSet<String>();

		// this.logger.warn("[jwm] default users are : " + alertMsgManager.getAlertConfig().getAlertUsers());
		set.addAll(alertMsgManager.getAlertConfig().getAlertUsers());
		logger.warn("[jwm] d");
		set.addAll(monitorTaskNode.getWwAlertUsers());

		list.addAll(set);

		for (String user : list) {

			alertMsgManager.alertByWW(user, msg);
			Thread.sleep(1000);
		}

		if (logger.isWarnEnabled()) {
			logger.warn("sent ww alarm message  " + msg + " to " + list);
		}
	}

	private void alertBySm(AlertMsgManager alertMsgManager, MonitorTaskNode monitorTaskNode, String msg)
			throws InterruptedException {
		long timeInMillis = System.currentTimeMillis();

		if (!nowIsWorkDayTime(timeInMillis)) {

			List<String> list = new ArrayList<String>();

			Set<String> set = new HashSet<String>();
			set.addAll(alertMsgManager.getAlertConfig().getSmsNumbers());
			set.addAll(monitorTaskNode.getSmsAlertUsers());

			list.addAll(set);

			for (String user : list) {
				alertMsgManager.alertByMobile(user, msg);
				Thread.sleep(1000);
			}

			if (logger.isWarnEnabled()) {
				logger.warn("sent mobile alarm message  " + msg + " to " + list);
			}
		}
	}

	public static boolean nowIsWorkDayTime(long timeInMillis) {
		return (nowIsWorkDay(timeInMillis) && nowIsWorkTime(timeInMillis));
	}

	public static boolean nowIsWorkDay(long timeInMillis) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeInMillis);
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
			return false;
		}

		return true;
	}

	public static boolean nowIsWorkTime(long timeInMillis) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeInMillis);
		int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

		if (hourOfDay >= START_WORK_TIME && hourOfDay < END_WORK_TIME) {
			return true;
		}

		return false;
	}
}