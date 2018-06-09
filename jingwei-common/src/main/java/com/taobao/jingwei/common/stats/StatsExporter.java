package com.taobao.jingwei.common.stats;

import com.taobao.jingwei.common.JingWeiUtil;
import com.taobao.jingwei.common.log.JingweiLogConfig;
import com.taobao.jingwei.common.log.LogType;
import com.taobao.jingwei.common.node.StatsNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;

/**
 * @desc ��ZK����־�ļ�дͳ������
 * @author <a href="mailto:shuohai.lhl@taobao.com">˷�� shuohailhl</a>
 * @date 2011-12-12����5:29:23
 */
public class StatsExporter extends Thread {
	// ������־��key
	private static final String DELETE_DELAY_LADDER_KEY = "DELETE_DELAY_LADDER_KEY";

	private static final String INSERT_DELAY_LADDER_KEY = "INSERT_DELAY_LADDER_KEY";

	private static final String UPDATE_DELAY_LADDER_KEY = "UPDATE_DELAY_LADDER_KEY";

	private static final String EXTRACTOR_DELAY_LADDER_KEY = "EXTRACTOR_DELAY_LADDER_KEY";

	private Log log = LogFactory.getLog(this.getClass());

	private static final String STATS_LOG_ROOT_PATH = System
			.getProperty("user.home", JingweiLogConfig.DEFAULT_LOG_PATH);
	//home/admin/logs/jingwei
	private static final String STATS_LOG_SUB_PATH = "logs";

	/** ͳ��������־�ļ� */
	private final Log statsLog;

	/** ��־�ļ��ָ���*/
	private static final String STATS_LOG_SEPERATOR = "#@#";

	/** stats��־��ʱ���ʽ */
	private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/** ������ */
	private final Calendar ca = Calendar.getInstance();

	/** ͳ�Ƽ�����  */
	private StatsUnit statsUnit;

	/** ͳ�Ƶ�Ԫ*/
	private StatsNode statsNode;

	public StatsExporter(StatsUnit statsUnit) {
		this.statsUnit = statsUnit;
		this.statsNode = new StatsNode(statsUnit.getTaskName(), statsUnit.getHostName());
		this.statsLog = this.configStatsLog();
	}

	@Override
	public void run() {
		while (this.statsUnit.isStart()) {
			try {
				Thread.sleep(this.statsUnit.getStatsPeriod());
			} catch (InterruptedException e) {
				log.error("[jingwei stats] sleep status period interrupt error! ", e);
				break;
			}
			this.fillStatsNode();
			this.publishStatsData();
		}
	}

