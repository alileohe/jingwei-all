package com.taobao.jingwei.core.kernel;

import com.taobao.jingwei.common.node.tasks.SyncTaskNode.GroupingSetting;
import com.taobao.jingwei.core.util.RowChangeBuilder;
import com.taobao.tddl.dbsync.DbsyncException;
import com.taobao.tddl.dbsync.dbms.DBMSAction;
import com.taobao.tddl.dbsync.dbms.DBMSRowChange;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @desc ָ��2���ֿ��
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Aug 28, 2012 2:27:31 PM
 */

public class MultiGroupingPolicyMutilFiledTest {
	private static String SCHEMA1 = "schema1";
	private static String TABLE1 = "table1";
	private static String TABLE2 = "table2";
	private static RowChangeBuilder builder1 = RowChangeBuilder.createBuilder(SCHEMA1, TABLE1, DBMSAction.INSERT);
	private static RowChangeBuilder builder2 = RowChangeBuilder.createBuilder(SCHEMA1, TABLE2, DBMSAction.INSERT);

	private MultiGroupingPolicy multiGroupingPolicy;

	@Before
	public void init() throws DbsyncException {
		multiGroupingPolicy = new MultiGroupingPolicy();

		List<GroupingSetting> groupingSettings = new ArrayList<GroupingSetting>();
		GroupingSetting groupintSetting1 = new GroupingSetting();
		groupintSetting1.setSchemaReg("schema1");
		groupintSetting1.setTableReg("table1");
		groupintSetting1.setFields("c1,c2");
		groupingSettings.add(groupintSetting1);

		GroupingSetting groupintSetting2 = new GroupingSetting();
		groupintSetting2.setSchemaReg("schema1");
		groupintSetting2.setTableReg("table2");
		groupintSetting2.setFields("d1,d2");
		groupingSettings.add(groupintSetting2);

		multiGroupingPolicy.init(groupingSettings);
	}

	// һ���ֿ����group��ͬ
	@Test
	public void testFenkujianValueEquals() throws Exception {
		builder1.addMetaColumn("c1", Integer.class);
		builder1.addMetaColumn("c2", String.class);

		Map<String, Serializable> rowDataMap1 = new HashMap<String, Serializable>(2);
		rowDataMap1.put("c1", 1);
		rowDataMap1.put("c2", "testValue");
		builder1.addRowData(rowDataMap1);
		DBMSRowChange rowChange1 = builder1.build();

		builder2.addMetaColumn("d1", Integer.class);
		builder2.addMetaColumn("d2", String.class);

		Map<String, Serializable> rowDataMap2 = new HashMap<String, Serializable>(2);
		rowDataMap2.put("d1", 1);
		rowDataMap2.put("d2", "testValue");
		builder2.addRowData(rowDataMap2);
		DBMSRowChange rowChange2 = builder2.build();

		int group1 = multiGroupingPolicy.getGroup(SCHEMA1, TABLE1, rowChange1, 1);
		int group2 = multiGroupingPolicy.getGroup(SCHEMA1, TABLE2, rowChange2, 1);

		assertEquals(group1, group2);
	}

	// һ���ֿ����group��ͬ(һ���ֿ����ͬ) rowDataMap2.put("d1", 2);
	@Test
	public void testFenkujianValueNotDiff() throws Exception {
		builder1.addMetaColumn("c1", Integer.class);
		builder1.addMetaColumn("c2", String.class);

		Map<String, Serializable> rowDataMap1 = new HashMap<String, Serializable>(2);
		rowDataMap1.put("c1", 1);
		rowDataMap1.put("c2", "testValue");
		builder1.addRowData(rowDataMap1);
		DBMSRowChange rowChange1 = builder1.build();

		builder2.addMetaColumn("d1", Integer.class);
		builder2.addMetaColumn("d2", String.class);

		Map<String, Serializable> rowDataMap2 = new HashMap<String, Serializable>(2);
		rowDataMap2.put("d1", 2);
		rowDataMap2.put("d2", "testValue");
		builder2.addRowData(rowDataMap2);
		DBMSRowChange rowChange2 = builder2.build();

		int group1 = multiGroupingPolicy.getGroup(SCHEMA1, TABLE1, rowChange1, 2);
		int group2 = multiGroupingPolicy.getGroup(SCHEMA1, TABLE2, rowChange2, 2);

		Assert.assertNotSame(group1, group2);
	}

	//  һ���ֿ����group��ͬ(һ���ֿ����ͬ) rowDataMap2.put("d2", "testValue1");
	@Test
	public void testFenkujianValueNotDiff2() throws Exception {
		builder1.addMetaColumn("c1", Integer.class);
		builder1.addMetaColumn("c2", String.class);

		Map<String, Serializable> rowDataMap1 = new HashMap<String, Serializable>(2);
		rowDataMap1.put("c1", 1);
		rowDataMap1.put("c2", "testValue");
		builder1.addRowData(rowDataMap1);
		DBMSRowChange rowChange1 = builder1.build();

		builder2.addMetaColumn("d1", Integer.class);
		builder2.addMetaColumn("d2", String.class);

		Map<String, Serializable> rowDataMap2 = new HashMap<String, Serializable>(2);
		rowDataMap2.put("d1", 1);
		rowDataMap2.put("d2", "testValue1");
		builder2.addRowData(rowDataMap2);
		DBMSRowChange rowChange2 = builder2.build();

		int group1 = multiGroupingPolicy.getGroup(SCHEMA1, TABLE1, rowChange1, 3);
		int group2 = multiGroupingPolicy.getGroup(SCHEMA1, TABLE2, rowChange2, 3);

		assertTrue(group1 != group2);
	}

	//  һ���ֿ����group��ͬ(2���ֿ����ͬ) rowDataMap2.put("d2", "testValue1");
	@Test
	public void testFenkujianValueNotDiff3() throws Exception {
		builder1.addMetaColumn("c1", Integer.class);
		builder1.addMetaColumn("c2", String.class);

		Map<String, Serializable> rowDataMap1 = new HashMap<String, Serializable>(2);
		rowDataMap1.put("c1", 1);
		rowDataMap1.put("c2", "testValue");
		builder1.addRowData(rowDataMap1);
		DBMSRowChange rowChange1 = builder1.build();

		builder2.addMetaColumn("d1", Integer.class);
		builder2.addMetaColumn("d2", String.class);

		Map<String, Serializable> rowDataMap2 = new HashMap<String, Serializable>(2);
		rowDataMap2.put("d1", 2);
		rowDataMap2.put("d2", "testValue1");
		builder2.addRowData(rowDataMap2);
		DBMSRowChange rowChange2 = builder2.build();

		int group1 = multiGroupingPolicy.getGroup(SCHEMA1, TABLE1, rowChange1, 3);
		int group2 = multiGroupingPolicy.getGroup(SCHEMA1, TABLE2, rowChange2, 3);

		assertTrue(group1 != group2);
	}

}
