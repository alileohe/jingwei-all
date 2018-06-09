package com.taobao.jingwei.common.node;

import jodd.util.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * ͳ�ƽڵ㣬��Ӧ·��/jingwei/tasks/**task/**host/stats
 * 
 * @author shuohailhl
 * 
 */
public class StatsNode extends AbstractNode {
	/**
	 * ����jsonobj��ͳ�����ݶ�Ӧ��KEY�� �� Key��β, Ϊ����д��־ʱ���� ����
	 * 
	 */
	public static final String STATUS_SUFFIX = "Key";
	public static final String LAST_PERIOD_DELETE_COUNT_KEY = "lastPeriodDeleteCount"
			+ STATUS_SUFFIX;
	public static final String LAST_PERIOD_INSERT_COUNT_KEY = "lastPeriodInsertCount"
			+ STATUS_SUFFIX;
	public static final String LAST_PERIOD_UPDATE_COUNT_KEY = "lastPeriodUpdateCount"
			+ STATUS_SUFFIX;
	public static final String TODAY_DELETE_COUNT_KEY = "todayDeleteCount"
			+ STATUS_SUFFIX;
	public static final String TODAY_INSERT_COUNT_KEY = "todayInsertCount"
			+ STATUS_SUFFIX;
	public static final String TODAY_UPDATE_COUNT_KEY = "todayUpdateCount"
			+ STATUS_SUFFIX;
	public static final String TOTAL_DELETE_COUNT_KEY = "totalDeleteCount"
			+ STATUS_SUFFIX;
	public static final String TOTAL_INSERT_COUNT_KEY = "totalInsertCount"
			+ STATUS_SUFFIX;
	public static final String TOTAL_UPDATE_COUNT_KEY = "totalUpdateCount"
			+ STATUS_SUFFIX;

	public static final String LAST_PERIOD_DELETE_TPS_KEY = "lastPeriodDeleteTps"
			+ STATUS_SUFFIX;
	public static final String LAST_PERIOD_INSERT_TPS_KEY = "lastPeriodInsertTps"
			+ STATUS_SUFFIX;
	public static final String LAST_PERIOD_UPDATE_TPS_KEY = "lastPeriodUpdateTps"
			+ STATUS_SUFFIX;

	public static final String LAST_PERIOD_AVG_DELETE_DELAY_KEY = "lastPeriodAvgDeleteDelay"
			+ STATUS_SUFFIX;
	public static final String LAST_PERIOD_AVG_INSERT_DELAY_KEY = "lastPeriodAvgInsertDelay"
			+ STATUS_SUFFIX;
	public static final String LAST_PERIOD_AVG_UPDATE_DELAY_KEY = "lastPeriodAvgUpdateDelay"
			+ STATUS_SUFFIX;
	public static final String LAST_PERIOD_AVG_EXTRACTOR_DELAY_KEY = "lastPeriodAvgExtractorDelay"
			+ STATUS_SUFFIX;

	public static final String LAST_PERIOD_DELETE_EXCEPTION_COUNT_KEY = "lastPeriodDeleteExceptionCount"
			+ STATUS_SUFFIX;
	public static final String LAST_PERIOD_INSERT_EXCEPTION_COUNT_KEY = "lastPeriodInsertExceptionCount"
			+ STATUS_SUFFIX;
	public static final String LAST_PERIOD_UPDATE_EXCEPTION_COUNT_KEY = "lastPeriodUpdateExceptionCount"
			+ STATUS_SUFFIX;

	public static final String TODAY_DELETE_EXCEPTION_COUNT_KEY = "todayDeleteExceptionCount"
			+ STATUS_SUFFIX;
	public static final String TODAY_INSERT_EXCEPTION_COUNT_KEY = "todayInsertExceptionCount"
			+ STATUS_SUFFIX;
	public static final String TODAY_UPDATE_EXCEPTION_COUNT_KEY = "todayUpdateExceptionCount"
			+ STATUS_SUFFIX;

	public static final String TOTAL_DELETE_EXCEPTION_COUNT_KEY = "totalDeleteExceptionCount"
			+ STATUS_SUFFIX;
	public static final String TOTAL_INSERT_EXCEPTION_COUNT_KEY = "totalInsertExceptionCount"
			+ STATUS_SUFFIX;
	public static final String TOTAL_UPDATE_EXCEPTION_COUNT_KEY = "totalUpdateExceptionCount"
			+ STATUS_SUFFIX;

