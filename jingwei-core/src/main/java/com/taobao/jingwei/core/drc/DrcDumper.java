package com.taobao.jingwei.core.drc;

import com.alibaba.common.lang.StringUtil;
import com.taobao.drc.sdk.Checkpoint;
import com.taobao.drc.sdk.DRCOptions;
import com.taobao.drc.sdk.message.DataMessage;
import com.taobao.drc.sdk.message.ErrorMessage;
import com.taobao.drc.sdk.message.Message;
import com.taobao.drc.sdk.message.RedirectMessage;
import com.taobao.jingwei.common.JingWeiConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * User: <a href="mailto:qihao@taobao.com">qihao</a>
 * Date: 13-2-4
 * Time: 下午4:14
 * version 1.0
 */
public class DrcDumper {

    protected static final Log logger = LogFactory.getLog(DrcDumper.class);
    private final DRCOptions drcOptions = new DRCOptions();
    private final DrcHttpWrapper drcHttpWrapper = new DrcHttpWrapper();
    private String clusterUrl;
    private String groupName;
    private String dbName;
    private String passwd;
    private String filterStr;
    private int conTimeout;
    private int soTimeout;
    private String urlEncoding = "UTF-8";
    private String responseEncoding = "GBK";
    private DrcDataListener dataListener;
    private volatile String instance;
    private DumpThread dumpThread;
    private static final long MAX_SLEEP_TIME = 10 * 1000;

    public static final String DRC_TIME_SEEK_COMMOND="$Commond-TimeSeek";

    private ThreadLocal<Boolean> Tcontext = new ThreadLocal();

    public static void main(String[] args) throws DRCClientException {

        // 初始化第一次连接时的参数
        String url = "http://10.232.31.54:8080/clustermanager/congo";
        String groupName = "ClusterUT";
        String dbName = "icdb0";
        String password = "123456";
        String checkpoint = "1359348377";

        DrcDumper dumper = new DrcDumper();
        dumper.setClusterUrl(url);
        dumper.setDbName(dbName);
        dumper.setGroupName(groupName);
        dumper.setPasswd(password);
        dumper.setFilterStr("*;*");

        dumper.setDataListener(new DrcDataListener() {
            @Override
            public void onDataChange(DataMessage dataMessage, String dbName) throws Exception {
                System.out.println(dataMessage);
            }
        });
        Checkpoint point = new Checkpoint();
        //point.setTimestamp(checkpoint);
        point.setMetaDataVersion("0");
        System.out.printf(dumper.toString());
        dumper.startDump(point);
    }

    public void startDump(Checkpoint position) throws DRCClientException {
        //如果对于ZeroTime有默认值，这里设置下
        //DRCMessageParser.setZeroTime(Timestamp ts);
        //初始化drcOptions
        try {
            logger.warn("All ClusterUrl : " + this.getClusterUrl());
            String[] clusterUrls = StringUtil.split(this.getClusterUrl(), JingWeiConstants.SEMICOLON_STR);
            int i = new Random().nextInt(clusterUrls.length);
            String randomUrl = clusterUrls[i];
            logger.warn("Use Random ClusterUrl : " + randomUrl);
            drcOptions.setURL(randomUrl);
        } catch (Exception e) {
            throw new DRCClientException("setClusterUrl Error! url: " + this.getClusterUrl(), e);
        }
        drcOptions.setGroup(this.getGroupName());
        drcOptions.setDatabase(this.getDbName());
        drcOptions.setPassword(this.getPasswd());

        //设置表名和列名过滤字符串，逗号分割 如：tableA;ColumnA,tableB;ColumnB
        if (StringUtil.isNotBlank(this.getFilterStr())) {
            String[] filters = StringUtil.split(this.getFilterStr(), JingWeiConstants.COMMA_STR);
            for (String filter : filters) {
                drcOptions.setRequiredTablesAndColumns(filter);
            }
        }
        //设置启动位点对象
        if (null != position) {
            drcOptions.setCheckpoint(position);
            drcOptions.setDatabaseServerId(position.getServerId());
        }
        //准备drcHttpWrapper的参数
        drcHttpWrapper.setConTimeout(this.getConTimeout());
        drcHttpWrapper.setSoTimeout(this.getSoTimeout());
        drcHttpWrapper.setUrlEncoding(this.getUrlEncoding());
        drcHttpWrapper.setResponseEncoding(this.getResponseEncoding());
        drcHttpWrapper.setUrl(drcOptions.getRandomURL());
        drcHttpWrapper.addParams(drcOptions.getParameters());
        try {
            drcHttpWrapper.sendEncodingRequest();
        } catch (Exception e) {
            throw new DRCClientException("Frist send Request Error url: " + drcHttpWrapper.getDumpUrl(), e);
        }
        dumpThread = new DumpThread();
        dumpThread.start();

        while (!dumpThread.isStop() && null == dumpThread.getThrowable()) {
            try {
                Thread.sleep(MAX_SLEEP_TIME);
            } catch (InterruptedException e) {
                logger.error("dumpThread dump Error !", e);
                break;
            }
        }
        try {
            stopDump();
        } finally {
            if (null != dumpThread.getThrowable()) {
                throw dumpThread.getThrowable();
            }
        }
    }

