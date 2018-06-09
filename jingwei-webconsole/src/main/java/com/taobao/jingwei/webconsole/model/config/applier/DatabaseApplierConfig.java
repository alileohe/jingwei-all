package com.taobao.jingwei.webconsole.model.config.applier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jodd.util.StringUtil;

import com.taobao.jingwei.common.node.applier.AbstractApplierNode;
import com.taobao.jingwei.common.node.applier.DataBaseApplierNode;
import com.taobao.jingwei.common.node.type.ApplierType;
import com.taobao.jingwei.common.node.type.DBType;

/**
 * @desc 
 * 
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Dec 20, 2012 4:59:39 PM
 */

public class DatabaseApplierConfig extends ApplierConfig {

	private static final long serialVersionUID = 1L;

	/**迁移使用的matrixName和动态规则名称相同	 */
	private String matrixName;

	/** 规则名称，如果为空则和matrixName相同 */
	private String ruleName;

	private String dbType;

	private boolean replace;

	private boolean failContinue;

	private String tableMapConfigString;

	private List<TableMapConfig> tableMapConfigs;

	public String getMatrixName() {
		return matrixName;
	}

	public void setMatrixName(String matrixName) {
		this.matrixName = matrixName;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public String getDbType() {
		return dbType;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	public List<TableMapConfig> getTableMapConfigs() {
		return tableMapConfigs;
	}

	public void setTableMapConfigs(List<TableMapConfig> tableMapConfigs) {
		this.tableMapConfigs = tableMapConfigs;
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

	public String getTableMapConfigString() {
		return tableMapConfigString;
	}

	public void setTableMapConfigString(String tableMapConfigString) {
		this.tableMapConfigString = tableMapConfigString;
	}

	@Override
	public AbstractApplierNode getApplierNode() {
		DataBaseApplierNode node = new DataBaseApplierNode();

		// dbtype
		String dbType = this.getDbType();

		if (dbType.equalsIgnoreCase("mysql")) {
			node.setDbType(DBType.MYSQL);
		} else if (dbType.equalsIgnoreCase("oracle")) {
			node.setDbType(DBType.ORACLE);
		} else if (dbType.equalsIgnoreCase("andor")) {
			node.setDbType(DBType.ANDOR);
		}

		node.setMatrixName(this.getMatrixName());
		node.setFailContinue(this.isFailContinue());
		node.setApplierData(this.getMatrixName());
		node.setReplace(this.isReplace());
		node.setRuleName(this.getRuleName());

		node.setColumnMapping(this.getColumnMappings());
		node.setLogicTableNames(this.getLogicTableNames());
		node.setFilterFlags(this.getFilterFlags());
		node.setIgnoreColumns(this.getFilterColumns());

		return node;
	}

	/**
	 *  获取所有表的列映射
	 * @param databaseApplierConfig
	 * @return
	 */
	private Map<String, Map<String, String>> getColumnMappings() {
		Map<String, Map<String, String>> columnMapping = new HashMap<String, Map<String, String>>();

		List<TableMapConfig> tableMapConfigList = this.getTableMapConfigs();

		for (TableMapConfig tableMapConfig : tableMapConfigList) {
			columnMapping.put(tableMapConfig.getSourceTable(), tableMapConfig.getColumnMap());
		}

		return columnMapping;
	}

	/**
	 *  获取所有表的列映射
	 * @param databaseApplierConfig
	 * @return
	 */
	private Map<String, String> getLogicTableNames() {
		Map<String, String> tableNameMapping = new HashMap<String, String>();

		List<TableMapConfig> tableMapConfigList = this.getTableMapConfigs();

		for (TableMapConfig tableMapConfig : tableMapConfigList) {
			tableNameMapping.put(tableMapConfig.getSourceTable().toLowerCase(), tableMapConfig.getTargetTable()
					.toLowerCase());
		}

		return tableNameMapping;
	}

	/**
	 *  获取所有表的列映射
	 * @param databaseApplierConfig
	 * @return
	 */
	private Map<String, Boolean> getFilterFlags() {
		Map<String, Boolean> filterFlags = new HashMap<String, Boolean>();

		List<TableMapConfig> tableMapConfigList = this.getTableMapConfigs();

		for (TableMapConfig tableMapConfig : tableMapConfigList) {
			filterFlags.put(tableMapConfig.getSourceTable(), tableMapConfig.isFilterUseInclude());
		}

		return filterFlags;
	}

	/**
	 *  获取所有表的列映射
	 * @param databaseApplierConfig
	 * @return
	 */
	private Map<String, Set<String>> getFilterColumns() {
		Map<String, Set<String>> filterColumns = new HashMap<String, Set<String>>();

		List<TableMapConfig> tableMapConfigList = this.getTableMapConfigs();

		for (TableMapConfig tableMapConfig : tableMapConfigList) {
			Set<String> columnSet = new HashSet<String>();

			if (StringUtil.isBlank(tableMapConfig.getFilterColumns())) {
				continue;
			}
			
			String[] columns = tableMapConfig.getFilterColumns().split(",");

			for (String column : columns) {
				columnSet.add(column.toLowerCase());
			}
			
			if (!columnSet.isEmpty()) {
				filterColumns.put(tableMapConfig.getSourceTable(), columnSet);
			}
			
		}

		return filterColumns;
	}

	@Override
	public ApplierType getApplierType() {

		return ApplierType.DATABASE_APPLIER;
	}
}
