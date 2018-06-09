package com.taobao.jingwei.common.node.tasks;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.node.AbstractNode;
import com.taobao.jingwei.common.node.type.ApplierType;
import com.taobao.jingwei.common.node.type.ExtractorType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Class SyncTaskNode
 * <p/>
 * ��������������Ϣ�洢�ڵ���
 *
 * @author qihao <dqylyln@gmail.com>
 * @since 11-11-15
 */
public class SyncTaskNode extends AbstractNode {

	private String extractorData;

	private String applierData;

	private String applierFilterData;

	private ExtractorType extractorType;

	private ApplierType applierType;

	/** ֻ�з�singleʹ��, ��ʾͬһ������������ж��ٸ�ʵ��, Ĭ����2 */
	private int taskInstanceCount = 1;

	/**
	 * ����ʱ�Ƿ�ʹ���ϴε�λ�㣬����ر�
	 * ��ѡ����ÿ�������������µ�λ�㿪ʼ
	 * ����������ݶ�ʧ������رո�ѡ���
	 * ����Ҫҵ���Լ�������ʧ�����ݡ�
	 * 
	 * ע�⣺��ѡ��ֻ����Core����ǰ���ò���Ч
	 */
	private boolean useLastPosition = true;

	/**
	 * ʵʱ�����ͳ������
	 */
	private long statsPeriod = 10000;

	private long summaryPeriod = 30000; // Inteval time in milliseconds
	private long comitLogPeriod = 15000; // Inteval time in milliseconds 
	private int comitLogCount = 10000;// Inteval tx count

	private boolean multiThread = false;
	private int queueCapacity = 256;
	private int maxThreadCount = 16;

	/** java opt*/
	private String javaOpt;

	/**multi grouping, ʹ��3����ֵ GROUPING_SCHEMA_REG GROUPING_TABLE_REG GROUPING_FIELDS*/
	private List<GroupingSetting> groupingSettings;

	private final static String MULTI_THREAD = "multiThread";
	private final static String QUEUE_CAPACITY = "queueCapacity";
	private final static String MAX_THREAD_COUNT = "maxThreadCount";
	private final static String JAVA_OPT = "javaOpt";
	private final static String GROUPING_SCHEMA_REG = "groupingSchemaReg";
	private final static String GROUPING_TABLE_REG = "groupingTableReg";
	private final static String GROUPING_FIELDS = "groupingFields";
	private final static String GROUPING_SETTINGS = "groupingSettings";

	/**
	 * json�洢����key����
	 */
	private final static String EXTRACTOR_DATA = "extractorData";
	private final static String APPLIER_DATA = "applierData";
	private final static String EXTRACTOR_TYPE = "extractorType";
	private final static String APPLIER_TYPE = "applierType";
	private final static String SINGLE_TASK = "singleTask";
	private final static String STATS_PERIOD = "statsPeriod";
	private final static String SUMMARY_PERIOD = "summaryPeriod";
	private final static String COMIT_LOG_PERIOD = "comitLogPeriod";
	private final static String COMIT_LOG_COUNT = "comitLogCount";
	private final static String USE_LAST_POSITION = "useLastPosition";
	private final static String APPLIER_FILTER_DATA = "applierFilterData";
	private final static String TASK_INSTANCE_COUNT = "taskInstanceCount";

	@Override
	protected void specilizeAttributeToJsonObject(JSONObject jsonObject) throws JSONException {
		jsonObject.put(EXTRACTOR_DATA, StringUtil.defaultIfBlank(this.getExtractorData()));
		jsonObject.put(APPLIER_DATA, StringUtil.defaultIfBlank(this.getApplierData()));

		jsonObject.put(EXTRACTOR_TYPE, null == this.extractorType ? ExtractorType.CUSTOM_EXTRACTOR.getType()
				: this.extractorType.getType());
		jsonObject.put(APPLIER_TYPE,
				null == this.applierType ? ApplierType.CUSTOM_APPLIER.getType() : this.applierType.getType());
		jsonObject.put(SINGLE_TASK, this.isSingleTask());
		jsonObject.put(STATS_PERIOD, this.getStatsPeriod());
		jsonObject.put(SUMMARY_PERIOD, this.getSummaryPeriod());
		jsonObject.put(COMIT_LOG_PERIOD, this.getComitLogPeriod());
		jsonObject.put(COMIT_LOG_COUNT, this.getComitLogCount());
		jsonObject.put(USE_LAST_POSITION, this.isUseLastPosition());
		jsonObject.put(MULTI_THREAD, multiThread);
		jsonObject.put(QUEUE_CAPACITY, queueCapacity);
		jsonObject.put(MAX_THREAD_COUNT, maxThreadCount);
		jsonObject.put(APPLIER_FILTER_DATA, this.getApplierFilterData());

		// ��singleʵ����
		jsonObject.put(TASK_INSTANCE_COUNT, this.getTaskInstanceCount());

		// java opt
		jsonObject.put(JAVA_OPT, this.getJavaOpt());

		// grouping setting
		if (null == this.getGroupingSettings()) {
			jsonObject.put(GROUPING_SETTINGS, new JSONArray());
		} else {
			JSONArray array = new JSONArray();
			for (GroupingSetting groupingSetting : this.getGroupingSettings()) {
				JSONObject entry = new JSONObject();
				entry.put(GROUPING_SCHEMA_REG, groupingSetting.getSchemaReg());
				entry.put(GROUPING_TABLE_REG, groupingSetting.getTableReg());
				entry.put(GROUPING_FIELDS, groupingSetting.getFields());
				array.put(entry);
			}
			jsonObject.put(GROUPING_SETTINGS, array);
		}

	}

