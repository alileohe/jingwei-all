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
 * Time: ����10:18
 * version 1.0
 */
public class DrcExtractor extends AbstractPlugin implements Extractor, JingWeiConstants {
    private final DrcExtractorNode drcExtractorNode;
    private final DrcDumper drcDumper = new DrcDumper();
    //��������
    private long heartbeatPeriod = 15000L;

    //�������ݶ��󴴽���Ҫ�Ļ������
    private Tx currentTx;
    private RowChangeBuilder dataBuilder;
    private boolean needBuildMetaData = true;
    private Set<String> chengeColumSet = null;
    private List<DataMessage.Record> changeRecordBuffer = null;

    private Checkpoint startCheckpoint;
    private Checkpoint fristDataPoint;

    public DrcExtractor(DrcExtractorNode drcExtractorNode) {
        this.drcExtractorNode = drcExtractorNode;
        //��ʼ��DRCDUMPER����ز���
        drcDumper.setClusterUrl(this.drcExtractorNode.getClusterUrl());
        drcDumper.setDbName(this.drcExtractorNode.getDbName());
        drcDumper.setPasswd(this.drcExtractorNode.getPasswd());
        drcDumper.setGroupName(this.drcExtractorNode.getGroupName());
        drcDumper.setFilterStr(this.drcExtractorNode.getFilterStr());
    }

    @Override
    public void extract(Transferer transferer) throws ExtractorException, InterruptedException {
        //�������λ��
        resetCheckPoint();
        //�������ݼ�����
        drcDumper.setDataListener(new DrcMessageListener(transferer));
        //��ZK�л�ȡ�ϴ��жϵ�λ��
        startCheckpoint = loadPosition(transferer);
        logger.warn("=====Start Position===== : " + startCheckpoint.getMysqlCheckpoint());
        try {
            drcDumper.startDump(startCheckpoint);
        } catch (DRCClientException e) {
            logger.error("drc dump Error! conf: " + drcDumper.toString(), e);
            throw new ExtractorBrokenException(e);
        }
        //����ߵ�����˵��DRC�����������,ֱ����dbsync��ʼ���������̵���destory����
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
            //��һ�ν���extract��������ȡ����λ��
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
            //��λ��ر���
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
            //�����Ϣ��NULL,����dbName��һ������ֵ����������ֵ����ʼ���
            if (null == dataMessage && dbName.equals(DrcDumper.DRC_TIME_SEEK_COMMOND)) {
                logger.warn("timeSeek Maybe dbSwitch: " + dbName);
                timeSeek = true;
                return;
            }
            //�̼߳�飬������ָ����߳̿�����ǰTX�������߳���
            if (null != currentTx) {
                currentTx.bind();
            }
            //����txId
            List<DataMessage.Record> recordList = dataMessage.getRecordList();
            for (DataMessage.Record record : recordList) {
                //�����DDL�Ļ�������
                if (DataMessage.Record.Type.DDL == record.getOpt()) {
                    continue;
                }
                //���������,�������������,������������
                if (DataMessage.Record.Type.HEARTBEAT == record.getOpt()) {
                    //��������
                    processHeartBeat();
                    continue;
                }
                //��һ�ν�����ʼλ����
                if (null == currentTx) {
                    if (DataMessage.Record.Type.BEGIN != record.getOpt()) {
                        if (timeSeek) {
                            logger.warn("first start not txBegin position: " + record.getCheckpoint());
                            timeSeek = false;
                            continue;
                        } else {
                            //�����λ�㲢�Ҹ������ݻ���������ʼ�����׳��쳣
                            throw new Exception("start Check Point not Tx Begin! position: " + record.getCheckpoint());
                        }
                    }
                }
                //������������,��λ���ύ��ZK,������������Ϣ
                if (DataMessage.Record.Type.COMMIT == record.getOpt()) {
                    //������������,��Recordλ���¼����ǰTX������?
                    txEnd(record);
                    continue;
                }
                //�����µ�TX��������������Ϣ
                if (DataMessage.Record.Type.BEGIN == record.getOpt()) {
                    txBegin(record);
                    continue;
                }
                //��DRC��TYPEת����jingwei��ACTION
                DBMSAction action = d2jConvertEventAction(record.getOpt());
                //ACTION���ͼ��
                if (action == null) {
                    throw new ExtractorBrokenException("unknow Drc Record Type: " + record.getOpt());
                }
                //��¼��һ�����ݵ�����λ��
                if (null == fristDataPoint) {
                    fristDataPoint = drcDumper.getCheckpoint(record);
                    logger.warn("=====Frist Data===== : " + fristDataPoint.getMysqlCheckpoint());
                }
                if (null == dataBuilder) {
                    //�����µ�BUILDER
                    dataBuilder = RowChangeBuilder.createBuilder(dbName, record.getTablename(), action);
                }
                //��������
                processRowData(record, action);
                //�ر��ؽ���ͷ����,ͬһ��LOGEVENT����һ�����ݲ������¹�����ͷMetaData��Ϣ
                if (needBuildMetaData == true) {
                    //����metaData��Build����
                    needBuildMetaData = false;
                }
                //��ǰ���ݸ�֮ǰ����ͬһ��LOGEVENT�Ļ��������ύ
                if (!record.isHomotypic()) {
                    //��Applier��Ͷ������
                    offerData();
                }
            }
            //��������Ϣ������,�����BUILDER�ﻹ������,��ô���ύһ������,���ǲ���¼λ��,��ֹ������ű��ڴ�
            if (null != dataBuilder) {
                offerData();
            }
        }

