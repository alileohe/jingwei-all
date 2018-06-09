package com.taobao.jingwei.core.loader;

import com.taobao.jingwei.common.config.impl.zk.ZkConfig;
import com.taobao.jingwei.common.config.impl.zk.ZkConfigManager;
import com.taobao.jingwei.core.kernel.JingWeiCore;
import com.taobao.tddl.dbsync.monitor.MonitorImpl;

public class SnsTestLoader {

	private static String TASK_NAME = "daily-SnsBaskItemConverter";
	// daily
	private static final String ZK = "10.232.102.188:2181,10.232.102.189:2181,10.232.102.190:2181";

	public static void main(String[] args) throws Exception {

		//ZkConfig zkConfig = new ZkConfig("10.232.102.188:2181,10.232.102.189:2181,10.232.102.190:2181");
		ZkConfig zkConfig = new ZkConfig(ZK);
		zkConfig.setZkSessionTimeoutMs(5000);
		ZkConfigManager zkConfigManager = new ZkConfigManager();
		zkConfigManager.setZkConfig(zkConfig);
		zkConfigManager.init();

		System.setProperty(MonitorImpl.DBSYNC_POSITION, "mysql-bin.000477:0000000000000000");

		//´´½¨jingweCore
		JingWeiCore jingWeiCore = new JingWeiCore();

		jingWeiCore.setTaskName(TASK_NAME);
		jingWeiCore.setConfigManager(zkConfigManager);
		jingWeiCore.setApplier(new SimpleApplier());

		jingWeiCore.init();
		System.out.println("============jingWeiCore Start============");
	}
}
