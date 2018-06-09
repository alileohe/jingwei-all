package com.taobao.jingwei.core.kernel;

import com.taobao.jingwei.common.node.tasks.SyncTaskNode.GroupingSetting;
import com.taobao.jingwei.core.util.RowChangeBuilder;
import com.taobao.tddl.dbsync.DbsyncException;
import com.taobao.tddl.dbsync.dbms.DBMSAction;
import com.taobao.tddl.dbsync.dbms.DBMSRowChange;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @desc ��1�������쳣��ָ���ķֿ��������ʵ����в�����
 *       ��2����ʼ���쳣�����õĿ������ʽΪnull���
 *       ��3����ʼ���쳣�����õı������ʽΪnull���
 *       ��3����ʼ���쳣�����õ��ֶ�Ϊnull���
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Aug 28, 2012 2:27:31 PM
 */

public class MultiGroupingPolicyErrorFieldTest {
	private static String SCHEMA1 = "schema1";
	private static String TABLE1 = "table1";
	private static RowChangeBuilder builder1 = RowChangeBuilder.createBuilder(SCHEMA1, TABLE1, DBMSAction.INSERT);

	private MultiGroupingPolicy multiGroupingPolicy;

	@Before
	public void init() throws DbsyncException {
		multiGroupingPolicy = new MultiGroupingPolicy();

		List<GroupingSetting> groupingSettings = new ArrayList<GroupingSetting>();
		GroupingSetting groupintSetting1 = new GroupingSetting();
		groupintSetting1.setSchemaReg("schema1");
		groupintSetting1.setTableReg("table1");
		groupintSetting1.setFields("asd,fds");
		groupingSettings.add(groupintSetting1);

		multiGroupingPolicy.init(groupingSettings);
	}

	// ָ���ķֿ��������ʵ����в�����
	@Test
	@Ignore
	public void testFenkujianValueEquals() throws Exception {
		builder1.addMetaColumn("c1", Integer.class);
		builder1.addMetaColumn("c2", String.class);

		Map<String, Serializable> rowDataMap1 = new HashMap<String, Serializable>(2);
		rowDataMap1.put("c1", 1);
		rowDataMap1.put("c2", "testValue");
		builder1.addRowData(rowDataMap1);
		DBMSRowChange rowChange1 = builder1.build();

		try {
			multiGroupingPolicy.getGroup(SCHEMA1, TABLE1, rowChange1, 1);
			fail("not throw exception!");
		} catch (DbsyncException e) {
			assertTrue(true);
		}
	}

	// �������ʽΪ��
	@Test
	public void testSchemaRegEmpty() {

		List<GroupingSetting> groupingSettings = new ArrayList<GroupingSetting>();
		GroupingSetting groupintSetting1 = new GroupingSetting();
		groupintSetting1.setSchemaReg("");
		groupintSetting1.setTableReg(TABLE1);
		groupintSetting1.setFields("c1");
		groupingSettings.add(groupintSetting1);

		try {
			multiGroupingPolicy.init(groupingSettings);
			fail("not throw exception!");
		} catch (DbsyncException e) {
			assertTrue(true);
		}
	}

	// �������ʽΪ�ջ�null
	@Test
	public void testSchemaRegNull() {

		List<GroupingSetting> groupingSettings = new ArrayList<GroupingSetting>();
		GroupingSetting groupintSetting1 = new GroupingSetting();
		groupintSetting1.setSchemaReg(null);
		groupintSetting1.setTableReg(TABLE1);
		groupintSetting1.setFields("c1");
		groupingSettings.add(groupintSetting1);

		try {
			multiGroupingPolicy.init(groupingSettings);
			fail("not throw exception!");
		} catch (DbsyncException e) {
			assertTrue(true);
		}
	}

	// �������ʽΪ�ջ�null
	@Test
	public void testTableRegEmpty() {

		List<GroupingSetting> groupingSettings = new ArrayList<GroupingSetting>();
		GroupingSetting groupintSetting1 = new GroupingSetting();
		groupintSetting1.setSchemaReg(SCHEMA1);
		groupintSetting1.setTableReg("");
		groupintSetting1.setFields("c1");
		groupingSettings.add(groupintSetting1);

		try {
			multiGroupingPolicy.init(groupingSettings);
			fail("not throw exception!");
		} catch (DbsyncException e) {
			assertTrue(true);
		}
	}

	//  �������ʽΪ�ջ�null
	@Test
	public void testTableRegNull() {

		List<GroupingSetting> groupingSettings = new ArrayList<GroupingSetting>();
		GroupingSetting groupintSetting1 = new GroupingSetting();
		groupintSetting1.setSchemaReg(SCHEMA1);
		groupintSetting1.setTableReg(null);
		groupintSetting1.setFields("c1");
		groupingSettings.add(groupintSetting1);

		try {
			multiGroupingPolicy.init(groupingSettings);
			fail("not throw exception!");
		} catch (DbsyncException e) {
			assertTrue(true);
		}
	}

	// �������ʽΪ�ջ�null
	@Test
	public void testFieldRegEmpty() {

		List<GroupingSetting> groupingSettings = new ArrayList<GroupingSetting>();
		GroupingSetting groupintSetting1 = new GroupingSetting();
		groupintSetting1.setSchemaReg(SCHEMA1);
		groupintSetting1.setTableReg(TABLE1);
		groupintSetting1.setFields("");
		groupingSettings.add(groupintSetting1);

		try {
			multiGroupingPolicy.init(groupingSettings);
			fail("not throw exception!");
		} catch (DbsyncException e) {
			assertTrue(true);
		}
	}

	// �������ʽΪ�ջ�null
	@Test
	public void testFieldRegNull() {

		List<GroupingSetting> groupingSettings = new ArrayList<GroupingSetting>();
		GroupingSetting groupintSetting1 = new GroupingSetting();
		groupintSetting1.setSchemaReg(SCHEMA1);
		groupintSetting1.setTableReg(TABLE1);
		groupintSetting1.setFields(null);
		groupingSettings.add(groupintSetting1);
		try {
			multiGroupingPolicy.init(groupingSettings);
			fail("not throw exception!");
		} catch (DbsyncException e) {
			assertTrue(true);
		}
	}

}
