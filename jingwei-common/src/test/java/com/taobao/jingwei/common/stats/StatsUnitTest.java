package com.taobao.jingwei.common.stats;

import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.config.impl.zk.ZkConfig;
import com.taobao.jingwei.common.config.impl.zk.ZkConfigManager;
import com.taobao.jingwei.common.log.JingweiLogConfig;
import com.taobao.jingwei.common.node.OperateNode;
import com.taobao.jingwei.common.node.OperateNode.OperateEnum;
import com.taobao.jingwei.common.node.StatusNode;
import com.taobao.jingwei.common.node.StatusNode.StatusEnum;
import com.taobao.util.RandomUtil;
import org.apache.log4j.Level;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @desc
 * @author <a href="mailto:shuohai.lhl@taobao.com">shuohailhl</a>
 * @date 2011-11-14����2:58:51
 */
public class StatsUnitTest {
	private String confPath = "d:\\jingwei-wss\\jingwei\\jingwei-monitor\\src\\main\\conf\\monitor.ini";
	private static String TASK_NAME = "BUILDIN_TASK_TEST";
	private static String SERVER_NAME = "v035114.sqa.cm4";
	private static ZkConfigManager zkConfigManager = new ZkConfigManager();
	final static StatsUnit unit = new StatsUnit(TASK_NAME, zkConfigManager);
	static volatile AtomicLong count = new AtomicLong(0L);

	final Semaphore startLatch = new Semaphore(2);

	//	CyclicBarrier barrier;

	@Before
	public void init() {
		// ��ʼ��ZK������
		ZkConfig zkConfig = ZkConfig.getZkConfigFromFile(confPath);

		zkConfigManager.setZkConfig(zkConfig);
		zkConfigManager.init();

		// ��ʼ����־
		JingweiLogConfig log = new JingweiLogConfig();
		log.setFileLogEncoding("UTF-8");
		log.setLogLevel(Level.INFO);
		log.setLogPath("../log");
		log.setMaxLogFileSize("10MB");
		log.setPatternLogLayout("%m%n");

		unit.setHostName(SERVER_NAME);
		unit.setLogConfig(log);
		unit.init();
	}

