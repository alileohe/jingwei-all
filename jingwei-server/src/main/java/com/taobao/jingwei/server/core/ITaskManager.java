package com.taobao.jingwei.server.core;

import java.util.Set;

/**
 * @desc ����������ӿ�
 * 
 * @author <a href="mailto:shuohai.lhl@taobao.com">˷�� shuohailhl</a>
 * 
 * @date 2011-12-7����3:27:01
 */
public interface ITaskManager {
	/**
	 * ��ʼ��
	 */
	void init();

	/**
	 * ��ȡ������
	 * 
	 * @return ���ڹ����task��
	 */
	Set<String> getTasks();

	/**
	 * ���������������������Ϣ��op��������target��lastModifiedTime��work��lastModifiedTime
	 * 
	 * @param ctx
	 */
	void addContext(TaskContext ctx);

	/**
	 * ɾ�������task��Ӧ��context
	 * 
	 * @param taskName
	 */
	void removeContext(String taskName);

	/**
	 * �������������
	 * 
	 * @return ���ڹ����task��
	 */
	void startTask(String taskName);

	/**
	 * �������������
	 * 
	 * @return ���ڹ����task��
	 */
	void startTask(String taskName, String lockIndex, String groupName);

	/**
	 * ֹͣ���������
	 * 
	 * @return ���ڹ����task��
	 */
	void stopTask(String taskName);

}
