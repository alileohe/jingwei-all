package com.taobao.jingwei.common.node.extractor;

import com.alibaba.common.lang.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class MetaExtractorNode
 * <p/>
 * MetaExtractorNode �����ô洢�ڵ���
 *
 * @author qihao <dqylyln@gmail.com>
 * @since 11-11-15
 */
public class MetaExtractorNode extends AbstractExtractorNode {

	/**
	 * META��������
	 */
	private String metaTopic;

	private String metaGroup;

	private long fetchTimeoutInMills = 1000;

	private long maxDelayFetchTimeInMills = 100;

	private int maxMessageSize = 6 * 1024;

	private int fetchRunnerCount = Runtime.getRuntime().availableProcessors();

	private String metaZkHosts = StringUtil.EMPTY_STRING;

	private int metaZkConnectionTimeout = 10000;

	private int metaZkSessionTimeoutMs = 15000;
	/**
	 * json�洢key����
	 */
	private final static String META_TOPIC_KEY = "metaTopic";
	private final static String META_GROUP_KEY = "metaGroup";
	private final static String FETCH_TIMEOUT_IN_MILLS = "fetchTimeoutInMills";
	private final static String MAX_DELAY_FETCH_TIME_IN_MILLS = "maxDelayFetchTimeInMills";
	private final static String MAX_MESSAGE_SIZE = "maxMessageSize";
	private final static String FETCH_RUNNER_COUNT = "fetchRunnerCount";

	private final static String META_ZK_HOSTS = "metaZkHosts";
	private final static String META_ZK_CONNECTION_TIMEOUT = "metaZkConnectionTimeout";
	private final static String META_ZK_SESSION_TIMEOUTMS = "metaZkSessionTimeoutMs";

	@Override
	protected void specilizeAttributeToJsonObject(JSONObject jsonObject) throws JSONException {
		jsonObject.put(META_TOPIC_KEY, StringUtil.defaultIfBlank(this.getMetaTopic()));
		jsonObject.put(META_GROUP_KEY, StringUtil.defaultIfBlank(this.getMetaGroup()));
		jsonObject.put(FETCH_TIMEOUT_IN_MILLS, this.getFetchTimeoutInMills());
		jsonObject.put(MAX_DELAY_FETCH_TIME_IN_MILLS, this.getMaxDelayFetchTimeInMills());
		jsonObject.put(MAX_MESSAGE_SIZE, this.getMaxMessageSize());
		jsonObject.put(FETCH_RUNNER_COUNT, this.getFetchRunnerCount());
		jsonObject.put(META_ZK_HOSTS, this.getMetaZkHosts());
		jsonObject.put(META_ZK_CONNECTION_TIMEOUT, this.getMetaZkConnectionTimeout());
		jsonObject.put(META_ZK_SESSION_TIMEOUTMS, this.getMetaZkSessionTimeoutMs());
	}

	@Override
	protected void jsonObjectToSpecilizeAttribute(JSONObject jsonObject) throws JSONException {
		this.setMetaTopic(jsonObject.getString(META_TOPIC_KEY));
		this.setMetaGroup(jsonObject.getString(META_GROUP_KEY));
		this.setFetchTimeoutInMills(jsonObject.getLong(FETCH_TIMEOUT_IN_MILLS));
		this.setMaxDelayFetchTimeInMills(jsonObject.getLong(MAX_DELAY_FETCH_TIME_IN_MILLS));
		this.setMaxMessageSize(jsonObject.getInt(MAX_MESSAGE_SIZE));
		this.setFetchRunnerCount(jsonObject.getInt(FETCH_RUNNER_COUNT));
		if (jsonObject.has(META_ZK_HOSTS)) {
			this.setMetaZkHosts(jsonObject.getString(META_ZK_HOSTS));
		}
		if (jsonObject.has(META_ZK_CONNECTION_TIMEOUT)) {
			this.setMetaZkConnectionTimeout(jsonObject.getInt(META_ZK_CONNECTION_TIMEOUT));
		}
		if (jsonObject.has(META_ZK_SESSION_TIMEOUTMS)) {
			this.setMetaZkSessionTimeoutMs(jsonObject.getInt(META_ZK_SESSION_TIMEOUTMS));
		}
	}

	public void setMetaTopic(String metaTopic) {
		this.metaTopic = metaTopic;
	}

	public void setMetaGroup(String metaGroup) {
		this.metaGroup = metaGroup;
	}

	public void setFetchTimeoutInMills(long fetchTimeoutInMills) {
		this.fetchTimeoutInMills = fetchTimeoutInMills;
	}

	public void setMaxMessageSize(int maxMessageSize) {
		this.maxMessageSize = maxMessageSize;
	}

	public String getMetaTopic() {
		return metaTopic;
	}

	public String getMetaGroup() {
		return metaGroup;
	}

	public long getFetchTimeoutInMills() {
		return fetchTimeoutInMills;
	}

	public int getMaxMessageSize() {
		return maxMessageSize;
	}

	public long getMaxDelayFetchTimeInMills() {
		return maxDelayFetchTimeInMills;
	}

	public void setMaxDelayFetchTimeInMills(long maxDelayFetchTimeInMills) {
		this.maxDelayFetchTimeInMills = maxDelayFetchTimeInMills;
	}

	public int getFetchRunnerCount() {
		return fetchRunnerCount;
	}

	public void setFetchRunnerCount(int fetchRunnerCount) {
		this.fetchRunnerCount = fetchRunnerCount;
	}

	public String getMetaZkHosts() {
		return metaZkHosts;
	}

	public void setMetaZkHosts(String metaZkHosts) {
		this.metaZkHosts = metaZkHosts;
	}

	public int getMetaZkConnectionTimeout() {
		return metaZkConnectionTimeout;
	}

	public void setMetaZkConnectionTimeout(int metaZkConnectionTimeout) {
		this.metaZkConnectionTimeout = metaZkConnectionTimeout;
	}

	public int getMetaZkSessionTimeoutMs() {
		return metaZkSessionTimeoutMs;
	}

	public void setMetaZkSessionTimeoutMs(int metaZkSessionTimeoutMs) {
		this.metaZkSessionTimeoutMs = metaZkSessionTimeoutMs;
	}
}
