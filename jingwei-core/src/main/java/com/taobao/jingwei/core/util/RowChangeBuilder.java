package com.taobao.jingwei.core.util;

import com.alibaba.common.lang.StringUtil;
import com.taobao.tddl.dbsync.dbms.*;
import org.apache.commons.lang.ClassUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Types;
import java.util.*;

/**
 * <p>description: RowChange���������㹹��RowChange<p>
 * <p/>
 * 1. ��ʹ�þ�̬������ȡRowChangeBuilderʵ������
 * 2. ʹ��addMetaColumnϵ�з�������column��meta��Ϣ��
 * 3. ʹ��addRowDataЩ�з������������ݵ�ֵ��ע��ֵ�ĸ���������metaColumn����һ��
 * <p/>
 * 4. �����update��������addRowData���������addChangeRowDataϵ�з��������ֶα仯
 * ���ֵ��ע��changeData���������������metaColumn�У�����Ƕ��б仯���ݣ����м�
 * ��changeData�еı仯�ֶα�����ȫһ�¡�
 *
 * @author <a href="mailto:qihao@taobao.com">qihao</a>
 * @version 1.0
 * @{#} RowChangeEventBuilder.java Create on Jun 5, 2012 9:46:32 AM
 * <p/>
 * Copyright (c) 2012 by qihao.
 */
public class RowChangeBuilder {

    private DBMSAction action;

    private String schema;

    private String table;

    private final List<DBMSColumn> metaColumns = new ArrayList<DBMSColumn>();

    private final List<DBMSOption> options = new ArrayList<DBMSOption>();

    private final List<Map<String, Serializable>> rowDatas = new ArrayList<Map<String, Serializable>>();

    private final List<Map<String, Serializable>> changeRowDatas = new ArrayList<Map<String, Serializable>>();

    private final static int DEFAULT_ORDINAL_INDEX = -1;
    private final static boolean DEFAULT_SIGNED = true;
    private final static boolean DEFAULT_NULLABLE = true;
    private final static boolean DEFAULT_PRIMARYKEY = false;

    private int currentFieldIndex = 1;

    private RowChangeBuilder(String schema, String table, DBMSAction action) {
        this.schema = StringUtil.toLowerCase(schema);
        this.table = StringUtil.toLowerCase(table);
        this.action = action;
    }

    public static RowChangeBuilder createBuilder(String schema, String table, DBMSAction action) {
        return new RowChangeBuilder(schema, table, action);
    }

    public static RowChangeBuilder createFromEvent(DBMSRowChange event) {
        //ת����MAP�ṹ����
        RowChangeHelper.MapData mapData = RowChangeHelper.convertEvent2Map(event);
        RowChangeBuilder builder = new RowChangeBuilder(mapData.getDbName(), mapData.getTableName(), mapData.getAction());
        //��ñ�ͷ
        List<? extends DBMSColumn> oMetaDatas = event.getColumns();
        List<DBMSColumn> nMetaDatas = new ArrayList<DBMSColumn>(oMetaDatas.size());
        //copy old metaData
        nMetaDatas.addAll(oMetaDatas);
        builder.addMetaColumns(nMetaDatas);
        //���仯ǰ����
        builder.addRowDatas(mapData.getRowDataMaps());
        //���仯��ķ�����
        builder.addChangeRowDatas(mapData.getModifyRowDataMaps());
        //���OPTIONS
        builder.addOptions(event.getOptions());
        return builder;
    }

