package com.taobao.jingwei.core.internal.extractor;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.node.extractor.MetaExtractorNode;
import com.taobao.tddl.dbsync.extractor.Extractor;
import com.taobao.tddl.dbsync.extractor.ExtractorException;
import com.taobao.tddl.dbsync.extractor.Transferer;
import com.taobao.tddl.dbsync.plugin.AbstractPlugin;
import com.taobao.tddl.dbsync.plugin.PluginContext;
import com.taobao.tddl.dbsync.plugin.PluginException;

import java.util.HashSet;
import java.util.Set;

/**   
 * <p>description:包装过的精卫metaExtractor<p> 
 *
 * @{#} MetaExtractor.java Create on Jan 25, 2012 7:57:30 AM   
 *   
 * Copyright (c) 2012 by qihao.
 *
 *@author <a href="mailto:qihao@taobao.com">qihao</a> 
 *@version 1.0   
 */
public class MetaExtractor extends AbstractPlugin implements Extractor {

	private final com.taobao.tddl.dbsync.meta.MetaExtractor metaExtractor = new com.taobao.tddl.dbsync.meta.MetaExtractor();

	private MetaExtractorNode metaExtractorNode;

	private boolean useLastPosition = true;

	public MetaExtractor(MetaExtractorNode metaExtractorNode, boolean useLastPosition) {
		this.metaExtractorNode = metaExtractorNode;
		this.useLastPosition = useLastPosition;
	}

	@Override
	public void init(String name, PluginContext context) throws PluginException, InterruptedException {
		String[] topics = StringUtil.split(this.metaExtractorNode.getMetaTopic(), ",");
		Set<String> topicSet = new HashSet<String>(topics.length);
		for (String topic : topics) {
			if (StringUtil.isNotBlank(topic)) {
				topicSet.add(StringUtil.trim(topic));
			}
		}
		this.metaExtractor.setTopics(topicSet);
		this.metaExtractor.setGroup(this.metaExtractorNode.getMetaGroup());
		this.metaExtractor.setMaxDelayFetchTimeInMills(this.metaExtractorNode.getMaxDelayFetchTimeInMills());
		this.metaExtractor.setMaxMessageSize(this.metaExtractorNode.getMaxMessageSize());
		this.metaExtractor.setFetchTimeoutInMills(this.metaExtractorNode.getFetchTimeoutInMills());
		this.metaExtractor.setMaxFetchRetries(Integer.MAX_VALUE);
		this.metaExtractor.setFetchRunnerCount(this.metaExtractorNode.getFetchRunnerCount());
		this.metaExtractor.setUseLastPosition(this.useLastPosition);
		if (StringUtil.isNotBlank(this.metaExtractorNode.getMetaZkHosts())) {
			this.metaExtractor.setZkConnect(this.metaExtractorNode.getMetaZkHosts());
		}
		this.metaExtractor.setZkConnectionTimeoutMs(this.metaExtractorNode.getMetaZkConnectionTimeout());
		this.metaExtractor.setZkSessionTimeoutMs(this.metaExtractorNode.getMetaZkSessionTimeoutMs());
		this.metaExtractor.init(name, context);
	}

	public void destory() throws PluginException, InterruptedException {
		this.metaExtractor.destory();
	}

	@Override
	public void extract(Transferer transferer) throws ExtractorException, InterruptedException {
		this.metaExtractor.extract(transferer);
	}
}