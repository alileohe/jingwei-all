package com.taobao.jingwei.webconsole.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.node.applier.EventFilterNode;
import com.taobao.jingwei.common.node.applier.EventFilterNode.ColumnFilterConditionNode;
import com.taobao.jingwei.common.node.applier.MetaApplierNode;
import com.taobao.jingwei.webconsole.common.JingweiWebConsoleConstance;

public class JingweiApplierMetaType {
	private static final Log log = LogFactory.getLog(JingweiWebConsoleConstance.JINGWEI_LOG);

	private String appMetaTopic;
	private String appShardColumn;
	private boolean appSplitTxEvent;
	private int appMaxEventSize;

	private long appSendTimeOut;
	private boolean appEnableColumnFilter;
	private String appColumnFilterString;

	private String appCompressionType;

	private boolean appInsert;
	private boolean appUpdate;
	private boolean appDelete;

	// ·ÏÆúÊôÐÔ
	private boolean multiThread;
	private int queueCapacity;
	private int maxThreadCount;

	public MetaApplierNode getMetaApplierNode() {
		MetaApplierNode node = new MetaApplierNode();
		node.setMetaTopic(this.appMetaTopic);
		node.setShardColumn(this.appShardColumn);
		node.setSplitTxEvent(this.appSplitTxEvent);
		node.setMaxEventSize(this.appMaxEventSize);
		node.setSendTimeOut(this.appSendTimeOut);
		node.setEnableColumnfilter(this.appEnableColumnFilter);
		node.setCompressionType(this.appCompressionType);
		EventFilterNode filter = new EventFilterNode();
		filter.setIncludeInsert(this.appInsert);
		filter.setIncludeUpdate(this.appUpdate);
		filter.setIncludeDelete(this.appDelete);
		try {
			if (this.appEnableColumnFilter && StringUtil.isNotBlank(this.appColumnFilterString)) {
				Map<String, HashMap<String, ColumnFilterConditionNode>> condition = JingweiModelHelper
						.columnFilterStringToJson(this.appColumnFilterString);
				filter.setConditions(condition);
			}
			node.setEventFilterData(filter.toJSONString());
		} catch (Exception e) {
			log.error("½âÎöEvent Filter Node³ö´í", e);
		}
		return node;
	}

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

	public String getAppColumnFilterString() {
		return appColumnFilterString;
	}

	public void setAppColumnFilterString(String appColumnFilterString) {
		this.appColumnFilterString = appColumnFilterString;
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
