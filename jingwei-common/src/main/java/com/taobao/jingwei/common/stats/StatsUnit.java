package com.taobao.jingwei.common.stats;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.JingWeiUtil;
import com.taobao.jingwei.common.config.ConfigDataListener;
import com.taobao.jingwei.common.config.ConfigManager;
import com.taobao.jingwei.common.log.JingweiLogConfig;
import com.taobao.jingwei.common.node.StatsNode;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 1 ����/jingwei/tasks/**task�ڵ��statsPeriod�仯,����ͳ������
 * 
 * <p/>
 * 2 �����԰�ͳ������д��zk�ͱ�����־�ļ�
 * 
 * @author shuohailhl
 */
public class StatsUnit extends ConfigDataListener implements JingWeiConstants {

	private Log log = LogFactory.getLog(this.getClass());

	/** ���뵥λ�ķ�Χ */
	private static final int DEFAULT_RANGE = 200;

	/** ���뵥λ�Ŀ̶� */
	private static final int DEFAULT_SCALE = 5;

	/** ͳ�����ڣ�׼ʵʱ������ǰ������״��������Ϊ��λ */
	private volatile long statsPeriod = JingWeiConstants.DEFAULT_STATS_PERIOD;

	/** ��ʶͳ�ƶ��������� */
	private String taskName;

	/** ��ʶͳ�ƶ��������� */
	private String hostName;

	/** ͳ�Ƶ�Ԫ */
	private StatsNode statsNode;

	/** ��ZKдͳ�����ݣ���ZK��ȡ������Ϣ */
	private ConfigManager configManager;

	/** ��־������Ϣ */
	private JingweiLogConfig logConfig;

	/** ��ʱ��,��ÿ����ʱ�ѵ����ͳ���������� */
	private ScheduledExecutorService dayScheduler = new ScheduledThreadPoolExecutor(
			1);

	/** ���ڴ���extractor�¼������� */
	private AtomicLong periodExtractorCounter = new AtomicLong(0L);

	/** ���ڴ���delete�¼������� */
	private AtomicLong periodDeleteCounter = new AtomicLong(0L);

	/** ���ڴ���insert�¼������� */
	private AtomicLong periodInsertCounter = new AtomicLong(0L);

	/** ���ڴ���update�¼����� */
	private AtomicLong periodUpdateCounter = new AtomicLong(0L);

	/** ���մ����delete�¼������� */
	private AtomicLong todayDeleteCounter = new AtomicLong(0L);

	/** ���մ����insert�¼������� */
	private AtomicLong todayInsertCounter = new AtomicLong(0L);

	/** ���մ����update�¼������� */
	private AtomicLong todayUpdateCounter = new AtomicLong(0L);

	/** ���ڴ���delete�¼��ӳ�ʱ�� */
	private AtomicLong periodDeleteDelay = new AtomicLong(0L);

	/** ���ڴ���insert�¼��ӳ�ʱ�� */
	private AtomicLong periodInsertDelay = new AtomicLong(0L);

	/** ���ڴ���update�¼��ӳ�ʱ�� */
	private AtomicLong periodUpdateDelay = new AtomicLong(0L);

	/** ���ڴ���extrator�¼��ӳ�ʱ�� */
	private AtomicLong periodExtractorDelay = new AtomicLong(0L);

	/** ���ڴ���ɾ���������쳣���� */
	private AtomicLong periodDeleteExceptionCounter = new AtomicLong(0L);

	/** ���ڴ������������쳣���� */
	private AtomicLong periodInsertExceptionCounter = new AtomicLong(0L);

	/** ���ڴ�����²������쳣���� */
	private AtomicLong periodUpdateExceptionCounter = new AtomicLong(0L);

	/** ���մ���delete�����쳣�ļ����� */
	private AtomicLong todayDeleteExceptionCounter = new AtomicLong(0L);

