package com.taobao.jingwei.common.node.applier;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiUtil;
import com.taobao.jingwei.common.node.AbstractNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * @desc 
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Mar 22, 2012 5:02:44 PM
 */

public class EventFilterNode extends AbstractNode {

	private volatile Map<String/*schema*/, HashMap<String/* sourceLogicTable */, ColumnFilterConditionNode /*condition node*/>> conditions = Collections
			.emptyMap();

	private Boolean includeInsert = Boolean.TRUE;
	private Boolean includeUpdate = Boolean.TRUE;
	private Boolean includeDelete = Boolean.TRUE;

	private String sourceCode = StringUtil.EMPTY_STRING;

	/** �ֶι����ַ��� */
	public static final String EVENT_FILTER = "eventFilter";
	private static final String INCLUDE_INSERT = "ENABLE_INSERT";
	private static final String INCLUDE_UPDATE = "ENABLE_UPDATE";
	private static final String INCLUDE_DELETE = "ENABLE_DELETE";
	private static final String SOURCE_CODE_KEY = "sourceCode";

	public EventFilterNode() {

	}

	public EventFilterNode(String jsonStr) {
		try {
			this.jsonStringToNodeSelf(jsonStr);
		} catch (JSONException e) {
			logger.error("new EventFilterNode  paser  Error!", e);
		}
	}

	@Override
	public boolean isPersistent() {
		return true;
	}

	@Override
	public String getDataIdOrNodePath() {
		return StringUtil.EMPTY_STRING;
	}

	@Override
	protected void specilizeAttributeToJsonObject(JSONObject jsonObject) throws JSONException {

		// ����INSERT
		jsonObject.put(INCLUDE_INSERT, this.getIncludeInsert());

		// ����update
		jsonObject.put(INCLUDE_UPDATE, this.getIncludeUpdate());

		// ����delete
		jsonObject.put(INCLUDE_DELETE, this.getIncludeDelete());

		jsonObject.put(SOURCE_CODE_KEY, this.getSourceCode());

		// �ֶι���
		JSONObject conditionJsonObject = new JSONObject();
		for (String schema : conditions.keySet()) {

			JSONObject schemaJsonObject = new JSONObject();
			for (String table : conditions.get(schema).keySet()) {
				schemaJsonObject.put(table, conditions.get(schema).get(table).toJSONString());
			}

			conditionJsonObject.put(schema, schemaJsonObject);
		}

		jsonObject.put(EVENT_FILTER, conditionJsonObject);
	}

	@Override
	protected void jsonObjectToSpecilizeAttribute(JSONObject jsonObject) throws JSONException {
		// �ֶι���
		JSONObject jsonConditions = jsonObject.getJSONObject(EVENT_FILTER);

		this.conditions = new HashMap<String/*schema*/, HashMap<String/* sourceLogicTable */, ColumnFilterConditionNode /*condition node*/>>();
		@SuppressWarnings("unchecked")
		Iterator<String> schemaIterator = jsonConditions.keys();
		while (schemaIterator.hasNext()) {
			String schema = schemaIterator.next();

			JSONObject tables = jsonConditions.getJSONObject(schema);

			HashMap<String, ColumnFilterConditionNode> tableConditions = new HashMap<String, EventFilterNode.ColumnFilterConditionNode>();
			@SuppressWarnings("unchecked")
			Iterator<String> tableIterator = tables.keys();
			while (tableIterator.hasNext()) {
				String table = tableIterator.next();

				String condition = tables.getString(table);

				ColumnFilterConditionNode conditionNode = new ColumnFilterConditionNode();
				conditionNode.jsonStringToNodeSelf(condition);
				conditionNode.getIncludeColumns();
				conditionNode.getExcludeColumns();
				conditionNode.isUseIncludeRule();

				tableConditions.put(table, conditionNode);
			}

			this.conditions.put(schema, tableConditions);
		}

		// ���͹���
		if (jsonObject.has(INCLUDE_INSERT)) {
			this.setIncludeInsert(jsonObject.getBoolean(INCLUDE_INSERT));
		}
		if (jsonObject.has(INCLUDE_UPDATE)) {
			this.setIncludeUpdate(jsonObject.getBoolean(INCLUDE_UPDATE));
		}
		if (jsonObject.has(INCLUDE_DELETE)) {
			this.setIncludeDelete(jsonObject.getBoolean(INCLUDE_DELETE));
		}
		if (jsonObject.has(SOURCE_CODE_KEY)) {
			this.setSourceCode(jsonObject.getString(SOURCE_CODE_KEY));
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String schema : this.conditions.keySet()) {
			sb.append(" schema : ").append(schema).append("\n");
			HashMap<String, ColumnFilterConditionNode> tableMap = this.conditions.get(schema);
			for (String table : tableMap.keySet()) {
				sb.append(" table : ").append(table).append("\n");
				sb.append(" condictionNode : ").append(this.conditions.get(schema).get(table)).append("\n");
			}
		}

		return sb.toString();
	}

