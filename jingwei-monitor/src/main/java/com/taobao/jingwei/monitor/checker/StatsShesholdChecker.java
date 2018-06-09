package com.taobao.jingwei.monitor.checker;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.config.ConfigManager;
import com.taobao.jingwei.common.node.StatsNode;
import com.taobao.jingwei.common.node.monitor.MonitorTaskNode;
import com.taobao.jingwei.monitor.alert.AlertMsgManager;
import com.taobao.jingwei.monitor.listener.MonitorTaskScheduler;
import com.taobao.jingwei.monitor.util.MonitorUtil;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @desc ɨ��/jingwei/tasks/**task/**host/status�ڵ����ݣ�������ֵ��ͨ����������
 * 
 * @author <a href="mailto:shuohai.lhl@taobao.com">shuohailhl</a>
 * 
 * @date 2011-11-16����11:32:27
 */
public class StatsShesholdChecker {
	private Logger log = LoggerFactory.getLogger(StatsShesholdChecker.class);

	private final MonitorTaskScheduler monitorTaskScheduler;

	private final ConfigManager configManager;

	private final AlertMsgManager alertMsgManager;

	private final String taskName;

	/** �ϴ�ɾ���ӳٸ澯ʱ�� */
	private long lastDeleteDelayAlarmTime = 0L;

	/** �ϴ�ɾ��tps���޸澯ʱ�� */
	private long lastDeleteLowerTpsAlarmTime = 0L;

	/** �ϴ�ɾ���ӳٸ澯ʱ�� */
	private long lastDeleteUpperTpsAlarmTime = 0L;

	/** �ϴ�ɾ���쳣�澯ʱ�� */
	private long lastDeleteExceptionAlarmTime = 0L;

	/** �ϴ�ɾ���ӳٸ澯ʱ�� */
	private long lastUpdateDelayAlarmTime = 0L;

	/** �ϴ�ɾ��tps���޸澯ʱ�� */
	private long lastUpdateLowerTpsAlarmTime = 0L;

	/** �ϴ�ɾ���ӳٸ澯ʱ�� */
	private long lastUpdateUpperTpsAlarmTime = 0L;

	/** �ϴ�ɾ���쳣�澯ʱ�� */
	private long lastUpdateExceptionAlarmTime = 0L;

	/** �ϴ�ɾ���ӳٸ澯ʱ�� */
	private long lastInsertDelayAlarmTime = 0L;

	/** �ϴ�ɾ��tps���޸澯ʱ�� */
	private long lastInsertLowerTpsAlarmTime = 0L;

	/** �ϴ�ɾ���ӳٸ澯ʱ�� */
	private long lastInsertUpperTpsAlarmTime = 0L;

	/** �ϴ�ɾ���쳣�澯ʱ�� */
	private long lastInsertExceptionAlarmTime = 0L;

	/** �ϴ�extractor�ӳٸ澯ʱ�� */
	private long lastExtractorDelayAlarmTime = 0L;

	public StatsShesholdChecker(MonitorTaskScheduler monitorTaskScheduler) {
		this.monitorTaskScheduler = monitorTaskScheduler;
		this.configManager = monitorTaskScheduler.getConfigManager();
		this.alertMsgManager = monitorTaskScheduler.getAlertMsgManager();
		this.taskName = monitorTaskScheduler.getMonitorTaskNode().getTaskName();
	}

	public MonitorTaskScheduler getMonitorTaskScheduler() {
		return monitorTaskScheduler;
	}

