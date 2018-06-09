package com.taobao.jingwei.common.node.extractor;

import com.alibaba.common.lang.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.Properties;

/**
*
<p>
description:oracleExtractor�����ö���
<p>
*
* OracleExtractorNode.java Create on Dec 6, 2012 9:48:51 AM
*
* Copyright (c) 2011 by qihao.
*
*@author
<a href="mailto:qihao@taobao.com">qihao</a>
*@version 1.0
*/
public class OracleExtractorNode extends AbstractExtractorNode {

	private final Properties conf = new Properties();

	private final static String EXTRACTOR_DATA = "extractorData";

	public OracleExtractorNode() {

	}

	public OracleExtractorNode(Properties conf) {
		for (Entry<Object, Object> entry : conf.entrySet()) {
			String key = StringUtil.trim((String) entry.getKey());
			String value = StringUtil.trim((String) entry.getValue());
			this.conf.put(key, value);
		}
	}

	public OracleExtractorNode(String jsonString) {
		try {
			this.jsonStringToNodeSelf(jsonString);
		} catch (JSONException e) {
			logger.error("load json Create BinLogExtractorNode Error", e);
		}
	}

	public OracleExtractorNode(File propFile) {
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
}
