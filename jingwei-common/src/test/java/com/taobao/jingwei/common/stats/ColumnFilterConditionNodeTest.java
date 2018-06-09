package com.taobao.jingwei.common.stats;

import com.taobao.jingwei.common.node.applier.EventFilterNode.ColumnFilterConditionNode;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @desc 
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Apr 18, 2012 11:51:30 AM
 */

public class ColumnFilterConditionNodeTest {

	static final String INCLUDE_COLUMN_1 = "INCLUDE_COLUMN_1";
	static final String INCLUDE_COLUMN_2 = "INCLUDE_COLUMN_2";

	static final String EXCLUDE_COLUMN_1 = "EXCLUDE_COLUMN_1";
	static final String EXCLUDE_COLUMN_2 = "EXCLUDE_COLUMN_2";

	static final String SCHEMA_1 = "SCHEMA_1";
	static final String SCHEMA_2 = "SCHEMA_2";

	static final String TABLE_1 = "TABLE_1";
	static final String TABLE_2 = "TABLE_2";

	static final String SCHEMA_REG_1 = "^(SCHEMA_REG){1}_[0-9]{1,2}$";

	ColumnFilterConditionNode conNode = new ColumnFilterConditionNode();

	@Before
	public void init() {

		Set<String> includeColumns = new HashSet<String>();
		includeColumns.add(INCLUDE_COLUMN_1);
		includeColumns.add(INCLUDE_COLUMN_2);

		Set<String> excludeColumns = new HashSet<String>();
		excludeColumns.add(EXCLUDE_COLUMN_1);
		excludeColumns.add(EXCLUDE_COLUMN_2);

		conNode.setIncludeColumns(includeColumns);
		conNode.setExcludeColumns(excludeColumns);
		conNode.setUseIncludeRule(true);
	}

	@Test
	public void test() {
		assertEquals(2, conNode.getIncludeColumns().size());
		assertTrue(conNode.getIncludeColumns().contains(INCLUDE_COLUMN_1));
		assertTrue(conNode.getIncludeColumns().contains(INCLUDE_COLUMN_2));

		assertEquals(2, conNode.getExcludeColumns().size());
		assertTrue(conNode.getExcludeColumns().contains(EXCLUDE_COLUMN_1));
		assertTrue(conNode.getExcludeColumns().contains(EXCLUDE_COLUMN_2));

		assertEquals(true, conNode.isUseIncludeRule());
	}

}