    class DumpThread extends Thread {

        private volatile boolean stop = false;
        private DRCClientException throwable;

        @Override
        public void run() {
            Message message;
            labDO:
            do {
                // 从server获取消息
                try {
                    if ((message = drcHttpWrapper.getResponse()) == null) {
                        this.setThrowable(new DRCClientException("Get null message, server may shutdown."));
                        break;
                    }
                    //正常消息部分
                    switch (message.getType()) {
                        case 300:
                            processRedirect((RedirectMessage) message);
                            break;
                        case 100:
                            processData((DataMessage) message);
                            break;
                        case 200:
                            break;
                        case 400:
                            processError((ErrorMessage) message);
                            break;
                        default:
                            this.setThrowable(new DRCClientException("Wrong DRCMessage type " + message.getType()));
                            break labDO;
                    }
                } catch (DRCClientException e) {
                    this.setThrowable(e);
                    break;
                }
            } while (!stop);
        }

        public boolean isStop() {
            return stop;
        }

        public void setStop(boolean stop) {
            this.stop = stop;
        }

        public DRCClientException getThrowable() {
            return throwable;
        }

        public void setThrowable(DRCClientException throwable) {
            this.throwable = throwable;
        }
    }


    private void processError(ErrorMessage message) throws DRCClientException {
        ErrorMessage errorMessage = (ErrorMessage) message;
        if (errorMessage.getCode().equalsIgnoreCase("ERROR")) {
            StringBuilder errorBuilder = new StringBuilder("Server: ");
            errorBuilder.append(drcHttpWrapper.getDumpUrl());
            errorBuilder.append(" internal error: ");
            errorBuilder.append(errorMessage.getMessage());
            errorBuilder.append(" ErrorCode: 500");
            throw new DRCClientException(errorBuilder.toString());
        }
    }

    private void processData(DataMessage message) throws DRCClientException {
        DataMessage dataMessage = (DataMessage) message;
        if (dataMessage.getRecordCount() > 0) {
            try {
                //回调监听器
                this.getDataListener().onDataChange(dataMessage, this.getDbName());
            } catch (Exception e) {
                throw new DRCClientException("onDataChange Error: ", e);
            }
            //找到记录中最后一条非心跳位点，用其位点更新内存中的位点为了给重定向使用
            //DRC保证一定是在事务结束后才发重定向消息
            List<DataMessage.Record> recordList = dataMessage.getRecordList();
            int recordCount = recordList.size();
            for (int i = recordCount - 1; i >= 0; i--) {
                DataMessage.Record record = recordList.get(i);
                if (DataMessage.Record.Type.HEARTBEAT != record.getOpt()) {
                    //创建一个位点对象
                    Checkpoint currentPosition = getCheckpoint(record);
                    drcOptions.setCheckpoint(currentPosition);
                    break;
                }
            }
        }
    }

    public Checkpoint getCheckpoint(DataMessage.Record record) {
        Checkpoint currentPosition = new Checkpoint();
        String strPosition = record.getCheckpoint();
        int index = strPosition.indexOf(Checkpoint.CHECKPOINT_SEP);
        currentPosition.setFile(strPosition.substring(index + 1));
        currentPosition.setOffset(strPosition.substring(0, index));
        currentPosition.setTimestamp(record.getTimestamp());
        String currentMetaVersion = record.getMetadataVersion();
        currentPosition.setMetaDataVersion(currentMetaVersion);
        currentPosition.setServerId(instance);
        return currentPosition;
    }