	/** ���մ���insert�����쳣�ļ����� */
	private AtomicLong todayInsertExceptionCounter = new AtomicLong(0L);

	/** ���մ���update�����쳣�ļ����� */
	private AtomicLong todayUpdateExceptionCounter = new AtomicLong(0L);

	/** extractor��������� */
	private final TransactionStats txStats = new TransactionStats();

	/** toggle �رմ���־ */
	private volatile Boolean start = true;

	/** ͳ����������� */
	private StatsExporter statsExportor;

	/** ������� */
	private DayStatsReseter dayStatsReseter;

	/** �ӳ�ʱ�䷶Χ���� */
	private final CounterScalar insertDelayScalar = new CounterScalar(
			DEFAULT_SCALE, DEFAULT_RANGE);

	private final CounterScalar deleteDelayScalar = new CounterScalar(
			DEFAULT_SCALE, DEFAULT_RANGE);

	private final CounterScalar updateDelayScalar = new CounterScalar(
			DEFAULT_SCALE, DEFAULT_RANGE);

	private final CounterScalar extractorDelayScalar = new CounterScalar(
			DEFAULT_SCALE, DEFAULT_RANGE);

	public StatsUnit(String taskName, ConfigManager configManager) {
		this.taskName = taskName;
		this.configManager = configManager;
		this.dayStatsReseter = new DayStatsReseter();
	}

	public void init() throws IllegalArgumentException {
		if (StringUtil.isBlank(hostName)) {
			throw new IllegalArgumentException(
					"hostName should not be null or empty!");
		}

		// ͳ�ƽڵ�
		this.statsNode = new StatsNode(taskName, hostName);

		// ����У��
		if (statsPeriod == 0) {
			this.statsPeriod = JingWeiConstants.DEFAULT_STATS_PERIOD;
		}
		// ������ʱÿ����¼���������
		long initialDelay = DAY_TIME_SECONDS - JingWeiUtil.getSecondOfDate();
		this.dayScheduler.scheduleAtFixedRate(this.dayStatsReseter,
				initialDelay, DAY_TIME_SECONDS, TimeUnit.SECONDS);

		this.statsExportor = new StatsExporter(this);

		// ��ʼ���������ֵ
		this.intDayCounterFromZk();

		// ������ʱ��zk����־дͳ�����ݵ��߳�
		this.statsExportor.start();
	}

	/**
	 * ͣ����ʱ�������޶�ʱ��־���
	 */
	public void destroy() {
		this.start = false;
		this.dayScheduler.shutdown();
	}

	@Override
	public void handleData(String dataIdOrPath, String data) {
		if (StringUtil.isBlank(data)) {
			return;
		}
		// ��task�ڵ��л�ȡ���µ�statsPeriod,���±��ػ�������
		try {
			this.statsPeriod = SyncTaskNode.getStatsPeriodFromJsonObject(data);
		} catch (JSONException e) {
			this.statsPeriod = JingWeiConstants.DEFAULT_STATS_PERIOD;
			this.log.error(" task node get stats period from json data error!");
		}
	}

	/**
	 * toggle ֹͣ��־
	 */
	public void disable() {
		this.start = false;
	}

	/**
	 * �ж��Ƿ�����״̬
	 * 
	 * @return true ��ʾ����־��false��ʾ������־
	 */
	public boolean isStart() {
		return this.start;
	}

	/**
	 * client����delete�¼�������
	 */
	public void incrementDeleteCount() {
		this.periodDeleteCounter.getAndIncrement();
		this.todayDeleteCounter.getAndIncrement();
	}

	/**
	 * client����insert�¼�������
	 */
	public void incrementInsertCount() {
		this.periodInsertCounter.getAndIncrement();
		this.todayInsertCounter.getAndIncrement();
	}

	/**
	 * client����update�¼�������
	 */
	public void incrementUpdateCount() {
		this.periodUpdateCounter.getAndIncrement();
		this.todayUpdateCounter.getAndIncrement();
	}

