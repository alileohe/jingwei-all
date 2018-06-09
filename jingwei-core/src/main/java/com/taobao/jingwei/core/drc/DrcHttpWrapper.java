package com.taobao.jingwei.core.drc;

import com.alibaba.common.lang.StringUtil;
import com.taobao.drc.sdk.message.Message;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: <a href="mailto:qihao@taobao.com">qihao</a>
 * Date: 13-2-4
 * Time: ÏÂÎç4:28
 * version 1.0
 */
public class DrcHttpWrapper {

    private final static String HTTP_PREFIX = "http://";

    private final static int DEFAULT_CONNECTION_TIMEOUT = 3600 * 1000; /* 1 hours. */

    private final static String DEFAULT_URL_ENCODING = "UTF-8";
    private final static String DEFAULT_RESPONSE_ENCODING = "GBK";

    private ReentrantLock lock = new ReentrantLock();

    private volatile HttpHandler httpHandler;

    private String urlEncoding;

    private String responseEncoding;

    private int conTimeout;

    private int soTimeout;

    public void setUrl(final String url) {
        String sendUrl;
        //Remove Http From Url
        if (url.startsWith(HTTP_PREFIX)) {
            sendUrl = url.replaceAll(HTTP_PREFIX, "");
        } else {
            sendUrl = url;
        }
        lock.lock();
        try {
            //Close Before Inited HttpHandler
            if (null != httpHandler) {
                this.httpHandler.close();
            }
            this.httpHandler = new HttpHandler(HTTP_PREFIX + sendUrl);
        } finally {
            lock.unlock();
        }
    }

    public void sendEncodingRequest() throws IOException, HttpBadResponseException {
        this.httpHandler.sendRequest(this.getUrlEncoding(), this.getResponseEncoding(), this.getConTimeout(), this.getSoTimeout());
    }

    /**
     * Add parameters to the http request.
     *
     * @param props is the map of the parameters.
     */
    public void addParams(final Map<String, String> props) {
        if (null != httpHandler) {
            for (Map.Entry<String, String> kv : props.entrySet()) {
                addParam(kv.getKey(), kv.getValue());
            }
        }
    }

    public Message getResponse() throws DRCClientException {
        try {
            return null != httpHandler ? httpHandler.recvDRCPResponse() : null;
        } catch (Exception e) {
            throw new DRCClientException("getDrcResponse Error!", e);
        }
    }

    /**
     * Add parameters to the http request.
     *
     * @param key   parameter key
     * @param value parameter value
     */

    public void addParam(final String key, final String value) {
        if (null != httpHandler) {
            httpHandler.addFormParam(key, value);
        }
    }

    public int getConTimeout() {
        return conTimeout <= 0 ? DEFAULT_CONNECTION_TIMEOUT : conTimeout;
    }

    public void setConTimeout(int conTimeout) {
        this.conTimeout = conTimeout;
    }

    public int getSoTimeout() {
        return soTimeout <= 0 ? DEFAULT_CONNECTION_TIMEOUT : soTimeout;
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    public String getUrlEncoding() {
        return StringUtil.isEmpty(urlEncoding) ? DEFAULT_URL_ENCODING : urlEncoding;
    }

    public void setUrlEncoding(String urlEncoding) {
        this.urlEncoding = urlEncoding;
    }

    public String getDumpUrl() {
        return null != httpHandler ? httpHandler.getUrl() : StringUtil.EMPTY_STRING;
    }

    public String getResponseEncoding() {
        return StringUtil.isBlank(responseEncoding) ? DEFAULT_RESPONSE_ENCODING : responseEncoding;
    }

    public void setResponseEncoding(String responseEncoding) {
        this.responseEncoding = responseEncoding;
    }

    public void close() {
        this.httpHandler.close();
    }
}