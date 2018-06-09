package com.taobao.jingwei.webconsole.model.config.extractor;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.node.extractor.AbstractExtractorNode;
import com.taobao.jingwei.common.node.extractor.MetaExtractorNode;
import com.taobao.jingwei.common.node.type.ExtractorType;

/**
 * @desc 
 * 
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Dec 19, 2012 6:06:45 PM
 */

public class MetaExtractorConfig extends ExtractorConfig {

	private static final long serialVersionUID = -2137122139792636525L;

	private String metaTopic;

	private String metaGroup;

	private long fetchTimeoutInMills = 1000;

	private long maxDelayFetchTimeInMills = 100;

	private int maxMessageSize = 6 * 1024;

	private int fetchRunnerCount = Runtime.getRuntime().availableProcessors();

	private String metaZkHosts = StringUtil.EMPTY_STRING;

	private int metaZkConnectionTimeout = 10000;

	private int metaZkSessionTimeoutMs = 15000;

	public String getMetaTopic() {
		return metaTopic;
	}

	public void setMetaTopic(String metaTopic) {
		this.metaTopic = metaTopic;
	}

	public String getMetaGroup() {
		return metaGroup;
	}

	public void setMetaGroup(String metaGroup) {
		this.metaGroup = metaGroup;
	}

	public long getFetchTimeoutInMills() {
		return fetchTimeoutInMills;
	}

	public void setFetchTimeoutInMills(long fetchTimeoutInMills) {
		this.fetchTimeoutInMills = fetchTimeoutInMills;
	}

	public long getMaxDelayFetchTimeInMills() {
		return maxDelayFetchTimeInMills;
	}

	public void setMaxDelayFetchTimeInMills(long maxDelayFetchTimeInMills) {
		this.maxDelayFetchTimeInMills = maxDelayFetchTimeInMills;
	}

	public int getMaxMessageSize() {
		return maxMessageSize;
	}

	public void setMaxMessageSize(int maxMessageSize) {
		this.maxMessageSize = maxMessageSize;
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

	@Override
	/**
	 * MetaExtractor的Config转换成Node
	 * @param metaExtractorConfig
	 * @return
	 */
	public AbstractExtractorNode getExtarctorNode() {

		MetaExtractorNode node = new MetaExtractorNode();

		node.setFetchRunnerCount(this.getFetchRunnerCount());
		node.setFetchTimeoutInMills(this.getFetchTimeoutInMills());
		node.setMaxDelayFetchTimeInMills(this.getMaxDelayFetchTimeInMills());
		node.setMaxMessageSize(this.getMaxMessageSize());
		node.setMetaGroup(this.getMetaGroup());
		node.setMetaTopic(this.getMetaTopic());
		node.setMetaZkConnectionTimeout(this.getMetaZkConnectionTimeout());
		node.setMetaZkSessionTimeoutMs(this.getMetaZkSessionTimeoutMs());
		node.setMetaZkHosts(this.getMetaZkHosts());
		node.setName(this.getName());

		return node;
	}

	@Override
	public ExtractorType getExtractorType() {

		return ExtractorType.META_EXTRACTOR;
	}
}