        private void processHeartBeat() throws DbsyncException {
            //��������
            long now = System.currentTimeMillis();
            //����ʱ�䷥ֵ����Ӧһ������
            if (now - this.lastHeartBeat >= DrcExtractor.this.heartbeatPeriod) {
                try {
                    this.transferer.heartbeat();
                    this.lastHeartBeat = now;
                } catch (Throwable e) {
                    //�����������Ӧʵ�ַ����쳣ֱ���׵��ϲ��ж��������ƹܵ�
                    //�����������������񲻹��ģ���ô���Լ�������ʵ�ֳԵ��쳣����
                    throw new DbsyncException("Dbsync error on heartbeat: " + e.getMessage(), e);
                }
            }
        }

        private void processRowData(DataMessage.Record record, DBMSAction action) throws ExtractorBrokenException, UnsupportedEncodingException {
            //����INSERT��DELETE
            if (DBMSAction.INSERT == action || DBMSAction.DELETE == action) {
                buildInsertAndDeleteRowData(record);
            } else if (DBMSAction.UPDATE == action) {
                buildUpdateRowData(record);
            }
        }

        private void buildUpdateRowData(DataMessage.Record record) throws ExtractorBrokenException, UnsupportedEncodingException {

            List<DataMessage.Record.Field> fieldList = record.getFieldList();
            //����UPDATE
            int fieldCount = fieldList.size();

            //��ʼ��ChangeColumnSet
            if (needBuildMetaData && null == chengeColumSet && null == changeRecordBuffer) {
                //update drc��֤�ֶα仯��2N��
                chengeColumSet = new HashSet<String>(fieldCount / 2);
                changeRecordBuffer = new ArrayList<DataMessage.Record>();
            }
            //������UPDATEA�Ȼ�������
            changeRecordBuffer.add(record);

            //����仯�ֶε�metaData��ͷ��Ϣ,�ͱ仯�ֶ�SET
            for (int i = 0; i < fieldCount; i++) {
                DataMessage.Record.Field oldField = record.getFieldList().get(i);
                DataMessage.Record.Field newField = record.getFieldList().get(++i);
                //UPDATE�ֶ����Ƽ��
                if (!StringUtil.equals(oldField.getFieldname(), newField.getFieldname())) {
                    //�ֶ����Ƽ��
                    throw new ExtractorBrokenException("update Before After FieldName Not Same ! before: " + oldField.getFieldname() + " after: " + newField.getFieldname());
                }
                String fieldName = oldField.getFieldname();
                //����update��METADATA��Ϣ
                if (needBuildMetaData) {
                    //����ֻҪ��һ����NULL���ֶ���Ϊ�ǿ�ΪNULL
                    boolean nullable = isFieldNullable(oldField, newField);
                    dataBuilder.addMetaColumn(fieldName, oldField.getSqlType(), !oldField.isUnsigned(), nullable, oldField.isPrimaryKey());
                }
                //���仯�����ݷŵ�ChangeValue��Map��
                if (isFieldChange(oldField, newField)) {
                    chengeColumSet.add(fieldName);
                }
            }
        }

