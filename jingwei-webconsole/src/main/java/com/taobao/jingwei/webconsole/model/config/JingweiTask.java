package com.taobao.jingwei.webconsole.model.config;

/**
 * @desc 
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Jan 8, 2013 12:24:08 PM
 */

public interface JingweiTask {
	void addTask(String taskName, SyncTaskConfig syncTaskConfig);

	void deleteTask(String taskName);

	void updateTask(String taskName, SyncTaskConfig newTaskConfig);

	void getSyncTaskConfig(String taskName);

	/**
	 * ��������
	 * @param taskName
	 */
	void start(String taskName);

	/**
	 * ֹͣ����
	 * @param taskName
	 */
	void stop(String taskName);

	void start(String taskName, String host);

	void stop(String taskName, String host);
}
