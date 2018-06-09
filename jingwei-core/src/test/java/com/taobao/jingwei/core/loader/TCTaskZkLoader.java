package com.taobao.jingwei.core.loader;

/**
 * @desc 
 * 
 * @author Ë·º£ <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Apr 1, 2012 3:18:50 PM
 */

import com.taobao.jingwei.common.config.impl.zk.ZkConfig;
import com.taobao.jingwei.common.config.impl.zk.ZkConfigManager;
import com.taobao.jingwei.core.kernel.JingWeiCore;

public class TCTaskZkLoader {

	private static final String TASK_NAME = "nihao-tc";
	//private static final String ZK = "10.232.102.188:2181,10.232.102.189:2181,10.232.102.190:2181";

	public static void main(String[] args) throws Exception {

		ZkConfig zkConfig = new ZkConfig();
		zkConfig.setZkSessionTimeoutMs(5000);
		ZkConfigManager zkConfigManager = new ZkConfigManager();
		zkConfigManager.setZkConfig(zkConfig);
		zkConfigManager.init();

		//´´½¨jingweCore
		JingWeiCore jingWeiCore = new JingWeiCore();

		// ^(jingwei_test_){1}[0-9]{2}$ 000001:0000000000026248;0
		//System.setProperty(MonitorImpl.DBSYNC_POSITION, "mysql-bin.001390:0000000000000000");

		jingWeiCore.setConfigManager(zkConfigManager);
		jingWeiCore.setTaskName(TASK_NAME);
		jingWeiCore.setApplier(new SimpleApplier());

		jingWeiCore.init();
		System.out.println("============jingWeiCore Start============");
	}
}
