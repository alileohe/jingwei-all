package com.taobao.jingwei.monitor.core;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.config.ConfigManager;
import com.taobao.jingwei.common.node.monitor.MonitorTaskNode;
import com.taobao.jingwei.monitor.listener.MonitorTaskScheduler;
import com.taobao.jingwei.monitor.util.MonitorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @desc ɨ����������״̬
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date May 22, 2012 11:08:49 AM
 */

public abstract class AbstractScanWorker implements IStateChecker, JingWeiConstants {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/** tickʱ���� ��һ��2~3�� */
	private final long tickTime;

	/** ������ */
	protected final String taskName;

	/** ��Ӧ�ĵ��������൱��context */
	protected final MonitorTaskScheduler scheduler;

	/** ���ù����� */
	protected final ConfigManager configManager;

	public AbstractScanWorker(MonitorTaskScheduler scheduler, long tickTime) {
		this.scheduler = scheduler;
		this.configManager = scheduler.getConfigManager();
		this.tickTime = tickTime;
		this.taskName = this.scheduler.getMonitorTaskNode().getTaskName();
	}

	/**
	 * ��ȡ��zk���õ�ɨ��ʱ����
	 * 
	 * @return
	 */
	abstract protected long getConfigScanInterval();

	/**
	 * ��ȡ��ʱ��
	 * 
	 * @return
	 */
	abstract protected AtomicLong getRunTimeTicker();

	/**
	 * ��������澯�ڷ���false�����û�з����澯˵������������true
	 * 
	 * @return
	 */
	protected void scan(String hostName, List<String> hostNames) {

		MonitorTaskNode monitorTaskNode = this.scheduler.getMonitorTaskNode();

		if (monitorTaskNode == null) {
			logger.error("[jwm] MonitorTaskNode is null -" + taskName + ", return.");
			return;
		}

		long current = System.currentTimeMillis();

		// �����쳣���
		if (this.findException(monitorTaskNode, hostName, hostNames)) {
			String msg = this.getMsg();

			// ����־
			this.log(msg);

			// �澯���ش򿪲��ҵ��ﶳ������
			if (this.isToggleOpened(monitorTaskNode) && this.isBeyondFrozenTime(current, monitorTaskNode, hostName)) {
				if (StringUtil.isNotBlank(msg)) {
					logger.warn("[jwm] send alarm : " + msg);
					this.sendAlarm(monitorTaskNode, msg);
				}

				this.flushLastAlarmTime(current, hostName);
			}
		}
	}

	abstract protected boolean findException(MonitorTaskNode monitorTaskNode, String hostName, List<String> hostNames);

	abstract protected String getMsg();

	abstract protected boolean isToggleOpened(MonitorTaskNode monitorTaskNode);

	abstract protected boolean isBeyondFrozenTime(long current, MonitorTaskNode monitorTaskNode, String hostName);

	abstract protected void log(String msg);

	/**
	 * ���͸澯ww�Ͷ���
	 * 
	 * @param monitorTaskNode
	 * @param msg
	 */
	protected void sendAlarm(MonitorTaskNode monitorTaskNode, String msg) {
		if (StringUtil.isNotBlank(msg)) {
			MonitorUtil.wwAndSmsAlert(scheduler.getAlertMsgManager(), monitorTaskNode, msg);
		}
	}

	abstract protected void flushLastAlarmTime(long tmsp, String hostName);

	/**
	 * �������
	 */
	protected void resetTicker() {
		if (logger.isInfoEnabled()) {
			logger.info("[jwm] reset tick time.");
		}
		this.getRunTimeTicker().set(this.getConfigScanInterval());
	}

	@Override
	public boolean check(String hostName, List<String> hostNames) {
		// ʣ�¶���ʱ��ſ���ɨ��
		long tickerRemain = this.getRunTimeTicker().addAndGet(-this.getTickTime());

		if (tickerRemain > 0) {
			if (logger.isInfoEnabled()) {
				logger.info("[jwm] tick time remain is " + tickerRemain + " " + taskName);
			}
			return false;
		}

		// ���С�ڵ���0����˵��ɨ����ʱ�䵽����Ҫɨ��zk�ڵ���
		// ɨ��zk����ʱ����
		this.scan(hostName, hostNames);

		// ɨ������λ
		this.resetTicker();

		return true;
	}

	protected void scan(List<String> hostName) {

	}

	public MonitorTaskScheduler getScheduler() {
		return scheduler;
	}

	public long getTickTime() {
		return tickTime;
	}

	public String getTaskName() {
		return taskName;
	}
}