	/**
	 * ɨ��/jingwei/tasks/**task/**host/status�ڵ����ݣ�������ֵ��ͨ����������
	 * @param taskName
	 * @param hostName
	 */
	public void checkStatsForAlert(MonitorTaskNode monitorTaskNode, String taskName, String hostName) {
		// ���ҵ��澯�����ǹر�״̬���򷵻�
		if (!monitorTaskNode.isThresholdToggle()) {
			if (log.isInfoEnabled()) {
				log.info("[jwm] stats toggle is closed for " + taskName);
			}
			return;
		}

		String statsNodePath = StatsNode.getNodeIdOrPath(taskName, hostName);

		// ZK���ݿ��ܲ�һ�µĵط� ��2��
		String statsData = configManager.getData(statsNodePath);
		if (log.isDebugEnabled()) {
			log.debug("[jwm] get stats data from zk for task: " + taskName + ", host: " + hostName + ", data : "
					+ StringUtil.defaultIfBlank(statsData));
		}

		// ��ZK���ݿ��ܲ�һ�µĵط� ��1��--> ZK���ݿ��ܲ�һ�µĵط� ��2��
		// ֮�䣬�Ȼ�ȡtask�����е�host�ĺ�ҳ��ɾ����һ��host����ʱconfigManager.getData(statsNodePath)����null
		if (StringUtil.isEmpty(statsData)) {
			if (log.isDebugEnabled()) {
				log.debug("[jwm] get stats data from zk for task: " + taskName + ", host: " + hostName
						+ ", data : is empty!");
			}
			return;
		}

		StatsNode statsNode = new StatsNode(taskName, hostName);
		try {
			statsNode.jsonStringToNodeSelf(statsData);
		} catch (JSONException e) {
			log.error("[jingwei monitor] get stats data to jsonobj error!", e);
			return;
		}

		if (log.isDebugEnabled()) {
			log.debug("[jwm] runtime stats is " + statsNode.toString());
		}
		long now = System.currentTimeMillis();

		this.deleteDeley(monitorTaskNode, now, statsNode, hostName);
		this.insertDeley(monitorTaskNode, now, statsNode, hostName);
		this.updateDelay(monitorTaskNode, now, statsNode, hostName);

		this.tpsUpperDelete(monitorTaskNode, now, statsNode, hostName);
		this.tpsUpperInsert(monitorTaskNode, now, statsNode, hostName);
		this.tpsUpperUpdate(monitorTaskNode, now, statsNode, hostName);
		this.tpsLowerDelete(monitorTaskNode, now, statsNode, hostName);
		this.tpsLowerInsert(monitorTaskNode, now, statsNode, hostName);
		this.tpsLowerUpdate(monitorTaskNode, now, statsNode, hostName);

		this.deleteException(monitorTaskNode, now, statsNode, hostName);
		this.insertException(monitorTaskNode, now, statsNode, hostName);
		this.updateException(monitorTaskNode, now, statsNode, hostName);

		this.extractorDelay(monitorTaskNode, now, statsNode, hostName);

	}

	/** ɾ��������ƽ���ӳ� */
	private void deleteDeley(MonitorTaskNode monitorTaskNode, long now, StatsNode statsNode, String hostName) {
		Long deleteDelayThreshold = monitorTaskNode.getDeleteDelayThreshold();
		long currentThresholdFrozenPeriod = monitorTaskNode.getThresholdFrozenPeriod();
		double zkAvgDeleteDelay = statsNode.getLastPeriodAvgDeleteDelay();

		if (deleteDelayThreshold < zkAvgDeleteDelay) {

			String msg = "[[jwm]" + taskName + "," + hostName + " delete avg delay is " + zkAvgDeleteDelay
					+ " ms more than threshold" + deleteDelayThreshold + " ms.";

			if (monitorTaskNode.isToggleDeleteDelayThreshold()) {

				if ((now - this.lastDeleteDelayAlarmTime) > currentThresholdFrozenPeriod) {
					MonitorUtil.wwAndSmsAlert(alertMsgManager, monitorTaskNode, msg);
					this.lastDeleteDelayAlarmTime = now;
				}
			}

			log.warn(msg);
		}
	}

	/** ����insert�¼��ӳٴ�����ֵ��WW֪ͨ	 */
	private void insertDeley(MonitorTaskNode monitorTaskNode, long now, StatsNode statsNode, String hostName) {
		long insertDelayThreshold = monitorTaskNode.getInsertDelayThreshold();
		long currentThresholdFrozenPeriod = monitorTaskNode.getThresholdFrozenPeriod();
		double lastPeriodAvgInsertDelay = statsNode.getLastPeriodAvgInsertDelay();

		if (insertDelayThreshold < lastPeriodAvgInsertDelay) {
			String msg = "[jwm]" + taskName + "," + hostName + " insert avg delay is " + lastPeriodAvgInsertDelay
					+ " ms more than threshold " + insertDelayThreshold + " ms.";

			if (monitorTaskNode.isToggleInsertDelayThreshold()) {
				if ((now - this.lastInsertDelayAlarmTime) > currentThresholdFrozenPeriod) {
					MonitorUtil.wwAndSmsAlert(alertMsgManager, monitorTaskNode, msg);
					this.lastInsertDelayAlarmTime = now;
				}
			}
			log.warn(msg);
		}
	}

