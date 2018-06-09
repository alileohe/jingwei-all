package com.taobao.jingwei.common.node.applier;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiUtil;
import com.taobao.jingwei.common.node.type.DBType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * Class DataBaseApplierNode
 *
 * @author qihao <dqylyln@gmail.com>
 * @since 11-11-15
 */
public class DataBaseApplierNode extends AbstractApplierNode {
	/**
	 * 迁移使用的matrixName和动态规则名称相同
	 */
	private String matrixName;

	/**
	 * 规则名称，如果为空则和matrixName相同
	 */
	private String ruleName;

	private DBType dbType = DBType.MYSQL;

	/**
	 * 迁移参数部分
	 */
	private volatile List<String> schemaNames = Collections.emptyList();

	private volatile Map<String/*sourceLogicTable*/, String/*targetLogicTable*/> logicTableNames = Collections
			.emptyMap();
	/**
	 * 列过滤，虽然叫ignoreColumns但是本身含义只是列过滤的名称
	 * 具体过滤方式是包含还是排除取决于对应filterFlags的MAP的BOOLEAN选项
	 */
	private volatile Map<String/* sourceLogicTable */, Set<String>/* columnName */> ignoreColumns = Collections
			.emptyMap();
	private volatile Map<String/* sourceLogicTable */, Boolean/* include:true;eclude:false */> filterFlags = Collections
			.emptyMap();

	private volatile Map<String/*sourceLogicTable*/, Map<String/*SorceColumnName*/, String/*TargeColumnName*/>> columnMapping = Collections
			.emptyMap();
	/**
	 * json存储数据key定义
	 */
	private final static String MATRIX_NAME_KEY = "matrixName";
	private final static String RULE_NAME_KEY = "ruleName";
	private final static String DB_TYPE_KEY = "dbType";
	private final static String SCHEMA_NAMES_KEY = "schemaNames";
	private final static String LOGIC_TABLE_NAMES_KEY = "logicTableNames";
	private final static String IGNORE_COLUMNS_KEY = "ignoreColumns";
	private final static String COLUMNS_MAPPING_KEY = "columnMapping";
	private final static String APPLIER_DATA_KEY = "applierData";
	private final static String REPLACE_KEY = "replace";
	private final static String FAILCONTINUE_KEY = "failContinue";
	private final static String FILTERFLAGS_KEY = "filterFlags";

	public DataBaseApplierNode() {
	}

	public DataBaseApplierNode(String applierData) {
		try {
			this.jsonStringToNodeSelf(applierData);
		} catch (JSONException e) {
			logger.error("new DataBaseApplierNode  paser applierData Error!", e);
		}
	}

