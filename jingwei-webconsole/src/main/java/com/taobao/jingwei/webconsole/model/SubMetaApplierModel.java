package com.taobao.jingwei.webconsole.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.node.applier.EventFilterNode;
import com.taobao.jingwei.common.node.applier.EventFilterNode.ColumnFilterConditionNode;
import com.taobao.jingwei.common.node.applier.SubMetaApplierNode;
import com.taobao.jingwei.webconsole.common.JingweiWebConsoleConstance;
import com.taobao.jingwei.webconsole.model.config.util.ConfigUtil;

/**
 * @desc 给批量修改页面使用，对应SubMetaApplierNode
 * 
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Mar 28, 2013 8:02:50 PM
 * 
 */

public class SubMetaApplierModel {
	private static final Log log = LogFactory.getLog(JingweiWebConsoleConstance.JINGWEI_LOG);

	private String metaTopic;
	private String shardColumn;
	private boolean splitTxEvent;
	private int maxEventSize;

	private long sendTimeOut;
	private String srcSchemaReg;
	private String srcTableReg;

	private boolean enableColumnFilter;
	private String columnFilterString;
	private String compressionType;
	// 是否使用动态代码
	private boolean columnFilterAdvEnabled;
	// 动态字节码
	private String sourceCode;

	private boolean insert;
	private boolean update;
	private boolean delete;

	private String columnFilterSchemaReg;
	private String columnFilterLogicTableName;
	private boolean columnFilterUseInclude;
	// 逗号分隔
	private String columnFilterColums;

	public SubMetaApplierNode getMultiMetaApplierNode() {

		SubMetaApplierNode subMetaApplierNode = new SubMetaApplierNode();

		subMetaApplierNode.setSrcSchemaReg(srcSchemaReg);
		subMetaApplierNode.setSrcTableReg(srcTableReg);
		subMetaApplierNode.setMetaTopic(metaTopic);
		subMetaApplierNode.setShardColumn(shardColumn);
		subMetaApplierNode.setSplitTxEvent(splitTxEvent);
		subMetaApplierNode.setMaxEventSize(maxEventSize);
		subMetaApplierNode.setSendTimeOut(sendTimeOut);
		subMetaApplierNode.setCompressionType(compressionType);
		subMetaApplierNode.setEnableColumnfilter(enableColumnFilter);
		EventFilterNode filter = new EventFilterNode();
		filter.setIncludeInsert(insert);
		filter.setIncludeUpdate(update);
		filter.setIncludeDelete(delete);
		if (columnFilterAdvEnabled) {
			filter.setSourceCode(sourceCode);

		} else {
			if (subMetaApplierNode.isEnableColumnfilter() && StringUtil.isNotBlank(columnFilterString)) {
				Map<String, HashMap<String, EventFilterNode.ColumnFilterConditionNode>> condition = getColumnFilterConditionNode(
						columnFilterSchemaReg, columnFilterLogicTableName, columnFilterUseInclude, columnFilterColums);
				filter.setConditions(condition);
			}
		}
		try {
			subMetaApplierNode.setEventFilterData(filter.toJSONString());
		} catch (JSONException e) {
		}

		return subMetaApplierNode;
	}

	/**
	 * 
	 * @param columnFilterSchemaReg
	 * @param columnFilterLogicTableName
	 * @param columnFilterUseInclude
	 * @param columnFilterColums 逗号分隔
	 * @return
	 */
	private Map<String, HashMap<String, EventFilterNode.ColumnFilterConditionNode>> getColumnFilterConditionNode(
			String columnFilterSchemaReg, String columnFilterLogicTableName, boolean columnFilterUseInclude,
			String columnFilterColums) {
		Map<String, HashMap<String, ColumnFilterConditionNode>> conditions = new HashMap<String, HashMap<String, ColumnFilterConditionNode>>();

		ColumnFilterConditionNode columnFilterConditionNode = new ColumnFilterConditionNode();
		columnFilterConditionNode.setUseIncludeRule(columnFilterUseInclude);

		if (columnFilterUseInclude) {
			columnFilterConditionNode.setIncludeColumns(ConfigUtil.commaSepString2Set(columnFilterColums, true));
		} else {
			columnFilterConditionNode.setExcludeColumns(ConfigUtil.commaSepString2Set(columnFilterColums, true));
		}

		HashMap<String, ColumnFilterConditionNode> tableMap = new HashMap<String, ColumnFilterConditionNode>();
		tableMap.put(columnFilterLogicTableName, columnFilterConditionNode);

		conditions.put(columnFilterSchemaReg, tableMap);

		return conditions;
	}