    public synchronized DBMSRowChange build() throws Exception {
        //��Ҫ�������
        if (StringUtil.isBlank(this.schema)) {
            throw new Exception("schema is empty!");
        }
        if (StringUtil.isBlank(this.table)) {
            throw new Exception("table is empty!");
        }
        if (null == this.action) {
            throw new Exception("action is null!");
        }
        if (this.metaColumns.isEmpty()) {
            throw new Exception("metaColumns is empty!");
        }
        if (this.rowDatas.isEmpty()) {
            throw new Exception("rowData is empty!");
        }
        //��ȡ�����ֶ����ƣ������������ж��ֶ��Ƿ����
        Set<String> metaColumnNames = new HashSet<String>(metaColumns.size());
        for (DBMSColumn column : metaColumns) {
            metaColumnNames.add(StringUtil.toLowerCase(column.getName()));
        }
        // ����columnSet
        DBMSColumnSet dbmsColumnSet = new DefaultColumnSet(metaColumns);
        DBMSRowChange rowChange = new DefaultRowChange(action, schema, table, dbmsColumnSet);
        //��ȡ�ܼ�¼��
        int rowCount = this.rowDatas.size();
        // ���Row����
        for (int i = 0; i < rowCount; i++) {
            Map<String, Serializable> dataMap = this.rowDatas.get(i);
            if (metaColumnNames.size() != dataMap.size()) {
                throw new Exception("rowData column Count not match metaColumn count!!");
            }
            // ȫ������
            for (Map.Entry<String, Serializable> entry : dataMap.entrySet()) {
                String columnName = StringUtil.toLowerCase(entry.getKey());
                if (!metaColumnNames.contains(columnName)) {
                    throw new Exception("metaColumns not contains rowData column: " + columnName);
                }
                rowChange.setRowValue(i + 1, columnName, entry.getValue());
            }
            if (DBMSAction.UPDATE == action) {
                if (!this.changeRowDatas.isEmpty()) {
                    if (this.rowDatas.size() != this.changeRowDatas.size()) {
                        throw new Exception("update  changeRowData Count not Match rowData Count!");
                    }
                    // ���RowChange����
                    Set<String> changeColumnNames = new HashSet<String>(metaColumns.size());
                    Map<String, Serializable> changeDataMap = this.changeRowDatas.get(i);
                    changeColumnNames.addAll(changeDataMap.keySet());

                    for (Map.Entry<String, Serializable> entry : changeDataMap.entrySet()) {
                        String columnName = StringUtil.toLowerCase(entry.getKey());
                        if (!metaColumnNames.contains(columnName)) {
                            throw new Exception("metaColumns not contains rowChangeData column: " + columnName);
                        }
                        rowChange.setChangeValue(i + 1, columnName, entry.getValue());
                        changeColumnNames.add(columnName);
                    }
                }
            }
        }
        // ��� options
        for (DBMSOption option : this.options) {
            rowChange.setOptionValue(option.getName(), option.getValue());
        }
        return rowChange;
    }

    public void addOptions(List<? extends DBMSOption> list) {
        if (null != list && !list.isEmpty()) {
            this.options.addAll(list);
        }
    }

    public void addOption(DBMSOption option) {
        this.options.add(option);
    }

    public void addOption(String name, Serializable value) {
        this.options.add(new DefaultOption(name, value));
    }

    public void addRowDatas(List<Map<String, Serializable>> rowDatas) {
        if (null != rowDatas && !rowDatas.isEmpty()) {
            this.rowDatas.addAll(rowDatas);
        }
    }

    public void addChangeRowDatas(List<Map<String, Serializable>> changeRowDatas) {
        if (null != changeRowDatas && !changeRowDatas.isEmpty()) {
            this.changeRowDatas.addAll(changeRowDatas);
        }
    }

    public void addRowData(Map<String, Serializable> rowData) {
        if (null != rowData && !rowData.isEmpty()) {
            this.rowDatas.add(rowData);
        }
    }

    public void addChangeRowData(Map<String, Serializable> changeRowData) {
        if (null != changeRowData && !changeRowData.isEmpty()) {
            this.changeRowDatas.add(changeRowData);
        }
    }

    public void addMetaColumns(Collection<DBMSColumn> metaColumns) {
        if (null != metaColumns && !metaColumns.isEmpty()) {
            List<DBMSColumn> newColumns = new ArrayList<DBMSColumn>(metaColumns.size());
            for (DBMSColumn column : metaColumns) {
                newColumns.add(cloneDBMSColumn(column));
            }
            this.metaColumns.addAll(newColumns);
        }
    }

