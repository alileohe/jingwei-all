package com.taobao.jingwei.core.internal.applier;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.node.applier.DataBaseApplierNode;
import com.taobao.jingwei.common.node.type.DBType;
import com.taobao.jingwei.core.util.TaskCoreUtil;
import com.taobao.tddl.client.jdbc.TDataSource;
import com.taobao.tddl.dbsync.applier.Applier;
import com.taobao.tddl.dbsync.applier.ApplierBrokenException;
import com.taobao.tddl.dbsync.applier.ApplierException;
import com.taobao.tddl.dbsync.dbms.*;
import com.taobao.tddl.dbsync.plugin.AbstractPlugin;
import com.taobao.tddl.dbsync.plugin.PluginContext;
import com.taobao.tddl.dbsync.plugin.PluginException;
import com.taobao.tddl.dbsync.tx.Tx;
import com.taobao.tddl.rule.le.TddlRule;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;

/**
 * <p>description:���ݿ�applieʵ�֣�֧��mysql/oracle����ͨ����<p>
 *
 * @author <a href="mailto:qihao@taobao.com">qihao</a>
 * @version 1.0
 * @{#} DataBaseApplier.java Create on Dec 15, 2011 1:38:03 PM
 * <p/>
 * Copyright (c) 2011 by qihao.
 */
public class DataBaseApplier extends AbstractPlugin implements Applier {

    private static Log logger = LogFactory.getLog(DataBaseApplier.class);

    /**
     * ����ʱʹ�õ�SQL��FORMAT
     */
    private static final String RPLACE_FORMATE = "REPLACE INTO {0} ({1}) VALUES ({2})";

    private static final String INSERT_FORMATE = "INSERT  INTO {0} ({1}) VALUES ({2})";

    private static final String UPDATE_FORMATE = "UPDATE {0} SET {1}";

    private static final String DELETE_FORMATE = "DELETE FROM {0} WHERE {1}";

    /**
     * MYSQL������ͻErrorCode
     */
    private static final int ERROR_CODE_DUPLICATE_PRIMARY_KEY_MYSQL = 1062;

    /**
     * ORACLE ������ͻErrorCode
     */
    private static final int ERROR_CODE_DUPLICATE_PRIMARY_KEY_ORACLE = 1;

    /**
     * д��ʹ�õ�����Դ
     */
    private DataSource dataSource;

    private TddlRule tddlRule;

    /**
     * DataBaseApplier�����ýڵ����
     */
    private DataBaseApplierNode dataBaseApplierNode;

    public DataBaseApplier(DataBaseApplierNode dataBaseApplierNode) {
        this.dataBaseApplierNode = dataBaseApplierNode;
    }

    public void init(String name, PluginContext context) throws PluginException, InterruptedException {
        super.init(name, context);
        //��������
        tddlRule = new TddlRule();
        //�����������Ϊ�գ���ʹ��matrix���Ƶ�����������
        tddlRule.setAppName(StringUtil.isNotBlank(this.dataBaseApplierNode.getRuleName()) ? this.dataBaseApplierNode
                .getRuleName() : this.dataBaseApplierNode.getMatrixName());
        tddlRule.init();
        if (DBType.ANDOR == this.dataBaseApplierNode.getDbType()) {
//            UstoreDataSource ds = new UstoreDataSource();
//            ds.setAppName(this.dataBaseApplierNode.getMatrixName());
//            try {
//                ds.init();
//                this.dataSource = ds;
//            } catch (Exception e) {
//                logger.error("UstoreDataSource init Error! ", e);
//                throw new PluginException("UstoreDataSource init Error", e);
//            }
        } else {
            // ����TDataSource
            TDataSource tdataSource = new TDataSource();
            tdataSource.setAppName(this.dataBaseApplierNode.getMatrixName());
            tdataSource.setDynamicRule(true);
            tdataSource.setTddlRule(tddlRule);
            tdataSource.init();
            this.dataSource = tdataSource;
        }
    }

