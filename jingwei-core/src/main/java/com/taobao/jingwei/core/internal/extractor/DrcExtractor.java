package com.taobao.jingwei.core.internal.extractor;

import com.alibaba.common.lang.StringUtil;
import com.taobao.drc.sdk.Checkpoint;
import com.taobao.drc.sdk.message.ByteString;
import com.taobao.drc.sdk.message.DataMessage;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.JingWeiUtil;
import com.taobao.jingwei.common.node.extractor.DrcExtractorNode;
import com.taobao.jingwei.core.drc.DRCClientException;
import com.taobao.jingwei.core.drc.DrcDataListener;
import com.taobao.jingwei.core.drc.DrcDumper;
import com.taobao.jingwei.core.util.RowChangeBuilder;
import com.taobao.tddl.dbsync.DbsyncException;
import com.taobao.tddl.dbsync.dbms.DBMSAction;
import com.taobao.tddl.dbsync.extractor.Extractor;
import com.taobao.tddl.dbsync.extractor.ExtractorBrokenException;
import com.taobao.tddl.dbsync.extractor.ExtractorException;
import com.taobao.tddl.dbsync.extractor.Transferer;
import com.taobao.tddl.dbsync.plugin.AbstractPlugin;
import com.taobao.tddl.dbsync.plugin.PluginException;
import com.taobao.tddl.dbsync.tx.Tx;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.*;

/**
 * User: <a href="mailto:qihao@taobao.com">qihao</a>
 * Date: 13-2-14
 * Time: 下午10:18
 * version 1.0
 */
public class DrcExtractor extends AbstractPlugin implements Extractor, JingWeiConstants {
    private final DrcExtractorNode drcExtractorNode;
    private final DrcDumper drcDumper = new DrcDumper();
    //心跳周期
    private long heartbeatPeriod = 15000L;

    //精卫数据对象创建需要的缓冲对象
    private Tx currentTx;
    private RowChangeBuilder dataBuilder;
    private boolean needBuildMetaData = true;
    private Set<String> chengeColumSet = null;
    private List<DataMessage.Record> changeRecordBuffer = null;

    private Checkpoint startCheckpoint;
    private Checkpoint fristDataPoint;

    public DrcExtractor(DrcExtractorNode drcExtractorNode) {
        this.drcExtractorNode = drcExtractorNode;
        //初始化DRCDUMPER的相关参数
        drcDumper.setClusterUrl(this.drcExtractorNode.getClusterUrl());
        drcDumper.setDbName(this.drcExtractorNode.getDbName());
        drcDumper.setPasswd(this.drcExtractorNode.getPasswd());
        drcDumper.setGroupName(this.drcExtractorNode.getGroupName());
        drcDumper.setFilterStr(this.drcExtractorNode.getFilterStr());
    }

    @Override
    public void extract(Transferer transferer) throws ExtractorException, InterruptedException {
        //重置相关位点
        resetCheckPoint();
        //设置数据监听器
        drcDumper.setDataListener(new DrcMessageListener(transferer));
        //从ZK中获取上次中断的位点
        startCheckpoint = loadPosition(transferer);
        logger.warn("=====Start Position===== : " + startCheckpoint.getMysqlCheckpoint());
        try {
            drcDumper.startDump(startCheckpoint);
        } catch (DRCClientException e) {
            logger.error("drc dump Error! conf: " + drcDumper.toString(), e);
            throw new ExtractorBrokenException(e);
        }
        //如果走到这里说明DRC出现问题挂了,直接由dbsync开始走销毁流程调用destory方法
        throw new ExtractorBrokenException("DRC client has shutdown.");
    }

    protected void resetCheckPoint() {
        fristDataPoint = null;
        startCheckpoint = null;
    }

    protected void resetContext() {
        dataBuilder = null;
        changeRecordBuffer = null;
        chengeColumSet = null;
        needBuildMetaData = true;
    }

