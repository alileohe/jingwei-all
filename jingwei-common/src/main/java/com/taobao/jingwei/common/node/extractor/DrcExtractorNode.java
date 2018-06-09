package com.taobao.jingwei.common.node.extractor;

import com.alibaba.common.lang.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

/**
 * User: <a href="mailto:qihao@taobao.com">qihao</a>
 * Date: 13-2-14
 * Time: ÏÂÎç10:20
 * version 1.0
 */
public class DrcExtractorNode extends AbstractExtractorNode {

    private final Properties conf = new Properties();

    private String responseEncoding;

    private final static String PRAM_CLUSTER_URL = "clusterUrl";
    private final static String PRAM_GROUP_NAME = "groupName";
    private final static String PRAM_DBNAME = "dbName";
    private final static String PRAM_PASSWD = "passwd";
    private final static String PRAM_FILTER_STR = "filterStr";
    private final static String PRAM_URL_ENCODING = "urlEncoding";
    private final static String PRAM_RESPONSE_ENCODING = "responseEncoding";
    private final static String EXTRACTOR_DATA = "extractorData";

    public DrcExtractorNode() {
        super();
    }

    public DrcExtractorNode(Properties conf) {
        for (Map.Entry<Object, Object> entry : conf.entrySet()) {
            String key = StringUtil.trim((String) entry.getKey());
            String value = StringUtil.trim((String) entry.getValue());
            this.conf.put(key, value);
        }
    }

    public DrcExtractorNode(String jsonString) {
        try {
            this.jsonStringToNodeSelf(jsonString);
        } catch (JSONException e) {
            logger.error("load json Create BinLogExtractorNode Error", e);
        }
    }

    public DrcExtractorNode(File propFile) {
        if (null != propFile) {
            FileInputStream ips = null;
            try {
                ips = new FileInputStream(propFile);
                this.getConf().clear();
                this.getConf().load(ips);
            } catch (IOException e) {
                logger.error("load propFile Create OracleExtractorNode Error", e);
            } finally {
                if (null != ips) {
                    try {
                        ips.close();
                    } catch (IOException e) {
                        logger.error("load propFile Close OracleExtractorNode Error", e);
                    }
                }
            }
        }
    }

    @Override
    protected void specilizeAttributeToJsonObject(JSONObject jsonObject) throws JSONException {
        StringBuffer sb = new StringBuffer();
        Enumeration<Object> enumeration = conf.keys();
        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            String value = conf.getProperty(key, StringUtil.EMPTY_STRING);
            sb.append(key).append("=").append(StringUtil.defaultIfBlank(value));
            if (enumeration.hasMoreElements()) {
                sb.append(LINE_SEP);
            }
        }
        jsonObject.put(EXTRACTOR_DATA, sb.toString());
    }

    @Override
    protected void jsonObjectToSpecilizeAttribute(JSONObject jsonObject) throws JSONException {
        String extractorData = jsonObject.getString(EXTRACTOR_DATA);
        if (StringUtil.isNotBlank(extractorData)) {
            this.setExtractorData(extractorData);
            try {
                conf.load(new StringReader(extractorData));
            } catch (IOException e) {
                logger.error("load prop data Error ", e);
            }
        }
    }

    public Properties getConf() {
        return conf;
    }

    public String getClusterUrl() {
        return this.getConfValue(PRAM_CLUSTER_URL);
    }

    public void setClusterUrl(String clusterUrl) {
        this.setConfValue(PRAM_CLUSTER_URL, clusterUrl);
    }

    public String getGroupName() {
        return this.getConfValue(PRAM_GROUP_NAME);
    }

    public void setGroupName(String groupName) {
        this.setConfValue(PRAM_GROUP_NAME, groupName);
    }

    public String getDbName() {
        return this.getConfValue(PRAM_DBNAME);
    }

    public void setDbName(String dbName) {
        this.setConfValue(PRAM_DBNAME, dbName);
    }

    public String getPasswd() {
        return this.getConfValue(PRAM_PASSWD);
    }

    public void setPasswd(String passwd) {
        this.setConfValue(PRAM_PASSWD, passwd);
    }

    public String getFilterStr() {
        return this.getConfValue(PRAM_FILTER_STR);
    }

    public void setFilterStr(String filterStr) {
        this.setConfValue(PRAM_FILTER_STR, filterStr);
    }

    public String getUrlEncoding() {
        return this.getConfValue(PRAM_URL_ENCODING);
    }

    public void setUrlEncoding(String urlEncoding) {
        this.setConfValue(PRAM_URL_ENCODING, urlEncoding);
    }

    public String getResponseEncoding() {
        return this.getConfValue(PRAM_RESPONSE_ENCODING);
    }

    public void setResponseEncoding(String responseEncoding) {
        this.setConfValue(PRAM_RESPONSE_ENCODING, responseEncoding);
    }

    private String getConfValue(String key) {
        return this.getConf().getProperty(key);
    }

    private void setConfValue(String key, String value) {
        this.getConf().setProperty(key, value);
    }
}