    @Override
    public void apply(DBMSEvent event) throws ApplierException, InterruptedException {
        if (null != event && event instanceof DBMSRowChange) {
            DBMSRowChange rowChangeEvent = (DBMSRowChange) event;

            String schemaName = StringUtil.toLowerCase(rowChangeEvent.getSchema());
            // ������˷��ֲ�����Ҫ��Schema������������
            if (isIgnoreSchema(schemaName)) {
                return;
            }
            String fullTableName = StringUtil.toLowerCase(rowChangeEvent.getTable());
            // ���ݸ������Ʊ��б���й���
            String targetLogicTable = StringUtil.toLowerCase(filterTable(fullTableName,
                    this.dataBaseApplierNode.getLogicTableNames(), Boolean.FALSE));

            //��ȡԴʵ�ʱ��Ӧ���߼�����
            String sourceLogicTable = StringUtil.toLowerCase(filterTable(fullTableName,
                    this.dataBaseApplierNode.getLogicTableNames(), Boolean.TRUE));
            // ������Ʊ������в�������ǰ��������
            if (StringUtil.isBlank(targetLogicTable)) {
                return;
            }
            DBMSAction action = rowChangeEvent.getAction();
            RowSqlExceContext sqlContext = null;

            //��У��ԭʼ�в���Ϊ��
            List<? extends DBMSColumn> rowColumns = rowChangeEvent.getColumns();
            if (null == rowColumns || rowColumns.isEmpty()) {
                String msg = "update beforeColumns is empty!  schema: " + rowChangeEvent.getSchema() + " tableName: "
                        + rowChangeEvent.getTable();
                logger.warn(msg);
                return;
            }
            // ����action���ʹ���
            if (DBMSAction.INSERT == action) {
                sqlContext = getInsertSqlExceContext(rowChangeEvent, sourceLogicTable, targetLogicTable);
            } else if (DBMSAction.UPDATE == action) {

                //��update��ʱ��У��仯�в���Ϊ��
                List<? extends DBMSColumn> changeColumns = rowChangeEvent.getChangeColumns();
                if (null == changeColumns || changeColumns.isEmpty()) {
                    String msg = "update changeColumns is empty! schema: " + rowChangeEvent.getSchema()
                            + " tableName: " + rowChangeEvent.getTable();
                    logger.warn(msg);
                    return;
                }
                sqlContext = getUpdateSqlExceContext(rowChangeEvent, sourceLogicTable, targetLogicTable);
            } else if (DBMSAction.DELETE == action) {
                sqlContext = getDeleteSqlExceContext(rowChangeEvent, sourceLogicTable, targetLogicTable);
            } else {
                // ��֧�ֵ�action����
                logger.warn("DataBaseApplier not support Data Action! action: " + action);
                return;
            }
            try {
                if (null != sqlContext) {
                    exceSql(sqlContext);
                } else {
                    String msg = "DataBaseApplier exceSqlContext is Null Maybe All Column filted! ";
                    logger.warn(msg);
                }
            } catch (Throwable e) {
                Tx tx = Tx.local();
                logger.error(" applie event Error! Position: " + tx.getPosition(), e);
                if (!this.dataBaseApplierNode.isFailContinue()) {
                    //���û����failContinue�Ļ��ӳ��쳣��dbsyncֹͣ
                    TaskCoreUtil.errorApplierExceptionEvent(event, logger);
                    throw new ApplierBrokenException(e);
                }
            }
        }
    }

