package com.taobao.jingwei.core.util;

import com.taobao.jingwei.common.node.applier.EventFilterNode;
import com.taobao.jingwei.common.node.applier.EventFilterNode.ColumnFilterConditionNode;
import com.taobao.jingwei.core.util.DBMSEventFileFilter.FilterCondition;
import com.taobao.tddl.dbsync.dbms.DBMSRowChange;
import com.taobao.tddl.dbsync.dbms.DefaultRowChange;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @desc 
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Mar 23, 2012 2:21:08 PM
 */

public class EventFilterNodeTest implements ColumnFilterTestConstant {

	public static void main(String[] args) throws Exception {

		Map<String/*schema*/, HashMap<String/* sourceLogicTable */, ColumnFilterConditionNode /*condition node*/>> conditions = new HashMap<String, HashMap<String, ColumnFilterConditionNode>>();

		ColumnFilterConditionNode conNode = new ColumnFilterConditionNode();

		Set<String> includeColumns = new HashSet<String>();
		includeColumns.add(INCLUDE_COLUMN_1);
		includeColumns.add(INCLUDE_COLUMN_2);

		Set<String> excludeColumns = new HashSet<String>();
		excludeColumns.add(EXCLUDE_COLUMN_1);
		excludeColumns.add(EXCLUDE_COLUMN_2);

		conNode.setIncludeColumns(includeColumns);
		conNode.setExcludeColumns(excludeColumns);
		conNode.setUseIncludeRule(true);

		HashMap<String, ColumnFilterConditionNode> tableContition = new HashMap<String, EventFilterNode.ColumnFilterConditionNode>();

		tableContition.put(TABLE_1, conNode);
		tableContition.put(TABLE_2, conNode);

		conditions.put(SCHEMA_REG_1, tableContition);
		conditions.put(SCHEMA_2, tableContition);

		EventFilterNode filterNode = new EventFilterNode();
		filterNode.setConditions(conditions);

		String conString = filterNode.toJSONString();

		EventFilterNode node = new EventFilterNode();
		node.jsonStringToNodeSelf(conString);

		FilterCondition condition = new FilterCondition();
		condition.setFilterContion(node.getConditions());

		DBMSEventFileFilter filter = new DBMSEventFileFilter(condition);

		DBMSRowChange dbmsRowChange = new DefaultRowChange(null, "SCHEMA_REG1", TABLE_1, null);

		System.out.println(node);
	}

}
