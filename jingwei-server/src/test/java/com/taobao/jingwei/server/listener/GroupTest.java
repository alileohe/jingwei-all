package com.taobao.jingwei.server.listener;

import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.config.impl.zk.ZkConfig;
import com.taobao.jingwei.common.config.impl.zk.ZkConfigManager;
import com.taobao.jingwei.common.node.OperateNode;
import com.taobao.jingwei.common.node.OperateNode.OperateEnum;
import com.taobao.jingwei.common.node.server.ServerNode;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

/**
 * @desc 
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Mar 6, 2012 7:09:54 PM
 */

public class GroupTest {
	private String confPath = "D:\\jingwei-wss\\jingwei\\jingwei-server\\src\\main\\conf\\server.ini";
	private static String TASK_NAME = "BUILDIN_TASK_TEST";
	private static String SERVER_NAME = "arch036075.sqa.cm4";
	private static String[] groups = { "GROUP1", "GROUP2" };
	private static ZkConfigManager zkConfigManager = new ZkConfigManager();

	@Before
	public void init() {
		// ��ʼ��ZK������
		ZkConfig zkConfig = ZkConfig.getZkConfigFromFile(confPath);

		zkConfigManager.setZkConfig(zkConfig);
		zkConfigManager.init();
	}

	//	@Test
	public void testCraeteServerGroups() {
		//path
		String path = JingWeiConstants.JINGWEI_SERVER_ROOT_PATH + JingWeiConstants.ZK_PATH_SEP + SERVER_NAME;

		// data
		ServerNode serverNode = new ServerNode();

		serverNode.setGroups(Arrays.asList(groups));

		try {
			zkConfigManager.publishOrUpdateData(path, serverNode.toJSONString(), true);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testCraeteGroups() {
		//path
		String path = JingWeiConstants.JINGWEI_GROUP_ROOT_PATH + JingWeiConstants.ZK_PATH_SEP + "GROUP1"
				+ JingWeiConstants.ZK_PATH_SEP + JingWeiConstants.JINGWEI_GROUP_TASKS_NAME
				+ JingWeiConstants.ZK_PATH_SEP + TASK_NAME;

		// data

		try {
			zkConfigManager.publishOrUpdateData(path, null, true);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//	@Test
	public void testCraeteGroupOpetate() {
		//path
		String path = "/jingwei-v2/groups/GROUP1/tasks/BUILDIN_TASK_TEST_2/operate";

		OperateNode opNode = new OperateNode();
		opNode.setOperateEnum(OperateEnum.NODE_START);

		try {
			zkConfigManager.publishOrUpdateData(path, opNode.toJSONString(), true);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
