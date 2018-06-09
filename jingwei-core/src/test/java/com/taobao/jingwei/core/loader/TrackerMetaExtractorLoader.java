package com.taobao.jingwei.core.loader;

import com.taobao.jingwei.common.config.impl.zk.ZkConfig;
import com.taobao.jingwei.common.config.impl.zk.ZkConfigManager;
import com.taobao.jingwei.core.kernel.JingWeiCore;

/**
 * @desc 
 * 
 * @author Ë·º£ <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Jul 4, 2012 1:35:17 PM
 */

public class TrackerMetaExtractorLoader {
	private static String TASK_NAME = "DAILY-VSEARCH-AA";

	public static void main(String[] args) throws Exception {
		ZkConfig zkConfig = new ZkConfig();
		zkConfig.setZkSessionTimeoutMs(15000);
		zkConfig.setZkConnectionTimeoutMs(15000);
		ZkConfigManager zkConfigManager = new ZkConfigManager();
		zkConfigManager.setZkConfig(zkConfig);
		zkConfigManager.init();

		JingWeiCore jingWeiCore = new JingWeiCore();
		jingWeiCore.setTaskName(TASK_NAME);
		jingWeiCore.setApplier(new SimpleApplier());
		jingWeiCore.setConfigManager(zkConfigManager);

		jingWeiCore.init();
		System.out.println("============jingWeiCore Start============");
	}
}