	@Override
	protected void specilizeAttributeToJsonObject(JSONObject jsonObject) throws JSONException {
		jsonObject.put(MATRIX_NAME_KEY, StringUtil.defaultIfBlank(this.getMatrixName()));
		jsonObject.put(RULE_NAME_KEY, StringUtil.defaultIfBlank(this.getRuleName()));
		jsonObject.put(DB_TYPE_KEY, this.getDbType().getType());
		jsonObject.put(SCHEMA_NAMES_KEY, this.schemaNames != null ? new JSONArray(this.schemaNames) : new JSONArray());
		JSONObject logicTables = new JSONObject();
		if (null != logicTableNames && !logicTableNames.isEmpty()) {
			for (Map.Entry<String, String> entry : this.logicTableNames.entrySet()) {
				logicTables.put(entry.getKey(), StringUtil.defaultIfBlank(entry.getValue()));
			}
		}
		jsonObject.put(LOGIC_TABLE_NAMES_KEY, logicTables);

		JSONObject ignoreColumn = new JSONObject();
		if (null != ignoreColumns) {
			for (Map.Entry<String, Set<String>> entry : ignoreColumns.entrySet()) {
				String tableName = entry.getKey();
				Set<String> columnsSet = entry.getValue();
				if (null != columnsSet && !columnsSet.isEmpty()) {
					ignoreColumn.put(tableName, new JSONArray(columnsSet));
				}
			}
		}
		jsonObject.put(IGNORE_COLUMNS_KEY, ignoreColumn);
		JSONObject filterFlag = new JSONObject();
		if (null != filterFlags) {
			for (Map.Entry<String, Boolean> entry : filterFlags.entrySet()) {
				filterFlag.put(StringUtil.toLowerCase(entry.getKey()), entry.getValue());
			}
		}
		jsonObject.put(FILTERFLAGS_KEY, filterFlag);
		JSONObject jsonColumns = new JSONObject();
		if (null != this.columnMapping) {
			for (Map.Entry<String, Map<String, String>> tableEntry : this.columnMapping.entrySet()) {
				String tableName = tableEntry.getKey();
				Map<String, String> mapingMap = tableEntry.getValue();
				JSONObject jsonColumMapping = new JSONObject();
				for (Map.Entry<String, String> columnEntry : mapingMap.entrySet()) {
					String columnName = columnEntry.getKey();
					String mappingName = columnEntry.getValue();
					jsonColumMapping.put(columnName, mappingName);
				}
				jsonColumns.put(tableName, jsonColumMapping);
			}
		}
		jsonObject.put(COLUMNS_MAPPING_KEY, jsonColumns);
		jsonObject.put(APPLIER_DATA_KEY, StringUtil.defaultIfBlank(this.getApplierData()));
		jsonObject.put(REPLACE_KEY, this.isReplace());
		jsonObject.put(FAILCONTINUE_KEY, this.isFailContinue());
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void jsonObjectToSpecilizeAttribute(JSONObject jsonObject) throws JSONException {
		this.setMatrixName(jsonObject.getString(MATRIX_NAME_KEY));
		this.setDbType(DBType.getEnumByType(jsonObject.getInt(DB_TYPE_KEY)));
		if (jsonObject.has(RULE_NAME_KEY)) {
			this.setRuleName(jsonObject.getString(RULE_NAME_KEY));
		}

		this.setSchemaNames(JingWeiUtil.<String> jsonArray2List(jsonObject.getJSONArray(SCHEMA_NAMES_KEY)));
		JSONObject logicTableNames = jsonObject.getJSONObject(LOGIC_TABLE_NAMES_KEY);
		if (null != logicTableNames && logicTableNames.length() > 0) {
			Map<String, String> logicTableMap = new HashMap<String, String>(logicTableNames.length());
			Iterator<String> it = logicTableNames.keys();
			for (; it.hasNext();) {
				String key = it.next();
				logicTableMap.put(key, logicTableNames.getString(key));
			}
			this.setLogicTableNames(logicTableMap);
		}
		JSONObject ignoreColumnJson = jsonObject.getJSONObject(IGNORE_COLUMNS_KEY);
		Map<String/* tableName */, Set<String>/* columnName */> ignoreColumnMap = new HashMap<String, Set<String>>();
		if (null != ignoreColumnJson) {
			Iterator<String> it = ignoreColumnJson.keys();
			for (; it.hasNext();) {
				String tableName = it.next();
				Set<String> ignoreColumnNames = JingWeiUtil.<String> jsonArray2Set(ignoreColumnJson
						.getJSONArray(tableName));
				if (!ignoreColumnNames.isEmpty()) {
					ignoreColumnMap.put(tableName, ignoreColumnNames);
				}
			}
			this.setIgnoreColumns(ignoreColumnMap);
		}
		JSONObject jsonColumnMapping = jsonObject.getJSONObject(COLUMNS_MAPPING_KEY);
		if (null != jsonColumnMapping) {
			Map<String, Map<String, String>> columnMapping = new HashMap<String, Map<String, String>>(
					jsonColumnMapping.length());
			Iterator<String> tableIt = jsonColumnMapping.keys();
			for (; tableIt.hasNext();) {
				String tableName = tableIt.next();
				JSONObject columns = jsonColumnMapping.getJSONObject(tableName);
				if (null != columns) {
					Map<String, String> columnMap = new HashMap<String, String>(columns.length());
					Iterator<String> columnIt = columns.keys();
					for (; columnIt.hasNext();) {
						String columnName = columnIt.next();
						String mappingName = columns.getString(columnName);
						columnMap.put(columnName, mappingName);
					}
					columnMapping.put(tableName, columnMap);
				}
			}
			this.columnMapping = columnMapping;
		}

		if (jsonObject.has(FILTERFLAGS_KEY)) {
			JSONObject jsonFilterFlags = jsonObject.getJSONObject(FILTERFLAGS_KEY);
			Map<String, Boolean> flagMap = new HashMap<String, Boolean>(jsonFilterFlags.length());
			Iterator<String> fit = jsonFilterFlags.keys();
			for (; fit.hasNext();) {
				String tableName = StringUtil.toLowerCase(fit.next());
				flagMap.put(tableName, jsonFilterFlags.getBoolean(tableName));
			}
			this.filterFlags = flagMap;
		}
		this.setApplierData(jsonObject.getString(APPLIER_DATA_KEY));
		this.setReplace(jsonObject.getBoolean(REPLACE_KEY));
		this.setFailContinue(jsonObject.getBoolean(FAILCONTINUE_KEY));
	}

	public String getMatrixName() {
		return matrixName;
	}

	public void setMatrixName(String matrixName) {
		this.matrixName = matrixName;
	}

	public List<String> getSchemaNames() {
		return schemaNames;
	}

	public void setSchemaNames(List<String> schemaNames) {
		if (null != schemaNames && !schemaNames.isEmpty()) {
			List<String> lowerCaseList = new ArrayList<String>(schemaNames.size());
			for (String schemaName : schemaNames) {
				lowerCaseList.add(StringUtil.toLowerCase(schemaName));
			}
			this.schemaNames = lowerCaseList;
		}
	}

	public Map<String, String> getLogicTableNames() {
		return logicTableNames;
	}

	public void setLogicTableNames(Map<String, String> logicTableNames) {
		if (null != logicTableNames && !logicTableNames.isEmpty()) {
			Map<String, String> lowerCaseMap = new HashMap<String, String>(logicTableNames.size());
			for (Map.Entry<String, String> entry : logicTableNames.entrySet()) {
				lowerCaseMap.put(StringUtil.toLowerCase(entry.getKey()), StringUtil.toLowerCase(entry.getValue()));
			}
			this.logicTableNames = lowerCaseMap;
		}

	}

	public Map<String, Set<String>> getIgnoreColumns() {
		return ignoreColumns;
	}

	public void setIgnoreColumns(Map<String, Set<String>> ignoreColumns) {
		if (null != ignoreColumns && !ignoreColumns.isEmpty()) {
			Map<String, Set<String>> lowerCaseMap = new HashMap<String, Set<String>>(ignoreColumns.size());
			for (Map.Entry<String, Set<String>> entry : ignoreColumns.entrySet()) {
				String key = StringUtil.toLowerCase(entry.getKey());
				Set<String> value = entry.getValue();
				if (null != value && !value.isEmpty()) {
					Set<String> lowerCaseSet = new HashSet<String>(value.size());
					for (String tmp : value) {
						lowerCaseSet.add(StringUtil.toLowerCase(tmp));
					}
					lowerCaseMap.put(key, lowerCaseSet);
				}
			}
			this.ignoreColumns = lowerCaseMap;
		}
	}

	public Map<String, Map<String, String>> getColumnMapping() {
		return columnMapping;
	}

	public void setColumnMapping(Map<String, Map<String, String>> columnMapping) {
		if (null != columnMapping && !columnMapping.isEmpty()) {
			Map<String, Map<String, String>> mapping = new HashMap<String, Map<String, String>>(columnMapping.size());
			for (Map.Entry<String, Map<String, String>> tableEntry : columnMapping.entrySet()) {
				String tableName = StringUtil.toLowerCase(tableEntry.getKey());
				Map<String, String> columns = tableEntry.getValue();
				if (null != columns && !columns.isEmpty()) {
					Map<String, String> columnsMap = new HashMap<String, String>(columns.size());
					for (Map.Entry<String, String> columnEntry : columns.entrySet()) {
						String columnName = StringUtil.toLowerCase(columnEntry.getKey());
						String mappingName = StringUtil.toLowerCase(columnEntry.getValue());
						if (StringUtil.isNotBlank(columnName) && StringUtil.isNotBlank(mappingName)) {
							columnsMap.put(columnName, mappingName);
						}
					}
					mapping.put(tableName, columnsMap);
				}
			}
			this.columnMapping = mapping;
		}
	}

	public DBType getDbType() {
		return dbType;
	}

	public void setDbType(DBType dbType) {
		this.dbType = dbType;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public Map<String, Boolean> getFilterFlags() {
		return filterFlags;
	}

	public void setFilterFlags(Map<String, Boolean> filterFlags) {
		this.filterFlags = filterFlags;
	}
}