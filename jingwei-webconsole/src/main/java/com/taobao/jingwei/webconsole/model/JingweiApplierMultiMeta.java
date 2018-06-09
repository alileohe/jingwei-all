package com.taobao.jingwei.webconsole.model;

import java.util.List;

public class JingweiApplierMultiMeta {
	private String multiMetaTopic;
	private String multiShardColumn;
	private String multiSplitTxEvent;
	private int multiMaxEventSize;

	private long multiSendTimeOut;
	private String multiSrcSchemaReg;
	private String multiSrcTableReg;

	private String multiEnableColumnFilter;
	private List<String> multiColumnFilterString;
	private String multiCompressionType;
	private boolean multiColumnFilterAdvEnabled;
	private String multiColumnFilterAdv;

	private boolean multiInsert;
	private boolean multiUpdate;
	private boolean multiDelete;

	public String getMultiMetaTopic() {
		return multiMetaTopic;
	}

	public void setMultiMetaTopic(String multiMetaTopic) {
		this.multiMetaTopic = multiMetaTopic;
	}

	public String getMultiShardColumn() {
		return multiShardColumn;
	}

	public void setMultiShardColumn(String multiShardColumn) {
		this.multiShardColumn = multiShardColumn;
	}

	public String getMultiSplitTxEvent() {
		return multiSplitTxEvent;
	}

	public void setMultiSplitTxEvent(String multiSplitTxEvent) {
		this.multiSplitTxEvent = multiSplitTxEvent;
	}

	public int getMultiMaxEventSize() {
		return multiMaxEventSize;
	}

	public void setMultiMaxEventSize(int multiMaxEventSize) {
		this.multiMaxEventSize = multiMaxEventSize;
	}

	public long getMultiSendTimeOut() {
		return multiSendTimeOut;
	}

	public void setMultiSendTimeOut(long multiSendTimeOut) {
		this.multiSendTimeOut = multiSendTimeOut;
	}

	public String getMultiSrcSchemaReg() {
		return multiSrcSchemaReg;
	}

	public void setMultiSrcSchemaReg(String multiSrcSchemaReg) {
		this.multiSrcSchemaReg = multiSrcSchemaReg;
	}

	public String getMultiSrcTableReg() {
		return multiSrcTableReg;
	}

	public void setMultiSrcTableReg(String multiSrcTableReg) {
		this.multiSrcTableReg = multiSrcTableReg;
	}

	public String getMultiEnableColumnFilter() {
		return multiEnableColumnFilter;
	}

	public void setMultiEnableColumnFilter(String multiEnableColumnFilter) {
		this.multiEnableColumnFilter = multiEnableColumnFilter;
	}

	public List<String> getMultiColumnFilterString() {
		return multiColumnFilterString;
	}

	public void setMultiColumnFilterString(List<String> multiColumnFilterString) {
		this.multiColumnFilterString = multiColumnFilterString;
	}

	public boolean isExclude() {
		boolean yes = false;
		if (this.multiColumnFilterString != null && !this.multiColumnFilterString.isEmpty()) {
			yes = this.multiColumnFilterString.get(0).startsWith("exclude");
		}
		return yes;
	}

	public String getMultiCompressionType() {
		return multiCompressionType;
	}

	public void setMultiCompressionType(String multiCompressionType) {
		this.multiCompressionType = multiCompressionType;
	}

	public boolean isMultiColumnFilterAdvEnabled() {
		return multiColumnFilterAdvEnabled;
	}

	public void setMultiColumnFilterAdvEnabled(boolean multiColumnFilterAdvEnabled) {
		this.multiColumnFilterAdvEnabled = multiColumnFilterAdvEnabled;
	}

	public String getMultiColumnFilterAdv() {
		return multiColumnFilterAdv;
	}

	public void setMultiColumnFilterAdv(String multiColumnFilterAdv) {
		this.multiColumnFilterAdv = multiColumnFilterAdv;
	}

	public boolean isMultiInsert() {
		return multiInsert;
	}

	public void setMultiInsert(boolean multiInsert) {
		this.multiInsert = multiInsert;
	}

	public boolean isMultiUpdate() {
		return multiUpdate;
	}

	public void setMultiUpdate(boolean multiUpdate) {
		this.multiUpdate = multiUpdate;
	}

	public boolean isMultiDelete() {
		return multiDelete;
	}

	public void setMultiDelete(boolean multiDelete) {
		this.multiDelete = multiDelete;
	}

}