	/***
	 * ��ͳ��������䵽ͳ�ƽڵ�
	 */
	private void fillStatsNode() {
		// ͳ�������ڵļ���
		long oldDeleteCount = this.statsUnit.getPeriodDeleteCounter().get();
		long oldInsertCount = this.statsUnit.getPeriodInsertCounter().get();
		long oldUpdateCount = this.statsUnit.getPeriodUpdateCounter().get();
		long oldEtractorCount = this.statsUnit.getPeriodExtractorCounter().get();

		long oldDeleteExceptionCount = this.statsUnit.getPeriodDeleteExceptionCounter().get();
		long oldInsertExceptionCount = this.statsUnit.getPeriodInsertExceptionCounter().get();
		long oldUpdateExceptionCount = this.statsUnit.getPeriodUpdateExceptionCounter().get();

		long oldDeleteDelay = this.statsUnit.getPeriodDeleteDelay().get();
		long oldInsertDelay = this.statsUnit.getPeriodInsertDelay().get();
		long oldUpdateDelay = this.statsUnit.getPeriodUpdateDelay().get();
		long oldExtractorDelay = this.statsUnit.getPeriodExtractorDelay().get();

		this.statsNode.setLastPeriodDeleteCount(oldDeleteCount);
		this.statsNode.setTodayDeleteCount(this.statsUnit.getTodayDeleteCounter().get());

		this.statsNode.setLastPeriodInsertCount(oldInsertCount);
		this.statsNode.setTodayInsertCount(this.statsUnit.getTodayInsertCounter().get());

		this.statsNode.setLastPeriodUpdateCount(oldUpdateCount);
		this.statsNode.setTodayUpdateCount(this.statsUnit.getTodayUpdateCounter().get());

		// ͳ��������TPS
		double deleteTps = BigDecimal.valueOf(oldDeleteCount)
				.divide(BigDecimal.valueOf((double) this.statsUnit.getStatsPeriod() / 1000), 2, RoundingMode.HALF_UP)
				.doubleValue();
		this.statsNode.setLastPeriodDeleteTps(deleteTps);
		double insertTps = BigDecimal.valueOf(oldInsertCount)
				.divide(BigDecimal.valueOf((double) this.statsUnit.getStatsPeriod() / 1000), 2, RoundingMode.HALF_UP)
				.doubleValue();
		this.statsNode.setLastPeriodInsertTps(insertTps);
		double updateTps = BigDecimal.valueOf(oldUpdateCount)
				.divide(BigDecimal.valueOf((double) this.statsUnit.getStatsPeriod() / 1000), 2, RoundingMode.HALF_UP)
				.doubleValue();
		this.statsNode.setLastPeriodUpdateTps(updateTps);

		// ͳ��������ƽ���ӳ�ʱ��
		double avgDeleteDelay = 0.00D;
		if (oldDeleteCount != 0) {
			avgDeleteDelay = BigDecimal.valueOf(oldDeleteDelay)
					.divide(BigDecimal.valueOf(oldDeleteCount), 2, RoundingMode.HALF_UP).doubleValue();
		}
		this.statsNode.setLastPeriodAvgDeleteDelay(avgDeleteDelay);

		double avgInsertDelay = 0.00D;
		if (oldInsertCount != 0) {
			avgInsertDelay = BigDecimal.valueOf(oldInsertDelay)
					.divide(BigDecimal.valueOf(oldInsertCount), 2, RoundingMode.HALF_UP).doubleValue();
		}
		this.statsNode.setLastPeriodAvgInsertDelay(avgInsertDelay);

		double avgUpdateDelay = 0.00D;
		if (oldUpdateCount != 0) {
			avgUpdateDelay = BigDecimal.valueOf(oldUpdateDelay)
					.divide(BigDecimal.valueOf(oldUpdateCount), 2, RoundingMode.HALF_UP).doubleValue();
		}
		this.statsNode.setLastPeriodAvgUpdateDelay(avgUpdateDelay);

		double avgExtractorDelay = 0.00D;

		if (oldEtractorCount != 0) {
			avgExtractorDelay = BigDecimal.valueOf(oldExtractorDelay)
					.divide(BigDecimal.valueOf(oldEtractorCount), 2, RoundingMode.HALF_UP).doubleValue();
		}
		this.statsNode.setLastPeriodAvgExtractorDelay(avgExtractorDelay);

		// ͳ���������쳣
		this.statsNode.setLastPeriodDeleteExceptionCount(oldDeleteExceptionCount);
		this.statsNode.setTodayDeleteExceptionCount(this.statsUnit.getTodayDeleteExceptionCounter().get());

		this.statsNode.setLastPeriodInsertExceptionCount(oldInsertExceptionCount);
		this.statsNode.setTodayInsertExceptionCount(this.statsUnit.getTodayInsertExceptionCounter().get());

		this.statsNode.setLastPeriodUpdateExceptionCount(oldUpdateExceptionCount);
		this.statsNode.setTodayUpdateExceptionCount(this.statsUnit.getTodayUpdateExceptionCounter().get());

		this.statsUnit.getPeriodDeleteCounter().getAndAdd(-oldDeleteCount);
		this.statsUnit.getPeriodInsertCounter().getAndAdd(-oldInsertCount);
		this.statsUnit.getPeriodUpdateCounter().getAndAdd(-oldUpdateCount);
		this.statsUnit.getPeriodExtractorCounter().getAndAdd(-oldEtractorCount);

		this.statsUnit.getPeriodDeleteExceptionCounter().getAndAdd(-oldDeleteExceptionCount);
		this.statsUnit.getPeriodInsertExceptionCounter().getAndAdd(-oldInsertExceptionCount);
		this.statsUnit.getPeriodUpdateExceptionCounter().getAndAdd(-oldUpdateExceptionCount);

		this.statsUnit.getPeriodDeleteDelay().getAndAdd(-oldDeleteDelay);
		this.statsUnit.getPeriodInsertDelay().getAndAdd(-oldInsertDelay);
		this.statsUnit.getPeriodUpdateDelay().getAndAdd(-oldUpdateDelay);
		this.statsUnit.getPeriodExtractorDelay().getAndAdd(-oldExtractorDelay);

		// ͳ��ʱ���
		statsNode.setStatsTime(System.currentTimeMillis());

		// ���� ��ͳ������
		statsNode.setTxCount(this.statsUnit.getTxStats().getTxCount());
		statsNode.setTxTps(this.statsUnit.getTxStats().getTxTps());
		statsNode.setTxMillisMinLatency(this.statsUnit.getTxStats().getMillisMinLatency());
		statsNode.setTxMillisMaxLatency(this.statsUnit.getTxStats().getMillisMaxLatency());
		statsNode.setTxMillisAvgLatency(this.statsUnit.getTxStats().getMillisAvgLatency());
	}