	/**
	 * ��ͳ������д��ZK�ڵ�
	 */
	@Test
	@Ignore
	public void testWriteWoZk() {

		Thread t = new Thread() {
			long i = 1;

			public void run() {
				while (true) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					// ����ͳ������
					unit.incrementDeleteCount();
					unit.incrementDeleteExceptionCount();
					unit.incrementInsertCount();
					unit.incrementInsertExceptionCount();
					unit.incrementUpdateCount();
					unit.incrementUpdateExceptionCount();

					// �ӳ�ͳ������
					unit.addDeleteDelay(5);
					unit.addInsertDelay(5);
					unit.addUpdateDelay(5);
					unit.addExtractorDelay(RandomUtil.getInt(40, 50));

					// ����ͳ������
					unit.getTxStats().setMillisAvgLatency(15L);
					unit.getTxStats().setMillisMinLatency(15L);
					unit.getTxStats().setMillisMaxLatency(15L);
					unit.getTxStats().setTxCount(i++);
					unit.getTxStats().setTxTps(124F);
				}
			};
		};
		t.start();

		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * д/jingwei/tasks/**task/**host/stats/status, ���м�س��򲻱���
	 */
	// @Test
	public void testTaskRunning() {
		StatusNode statusNode = new StatusNode();
		String ownerDataIdOrPath = JingWeiConstants.JINGWEI_TASK_ROOT_PATH + "/" + TASK_NAME + "/" + SERVER_NAME;
		String path = StatusNode.getDataIdOrNodePathByOwner(ownerDataIdOrPath);
		try {
			zkConfigManager.publishOrUpdateData(path, StatusEnum.RUNNING.toString(), statusNode.isPersistent());
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// @Test
	public void testToggleOperate() {
		String dateIdOrPath = JingWeiConstants.JINGWEI_SERVER_ROOT_PATH + "/" + SERVER_NAME + "/" + TASK_NAME + "/"
				+ JingWeiConstants.JINGWEI_OPERATE_NODE_NAME;

		OperateNode opNode = new OperateNode();
		opNode.setOperateEnum(OperateEnum.NODE_START);

		try {
			zkConfigManager.publishOrUpdateData(dateIdOrPath, opNode.toJSONString(), opNode.isPersistent());

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// @Test
	public void testWriteAlarm() {
		String dateIdOrPath = JingWeiConstants.JINGWEI_TASK_ROOT_PATH + "/" + TASK_NAME + "/" + SERVER_NAME + "/"
				+ JingWeiConstants.JINGWEI_SCAN_ALARM_NODE;

		OperateNode opNode = new OperateNode();
		opNode.setOperateEnum(OperateEnum.NODE_START);

		try {
			zkConfigManager.publishOrUpdateData(dateIdOrPath, "Error Message !", true);

			Thread.sleep(10000);

			zkConfigManager.publishOrUpdateData(dateIdOrPath, "Another Error Message !", true);

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ���Բ��������StatUnitЧ��
	 */
	// @Test
	// public void testConcurrent() {
	// barrier = new CyclicBarrier(2, new Thread() {
	// @Override
	// public void run() {
	// barrier.reset();
	// }
	// });
	//
	// final Runnable count1 = new Thread(new Count1());
	// final Runnable count2 = new Thread(new Count2());
	//
	// new Thread(new Runnable() {
	// public void run() {
	//
	// count1.run();
	// count2.run();
	//
	// }
	// }).start();
	//
	// try {
	// Thread.currentThread().join();
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// }
	//
	// }

	// class Count1 implements Runnable {
	//
	// @Override
	// public void run() {
	// for (int i = 0; i < 1000; i++) {
	// count1();
	// }
	//
	// }
	//
	// }
	//
	// class Count2 implements Runnable {
	//
	// @Override
	// public void run() {
	//
	// for (int i = 0; i < 1000; i++) {
	// count2();
	// }
	// }
	//
	// }

	//	private void count1() {
	//		try {
	//			barrier.await();
	//
	//		} catch (InterruptedException e) {
	//			e.printStackTrace();
	//		} catch (BrokenBarrierException e) {
	//			e.printStackTrace();
	//		}
	//		// ����ͳ������
	//		StatsUnitTest.unit.incrementDeleteCount();
	//		StatsUnitTest.unit.incrementDeleteExceptionCount();
	//		StatsUnitTest.unit.incrementInsertCount();
	//		StatsUnitTest.unit.incrementInsertExceptionCount();
	//		StatsUnitTest.unit.incrementUpdateCount();
	//		StatsUnitTest.unit.incrementUpdateExceptionCount();
	//
	//		// �ӳ�ͳ������
	//		StatsUnitTest.unit.addDeleteDelay(5);
	//		StatsUnitTest.unit.addInsertDelay(5);
	//		StatsUnitTest.unit.addUpdateDelay(5);
	//		StatsUnitTest.unit.addExtractorDelay(5);
	//
	//	}

	//	private void count2() {
	//		try {
	//			barrier.await();
	//
	//		} catch (InterruptedException e) {
	//			e.printStackTrace();
	//		} catch (BrokenBarrierException e) {
	//			e.printStackTrace();
	//		}
	//		StatsUnitTest.count.getAndIncrement();
	//		StatsUnitTest.unit.getTxStats().setMillisAvgLatency(
	//				StatsUnitTest.count.get());
	//		StatsUnitTest.unit.getTxStats().setMillisMinLatency(
	//				StatsUnitTest.count.get());
	//		StatsUnitTest.unit.getTxStats().setMillisMaxLatency(
	//				StatsUnitTest.count.get());
	//		StatsUnitTest.unit.getTxStats().setTxCount(StatsUnitTest.count.get());
	//		StatsUnitTest.unit.getTxStats().setTxTps((float) 123456);
	//
	//	}

}
