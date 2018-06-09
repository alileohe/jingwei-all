package com.taobao.jingwei.core.loader;

import com.taobao.jingwei.common.config.impl.zk.ZkConfigManager;
import com.taobao.jingwei.core.kernel.JingWeiCore;

public class DynaTestLoader {

	public static void main(String[] args) throws Exception {
		String taskName = "QIHAO-DYAN-TEST";

		ZkConfigManager zkConfigManager = new ZkConfigManager();
		zkConfigManager.init();

		JingWeiCore jingWeiCore = new JingWeiCore();
		jingWeiCore.setTaskName(taskName);
		jingWeiCore.setApplier(new SimpleApplier());
		jingWeiCore.setConfigManager(zkConfigManager);
		jingWeiCore.init();
	}
}
