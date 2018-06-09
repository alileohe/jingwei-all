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
 * EVENT工具类
 * <p/>
 * <p>description:<p> 将event转换成KEY-VALUE
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
     * 将DBMSRowChange 转换成K-V 的MAP形式
     *
     * @param event     DBMSRowChange
     * @param upperCase 是否将dbname,table,column的名称转换成大写
     * @return MapData 对象
     */
    public static MapData convertEvent2Map(DBMSRowChange event, boolean upperCase) {
        // 获取数据的描述信息
        String dbName = upperCase ? StringUtil.toUpperCase(event.getSchema()) : StringUtil.toLowerCase(event
                .getSchema());
        String tableName = upperCase ? StringUtil.toUpperCase(event.getTable()) : StringUtil.toLowerCase(event
                .getTable());
        DBMSAction action = event.getAction();

        //创建MapData
        MapData mapData = new MapData(dbName, tableName, action);

        // 设置 primaryKey到DataInfo描述对象中
        List<DBMSColumn> primaryKeyColumns = event.getPrimaryKey();
        List<String> primaryColumnNames = new ArrayList<String>(primaryKeyColumns.size());
        for (DBMSColumn primaryKey : primaryKeyColumns) {
            String primaryName = upperCase ? StringUtil.toUpperCase(primaryKey.getName()) : StringUtil
                    .toLowerCase(primaryKey.getName());
            primaryColumnNames.add(primaryName);
        }
        mapData.setPrimaryKeyNames(primaryColumnNames);

        // 如果是update设置changeColumnNames到DataInfo描述对象中
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
            // 获取变化的Column列表
            List<? extends DBMSColumn> modifyColumns = event.getChangeColumns();
            // 准备调用Dispatcher使用的修改前数据列表，和修改后数据列表
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
                    // 设置修改前rowDataMap
                    rowDataMap.put(columnName, columnValue);
                    if (modifyColumns.contains(column)) {
                        // 如果当前字段是修改的字段，使用修改的值
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
         * 数据库名
         */
        private String dbName;
        /**
         * 表名
         */
        private String tableName;

        /**
         * 主键名称
         */
        private List<String> primaryKeyNames;
        /**
         * 修改后字段名称，只有Update的时候才有
         */
        private List<String> modifiedFieldNames;

        /**
         * INSERT，UPDATE，DELETE 对应的枚举类
         */
        private DBMSAction action;

        /**
         * 修改前数据，Insert,Update,Delete时都有
         */
        private List<Map<String, Serializable>> rowDataMaps;

        /**
         * 修改后数据,只有Update的时候才有
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