    public void addMetaColumn(DBMSColumn metaColumn) {
        this.metaColumns.add(this.cloneDBMSColumn(metaColumn));
    }

    public void addMetaColumnWithoutClone(DBMSColumn metaColumn) {
        this.metaColumns.add(metaColumn);
    }

    public void addMetaColumn(String name, Class<?> valueClass) {
        this.addMetaColumn(name, getSqyTypeByClass(valueClass));
    }

    public void addMetaColumn(String name, Class<?> valueClass, boolean primaryKey) {

        this.addMetaColumn(name, getSqyTypeByClass(valueClass), primaryKey);
    }

    public void addMetaColumn(String name, Class<?> valueClass, boolean nullable, boolean primaryKey) {
        this.addMetaColumn(name, getSqyTypeByClass(valueClass), DEFAULT_SIGNED, nullable, primaryKey);
    }

    public void addMetaColumn(String name, Class<?> valueClass, boolean signed, boolean nullable, boolean primaryKey) {
        this.addMetaColumn(name, getSqyTypeByClass(valueClass), signed, nullable, primaryKey);
    }

    public void addMetaColumn(String name, Class<?> valueClass, int ordinalIndex, boolean signed, boolean nullable,
                              boolean primaryKey) {
        this.addMetaColumn(name, ordinalIndex, getSqyTypeByClass(valueClass), signed, nullable, primaryKey);
    }

    public void addMetaColumn(String name, int sqlType) {
        this.addMetaColumn(name, currentFieldIndex++, sqlType, DEFAULT_SIGNED, DEFAULT_NULLABLE, DEFAULT_PRIMARYKEY);
    }

    public void addMetaColumn(String name, int sqlType, boolean primaryKey) {
        this.addMetaColumn(name, currentFieldIndex++, sqlType, DEFAULT_SIGNED, DEFAULT_NULLABLE, primaryKey);
    }

    public void addMetaColumn(String name, int sqlType, boolean nullable, boolean primaryKey) {
        this.addMetaColumn(name, currentFieldIndex++, sqlType, DEFAULT_SIGNED, nullable, primaryKey);
    }

    public void addMetaColumn(String name, int sqlType, boolean signed, boolean nullable, boolean primaryKey) {
        this.addMetaColumn(name, currentFieldIndex++, sqlType, signed, nullable, primaryKey);
    }

    public void addMetaColumn(String name, int ordinalIndex, int sqlType, boolean signed, boolean nullable,
                              boolean primaryKey) {
        this.metaColumns.add(createMetaColumn(name, ordinalIndex, sqlType, signed, nullable, primaryKey));
    }

    public void addMetaColumn(String name, int ordinalIndex, int sqlType, boolean nullable, boolean primaryKey) {
        this.addMetaColumn(name, ordinalIndex, sqlType, DEFAULT_SIGNED, nullable, primaryKey);
    }

    public static DBMSColumn createMetaColumn(String name, int sqlType) {
        return createMetaColumn(name, DEFAULT_ORDINAL_INDEX, sqlType, DEFAULT_SIGNED, DEFAULT_NULLABLE,
                DEFAULT_PRIMARYKEY);
    }

    public static DBMSColumn createMetaColumn(String name, int sqlType, boolean primaryKey) {
        return createMetaColumn(name, DEFAULT_ORDINAL_INDEX, sqlType, DEFAULT_SIGNED, DEFAULT_NULLABLE, primaryKey);
    }

    public static DBMSColumn createMetaColumn(String name, int sqlType, boolean nullable, boolean primaryKey) {
        return createMetaColumn(name, DEFAULT_ORDINAL_INDEX, sqlType, DEFAULT_SIGNED, nullable, primaryKey);
    }

    public static DBMSColumn createMetaColumn(String name, int ordinalIndex, int sqlType, boolean nullable,
                                              boolean primaryKey) {
        return createMetaColumn(name, ordinalIndex, sqlType, DEFAULT_SIGNED, nullable, primaryKey);
    }