	public SyncTaskNode() {
	}

	public SyncTaskNode(String jsonStr) {
		try {
			this.jsonStringToNodeSelf(jsonStr);
		} catch (JSONException e) {
			logger.error("new SyncTaskNode  paser Error!", e);
		}
	}

	@Override
	protected void jsonObjectToSpecilizeAttribute(JSONObject jsonObject) throws JSONException {
		this.setExtractorData(jsonObject.getString(EXTRACTOR_DATA));
		this.setApplierData(jsonObject.getString(APPLIER_DATA));
		this.setExtractorType(ExtractorType.getEnumByType(jsonObject.getInt(EXTRACTOR_TYPE)));
		this.setApplierType(ApplierType.getEnumByType(jsonObject.getInt(APPLIER_TYPE)));

		this.setStatsPeriod(jsonObject.getLong(STATS_PERIOD));
		this.setSummaryPeriod(jsonObject.getLong(SUMMARY_PERIOD));
		this.setComitLogPeriod(jsonObject.getLong(COMIT_LOG_PERIOD));
		this.setComitLogCount(jsonObject.getInt(COMIT_LOG_COUNT));
		this.setUseLastPosition(jsonObject.getBoolean(USE_LAST_POSITION));
		if (jsonObject.has(MULTI_THREAD)) {
			this.setMultiThread(jsonObject.getBoolean(MULTI_THREAD));
		}
		if (jsonObject.has(QUEUE_CAPACITY)) {
			this.setQueueCapacity(jsonObject.getInt(QUEUE_CAPACITY));
		}
		if (jsonObject.has(MAX_THREAD_COUNT)) {
			this.setMaxThreadCount(jsonObject.getInt(MAX_THREAD_COUNT));
		}

		if (jsonObject.has(APPLIER_FILTER_DATA)) {
			this.setApplierFilterData(jsonObject.getString(APPLIER_FILTER_DATA));
		}
		// ��single��ʵ����
		if (jsonObject.has(TASK_INSTANCE_COUNT)) {
			this.setTaskInstanceCount(jsonObject.getInt(TASK_INSTANCE_COUNT));
		}

		// java opt
		if (jsonObject.has(JAVA_OPT)) {
			this.setJavaOpt(jsonObject.getString(JAVA_OPT));
		}

		// grouping settings
		if (jsonObject.has(GROUPING_SETTINGS)) {
			JSONArray array = jsonObject.getJSONArray(GROUPING_SETTINGS);
			int length = array.length();
			List<GroupingSetting> settings = new ArrayList<GroupingSetting>(length);

			for (int i = 0; i < length; i++) {
				JSONObject ele = (JSONObject) array.get(i);
				GroupingSetting setting = new GroupingSetting();
				setting.setSchemaReg(ele.getString(GROUPING_SCHEMA_REG));
				setting.setTableReg(ele.getString(GROUPING_TABLE_REG));
				setting.setFields(ele.getString(GROUPING_FIELDS));
				settings.add(setting);
			}

			this.setGroupingSettings(settings);
		}
	}

	public String getExtractorData() {
		return extractorData;
	}

	public void setExtractorData(String extractorData) {
		this.extractorData = extractorData;
	}

	public String getApplierData() {
		return applierData;
	}

	public void setApplierData(String applierData) {
		this.applierData = applierData;
	}

