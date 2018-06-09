package com.taobao.jingwei.webconsole.model.config.applier;

import java.io.Serializable;
import java.util.Map;

/**
 * @desc 
 * 
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Jan 5, 2013 7:05:09 PM
 */

public class TableMapConfig implements Cloneable, Serializable {
	private static final long serialVersionUID = 6358064285840537435L;
	private String sourceTable;
	private String targetTable;
	private Map<String, String> columnMap;
	// 逗号分隔的列名
	private String filterColumns;
	private boolean filterUseInclude;

	public String getSourceTable() {
		return sourceTable;
	}

	public void setSourceTable(String sourceTable) {
		this.sourceTable = sourceTable;
	}

	public String getTargetTable() {
		return targetTable;
	}

	public void setTargetTable(String targetTable) {
		this.targetTable = targetTable;
	}

	public String getFilterColumns() {
		return filterColumns;
	}

	public void setFilterColumns(String filterColumns) {
		this.filterColumns = filterColumns;
	}

	public boolean isFilterUseInclude() {
		return filterUseInclude;
	}

	public void setFilterUseInclude(boolean filterUseInclude) {
		this.filterUseInclude = filterUseInclude;
	}

	public Map<String, String> getColumnMap() {
		return columnMap;
	}

	public void setColumnMap(Map<String, String> columnMap) {
		this.columnMap = columnMap;
	}

}