	/**
	 * client����delete�¼�������
	 * 
	 * @param delta
	 *            �¼�����
	 */
	public void addDeleteCount(int delta) {
		this.periodDeleteCounter.getAndAdd(delta);
		this.todayDeleteCounter.getAndAdd(delta);
	}

	/**
	 * client����insert������
	 * 
	 * @param delta
	 *            �¼�����
	 */
	public void addInsertCount(int delta) {
		this.periodInsertCounter.getAndAdd(delta);
		this.todayInsertCounter.getAndAdd(delta);
	}

	/**
	 * client����update������
	 * 
	 * @param delta
	 *            �¼�����
	 */
	public void addUpdateCount(int delta) {
		this.periodUpdateCounter.getAndAdd(delta);
		this.todayUpdateCounter.getAndAdd(delta);
	}

	/**
	 * client����delete�ӳ�
	 * 
	 * @param delay
	 *            ����ʱ��
	 */
	public void addDeleteDelay(long delay) {
		this.periodDeleteDelay.getAndAdd(delay);
		this.deleteDelayScalar.put((int) delay);
	}

	/**
	 * client����insert�ӳ�
	 * 
	 * @param delay
	 *            ����ʱ��
	 */
	public void addInsertDelay(long delay) {
		this.periodInsertDelay.getAndAdd(delay);
		this.insertDelayScalar.put((int) delay);
	}

	/**
	 * client����update�ӳ�
	 * 
	 * @param delay
	 *            ����ʱ��
	 */
	public void addUpdateDelay(long delay) {
		this.periodUpdateDelay.getAndAdd(delay);
		this.updateDelayScalar.put((int) delay);
	}

	/**
	 * extractor���ӳ�
	 * 
	 * @param delay
	 */
	public void addExtractorDelay(long delay) {
		this.periodExtractorDelay.getAndAdd(delay);
		this.periodExtractorCounter.getAndIncrement();
		this.extractorDelayScalar.put((int) delay);
	}

	/**
	 * client����delete�����쳣ʱ����
	 */
	public void incrementDeleteExceptionCount() {
		this.periodDeleteExceptionCounter.getAndIncrement();
		this.todayDeleteExceptionCounter.getAndIncrement();
	}

	/**
	 * client����insert�����쳣ʱ����
	 */
	public void incrementInsertExceptionCount() {
		this.periodInsertExceptionCounter.getAndIncrement();
		this.todayInsertExceptionCounter.getAndIncrement();
	}

	/**
	 * client����update�����쳣ʱ����
	 */
	public void incrementUpdateExceptionCount() {
		this.periodUpdateExceptionCounter.getAndIncrement();
		this.todayUpdateExceptionCounter.getAndIncrement();
	}

	/**
	 * ÿ����ʱˢ�µ���ļ�����
	 * 
	 * @author shuohailhl
	 * 
	 */
	private class DayStatsReseter implements Runnable {
		@Override
		public void run() {
			resetDayCounter();
		}

		private void resetDayCounter() {
			StatsUnit.this.todayDeleteCounter.set(0L);
			StatsUnit.this.todayInsertCounter.set(0L);
			StatsUnit.this.todayUpdateCounter.set(0L);

			StatsUnit.this.todayDeleteExceptionCounter.set(0L);
			StatsUnit.this.todayInsertExceptionCounter.set(0L);
			StatsUnit.this.todayUpdateExceptionCounter.set(0L);
		}
	}

