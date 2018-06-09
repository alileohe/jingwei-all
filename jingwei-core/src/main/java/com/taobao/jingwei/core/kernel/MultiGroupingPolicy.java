package com.taobao.jingwei.core.kernel;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode.GroupingSetting;
import com.taobao.tddl.dbsync.DbsyncException;
import com.taobao.tddl.dbsync.dbms.*;
import com.taobao.tddl.dbsync.pipeline.grouping.GroupingChain;
import com.taobao.tddl.dbsync.pipeline.grouping.GroupingPolicy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * @desc  ��1����������ƥ�䣬�ֶ���ȷ��group = schema.hash ^ field.hash
 *        ��2����������ƥ�䣬�ֶβ���ȷ��group = schema.hash ^ table.hash
 *        ��3����������ƥ�䣬�ֶ�ֵΪnull��group = schema.hash ^ DEFAULT_HASH
 *        ��4������ƥ�������ƥ�䣬group = schema.hash ^ pk.hash
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Aug 24, 2012 11:30:43 AM
 */

public class MultiGroupingPolicy implements GroupingPolicy {

	private static final int DEFAULT_HASHCODE = Integer.MAX_VALUE;

	private static final String DEFAULT_SCHEMA_DEFAULT_SCHEMA = "DEFAULT_SCHEMA_@@_DEFAULT_TABLE";

	private Log log = LogFactory.getLog(MultiGroupingPolicy.class);

	private final String COMMA_SEP = ",";

	/** schema-reg, table-reg, fields */
	private ConcurrentHashMap<String, ConcurrentHashMap<String, HashSet<String>>> groupSettings = new ConcurrentHashMap<String, ConcurrentHashMap<String, HashSet<String>>>();

	/** ���� */
	private GroupingCache groupingCache = new GroupingCache();

	/**
	 * 
	 * @param grouppingSettings �������ʽ���������ʽ���ֶΣ����ŷָ���
	 * @throws DbsyncException �������Ϊ�ջ�null��������Ŀ������ʽ���������ʽ���ֶ�Ϊ�ջ�null���׳��쳣
	 */
	public void init(List<GroupingSetting> grouppingSettings) throws DbsyncException {
		if (null == grouppingSettings || grouppingSettings.isEmpty()) {
			log.warn("[jingwei core] grouping setting is null or empty!");
			return;
		}

		for (GroupingSetting groupingSetting : grouppingSettings) {
			String schemaReg = groupingSetting.getSchemaReg();
			if (StringUtil.isBlank(schemaReg)) {
				String msg = "[jingwei core] grouping setting schema reg is null or empty! ";
				log.error(msg);
				throw new DbsyncException(msg);
			} else {

				String tableReg = groupingSetting.getTableReg();
				if (StringUtil.isBlank(tableReg)) {
					String msg = "[jingwei core] grouping setting table reg is null or empty! ";
					log.error(msg);
					throw new DbsyncException(msg);
				}

				String fields = groupingSetting.getFields();
				if (StringUtil.isBlank(fields)) {
					String msg = "[jingwei core] grouping setting fields is null or empty! ";
					log.error(msg);
					throw new DbsyncException(msg);
				}

				ConcurrentHashMap<String, HashSet<String>> tableMapFileds;
				if (this.groupSettings.containsKey(schemaReg)) {
					tableMapFileds = this.groupSettings.get(schemaReg);
				} else {
					tableMapFileds = new ConcurrentHashMap<String, HashSet<String>>();
					this.groupSettings.put(schemaReg, tableMapFileds);
				}

				HashSet<String> exitFields;
				if (tableMapFileds.containsKey(tableReg)) {
					exitFields = tableMapFileds.get(tableReg);
				} else {
					exitFields = new HashSet<String>();
					tableMapFileds.put(tableReg, exitFields);
				}

				String singleFields[] = fields.split(COMMA_SEP);

				for (String field : singleFields) {
					exitFields.add(field);
				}
			}
		}
	}

