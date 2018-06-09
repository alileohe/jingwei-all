package com.taobao.jingwei.server.core;

import com.taobao.jingwei.common.node.server.ServerTaskNode.TaskTypeEnum;
import com.taobao.jingwei.server.util.ServerUtil;

import java.util.Set;

/**
 * @desc �������������
 * 
 * @author <a href="mailto:shuohai.lhl@taobao.com">˷�� shuohailhl</a>
 * 
 * @date 2011-12-7����4:12:22
 */
public class BuildinTaskManager extends AbstractTaskManager {

	// ��������������
	public static final String BUILDIN_BOOTER_MAIN_CLASS = "com.taobao.jingwei.server.core.ServerTaskMain";

	public BuildinTaskManager(ServerCoreThread serverCoreThread) {
		super(serverCoreThread);
	}

	/**
	 * Ϊzk���Ѿ����õ�taskע��op������
	 * @param taskNames
	 */
	public void prepare(Set<String> taskNames) {
		for (String taskName : taskNames) {
			log.warn("[jingwei server] add buildin task : " + taskName);

			this.addContext(new TaskContext(taskName, null));
		}
	}

	@Override
	public void startTask(String taskName, String lockIndex, String groupName) {
		super.startTask(taskName, lockIndex, groupName);
		
		String javaOpt = this.getJavaOpt(taskName);

		// MAIN_CLASS=$1	TASK_NAME=$2	TASK_TYPE=$3	SERVER_NAME=$4 LOCK_INDEX=$5	GROUP_NAME=$6	
		// ���ýű�����, ����group���͵���������  GROUP_NAME=$6	 ��һ������ʹ��Ĭ��ֵDFAULT_GROUP
		log.warn("[jingwei server] try to start buildin task : " + taskName + " lock index :" + lockIndex);
		ServerUtil.startJingweiTask(
				this.getTaskBootFileFullPath(), // NL
				BUILDIN_BOOTER_MAIN_CLASS, taskName, TaskTypeEnum.BUILDIN, this.getServerConfig().getServerName(),
				lockIndex, groupName, taskName/*��������, �����workĿ¼��jar��, �������û��ʵ������ added by leiwen.zh*/, javaOpt);
	}
}
