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
 * description:ORACLE的Extractor实现利用Erosa完成功能
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
        //根据配置文件创建连接对象，对于配置变化直接重启精卫，暂时不考虑动态更新
        this.connection = new OracleErosaConnection(OracleErosaConnectionStartInfo.parseFrom(extractorNode.getConf()));
    }

    @Override
    public void extract(Transferer transferer) throws ExtractorException, InterruptedException {
        String startPosition = StringUtil.EMPTY_STRING;
        try {
            //第一次进入extract方法，获取启动位点
            startPosition = transferer.getLastPosition();
        } catch (DbsyncException e) {
            logger.error("OracleExtractor extract getLastPosition Error!", e);
            throw new ExtractorBrokenException(e);
        }
        //解析位点,提取内部的file,offset,timestamp
        //类似于002580:0269723123#1521223144.1354007096
        String[] pos = JingWeiUtil.parseStrPosition(startPosition);
        String file = pos[0];
        Long offset = Long.valueOf(pos[1]);
        Long timestamp = Long.valueOf(pos[3]);
        try {
            //创建连接
            this.connection.connect();
            //这步正常应该诸塞住
            this.connection.dump(file, offset, timestamp, new ErosaMessageListener(transferer));
        } catch (IOException e) {
            logger.error("ErosaConnection dump Error! conf: " + this.getExtractorNode().getConf(), e);
            throw new ExtractorBrokenException(e);
        }
        //如果走到这里说明Erosa出现问题挂了,直接由dbsync开始走销毁流程调用destory方法
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
                //复位相关变量
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

        /* 该回调Erosa绝对要保证单线程调用
         * @see com.taobao.erosa.filter.ErosaEntrySink#onRowChanged(com.taobao.erosa.protocol.ErosaEntry.Entry)
         */
        @Override
        public void onRowChanged(com.taobao.erosa.protocol.ErosaEntry.Entry entry) throws ErosaParseException {
            try {
                //线程检查，如果发现更换线程拷贝当前TX对象到新线程中
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
                        //当ORACLE的UNDO空间满的时候会产生空的数据，这个里做下标记跳过
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
                //上个事务未END
                throw new ErosaParseException("Old Transaction Not commit ! position: " + currentPosition);
            }
            //设置txId
            String txId = begin.getTransactionId();
            try {
                //创建新的TX对象
                currentTx = this.transferer.begin();
            } catch (Exception e) {
                throw new ErosaParseException("processBegin  Transferer Create Tx Error! txId: " + txId);
            }
            currentTx.setVariable(TX_ID_KEY, txId);
            //设置时间部分
            Timestamp extractTimestamp = new Timestamp(begin.getExecuteTime());
            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
            //设置当前时间为解析时间
            currentTx.setExtractTimestamp(currentTimestamp);
            //设置数据库的时间
            currentTx.setSourceTimestamp(extractTimestamp);
        }

        private void processRowData(String currentPosition, RowData rowData) throws ErosaParseException {
            if (null == currentTx) {
                //未发送BEGIN，直接发送了ROWDATA
                throw new ErosaParseException("Not Begin Transaction RowData ! position: " + currentPosition);
            }
            //如果是DDL直接返回，由于拿不到TX_ID所以这里无法做事务检查
            if (rowData.getIsDdl()) {
                currentTx.setVariable(TX_ISDDL_KEY, true);
                logger.warn("unknow EventType type: " + rowData.getEventType()
                        + (rowData.getIsDdl() ? " sql: " + rowData.getSql() : StringUtil.EMPTY_STRING));
                return;
            }
            //检查两次rowdata是否是同一个tx
            String txId = rowData.getTransactionId();
            String currentTxId = currentTx.getVariable(TX_ID_KEY);
            if (!currentTxId.equals(txId)) {
                String msg = "before RowData not Comit beforeTxId: " + txId + "currentTxId: " + currentTxId;
                logger.error(msg);
                throw new ErosaParseException(msg);
            }

            DBMSRowChange currentData = null;
            try {
                //将ErosaEntry.RowData转换成精卫的DBMSRowChange
                currentData = ErosaHelper.e2jConvertRowData(rowData, DBType.ORACLE);
            } catch (Exception e) {
                throw new ErosaParseException("processRowData convert DBMSRowChange Error! Position :"
                        + currentPosition, e);
            }
            /*
             * 如果非INSERT,UPDATE,DELETE的ACTION，则导致currentData转换为NULL
			 * 假定这些ACTION是属于DDL,理论上面有DDL的检查但是为了防止EROSA提供的
			 * DDL标记有问题，这里还是多做一下判断吧!
			 */
            if (null == currentData) {
                currentTx.setVariable(TX_ISDDL_KEY, true);
                logger.warn("Maybe Erosa RowData isDDL Invalid unknow EventType type: " + rowData.getEventType()
                        + (rowData.getIsDdl() ? " sql: " + rowData.getSql() : StringUtil.EMPTY_STRING));
                return;
            }
            /*
             * RowData的offer判定，事务开始第一条数据先不offer,offer逻辑为:
			 * 每次offer上一条数据事务开始的第一条数据打上事务开始的标记
			 */
            if (null == unOfferData) {
                //事务开始第一条设置事务开始标记
                currentData.setOptionValue(TX_STATUS_KEY, TxStatus.BEGIN.toString());
            } else {
                //立即offer上一条未Offer的数据
                currentTx.setPosition(unOfferPosition);
                try {
                    currentTx.offer(unOfferData);
                } catch (Exception e) {
                    throw new ErosaParseException("processRowData offer DBMSRowChange Error! Position: "
                            + unOfferPosition);
                }
            }
            //重置当前数据以及对应的位点为下次提交的数据
            unOfferData = currentData;
            unOfferPosition = currentPosition;
        }

        private void processEnd(String currentPosition, TransactionEnd end) throws ErosaParseException,
                DbsyncException, InterruptedException {
            try {
                String txId = end.getTransactionId();
                if (null == currentTx) {
					/*
					 * 没有begin就end 可能有几种原因:
					 * 1.精卫记录的位点是end命令开始的位点，每次重启一定会从这个位点重发END
					 * 2.erosa可能存在没发Begin就发END的可能
					 * 以上几种情况目前都可以忽略
					 */
                    logger.warn("end without begin Transaction  txId: " + txId + " position: " + currentPosition);
                    return;
                }
                //有begin没有rowData的情况处理
                if (null == unOfferData) {
                    Boolean isDDL = currentTx.getVariable(TX_ISDDL_KEY);
                    if ((null!=isDDL&&isDDL) || emptyRowData) {
                        //提交的数据是DDL，所以unOfferData是NULL,可以直接提交位点
                        currentTx.setPosition(currentPosition);
                        currentTx.commit();
                        return;
                    } else {
                        //erosa发了begin就没发rowData直接发了END
                        throw new ErosaParseException("transaction begin whithout RowData End  TxId: " + txId
                                + "currentTxId: " + currentPosition);
                    }
                }
                //判断提交的事务是否是当前的事务
                String currentTxId = currentTx.getVariable(TX_ID_KEY);
                if (!currentTxId.equals(txId)) {
                    throw new ErosaParseException("transaction End comit not currentTx txId: " + txId + "currentTxId: "
                            + currentTxId);
                }
                //标记事务状态
                boolean row_begin = TxStatus.BEGIN.toString().equals(unOfferData.getOptionValue(TX_STATUS_KEY));
                unOfferData.setOptionValue(TX_STATUS_KEY,
                        row_begin ? TxStatus.BEGIN_END.toString() : TxStatus.END.toString());

                //设置offer数据的位点，offer出错时日志排查用
                currentTx.setPosition(unOfferPosition);
                //向管道提交数据
                currentTx.offer(unOfferData);

                //提交事务结束位点commit时候记录
                currentTx.setPosition(currentPosition);
                currentTx.commit();
            } finally {
                //复位相关变量
                currentTx = null;
                unOfferData = null;
                unOfferPosition = null;
            }
        }

        @Override
        public void onHeartBeat(HeartBeat heartBeat) throws ErosaParseException {
            long now = System.currentTimeMillis();
            //到达时间伐值就响应一次心跳
            if (now - this.lastHeartBeat >= OracleExtractor.this.heartbeatPeriod) {
                try {
                    this.transferer.heartbeat();
                    this.lastHeartBeat = now;
                } catch (Throwable e) {
                    //如果心跳的响应实现发生异常直接抛到上层中断整个复制管道
                    //如果对于心跳正常与否不关心，那么在自己的心跳实现吃掉异常即可
                    throw new ErosaParseException("Dbsync error on heartbeat: " + e.getMessage(), e);
                }
            }
        }
    }
}