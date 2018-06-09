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
import com.taobao.tddl.dbsync.monitor.MonitorImpl;

public class KRQTaskZkLoader {

	private static final String TASK_NAME = "DAILY-KRQ-MOVIE-TICKET";

	public static void main(String[] args) throws Exception {

		ZkConfig zkConfig = new ZkConfig();
		zkConfig.setZkSessionTimeoutMs(5000);
		ZkConfigManager zkConfigManager = new ZkConfigManager();
		zkConfigManager.setZkConfig(zkConfig);
		zkConfigManager.init();

		//´´½¨jingweCore
		JingWeiCore jingWeiCore = new JingWeiCore();

		System.setProperty(MonitorImpl.DBSYNC_POSITION, "mysql-bin.000405:0000000000460447");
		jingWeiCore.setConfigManager(zkConfigManager);
		jingWeiCore.setTaskName(TASK_NAME);

		jingWeiCore.init();
		System.out.println("============jingWeiCore Start============");
	}
}
