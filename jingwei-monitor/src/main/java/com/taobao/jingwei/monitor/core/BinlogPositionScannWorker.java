package com.taobao.jingwei.monitor.core;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiUtil;
import com.taobao.jingwei.common.TaskUtil;
import com.taobao.jingwei.common.node.PositionNode;
import com.taobao.jingwei.common.node.monitor.MonitorTaskNode;
import com.taobao.jingwei.monitor.listener.MonitorTaskScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @desc /jingwei/tasks/**task/hosts/host/lastCommit �ڵ��¼�˲���binlog��ʱ�䣨sourceTime����position��MySQL������binlog��sourceTime��-> jingwei
 *       core�������ѵ�Event��sourceTime�ύд��ZK��-> jingwei monitor����ȡzk��sourceTime������������ĵ�ǰʱ�� >> ��zk��ȡ����sourceTime����澯 (1) λ��೤ʱ��û�б仯��澯
 *       (2) λ���timestamp�Ӻ�೤ʱ����澯
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Jun 8, 2012 11:31:28 AM
 */

public class BinlogPositionScannWorker extends AbstractScanWorker {
	/** "yyyy-MM-dd HH:mm:ss.SSS" */
	private SimpleDateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);

	private static Logger logger = LoggerFactory.getLogger(BinlogPositionScannWorker.class);

	private ThreadLocal<String> msg = new ThreadLocal<String>();

	public BinlogPositionScannWorker(MonitorTaskScheduler scheduler, long tickTime) {
		super(scheduler, tickTime);
	}

	@Override
	protected long getConfigScanInterval() {
		return DEFAULT_SCAN_POSITIN_PERIOD;
	}

	@Override
	protected AtomicLong getRunTimeTicker() {
		return this.scheduler.getPositionTicker();
	}

	@Override
	protected boolean findException(MonitorTaskNode monitorTaskNode, String hostName, List<String> hostNames) {

		PositionNode positionNode = TaskUtil.getPositionNode(configManager, taskName);

		if (null == positionNode) {
			logger.error("[jwm]position node is null " + taskName);
			return false;
		}

		long current = System.currentTimeMillis();

		boolean findPositionNotChangeErr = this.processPositionNotChange(monitorTaskNode, positionNode, current);
		// �������λ��û�б仯�򷵻أ� ����Ҫ�ټ��λ���ʱ����Ƿ����
		if (findPositionNotChangeErr) {
			return true;
		}

		// ���λ��ʱ���Ƿ�����̫��
		boolean findPositionTmspDelay = this.processTimeDelay(monitorTaskNode, positionNode, current);
		if (findPositionTmspDelay) {
			return true;
		}

		return false;
	}

	@Override
	protected String getMsg() {
		String temp = this.msg.get();
		this.msg.remove();
		return temp;
	}

	@Override
	protected boolean isToggleOpened(MonitorTaskNode monitorTaskNode) {
		logger.warn("[jwm]" + " task : " + monitorTaskNode.getTaskName() + " position toggle : "
				+ monitorTaskNode.isPositionToggle());
		return monitorTaskNode.isPositionToggle();
	}

	@Override
	protected boolean isBeyondFrozenTime(long current, MonitorTaskNode monitorTaskNode, String hostName) {
		logger.warn("[jwm]" + " task : " + monitorTaskNode.getTaskName() + " current : " + current
				+ "; last position alarm time : " + this.scheduler.getLastPositionAlarmTime() + ", frozen period : "
				+ monitorTaskNode.getPositionFrozenPeriod());
		return (current - this.scheduler.getLastPositionAlarmTime()) > monitorTaskNode.getPositionFrozenPeriod();
	}

	@Override
	protected void log(String msg) {
		logger.warn(msg);
	}

	@Override
	protected void flushLastAlarmTime(long tmsp, String hostName) {
		this.scheduler.setLastPositionAlarmTime(tmsp);
	}

	/**
	 * 
	 * @param monitorTaskNode
	 * @param positionNode
	 * @param current
	 * @return <code>true</code> ��ʾ�����˸澯��Ϣ��<code>false</code> ��ʾû�в����澯��Ϣ
	 */
	private boolean processTimeDelay(MonitorTaskNode monitorTaskNode, PositionNode positionNode, long current) {
		long positionTimeStamp = 0L;
		Date date = positionNode.getTimestamp();
		if (date == null) {
			if (logger.isInfoEnabled()) {
				logger.info("[jwm] position timestamp is null " + taskName + ", return.");
			}
			return false;
		} else {
			positionTimeStamp = date.getTime();
		}
		
		String positionStr =  positionNode.getPosition();
		if (StringUtil.isBlank(positionStr)) {
			positionTimeStamp = 0L;
		} else {
			positionTimeStamp =  JingWeiUtil.getPositionMillis(positionStr);
		}
		

		long diff = current - positionTimeStamp;

		String positionTime = this.dateFormat.format(positionTimeStamp);

		String msg = "[jwm]" + taskName + " binlog delay " + diff / 1000 + " s. zk:" + positionTime;
		logger.warn(msg);

		long positionDelayAlarmThreshold = monitorTaskNode.getPositionDelayAlarmThreshold();
		logger.warn("[jwm]" + taskName + " zk position delay alarm threshold is : " + positionDelayAlarmThreshold
				+ "ms.");
		if (diff > positionDelayAlarmThreshold) {
			this.msg.set(msg);
			return true;
		}

		return false;
	}

	/**
	 * 
	 * @param monitorTaskNode
	 * @param positionNode
	 * @param current
	 * @return <code>true</code> ��ʾ�����˸澯��Ϣ��<code>false</code> ��ʾû�в����澯��Ϣ
	 */
	private boolean processPositionNotChange(MonitorTaskNode monitorTaskNode, PositionNode positionNode, long current) {
		String zkPosition = positionNode.getPosition();
		String lastCommitPosition = this.scheduler.getLastCommitPosition();
		boolean positionNotChange = this.scheduler.isPositionNotChange();

		if (zkPosition.equals(lastCommitPosition)) {
			logger.warn("[jwm] position not change " + lastCommitPosition + " task " + taskName);

			// ��һ�η���λ��û�б仯�����ʱ������ñ�־λ
			if (!positionNotChange) {
				this.scheduler.setPositionNotChange(true);
				this.scheduler.setPositionNotChangeTime(current);
			}

			long alarmThreshold = monitorTaskNode.getPositionAlarmThreshold();

			long positionNotChangeTime = this.scheduler.getPositionNotChangeTime();
			// �ó�ʱ��λ�㶼û�б仯
			long diff = current - positionNotChangeTime;
			if (diff > alarmThreshold) {
				String msg = "[jwm]" + this.taskName + " position not change" + " for " + diff / 1000 + "s.";
				this.msg.set(msg);
				return true;
			}
		} else {
			this.scheduler.setPositionNotChange(false);
		}

		this.scheduler.setLastCommitPosition(zkPosition);

		return false;
	}

}
