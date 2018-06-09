package com.taobao.jingwei.core.util;

import com.alibaba.common.lang.StringUtil;
import com.taobao.tddl.dbsync.dbms.DBMSAction;
import com.taobao.tddl.dbsync.dbms.DBMSColumn;
import com.taobao.tddl.dbsync.dbms.DBMSRowChange;
import com.taobao.tddl.dbsync.dbms.DBMSRowData;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EVENT������
 * <p/>
 * <p>description:<p> ��eventת����KEY-VALUE
 *
 * @author <a href="mailto:qihao@taobao.com">qihao</a>
 * @version 1.0
 * @{#} JingWeiEventHelper.java Create on Mar 12, 2012 11:01:04 AM
 * <p/>
 * Copyright (c) 2012 by qihao.
 */
public class RowChangeHelper {


    public static MapData convertEvent2Map(DBMSRowChange event) {
        return convertEvent2Map(event, Boolean.FALSE);
    }

    /**
     * ��DBMSRowChange ת����K-V ��MAP��ʽ
     *
     * @param event     DBMSRowChange
     * @param upperCase �Ƿ�dbname,table,column������ת���ɴ�д
     * @return MapData ����
     */
    public static MapData convertEvent2Map(DBMSRowChange event, boolean upperCase) {
        // ��ȡ���ݵ�������Ϣ
        String dbName = upperCase ? StringUtil.toUpperCase(event.getSchema()) : StringUtil.toLowerCase(event
                .getSchema());
        String tableName = upperCase ? StringUtil.toUpperCase(event.getTable()) : StringUtil.toLowerCase(event
                .getTable());
        DBMSAction action = event.getAction();

        //����MapData
        MapData mapData = new MapData(dbName, tableName, action);

        // ���� primaryKey��DataInfo����������
        List<DBMSColumn> primaryKeyColumns = event.getPrimaryKey();
        List<String> primaryColumnNames = new ArrayList<String>(primaryKeyColumns.size());
        for (DBMSColumn primaryKey : primaryKeyColumns) {
            String primaryName = upperCase ? StringUtil.toUpperCase(primaryKey.getName()) : StringUtil
                    .toLowerCase(primaryKey.getName());
            primaryColumnNames.add(primaryName);
        }
        mapData.setPrimaryKeyNames(primaryColumnNames);

        // �����update����changeColumnNames��DataInfo����������
        if (DBMSAction.UPDATE == action) {
            List<? extends DBMSColumn> changeColumns = event.getChangeColumns();
            List<String> changeColumnNames = new ArrayList<String>(changeColumns.size());
            for (DBMSColumn changeKey : changeColumns) {
                String changeColumnName = upperCase ? StringUtil.toUpperCase(changeKey.getName()) : StringUtil
                        .toLowerCase(changeKey.getName());
                changeColumnNames.add(changeColumnName);
            }
            mapData.setModifiedFieldNames(changeColumnNames);
        }

        int rowSize = event.getRowSize();
        List<? extends DBMSColumn> allColumns = event.getColumns();
        if (DBMSAction.UPDATE == action) {
            // ��ȡ�仯��Column�б�
            List<? extends DBMSColumn> modifyColumns = event.getChangeColumns();
            // ׼������Dispatcherʹ�õ��޸�ǰ�����б����޸ĺ������б�
            List<Map<String, Serializable>> rowDataMaps = new ArrayList<Map<String, Serializable>>(rowSize);
            List<Map<String, Serializable>> modifyRowDataMaps = new ArrayList<Map<String, Serializable>>(rowSize);
            for (int i = 1; i <= rowSize; i++) {
                Map<String, Serializable> rowDataMap = new HashMap<String, Serializable>(allColumns.size());
                Map<String, Serializable> modifyRowDataMap = new HashMap<String, Serializable>(allColumns.size());
                DBMSRowData rowData = event.getRowData(i);
                for (DBMSColumn column : allColumns) {
                    String columnName = upperCase ? StringUtil.toUpperCase(column.getName()) : StringUtil
                            .toLowerCase(column.getName());
                    Serializable columnValue = rowData.getRowValue(column);
                    // �����޸�ǰrowDataMap
                    rowDataMap.put(columnName, columnValue);
                    if (modifyColumns.contains(column)) {
                        // �����ǰ�ֶ����޸ĵ��ֶΣ�ʹ���޸ĵ�ֵ
                        Serializable modifyColumnValue = event.getChangeValue(i, column);
                        modifyRowDataMap.put(columnName, modifyColumnValue);
                    }
                }
                rowDataMaps.add(rowDataMap);
                modifyRowDataMaps.add(modifyRowDataMap);
            }
            mapData.setRowDataMaps(rowDataMaps);
            mapData.setModifyRowDataMaps(modifyRowDataMaps);
        } else if (DBMSAction.INSERT == action || DBMSAction.DELETE == action) {
            List<Map<String, Serializable>> rowDataMaps = new ArrayList<Map<String, Serializable>>(rowSize);
            for (int i = 1; i <= rowSize; i++) {
                Map<String, Serializable> rowDataMap = new HashMap<String, Serializable>(allColumns.size());
                DBMSRowData rowData = event.getRowData(i);
                for (DBMSColumn column : allColumns) {
                    String columnName = upperCase ? StringUtil.toUpperCase(column.getName()) : StringUtil
                            .toLowerCase(column.getName());
                    Serializable columnValue = rowData.getRowValue(column);
                    rowDataMap.put(columnName, columnValue);
                }
                rowDataMaps.add(rowDataMap);
            }
            mapData.setRowDataMaps(rowDataMaps);
        }
        return mapData;
    }

    public static DBMSRowChange removeRowData(DBMSRowChange event, RemoveCondition condition) {
        if (null == event) {
            return null;
        }
        int rowCount = event.getRowSize();
        for (int i = rowCount; i > 0; i--) {
            DBMSRowData rowData = event.getRowData(i);
            if (condition.isRemove(event, rowData)) {
                event.removeRowData(i);
            }
        }
        if (event.getRowSize() == 0) {
            return null;
        } else {
            return event;
        }
    }

    public static interface RemoveCondition {
        public boolean isRemove(DBMSRowChange event, DBMSRowData rowData);
    }

    public static class MapData {
        /**
         * ���ݿ���
         */
        private String dbName;
        /**
         * ����
         */
        private String tableName;

        /**
         * ��������
         */
        private List<String> primaryKeyNames;
        /**
         * �޸ĺ��ֶ����ƣ�ֻ��Update��ʱ�����
         */
        private List<String> modifiedFieldNames;

        /**
         * INSERT��UPDATE��DELETE ��Ӧ��ö����
         */
        private DBMSAction action;

        /**
         * �޸�ǰ���ݣ�Insert,Update,Deleteʱ����
         */
        private List<Map<String, Serializable>> rowDataMaps;

        /**
         * �޸ĺ�����,ֻ��Update��ʱ�����
         */
        private List<Map<String, Serializable>> modifyRowDataMaps;

        public MapData(String dbName, String tableName, DBMSAction action) {
            this.dbName = dbName;
            this.tableName = tableName;
            this.action = action;
        }

        public String getDbName() {
            return dbName;
        }

        public String getTableName() {
            return tableName;
        }

        public List<String> getPrimaryKeyNames() {
            return primaryKeyNames;
        }

        public void setPrimaryKeyNames(List<String> primaryKeyNames) {
            this.primaryKeyNames = primaryKeyNames;
        }

        public List<String> getModifiedFieldNames() {
            return modifiedFieldNames;
        }

        public void setModifiedFieldNames(List<String> modifiedFieldNames) {
            this.modifiedFieldNames = modifiedFieldNames;
        }

        public DBMSAction getAction() {
            return action;
        }

        public List<Map<String, Serializable>> getRowDataMaps() {
            return rowDataMaps;
        }

        public void setRowDataMaps(List<Map<String, Serializable>> rowDataMaps) {
            this.rowDataMaps = rowDataMaps;
        }

        public List<Map<String, Serializable>> getModifyRowDataMaps() {
            return modifyRowDataMaps;
        }

        public void setModifyRowDataMaps(List<Map<String, Serializable>> modifyRowDataMaps) {
            this.modifyRowDataMaps = modifyRowDataMaps;
        }

        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }
}
