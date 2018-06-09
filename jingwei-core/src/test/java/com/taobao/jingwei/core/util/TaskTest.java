package com.taobao.jingwei.core.util;

import com.taobao.jingwei.core.kernel.JingWeiCore;
import com.taobao.jingwei.core.loader.SimpleApplier;

public class TaskTest {
	// private static String TASK_NAME = "DEV-TAO-ALBUM";
	// private static String TASK_NAME = "diamond-daily2second";
	private static String TASK_NAME = "INVENTORY-TO-META";

	//	private static String TASK_NAME = "ark";

	// private static String TASK_NAME = "ICBU_RFQ-TO-META";
	// private static String TASK_NAoE = "brandku-std_property-and-value";

	// private static String TASK_NAME = "ark-from-meta";
	// private static String TASK_NAME = "TM-WLB-ROLLBACK";
	//private static String TASK_NAME = "mic_comb_rel-to-camp";

	// daily
	// private static final String ZK =
	// "10.232.102.188:2181,10.232.102.189:2181,10.232.102.190:2181";

	public static void main(String[] args) throws Exception {

		// 005724:0340180970 005724:0340183703 005724:0000000340183703;0
		// System.setProperty(MonitorImpl.DBSYNC_POSITION, "001431:160840957#15212444");

		JingWeiCore jingWeiCore = new JingWeiCore();

		jingWeiCore.setTaskName(TASK_NAME);
		jingWeiCore.setApplier(new SimpleApplier());

		jingWeiCore.init();
		System.out.println("============jingWeiCore Start============");
	}
}
