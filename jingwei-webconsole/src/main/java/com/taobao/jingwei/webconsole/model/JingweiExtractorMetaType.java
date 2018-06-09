package com.taobao.jingwei.webconsole.model;

import com.taobao.jingwei.common.node.extractor.MetaExtractorNode;

public class JingweiExtractorMetaType {
	private String extMetaTopic;
	private String extMetaGroup;
	private long extFetchTimeoutInMills;
	private long extMaxDelayFetchTimeInMills;
	private int extMaxMessageSize;
	private int extFetchRunnerCount;
	private String extMetaZkHosts;
	private int extMetaZkConnectionTimeout;
	private int extMetaZkSessionTimeoutMs;

	public MetaExtractorNode getMetaExtractorNode() {
		MetaExtractorNode node = new MetaExtractorNode();
		node.setMetaTopic(this.extMetaTopic);
		node.setMetaGroup(this.extMetaGroup);
		node.setFetchTimeoutInMills(this.extFetchTimeoutInMills);
		node.setMaxDelayFetchTimeInMills(this.extMaxDelayFetchTimeInMills);
		node.setMaxMessageSize(this.extMaxMessageSize);
		node.setFetchRunnerCount(this.extFetchRunnerCount);
		node.setMetaZkHosts(this.extMetaZkHosts);
		node.setMetaZkConnectionTimeout(this.extMetaZkConnectionTimeout);
		node.setMetaZkSessionTimeoutMs(this.extMetaZkSessionTimeoutMs);
		return node;
	}

	public String getExtMetaTopic() {
		return extMetaTopic;
	}

	public void setExtMetaTopic(String extMetaTopic) {
		this.extMetaTopic = extMetaTopic;
	}

	public String getExtMetaGroup() {
		return extMetaGroup;
	}

	public void setExtMetaGroup(String extMetaGroup) {
		this.extMetaGroup = extMetaGroup;
	}

	public long getExtFetchTimeoutInMills() {
		return extFetchTimeoutInMills;
	}

	public void setExtFetchTimeoutInMills(long extFetchTimeoutInMills) {
		this.extFetchTimeoutInMills = extFetchTimeoutInMills;
	}

	public long getExtMaxDelayFetchTimeInMills() {
		return extMaxDelayFetchTimeInMills;
	}

	public void setExtMaxDelayFetchTimeInMills(long extMaxDelayFetchTimeInMills) {
		this.extMaxDelayFetchTimeInMills = extMaxDelayFetchTimeInMills;
	}

	public int getExtMaxMessageSize() {
		return extMaxMessageSize;
	}

	public void setExtMaxMessageSize(int extMaxMessageSize) {
		this.extMaxMessageSize = extMaxMessageSize;
	}

	public int getExtFetchRunnerCount() {
		return extFetchRunnerCount;
	}

	public void setExtFetchRunnerCount(int extFetchRunnerCount) {
		this.extFetchRunnerCount = extFetchRunnerCount;
	}

	public String getExtMetaZkHosts() {
		return extMetaZkHosts;
	}

	public void setExtMetaZkHosts(String extMetaZkHosts) {
		this.extMetaZkHosts = extMetaZkHosts;
	}

	public int getExtMetaZkConnectionTimeout() {
		return extMetaZkConnectionTimeout;
	}

	public void setExtMetaZkConnectionTimeout(int extMetaZkConnectionTimeout) {
		this.extMetaZkConnectionTimeout = extMetaZkConnectionTimeout;
	}

	public int getExtMetaZkSessionTimeoutMs() {
		return extMetaZkSessionTimeoutMs;
	}

	public void setExtMetaZkSessionTimeoutMs(int extMetaZkSessionTimeoutMs) {
		this.extMetaZkSessionTimeoutMs = extMetaZkSessionTimeoutMs;
	}

}
