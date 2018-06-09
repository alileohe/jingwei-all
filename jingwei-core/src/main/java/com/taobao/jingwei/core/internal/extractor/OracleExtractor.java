package com.taobao.jingwei.core.internal.extractor;

import com.alibaba.common.lang.StringUtil;
import com.google.protobuf.ByteString;
import com.taobao.erosa.ErosaConnection;
import com.taobao.erosa.exception.ErosaParseException;
import com.taobao.erosa.oracle.OracleErosaConnection;
import com.taobao.erosa.oracle.OracleErosaConnectionStartInfo;
import com.taobao.erosa.oracle.filter.ErosaEntrySink;
import com.taobao.erosa.oracle.filter.ErosaEntrySinkConfig;
import com.taobao.erosa.protocol.E3.HeartBeat;
import com.taobao.erosa.protocol.ErosaEntry;
import com.taobao.erosa.protocol.ErosaEntry.Header;
import com.taobao.erosa.protocol.ErosaEntry.RowData;
import com.taobao.erosa.protocol.ErosaEntry.TransactionBegin;
import com.taobao.erosa.protocol.ErosaEntry.TransactionEnd;
import com.taobao.jingwei.common.JingWeiUtil;
import com.taobao.jingwei.common.node.extractor.OracleExtractorNode;
import com.taobao.jingwei.common.node.type.DBType;
import com.taobao.jingwei.core.util.ErosaHelper;
import com.taobao.tddl.dbsync.DbsyncException;
import com.taobao.tddl.dbsync.dbms.DBMSRowChange;
import com.taobao.tddl.dbsync.extractor.Extractor;
import com.taobao.tddl.dbsync.extractor.ExtractorBrokenException;
import com.taobao.tddl.dbsync.extractor.ExtractorException;
import com.taobao.tddl.dbsync.extractor.Transferer;
import com.taobao.tddl.dbsync.plugin.AbstractPlugin;
import com.taobao.tddl.dbsync.plugin.PluginException;
import com.taobao.tddl.dbsync.tx.Tx;

import java.io.IOException;
import java.sql.Timestamp;

/**
 * description:ORACLE��Extractorʵ������Erosa��ɹ���
 * <p/>
 * <p/>
 * OracleExtractor.java Create on Nov 27, 2012 3:24:17 PM
 * <p/>
 * Copyright (c) 2011 by qihao.
 *
 * @author <a href="mailto:qihao@taobao.com">qihao</a>
 * @version 1.0
 */
public class OracleExtractor extends AbstractPlugin implements Extractor {

    private final OracleExtractorNode extractorNode;
    private final ErosaConnection connection;
    private Tx currentTx;
    private DBMSRowChange unOfferData;
    private String unOfferPosition;
    private long heartbeatPeriod = 15000L;
    private boolean emptyRowData = false;

    public OracleExtractor(OracleExtractorNode extractorNode) {
        this.extractorNode = extractorNode;
        //���������ļ��������Ӷ��󣬶������ñ仯ֱ��������������ʱ�����Ƕ�̬����
        this.connection = new OracleErosaConnection(OracleErosaConnectionStartInfo.parseFrom(extractorNode.getConf()));
    }

    @Override
    public void extract(Transferer transferer) throws ExtractorException, InterruptedException {
        String startPosition = StringUtil.EMPTY_STRING;
        try {
            //��һ�ν���extract��������ȡ����λ��
            startPosition = transferer.getLastPosition();
        } catch (DbsyncException e) {
            logger.error("OracleExtractor extract getLastPosition Error!", e);
            throw new ExtractorBrokenException(e);
        }
        //����λ��,��ȡ�ڲ���file,offset,timestamp
        //������002580:0269723123#1521223144.1354007096
        String[] pos = JingWeiUtil.parseStrPosition(startPosition);
        String file = pos[0];
        Long offset = Long.valueOf(pos[1]);
        Long timestamp = Long.valueOf(pos[3]);
        try {
            //��������
            this.connection.connect();
            //�ⲽ����Ӧ������ס
            this.connection.dump(file, offset, timestamp, new ErosaMessageListener(transferer));
        } catch (IOException e) {
            logger.error("ErosaConnection dump Error! conf: " + this.getExtractorNode().getConf(), e);
            throw new ExtractorBrokenException(e);
        }
        //����ߵ�����˵��Erosa�����������,ֱ����dbsync��ʼ���������̵���destory����
        throw new ExtractorBrokenException("Erosa client has shutdown.");
    }

    /**
     * {@inheritDoc}
     *
     * @see com.taobao.tddl.dbsync.plugin.DbsyncPlugin#destory()
     */
    public void destory() throws PluginException, InterruptedException {
        try {
            // Do shutdown Erosa.
            if (connection != null) {
                connection.disconnect();
            }
            // Rollback uncommit tx
            try {
                if (null != this.currentTx) {
                    this.currentTx.bind();
                }
            } finally {
                //��λ��ر���
                currentTx = null;
                unOfferData = null;
                unOfferPosition = null;
            }
            super.destory();
        } catch (IOException e) {
            throw new PluginException("Erosa client error: " + e.getMessage(), e);
        }
    }

