package com.taobao.jingwei.server.core;

import java.util.Set;

/**
 * @desc 任务管理器接口
 * 
 * @author <a href="mailto:shuohai.lhl@taobao.com">朔海 shuohailhl</a>
 * 
 * @date 2011-12-7下午3:27:01
 */
public interface ITaskManager {
	/**
	 * 初始化
	 */
	void init();

	/**
	 * 获取任务名
	 * 
	 * @return 处于管理的task名
	 */
	Set<String> getTasks();

	/**
	 * 缓存运行期任务的属性信息：op监听器，target的lastModifiedTime，work的lastModifiedTime
	 * 
	 * @param ctx
	 */
	void addContext(TaskContext ctx);

	/**
	 * 删除缓存的task对应的context
	 * 
	 * @param taskName
	 */
	void removeContext(String taskName);

	/**
	 * 启动管理的任务
	 * 
	 * @return 处于管理的task名
	 */
	void startTask(String taskName);

	/**
	 * 启动管理的任务
	 * 
	 * @return 处于管理的task名
	 */
	void startTask(String taskName, String lockIndex, String groupName);

	/**
	 * 停止管理的任务
	 * 
	 * @return 处于管理的task名
	 */
	void stopTask(String taskName);

}
