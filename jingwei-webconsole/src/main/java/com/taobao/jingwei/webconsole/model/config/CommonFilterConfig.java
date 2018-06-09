package com.taobao.jingwei.webconsole.model.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.node.applier.EventFilterNode;
import com.taobao.jingwei.common.node.applier.EventFilterNode.ColumnFilterConditionNode;
import com.taobao.jingwei.webconsole.model.config.exception.BatchConfigException;
import com.taobao.jingwei.webconsole.model.config.util.ConfigUtil;

/**
 * @desc 提供Node和Config的互相转换方法
 * 
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Dec 21, 2012 6:53:05 PM
 */

public class CommonFilterConfig implements Cloneable, Serializable, JingWeiConstants {
	private static Log log = LogFactory.getLog(CommonFilterConfig.class);
	private static final long serialVersionUID = -7861826518223619759L;

	// 要过滤的schema，默认所有
	private String srcSchemaReg = ".*";

	// 要过滤的table，默认所有
	private String srcTableReg = ".*";

	private boolean includeInsert = true;
	private boolean includeUpdate = true;
	private boolean includeDelete = true;

	// schemaReg.logicTable.column
	// 一行拼成一个条目 ,对应结构map<string, map<string, Set<string>>>
	private String columnFilterCondition;

	private boolean columnFilterUseInclude;

	// 使用列过滤
	private boolean enableColumnFilter;

	// 使用动态代码
	private boolean enableColumnFilterAdv;

	private String columnFilterDynaCode;

	private static final String COMMA = ".";

	public CommonFilterConfig() {

	}

