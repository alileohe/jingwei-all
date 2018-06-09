package com.taobao.jingwei.webconsole.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.node.applier.EventFilterNode;
import com.taobao.jingwei.common.node.applier.EventFilterNode.ColumnFilterConditionNode;

public class JingweiModelHelper {
	public static Map<String, HashMap<String, ColumnFilterConditionNode>> columnFilterStringToJson(String filterString)
			throws JSONException {
		if (StringUtil.isBlank(filterString)) {
			return null;
		}

		Map<String, HashMap<String, ColumnFilterConditionNode>> conditions = new HashMap<String, HashMap<String, ColumnFilterConditionNode>>();
		String[] columns = StringUtil.split(filterString, ";");
		for (int i = 0; i < columns.length; i++) {
			String column = columns[i];
			String[] type = StringUtil.split(column, ":");
			String[] stc = StringUtil.split(type[1], ".");
			HashMap<String, ColumnFilterConditionNode> table = conditions.get(stc[0]);
			if (table == null) {
				table = new HashMap<String, EventFilterNode.ColumnFilterConditionNode>();
				conditions.put(stc[0], table);
			}
			ColumnFilterConditionNode columnFilter = table.get(stc[1]);
			if (columnFilter == null) {
				columnFilter = new ColumnFilterConditionNode();
				table.put(stc[1], columnFilter);
			}
			boolean useIncludeRule = "include".equalsIgnoreCase(type[0]);
			columnFilter.setUseIncludeRule(useIncludeRule);
			if (useIncludeRule) {
				Set<String> set = columnFilter.getIncludeColumns();
				if (set.isEmpty()) {
					set = new HashSet<String>();
					columnFilter.setIncludeColumns(set);
				}
				set.add(stc[2]);
			} else {
				Set<String> set = columnFilter.getExcludeColumns();
				if (set.isEmpty()) {
					set = new HashSet<String>();
					columnFilter.setExcludeColumns(set);
				}
				set.add(stc[2]);
			}
		}
		return conditions;
	}

	public static List<String> parseColumnFilter(String columnFilterData) {
		List<String> list = new ArrayList<String>();
		EventFilterNode filter = new EventFilterNode(columnFilterData);
		Map<String, HashMap<String, ColumnFilterConditionNode>> conditions = filter.getConditions();
		for (Map.Entry<String, HashMap<String, ColumnFilterConditionNode>> schemas : conditions.entrySet()) {
			for (Map.Entry<String, ColumnFilterConditionNode> tables : schemas.getValue().entrySet()) {
				for (String include : tables.getValue().getIncludeColumns()) {
					list.add("include:" + schemas.getKey() + "." + tables.getKey() + "." + include);
				}

				for (String exclude : tables.getValue().getExcludeColumns()) {
					list.add("exclude:" + schemas.getKey() + "." + tables.getKey() + "." + exclude);
				}
			}
		}
		return list;
	}
}