	public ExtractorType getExtractorType() {
		return extractorType;
	}

	public void setExtractorType(ExtractorType extractorType) {
		this.extractorType = extractorType;
	}

	public ApplierType getApplierType() {
		return applierType;
	}

	public void setApplierType(ApplierType applierType) {
		this.applierType = applierType;
	}

	public long getStatsPeriod() {
		return statsPeriod;
	}

	public long getSummaryPeriod() {
		return summaryPeriod;
	}

	public void setSummaryPeriod(long summaryPeriod) {
		this.summaryPeriod = summaryPeriod;
	}

	public long getComitLogPeriod() {
		return comitLogPeriod;
	}

	public void setComitLogPeriod(long comitLogPeriod) {
		this.comitLogPeriod = comitLogPeriod;
	}

	public int getComitLogCount() {
		return comitLogCount;
	}

	public void setComitLogCount(int comitLogCount) {
		this.comitLogCount = comitLogCount;
	}

	public void setStatsPeriod(long statsPeriod) {
		this.statsPeriod = statsPeriod;
	}

	public boolean isSingleTask() {
		return this.taskInstanceCount == 1;
	}

	public boolean isUseLastPosition() {
		return useLastPosition;
	}

	public void setUseLastPosition(boolean useLastPosition) {
		this.useLastPosition = useLastPosition;
	}

	public static long getStatsPeriodFromJsonObject(String data) throws JSONException {
		JSONObject jsonObject;
		jsonObject = new JSONObject(data);
		return jsonObject.getLong("statsPeriod");
	}

	/**�����������ƣ��ͻ������ƻ��
	 * task��Ӧ��host�ĸ��ڵ�
	 * ���磺/jingwei-v2/tasks/��������/hosts/��������  
	 * @param taskName
	 * @param hostName
	 * @return
	 */
	public static String getHostTaskDataIdByName(String taskName, String hostName) {
		StringBuffer sb = new StringBuffer(JINGWEI_TASK_ROOT_PATH);
		sb.append(ZK_PATH_SEP).append(taskName).append(ZK_PATH_SEP);
		sb.append(JINGWEI_TASK_HOST_NODE).append(ZK_PATH_SEP).append(hostName);
		return sb.toString();
	}

	/**��ȡsyncNode����ĸ��ڵ�
	 * ���磺/jingwei-v2/tasks/��������
	 * @return
	 */
	@Override
	public String getDataIdOrNodePath() {
		return JINGWEI_TASK_ROOT_PATH + ZK_PATH_SEP + this.getName();
	}

	@Override
	public boolean isPersistent() {
		return true;
	}

	public boolean isMultiThread() {
		return multiThread;
	}

	public void setMultiThread(boolean multiThread) {
		this.multiThread = multiThread;
	}

	public int getQueueCapacity() {
		return queueCapacity;
	}

	public void setQueueCapacity(int queueCapacity) {
		this.queueCapacity = queueCapacity;
	}

	public int getMaxThreadCount() {
		return maxThreadCount;
	}

	public void setMaxThreadCount(int maxThreadCount) {
		this.maxThreadCount = maxThreadCount;
	}

	public String getApplierFilterData() {
		return applierFilterData;
	}

	public void setApplierFilterData(String applierFilterData) {
		this.applierFilterData = applierFilterData;
	}

	public int getTaskInstanceCount() {
		return taskInstanceCount;
	}

	public void setTaskInstanceCount(int taskInstanceCount) {
		this.taskInstanceCount = taskInstanceCount;
	}

	public String getJavaOpt() {
		return javaOpt;
	}

	public void setJavaOpt(String javaOpt) {
		this.javaOpt = javaOpt;
	}

	public List<GroupingSetting> getGroupingSettings() {
		return groupingSettings;
	}

	public void setGroupingSettings(List<GroupingSetting> groupingSettings) {
		this.groupingSettings = groupingSettings;
	}

	public static class GroupingSetting {

		private String schemaReg;
		private String tableReg;
		/** �ֿ�� ������ʹ�ö��ŷָ� */
		private String fields;

		public String getFields() {
			return fields;
		}

		public void setFields(String fields) {
			this.fields = fields;
		}

		public String getSchemaReg() {
			return schemaReg;
		}

		public void setSchemaReg(String schemaReg) {
			this.schemaReg = schemaReg;
		}

		public String getTableReg() {
			return tableReg;
		}

		public void setTableReg(String tableReg) {
			this.tableReg = tableReg;
		}

	}
}
