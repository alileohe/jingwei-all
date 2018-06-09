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
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * @desc DBMSChangeRow������
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
        //��̬���벢�Ҽ���java��source����
        if (StringUtil.isNotBlank(condition.getSourceCode())) {
            DynaCode dynaCode = new DynaCode(condition.getSourceCode());
            dynaCode.compileAndLoadClass();
            //��ȡ��Ӧ��clazz
            Map<String, Class<?>> map = dynaCode.getLoadClass();
            //����ִ�н��
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
     * fixme �ֶι��˱��ʽ��ֻ�ҵ�һ��ƥ���
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

        // �������͹���
        DBMSRowChange afterActionFilterRowChange = this.acceptAction(dbmsRowChange);
        if (null == afterActionFilterRowChange) {
            return null;
        }

        if (null != this.dynaObject && null != this.dynaMethod) {
            // �����̬�����ֽڲ�Ϊ�գ����ж�̬���봦��
            try {
                newDBMSRowChange = this.invokeConvert(afterActionFilterRowChange);
                //��������˹��ˣ��ͷ���NULL
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

            // �������ֶι�������
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
     * ��ȡ���˺�Ҫѡȡ������
     *
     * @param dbmsRowChange
     * @return �������� <code>empty set</code>��ʾ�����ж�������
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
     * ���ݰ������й���
     *
     * @param dbmsRowChange
     * @param includeColumnNames
     * @return �µ�DBMSRowChange
     * @throws Exception
     */
    private DBMSRowChange getDBMSRowChange(DBMSRowChange dbmsRowChange, Set<String> includeColumnNames)
            throws Exception {
        DBMSAction action = dbmsRowChange.getAction();
        String schema = dbmsRowChange.getSchema();
        String table = dbmsRowChange.getTable();
        //����builder
        RowChangeBuilder rowBuilder = RowChangeBuilder.createBuilder(schema, table, action);

        // Ԫ����
        List<DBMSColumn> eventMetaColumns = new ArrayList<DBMSColumn>(includeColumnNames.size());
        for (String includeColumnName : includeColumnNames) {
            // Ԫ����������
            DBMSColumn eventMetaColumn = dbmsRowChange.getColumnSet().findColumn(includeColumnName);
            eventMetaColumns.add(eventMetaColumn);
        }
        //���column�������˹��˷���null������builder
        if (eventMetaColumns.isEmpty()) {
            return null;
        }
        rowBuilder.addMetaColumns(eventMetaColumns);

        int rowSize = dbmsRowChange.getRowSize();
        // �������
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
     * ��������������б��д�������ĳ�߼��������������ظ���������Ӧ���߼����� ��������������б���û�������߼���������
     *
     * @param eventSchema ʵ�ʱ���
     * @return <code>String</code>������<code>null</code>������ƥ��
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
     * ��������������б��д�������ĳ�߼��������������ظ���������Ӧ���߼����� ��������������б���û�������߼���������
     *
     * @param eventTable      ʵ�ʱ���
     * @param logicTableNames �߼������б�,���ܰ�����ʵ����
     * @return ƥ����߼�������<code>null</code>������ƥ��
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
     * �ж��Ƿ�����tableƥ��
     * @param schemaReg event�е�ʵ�ʿ�������������������ʽ
     * @param dbmsRowChange
     * @return �߼���������tableƥ��, <code>null</code>������tableƥ�䣬�������ǿ���������������ʽ
     */
    String acceptTable(String schemaReg, DBMSRowChange dbmsRowChange);

    /**
     * ��ȡ�ֶι��˺��DBMSRowChange
     *
     * @param dbmsRowChange
     * @return �ֶι��˺��DBMSRowChange, <code>null</code>������schema��tableƥ��
     * @throws Exception
     */
    DBMSRowChange convert(DBMSRowChange dbmsRowChange) throws Exception;

    /**
     * �ж��Ƿ�����schemaƥ��
     *
     * @param dbmsRowChange
     * @return �߼���������schemaƥ��, <code>null</code>������schemaƥ��
     */
    String acceptSchema(DBMSRowChange dbmsRowChange);

    /**
     * �ж��Ƿ����insert��update��delete����
     *
     * @param dbmsRowChange
     * @return insert��updatea��delete�������͵�DBMSRowChange, <code>null</code>���������͹�������
     */
    DBMSRowChange acceptAction(DBMSRowChange dbmsRowChange);
}