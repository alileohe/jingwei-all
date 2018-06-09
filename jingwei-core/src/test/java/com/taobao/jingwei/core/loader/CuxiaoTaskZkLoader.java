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

public class CuxiaoTaskZkLoader {

	private static final String TASK_NAME = "daily-cu-syncserver";

	public static void main(String[] args) throws Exception {

		ZkConfig zkConfig = new ZkConfig();
		zkConfig.setZkSessionTimeoutMs(15000);
		ZkConfigManager zkConfigManager = new ZkConfigManager();
		zkConfigManager.setZkConfig(zkConfig);
		zkConfigManager.init();

		//´´½¨jingweCore
		JingWeiCore jingWeiCore = new JingWeiCore();

		// ^(jingwei_test_){1}[0-9]{2}$ 000001:0000000000026248;0
		//System.setProperty(MonitorImpl.DBSYNC_POSITION, "mysql-bin.000001:0000000000026248");

		jingWeiCore.setConfigManager(zkConfigManager);
		jingWeiCore.setTaskName(TASK_NAME);
		jingWeiCore.setApplier(new SimpleApplier());

		jingWeiCore.init();
		System.out.println("============jingWeiCore Start============");
	}
}
