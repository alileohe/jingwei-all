package $package;

import com.taobao.jingwei.common.config.impl.zk.ZkConfig;
import com.taobao.jingwei.common.config.impl.zk.ZkConfigManager;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode;
import com.taobao.jingwei.server.core.ServerTaskCore;

/**   
 * <p>description:<p> 
 * ���Ҫ���ø�loader�뽫JINGWEI.MC�е�Main-Class: ���óɸ������������
 * ���磺Main-Class: xxx.SimpleLoader
 *
 * @{#} SimpleLoader.java Create on Dec 31, 2011 3:34:30 PM   
 *   
 * Copyright (c) 2011 by qihao.
 *
 *@author <a href="mailto:qihao@taobao.com">qihao</a> 
 *@version 1.0   
 */
public class SimpleLoader {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
        // ���ص���ʹ��
        // String taskName = ${artifactId}

		String taskName = getTaskName();

		//����jingweCore
		ServerTaskCore jingWeiCore = new ServerTaskCore();

		//׼��SyncTaskNode
		SyncTaskNode syncTaskNode = new SyncTaskNode();
		syncTaskNode.setName(taskName);

		//�Ƿ��ǵ�������
		syncTaskNode.setTaskInstanceCount(1);
		jingWeiCore.setSyncTaskNode(syncTaskNode);

		//��ʼ��ZK,���ZKʹ�ò���Ĭ�Ͼ�����Ⱥ��Ҫ��������
		ZkConfig zkConfig = new ZkConfig("10.232.37.114:2181,10.232.37.112:2181,10.232.102.191:2181");
		zkConfig.setZkSessionTimeoutMs(5000);
		ZkConfigManager zkConfigManager = new ZkConfigManager();
		zkConfigManager.setZkConfig(zkConfig);
		zkConfigManager.init();

		//����zkConfigManager
		jingWeiCore.setConfigManager(zkConfigManager);
		/**
		 * ����ExtractorҲ��������������
		 * ���ʹ�����õ�Extractor��Ҫ����
		 * syncTaskNode.setExtractorType();
		 */
		jingWeiCore.setExtractor(new SimpleExtractor());
		/**
		 * ����applier������ָ���Զ����applier
		 * ���ʹ�����õ�applier��Ҫ����
		 * syncTaskNode.setApplierType();
		 */
		jingWeiCore.setApplier(new SimpleApplier());

		jingWeiCore.init();
	}

	private static String getTaskName() {
		String bootArgString = JingWeiUtil.getArgString();
        Map<String, String> bootArgs = JingWeiUtil.handleArgs(bootArgString);
        String taskName = bootArgs.get("taskName");
		return taskName;
	}
}