    /**
     * ִ�и���SQL
     *
     * @param rowSqlExceContext
     * @throws Exception
     */
    private void exceSql(RowSqlExceContext rowSqlExceContext) throws Exception {
        Connection conn = null;
        PreparedStatement ps = null;
        int count = 0;
        try {
            conn = dataSource.getConnection();
            for (List<Serializable> dataValues : rowSqlExceContext.rowDataList) {
                int i = 1;
                ps = conn.prepareStatement(rowSqlExceContext.sql);
                for (Serializable dataValue : dataValues) {
                    ps.setObject(i, dataValue);
                    i++;
                }
                try {
                    // ִ��SQL
                    ps.execute();
                    count++;
                } finally {
                    if (null != ps) {
                        ps.close();
                    }
                }
            }
        } catch (Exception e) {
            if (isDuplicateException(e, this.dataBaseApplierNode.getDbType())) {
                return;
            }
            StringBuffer msg = new StringBuffer("DataBaseApplier exceSql Error sql: " + rowSqlExceContext.sql);
            msg.append(JingWeiConstants.LINE_SEP);
            if (rowSqlExceContext.rowDataList.size() >= count) {
                List<Serializable> pramList = rowSqlExceContext.rowDataList.get(count);
                if (null != pramList) {
                    for (Serializable object : pramList) {
                        msg.append(object).append(",");
                    }
                }
            }
            msg.append(rowSqlExceContext.rowDataList);
            logger.error(msg.toString(), e);
            if (e instanceof SQLException && "current thread is interrupted!".equals(e.getMessage())) {
                throw new InterruptedException();
            } else {
                throw e;
            }
        } finally {
            if (null != conn) {
                conn.close();
            }
        }
    }