	public static final String TX_COUNT_KEY = "txCount" + STATUS_SUFFIX;

	public static final String TX_TPS_KEY = "txCountTps" + STATUS_SUFFIX;

	public static final String TX_MILLIS_MIN_LATENCY_KEY = "txMillisMinLatency"
			+ STATUS_SUFFIX;

	public static final String TX_MILLIS_MAX_LATENCY_KEY = "txMillisMaxLatency"
			+ STATUS_SUFFIX;

	public static final String TX_MILLIS_AVG_LATENCY_KEY = "txMillisAvgLatency"
			+ STATUS_SUFFIX;

	// taskName��hostNameȷ����Ϳ���ȷ��dataIdOrPath��·��
	protected String dataIdOrPath;

	// ����������
	private String taskName;

	// ���е�������
	private String hostName;

	// ������ZK��ʱ���,Ҳ��ͳ�Ƶ�ʱ���
	private long statsTime;

	// ����ɾ���¼��ļ���
	private long lastPeriodDeleteCount = 0L;
	// ��������¼��ļ���
	private long lastPeriodInsertCount = 0L;
	// ��������¼��ļ���
	private long lastPeriodUpdateCount = 0L;
	// ���촦��ɾ���¼��ļ���
	private long todayDeleteCount = 0L;
	// ���촦������¼��ļ���
	private long todayInsertCount = 0L;
	// ���촦������¼��ļ���
	private long todayUpdateCount = 0L;

	// ͳ��������ƽ���ӳ�ʱ��
	private double lastPeriodAvgDeleteDelay = 0.00D;
	private double lastPeriodAvgInsertDelay = 0.00D;
	private double lastPeriodAvgUpdateDelay = 0.00D;
	private double lastPeriodAvgExtractorDelay = 0.00D;

	// ͳ�����ڵ�TPS
	private double lastPeriodDeleteTps = 0.00D;
	private double lastPeriodInsertTps = 0.00D;
	private double lastPeriodUpdateTps = 0.00D;

	// ����ɾ������������쳣����
	private long todayDeleteExceptionCount = 0;
	// ���ղ������������쳣����
	private long todayInsertExceptionCount = 0;
	// ���ո��²���������쳣����
	private long todayUpdateExceptionCount = 0;
	// ͳ��������ɾ������������쳣����
	private long lastPeriodDeleteExceptionCount = 0;
	// ͳ�������ڲ������������쳣����
	private long lastPeriodInsertExceptionCount = 0;
	// ͳ�������ڸ��²���������쳣����
	private long lastPeriodUpdateExceptionCount = 0;

	// ��������
	private long txCount = 0L;
	// ����TPS
	private float txTps = 0F;
	// ��������С�ӳ�
	private long txMillisMinLatency = 0L;
	// ����������ӳ�
	private long txMillisMaxLatency = 0L;
	// ������ƽ���ӳ�
	private long txMillisAvgLatency = 0L;

	public StatsNode(String taskName, String hostName) {
		this.taskName = taskName;
		this.hostName = hostName;
	}

	/**
	 * @param taskName
	 * @param hostName
	 * @return /jingwei/tasks/��������/hosts/��������/stats
	 */
	public static String getNodeIdOrPath(String taskName, String hostName) {
		StringBuffer sb = new StringBuffer(JINGWEI_TASK_ROOT_PATH);
		sb.append(ZK_PATH_SEP).append(taskName).append(ZK_PATH_SEP);
		sb.append(JINGWEI_TASK_HOST_NODE).append(ZK_PATH_SEP).append(hostName);
		sb.append(ZK_PATH_SEP).append(JINGWEI_STATS_NODE_NAME);
		return sb.toString();
	}

	@Override
	public String getDataIdOrNodePath() {
		return StatsNode.getNodeIdOrPath(taskName, hostName);
	}

