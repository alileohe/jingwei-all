package com.taobao.jingwei.core.util;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.dynacode.DynaCode;
import com.taobao.jingwei.common.node.applier.EventFilterNode;
import com.taobao.jingwei.common.node.applier.EventFilterNode.ColumnFilterConditionNode;
import com.taobao.tddl.dbsync.dbms.DBMSAction;
import com.taobao.tddl.dbsync.dbms.DBMSColumn;
import com.taobao.tddl.dbsync.dbms.DBMSRowChange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * @desc DBMSChangeRow过滤器
 * @date Mar 22, 2012 4:26:31 PM
 */

public final class DBMSEventFileFilter implements IDBMSEventFilter {
    private Log log = LogFactory.getLog(DBMSEventFileFilter.class);

    private final FilterCondition condition;
    private final Set<Pattern> schemaPatterns;
    private Method dynaMethod;
    private Object dynaObject;

    public DBMSEventFileFilter(FilterCondition condition) throws Exception {
        this.condition = condition;
        Set<String> schemaRegs = this.condition.getSchemaRegs();
        this.schemaPatterns = new HashSet<Pattern>(schemaRegs.size());
        for (String schemaPattern : schemaRegs) {
            schemaPatterns.add(Pattern.compile(schemaPattern));
        }
        //动态编译并且加载java的source代码
        if (StringUtil.isNotBlank(condition.getSourceCode())) {
            DynaCode dynaCode = new DynaCode(condition.getSourceCode());
            dynaCode.compileAndLoadClass();
            //获取对应的clazz
            Map<String, Class<?>> map = dynaCode.getLoadClass();
            //反射执行结果
            Class<?> clazz = map.get(DynaCode.getQualifiedName(condition.getSourceCode()));
            dynaObject = (Object) clazz.newInstance();
            dynaMethod = clazz.getMethod("convert", new Class<?>[]{DBMSRowChange.class});
        }
    }