	/**
	 * ��ʼ�����ռ�����
	 */
	private void intDayCounterFromZk() {
		String path = this.statsNode.getDataIdOrNodePath();
		String data = this.configManager.getData(path);

		if (StringUtil.isEmpty(data)) {
			return;
		}
		try {
			this.statsNode.jsonStringToNodeSelf(data);
		} catch (JSONException e) {
			log.error("[jingwei-core] stats get data from json string error, data is : " // NL
					+ data
					+ ", node is : "
					+ this.statsNode.getDataIdOrNodePath());
		}

		long daySecondDif = (System.currentTimeMillis() - this.statsNode // NL
				.getStatsTime()) / 1000;

		// �������ͬһ��ģ������zk�ϱ�������
		if (daySecondDif > 0 && daySecondDif < JingWeiUtil.getSecondOfDate()) { // NL
			this.todayDeleteCounter.addAndGet(this.statsNode
					.getTodayDeleteCount());
			this.todayDeleteExceptionCounter.addAndGet(this.statsNode
					.getTodayDeleteExceptionCount());

			this.todayInsertCounter.addAndGet(this.statsNode
					.getTodayInsertCount());
			this.todayInsertExceptionCounter.addAndGet(this.statsNode // NL
					.getTodayInsertExceptionCount());

			this.todayUpdateCounter.addAndGet(this.statsNode
					.getTodayUpdateCount());
			this.todayUpdateExceptionCounter.addAndGet(this.statsNode // NL
					.getTodayUpdateExceptionCount());
		}

	}

	public long getStatsPeriod() {
		return statsPeriod;
	}

	public void setStatsPeriod(long statsPeriod) {
		this.statsPeriod = statsPeriod;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public StatsNode getStatsNode() {
		return statsNode;
	}

	public void setStatsNode(StatsNode statsNode) {
		this.statsNode = statsNode;
	}

	public TransactionStats getTxStats() {
		return txStats;
	}

	public ConfigManager getConfigManager() {
		return configManager;
	}

	public AtomicLong getPeriodExtractorCounter() {
		return periodExtractorCounter;
	}

	public AtomicLong getPeriodDeleteCounter() {
		return periodDeleteCounter;
	}

	public AtomicLong getPeriodInsertCounter() {
		return periodInsertCounter;
	}

	public AtomicLong getPeriodUpdateCounter() {
		return periodUpdateCounter;
	}

	public AtomicLong getTodayDeleteCounter() {
		return todayDeleteCounter;
	}

	public AtomicLong getTodayInsertCounter() {
		return todayInsertCounter;
	}

	public AtomicLong getTodayUpdateCounter() {
		return todayUpdateCounter;
	}

	public AtomicLong getPeriodDeleteDelay() {
		return periodDeleteDelay;
	}

	public AtomicLong getPeriodInsertDelay() {
		return periodInsertDelay;
	}

	public AtomicLong getPeriodUpdateDelay() {
		return periodUpdateDelay;
	}

	public AtomicLong getPeriodExtractorDelay() {
		return periodExtractorDelay;
	}

	public AtomicLong getPeriodDeleteExceptionCounter() {
		return periodDeleteExceptionCounter;
	}

	public AtomicLong getPeriodInsertExceptionCounter() {
		return periodInsertExceptionCounter;
	}

	public AtomicLong getPeriodUpdateExceptionCounter() {
		return periodUpdateExceptionCounter;
	}

	public AtomicLong getTodayDeleteExceptionCounter() {
		return todayDeleteExceptionCounter;
	}

	public AtomicLong getTodayInsertExceptionCounter() {
		return todayInsertExceptionCounter;
	}

	public AtomicLong getTodayUpdateExceptionCounter() {
		return todayUpdateExceptionCounter;
	}

	public JingweiLogConfig getLogConfig() {
		return logConfig;
	}

	public void setLogConfig(JingweiLogConfig logConfig) {
		this.logConfig = logConfig;
	}

	public CounterScalar getInsertDelayScalar() {
		return insertDelayScalar;
	}

	public CounterScalar getDeleteDelayScalar() {
		return deleteDelayScalar;
	}

	public CounterScalar getUpdateDelayScalar() {
		return updateDelayScalar;
	}

	public CounterScalar getExtractorDelayScalar() {
		return extractorDelayScalar;
	}

}