	@Override
	protected synchronized void specilizeAttributeToJsonObject(
			JSONObject jsonObject) throws JSONException {
		// ͳ��ʱ���
		jsonObject.put("statsTime", this.getStatsTime());

		// ���������Event����
		jsonObject.put(LAST_PERIOD_DELETE_COUNT_KEY,
				this.getLastPeriodDeleteCount());
		jsonObject.put(LAST_PERIOD_INSERT_COUNT_KEY,
				this.getLastPeriodInsertCount());
		jsonObject.put(LAST_PERIOD_UPDATE_COUNT_KEY,
				this.getLastPeriodUpdateCount());

		jsonObject.put(TODAY_DELETE_COUNT_KEY, this.getTodayDeleteCount());
		jsonObject.put(TODAY_INSERT_COUNT_KEY, this.getTodayInsertCount());
		jsonObject.put(TODAY_UPDATE_COUNT_KEY, this.getTodayUpdateCount());

		// ���������TPS
		jsonObject.put(LAST_PERIOD_DELETE_TPS_KEY,
				this.getLastPeriodDeleteTps());
		jsonObject.put(LAST_PERIOD_INSERT_TPS_KEY,
				this.getLastPeriodInsertTps());
		jsonObject.put(LAST_PERIOD_UPDATE_TPS_KEY,
				this.getLastPeriodUpdateTps());

		// ͳ�������ڵ�ƽ���ӳ�ʱ��
		jsonObject.put(LAST_PERIOD_AVG_DELETE_DELAY_KEY,
				this.getLastPeriodAvgDeleteDelay());
		jsonObject.put(LAST_PERIOD_AVG_INSERT_DELAY_KEY,
				this.getLastPeriodAvgInsertDelay());
		jsonObject.put(LAST_PERIOD_AVG_UPDATE_DELAY_KEY,
				this.getLastPeriodAvgUpdateDelay());
		jsonObject.put(LAST_PERIOD_AVG_EXTRACTOR_DELAY_KEY,
				this.getLastPeriodAvgExtractorDelay());

		// �쳣����
		jsonObject.put(LAST_PERIOD_DELETE_EXCEPTION_COUNT_KEY,
				this.getLastPeriodDeleteExceptionCount());
		jsonObject.put(LAST_PERIOD_INSERT_EXCEPTION_COUNT_KEY,
				this.getLastPeriodInsertExceptionCount());
		jsonObject.put(LAST_PERIOD_UPDATE_EXCEPTION_COUNT_KEY,
				this.getLastPeriodUpdateExceptionCount());
		jsonObject.put(TODAY_DELETE_EXCEPTION_COUNT_KEY,
				this.getTodayDeleteExceptionCount());
		jsonObject.put(TODAY_INSERT_EXCEPTION_COUNT_KEY,
				this.getTodayInsertExceptionCount());
		jsonObject.put(TODAY_UPDATE_EXCEPTION_COUNT_KEY,
				this.getTodayUpdateExceptionCount());

		// extractor����ͳ��
		jsonObject.put(TX_COUNT_KEY, this.getTxCount());
		jsonObject.put(TX_TPS_KEY, this.getTxTps());
		jsonObject.put(TX_MILLIS_MIN_LATENCY_KEY, this.getTxMillisMinLatency());
		jsonObject.put(TX_MILLIS_MAX_LATENCY_KEY, this.getTxMillisMaxLatency());
		jsonObject.put(TX_MILLIS_AVG_LATENCY_KEY, this.getTxMillisAvgLatency());
	}

	@Override
	public boolean isPersistent() {
		return true;
	}

