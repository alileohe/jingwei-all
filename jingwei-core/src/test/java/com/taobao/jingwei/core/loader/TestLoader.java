package com.taobao.jingwei.core.loader;

import com.taobao.jingwei.common.config.impl.zk.ZkConfig;
import com.taobao.jingwei.common.config.impl.zk.ZkConfigManager;
import com.taobao.jingwei.common.node.extractor.BinLogExtractorNode;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode;
import com.taobao.jingwei.common.node.type.ApplierType;
import com.taobao.jingwei.common.node.type.ExtractorType;
import com.taobao.jingwei.core.kernel.JingWeiCore;

import java.io.InputStream;

public class TestLoader {

	private static String TASK_NAME = "nihao";

	private static String ZK_HOSTS = "10.232.37.114:2181,10.232.37.112:2181,10.232.102.191:2181";

	public static void main(String[] args) throws Exception {
		//
		//		//׼��SyncTaskNode
		SyncTaskNode syncTaskNode = new SyncTaskNode();
		syncTaskNode.setName(TASK_NAME);
		syncTaskNode.setTaskInstanceCount(1);
		syncTaskNode.setExtractorType(ExtractorType.BINLOG_EXTRACTOR);
		syncTaskNode.setApplierType(ApplierType.CUSTOM_APPLIER);
		syncTaskNode.setUseLastPosition(false);

		//����replicator.properties
//		Thread.sleep(5000);
		//System.setProperty(MonitorImpl.DBSYNC_POSITION, "mysql-bin.000463:0000000000000004");
		InputStream ips = TestLoader.class.getClassLoader().getResourceAsStream("udc-uic-replicator.properties");
		BinLogExtractorNode binLogExtractorNode = new BinLogExtractorNode();
		binLogExtractorNode.getConf().load(ips);
		syncTaskNode.setExtractorData(binLogExtractorNode.toJSONString());

		ZkConfig zkConfig = new ZkConfig(ZK_HOSTS);
		zkConfig.setZkSessionTimeoutMs(5000);
		ZkConfigManager zkConfigManager = new ZkConfigManager();
		zkConfigManager.setZkConfig(zkConfig);
		zkConfigManager.init();
		//��SyncTaskNode������zk��
		//zkConfigManager.publishOrUpdatePersistentData(syncTaskNode.getDataIdOrNodePath(), syncTaskNode.toJSONString());

		//����jingweCore
		JingWeiCore jingWeiCore = new JingWeiCore();
		jingWeiCore.setApplier(new SimpleApplier());
		jingWeiCore.setConfigManager(zkConfigManager);
		//jingWeiCore.setTaskName(syncTaskNode.getName());
		jingWeiCore.setSyncTaskNode(syncTaskNode);
		jingWeiCore.init();
		System.out.println("============jingWeiCore Start============");
	}
}