	/** ����update�¼��ӳٴ�����ֵ	 */
	private void updateDelay(MonitorTaskNode monitorTaskNode, long now, StatsNode statsNode, String hostName) {
		long updateDelayThreshold = monitorTaskNode.getUpdateDelayThreshold();
		long currentThresholdFrozenPeriod = monitorTaskNode.getThresholdFrozenPeriod();
		double lastPeriodAvgUpdateDelay = statsNode.getLastPeriodAvgUpdateDelay();

		if (updateDelayThreshold < lastPeriodAvgUpdateDelay) {
			String msg = "[jwm]" + taskName + "," + hostName + " update avg delay is " + lastPeriodAvgUpdateDelay
					+ " ms more than threshold " + updateDelayThreshold + " ms.";

			if (monitorTaskNode.isToggleUpdateDelayThreshold()) {
				if ((now - this.lastUpdateDelayAlarmTime) > currentThresholdFrozenPeriod) {
					MonitorUtil.wwAndSmsAlert(alertMsgManager, monitorTaskNode, msg);
					this.lastUpdateDelayAlarmTime = now;
				}
			}
			log.warn(msg);
		}
	}

	/** delete tps ���� 	 */
	private void tpsUpperDelete(MonitorTaskNode monitorTaskNode, long now, StatsNode statsNode, String hostName) {
		long tpsUpperDeleteThreshold = monitorTaskNode.getTpsUpperDeleteThreshold();
		long currentThresholdFrozenPeriod = monitorTaskNode.getThresholdFrozenPeriod();
		double lastPeriodDeleteTps = statsNode.getLastPeriodDeleteTps();

		if (tpsUpperDeleteThreshold < lastPeriodDeleteTps) {
			String msg = "[jwm]" + taskName + "," + hostName + " delete tps is " + lastPeriodDeleteTps
					+ " more than threshold " + tpsUpperDeleteThreshold;

			if (monitorTaskNode.isToggleTpsUpperDeleteThreshold()) {

				if ((now - this.lastDeleteUpperTpsAlarmTime) > currentThresholdFrozenPeriod) {
					MonitorUtil.wwAndSmsAlert(alertMsgManager, monitorTaskNode, msg);
					this.lastDeleteUpperTpsAlarmTime = now;
				}
			}
			log.warn(msg);
		}
	}

	/** ����insert�¼���tps������ֵ��WW֪ͨ*/
	private void tpsUpperInsert(MonitorTaskNode monitorTaskNode, long now, StatsNode statsNode, String hostName) {
		long tpsUpperInsertThreshold = monitorTaskNode.getTpsUpperInsertThreshold();
		long currentThresholdFrozenPeriod = monitorTaskNode.getThresholdFrozenPeriod();
		double lastPeriodInsertTps = statsNode.getLastPeriodInsertTps();

		if (tpsUpperInsertThreshold < lastPeriodInsertTps) {
			String msg = "[jwm]" + taskName + "," + hostName + " insert tps is " + lastPeriodInsertTps
					+ " more than threshold " + tpsUpperInsertThreshold;

			if (monitorTaskNode.isToggleTpsUpperInsertThreshold()) {
				if ((now - this.lastInsertUpperTpsAlarmTime) > currentThresholdFrozenPeriod) {
					MonitorUtil.wwAndSmsAlert(alertMsgManager, monitorTaskNode, msg);
					this.lastInsertUpperTpsAlarmTime = now;
				}
			}
			log.warn(msg);
		}
	}

	/** ����update�¼���tps������ֵ��WW֪ͨ */
	private void tpsUpperUpdate(MonitorTaskNode monitorTaskNode, long now, StatsNode statsNode, String hostName) {
		long tpsUpperUpdateThreshold = monitorTaskNode.getTpsUpperUpdateThreshold();
		long currentThresholdFrozenPeriod = monitorTaskNode.getThresholdFrozenPeriod();
		double lastPeriodUpdateTps = statsNode.getLastPeriodUpdateTps();
		if (tpsUpperUpdateThreshold < lastPeriodUpdateTps) {

			String msg = "[jwm]" + taskName + "," + hostName + " update tps is " + lastPeriodUpdateTps
					+ " more than threshold " + tpsUpperUpdateThreshold;

			if (monitorTaskNode.isToggleTpsUpperUpdateThreshold()) {
				if ((now - this.lastUpdateUpperTpsAlarmTime) > currentThresholdFrozenPeriod) {
					MonitorUtil.wwAndSmsAlert(alertMsgManager, monitorTaskNode, msg);
					this.lastUpdateUpperTpsAlarmTime = now;
				}
			}
			log.warn(msg);
		}
	}

