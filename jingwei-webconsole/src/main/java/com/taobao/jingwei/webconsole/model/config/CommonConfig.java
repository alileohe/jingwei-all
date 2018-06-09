package com.taobao.jingwei.webconsole.model.config;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * @desc 
 * 
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Dec 20, 2012 1:43:51 PM
 */

public class CommonConfig implements Serializable {

	private static final long serialVersionUID = 1L;

	/** 任务名 */
	private String taskName;

	/** 只有非single使用, 表示同一个任务可以运行多少个实例, 默认是1 */
	private int taskInstanceCount = 1;

	/** 注意：该选项只有在Core启动前设置才有效	 */
	private boolean useLastPosition = true;

	/** 实时任务的统计周期 */
	private long statsPeriod = 10000;

	// Inteval time in milliseconds
	private long summaryPeriod = 30000;

	// Inteval time in milliseconds 
	private long comitLogPeriod = 15000;

	// Inteval tx count
	private int comitLogCount = 10000;

	/** java opt*/
	private String javaOpt;

	// multi thread config
	private boolean multiThread = false;
	private int queueCapacity = 256;
	private int maxThreadCount = 16;
	private List<GroupingConfig> groupingSettings;

	// 是否使用过滤器
	private Boolean useFilter = false;

	private CommonFilterConfig commonFilterConfig;

	// other info
	private String dbaStaff;
	private String devStaff;
	private String configStaff;
	private Timestamp startTime;
	private Timestamp onlineTime;
	private Timestamp offlineTime;
	private String description;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDbaStaff() {
		return dbaStaff;
	}

	public void setDbaStaff(String dbaStaff) {
		this.dbaStaff = dbaStaff;
	}

	public String getDevStaff() {
		return devStaff;
	}

	public void setDevStaff(String devStaff) {
		this.devStaff = devStaff;
	}

	public String getConfigStaff() {
		return configStaff;
	}

	public void setConfigStaff(String configStaff) {
		this.configStaff = configStaff;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public Timestamp getOnlineTime() {
		return onlineTime;
	}

	public void setOnlineTime(Timestamp onlineTime) {
		this.onlineTime = onlineTime;
	}

	public Timestamp getOfflineTime() {
		return offlineTime;
	}

	public void setOfflineTime(Timestamp offlineTime) {
		this.offlineTime = offlineTime;
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

	public List<GroupingConfig> getGroupingSettings() {
		return groupingSettings;
	}

	public void setGroupingSettings(List<GroupingConfig> groupingSettings) {
		this.groupingSettings = groupingSettings;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public int getTaskInstanceCount() {
		return taskInstanceCount;
	}

	public void setTaskInstanceCount(int taskInstanceCount) {
		this.taskInstanceCount = taskInstanceCount;
	}

	public boolean isUseLastPosition() {
		return useLastPosition;
	}

	public void setUseLastPosition(boolean useLastPosition) {
		this.useLastPosition = useLastPosition;
	}

	public long getStatsPeriod() {
		return statsPeriod;
	}

	public void setStatsPeriod(long statsPeriod) {
		this.statsPeriod = statsPeriod;
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

	public String getJavaOpt() {
		return javaOpt;
	}

	public void setJavaOpt(String javaOpt) {
		this.javaOpt = javaOpt;
	}

	public CommonFilterConfig getCommonFilterConfig() {
		return commonFilterConfig;
	}

	public void setCommonFilterConfig(CommonFilterConfig commonFilterConfig) {
		this.commonFilterConfig = commonFilterConfig;
	}

	public Boolean getUseFilter() {
		return useFilter;
	}

	public void setUseFilter(Boolean useFilter) {
		this.useFilter = useFilter;
	}

}