    private Checkpoint loadPosition(Transferer transferer) throws ExtractorBrokenException {
        String startPosition;
        try {
            //第一次进入extract方法，获取启动位点
            startPosition = transferer.getLastPosition();
        } catch (DbsyncException e) {
            logger.error("DrcExtractor extract getLastPosition Error!", e);
            throw new ExtractorBrokenException(e);
        }
        String[] pos = JingWeiUtil.parseDrcStrPosition(startPosition);
        Checkpoint point = new Checkpoint();
        if (StringUtil.isNotBlank(pos[0])) {
            point.setFile(Checkpoint.MYSQL_FILE + pos[0]);
        }
        point.setOffset(pos[1]);
        point.setServerId(pos[2]);
        point.setTimestamp(pos[3]);
        point.setMetaDataVersion(pos[4]);
        return point;
    }

    public void destory() throws PluginException, InterruptedException {
        try {
            if (null != this.currentTx) {
                this.currentTx.bind();
                // Do shutdown DRC.
                drcDumper.stopDump();
            }
            super.destory();
        } catch (Exception e) {
            throw new PluginException("Drc client error: " + e.getMessage(), e);
        } finally {
            //复位相关变量
            currentTx = null;
            resetContext();
        }
    }

    protected final class DrcMessageListener implements DrcDataListener {

        private final Transferer transferer;
        private long lastHeartBeat = System.currentTimeMillis();

        private boolean timeSeek = false;


        public DrcMessageListener(Transferer transferer) {
            this.transferer = transferer;
        }

        @Override
        public void onDataChange(DataMessage dataMessage, String dbName) throws Exception {
            //如果消息是NULL,并且dbName是一个特殊值，就立即充值事务开始检查
            if (null == dataMessage && dbName.equals(DrcDumper.DRC_TIME_SEEK_COMMOND)) {
                logger.warn("timeSeek Maybe dbSwitch: " + dbName);
                timeSeek = true;
                return;
            }
            //线程检查，如果发现更换线程拷贝当前TX对象到新线程中
            if (null != currentTx) {
                currentTx.bind();
            }
            //设置txId
            List<DataMessage.Record> recordList = dataMessage.getRecordList();
            for (DataMessage.Record record : recordList) {
                //如果是DDL的话就跳过
                if (DataMessage.Record.Type.DDL == record.getOpt()) {
                    continue;
                }
                //如果是心跳,处理好心跳数据,就跳过该数据
                if (DataMessage.Record.Type.HEARTBEAT == record.getOpt()) {
                    //处理心跳
                    processHeartBeat();
                    continue;
                }
                //第一次进入起始位点检查
                if (null == currentTx) {
                    if (DataMessage.Record.Type.BEGIN != record.getOpt()) {
                        if (timeSeek) {
                            logger.warn("first start not txBegin position: " + record.getCheckpoint());
                            timeSeek = false;
                            continue;
                        } else {
                            //如果有位点并且给的数据还不是事务开始必须抛出异常
                            throw new Exception("start Check Point not Tx Begin! position: " + record.getCheckpoint());
                        }
                    }
                }
                //如果是事务结束,将位点提交到ZK,并且跳过该消息
                if (DataMessage.Record.Type.COMMIT == record.getOpt()) {
                    //如果是事务结束,将Record位点记录到当前TX对象中?
                    txEnd(record);
                    continue;
                }
                //创建新的TX对象并且跳过该消息
                if (DataMessage.Record.Type.BEGIN == record.getOpt()) {
                    txBegin(record);
                    continue;
                }
                //将DRC的TYPE转换成jingwei的ACTION
                DBMSAction action = d2jConvertEventAction(record.getOpt());
                //ACTION类型检查
                if (action == null) {
                    throw new ExtractorBrokenException("unknow Drc Record Type: " + record.getOpt());
                }
                //记录第一次数据到来的位点
                if (null == fristDataPoint) {
                    fristDataPoint = drcDumper.getCheckpoint(record);
                    logger.warn("=====Frist Data===== : " + fristDataPoint.getMysqlCheckpoint());
                }
                if (null == dataBuilder) {
                    //创建新的BUILDER
                    dataBuilder = RowChangeBuilder.createBuilder(dbName, record.getTablename(), action);
                }
                //处理数据
                processRowData(record, action);
                //关闭重建表头开关,同一个LOGEVENT的下一个数据不再重新构建表头MetaData信息
                if (needBuildMetaData == true) {
                    //重置metaData的Build开关
                    needBuildMetaData = false;
                }
                //当前数据跟之前不在同一个LOGEVENT的话，立即提交
                if (!record.isHomotypic()) {
                    //向Applier里投递数据
                    offerData();
                }
            }
            //如果这个消息结束后,缓存的BUILDER里还有数据,那么就提交一次数据,但是不记录位点,防止大事物撑爆内存
            if (null != dataBuilder) {
                offerData();
            }
        }

