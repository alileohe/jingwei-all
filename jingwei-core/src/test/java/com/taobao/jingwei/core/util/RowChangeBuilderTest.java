package com.taobao.jingwei.core.util;

import com.taobao.tddl.dbsync.dbms.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RowChangeBuilderTest {

	@Test(expected = Exception.class)
	public void test_createBuilder_empty_schema() throws Exception {
		String schema = "";
		String table = "test_table";
		DBMSAction action = DBMSAction.INSERT;
		RowChangeBuilder.createBuilder(schema, table, action).build();
	}

	@Test(expected = Exception.class)
	public void test_createBuilder_empty_table() throws Exception {
		String schema = "test_schema";
		String table = "";
		DBMSAction action = DBMSAction.INSERT;
		RowChangeBuilder.createBuilder(schema, table, action).build();
	}

	@Test(expected = Exception.class)
	public void test_createBuilder_empty_action() throws Exception {
		String schema = "test_schema";
		String table = "test_table";
		DBMSAction action = null;
		RowChangeBuilder.createBuilder(schema, table, action).build();
	}

	@Test(expected = Exception.class)
	public void test_createBuilder_empty_metaDataColum() throws Exception {
		String schema = "test_schema";
		String table = "test_table";
		DBMSAction action = DBMSAction.INSERT;
		RowChangeBuilder.createBuilder(schema, table, action).build();
	}

	@Test(expected = Exception.class)
	public void test_createBuilder_empty_rowDataColum() throws Exception {
		String schema = "test_schema";
		String table = "test_table";
		DBMSAction action = DBMSAction.INSERT;
		RowChangeBuilder builder = RowChangeBuilder.createBuilder(schema, table, action);
		builder.addMetaColumn("c1", Integer.class);
		builder.addMetaColumn("c2", String.class);
		builder.build();
	}

	@Test(expected = Exception.class)
	public void test_createBuilder_metaColumn_not_contains_ColumName() throws Exception {
		String schema = "test_schema";
		String table = "test_table";
		DBMSAction action = DBMSAction.INSERT;
		RowChangeBuilder builder = RowChangeBuilder.createBuilder(schema, table, action);
		builder.addMetaColumn("c1", Integer.class);
		builder.addMetaColumn("c2", String.class);

		Map<String, Serializable> rowDataMap = new HashMap<String, Serializable>(2);
		rowDataMap.put("c1", 1);
		rowDataMap.put("c3", "testValue");
		builder.addRowData(rowDataMap);
		builder.build();
	}

	@Test
	public void test_createBuilder_insert() throws Exception {
		String schema = "test_schema";
		String table = "test_table";
		DBMSAction action = DBMSAction.INSERT;
		RowChangeBuilder builder = RowChangeBuilder.createBuilder(schema, table, action);
		builder.addMetaColumn("c1", Integer.class);
		builder.addMetaColumn("c2", String.class);

		Map<String, Serializable> rowDataMap = new HashMap<String, Serializable>(2);
		rowDataMap.put("c1", 1);
		rowDataMap.put("c2", "testValue");
		builder.addRowData(rowDataMap);
		DBMSRowChange rowChange = builder.build();

		Assert.assertNotNull(rowChange);
		Assert.assertEquals(schema, rowChange.getSchema());
		Assert.assertEquals(table, rowChange.getTable());
		Assert.assertEquals(action, rowChange.getAction());

		StringBuilder sb = new StringBuilder();
		DBMSHelper.printDBMSEvent(sb, rowChange);
		System.out.println(sb);
	}

	@Test
	public void test_createBuilder_insert_for_list() throws Exception {
		String schema = "test_schema";
		String table = "test_table";
		DBMSAction action = DBMSAction.INSERT;
		RowChangeBuilder builder = RowChangeBuilder.createBuilder(schema, table, action);
		builder.addMetaColumn("c1", Integer.class);
		builder.addMetaColumn("c2", String.class);
		List<Map<String, Serializable>> rowDatas = new ArrayList<Map<String, Serializable>>(2);
		Map<String, Serializable> rowDataMap1 = new HashMap<String, Serializable>(2);
		rowDataMap1.put("c1", 1);
		rowDataMap1.put("c2", "testValue1");

		Map<String, Serializable> rowDataMap2 = new HashMap<String, Serializable>(2);
		rowDataMap2.put("c1", 2);
		rowDataMap2.put("c2", "testValue2");

		rowDatas.add(rowDataMap1);
		rowDatas.add(rowDataMap2);

		builder.addRowDatas(rowDatas);
		DBMSRowChange rowChange = builder.build();

		Assert.assertNotNull(rowChange);
		Assert.assertNotNull(rowChange);
		Assert.assertEquals(schema, rowChange.getSchema());
		Assert.assertEquals(table, rowChange.getTable());
		Assert.assertEquals(action, rowChange.getAction());

		StringBuilder sb = new StringBuilder();
		DBMSHelper.printDBMSEvent(sb, rowChange);
		System.out.println(sb);
	}

	@Test
	public void test_createBuilder_delete_metaColumns() throws Exception {
		String schema = "test_schema";
		String table = "test_table";
		DBMSAction action = DBMSAction.DELETE;
		RowChangeBuilder builder = RowChangeBuilder.createBuilder(schema, table, action);

		DBMSColumn c1 = new DefaultColumn("c1", 1, Types.INTEGER, true, true, true);
		DBMSColumn c2 = new DefaultColumn("c2", 2, Types.VARCHAR, true, true, false);

		List<DBMSColumn> metaColumns = new ArrayList<DBMSColumn>(2);
		metaColumns.add(c1);
		metaColumns.add(c2);
		builder.addMetaColumns(metaColumns);
		List<Map<String, Serializable>> rowDatas = new ArrayList<Map<String, Serializable>>(2);
		Map<String, Serializable> rowDataMap1 = new HashMap<String, Serializable>(2);
		rowDataMap1.put(c1.getName(), 1);
		rowDataMap1.put(c2.getName(), "testValue1");

		Map<String, Serializable> rowDataMap2 = new HashMap<String, Serializable>(2);
		rowDataMap2.put(c1.getName(), 2);
		rowDataMap2.put(c2.getName(), "testValue2");

		rowDatas.add(rowDataMap1);
		rowDatas.add(rowDataMap2);

		builder.addRowDatas(rowDatas);

		DBMSRowChange rowChange = builder.build();

		Assert.assertNotNull(rowChange);
		Assert.assertNotNull(rowChange);
		Assert.assertEquals(schema, rowChange.getSchema());
		Assert.assertEquals(table, rowChange.getTable());
		Assert.assertEquals(action, rowChange.getAction());

		StringBuilder sb = new StringBuilder();
		DBMSHelper.printDBMSEvent(sb, rowChange);
		System.out.println(sb);
	}

	@Test(expected = Exception.class)
	public void test_createBuilder_update_metaData_not_contains_changeColumName() throws Exception {
		String schema = "test_schema";
		String table = "test_table";
		DBMSAction action = DBMSAction.UPDATE;
		RowChangeBuilder builder = RowChangeBuilder.createBuilder(schema, table, action);
		builder.addMetaColumn("c1", Integer.class);
		builder.addMetaColumn("c2", String.class);

		Map<String, Serializable> rowDataMap = new HashMap<String, Serializable>(2);
		rowDataMap.put("c1", 1);
		rowDataMap.put("c2", "testValue");
		builder.addRowData(rowDataMap);

		Map<String, Serializable> changeRowDataMap = new HashMap<String, Serializable>(2);
		changeRowDataMap.put("c3", "testValue2");
		builder.addChangeRowData(changeRowDataMap);

		builder.build();
	}

	@Test
	public void test_createBuilder_update() throws Exception {
		String schema = "test_schema";
		String table = "test_table";
		DBMSAction action = DBMSAction.UPDATE;
		RowChangeBuilder builder = RowChangeBuilder.createBuilder(schema, table, action);
		builder.addMetaColumn("c1", Integer.class);
		builder.addMetaColumn("c2", String.class);

		Map<String, Serializable> rowDataMap = new HashMap<String, Serializable>(2);
		rowDataMap.put("c1", 1);
		rowDataMap.put("c2", "testValue");
		builder.addRowData(rowDataMap);

		Map<String, Serializable> changeRowDataMap = new HashMap<String, Serializable>(2);
		changeRowDataMap.put("c2", "testValue2");
		builder.addChangeRowData(changeRowDataMap);
		DBMSRowChange rowChange = builder.build();

		Assert.assertNotNull(rowChange);
		Assert.assertNotNull(rowChange);
		Assert.assertEquals(schema, rowChange.getSchema());
		Assert.assertEquals(table, rowChange.getTable());
		Assert.assertEquals(action, rowChange.getAction());

		StringBuilder sb = new StringBuilder();
		DBMSHelper.printDBMSEvent(sb, rowChange);
		System.out.println(sb);
	}

	@Test(expected = Exception.class)
	public void test_createBuilder_update_rowCountNotEqualsChangeRowCount() throws Exception {
		String schema = "test_schema";
		String table = "test_table";
		DBMSAction action = DBMSAction.UPDATE;
		RowChangeBuilder builder = RowChangeBuilder.createBuilder(schema, table, action);
		builder.addMetaColumn("c1", Integer.class);
		builder.addMetaColumn("c2", String.class);

		List<Map<String, Serializable>> rowDatas = new ArrayList<Map<String, Serializable>>(2);
		Map<String, Serializable> rowDataMap1 = new HashMap<String, Serializable>(2);
		rowDataMap1.put("c1", 1);
		rowDataMap1.put("c2", "testValue1");

		Map<String, Serializable> rowDataMap2 = new HashMap<String, Serializable>(2);
		rowDataMap2.put("c1", 2);
		rowDataMap2.put("c2", "testValue2");

		rowDatas.add(rowDataMap1);
		rowDatas.add(rowDataMap2);

		Map<String, Serializable> changeRowDataMap = new HashMap<String, Serializable>(2);
		changeRowDataMap.put("c2", "testValue2");
		builder.addChangeRowData(changeRowDataMap);
		builder.build();
	}

	@Test(expected = Exception.class)
	public void test_createBuilder_update_changeColumnNotSame() throws Exception {
		String schema = "test_schema";
		String table = "test_table";
		DBMSAction action = DBMSAction.UPDATE;
		RowChangeBuilder builder = RowChangeBuilder.createBuilder(schema, table, action);
		builder.addMetaColumn("c1", Integer.class);
		builder.addMetaColumn("c2", String.class);

		List<Map<String, Serializable>> rowDatas = new ArrayList<Map<String, Serializable>>(2);
		Map<String, Serializable> rowDataMap1 = new HashMap<String, Serializable>(2);
		rowDataMap1.put("c1", 1);
		rowDataMap1.put("c2", "testValue1");

		Map<String, Serializable> rowDataMap2 = new HashMap<String, Serializable>(2);
		rowDataMap2.put("c1", 2);
		rowDataMap2.put("c2", "testValue2");

		rowDatas.add(rowDataMap1);
		rowDatas.add(rowDataMap2);
		builder.addRowDatas(rowDatas);

		List<Map<String, Serializable>> changerowDatas = new ArrayList<Map<String, Serializable>>(2);
		Map<String, Serializable> changeRowDataMap1 = new HashMap<String, Serializable>(2);
		changeRowDataMap1.put("c2", "testValue1-1");

		Map<String, Serializable> changeRowDataMap2 = new HashMap<String, Serializable>(2);
		changeRowDataMap2.put("c3", "testValue2-1");
		changerowDatas.add(changeRowDataMap1);
		changerowDatas.add(changeRowDataMap2);

		builder.addChangeRowDatas(changerowDatas);
		builder.build();
	}

	@Test
	public void test_createBuilder_update_for_list() throws Exception {
		String schema = "test_schema";
		String table = "test_table";
		DBMSAction action = DBMSAction.UPDATE;
		RowChangeBuilder builder = RowChangeBuilder.createBuilder(schema, table, action);
		builder.addMetaColumn("c1", Integer.class);
		builder.addMetaColumn("c2", String.class);

		List<Map<String, Serializable>> rowDatas = new ArrayList<Map<String, Serializable>>(2);
		Map<String, Serializable> rowDataMap1 = new HashMap<String, Serializable>(2);
		rowDataMap1.put("c1", 1);
		rowDataMap1.put("c2", "testValue1");

		Map<String, Serializable> rowDataMap2 = new HashMap<String, Serializable>(2);
		rowDataMap2.put("c1", 2);
		rowDataMap2.put("c2", "testValue2");

		rowDatas.add(rowDataMap1);
		rowDatas.add(rowDataMap2);
		builder.addRowDatas(rowDatas);

		List<Map<String, Serializable>> changerowDatas = new ArrayList<Map<String, Serializable>>(2);
		Map<String, Serializable> changeRowDataMap1 = new HashMap<String, Serializable>(2);
		changeRowDataMap1.put("c2", "testValue1-1");

		Map<String, Serializable> changeRowDataMap2 = new HashMap<String, Serializable>(2);
		changeRowDataMap2.put("c2", "testValue2-1");
		changerowDatas.add(changeRowDataMap1);
		changerowDatas.add(changeRowDataMap2);

		builder.addChangeRowDatas(changerowDatas);
		builder.build();

		DBMSRowChange rowChange = builder.build();

		Assert.assertNotNull(rowChange);
		Assert.assertNotNull(rowChange);
		Assert.assertEquals(schema, rowChange.getSchema());
		Assert.assertEquals(table, rowChange.getTable());
		Assert.assertEquals(action, rowChange.getAction());
	}
}