package com.taobao.jingwei.core.util;

import com.alibaba.common.lang.StringUtil;
import com.taobao.erosa.exception.ErosaParseException;
import com.taobao.erosa.oracle.helper.OracleErosaHelper;
import com.taobao.erosa.protocol.ErosaEntry.EventType;
import com.taobao.erosa.protocol.ErosaEntry.Header;
import com.taobao.erosa.protocol.ErosaEntry.Pair;
import com.taobao.erosa.protocol.ErosaEntry.RowData;
import com.taobao.erosa.protocol.ErosaEntry.RowData.Column;
import com.taobao.jingwei.common.JingWeiUtil;
import com.taobao.jingwei.common.node.type.DBType;
import com.taobao.tddl.dbsync.dbms.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.*;

/**
*
<p>
description:����erosa��ع�����
<p>
*
* ErosaHelper.java Create on Nov 29, 2012 8:10:26 PM
*
* Copyright (c) 2011 by qihao.
*
*@author
<a href="mailto:qihao@taobao.com">qihao</a>
*@version 1.0
*/
public class ErosaHelper {

	protected static final Log logger = LogFactory.getLog(ErosaHelper.class);

	public static final String OP_SRC_EVENT_KEY = "s_event";
	public static final String OP_SRC_EVENT_VALUE = "erosa";

	public static final String OP_SRC_DBTYPE_KEY = "s_dbType";

	/**��Erosa��RowDataת���ɾ�����DBMSRowChange
	 * @param rowData
	 * @return
	 * @throws Exception
	 */
	public static DBMSRowChange e2jConvertRowData(RowData rowData, DBType dbType) throws Exception {
		String schemaName = rowData.getSchemaName();
		final String tableName = rowData.getTableName();
		DBMSAction action = ErosaHelper.e2jConvertEventAction(rowData.getEventType());

		//����ǲ�֧�����ͷ���null
		if (DBMSAction.OTHER == action) {
			return null;
		}
		//����builder����
		final RowChangeBuilder builder = RowChangeBuilder.createBuilder(schemaName, tableName, action);
		//���ǰ����
		final List<Column> beforeColumns = getListDefaultEmpty(rowData.getBeforeColumnsList());
		//���������
		final List<Column> afterColumns = getListDefaultEmpty(rowData.getAfterColumnsList());

		if (DBMSAction.UPDATE != action) {
			//INSERT,DELETE
			List<Column> dataColumns = DBMSAction.INSERT == action ? afterColumns : beforeColumns;
			Map<String, Serializable> columnData = new HashMap<String, Serializable>(dataColumns.size());

			for (Column column : dataColumns) {
				//��Ӹ��еı�ͷ��Ϣ��builder��
				builder.addMetaColumnWithoutClone(e2jConvertColumn(column));
				//��Ӹ���ת������е�ֵ��columnData��
				columnData.put(StringUtil.toLowerCase(column.getName()), column.getIsNull() ? null
						: e2jConvertRowValue(column.getValue(), column.getType(), dbType));
			}
			//��ת�����е�ֵ��builder��
			builder.addRowData(columnData);
		} else {
			//UPDATE
			//=========================�еı�ͷ��Ϣ������=============================
			//ƴװ�еı�ͷ��Ϣ��������������ݴ�����iteratorCall�ᱻ�ص�
			final Set<String> allColumnNameSet = new HashSet<String>(beforeColumns.size() + afterColumns.size());

			ColumnIteratorCall iteratorCall = new ColumnIteratorCall() {
				@Override
				public void call(Column column) {
					String columnName = column.getName();
					if (!allColumnNameSet.contains(columnName)) {
						//�����ǰ��Column�б����������򴴽�����ж�Ӧ�ı�ͷ��Ϣ��builder��
						builder.addMetaColumnWithoutClone(e2jConvertColumn(column));
						allColumnNameSet.add(columnName);
					}
				}
			};
			//=========================��ֵ�ı仯ǰ��仯������=======================
			//���仯ǰ�ͱ仯��Columnת���ɶ�Ӧ�ľ�����Map<columnName,columnValue>��������ӦColumnIteratorCall
			Map<String, Serializable> beforeMap = e2jConvertColumnsValueWitchIteratorCall(beforeColumns, iteratorCall,
					dbType);
			Map<String, Serializable> afterMap = e2jConvertColumnsValueWitchIteratorCall(afterColumns, iteratorCall,
					dbType);

			//�ñ仯�������б�����ȫ�仯ǰ�����б�
			for (Map.Entry<String, Serializable> entry : afterMap.entrySet()) {
				if (!beforeMap.containsKey(entry.getKey())) {
					beforeMap.put(entry.getKey(), entry.getValue());
				}
			}
			//���仯ǰ�ͱ仯���ֵ�builder��
			builder.addRowData(beforeMap);
			builder.addChangeRowData(afterMap);
		}
		//����options
		builder.addOptions(ErosaHelper.e2jConvertOption(rowData.getPropsList()));
		builder.addOption(OP_SRC_EVENT_KEY, OP_SRC_EVENT_VALUE);
		builder.addOption(OP_SRC_DBTYPE_KEY, dbType.toString());
		return builder.build();
	}