    private DBMSRowChange invokeConvert(DBMSRowChange dbmsRowChange) throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        return (DBMSRowChange) this.dynaMethod.invoke(this.dynaObject, dbmsRowChange);
    }

    /**
     * fixme 字段过滤表达式，只找第一个匹配的
     */
    @Override
    public String acceptTable(String schemaReg, DBMSRowChange dbmsRowChange) {

//        String schema = dbmsRowChange.getSchema();
//
//        String schemaReg = this.findSchema(schema);
//        if (null == schemaReg) {
//            return null;
//        }

        String eventTable = dbmsRowChange.getTable();

        Set<String> logicTableNames = this.condition.getLogicTables(schemaReg);

        String matchLogicTable = this.filterTable(eventTable, logicTableNames);

        return matchLogicTable;
    }

    @Override
    public DBMSRowChange convert(DBMSRowChange dbmsRowChange) throws Exception {

        DBMSRowChange newDBMSRowChange = null;

        // 操作类型过滤
        DBMSRowChange afterActionFilterRowChange = this.acceptAction(dbmsRowChange);
        if (null == afterActionFilterRowChange) {
            return null;
        }

        if (null != this.dynaObject && null != this.dynaMethod) {
            // 如果动态编译字节不为空，进行动态代码处理
            try {
                newDBMSRowChange = this.invokeConvert(afterActionFilterRowChange);
                //如果被过滤光了，就返回NULL
                if (null != newDBMSRowChange && newDBMSRowChange.getRowSize() == 0) {
                    return null;
                }
            } catch (Exception e) {
                log.error("invoke Convert Error!", e);
                throw e;
            }
        } else {
            Map<String, HashMap<String, ColumnFilterConditionNode>> map = condition.getFilterContion();
            if (map == null || map.isEmpty()) {
                return afterActionFilterRowChange;
            }

            // 配置了字段过滤条件
            String schema = dbmsRowChange.getSchema();
            String schemaReg = this.findSchema(schema);
            if (null == schemaReg) {
                return null;
            }
            String logicTable = this.acceptTable(schemaReg, dbmsRowChange);
            if (null == logicTable) {
                return null;
            }
            Set<String> includeColumnNames = this.getIncludeColumnNames(schemaReg, afterActionFilterRowChange,
                    logicTable);
            newDBMSRowChange = this.getDBMSRowChange(afterActionFilterRowChange, includeColumnNames);
        }

        return newDBMSRowChange;
    }

    /**
     * 获取过滤后要选取的列名
     *
     * @param dbmsRowChange
     * @return 包含的列 <code>empty set</code>表示所有列都不包含
     */
    private Set<String> getIncludeColumnNames(String schemaReg, DBMSRowChange dbmsRowChange, String logicTable) {

        boolean useIncludeRule = this.condition.isUseIncludeRule(schemaReg, logicTable);

        Set<String> includeColumnSet = null;

        if (useIncludeRule) {
            includeColumnSet = this.condition.getIncludeColumns(schemaReg, logicTable);
        } else {
            includeColumnSet = new HashSet<String>();
            List<? extends DBMSColumn> columns = dbmsRowChange.getColumns();
            for (DBMSColumn column : columns) {
                includeColumnSet.add(column.getName());
            }
            includeColumnSet.removeAll(this.condition.getExcludeColumns(schemaReg, logicTable));
        }

        return includeColumnSet;
    }

    /**
     * 根据包含的列过滤
     *
     * @param dbmsRowChange
     * @param includeColumnNames
     * @return 新的DBMSRowChange
     * @throws Exception
     */
    private DBMSRowChange getDBMSRowChange(DBMSRowChange dbmsRowChange, Set<String> includeColumnNames)
            throws Exception {
        DBMSAction action = dbmsRowChange.getAction();
        String schema = dbmsRowChange.getSchema();
        String table = dbmsRowChange.getTable();
        //创建builder
        RowChangeBuilder rowBuilder = RowChangeBuilder.createBuilder(schema, table, action);

        // 元数据
        List<DBMSColumn> eventMetaColumns = new ArrayList<DBMSColumn>(includeColumnNames.size());
        for (String includeColumnName : includeColumnNames) {
            // 元数据所有列
            DBMSColumn eventMetaColumn = dbmsRowChange.getColumnSet().findColumn(includeColumnName);
            eventMetaColumns.add(eventMetaColumn);
        }
        //如果column都被过滤光了返回null不调用builder
        if (eventMetaColumns.isEmpty()) {
            return null;
        }
        rowBuilder.addMetaColumns(eventMetaColumns);

        int rowSize = dbmsRowChange.getRowSize();
        // 填充数据
        for (int rownum = 1; rownum <= rowSize; rownum++) {
            Map<String, Serializable> rowDataMap = new HashMap<String, Serializable>(rowSize);
            for (DBMSColumn column : rowBuilder.getMetaColumns()) {
                rowDataMap.put(column.getName(), dbmsRowChange.getRowValue(rownum, column.getName()));
            }
            rowBuilder.addRowData(rowDataMap);
            if (DBMSAction.UPDATE == action) {
                // change data
                int changeColumnCount = dbmsRowChange.getChangeColumns().size();
                Map<String, Serializable> changeRowDataMap = new HashMap<String, Serializable>(changeColumnCount);
                for (DBMSColumn changeColumn : dbmsRowChange.getChangeColumns()) {
                    String changeColumnName = changeColumn.getName();
                    if (includeColumnNames.contains(changeColumnName)) {
                        Serializable value = dbmsRowChange.getChangeValue(rownum, changeColumnName);
                        changeRowDataMap.put(changeColumnName, value);
                    }
                }
                rowBuilder.addChangeRowData(changeRowDataMap);
            }
        }
        // options
        rowBuilder.addOptions(dbmsRowChange.getOptions());
        return rowBuilder.build();
    }

    /**
     * 如果给定库名在列表中存在满足某逻辑表名条件，返回给定库名对应的逻辑表名 如果给定表名在列表中没有满足逻辑库名条件
     *
     * @param eventSchema 实际表名
     * @return <code>String</code>库名，<code>null</code>库名不匹配
     */
    private String findSchema(String eventSchema) {
        for (Pattern pattern : this.schemaPatterns) {
            Matcher schemaMacher = pattern.matcher(eventSchema);

            if (schemaMacher.matches()) {
                return pattern.pattern();
            }
        }
        return null;
    }

    /**
     * 如果给定表名在列表中存在满足某逻辑表名条件，返回给定表名对应的逻辑表名 如果给定表名在列表中没有满足逻辑表名条件
     *
     * @param eventTable      实际表名
     * @param logicTableNames 逻辑表名列表,可能包含真实表名
     * @return 匹配的逻辑表名，<code>null</code>表名不匹配
     */
    private String filterTable(String eventTable, Set<String> logicTableNames) {

        for (String logicTableName : logicTableNames) {

            if (StringUtil.equals(eventTable, logicTableName)) {
                return eventTable;
            }

            int tableNameIndex = eventTable.indexOf(logicTableName);

            if (tableNameIndex == 0) {
                if (eventTable.length() > logicTableName.length() + 1) {
                    String split = eventTable.substring(logicTableName.length(), logicTableName.length() + 1);
                    String prefix = eventTable.substring(logicTableName.length() + 1);
                    if (StringUtil.equals("_", split) && StringUtil.isNotBlank(prefix) && StringUtil.isNumeric(prefix)) {
                        return logicTableName;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String acceptSchema(DBMSRowChange dbmsRowChange) {
        String schema = dbmsRowChange.getSchema();

        String schemaReg = this.findSchema(schema);
        return schemaReg;
    }

    @Override
    public DBMSRowChange acceptAction(DBMSRowChange dbmsRowChange) {
        DBMSAction actionType = dbmsRowChange.getAction();

        if (actionType == DBMSAction.INSERT && this.condition.getIncludeInsert() || actionType == DBMSAction.UPDATE
                && this.condition.getIncludeUpdate() || actionType == DBMSAction.DELETE
                && this.condition.getIncludeDelete()) {
            return dbmsRowChange;
        } else {
            if (log.isInfoEnabled()) {
                log.info("event is filtered out, action type is " + actionType);
            }

            return null;
        }
    }

    public static class FilterCondition {

        private volatile Map<String/*schema*/, HashMap<String/* sourceLogicTable */, ColumnFilterConditionNode>/* columnNames */> filterContion = new HashMap<String, HashMap<String, ColumnFilterConditionNode>>();

        private Boolean includeInsert = Boolean.TRUE;
        private Boolean includeUpdate = Boolean.TRUE;
        private Boolean includeDelete = Boolean.TRUE;
        private String sourceCode;

        public FilterCondition() {
        }

        public FilterCondition(EventFilterNode eventFilterNode) {
            this.filterContion = eventFilterNode.getConditions();
            this.includeInsert = eventFilterNode.getIncludeInsert();
            this.includeUpdate = eventFilterNode.getIncludeUpdate();
            this.includeDelete = eventFilterNode.getIncludeDelete();
            this.sourceCode = eventFilterNode.getSourceCode();
        }

        public Map<String, HashMap<String, ColumnFilterConditionNode>> getFilterContion() {
            return filterContion;
        }

        public void setFilterContion(Map<String, HashMap<String, ColumnFilterConditionNode>> filterContion) {
            this.filterContion = filterContion;
        }

        public boolean containsSchema(String schema) {
            return filterContion.containsKey(schema);
        }

        public boolean containsTable(String schema, String table) {
            if (!this.containsSchema(schema)) {
                return false;
            }

            Set<String> tables = filterContion.get(schema).keySet();

            return tables.contains(table);
        }

        public Set<String> getLogicTables(String schema) {
            return filterContion.get(schema).keySet();
        }

        public Set<String> getIncludeColumns(String schema, String table) {
            if (!this.containsTable(schema, table)) {
                return Collections.<String>emptySet();
            }
            return this.filterContion.get(schema).get(table).getIncludeColumns();
        }

        public Set<String> getExcludeColumns(String schema, String table) {
            if (!this.containsTable(schema, table)) {
                return Collections.<String>emptySet();
            }

            return this.filterContion.get(schema).get(table).getExcludeColumns();
        }

        public boolean isUseIncludeRule(String schema, String table) {
            if (!this.containsTable(schema, table)) {
                return true;
            }

            return this.filterContion.get(schema).get(table).isUseIncludeRule();
        }

        public Set<String> getSchemaRegs() {
            return this.filterContion.keySet();
        }

        public Boolean getIncludeInsert() {
            return includeInsert;
        }

        public void setIncludeInsert(Boolean includeInsert) {
            this.includeInsert = includeInsert;
        }

        public Boolean getIncludeUpdate() {
            return includeUpdate;
        }

        public void setIncludeUpdate(Boolean includeUpdate) {
            this.includeUpdate = includeUpdate;
        }

        public Boolean getIncludeDelete() {
            return includeDelete;
        }

        public void setIncludeDelete(Boolean includeDelete) {
            this.includeDelete = includeDelete;
        }

        public String getSourceCode() {
            return sourceCode;
        }
    }
}

interface IDBMSEventFilter {
    /**
     * 判断是否满足table匹配
     * @param schemaReg event中的实际库名需满足这个库名表达式
     * @param dbmsRowChange
     * @return 逻辑表名满足table匹配, <code>null</code>不满足table匹配，或者真是库名不满足库名表达式
     */
    String acceptTable(String schemaReg, DBMSRowChange dbmsRowChange);

    /**
     * 获取字段过滤后的DBMSRowChange
     *
     * @param dbmsRowChange
     * @return 字段过滤后的DBMSRowChange, <code>null</code>不满足schema和table匹配
     * @throws Exception
     */
    DBMSRowChange convert(DBMSRowChange dbmsRowChange) throws Exception;

    /**
     * 判断是否满足schema匹配
     *
     * @param dbmsRowChange
     * @return 逻辑表名满足schema匹配, <code>null</code>不满足schema匹配
     */
    String acceptSchema(DBMSRowChange dbmsRowChange);

    /**
     * 判断是否过滤insert、update、delete类型
     *
     * @param dbmsRowChange
     * @return insert、updatea、delete三种类型的DBMSRowChange, <code>null</code>不符合类型过滤条件
     */
    DBMSRowChange acceptAction(DBMSRowChange dbmsRowChange);
}