        private void processHeartBeat() throws DbsyncException {
            //心跳数据
            long now = System.currentTimeMillis();
            //到达时间伐值就响应一次心跳
            if (now - this.lastHeartBeat >= DrcExtractor.this.heartbeatPeriod) {
                try {
                    this.transferer.heartbeat();
                    this.lastHeartBeat = now;
                } catch (Throwable e) {
                    //如果心跳的响应实现发生异常直接抛到上层中断整个复制管道
                    //如果对于心跳正常与否不关心，那么在自己的心跳实现吃掉异常即可
                    throw new DbsyncException("Dbsync error on heartbeat: " + e.getMessage(), e);
                }
            }
        }

        private void processRowData(DataMessage.Record record, DBMSAction action) throws ExtractorBrokenException, UnsupportedEncodingException {
            //处理INSERT和DELETE
            if (DBMSAction.INSERT == action || DBMSAction.DELETE == action) {
                buildInsertAndDeleteRowData(record);
            } else if (DBMSAction.UPDATE == action) {
                buildUpdateRowData(record);
            }
        }

        private void buildUpdateRowData(DataMessage.Record record) throws ExtractorBrokenException, UnsupportedEncodingException {

            List<DataMessage.Record.Field> fieldList = record.getFieldList();
            //处理UPDATE
            int fieldCount = fieldList.size();

            //初始化ChangeColumnSet
            if (needBuildMetaData && null == chengeColumSet && null == changeRecordBuffer) {
                //update drc保证字段变化是2N的
                chengeColumSet = new HashSet<String>(fieldCount / 2);
                changeRecordBuffer = new ArrayList<DataMessage.Record>();
            }
            //将本次UPDATEA先缓存起来
            changeRecordBuffer.add(record);

            //处理变化字段的metaData表头信息,和变化字段SET
            for (int i = 0; i < fieldCount; i++) {
                DataMessage.Record.Field oldField = record.getFieldList().get(i);
                DataMessage.Record.Field newField = record.getFieldList().get(++i);
                //UPDATE字段名称检查
                if (!StringUtil.equals(oldField.getFieldname(), newField.getFieldname())) {
                    //字段名称检查
                    throw new ExtractorBrokenException("update Before After FieldName Not Same ! before: " + oldField.getFieldname() + " after: " + newField.getFieldname());
                }
                String fieldName = oldField.getFieldname();
                //构建update的METADATA信息
                if (needBuildMetaData) {
                    //新老只要有一个是NULL该字段认为是可为NULL
                    boolean nullable = isFieldNullable(oldField, newField);
                    dataBuilder.addMetaColumn(fieldName, oldField.getSqlType(), !oldField.isUnsigned(), nullable, oldField.isPrimaryKey());
                }
                //将变化后数据放到ChangeValue的Map中
                if (isFieldChange(oldField, newField)) {
                    chengeColumSet.add(fieldName);
                }
            }
        }

        private void buildInsertAndDeleteRowData(DataMessage.Record record) {
            List<DataMessage.Record.Field> fieldList = record.getFieldList();
            //创建变化前数据MAP
            Map<String, Serializable> fieldValueMap = new HashMap<String, Serializable>(fieldList.size());
            for (DataMessage.Record.Field field : fieldList) {
                //添加改字段的MetaData信息到BUILDER中
                String fieldName = field.getFieldname();
                Serializable fieldValue = (Serializable) field.getObjectValue();
                if (needBuildMetaData) {
                    boolean nullable = null == fieldValue;
                    dataBuilder.addMetaColumn(fieldName, field.getSqlType(), !field.isUnsigned(), nullable, field.isPrimaryKey());
                }
                //将字段值添加到fieldValueMap中
                fieldValueMap.put(fieldName, fieldValue);
            }
            //添加字段的值列表到BUILDER中
            dataBuilder.addRowData(fieldValueMap);
        }