    public static DBMSColumn createMetaColumn(String name, int sqlType, boolean signed, boolean nullable,
                                              boolean primaryKey) {
        return createMetaColumn(name, DEFAULT_ORDINAL_INDEX, sqlType, signed, nullable, primaryKey);
    }

    public static DBMSColumn createMetaColumn(String name, int ordinalIndex, int sqlType, boolean signed,
                                              boolean nullable, boolean primaryKey) {
        DBMSColumn newColumn = new DefaultColumn(StringUtil.toLowerCase(name), ordinalIndex, sqlType, signed, nullable,
                primaryKey);
        return newColumn;
    }

    /**
     * ����value��class��signed��ȡ��Ӧ��sqlType
     * �ο�mysql����com.mysql.jdbc.ResultSetMetaData.getClassNameForJavaType(int, boolean, int, boolean, boolean)
     *
     * @param valueClass
     * @return
     */
    public static int getSqyTypeByClass(Class<?> valueClass) {
        if (null == valueClass) {
            return Types.NULL;
        }
        //��¼������
        boolean isArray = valueClass.isArray();
        //���������ȡ������class,������ԭ�ȵ�class
        valueClass = isArray ? valueClass.getComponentType() : valueClass;
        //�����С�Ļ������ͣ���ת���ɴ�İ�װ����
        valueClass = valueClass.isPrimitive() ? ClassUtils.primitiveToWrapper(valueClass) : valueClass;
        if (isArray) {
            if (Byte.class.equals(valueClass)) {
                return Types.LONGVARBINARY;
            }
        } else {
            if (valueClass.isPrimitive()) {
                valueClass = ClassUtils.primitiveToWrapper(valueClass);
            }
            if (Boolean.class.equals(valueClass)) {
                return Types.BOOLEAN;
            } else if (Integer.class.equals(valueClass)) {
                return Types.INTEGER;
            } else if (Long.class.equals(valueClass)) {
                return Types.BIGINT;
            } else if (BigDecimal.class.equals(valueClass)) {
                return Types.DECIMAL;
            } else if (Float.class.equals(valueClass)) {
                return Types.REAL;
            } else if (Double.class.equals(valueClass)) {
                return Types.DOUBLE;
            } else if (String.class.equals(valueClass)) {
                return Types.VARCHAR;
            } else if (java.sql.Date.class.equals(valueClass) || java.util.Date.class.equals(valueClass)) {
                return Types.DATE;
            } else if (java.sql.Time.class.equals(valueClass)) {
                return Types.TIME;
            } else if (java.sql.Timestamp.class.equals(valueClass)) {
                return Types.TIMESTAMP;
            }
        }
        throw new RuntimeException("not support java Type class: " + valueClass.getName());
    }

    /**
     * ����һ��DBMSColumn Ҫ��DBMSColumnʵ��cloneable�ͺ���
     *
     * @param dbmsColumn
     * @return ���ƺ� <code>new</code> ������DBMSColumn
     */
    private DBMSColumn cloneDBMSColumn(DBMSColumn dbmsColumn) {
        String name = StringUtil.toLowerCase(dbmsColumn.getName());
        int ordinalIndex = dbmsColumn.getOrdinalIndex();
        int sqlType = dbmsColumn.getSqlType();
        boolean signed = dbmsColumn.isSigned();
        boolean nullable = dbmsColumn.isNullable();
        boolean primaryKey = dbmsColumn.isPrimaryKey();
        DBMSColumn newColumn = new DefaultColumn(name, ordinalIndex, sqlType, signed, nullable, primaryKey);
        return newColumn;
    }

    public DBMSAction getAction() {
        return action;
    }

    public String getSchema() {
        return schema;
    }

    public String getTable() {
        return table;
    }

    public List<DBMSColumn> getMetaColumns() {
        return metaColumns;
    }

    public List<? extends DBMSOption> getOptions() {
        return options;
    }

    public List<Map<String, Serializable>> getRowDatas() {
        return rowDatas;
    }

    public List<Map<String, Serializable>> getChangeRowDatas() {
        return changeRowDatas;
    }
}