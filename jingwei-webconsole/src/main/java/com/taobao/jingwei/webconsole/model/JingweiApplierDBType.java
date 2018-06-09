package com.taobao.jingwei.webconsole.model;

import com.alibaba.common.lang.ArrayUtil;
import com.alibaba.common.lang.StringUtil;

public class JingweiApplierDBType {
	private String matrixName;
	private String ruleName;
	private int dbType;
	private boolean replace;
	private boolean failContinue = true;
	private String db;
	private String tableMapping;
	private String columnMapping;
	/**
	 * 不要直译，详见DataBaseApplierNode.
	 * 
	 * @see DataBaseApplierNode
	 */
	private String ignoreTableList;

	public String getMatrixName() {
		return matrixName;
	}

	public void setMatrixName(String matrixName) {
		this.matrixName = matrixName;
	}

	public int getDbType() {
		return dbType;
	}

	public void setDbType(int dbType) {
		this.dbType = dbType;
	}

	public boolean isReplace() {
		return replace;
	}

	public void setReplace(boolean replace) {
		this.replace = replace;
	}

	public boolean isFailContinue() {
		return failContinue;
	}

	public void setFailContinue(boolean failContinue) {
		this.failContinue = failContinue;
	}

	public String getDb() {
		return db;
	}

	public void setDb(String db) {
		this.db = db;
	}

	public String getTableMapping() {
		return tableMapping;
	}

	public String[][] getTableMappings() {
		if (StringUtil.isBlank(tableMapping)) {
			return new String[0][0];
		}
		String[] tables = StringUtil.split(tableMapping, "|");
		String[][] str = new String[tables.length][];
		for (int i = 0; i < tables.length; i++) {
			str[i] = StringUtil.split(tables[i], " -> ");
		}
		return str;
	}

	public void setTableMapping(String tableMapping) {
		this.tableMapping = tableMapping;
	}

	public String getColumnMapping() {
		return columnMapping;
	}

	public String[] getColumnMappings() {
		if (StringUtil.isBlank(columnMapping)) {
			return ArrayUtil.EMPTY_STRING_ARRAY;
		}
		return StringUtil.split(columnMapping, "|");
	}

	public void setColumnMapping(String columnMapping) {
		this.columnMapping = columnMapping;
	}

	public String getIgnoreTableList() {
		return ignoreTableList;
	}

	public String[] getIgnoreTableLists() {
		if (StringUtil.isBlank(ignoreTableList)) {
			return ArrayUtil.EMPTY_STRING_ARRAY;
		}
		return StringUtil.split(ignoreTableList, "|");
	}

	public void setIgnoreTableList(String ignoreTableList) {
		this.ignoreTableList = ignoreTableList;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}
}