	/** ����delete�¼���tpsС����ֵ��WW֪ͨ */
	private void tpsLowerDelete(MonitorTaskNode monitorTaskNode, long now, StatsNode statsNode, String hostName) {
		long tpsLowerDeleteThreshold = monitorTaskNode.getTpsLowerDeleteThreshold();
		long currentThresholdFrozenPeriod = monitorTaskNode.getThresholdFrozenPeriod();
		double lastPeriodDeleteTps = statsNode.getLastPeriodDeleteTps();

		if (tpsLowerDeleteThreshold > lastPeriodDeleteTps) {
			String msg = "[jwm]" + taskName + "," + hostName + " delete tps is " + lastPeriodDeleteTps
					+ " less than threshold " + tpsLowerDeleteThreshold;

			if (monitorTaskNode.isToggleTpsLowerDeleteThreshold()) {
				if ((now - this.lastDeleteLowerTpsAlarmTime) > currentThresholdFrozenPeriod) {
					MonitorUtil.wwAndSmsAlert(alertMsgManager, monitorTaskNode, msg);
					this.lastDeleteLowerTpsAlarmTime = now;
				}
			}
			log.warn(msg);
		}
	}

	/** ����insert�¼���tpsС����ֵ��WW֪ͨ */
	private void tpsLowerInsert(MonitorTaskNode monitorTaskNode, long now, StatsNode statsNode, String hostName) {
		long tpsLowerInsertThreshold = monitorTaskNode.getTpsLowerInsertThreshold();
		long currentThresholdFrozenPeriod = monitorTaskNode.getThresholdFrozenPeriod();
		double lastPeriodInsertTps = statsNode.getLastPeriodInsertTps();
		if (tpsLowerInsertThreshold > lastPeriodInsertTps) {
			String msg = "[jwm]" + taskName + "," + hostName + " insert tps is " + lastPeriodInsertTps
					+ " less than threshold " + tpsLowerInsertThreshold;

			if (monitorTaskNode.isToggleTpsLowerInsertThreshold()) {

				if ((now - this.lastInsertLowerTpsAlarmTime) > currentThresholdFrozenPeriod) {
					MonitorUtil.wwAndSmsAlert(alertMsgManager, monitorTaskNode, msg);
					this.lastInsertLowerTpsAlarmTime = now;
				}
			}

			log.warn(msg);
		}
	}

	/** ����update�¼���tpsС����ֵ��WW֪ͨ */
	private void tpsLowerUpdate(MonitorTaskNode monitorTaskNode, long now, StatsNode statsNode, String hostName) {
		long tpsLowerUpdateThreshold = monitorTaskNode.getTpsLowerUpdateThreshold();
		long currentThresholdFrozenPeriod = monitorTaskNode.getThresholdFrozenPeriod();
		double lastPeriodUpdateTps = statsNode.getLastPeriodUpdateTps();

		if (tpsLowerUpdateThreshold > lastPeriodUpdateTps) {
			String msg = "[jwm]" + taskName + "," + hostName + " update tps is " + lastPeriodUpdateTps
					+ " less than threshold " + tpsLowerUpdateThreshold;

			if (monitorTaskNode.isToggleTpsLowerUpdateThreshold()) {
				if ((now - this.lastUpdateLowerTpsAlarmTime) > currentThresholdFrozenPeriod) {
					MonitorUtil.wwAndSmsAlert(alertMsgManager, monitorTaskNode, msg);
					this.lastUpdateLowerTpsAlarmTime = now;
				}
			}
			log.warn(msg);
		}
	}

