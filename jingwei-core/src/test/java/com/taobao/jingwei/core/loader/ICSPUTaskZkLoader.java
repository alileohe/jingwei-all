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

public class ICSPUTaskZkLoader {

	private static final String TASK_NAME = "VSEARCH-SPU";

	public static void main(String[] args) throws Exception {

		ZkConfig zkConfig = new ZkConfig();
		zkConfig.setZkSessionTimeoutMs(5000);
		ZkConfigManager zkConfigManager = new ZkConfigManager();
		zkConfigManager.setZkConfig(zkConfig);
		zkConfigManager.init();

		//´´½¨jingweCore
		JingWeiCore jingWeiCore = new JingWeiCore();

		jingWeiCore.setConfigManager(zkConfigManager);
		jingWeiCore.setTaskName(TASK_NAME);
		jingWeiCore.setApplier(new SimpleApplier());

		jingWeiCore.init();
		System.out.println("============jingWeiCore Start============");
	}
}