	@Override
	protected void jsonObjectToSpecilizeAttribute(JSONObject jsonObject)
			throws JSONException {
		// ͳ��ʱ���
		this.setStatsTime(jsonObject.getLong("statsTime"));

		// ƽ���ӳ�ʱ��
		this.setLastPeriodAvgDeleteDelay(jsonObject
				.getDouble(LAST_PERIOD_AVG_DELETE_DELAY_KEY));
		this.setLastPeriodAvgInsertDelay(jsonObject
				.getDouble(LAST_PERIOD_AVG_INSERT_DELAY_KEY));
		this.setLastPeriodAvgUpdateDelay(jsonObject
				.getDouble(LAST_PERIOD_AVG_UPDATE_DELAY_KEY));
		this.setLastPeriodAvgExtractorDelay(jsonObject
				.getDouble(LAST_PERIOD_AVG_EXTRACTOR_DELAY_KEY));

		// ���������TPS
		this.setLastPeriodDeleteTps(jsonObject
				.getDouble(LAST_PERIOD_DELETE_TPS_KEY));
		this.setLastPeriodInsertTps(jsonObject
				.getDouble(LAST_PERIOD_INSERT_TPS_KEY));
		this.setLastPeriodUpdateTps(jsonObject
				.getDouble(LAST_PERIOD_UPDATE_TPS_KEY));

		// ������ͳ����
		this.setLastPeriodDeleteCount(jsonObject
				.getLong(LAST_PERIOD_DELETE_COUNT_KEY));
		this.setTodayDeleteCount(jsonObject.getLong(TODAY_DELETE_COUNT_KEY));

		this.setLastPeriodInsertCount(jsonObject
				.getLong(LAST_PERIOD_INSERT_COUNT_KEY));
		this.setTodayInsertCount(jsonObject.getLong(TODAY_INSERT_COUNT_KEY));

		this.setLastPeriodUpdateCount(jsonObject
				.getLong(LAST_PERIOD_UPDATE_COUNT_KEY));
		this.setTodayUpdateCount(jsonObject.getLong(TODAY_UPDATE_COUNT_KEY));

		// �쳣ͳ����
		this.setLastPeriodDeleteExceptionCount(jsonObject
				.getInt(LAST_PERIOD_DELETE_EXCEPTION_COUNT_KEY));
		this.setTodayDeleteExceptionCount(jsonObject
				.getInt(TODAY_DELETE_EXCEPTION_COUNT_KEY));

		this.setLastPeriodInsertExceptionCount(jsonObject
				.getInt(LAST_PERIOD_INSERT_EXCEPTION_COUNT_KEY));
		this.setTodayInsertExceptionCount(jsonObject
				.getInt(TODAY_INSERT_EXCEPTION_COUNT_KEY));

		this.setLastPeriodUpdateExceptionCount(jsonObject
				.getInt(LAST_PERIOD_UPDATE_EXCEPTION_COUNT_KEY));
		this.setTodayUpdateExceptionCount(jsonObject
				.getInt(TODAY_UPDATE_EXCEPTION_COUNT_KEY));

		// extractor����ͳ��
		this.setTxCount(jsonObject.getLong(TX_COUNT_KEY));
		String tpsStr = jsonObject.getString(TX_TPS_KEY);
		if (StringUtil.isNotBlank(tpsStr)) {
			this.setTxTps(Float.valueOf(tpsStr));
		}
		this.setTxMillisMinLatency(jsonObject.getLong(TX_MILLIS_MIN_LATENCY_KEY));
		this.setTxMillisMaxLatency(jsonObject.getLong(TX_MILLIS_MAX_LATENCY_KEY));
		this.setTxMillisAvgLatency(jsonObject.getLong(TX_MILLIS_AVG_LATENCY_KEY));
	}

	public long getLastPeriodDeleteCount() {
		return lastPeriodDeleteCount;
	}

	public void setLastPeriodDeleteCount(long lastPeriodDeleteCount) {
		this.lastPeriodDeleteCount = lastPeriodDeleteCount;
	}

	public long getLastPeriodInsertCount() {
		return lastPeriodInsertCount;
	}

	public void setLastPeriodInsertCount(long lastPeriodInsertCount) {
		this.lastPeriodInsertCount = lastPeriodInsertCount;
	}

	public long getLastPeriodUpdateCount() {
		return lastPeriodUpdateCount;
	}

	public void setLastPeriodUpdateCount(long lastPeriodUpdateCount) {
		this.lastPeriodUpdateCount = lastPeriodUpdateCount;
	}

	public long getTodayDeleteCount() {
		return todayDeleteCount;
	}

	public void setTodayDeleteCount(long todayDeleteCount) {
		this.todayDeleteCount = todayDeleteCount;
	}

	public long getTodayInsertCount() {
		return todayInsertCount;
	}

	public void setTodayInsertCount(long todayInsertCount) {
		this.todayInsertCount = todayInsertCount;
	}

	public long getTodayUpdateCount() {
		return todayUpdateCount;
	}

	public void setTodayUpdateCount(long todayUpdateCount) {
		this.todayUpdateCount = todayUpdateCount;
	}

	public double getLastPeriodAvgDeleteDelay() {
		return lastPeriodAvgDeleteDelay;
	}

	public void setLastPeriodAvgDeleteDelay(double lastPeriodAvgDeleteDelay) {
		this.lastPeriodAvgDeleteDelay = lastPeriodAvgDeleteDelay;
	}

	public double getLastPeriodAvgInsertDelay() {
		return lastPeriodAvgInsertDelay;
	}

	public void setLastPeriodAvgInsertDelay(double lastPeriodAvgInsertDelay) {
		this.lastPeriodAvgInsertDelay = lastPeriodAvgInsertDelay;
	}

	public double getLastPeriodAvgUpdateDelay() {
		return lastPeriodAvgUpdateDelay;
	}