	/** ����delete�¼����쳣������ֵ��WW֪ͨ */
	private void deleteException(MonitorTaskNode monitorTaskNode, long now, StatsNode statsNode, String hostName) {
		long deleteExceptionThreshold = monitorTaskNode.getDeleteExceptionThreshold();
		long currentThresholdFrozenPeriod = monitorTaskNode.getThresholdFrozenPeriod();
		double lastPeriodDeleteExceptionCount = statsNode.getLastPeriodDeleteExceptionCount();

		if (deleteExceptionThreshold < lastPeriodDeleteExceptionCount) {
			String msg = "[jwm]" + taskName + "," + hostName + " delete exception avg is "
					+ lastPeriodDeleteExceptionCount + " more than threshold " + deleteExceptionThreshold;

			if (monitorTaskNode.isToggleDeleteExceptionThreshold()) {
				if ((now - this.lastDeleteExceptionAlarmTime) > currentThresholdFrozenPeriod) {
					MonitorUtil.wwAndSmsAlert(alertMsgManager, monitorTaskNode, msg);
					this.lastDeleteExceptionAlarmTime = now;
				}
			}
			log.warn(msg);
		}
	}

	/** ����insert�¼����쳣������ֵ��WW֪ͨ */
	private void insertException(MonitorTaskNode monitorTaskNode, long now, StatsNode statsNode, String hostName) {
		long insertExceptionThreshold = monitorTaskNode.getInsertExceptionThreshold();
		long currentThresholdFrozenPeriod = monitorTaskNode.getThresholdFrozenPeriod();
		double lastPeriodInsertExceptionCount = statsNode.getLastPeriodInsertExceptionCount();

		if (insertExceptionThreshold < lastPeriodInsertExceptionCount) {
			String msg = "[jwm]" + taskName + "," + hostName + " insert exception avg is "
					+ lastPeriodInsertExceptionCount + " more than threshold " + insertExceptionThreshold;

			if (monitorTaskNode.isToggleInsertExceptionThreshold()) {
				if ((now - this.lastInsertExceptionAlarmTime) > currentThresholdFrozenPeriod) {
					MonitorUtil.wwAndSmsAlert(alertMsgManager, monitorTaskNode, msg);
					this.lastInsertExceptionAlarmTime = now;
				}
			}
			log.warn(msg);
		}
	}

	/** ����update�¼����쳣������ֵ��WW֪ͨ */
	private void updateException(MonitorTaskNode monitorTaskNode, long now, StatsNode statsNode, String hostName) {
		long updateExceptionThreshold = monitorTaskNode.getUpdateExceptionThreshold();
		long currentThresholdFrozenPeriod = monitorTaskNode.getThresholdFrozenPeriod();
		double lastPeriodUpdateExceptionCount = statsNode.getLastPeriodUpdateExceptionCount();

		if (updateExceptionThreshold < lastPeriodUpdateExceptionCount) {
			String msg = "[jwm]" + taskName + "," + hostName + " update exception avg:"
					+ lastPeriodUpdateExceptionCount + " more than threshold " + updateExceptionThreshold;

			if (monitorTaskNode.isToggleUpdateExceptionThreshold()) {
				if ((now - this.lastUpdateExceptionAlarmTime) > currentThresholdFrozenPeriod) {
					MonitorUtil.wwAndSmsAlert(alertMsgManager, monitorTaskNode, msg);
					this.lastUpdateExceptionAlarmTime = now;
				}
			}
			log.warn(msg);
		}
	}

	/** ����update�¼����쳣������ֵ��WW֪ͨ */
	private void extractorDelay(MonitorTaskNode monitorTaskNode, long now, StatsNode statsNode, String hostName) {
		long extractorDelayThreshold = monitorTaskNode.getExtractorDelayThreshold();
		long currentThresholdFrozenPeriod = monitorTaskNode.getThresholdFrozenPeriod();
		double lastPeriodAvgExtractorDelay = statsNode.getLastPeriodAvgExtractorDelay();
		if (extractorDelayThreshold < lastPeriodAvgExtractorDelay) {

			String msg = "[jwm]" + taskName + "," + hostName + " extractor avg delay: " + lastPeriodAvgExtractorDelay
					+ " ms more than threshold " + extractorDelayThreshold + " ms.";

			if (monitorTaskNode.isToggleExtractorDelayThreshold()) {
				if ((now - this.lastExtractorDelayAlarmTime) > currentThresholdFrozenPeriod) {
					MonitorUtil.wwAndSmsAlert(alertMsgManager, monitorTaskNode, msg);
					this.lastExtractorDelayAlarmTime = now;
				}
			}
			log.warn(msg);
		}
	}
}