    public OracleExtractorNode getExtractorNode() {
        return extractorNode;
    }

    public void setHeartbeatPeriod(long heartbeatPeriod) {
        this.heartbeatPeriod = heartbeatPeriod;
    }

    public enum TxStatus {
        BEGIN, END, BEGIN_END;
    }

    protected final class ErosaMessageListener extends ErosaEntrySink {

        public static final String TX_ID_KEY = "ora_tx_id";
        public static final String TX_STATUS_KEY = "tx_status";
        public static final String TX_ISDDL_KEY = "tx_isDdl";
        private final Transferer transferer;
        private long lastHeartBeat = System.currentTimeMillis();

        public ErosaMessageListener(Transferer transferer) {
            super(new ErosaEntrySinkConfig(extractorNode.getConf()));
            this.transferer = transferer;
        }

        /* �ûص�Erosa����Ҫ��֤���̵߳���
         * @see com.taobao.erosa.filter.ErosaEntrySink#onRowChanged(com.taobao.erosa.protocol.ErosaEntry.Entry)
         */
        @Override
        public void onRowChanged(com.taobao.erosa.protocol.ErosaEntry.Entry entry) throws ErosaParseException {
            try {
                //�̼߳�飬������ָ����߳̿�����ǰTX�������߳���
                if (null != currentTx) {
                    currentTx.bind();
                }

                Header header = entry.getHeader();
                String currentPosition = ErosaHelper.getErosaPosition(header);
                ByteString storedValue = entry.getStoreValue();

                switch (entry.getEntryType()) {
                    case TRANSACTIONBEGIN:
                        ErosaEntry.TransactionBegin begin = ErosaEntry.TransactionBegin.parseFrom(storedValue);
                        processBegin(currentPosition, begin);
                        break;
                    case ROWDATA:
                        RowData rowData = ErosaEntry.RowData.parseFrom(storedValue);
                        currentTx.setVariable(TX_ISDDL_KEY, rowData.getIsDdl());
                        //��ORACLE��UNDO�ռ�����ʱ�������յ����ݣ���������±������
                        emptyRowData = rowData.getBeforeColumnsCount() == 0 && rowData.getAfterColumnsCount() == 0;
                        if (!emptyRowData) {
                            processRowData(currentPosition, rowData);
                        }
                        break;
                    case TRANSACTIONEND:
                        ErosaEntry.TransactionEnd end = ErosaEntry.TransactionEnd.parseFrom(storedValue);
                        processEnd(currentPosition, end);
                        break;
                    default:
                        throw new ErosaParseException("Unsupported entry type" + entry.toString());
                }
            } catch (Throwable e) {
                throw new ErosaParseException(e);
            }
        }

        private void processBegin(String currentPosition, TransactionBegin begin) throws ErosaParseException {
            if (null != currentTx) {
                //�ϸ�����δEND
                throw new ErosaParseException("Old Transaction Not commit ! position: " + currentPosition);
            }
            //����txId
            String txId = begin.getTransactionId();
            try {
                //�����µ�TX����
                currentTx = this.transferer.begin();
            } catch (Exception e) {
                throw new ErosaParseException("processBegin  Transferer Create Tx Error! txId: " + txId);
            }
            currentTx.setVariable(TX_ID_KEY, txId);
            //����ʱ�䲿��
            Timestamp extractTimestamp = new Timestamp(begin.getExecuteTime());
            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
            //���õ�ǰʱ��Ϊ����ʱ��
            currentTx.setExtractTimestamp(currentTimestamp);
            //�������ݿ��ʱ��
            currentTx.setSourceTimestamp(extractTimestamp);
        }

