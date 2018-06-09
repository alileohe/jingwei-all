package com.taobao.jingwei.webconsole.model;

import java.util.List;

public class JingweiApplierMeta {
	private String appMetaTopic;
	private String appShardColumn;
	private boolean appSplitTxEvent;
	private int appMaxEventSize;

	private long appSendTimeOut;
	private boolean appEnableColumnFilter;
	private List<String> appColumnFilterString;
	private String appCompressionType;

	private boolean appInsert;
	private boolean appUpdate;
	private boolean appDelete;

	// ∑œ∆˙ Ù–‘
	private boolean multiThread;
	private int queueCapacity;
	private int maxThreadCount;

	public String getAppMetaTopic() {
		return appMetaTopic;
	}

	public void setAppMetaTopic(String appMetaTopic) {
		this.appMetaTopic = appMetaTopic;
	}

	public String getAppShardColumn() {
		return appShardColumn;
	}

	public void setAppShardColumn(String appShardColumn) {
		this.appShardColumn = appShardColumn;
	}

	public boolean isAppSplitTxEvent() {
		return this.appSplitTxEvent;
	}

	public void setAppSplitTxEvent(boolean appSplitTxEvent) {
		this.appSplitTxEvent = appSplitTxEvent;
	}

	public int getAppMaxEventSize() {
		return appMaxEventSize;
	}

	public void setAppMaxEventSize(int appMaxEventSize) {
		this.appMaxEventSize = appMaxEventSize;
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

	public long getAppSendTimeOut() {
		return appSendTimeOut;
	}

	public void setAppSendTimeOut(long appSendTimeOut) {
		this.appSendTimeOut = appSendTimeOut;
	}

	public boolean isAppEnableColumnFilter() {
		return appEnableColumnFilter;
	}

	public void setAppEnableColumnFilter(boolean appEnableColumnFilter) {
		this.appEnableColumnFilter = appEnableColumnFilter;
	}

	public List<String> getAppColumnFilterString() {
		return appColumnFilterString;
	}

	public void setAppColumnFilterString(List<String> appColumnFilterString) {
		this.appColumnFilterString = appColumnFilterString;
	}

	public boolean isExclude() {
		boolean yes = false;
		if (this.appColumnFilterString != null && !this.appColumnFilterString.isEmpty()) {
			yes = this.appColumnFilterString.get(0).startsWith("exclude");
		}
		return yes;
	}

	public String getAppCompressionType() {
		return appCompressionType;
	}

	public void setAppCompressionType(String appCompressionType) {
		this.appCompressionType = appCompressionType;
	}

	public boolean isAppInsert() {
		return appInsert;
	}

	public void setAppInsert(boolean appInsert) {
		this.appInsert = appInsert;
	}

	public boolean isAppUpdate() {
		return appUpdate;
	}

	public void setAppUpdate(boolean appUpdate) {
		this.appUpdate = appUpdate;
	}

	public boolean isAppDelete() {
		return appDelete;
	}

	public void setAppDelete(boolean appDelete) {
		this.appDelete = appDelete;
	}

}
