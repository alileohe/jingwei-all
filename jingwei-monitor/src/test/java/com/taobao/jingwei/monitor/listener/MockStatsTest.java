package com.taobao.jingwei.monitor.listener;

import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.config.impl.zk.ZkConfig;
import com.taobao.jingwei.common.config.impl.zk.ZkConfigManager;
import com.taobao.jingwei.common.node.AlarmNode;
import com.taobao.jingwei.common.node.HeartbeatNode;
import com.taobao.jingwei.common.node.StatsNode;
import com.taobao.util.RandomUtil;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;

public class MockStatsTest {
	private String confPath = "f:\\dayu-work\\jingwei\\jingwei-monitor\\src\\main\\conf\\monitor.ini";
	private static String TASK_NAME = "ark";
	//	private static String MONITOR_NAME = "v035114.sqa.cm4";
	private static String SERVER_NAME = "shuohailhl-PC";
	//	private static String WW_USER = "˷��";
	//	private static String SMS_SUER = "15957197210";
	private static ZkConfigManager zkConfigManager = new ZkConfigManager();

	@Before
	public void init() {
		// ��ʼ��ZK������
		ZkConfig zkConfig = ZkConfig.getZkConfigFromFile(confPath);

		zkConfigManager.setZkConfig(zkConfig);
		zkConfigManager.init();
	}

	//	@Test
	public void testStats() {
		// ·��
		StatsNode statsNode = new StatsNode(TASK_NAME, SERVER_NAME);
		String path = statsNode.getDataIdOrNodePath();

		String data = zkConfigManager.getData(path);

		try {
			statsNode.jsonStringToNodeSelf(data);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		int count = 2;
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// ����tps ���� �������� ���� ����
			//			statsNode.setLastPeriodInsertTps((double)count++);

			// ����tps ���� �������� ���� ����
			//			statsNode.setLastPeriodInsertTps(1.0);

			// ɾ��tps ���� �������� ���� ����
			//			statsNode.setLastPeriodDeleteTps((double)count++);

			// ɾ��tps ���� �������� ���� ���� 
			//			statsNode.setLastPeriodDeleteTps(2.0);

			// ����tps ���� �������� ���� ����
			//			statsNode.setLastPeriodUpdateTps((double)count++);

			// ����tps ���� �������� ���� ����
			//			statsNode.setLastPeriodUpdateTps(3.0);

			// �����ӳ� �������� ���� ����
			statsNode.setLastPeriodAvgInsertDelay((double) count++);

			// �޸��ӳ� �������� ���� ����
			statsNode.setLastPeriodAvgUpdateDelay((double) count++);

			// ɾ���ӳ� �������� ���� ����
			statsNode.setLastPeriodAvgDeleteDelay((double) count++);

			// extractor �ӳ� �������� ���� ����
			statsNode.setLastPeriodAvgExtractorDelay((double) count++);

			// ɾ���쳣
			statsNode.setLastPeriodDeleteExceptionCount(count++);

			// �����쳣
			statsNode.setLastPeriodUpdateExceptionCount(count++);

			// �����쳣
			statsNode.setLastPeriodInsertExceptionCount(count++);

			statsNode.setStatsTime(new Date().getTime());

			try {
				zkConfigManager.publishOrUpdateData(path, statsNode.toJSONString(), true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

		@Test
		@Ignore
	public void testAlarm() {
		String path = JingWeiConstants.JINGWEI_TASK_ROOT_PATH + JingWeiConstants.ZK_PATH_SEP + TASK_NAME
				+ JingWeiConstants.ZK_PATH_SEP + JingWeiConstants.JINGWEI_TASK_HOST_NODE + JingWeiConstants.ZK_PATH_SEP
				+ SERVER_NAME + JingWeiConstants.ZK_PATH_SEP + JingWeiConstants.JINGWEI_SCAN_ALARM_NODE;

		while (true) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			try {
				String data = RandomUtil.getRandomString(100);

				AlarmNode node = new AlarmNode();
				node.setMessage(data);

				zkConfigManager.publishOrUpdateData(path, node.toJSONString(), true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	//@Test
	public void testHeartBeat() {
		String path = JingWeiConstants.JINGWEI_TASK_ROOT_PATH + JingWeiConstants.ZK_PATH_SEP + TASK_NAME
				+ JingWeiConstants.ZK_PATH_SEP + JingWeiConstants.JINGWEI_TASK_HOST_NODE + JingWeiConstants.ZK_PATH_SEP
				+ SERVER_NAME + JingWeiConstants.ZK_PATH_SEP + JingWeiConstants.JINGWEI_HOST_HEART_BEAT_NODE_NAME;

		while (true) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			try {
				HeartbeatNode heartBeatNode = new HeartbeatNode();
				heartBeatNode.setTimestamp(new Date());

				zkConfigManager.publishOrUpdateData(path, heartBeatNode.toJSONString(), true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