	public static SubMetaApplierModel getSubMetaApplierModel(SubMetaApplierNode subMetaApplierNode) {
		SubMetaApplierModel subMetaApplierModel = new SubMetaApplierModel();

		subMetaApplierModel.setMetaTopic(subMetaApplierNode.getMetaTopic());
		subMetaApplierModel.setShardColumn(subMetaApplierNode.getShardColumn());
		subMetaApplierModel.setCompressionType(subMetaApplierNode.getCompressionType());
		subMetaApplierModel.setMaxEventSize(subMetaApplierNode.getMaxEventSize());
		subMetaApplierModel.setSendTimeOut(subMetaApplierNode.getSendTimeOut());
		subMetaApplierModel.setSrcSchemaReg(subMetaApplierNode.getSrcSchemaReg());
		subMetaApplierModel.setSrcTableReg(subMetaApplierNode.getSrcTableReg());

		return null;
	}

	public String getMetaTopic() {
		return metaTopic;
	}

	public void setMetaTopic(String metaTopic) {
		this.metaTopic = metaTopic;
	}

	public String getShardColumn() {
		return shardColumn;
	}

	public void setShardColumn(String shardColumn) {
		this.shardColumn = shardColumn;
	}

	public boolean isSplitTxEvent() {
		return splitTxEvent;
	}

	public void setSplitTxEvent(boolean splitTxEvent) {
		this.splitTxEvent = splitTxEvent;
	}

	public int getMaxEventSize() {
		return maxEventSize;
	}

	public void setMaxEventSize(int maxEventSize) {
		this.maxEventSize = maxEventSize;
	}

	public long getSendTimeOut() {
		return sendTimeOut;
	}

	public void setSendTimeOut(long sendTimeOut) {
		this.sendTimeOut = sendTimeOut;
	}

	public String getSrcSchemaReg() {
		return srcSchemaReg;
	}

	public void setSrcSchemaReg(String srcSchemaReg) {
		this.srcSchemaReg = srcSchemaReg;
	}

	public String getSrcTableReg() {
		return srcTableReg;
	}

	public void setSrcTableReg(String srcTableReg) {
		this.srcTableReg = srcTableReg;
	}

	public boolean isEnableColumnFilter() {
		return enableColumnFilter;
	}

	public void setEnableColumnFilter(boolean enableColumnFilter) {
		this.enableColumnFilter = enableColumnFilter;
	}

	public String getColumnFilterString() {
		return columnFilterString;
	}

	public void setColumnFilterString(String columnFilterString) {
		this.columnFilterString = columnFilterString;
	}

	public String getCompressionType() {
		return compressionType;
	}

	public void setCompressionType(String compressionType) {
		this.compressionType = compressionType;
	}

	public boolean isColumnFilterAdvEnabled() {
		return columnFilterAdvEnabled;
	}

	public void setColumnFilterAdvEnabled(boolean columnFilterAdvEnabled) {
		this.columnFilterAdvEnabled = columnFilterAdvEnabled;
	}

	public String getSourceCode() {
		return sourceCode;
	}

	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
	}

	public boolean isInsert() {
		return insert;
	}

	public void setInsert(boolean insert) {
		this.insert = insert;
	}

	public boolean isUpdate() {
		return update;
	}

	public void setUpdate(boolean update) {
		this.update = update;
	}

	public boolean isDelete() {
		return delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}

	public String getColumnFilterSchemaReg() {
		return columnFilterSchemaReg;
	}

	public void setColumnFilterSchemaReg(String columnFilterSchemaReg) {
		this.columnFilterSchemaReg = columnFilterSchemaReg;
	}

	public String getColumnFilterLogicTableName() {
		return columnFilterLogicTableName;
	}

	public void setColumnFilterLogicTableName(String columnFilterLogicTableName) {
		this.columnFilterLogicTableName = columnFilterLogicTableName;
	}

	public boolean isColumnFilterUseInclude() {
		return columnFilterUseInclude;
	}

	public void setColumnFilterUseInclude(boolean columnFilterUseInclude) {
		this.columnFilterUseInclude = columnFilterUseInclude;
	}

	public String getColumnFilterColums() {
		return columnFilterColums;
	}

	public void setColumnFilterColums(String columnFilterColums) {
		this.columnFilterColums = columnFilterColums;
	}
}