        private void processRowData(String currentPosition, RowData rowData) throws ErosaParseException {
            if (null == currentTx) {
                //δ����BEGIN��ֱ�ӷ�����ROWDATA
                throw new ErosaParseException("Not Begin Transaction RowData ! position: " + currentPosition);
            }
            //�����DDLֱ�ӷ��أ������ò���TX_ID���������޷���������
            if (rowData.getIsDdl()) {
                currentTx.setVariable(TX_ISDDL_KEY, true);
                logger.warn("unknow EventType type: " + rowData.getEventType()
                        + (rowData.getIsDdl() ? " sql: " + rowData.getSql() : StringUtil.EMPTY_STRING));
                return;
            }
            //�������rowdata�Ƿ���ͬһ��tx
            String txId = rowData.getTransactionId();
            String currentTxId = currentTx.getVariable(TX_ID_KEY);
            if (!currentTxId.equals(txId)) {
                String msg = "before RowData not Comit beforeTxId: " + txId + "currentTxId: " + currentTxId;
                logger.error(msg);
                throw new ErosaParseException(msg);
            }

            DBMSRowChange currentData = null;
            try {
                //��ErosaEntry.RowDataת���ɾ�����DBMSRowChange
                currentData = ErosaHelper.e2jConvertRowData(rowData, DBType.ORACLE);
            } catch (Exception e) {
                throw new ErosaParseException("processRowData convert DBMSRowChange Error! Position :"
                        + currentPosition, e);
            }
            /*
             * �����INSERT,UPDATE,DELETE��ACTION������currentDataת��ΪNULL
			 * �ٶ���ЩACTION������DDL,����������DDL�ļ�鵫��Ϊ�˷�ֹEROSA�ṩ��
			 * DDL��������⣬���ﻹ�Ƕ���һ���жϰ�!
			 */
            if (null == currentData) {
                currentTx.setVariable(TX_ISDDL_KEY, true);
                logger.warn("Maybe Erosa RowData isDDL Invalid unknow EventType type: " + rowData.getEventType()
                        + (rowData.getIsDdl() ? " sql: " + rowData.getSql() : StringUtil.EMPTY_STRING));
                return;
            }
            /*
             * RowData��offer�ж�������ʼ��һ�������Ȳ�offer,offer�߼�Ϊ:
			 * ÿ��offer��һ����������ʼ�ĵ�һ�����ݴ�������ʼ�ı��
			 */
            if (null == unOfferData) {
                //����ʼ��һ����������ʼ���
                currentData.setOptionValue(TX_STATUS_KEY, TxStatus.BEGIN.toString());
            } else {
                //����offer��һ��δOffer������
                currentTx.setPosition(unOfferPosition);
                try {
                    currentTx.offer(unOfferData);
                } catch (Exception e) {
                    throw new ErosaParseException("processRowData offer DBMSRowChange Error! Position: "
                            + unOfferPosition);
                }
            }
            //���õ�ǰ�����Լ���Ӧ��λ��Ϊ�´��ύ������
            unOfferData = currentData;
            unOfferPosition = currentPosition;
        }

        private void processEnd(String currentPosition, TransactionEnd end) throws ErosaParseException,
                DbsyncException, InterruptedException {
            try {
                String txId = end.getTransactionId();
                if (null == currentTx) {
					/*
					 * û��begin��end �����м���ԭ��:
					 * 1.������¼��λ����end���ʼ��λ�㣬ÿ������һ��������λ���ط�END
					 * 2.erosa���ܴ���û��Begin�ͷ�END�Ŀ���
					 * ���ϼ������Ŀǰ�����Ժ���
					 */
                    logger.warn("end without begin Transaction  txId: " + txId + " position: " + currentPosition);
                    return;
                }
                //��beginû��rowData���������
                if (null == unOfferData) {
                    Boolean isDDL = currentTx.getVariable(TX_ISDDL_KEY);
                    if ((null!=isDDL&&isDDL) || emptyRowData) {
                        //�ύ��������DDL������unOfferData��NULL,����ֱ���ύλ��
                        currentTx.setPosition(currentPosition);
                        currentTx.commit();
                        return;
                    } else {
                        //erosa����begin��û��rowDataֱ�ӷ���END
                        throw new ErosaParseException("transaction begin whithout RowData End  TxId: " + txId
                                + "currentTxId: " + currentPosition);
                    }
                }
                //�ж��ύ�������Ƿ��ǵ�ǰ������
                String currentTxId = currentTx.getVariable(TX_ID_KEY);
                if (!currentTxId.equals(txId)) {
                    throw new ErosaParseException("transaction End comit not currentTx txId: " + txId + "currentTxId: "
                            + currentTxId);
                }
                //�������״̬
                boolean row_begin = TxStatus.BEGIN.toString().equals(unOfferData.getOptionValue(TX_STATUS_KEY));
                unOfferData.setOptionValue(TX_STATUS_KEY,
                        row_begin ? TxStatus.BEGIN_END.toString() : TxStatus.END.toString());

                //����offer���ݵ�λ�㣬offer����ʱ��־�Ų���
                currentTx.setPosition(unOfferPosition);
                //��ܵ��ύ����
                currentTx.offer(unOfferData);

                //�ύ�������λ��commitʱ���¼
                currentTx.setPosition(currentPosition);
                currentTx.commit();
            } finally {
                //��λ��ر���
                currentTx = null;
                unOfferData = null;
                unOfferPosition = null;
            }
        }

        @Override
        public void onHeartBeat(HeartBeat heartBeat) throws ErosaParseException {
            long now = System.currentTimeMillis();
            //����ʱ�䷥ֵ����Ӧһ������
            if (now - this.lastHeartBeat >= OracleExtractor.this.heartbeatPeriod) {
                try {
                    this.transferer.heartbeat();
                    this.lastHeartBeat = now;
                } catch (Throwable e) {
                    //�����������Ӧʵ�ַ����쳣ֱ���׵��ϲ��ж��������ƹܵ�
                    //�����������������񲻹��ģ���ô���Լ�������ʵ�ֳԵ��쳣����
                    throw new ErosaParseException("Dbsync error on heartbeat: " + e.getMessage(), e);
                }
            }
        }
    }
}