    /**
     * �ж��쳣�Ƿ���DuplicateException
     *
     * @param e
     * @return
     */
    private boolean isDuplicateException(Exception e, DBType dbType) {
        SQLException sqlException = null;
        if (e instanceof SQLException) {
            sqlException = (SQLException) e;
        } else {
            //�������SQLException,���ж�rootCause�Ƿ���SQLException
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause instanceof SQLException) {
                sqlException = (SQLException) rootCause;
            }
        }
        if (null != sqlException) {
            if ((DBType.MYSQL == dbType && ERROR_CODE_DUPLICATE_PRIMARY_KEY_MYSQL == sqlException.getErrorCode())
                    || (DBType.ORACLE == dbType && ERROR_CODE_DUPLICATE_PRIMARY_KEY_ORACLE == sqlException
                    .getErrorCode())) {
                return true;
            }
        }
        return false;
    }

    /**
     * ��DBMSRowChange ת���ɻ���UPDATEʹ�õ� SQLִ���õ�RowSqlExceContext�б�
     *
     * @param rowChangeEvent
     * @param targetLogicTable
     * @return
     * @throws ApplierException
     */
    private RowSqlExceContext getUpdateSqlExceContext(DBMSRowChange rowChangeEvent, String sourceLogicTable,
                                                      String targetLogicTable) throws ApplierException {

        // ���˺���ֶ��б�
        List<? extends DBMSColumn> rowColumns = this.filterColumn(rowChangeEvent.getColumns(), sourceLogicTable);
        if (null == rowColumns || rowColumns.isEmpty()) {
            //���������ȫ�������˵���
            logger.warn("DBSyncTask Update all columns has been filter! schema: " + rowChangeEvent.getSchema()
                    + " tableName: " + rowChangeEvent.getTable());
            return null;
        }

        List<? extends DBMSColumn> changeColumns = this.filterColumn(rowChangeEvent.getChangeColumns(),
                sourceLogicTable);
        if (null == changeColumns || changeColumns.isEmpty()) {
            //���������ȫ�������˵���
            logger.warn("DBSyncTask Update change columns has been filter! schema: " + rowChangeEvent.getSchema()
                    + " tableName: " + rowChangeEvent.getTable());
            return null;
        }
        int rowCount = rowChangeEvent.getRowSize();
        // ʹ�ñ仯��column����ƴװ�� update Sql��set column����
        StringBuilder updateSqlSb = new StringBuilder();
        Iterator<? extends DBMSColumn> changeColumnIt = changeColumns.iterator();
        for (; changeColumnIt.hasNext(); ) {
            DBMSColumn changeColumn = changeColumnIt.next();
            String columnName = columnMapping(sourceLogicTable, StringUtil.toLowerCase(changeColumn.getName()));
            //if mysql columnName skip keywords
            columnName = encodingColumnName(columnName);
            updateSqlSb.append(columnName).append("=?");
            if (changeColumnIt.hasNext()) {
                updateSqlSb.append(",");
            }
        }
        List<? extends DBMSColumn> whereColumns = getShardFilterColumn(rowChangeEvent.getPrimaryKey(),
                sourceLogicTable, targetLogicTable, rowColumns);
        updateSqlSb.append(" WHERE ");
        Iterator<? extends DBMSColumn> whereColumnIt = whereColumns.iterator();
        for (; whereColumnIt.hasNext(); ) {
            DBMSColumn whereColumn = whereColumnIt.next();
            String whereColumnName = columnMapping(sourceLogicTable, whereColumn.getName());
            //if mysql columnName skip keywords
            whereColumnName = encodingColumnName(whereColumnName);
            updateSqlSb.append(whereColumnName).append("=?");
            if (whereColumnIt.hasNext()) {
                updateSqlSb.append(" AND ");
            }
        }
        // ��ʽ������SQL
        String sql = MessageFormat.format(UPDATE_FORMATE, new Object[]{targetLogicTable, updateSqlSb.toString()});
        // ���˺��ֶζ�Ӧֵ���б�
        List<List<Serializable>> rowColumnValues = new ArrayList<List<Serializable>>(rowCount);
        // ����rowData��value,ֻ�����仯�ֶε�value
        for (int i = 1; i <= rowCount; i++) {
            // ��changeҲ����set�����ֶεĲ���?ռλ����ֵ��䵽rowDataList��
            List<Serializable> rowChangeValues = new ArrayList<Serializable>(rowColumns.size());
            for (DBMSColumn changeColumn : changeColumns) {
                rowChangeValues.add(rowChangeEvent.getChangeValue(i, changeColumn));
            }
            // ��where���ֵģ�ռλ����ֵ���䵽rowDataList��
            DBMSRowData prvRowData = rowChangeEvent.getRowData(i);
            for (DBMSColumn whereColumn : whereColumns) {
                rowChangeValues.add(prvRowData.getRowValue(whereColumn));
            }
            rowColumnValues.add(rowChangeValues);
            // ת����һ��RowSqlExceContext
        }
        return new RowSqlExceContext(sql, rowColumnValues, DBMSAction.UPDATE);
    }

    private RowSqlExceContext getInsertSqlExceContext(DBMSRowChange rowChangeEvent, String sourceLogicTable,
                                                      String targetLogicTable) {
        // ���˺���ֶ��б�
        List<? extends DBMSColumn> rowColumns = this.filterColumn(rowChangeEvent.getColumns(), sourceLogicTable);
        if (null == rowColumns || rowColumns.isEmpty()) {
            //���������ȫ�������˵���
            logger.warn("DBSyncTask Insert all columns has been filter! schema: " + rowChangeEvent.getSchema()
                    + " tableName: " + rowChangeEvent.getTable());
            return null;
        }
        int rowCount = rowChangeEvent.getRowSize();
        // ƴװinsert��SQL���ֶκ�value���ַ���
        StringBuilder sqlColumnNames = new StringBuilder();
        StringBuilder sqlColumnValues = new StringBuilder();

        Iterator<? extends DBMSColumn> rowColumnIt = rowColumns.iterator();
        for (; rowColumnIt.hasNext(); ) {
            DBMSColumn dbmsColumn = rowColumnIt.next();
            String columnName = columnMapping(sourceLogicTable, StringUtil.toLowerCase(dbmsColumn.getName()));
            //if mysql columnName skip keywords
            columnName = encodingColumnName(columnName);
            sqlColumnNames.append(columnName);
            sqlColumnValues.append("?");
            if (rowColumnIt.hasNext()) {
                sqlColumnNames.append(",");
                sqlColumnValues.append(",");
            }
        }
        // ��ʽ������SQL
        String sql = MessageFormat.format(this.dataBaseApplierNode.isReplace()
                && DBType.ORACLE != this.dataBaseApplierNode.getDbType() ? RPLACE_FORMATE : INSERT_FORMATE,
                new Object[]{targetLogicTable, sqlColumnNames.toString(), sqlColumnValues.toString()});
        // ���˺��ֶζ�Ӧֵ���б�
        List<List<Serializable>> rowColumnValues = new ArrayList<List<Serializable>>(rowCount);
        // ƴװrowDataList
        for (int i = 1; i <= rowCount; i++) {
            DBMSRowData rowData = rowChangeEvent.getRowData(i);
            List<Serializable> columnValues = new ArrayList<Serializable>(rowColumns.size());
            for (DBMSColumn column : rowColumns) {
                columnValues.add(rowData.getRowValue(column));
            }
            rowColumnValues.add(columnValues);
        }
        return new RowSqlExceContext(sql, rowColumnValues, DBMSAction.INSERT);
    }

    private RowSqlExceContext getDeleteSqlExceContext(DBMSRowChange rowChangeEvent, String sourceLogicTable,
                                                      String targetLogicTable) {
        // ���˺���ֶ��б�
        List<? extends DBMSColumn> rowColumns = this.filterColumn(rowChangeEvent.getColumns(), sourceLogicTable);
        if (null == rowColumns || rowColumns.isEmpty()) {
            //���������ȫ�������˵���
            logger.warn("DBSyncTask Delete all columns has been filter! schema: " + rowChangeEvent.getSchema()
                    + " tableName: " + rowChangeEvent.getTable());
            return null;
        }
        int rowCount = rowChangeEvent.getRowSize();
        //��������
        List<? extends DBMSColumn> whereColumns = getShardFilterColumn(rowChangeEvent.getPrimaryKey(),
                sourceLogicTable, targetLogicTable, rowColumns);
        // ����������˵���ֱ�ӷ���Null
        if (whereColumns.isEmpty()) {
            return null;
        }
        StringBuilder deleteWhereSqlSb = new StringBuilder();
        Iterator<? extends DBMSColumn> whereColumnIt = whereColumns.iterator();
        for (; whereColumnIt.hasNext(); ) {
            String columnName = columnMapping(sourceLogicTable, StringUtil.toLowerCase(whereColumnIt.next().getName()));
            //if mysql columnName skip keywords
            columnName = encodingColumnName(columnName);
            deleteWhereSqlSb.append(columnName).append("=?");
            if (whereColumnIt.hasNext()) {
                deleteWhereSqlSb.append(" AND ");
            }
        }
        // ��ʽ������SQL
        String sql = MessageFormat.format(DELETE_FORMATE,
                new Object[]{targetLogicTable, deleteWhereSqlSb.toString()});
        // ���˺��ֶζ�Ӧֵ���б�
        List<List<Serializable>> rowColumnValues = new ArrayList<List<Serializable>>(rowCount);
        // ����rowData��value,ֻ�����仯�ֶε�value
        for (int i = 1; i <= rowCount; i++) {
            DBMSRowData rowData = rowChangeEvent.getRowData(i);
            Serializable[] rowAllValues = rowData.getRowValues();
            // ���WHERE�ֶθ�����ȫ���ֶθ����൱
            if (whereColumns.size() == rowAllValues.length) {
                rowColumnValues.add(Arrays.asList(rowAllValues));
            } else {
                // ���WHERE�ֶθ�����ȫ���ֶβ�������ʹ��WHERE���ֶ�����ȡ���Ӧ��ֵ
                List<Serializable> rowDeleteData = new ArrayList<Serializable>(whereColumns.size());
                for (DBMSColumn deleteColumn : whereColumns) {
                    rowDeleteData.add(rowAllValues[deleteColumn.getColumnIndex() - 1]);
                }
                rowColumnValues.add(rowDeleteData);
            }
        }
        return new RowSqlExceContext(sql, rowColumnValues, DBMSAction.DELETE);
    }

    private String encodingColumnName(String columnName) {
        String encColumnName = columnName;
        if (DBType.MYSQL == this.dataBaseApplierNode.getDbType()) {
            if (!StringUtil.contains(columnName, "`")) {
                encColumnName = "`" + encColumnName + "`";
            }
        }
        return encColumnName;
    }

    /**
     * �ж��Ƿ��Ǻ��Ե�Schema
     *
     * @param schemaName
     * @return
     */
    private boolean isIgnoreSchema(String schemaName) {
        List<String> schemaNames = this.dataBaseApplierNode.getSchemaNames();
        if (null == schemaNames || schemaNames.isEmpty()) {
            return false;
        }
        // ������õĸ���schemaNames��������ǰschemaName�����ڹ��˵�schemaName
        return !schemaNames.contains(schemaName);
    }

    /**
     * ��������������б��д�������ĳ�߼��������������ظ���������Ӧ���߼����� ��������������б���û�������߼������������ж��Ƿ���������Ƿ���ȫ���߼�
     * ������ȣ��������򷵻�ʵ�ʱ������򷵻ؿ� �߼������������ȹ���
     * <p/>
     * ���isSource ��TRUE���ص���Դ����߼���������ʵ�ʱ������򷵻ص���ӳ���Ŀ�ı���߼�����
     *
     * @param actualTable     ʵ�ʱ���
     * @param logicTableNames �߼������б���ܰ�����ʵ����
     * @param isSource        �Ƿ���Դ��ƥ��
     * @return
     */
    private String filterTable(String actualTable, Map<String, String> logicTableNames, boolean isSource) {
        // �������Ϊ�գ�Ĭ�ϼ����ص�ǰ��ʵ�ʱ���
        if (null == logicTableNames || logicTableNames.isEmpty()) {
            return actualTable;
        }
        String logicTable = StringUtil.EMPTY_STRING;
        for (Map.Entry<String, String> entry : logicTableNames.entrySet()) {
            String tmpLogicTableName = entry.getKey();
            if (StringUtil.equals(actualTable, tmpLogicTableName)) {
                if (!isSource) {
                    String tableMapName = entry.getValue();
                    if (StringUtil.isNotBlank(tableMapName)) {
                        logicTable = tableMapName;
                    }
                }
                if (StringUtil.isBlank(logicTable)) {
                    logicTable = tmpLogicTableName;
                }
                break;
            } else {
                int tableNameIndex = actualTable.indexOf(tmpLogicTableName);
                // ����߼�������ʵ�ʱ������Ӽ�
                if (tableNameIndex == 0) {
                    if (actualTable.length() > tmpLogicTableName.length() + 1) {
                        String split = actualTable
                                .substring(tmpLogicTableName.length(), tmpLogicTableName.length() + 1);
                        String prefix = actualTable.substring(tmpLogicTableName.length() + 1);
                        // ����ҵ�ȷʵ�Ƿ��Ϸֱ���߼���
                        if (StringUtil.equals("_", split) && StringUtil.isNotBlank(prefix)
                                && StringUtil.isNumeric(prefix)) {
                            if (!isSource) {
                                String tableMapName = entry.getValue();
                                if (StringUtil.isNotBlank(tableMapName)) {
                                    logicTable = tableMapName;
                                }
                            }
                            if (StringUtil.isBlank(logicTable)) {
                                logicTable = tmpLogicTableName;
                            }
                            break;
                        }
                    }
                }
            }
        }
        return logicTable;
    }

    /**
     * �����߼����������ֶ�
     *
     * @param columns
     * @param logicTable
     * @return
     */
    private List<? extends DBMSColumn> filterColumn(List<? extends DBMSColumn> columns, String logicTable) {
        if (null == columns || columns.isEmpty()) {
            return columns;
        }
        Map<String, Set<String>> filterColumns = this.dataBaseApplierNode.getIgnoreColumns();
        // ���û�����ֶζι��ˣ�����ûƥ���߼���ԭ�ⲻ������columns
        if (filterColumns.isEmpty() || !filterColumns.containsKey(logicTable)) {
            return columns;
        }
        // ����ҵ�ƥ���߼�������û�������ֶι��ˣ�ԭ�ⲻ������columns
        Set<String> ignoreSet = filterColumns.get(logicTable);
        if (null == ignoreSet || ignoreSet.isEmpty()) {
            return columns;
        }
        Map<String, Boolean> columnFlags = this.dataBaseApplierNode.getFilterFlags();
        //��ȡ��ʽ��־λ
        boolean include = columnFlags.get(logicTable) == null ? false : columnFlags.get(logicTable);
        List<DBMSColumn> filterColumn = new ArrayList<DBMSColumn>(columns.size());
        for (DBMSColumn column : columns) {
            boolean contains = ignoreSet.contains(StringUtil.toLowerCase(column.getName()));
            if (include) {
                if (contains) {
                    filterColumn.add(column);
                }
            } else {
                if (!contains) {
                    filterColumn.add(column);
                }
            }
        }
        return filterColumn;
    }

    private List<? extends DBMSColumn> getShardFilterColumn(List<? extends DBMSColumn> primaryColumns,
                                                            String sourceLogicTable, String targetLogicTable, List<? extends DBMSColumn> rowColumns) {
        List<? extends DBMSColumn> whereColumns = Collections.emptyList();

        // ��ȡ�ֿ�ֱ��
        Set<String> shardColumns = this.getTableShardColumn(targetLogicTable);

        if (shardColumns.isEmpty()) {
            // �����������Ļ���ʹ������ƴװSQL��WHERE����
            List<? extends DBMSColumn> filterPrimaryColumns = this.filterColumn(primaryColumns, sourceLogicTable);
            if (null == filterPrimaryColumns || filterPrimaryColumns.isEmpty()) {
                // ���û�У��õ�ԭʼ������WEHER���������ֹ�����֤������ȷ
                whereColumns = rowColumns;
            } else {
                whereColumns = filterPrimaryColumns;
            }
        } else {
            if (null == primaryColumns || primaryColumns.isEmpty()) {
                // ���û�У��õ�ԭʼ������WEHER���������ֹ�����֤������ȷ
                whereColumns = rowColumns;
            } else {
                List<DBMSColumn> allWhereColumns = new ArrayList<DBMSColumn>(primaryColumns.size()
                        + shardColumns.size());
                allWhereColumns.addAll(primaryColumns);
                for (DBMSColumn column : rowColumns) {
                    if (shardColumns.contains(column.getName().toUpperCase()) && !allWhereColumns.contains(column)) {
                        allWhereColumns.add(column);
                    }
                }
                whereColumns = this.filterColumn(allWhereColumns, sourceLogicTable);
            }
        }
        return whereColumns;
    }

    /**
     * �����߼�������ȡ�ֿ���б�
     *
     * @param logicTable
     * @return
     */
    private Set<String> getTableShardColumn(String logicTable) {
        return this.tddlRule.getTableShardColumn(logicTable);
    }

    /**
     * @param tableName
     * @param columnName
     * @return
     */
    private String columnMapping(String tableName, String columnName) {
        String mappingName = columnName;
        if (StringUtil.isNotBlank(tableName) && StringUtil.isNotBlank(columnName)) {
            Map<String, Map<String, String>> mappingMap = this.dataBaseApplierNode.getColumnMapping();
            if (null != mappingMap && !mappingMap.isEmpty()) {
                Map<String, String> columnMappingMap = mappingMap.get(tableName);
                if (null != columnMappingMap && !columnMappingMap.isEmpty()) {
                    String tmpMappingName = columnMappingMap.get(columnName);
                    if (StringUtil.isNotBlank(tmpMappingName)) {
                        mappingName = tmpMappingName;
                    }
                }
            }
        }
        return mappingName;
    }

    /**
     * <p/>
     * description:EVENT��Ӧ��JDBCִ�ж���
     * <p/>
     *
     * @author <a href="mailto:qihao@taobao.com">qihao</a>
     * @version 1.0
     * @{# DBSyncTask.java Create on Aug 29, 2011 10:15:43 AM
     * <p/>
     * Copyright (c) 2011 by qihao.
     */
    static class RowSqlExceContext {
        String sql;
        List<List<Serializable>> rowDataList;

        /**
         * @param sql
         * @param rowDataList
         */
        public RowSqlExceContext(String sql, List<List<Serializable>> rowDataList, DBMSAction action) {
            this.sql = sql;
            this.rowDataList = rowDataList;
        }
    }
}