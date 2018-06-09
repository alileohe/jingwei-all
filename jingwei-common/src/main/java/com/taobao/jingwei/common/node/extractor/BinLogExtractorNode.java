package com.taobao.jingwei.common.node.extractor;

import com.alibaba.common.lang.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Class BinLogExtractorNode
 *
 * @author qihao <dqylyln@gmail.com>
 * @since 11-11-15
 */
public class BinLogExtractorNode extends AbstractExtractorNode {

	private final Properties conf = new Properties();

	private volatile boolean autoSwitch;

	private volatile String switchPolicy;

	private volatile String groupName;
	/**
	 * json�洢����key����
	 */
	private final static String EXTRACTOR_DATA = "extractorData";
	private final static String AUTO_SWITCH_KEY = "autoSwitch";
	private final static String SWITCH_POLICY_KEY = "switchPolicy";
	private final static String GROUP_NAME_KEY = "groupName";

	public BinLogExtractorNode() {

	}

	public BinLogExtractorNode(String propsData) {
		try {
			this.conf.load(new StringReader(propsData));
		} catch (IOException e) {
			logger.error("load propStr Create BinLogExtractorNode Error", e);
		}
	}

	public BinLogExtractorNode(File propFile) {
		if (null != propFile) {
			FileInputStream ips = null;
			try {
				ips = new FileInputStream(propFile);
				this.getConf().clear();
				this.getConf().load(ips);
			} catch (IOException e) {
				logger.error("load propFile Create BinLogExtractorNode Error", e);
			} finally {
				if (null != ips) {
					try {
						ips.close();
					} catch (IOException e) {
						logger.error("load propFile Close FileInputStream Error", e);
					}
				}
			}
		}
	}

	/**
	 * @param jsonObject
	 * @throws JSONException
	 */
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
		jsonObject.put(AUTO_SWITCH_KEY, this.isAutoSwitch());
		jsonObject.put(GROUP_NAME_KEY, this.getGroupName());
		jsonObject.put(SWITCH_POLICY_KEY, this.getSwitchPolicy());

	}

	/**
	 * @param jsonObject
	 * @throws JSONException
	 */
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
		if (jsonObject.has(AUTO_SWITCH_KEY)) {
			this.setAutoSwitch(jsonObject.getBoolean(AUTO_SWITCH_KEY));
		}
		if (jsonObject.has(SWITCH_POLICY_KEY)) {
			this.setSwitchPolicy(jsonObject.getString(SWITCH_POLICY_KEY));
		}
		if (jsonObject.has(GROUP_NAME_KEY)) {
			this.setGroupName(jsonObject.getString(GROUP_NAME_KEY));
		}
	}

	public Properties getConf() {
		return conf;
	}

	public boolean isAutoSwitch() {
		return autoSwitch;
	}

	public void setAutoSwitch(boolean autoSwitch) {
		this.autoSwitch = autoSwitch;
	}

	public String getSwitchPolicy() {
		return switchPolicy;
	}

	public void setSwitchPolicy(String switchPolicy) {
		this.switchPolicy = switchPolicy;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
}
