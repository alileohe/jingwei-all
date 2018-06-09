package com.taobao.jingwei.monitor.listener;

import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.config.impl.zk.ZkConfig;
import com.taobao.jingwei.common.config.impl.zk.ZkConfigManager;
import com.taobao.jingwei.common.node.HeartbeatNode;
import org.json.JSONException;
import org.junit.Before;

import java.util.Date;

/**
 * @desc ���ģ��д��ֵ��ZK��
 *       ����ͳ��ģ����Դ��룻��1��������ֵ�澯��2������opΪstart��statusΪnull�ĸ澯��3������alarm�澯
 * @author <a href="mailto:shuohai.lhl@taobao.com">shuohailhl</a>
 * @date 2011-11-15����11:10:46
 */
public class MonitorTaskSchedulerTest {
	private String confPath = "d:\\jingwei-wss\\jingwei\\jingwei-monitor\\src\\main\\conf\\monitor.ini";
	private static String TASK_NAME = "BUILDIN_TASK_TEST";
	// private static String HOST_NAME = "TEST_HOST_NAME";
	private static String SERVER_NAME = "v035114.sqa.cm4";

	private static ZkConfigManager zkConfigManager = new ZkConfigManager();

	@Before
	public void init() {
		// ��ʼ��ZK������
		ZkConfig zkConfig = ZkConfig.getZkConfigFromFile(confPath);

		zkConfigManager.setZkConfig(zkConfig);
		zkConfigManager.init();

		// ��ʼ���澯������

	}

	/**
	 * /jingwei-v2/tasks/BUILDIN_TASK_TEST/hosts/v035114.sqa.cm4/alarm�澯  
	 */
	//	@Test
	public void testOperateStatusNullAlarm() {
		String dateIdOrPath = JingWeiConstants.JINGWEI_TASK_ROOT_PATH + JingWeiConstants.ZK_PATH_SEP + TASK_NAME
				+ JingWeiConstants.ZK_PATH_SEP + JingWeiConstants.JINGWEI_TASK_HOST_NODE + JingWeiConstants.ZK_PATH_SEP
				+ SERVER_NAME + JingWeiConstants.ZK_PATH_SEP + "alarm";

		try {
			zkConfigManager.publishOrUpdateData(dateIdOrPath, "alarm info", true);

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * /jingwei-v2/tasks/BUILDIN_TASK_TEST/hosts/v035114.sqa.cm4/heartBeat�ڵ�
	 * @throws InterruptedException 
	 */
	//	@Test
	public void testHeartBeatAlarm() throws InterruptedException {
		String dateIdOrPath = JingWeiConstants.JINGWEI_TASK_ROOT_PATH + JingWeiConstants.ZK_PATH_SEP + TASK_NAME
				+ JingWeiConstants.ZK_PATH_SEP + JingWeiConstants.JINGWEI_TASK_HOST_NODE + JingWeiConstants.ZK_PATH_SEP
				+ SERVER_NAME + JingWeiConstants.ZK_PATH_SEP + JingWeiConstants.JINGWEI_HOST_HEART_BEAT_NODE_NAME;

		HeartbeatNode node = new HeartbeatNode();
		node.setName(JingWeiConstants.JINGWEI_HOST_HEART_BEAT_NODE_NAME);

		while (true) {
			Thread.sleep(20000);
			node.setTimestamp(new Date());
			try {
				zkConfigManager.publishOrUpdateData(dateIdOrPath, node.toJSONString(), true);

			} catch (JSONException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