	public CommonFilterConfig(EventFilterNode eventNode, String srcSchemaReg, String srcTableReg) {
		this.srcSchemaReg = srcSchemaReg;
		this.srcTableReg = srcTableReg;

		this.includeInsert = eventNode.getIncludeInsert();
		this.includeUpdate = eventNode.getIncludeUpdate();
		this.includeDelete = eventNode.getIncludeDelete();

		String sourceCode = eventNode.getSourceCode();
		if (StringUtil.isNotBlank(sourceCode)) {
			this.setEnableColumnFilterAdv(true);
			this.setColumnFilterDynaCode(eventNode.getSourceCode());
		} else {
			this.setEnableColumnFilterAdv(false);
		}

		Map<String, HashMap<String, ColumnFilterConditionNode>> schemaMap = eventNode.getConditions();
		if (schemaMap == null || schemaMap.isEmpty()) {
			return;
		}

		StringBuilder sb = new StringBuilder();
		for (String schemaReg : schemaMap.keySet()) {
			HashMap<String, ColumnFilterConditionNode> tableMap = schemaMap.get(schemaReg);
			for (String logicTable : tableMap.keySet()) {
				ColumnFilterConditionNode columnFilterConditionNode = tableMap.get(logicTable);

				// 多次赋值,源于结构不合理,历史原因
				this.columnFilterUseInclude = columnFilterConditionNode.isUseIncludeRule();

				Set<String> columns = Collections.emptySet();
				if (columnFilterConditionNode.isUseIncludeRule()) {
					columns = columnFilterConditionNode.getIncludeColumns();
				} else {
					columns = columnFilterConditionNode.getExcludeColumns();
				}

				for (String column : columns) {
					sb.append(schemaReg).append(COMMA).append(logicTable).append(COMMA).append(column).append(LINE_SEP);
				}
			}
		}

		this.columnFilterCondition = sb.toString();
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

	public boolean isIncludeInsert() {
		return includeInsert;
	}

	public void setIncludeInsert(boolean includeInsert) {
		this.includeInsert = includeInsert;
	}

	public boolean isIncludeUpdate() {
		return includeUpdate;
	}

	public void setIncludeUpdate(boolean includeUpdate) {
		this.includeUpdate = includeUpdate;
	}

	public boolean isIncludeDelete() {
		return includeDelete;
	}

	public void setIncludeDelete(boolean includeDelete) {
		this.includeDelete = includeDelete;
	}

	public boolean isColumnFilterUseInclude() {
		return columnFilterUseInclude;
	}

	public void setColumnFilterUseInclude(boolean columnFilterUseInclude) {
		this.columnFilterUseInclude = columnFilterUseInclude;
	}

	public String getColumnFilterDynaCode() {
		return columnFilterDynaCode;
	}

	public void setColumnFilterDynaCode(String columnFilterDynaCode) {
		this.columnFilterDynaCode = columnFilterDynaCode;
	}

	public boolean isEnableColumnFilterAdv() {
		return enableColumnFilterAdv;
	}

	public void setEnableColumnFilterAdv(boolean enableColumnFilterAdv) {
		this.enableColumnFilterAdv = enableColumnFilterAdv;
	}

	public String getColumnFilterCondition() {
		return columnFilterCondition;
	}

	public void setColumnFilterCondition(String columnFilterCondition) {
		this.columnFilterCondition = columnFilterCondition;
	}

	public boolean isEnableColumnFilter() {
		return enableColumnFilter;
	}

	public void setEnableColumnFilter(boolean enableColumnFilter) {
		this.enableColumnFilter = enableColumnFilter;
	}

	public Object clone() {
		CommonFilterConfig o = null;
		try {
			o = (CommonFilterConfig) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return o;
	}

	public EventFilterNode getEventFilterNode() throws BatchConfigException {

		EventFilterNode eventNode = new EventFilterNode();
		eventNode.setIncludeInsert(this.isIncludeInsert());
		eventNode.setIncludeUpdate(this.isIncludeUpdate());
		eventNode.setIncludeDelete(this.isIncludeDelete());

		String sourceCode = this.getColumnFilterDynaCode();
		if (enableColumnFilterAdv) {
			eventNode.setSourceCode(sourceCode);
		} else {
			if (enableColumnFilter) {
				Map<String, HashMap<String, EventFilterNode.ColumnFilterConditionNode>> condition = this.getCondition();

				eventNode.setConditions(condition);
			}
		}

		return eventNode;
	}

	private Map<String, HashMap<String, ColumnFilterConditionNode>> getCondition() throws BatchConfigException {
		Map<String, HashMap<String, ColumnFilterConditionNode>> condition = new HashMap<String, HashMap<String, ColumnFilterConditionNode>>();

		InputStream is = ConfigUtil.string2InputStream(columnFilterCondition);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = StringUtil.EMPTY_STRING;
		try {

			while ((line = br.readLine()) != null && (StringUtil.isNotBlank(line))) {
				String[] entry = line.split("\\.");
				if (entry.length != 3) {
					throw new BatchConfigException("column filter config error");
				}
				String schemaReg = entry[0];
				if (!condition.containsKey(schemaReg)) {
					condition.put(schemaReg, new HashMap<String, ColumnFilterConditionNode>());
				}

				HashMap<String, ColumnFilterConditionNode> tablesMapColumns = condition.get(schemaReg);
				String logicTable = entry[1];
				if (!tablesMapColumns.containsKey(logicTable)) {
					tablesMapColumns.put(logicTable, new ColumnFilterConditionNode());
				}

				String column = entry[2];
				ColumnFilterConditionNode columnFilterConditionNode = tablesMapColumns.get(logicTable);
				if (this.columnFilterUseInclude) {

					if (columnFilterConditionNode.getIncludeColumns() == null
							|| columnFilterConditionNode.getIncludeColumns().isEmpty()) {
						columnFilterConditionNode.setIncludeColumns(new HashSet<String>());
					}

					columnFilterConditionNode.getIncludeColumns().add(column);
				} else {
					if (columnFilterConditionNode.getExcludeColumns() == null
							|| columnFilterConditionNode.getExcludeColumns().isEmpty()) {
						columnFilterConditionNode.setExcludeColumns(new HashSet<String>());
					}

					columnFilterConditionNode.getExcludeColumns().add(column);
				}
			}
		} catch (IOException e) {
			log.error(e);
		}

		return condition;
	}
}