	@Override
	public void group(DBMSEvent event, GroupingChain chain) throws DbsyncException, InterruptedException {
		if (event instanceof DBMSRowChange) {
			DBMSRowChange change = (DBMSRowChange) event;
			final String schema = change.getSchema();
			final String table = change.getTable();
			DBMSColumnSet columnSet = change.getColumnSet();

			for (int rownum = 1; rownum <= change.getRowSize(); rownum++) {

				int group = this.getGroup(schema, table, change, rownum);

				if (change.getRowSize() == 1) {
					chain.handle(group, event);
				} else {
					DefaultRowChange split = new DefaultRowChange(change.getAction(), schema, table, columnSet);

					if (DBMSAction.UPDATE == change.getAction()) {
						split.setChangeSet(change.getChangeIndexes());
						split.setChangeData(1, change.getChangeData(rownum));
					}
					split.setRowData(1, change.getRowData(rownum));
					chain.handle(group, split);
				}
			}
		} else if (event instanceof DBMSQueryLog) {
			// XXX: We're not actually support QueryLog
			DBMSQueryLog queryLog = (DBMSQueryLog) event;
			final String schema = queryLog.getSchema();
			final int group = (schema != null) ? schema.hashCode() : 0;
			chain.handle(group, event);
		}
	}

	/**
	 * �ж��Ƿ��ҵ���ƥ��Ŀ������ʽ
	 * @param schema
	 * @return <code>null</code>��ʾ������ƥ����ʽ
	 */
	private String matchSchema(String schema) {
		// �����Ƿ�ƥ��Ŀ���
		Enumeration<String> schemaRegEnu = groupSettings.keys();

		while (schemaRegEnu.hasMoreElements()) {
			String schemaReg = schemaRegEnu.nextElement();
			Pattern pattern = Pattern.compile(schemaReg);
			if (pattern.matcher(schema).matches()) {
				return schemaReg;
			}
		}

		return null;
	}

	/**
	 * �ж��Ƿ��ҵ���ƥ��ı������ʽ
	 * @param schemaReg
	 * @param table
	 * @return <code>null</code>��ʾ������ƥ����ʽ
	 */
	private String matchTable(String schemaReg, String table) {
		ConcurrentHashMap<String, HashSet<String>> tableRegs = groupSettings.get(schemaReg);
		if (tableRegs == null || tableRegs.isEmpty()) {
			return null;
		}

		Enumeration<String> tableRegEnu = tableRegs.keys();
		while (tableRegEnu.hasMoreElements()) {
			String tableReg = tableRegEnu.nextElement();
			Pattern pattern = Pattern.compile(tableReg);
			if (pattern.matcher(table).matches()) {
				return tableReg;
			}
		}

		return null;
	}

	/**
	 * 
	 * @param schema ʵ�ʿ���
	 * @param table ʵ�ʱ���
	 * @return �յ�set��ʾ�����ͱ��������õı��ʽ��ƥ�䣻�ǿձ�ʾ�ҵ���ƥ��Ŀ����ͱ������ʽ��setֵ���ֶμ���
	 */
	private Set<String> matchSchemaTable(String schema, String table) {
		Set<String> fieldCols = new HashSet<String>();
		String matchSchemaReg = this.matchSchema(schema);
		if (StringUtil.isNotBlank(matchSchemaReg)) {
			// �ҵ�ƥ��Ŀ���ʽ�����ұ����Ƿ�ƥ��
			String matchTableReg = this.matchTable(matchSchemaReg, table);
			if (StringUtil.isNotBlank(matchTableReg)) {

				for (String field : groupSettings.get(matchSchemaReg).get(matchTableReg)) {
					fieldCols.add(field);
				}
			}
		}

		return fieldCols;
	}

