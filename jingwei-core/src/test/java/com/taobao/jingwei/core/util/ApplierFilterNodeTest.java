package com.taobao.jingwei.core.util;

import com.taobao.jingwei.common.node.applier.EventFilterNode;
import com.taobao.jingwei.common.node.applier.EventFilterNode.ColumnFilterConditionNode;
import com.taobao.jingwei.core.util.DBMSEventFileFilter.FilterCondition;
import com.taobao.tddl.dbsync.dbms.DBMSAction;
import com.taobao.tddl.dbsync.dbms.DBMSRowChange;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @desc 
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Apr 18, 2012 2:18:30 PM
 */

public class ApplierFilterNodeTest implements ColumnFilterTestConstant {

	private DBMSEventFileFilter filter;

	@Before
	public void init() {

	}

	/**
	 * ���԰���delete
	 * @throws Exception 
	 */
	@Test
	public void testIncludeDelete() throws Exception {
		// ����
		DBMSRowChange deleteRowChange = DBMSRowChangeHelper.getDefaultRowChange(DBMSAction.DELETE);

		// ��������
		EventFilterNode filterNode = getEventFilterNode();
		filterNode.setIncludeDelete(true);

		FilterCondition condition = new FilterCondition(filterNode);
		filter = new DBMSEventFileFilter(condition);

		DBMSRowChange newRowChange = filter.convert(deleteRowChange);
		assertNotNull(newRowChange);
	}

	@Test
	public void testIncludeUpdate() throws Exception {
		// ����
		DBMSRowChange rowChange = DBMSRowChangeHelper.getDefaultRowChange(DBMSAction.UPDATE);

		// ��������
		EventFilterNode filterNode = getEventFilterNode();
		//filterNode.setIncludeUpdate(true);

		FilterCondition condition = new FilterCondition(filterNode);
		filter = new DBMSEventFileFilter(condition);

		DBMSRowChange newRowChange = filter.convert(rowChange);
		assertNotNull(newRowChange);
	}

	@Test
	public void testIncludeInsert() throws Exception {
		// ����
		DBMSRowChange rowChange = DBMSRowChangeHelper.getDefaultRowChange(DBMSAction.INSERT);

		// ��������
		EventFilterNode filterNode = getEventFilterNode();
		//filterNode.setIncludeUpdate(true);

		FilterCondition condition = new FilterCondition(filterNode);
		filter = new DBMSEventFileFilter(condition);

		DBMSRowChange newRowChange = filter.convert(rowChange);
		assertNotNull(newRowChange);
	}

	@Test
	public void testNotIncludeInsert() throws Exception {
		// ����
		DBMSRowChange rowChange = DBMSRowChangeHelper.getDefaultRowChange(DBMSAction.INSERT);

		// ��������
		EventFilterNode filterNode = getEventFilterNode();
		filterNode.setIncludeInsert(false);

		FilterCondition condition = new FilterCondition(filterNode);
		filter = new DBMSEventFileFilter(condition);

		DBMSRowChange newRowChange = filter.convert(rowChange);
		assertNull(newRowChange);
	}

	@Test
	public void testNotIncludeUpdate() throws Exception {
		// ����
		DBMSRowChange rowChange = DBMSRowChangeHelper.getDefaultRowChange(DBMSAction.UPDATE);

		// ��������
		EventFilterNode filterNode = getEventFilterNode();
		filterNode.setIncludeUpdate(false);

		FilterCondition condition = new FilterCondition(filterNode);
		filter = new DBMSEventFileFilter(condition);

		DBMSRowChange newRowChange = filter.convert(rowChange);
		assertNull(newRowChange);
	}

	/**
	 * ���Բ�����delete
	 * @throws Exception 
	 */
	@Test
	public void testNotIncludeDelete() throws Exception {
		// ����
		DBMSRowChange deleteRowChange = DBMSRowChangeHelper.getDefaultRowChange(DBMSAction.DELETE);

		// ��������
		EventFilterNode filterNode = getEventFilterNode();
		filterNode.setIncludeDelete(false);

		FilterCondition condition = new FilterCondition(filterNode);
		filter = new DBMSEventFileFilter(condition);

		DBMSRowChange newRowChange = filter.convert(deleteRowChange);
		assertNull(newRowChange);
	}

	private EventFilterNode getEventFilterNode() {
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

		return filterNode;
	}
}