	/**����SqlType���ַ�����VALUEת���ɶ�Ӧ��JAVA���͵�ֵ
	 * @param str
	 * @param sqlType
	 * @return
	 * @throws ErosaParseException 
	 * @throws Exception 
	 */
	public static Serializable e2jConvertRowValue(String str, int sqlType, DBType dbType) throws ErosaParseException {
		if (DBType.ORACLE != dbType) {
			logger.error("not support now !");
		}
		return OracleErosaHelper.convertOracleRowValue(str, sqlType);
	}

	/**��Erosa��Column ת���ɾ�����DBMSColumn
	 * @param column
	 * @return
	 */
	public static DBMSColumn e2jConvertColumn(Column column) {
		DBMSColumn dbmsColumn = null;
		if (null != column) {
			dbmsColumn = RowChangeBuilder.createMetaColumn(StringUtil.toLowerCase(column.getName()),
					column.getColumnIndex(), column.getType(), column.getIsNull(), column.getIsKey());
		}
		return dbmsColumn;
	}

	/**��Erosa��List<Column> ת���ɾ�����Value Map<String, Serializable>
	* @param columns
	* @return
	 * @throws Exception 
	*/
	public static Map<String/*columnName*/, Serializable> e2jConvertColumnsValue(List<Column> columns, DBType dbType)
			throws Exception {
		return e2jConvertColumnsValueWitchIteratorCall(columns, null, dbType);
	}

	/**��Erosa��eventTypeת���ɾ�����DBMSAction
	 * @param evenType
	 * @return
	 */
	public static DBMSAction e2jConvertEventAction(EventType evenType) {
		DBMSAction action = null;
		switch (evenType) {
		case INSERT:
			action = DBMSAction.INSERT;
			break;
		case UPDATE:
			action = DBMSAction.UPDATE;
			break;
		case DELETE:
			action = DBMSAction.DELETE;
			break;
		case ALTER:
		case CREATE:
		case ERASE:
		default:
			action = DBMSAction.OTHER;
		}
		return action;
	}

	/**ת��Erosa��option�б��Ϊ������DBMSOption�б�
	 * @param pars
	 * @return
	 */
	public static List<DBMSOption> e2jConvertOption(List<Pair> pars) {
		if (null == pars || pars.isEmpty()) {
			return Collections.<DBMSOption> emptyList();
		}
		List<DBMSOption> options = new ArrayList<DBMSOption>(pars.size());
		for (Pair par : pars) {
			DBMSOption option = e2jConvertOption(par);
			if (null != option) {
				options.add(option);
			}
		}
		return options;
	}

	/**ת��Erosa��option�����Ϊ������DBMSOption����
	 * @param pair
	 * @return
	 */
	public static DBMSOption e2jConvertOption(Pair pair) {
		if (null == pair) {
			return null;
		}
		return new DefaultOption(pair.getKey(), pair.getValue());
	}

	/**��ȡһ��list���������list��NULL�᷵��һ��empty��list
	 * @param list
	 * @return
	 */
	public static <T> List<T> getListDefaultEmpty(List<T> list) {
		if (null == list) {
			return Collections.<T> emptyList();
		}
		return list;
	}

	/**��ȡErosa��Header�е��ַ���λ��
	 * @param header
	 * @return
	 */
	public static String getErosaPosition(Header header) {
		return JingWeiUtil.getStrPosition(header.getLogfilename(), String.valueOf(header.getLogfileoffset()), null,
				String.valueOf(header.getExecutetime()));
	}

	private interface ColumnIteratorCall {
		public void call(Column column);
	}

	private static Map<String/*columnName*/, Serializable> e2jConvertColumnsValueWitchIteratorCall(
			List<Column> columns, ColumnIteratorCall iteratorCall, DBType dbType) throws Exception {
		Map<String, Serializable> mapData = new HashMap<String, Serializable>(columns.size());
		for (Column column : columns) {
			String columnName = StringUtil.toLowerCase(column.getName());
			mapData.put(columnName,
					column.getIsNull() ? null : e2jConvertRowValue(column.getValue(), column.getType(), dbType));
			if (null != iteratorCall) {
				iteratorCall.call(column);
			}
		}
		return mapData;
	}
}