	public void setLastPeriodAvgUpdateDelay(double lastPeriodAvgUpdateDelay) {
		this.lastPeriodAvgUpdateDelay = lastPeriodAvgUpdateDelay;
	}

	public double getLastPeriodDeleteTps() {
		return lastPeriodDeleteTps;
	}

	public void setLastPeriodDeleteTps(double lastPeriodDeleteTps) {
		this.lastPeriodDeleteTps = lastPeriodDeleteTps;
	}

	public double getLastPeriodInsertTps() {
		return lastPeriodInsertTps;
	}

	public void setLastPeriodInsertTps(double lastPeriodInsertTps) {
		this.lastPeriodInsertTps = lastPeriodInsertTps;
	}

	public double getLastPeriodUpdateTps() {
		return lastPeriodUpdateTps;
	}

	public void setLastPeriodUpdateTps(double lastPeriodUpdateTps) {
		this.lastPeriodUpdateTps = lastPeriodUpdateTps;
	}

	public long getTodayDeleteExceptionCount() {
		return todayDeleteExceptionCount;
	}

	public void setTodayDeleteExceptionCount(long todayDeleteExceptionCount) {
		this.todayDeleteExceptionCount = todayDeleteExceptionCount;
	}

	public long getTodayInsertExceptionCount() {
		return todayInsertExceptionCount;
	}

	public void setTodayInsertExceptionCount(long todayInsertExceptionCount) {
		this.todayInsertExceptionCount = todayInsertExceptionCount;
	}

	public long getTodayUpdateExceptionCount() {
		return todayUpdateExceptionCount;
	}

	public void setTodayUpdateExceptionCount(long todayUpdateExceptionCount) {
		this.todayUpdateExceptionCount = todayUpdateExceptionCount;
	}

	public long getLastPeriodDeleteExceptionCount() {
		return lastPeriodDeleteExceptionCount;
	}

	public void setLastPeriodDeleteExceptionCount(
			long lastPeriodDeleteExceptionCount) {
		this.lastPeriodDeleteExceptionCount = lastPeriodDeleteExceptionCount;
	}

	public long getLastPeriodInsertExceptionCount() {
		return lastPeriodInsertExceptionCount;
	}

	public void setLastPeriodInsertExceptionCount(
			long lastPeriodInsertExceptionCount) {
		this.lastPeriodInsertExceptionCount = lastPeriodInsertExceptionCount;
	}

	public long getLastPeriodUpdateExceptionCount() {
		return lastPeriodUpdateExceptionCount;
	}

	public void setLastPeriodUpdateExceptionCount(
			long lastPeriodUpdateExceptionCount) {
		this.lastPeriodUpdateExceptionCount = lastPeriodUpdateExceptionCount;
	}

	public String getDataIdOrPath() {
		return dataIdOrPath;
	}

	public void setDataIdOrPath(String dataIdOrPath) {
		this.dataIdOrPath = dataIdOrPath;
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

	public long getStatsTime() {
		return statsTime;
	}

	public void setStatsTime(long statsTime) {
		this.statsTime = statsTime;
	}

	public long getTxCount() {
		return txCount;
	}

	public void setTxCount(long txCount) {
		this.txCount = txCount;
	}

	public long getTxMillisMinLatency() {
		return txMillisMinLatency;
	}

	public void setTxMillisMinLatency(long txMillisMinLatency) {
		this.txMillisMinLatency = txMillisMinLatency;
	}

	public long getTxMillisMaxLatency() {
		return txMillisMaxLatency;
	}

	public void setTxMillisMaxLatency(long txMillisMaxLatency) {
		this.txMillisMaxLatency = txMillisMaxLatency;
	}

	public long getTxMillisAvgLatency() {
		return txMillisAvgLatency;
	}

	public void setTxMillisAvgLatency(long txMillisAvgLatency) {
		this.txMillisAvgLatency = txMillisAvgLatency;
	}

	public double getLastPeriodAvgExtractorDelay() {
		return lastPeriodAvgExtractorDelay;
	}

	public void setLastPeriodAvgExtractorDelay(
			double lastPeriodAvgExtractorDelay) {
		this.lastPeriodAvgExtractorDelay = lastPeriodAvgExtractorDelay;
	}

	public float getTxTps() {
		return txTps;
	}

	public void setTxTps(float txTps) {
		this.txTps = txTps;
	}
}
