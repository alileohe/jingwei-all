package com.taobao.jingwei.webconsole.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.node.applier.EventFilterNode;
import com.taobao.jingwei.common.node.applier.MultiMetaApplierNode;
import com.taobao.jingwei.common.node.applier.SubMetaApplierNode;
import com.taobao.jingwei.webconsole.common.JingweiWebConsoleConstance;

public class JingweiApplierMultiMetaType {
	private static final Log log = LogFactory.getLog(JingweiWebConsoleConstance.JINGWEI_LOG);

	private String[] multiMetaTopic;
	private String[] multiShardColumn;
	private String[] multiSplitTxEvent;
	private int[] multiMaxEventSize;

	private long[] multiSendTimeOut;
	private String[] multiSrcSchemaReg;
	private String[] multiSrcTableReg;

	private String[] multiEnableColumnFilter;
	private String[] multiColumnFilterString;
	private String[] multiCompressionType;
	private boolean[] multiColumnFilterAdvEnabled;
	private String[] multiColumnFilterAdv;

	private boolean[] multiInsert;
	private boolean[] multiUpdate;
	private boolean[] multiDelete;

	public MultiMetaApplierNode getMultiMetaApplierNode() {
		MultiMetaApplierNode node = new MultiMetaApplierNode();
		int size = multiMetaTopic.length;
		List<SubMetaApplierNode> list = new ArrayList<SubMetaApplierNode>(size);
		for (int i = 0; i < size; i++) {
			// 过滤主题为空的数据
			if (i < multiMetaTopic.length && StringUtil.isBlank(multiMetaTopic[i])) {
				continue;
			}
			SubMetaApplierNode sub = new SubMetaApplierNode();
			sub.setSrcSchemaReg(i < multiSrcSchemaReg.length ? multiSrcSchemaReg[i] : null);
			sub.setSrcTableReg(i < multiSrcTableReg.length ? multiSrcTableReg[i] : null);
			sub.setMetaTopic(i < multiMetaTopic.length ? multiMetaTopic[i] : null);
			sub.setShardColumn(i < multiShardColumn.length ? multiShardColumn[i] : null);
			sub.setSplitTxEvent(i < multiSplitTxEvent.length ? "true".equals(multiSplitTxEvent[i]) : false);
			sub.setMaxEventSize(i < multiMaxEventSize.length ? multiMaxEventSize[i] : 0);
			sub.setSendTimeOut(i < multiSendTimeOut.length ? multiSendTimeOut[i] : 0);
			sub.setCompressionType(i < multiCompressionType.length ? multiCompressionType[i] : null);
			sub.setEnableColumnfilter(i < multiEnableColumnFilter.length ? "true".equals(multiEnableColumnFilter[i])
					: false);
			EventFilterNode filter = new EventFilterNode();
			filter.setIncludeInsert(multiInsert[i]);
			filter.setIncludeUpdate(multiUpdate[i]);
			filter.setIncludeDelete(multiDelete[i]);
			//if (i < multiColumnFilterString.length) {
				if (i < multiColumnFilterAdvEnabled.length) {
					if (multiColumnFilterAdvEnabled[i]) {
						filter.setSourceCode(multiColumnFilterAdv[i]);

					} else {
						if (sub.isEnableColumnfilter() && StringUtil.isNotBlank(multiColumnFilterString[i])) {
							Map<String, HashMap<String, EventFilterNode.ColumnFilterConditionNode>> condition = parseEventFilter(multiColumnFilterString[i]);
							filter.setConditions(condition);
						}
					}
				}
			//}
			try {
				sub.setEventFilterData(filter.toJSONString());
			} catch (JSONException e) {
			}
			list.add(sub);
		}
		node.setSubMetaApplierNodes(list);
		return node;
	}

	private Map<String, HashMap<String, EventFilterNode.ColumnFilterConditionNode>> parseEventFilter(String filterString) {
		try {
			if (StringUtil.isNotBlank(filterString)) {
				return JingweiModelHelper.columnFilterStringToJson(filterString);
			}
		} catch (Exception e) {
			log.error("解析Event Filter Node出错(MULTI)", e);
		}
		return null;
	}

	public String[] getMultiMetaTopic() {
		return multiMetaTopic;
	}

	public void setMultiMetaTopic(String[] multiMetaTopic) {
		this.multiMetaTopic = multiMetaTopic;
	}

	public String[] getMultiShardColumn() {
		return multiShardColumn;
	}

	public void setMultiShardColumn(String[] multiShardColumn) {
		this.multiShardColumn = multiShardColumn;
	}

	public String[] getMultiSplitTxEvent() {
		return multiSplitTxEvent;
	}

	public void setMultiSplitTxEvent(String[] multiSplitTxEvent) {
		this.multiSplitTxEvent = multiSplitTxEvent;
	}

	public int[] getMultiMaxEventSize() {
		return multiMaxEventSize;
	}

	public void setMultiMaxEventSize(int[] multiMaxEventSize) {
		this.multiMaxEventSize = multiMaxEventSize;
	}

	public long[] getMultiSendTimeOut() {
		return multiSendTimeOut;
	}

	public void setMultiSendTimeOut(long[] multiSendTimeOut) {
		this.multiSendTimeOut = multiSendTimeOut;
	}

	public String[] getMultiSrcSchemaReg() {
		return multiSrcSchemaReg;
	}

	public void setMultiSrcSchemaReg(String[] multiSrcSchemaReg) {
		this.multiSrcSchemaReg = multiSrcSchemaReg;
	}

	public String[] getMultiSrcTableReg() {
		return multiSrcTableReg;
	}

	public void setMultiSrcTableReg(String[] multiSrcTableReg) {
		this.multiSrcTableReg = multiSrcTableReg;
	}

	public String[] getMultiEnableColumnFilter() {
		return multiEnableColumnFilter;
	}

	public void setMultiEnableColumnFilter(String[] multiEnableColumnFilter) {
		this.multiEnableColumnFilter = multiEnableColumnFilter;
	}

	public String[] getMultiColumnFilterString() {
		return multiColumnFilterString;
	}

	public void setMultiColumnFilterString(String[] multiColumnFilterString) {
		this.multiColumnFilterString = multiColumnFilterString;
	}

	public String[] getMultiCompressionType() {
		return multiCompressionType;
	}

	public void setMultiCompressionType(String[] multiCompressionType) {
		this.multiCompressionType = multiCompressionType;
	}

	public boolean[] getMultiColumnFilterAdvEnabled() {
		return multiColumnFilterAdvEnabled;
	}

	public void setMultiColumnFilterAdvEnabled(boolean[] multiColumnFilterAdvEnabled) {
		this.multiColumnFilterAdvEnabled = multiColumnFilterAdvEnabled;
	}

	public String[] getMultiColumnFilterAdv() {
		return multiColumnFilterAdv;
	}

	public void setMultiColumnFilterAdv(String[] multiColumnFilterAdv) {
		this.multiColumnFilterAdv = multiColumnFilterAdv;
	}

	public boolean[] getMultiInsert() {
		return multiInsert;
	}

	public void setMultiInsert(boolean[] multiInsert) {
		this.multiInsert = multiInsert;
	}

	public boolean[] getMultiUpdate() {
		return multiUpdate;
	}

	public void setMultiUpdate(boolean[] multiUpdate) {
		this.multiUpdate = multiUpdate;
	}

	public boolean[] getMultiDelete() {
		return multiDelete;
	}

	public void setMultiDelete(boolean[] multiDelete) {
		this.multiDelete = multiDelete;
	}
}