        private void buildInsertAndDeleteRowData(DataMessage.Record record) {
            List<DataMessage.Record.Field> fieldList = record.getFieldList();
            //�����仯ǰ����MAP
            Map<String, Serializable> fieldValueMap = new HashMap<String, Serializable>(fieldList.size());
            for (DataMessage.Record.Field field : fieldList) {
                //��Ӹ��ֶε�MetaData��Ϣ��BUILDER��
                String fieldName = field.getFieldname();
                Serializable fieldValue = (Serializable) field.getObjectValue();
                if (needBuildMetaData) {
                    boolean nullable = null == fieldValue;
                    dataBuilder.addMetaColumn(fieldName, field.getSqlType(), !field.isUnsigned(), nullable, field.isPrimaryKey());
                }
                //���ֶ�ֵ��ӵ�fieldValueMap��
                fieldValueMap.put(fieldName, fieldValue);
            }
            //����ֶε�ֵ�б�BUILDER��
            dataBuilder.addRowData(fieldValueMap);
        }

        private void offerData() throws Exception {
            if (DBMSAction.UPDATE == dataBuilder.getAction()) {
                for (DataMessage.Record record : changeRecordBuffer) {
                    //��ȡDRC�ֶ��б�
                    List<DataMessage.Record.Field> fieldList = record.getFieldList();
                    int fieldCount = fieldList.size();
                    //�����仯ǰ����MAP
                    Map<String, Serializable> beforeValueMap = new HashMap<String, Serializable>(fieldCount / 2);
                    Map<String, Serializable> afterValueMap = new HashMap<String, Serializable>(fieldCount / 2);

                    for (int i = 0; i < fieldCount; i++) {
                        DataMessage.Record.Field oldField = record.getFieldList().get(i);
                        DataMessage.Record.Field newField = record.getFieldList().get(++i);
                        String fieldName = oldField.getFieldname();
                        Serializable oldValue = (Serializable) oldField.getObjectValue();
                        Serializable newValue = (Serializable) newField.getObjectValue();
                        //���仯ǰ��ָ�ŵ�beforeValueMap
                        beforeValueMap.put(fieldName, oldValue);
                        //��������ı仯SETƴװ�仯������
                        if (chengeColumSet.contains(fieldName)) {
                            //���仯���ָ�ŵ�afterValueMap
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
            //����TX����RowData����
            currentTx = this.transferer.begin();
            //���õ�ǰʱ��Ϊ����ʱ��
            Timestamp extractTimestamp = new Timestamp(System.currentTimeMillis());
            Timestamp sourceTimestamp = new Timestamp(1000 * Long.valueOf(record.getTimestamp()));
            currentTx.setExtractTimestamp(extractTimestamp);
            //�������ݿ��ʱ��
            currentTx.setSourceTimestamp(sourceTimestamp);
        }

        private void txEnd(DataMessage.Record record) throws DbsyncException, InterruptedException {
            //��Record�л�ȡ�����������Ӧ������λ��
            Checkpoint checkpoint = drcDumper.getCheckpoint(record);
            String file = StringUtil.substringAfterLast(checkpoint.getFile(), POINT_STR);
            //��Checkpoint ת���ɾ����õ��ַ���λ��
            String strPosition = JingWeiUtil.getDrcStrPosition(file, checkpoint.getOffset(),
                    checkpoint.getServerId(), checkpoint.getTimestamp(), checkpoint.getMetaDataVersion());
            currentTx.setPosition(strPosition);
            currentTx.commit();
        }
    }
}