        private void offerData() throws Exception {
            if (DBMSAction.UPDATE == dataBuilder.getAction()) {
                for (DataMessage.Record record : changeRecordBuffer) {
                    //获取DRC字段列表
                    List<DataMessage.Record.Field> fieldList = record.getFieldList();
                    int fieldCount = fieldList.size();
                    //创建变化前数据MAP
                    Map<String, Serializable> beforeValueMap = new HashMap<String, Serializable>(fieldCount / 2);
                    Map<String, Serializable> afterValueMap = new HashMap<String, Serializable>(fieldCount / 2);

                    for (int i = 0; i < fieldCount; i++) {
                        DataMessage.Record.Field oldField = record.getFieldList().get(i);
                        DataMessage.Record.Field newField = record.getFieldList().get(++i);
                        String fieldName = oldField.getFieldname();
                        Serializable oldValue = (Serializable) oldField.getObjectValue();
                        Serializable newValue = (Serializable) newField.getObjectValue();
                        //将变化前的指放到beforeValueMap
                        beforeValueMap.put(fieldName, oldValue);
                        //根据总体的变化SET拼装变化后数据
                        if (chengeColumSet.contains(fieldName)) {
                            //将变化后的指放到afterValueMap
                            afterValueMap.put(fieldName, newValue);
                        }
                    }
                    dataBuilder.addRowData(beforeValueMap);
                    dataBuilder.addChangeRowData(afterValueMap);
                }
            }
            currentTx.offer(dataBuilder.build());

            resetContext();
        }

        private boolean isFieldChange(DataMessage.Record.Field field1, DataMessage.Record.Field field2) throws UnsupportedEncodingException {
            ByteString byteValue1 = field1.getByteStringValue();
            ByteString byteValue2 = field2.getByteStringValue();
            if (byteValue1 == null) {
                return byteValue2 == null ? false : true;
            } else {
                return byteValue2 == null ? true :
                        !byteValue1.toString(field1.getEncoding()).equals(byteValue2.toString(field2.getEncoding()));
            }
        }

        private boolean isFieldNullable(DataMessage.Record.Field field1, DataMessage.Record.Field field2) {
            ByteString byteValue1 = field1.getByteStringValue();
            ByteString byteValue2 = field2.getByteStringValue();
            return (null == byteValue1 || null == byteValue2);
        }

        private DBMSAction d2jConvertEventAction(DataMessage.Record.Type type) {
            DBMSAction action;
            switch (type) {
                case INSERT:
                    action = DBMSAction.INSERT;
                    break;
                case UPDATE:
                    action = DBMSAction.UPDATE;
                    break;
                case DELETE:
                    action = DBMSAction.DELETE;
                    break;
                default:
                    action = DBMSAction.OTHER;
            }
            return action;
        }

        private void txBegin(DataMessage.Record record) throws DbsyncException, InterruptedException {
            //创建TX还有RowData对象
            currentTx = this.transferer.begin();
            //设置当前时间为解析时间
            Timestamp extractTimestamp = new Timestamp(System.currentTimeMillis());
            Timestamp sourceTimestamp = new Timestamp(1000 * Long.valueOf(record.getTimestamp()));
            currentTx.setExtractTimestamp(extractTimestamp);
            //设置数据库的时间
            currentTx.setSourceTimestamp(sourceTimestamp);
        }

        private void txEnd(DataMessage.Record record) throws DbsyncException, InterruptedException {
            //从Record中获取该事务结束对应的数据位点
            Checkpoint checkpoint = drcDumper.getCheckpoint(record);
            String file = StringUtil.substringAfterLast(checkpoint.getFile(), POINT_STR);
            //将Checkpoint 转化成精卫用的字符串位点
            String strPosition = JingWeiUtil.getDrcStrPosition(file, checkpoint.getOffset(),
                    checkpoint.getServerId(), checkpoint.getTimestamp(), checkpoint.getMetaDataVersion());
            currentTx.setPosition(strPosition);
            currentTx.commit();
        }
    }
}