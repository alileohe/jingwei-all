package com.taobao.jingwei.common.node;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * 报警节点
 * <p>description:<p>
 *
 * @author <a href="mailto:qihao@taobao.com">qihao</a>
 * @version 1.0
 * @{#} AlarmNode.java Create on Dec 14, 2011 11:53:23 AM
 * <p/>
 * Copyright (c) 2011 by qihao.
 */
public class AlarmNode extends AbstractNode {

    private String ownerDataIdOrPath;

    private Date timestamp;

    private String message;

    private Throwable throwable;

    private String stackTrace;

    /**
     * json存储数据key定义
     */

    private final static String TIMESTAMP_KEY = "timestamp";
    private final static String MESSAGE_KEY = "message";
    private final static String STACKTRACE_KEY = "stackTrace";

    @Override
    protected void specilizeAttributeToJsonObject(JSONObject jsonObject) throws JSONException {
        jsonObject.put(MESSAGE_KEY, StringUtil.defaultIfBlank(this.getMessage()));
        jsonObject.put(TIMESTAMP_KEY, JingWeiUtil.date2String(new Date()));
        if (null != throwable) {
            Throwable rootThrowable = ExceptionUtils.getRootCause(throwable);
            this.stackTrace = ExceptionUtils.getStackTrace(null == rootThrowable ? throwable : rootThrowable);
        }
        jsonObject.put(STACKTRACE_KEY, StringUtil.defaultIfBlank(this.getStackTrace()));
    }

    @Override
    protected void jsonObjectToSpecilizeAttribute(JSONObject jsonObject) throws JSONException {
        //throwable复位，方式外面复用该对象
        this.throwable = null;
        this.setMessage(jsonObject.getString(MESSAGE_KEY));
        this.stackTrace = jsonObject.getString(STACKTRACE_KEY);
        this.timestamp = JingWeiUtil.string2Date(jsonObject.getString(TIMESTAMP_KEY));
    }

    @Override
    public String getDataIdOrNodePath() {
        return AlarmNode.getDataIdOrNodePathByOwner(ownerDataIdOrPath);
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * zk路径为:例如/jingwei-v2/tasks/任务名称/hosts/机器名称/status
     * 其中/jingwei-v2/tasks/任务名称/hosts/机器名称 为ownerDataIdOrPath
     */

    public static String getDataIdOrNodePathByOwner(String ownerDataIdOrPath) {
        if (StringUtil.isBlank(ownerDataIdOrPath)) {
            return StringUtil.EMPTY_STRING;
        }
        return ownerDataIdOrPath + ZK_PATH_SEP + JINGWEI_SCAN_ALARM_NODE;
    }

    public String getOwnerDataIdOrPath() {
        return ownerDataIdOrPath;
    }

    public void setOwnerDataIdOrPath(String ownerDataIdOrPath) {
        this.ownerDataIdOrPath = ownerDataIdOrPath;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public String getStrTimestamp() {
        String strTime = StringUtil.EMPTY_STRING;
        if (null != this.timestamp) {
            strTime = JingWeiUtil.date2String(this.timestamp);
        }
        return strTime;
    }

    @Override
    public boolean isPersistent() {
        return true;
    }
}
