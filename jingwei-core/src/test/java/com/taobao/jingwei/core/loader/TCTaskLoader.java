package com.taobao.jingwei.core.loader;

/**
 * @desc 
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Apr 1, 2012 3:18:50 PM
 */

import com.taobao.jingwei.common.config.impl.zk.ZkConfig;
import com.taobao.jingwei.common.config.impl.zk.ZkConfigManager;
import com.taobao.jingwei.common.node.extractor.BinLogExtractorNode;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode;
import com.taobao.jingwei.common.node.type.ApplierType;
import com.taobao.jingwei.common.node.type.ExtractorType;
import com.taobao.jingwei.core.kernel.JingWeiCore;

import java.io.InputStream;

public class TCTaskLoader {

	private static final String TASK_NAME = "tc-loader-test";

	private static final String DB_PROPER_FILE = "tc-replicator.properties";

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
		InputStream ips = TestLoader.class.getClassLoader().getResourceAsStream(DB_PROPER_FILE);
		BinLogExtractorNode binLogExtractorNode = new BinLogExtractorNode();
		binLogExtractorNode.getConf().load(ips);
		syncTaskNode.setExtractorData(binLogExtractorNode.toJSONString());

		//��ʼ��ZK
		ZkConfig zkConfig = new ZkConfig("10.232.37.114:2181,10.232.37.112:2181,10.232.102.191:2181");
		// xing neng
		//ZkConfig zkConfig = new ZkConfig("10.232.20.133:2181,10.232.24.33:2181,10.232.24.83:2181");
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