    private void processRedirect(RedirectMessage message) throws DRCClientException {
        RedirectMessage redirectMessage = (RedirectMessage) message;
        logger.warn("Redirect Message: " + redirectMessage);
        /*
        * 这里非常恶心，DRC如果根据时间回溯的话，第一条数据不保证是事务开始，
        * 所以这种情况下精卫必须重置事务开始检查，让第一条非事务开始的数据流过去。
        * 如果是主备切换连续两次RedirectMessage消息间一定不能有数据
        *
        * 于DRC的二败约定：
        * 如果精卫给DRC位点，DRC重定向消息里checkpoint必须不为空(要么是绝对位点，要么是时间戳回溯)
        * DRC需要做位点替换，如果精卫给的位点是切库前的位点，DRC将位点替换掉，重定向消息的checkpoint中只保留时间戳返回给精卫。
        * 如果精卫给的位点是正常当前数据库的位点，DRC就将该位点原封不动的，在重定向消息的checkpoint中返回给精卫。
        *
        * 注：精卫根据DRC返回的位点判断
        * 如下：
        * 如果DRC重定向消息中checkpoint返回包含“@”说明是绝对位点正常的启停，无需重置精卫事务开始检查。
        * 如果dRC重定向消息中checkpoint返回不包含“@”说明是时间戳启动，说明之前的位点是主备切换前的位点，DRC要进行
        * 按照时间回溯，所以精卫这边需要重置事务开始检查。
        * */
        String strCheckPoint = redirectMessage.getUserParameters().get("checkpoint");
        if (StringUtil.isBlank(strCheckPoint)) {
            throw new DRCClientException("RedirectMessage checkpoint Is Empty! ");
        }
        if (!StringUtil.contains(strCheckPoint, JingWeiConstants.AT_STR)) {
            //如果重定向位点里没有@号就代表是时间戳回溯，一般DB发生主备切换会这样
            try {
                dataListener.onDataChange(null, DRC_TIME_SEEK_COMMOND);
            } catch (Exception e) {
                throw new DRCClientException("reset Tx Check Error! " + redirectMessage, e);
            }
        }
        //获取实例信息
        instance = redirectMessage.getUserParameters().get("instance");
        try {
            drcOptions.setURL(redirectMessage.getUrl());
        } catch (Exception e) {
            throw new DRCClientException("setRedirectUrl Error! url: " + redirectMessage, e);
        }
        // 获取跳转消息里传来的一些参数和其赋值, 比如可能group参数的值会从ClusterUT改变为ClusterUT1,此由服务端决定
        for (Map.Entry<String, String> extraParams :
                redirectMessage.getUserParameters().entrySet()) {
            drcOptions.addParameter(extraParams.getKey(),
                    extraParams.getValue());
        }
        // 获取一个随机的ip:port地址, 实例化一个server的连接
        drcHttpWrapper.setUrl(drcOptions.getRandomURL());
        for (String parameterName : redirectMessage.getRequires()) {
            // 设置服务器强制要求的参数, 如无法获取则报错
            final String value = drcOptions.getParameter(parameterName);
            if (value != null) {
                drcHttpWrapper.addParam(parameterName, value);
            } else {
                throw new DRCClientException
                        ("Required parameter " + parameterName + " cannot be provided by client  ErrorCode: " + 400);
            }
        }
        for (String parameterName : redirectMessage.getOptional()) {
            // 设置服务器需要的可选参数, 如无法获取则不提供
            final String value = drcOptions.getParameter(parameterName);
            if (value != null) {
                drcHttpWrapper.addParam(parameterName, value);
            }
        }
        // 有延时启动的要求的, 延迟对应的秒数
        if (redirectMessage.getDelayed() > 0) {
            try {
                Thread.sleep(redirectMessage.getDelayed());
            } catch (InterruptedException e) {
                throw new DRCClientException("Redirect Delay Error!", e);
            }
        }
        try {
            drcHttpWrapper.sendEncodingRequest();
        } catch (Exception e) {
            throw new DRCClientException("Redirect send Request Error url: " + drcHttpWrapper.getDumpUrl(), e);
        }
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public String getClusterUrl() {
        return clusterUrl;
    }

    public void setClusterUrl(String clusterUrl) {
        this.clusterUrl = clusterUrl;
    }

    public String getFilterStr() {
        return filterStr;
    }

    public void setFilterStr(String filterStr) {
        this.filterStr = filterStr;
    }

    public DrcDataListener getDataListener() {
        return dataListener;
    }

    public void setDataListener(DrcDataListener dataListener) {
        this.dataListener = dataListener;
    }

    public void stopDump() {
        if (null != dumpThread && !dumpThread.isStop()) {
            dumpThread.setStop(true);
            try {
                dumpThread.join();
                drcHttpWrapper.close();
                logger.warn("drc dumpThread is Close!");
            } catch (InterruptedException e) {
                logger.error("wait dumpThread Stop Error!", e);
            }
        }
    }

    public int getConTimeout() {
        return conTimeout;
    }

    public void setConTimeout(int conTimeout) {
        this.conTimeout = conTimeout;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    public String getUrlEncoding() {
        return urlEncoding;
    }

    public void setUrlEncoding(String urlEncoding) {
        this.urlEncoding = urlEncoding;
    }

    public String getResponseEncoding() {
        return responseEncoding;
    }

    public void setResponseEncoding(String responseEncoding) {
        this.responseEncoding = responseEncoding;
    }

    public String getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        return "DrcDumper{" +
                "clusterUrl='" + clusterUrl + '\'' +
                ", groupName='" + groupName + '\'' +
                ", dbName='" + dbName + '\'' +
                ", passwd='" + passwd + '\'' +
                ", filterStr='" + filterStr + '\'' +
                ", conTimeout=" + conTimeout +
                ", soTimeout=" + soTimeout +
                ", urlEncoding='" + urlEncoding + '\'' +
                ", responseEncoding='" + responseEncoding + '\'' +
                ", instance='" + instance + '\'' +
                '}';
    }
}