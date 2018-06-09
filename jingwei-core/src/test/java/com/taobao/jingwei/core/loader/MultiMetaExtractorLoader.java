package com.taobao.jingwei.core.loader;

import com.taobao.jingwei.common.config.impl.zk.ZkConfig;
import com.taobao.jingwei.common.config.impl.zk.ZkConfigManager;
import com.taobao.jingwei.common.node.extractor.MetaExtractorNode;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode;
import com.taobao.jingwei.common.node.type.ApplierType;
import com.taobao.jingwei.common.node.type.ExtractorType;
import com.taobao.jingwei.core.kernel.JingWeiCore;

/**
 * @desc 
 * 
 * @author Ë·º£ <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Mar 22, 2012 11:04:22 AM
 */

public class MultiMetaExtractorLoader {
	private static final int FETCHT_TIMEOUT_MILLIS = 2000;
	private static final int MAX_DELAY_FETCHT_TIMEOUT_MILLIS = 2000;
	private static final int MAX_MESSAGE_SIZE = 512 * 1024;
	//private static final String META_TOPIC = "DAILY-IC-AUCTIONS";
	private static final String META_TOPIC = "DAILY-TC-BIZ-ORDER-TOPIC";
	//private static final String META_TOPIC = "PERF-IC-AUCTIONS";
	private static final String NAME = "MULTI_META_EXTRACTOR_TEST";
	private static final int FETCH_RUNNABLE_COUNT = 1;
	private static final String MUTIL_META_GROUP_TEST = "SHUOHAI_1";

	// daily
	private static String ZK_HOSTS = "10.232.37.114:2181,10.232.37.112:2181,10.232.102.191:2181";

	public static void main(String[] args) throws Exception {
		MetaExtractorNode metaExtractorNode = new MetaExtractorNode();

		metaExtractorNode.setFetchTimeoutInMills(FETCHT_TIMEOUT_MILLIS);
		metaExtractorNode.setMaxDelayFetchTimeInMills(MAX_DELAY_FETCHT_TIMEOUT_MILLIS);
		metaExtractorNode.setMaxMessageSize(MAX_MESSAGE_SIZE);
		metaExtractorNode.setMetaTopic(META_TOPIC);
		metaExtractorNode.setName(NAME);
		metaExtractorNode.setFetchRunnerCount(FETCH_RUNNABLE_COUNT);
		metaExtractorNode.setMetaGroup(MUTIL_META_GROUP_TEST);
		metaExtractorNode.setMetaZkHosts(ZK_HOSTS);

		//MetaExtractor metaExtractor = new MetaExtractor(metaExtractorNode);

		//		//×¼±¸SyncTaskNode
		SyncTaskNode syncTaskNode = new SyncTaskNode();
		syncTaskNode.setName(NAME);
		syncTaskNode.setTaskInstanceCount(1);
		syncTaskNode.setUseLastPosition(false);
		syncTaskNode.setExtractorType(ExtractorType.META_EXTRACTOR);
		syncTaskNode.setApplierType(ApplierType.CUSTOM_APPLIER);

		syncTaskNode.setExtractorData(metaExtractorNode.toJSONString());
		syncTaskNode.setUseLastPosition(true);

		ZkConfig zkConfig = new ZkConfig(ZK_HOSTS);
		zkConfig.setZkSessionTimeoutMs(15000);
		zkConfig.setZkConnectionTimeoutMs(15000);
		ZkConfigManager zkConfigManager = new ZkConfigManager();
		zkConfigManager.setZkConfig(zkConfig);
		zkConfigManager.init();

		JingWeiCore jingWeiCore = new JingWeiCore();
		jingWeiCore.setApplier(new SimpleApplier());
		jingWeiCore.setConfigManager(zkConfigManager);
		jingWeiCore.setSyncTaskNode(syncTaskNode);
		jingWeiCore.init();
		System.out.println("============jingWeiCore Start============");
	}
}
