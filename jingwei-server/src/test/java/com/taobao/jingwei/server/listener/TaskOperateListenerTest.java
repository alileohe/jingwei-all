package com.taobao.jingwei.server.listener;

import com.taobao.jingwei.common.JingWeiUtil;
import com.taobao.jingwei.common.config.impl.zk.ZkConfig;
import com.taobao.jingwei.common.config.impl.zk.ZkConfigManager;
import com.taobao.jingwei.common.node.OperateNode;
import com.taobao.jingwei.common.node.OperateNode.OperateEnum;
import com.taobao.jingwei.common.node.server.ServerTaskNode;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author <a href="mailto:shuohai.lhl@taobao.com">shuohailhl</a>
 * 
 * @date 2011-12-14����1:35:21
 * 
 * @version 1.0
 */
public class TaskOperateListenerTest {
	private String confPath = "D:\\jingwei-wss\\jingwei\\jingwei-server\\src\\main\\conf\\server.ini";
	private static String TASK_NAME = "BUILDIN_TASK_TEST_2";
	private static String SERVER_NAME = "v035114.sqa.cm4";
	private static ZkConfigManager zkConfigManager = new ZkConfigManager();

	@Before
	public void init() {
		// ��ʼ��ZK������
		ZkConfig zkConfig = ZkConfig.getZkConfigFromFile(confPath);

		zkConfigManager.setZkConfig(zkConfig);
		zkConfigManager.init();
	}

	@Test
	public void testStartTask() {
		OperateNode opNode = new OperateNode();
		opNode.setName(JingWeiUtil.JINGWEI_OPERATE_NODE_NAME);
		opNode.setOperateEnum(OperateEnum.NODE_START);

		String parentNodeId = ServerTaskNode
				.getDataIdOrNodePathByServerTaskName(SERVER_NAME, TASK_NAME);
		opNode.setOwnerDataIdOrPath(parentNodeId);
		String opDataIdOrPath = opNode.getDataIdOrNodePath();

		try {
			zkConfigManager.publishOrUpdateData(opDataIdOrPath,
					opNode.toJSONString(), opNode.isPersistent());
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
