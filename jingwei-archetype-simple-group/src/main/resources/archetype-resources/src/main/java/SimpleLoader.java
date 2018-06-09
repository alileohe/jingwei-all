package $package;

import com.taobao.jingwei.common.config.impl.zk.ZkConfig;
import com.taobao.jingwei.common.config.impl.zk.ZkConfigManager;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode;
import com.taobao.jingwei.server.core.ServerTaskCore;

/**   
 * <p>description:<p> 
 * 如果要启用该loader请将JINGWEI.MC中的Main-Class: 设置成该类的完整类名
 * 例如：Main-Class: xxx.SimpleLoader
 *
 * @{#} SimpleLoader.java Create on Dec 31, 2011 3:34:30 PM   
 *   
 * Copyright (c) 2011 by qihao.
 *
 *@author <a href="mailto:qihao@taobao.com">qihao</a> 
 *@version 1.0   
 */
public class SimpleLoader {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
        // 本地调试使用
        // String taskName = ${artifactId}

		String taskName = getTaskName();

		//创建jingweCore
		ServerTaskCore jingWeiCore = new ServerTaskCore();

		//准备SyncTaskNode
		SyncTaskNode syncTaskNode = new SyncTaskNode();
		syncTaskNode.setName(taskName);

		//是否是单例任务
		syncTaskNode.setTaskInstanceCount(1);
		jingWeiCore.setSyncTaskNode(syncTaskNode);

		//初始化ZK,如果ZK使用不是默认精卫集群需要单独设置
		ZkConfig zkConfig = new ZkConfig("10.232.37.114:2181,10.232.37.112:2181,10.232.102.191:2181");
		zkConfig.setZkSessionTimeoutMs(5000);
		ZkConfigManager zkConfigManager = new ZkConfigManager();
		zkConfigManager.setZkConfig(zkConfig);
		zkConfigManager.init();

		//设置zkConfigManager
		jingWeiCore.setConfigManager(zkConfigManager);
		/**
		 * 设置Extractor也就是数据生产者
		 * 如果使用内置的Extractor需要设置
		 * syncTaskNode.setExtractorType();
		 */
		jingWeiCore.setExtractor(new SimpleExtractor());
		/**
		 * 设置applier，这里指定自定义的applier
		 * 如果使用内置的applier需要设置
		 * syncTaskNode.setApplierType();
		 */
		jingWeiCore.setApplier(new SimpleApplier());

		jingWeiCore.init();
	}

	private static String getTaskName() {
		String bootArgString = JingWeiUtil.getArgString();
        Map<String, String> bootArgs = JingWeiUtil.handleArgs(bootArgString);
        String taskName = bootArgs.get("taskName");
		return taskName;
	}
}