	/**
	 * @param groupingCols �ֿ��
	 * @param change event
	 * @param schemaGroup ����hash
	 * @param rownum �кţ���1��ʼ
	 * @return ����
	 */
	private int countGroup(Set<String> groupingCols, DBMSRowChange change, int schemaGroup, final int rownum)
			throws DbsyncException {
		int group = schemaGroup;

		DBMSColumnSet columnSet = change.getColumnSet();

		if (groupingCols != null && !groupingCols.isEmpty()) {
			// �����������
			for (String groupingColumn : groupingCols) {
                Serializable value=change.getRowValue(rownum,groupingColumn);
				if (value != null) {
                    group = group ^ value.hashCode();
				} else {
                    //���������grouping�ļ�������,��ʹ��Ĭ�ϵ�group��ֵ
                    group = group ^ DEFAULT_HASHCODE;
				}
			}
		} else {
			group = group ^ change.getTable().hashCode();
			List<DBMSColumn> primaryKey = columnSet.getPrimaryKey();
			// �ֿ����������û���ҵ�
			if (!(primaryKey == null || primaryKey.isEmpty())) {
				// û�зֿ������������
				for (DBMSColumn column : primaryKey) {
					Serializable value = change.getRowValue(rownum, column);
					if (null != value) {
						group = group ^ value.hashCode();
					}
				}
			}
		}

		// ��ƥ�䡢��ƥ�䣬���ظ���ʵ�ʿ�����ȡ��group
		return group;
	}

	/**
	 * ����group����ֵ
	 * @param schema ʵ�ʿ���
	 * @param table ʵ�ʱ���
	 * @param change event�¼�
	 * @param rownum �к�
	 * @return ����
	 * @throws DbsyncException ���õķֿ����Ϊ�գ�������ʵ�ʱ���û���ҵ�ָ�����ֶ�
	 */
	public int getGroup(String schema, String table, DBMSRowChange change, int rownum) throws DbsyncException {

		int group = schema.hashCode();

		// �ҷֿ��
		String cacheGroupingCols = this.groupingCache.getGroupingCols(schema, table);
		if (null == cacheGroupingCols) {
			Set<String> fieldCols = this.matchSchemaTable(schema, table);
			// �����ͱ�����ƥ�䣨1������group��2�����뻺��
			group = this.countGroup(fieldCols, change, group, rownum);

			// ����������ƥ��
			if (fieldCols.isEmpty()) {
				this.groupingCache.setGroupingCols(schema, table, DEFAULT_SCHEMA_DEFAULT_SCHEMA);
			} else {
				String groupfileds = this.setToStringSepByComma(fieldCols);
				this.groupingCache.setGroupingCols(schema, table, groupfileds);
			}
		} else {
			if (cacheGroupingCols.equals(DEFAULT_SCHEMA_DEFAULT_SCHEMA)) {
				// ʹ����������
				group = this.countGroup(null, change, group, rownum);
			} else {
				Set<String> groupingColsSet = this.getFields(cacheGroupingCols);

				// �������ҵ������,ƥ���ͱ��ʽ
				group = this.countGroup(groupingColsSet, change, group, rownum);
			}
		}

		return group;
	}

	/**
	 * ���ŷָ�����set<String>
	 * @param fields
	 * @return
	 */
	private Set<String> getFields(String fieldsString) {
		if (null == fieldsString) {
			return Collections.emptySet();
		}
		String[] fields = fieldsString.split(",");
		Set<String> groupingColsSet = new HashSet<String>(fields.length);
		for (String f : fields) {
			groupingColsSet.add(f);
		}

		return groupingColsSet;
	}

	/**
	 * 
	 * @param fields
	 * @return EMPTY_STRING ���setΪnull����Ϊ��
	 */
	private String setToStringSepByComma(Set<String> fields) {
		if (fields == null || fields.isEmpty()) {
			return StringUtil.EMPTY_STRING;
		}

		StringBuilder sb = new StringBuilder();
		for (String f : fields) {
			sb.append(f).append(",");
		}

		return sb.substring(0, sb.length());

	}
}