	/**
	 * ��zk�ͱ�����־дͳ������
	 */
	private void publishStatsData() {
		String dataIdOrPath = statsNode.getDataIdOrNodePath();
		try {
			JSONObject jsonobj = new JSONObject(statsNode.toJSONString());
			// ��ͳ������д���ļ�
			logStatsDataToFile(jsonobj);

		} catch (JSONException e) {
			log.error("[jingwei stats] get stats json oject attribute error!", e);
		}

		// ��ͳ�����ݷ�����ZK
		try {
			this.statsUnit.getConfigManager().publishOrUpdateData(dataIdOrPath, statsNode.toJSONString(),
					statsNode.isPersistent());
		} catch (JSONException e) {
			log.error("[jingwei stats] get stats json oject attribute error!", e);
		} catch (Exception e) {
			log.error("[jingwei stats] can't write to zk, path is : " + dataIdOrPath, e);
		}

		// ���ӳٽ���ֵд����־�ļ�
		this.logDelayDataToFile();
	}

	/**
	 * @param jsonObject
	 * @see {@link com.taobao.jingwei.common.node.StatsNode#STATUS_SUFFIX}

	 */
	private void logStatsDataToFile(JSONObject jsonObject) {
		@SuppressWarnings("unchecked")
		Iterator<String> keysIt = jsonObject.keys();
		while (keysIt.hasNext()) {
			String statsKey = keysIt.next();

			if (statsKey.endsWith(StatsNode.STATUS_SUFFIX)) {
				if (statsLog.isWarnEnabled()) {
					StringBuilder sb = new StringBuilder();
					sb.append(this.statsNode.getTaskName());
					sb.append(STATS_LOG_SEPERATOR);
					sb.append(this.statsNode.getHostName());
					sb.append(STATS_LOG_SEPERATOR);
					sb.append(statsKey);
					sb.append(STATS_LOG_SEPERATOR);
					try {
						sb.append(jsonObject.get(statsKey).toString());
					} catch (JSONException e) {
						log.error("[jingwei stats] get stats json oject attribute error!", e);
						continue;
					}
					sb.append(STATS_LOG_SEPERATOR);
					ca.setTimeInMillis(System.currentTimeMillis());
					sb.append(format.format(ca.getTime()));

					statsLog.warn(sb.toString());
				}
			}
		}
	}

	private void logDelayDataToFile() {
		this.logDelayDataToFile(DELETE_DELAY_LADDER_KEY, this.statsUnit.getDeleteDelayScalar());
		this.logDelayDataToFile(INSERT_DELAY_LADDER_KEY, this.statsUnit.getInsertDelayScalar());
		this.logDelayDataToFile(UPDATE_DELAY_LADDER_KEY, this.statsUnit.getUpdateDelayScalar());
		this.logDelayDataToFile(EXTRACTOR_DELAY_LADDER_KEY, this.statsUnit.getExtractorDelayScalar());
	}

	/**
	 *  ���ӳٽ���ֵд����־�ļ�
	 */
	private void logDelayDataToFile(String key, CounterScalar scalar) {

		Map<Integer, Integer> data = scalar.getAndReset();

		for (Map.Entry<Integer, Integer> entry : data.entrySet()) {

			String statsKey = scalar.getKeyString(entry.getKey());
			int value = entry.getValue();

			StringBuilder sb = new StringBuilder();
			sb.append(this.statsNode.getTaskName());
			sb.append(STATS_LOG_SEPERATOR).append(this.statsNode.getHostName());
			sb.append(STATS_LOG_SEPERATOR).append(key);
			sb.append(STATS_LOG_SEPERATOR).append(statsKey);
			sb.append(STATS_LOG_SEPERATOR).append(value);
			sb.append(STATS_LOG_SEPERATOR);
			ca.setTimeInMillis(System.currentTimeMillis());
			sb.append(format.format(ca.getTime()));

			statsLog.warn(sb.toString());
		}
	}

	/**
	 * ͳ����־λ��
	 * @return
	 */
	private String getStatsLogFilePath() {
		StringBuilder sb = new StringBuilder();
		sb.append(STATS_LOG_ROOT_PATH);
		sb.append(JingWeiUtil.FILE_SEP).append(STATS_LOG_SUB_PATH);
		return sb.toString();
	}

	/**
	* ����ͳ����־
	*/
	public Log configStatsLog() {
		JingweiLogConfig logConfig = new JingweiLogConfig();
		logConfig.setLogPath(this.getStatsLogFilePath());
		logConfig.setMaxLogFileSize("200MB");
		com.taobao.jingwei.common.log.LogFactory.setStatsLogConfig(logConfig);
		return com.taobao.jingwei.common.log.LogFactory.getLog(LogType.STATS, this.statsUnit.getTaskName());
	}
}