	public static class ColumnFilterConditionNode extends AbstractNode {
		/** ��������*/
		private Set<String> includeColumns = Collections.<String> emptySet();

		/** �ų�����*/
		private Set<String> excludeColumns = Collections.<String> emptySet();

		/** Ĭ��ʹ�ð������� ��false��ʾʹ��ʹ���ų���  */
		private boolean useIncludeRule = true;

		private static final String INCLUDE_COLUMNS = "includeColumns";
		private static final String EXCLUDE_COLUMNS = "excludeColumns";
		private static final String USE_INCLUDE_RULE = "useIncludeRule";

		@Override
		public boolean isPersistent() {
			return true;
		}

		@Override
		public String getDataIdOrNodePath() {
			return null;
		}

		@Override
		protected void specilizeAttributeToJsonObject(JSONObject jsonObject) throws JSONException {
			jsonObject.put(INCLUDE_COLUMNS, this.includeColumns != null ? new JSONArray(this.includeColumns)
					: new JSONArray());
			jsonObject.put(EXCLUDE_COLUMNS, this.excludeColumns != null ? new JSONArray(this.excludeColumns)
					: new JSONArray());
			jsonObject.put(USE_INCLUDE_RULE, this.isUseIncludeRule());
		}

		@Override
		protected void jsonObjectToSpecilizeAttribute(JSONObject jsonObject) throws JSONException {
			this.setIncludeColumns(JingWeiUtil.<String> jsonArray2Set(jsonObject.getJSONArray(INCLUDE_COLUMNS)));
			this.setExcludeColumns(JingWeiUtil.<String> jsonArray2Set(jsonObject.getJSONArray(EXCLUDE_COLUMNS)));
			this.setUseIncludeRule(jsonObject.getBoolean(USE_INCLUDE_RULE));
		}

		public Set<String> getIncludeColumns() {
			return includeColumns;
		}

		public void setIncludeColumns(Set<String> includeColumns) {
			this.includeColumns = includeColumns;
		}

		public Set<String> getExcludeColumns() {
			return excludeColumns;
		}

		public void setExcludeColumns(Set<String> excludeColumns) {
			this.excludeColumns = excludeColumns;
		}

		public boolean isUseIncludeRule() {
			return useIncludeRule;
		}

		public void setUseIncludeRule(boolean useIncludeRule) {
			this.useIncludeRule = useIncludeRule;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(" includeColumns : ");
			sb.append(includeColumns.toString()).append("\n");
			sb.append(" excludeColumns : ");
			sb.append(excludeColumns.toString()).append("\n");
			sb.append(" useIncludeRule : ");
			sb.append(useIncludeRule).append("\n");

			return sb.toString();
		}

	}

	public Map<String, HashMap<String, ColumnFilterConditionNode>> getConditions() {
		return conditions;
	}

	public void setConditions(Map<String, HashMap<String, ColumnFilterConditionNode>> conditions) {
		this.conditions = conditions;
	}

	public Boolean getIncludeInsert() {
		return includeInsert;
	}

	public void setIncludeInsert(Boolean includeInsert) {
		this.includeInsert = includeInsert;
	}

	public Boolean getIncludeUpdate() {
		return includeUpdate;
	}

	public void setIncludeUpdate(Boolean includeUpdate) {
		this.includeUpdate = includeUpdate;
	}

	public Boolean getIncludeDelete() {
		return includeDelete;
	}

	public void setIncludeDelete(Boolean includeDelete) {
		this.includeDelete = includeDelete;
	}

	public String getSourceCode() {
		return sourceCode;
	}

	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
	}
}
