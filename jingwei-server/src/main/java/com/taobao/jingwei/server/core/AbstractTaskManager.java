package com.taobao.jingwei.server.core;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.TaskUtil;
import com.taobao.jingwei.common.config.ConfigManager;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode;
import com.taobao.jingwei.server.config.ServerConfig;
import com.taobao.jingwei.server.util.ServerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @desc ����������������ʵ������ӡ�ɾ������ȡ���еĽӿ�
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date 2011-12-7����4:13:07
 */
abstract public class AbstractTaskManager implements ITaskManager, JingWeiConstants {

	protected final Log log = LogFactory.getLog(this.getClass());

	/** �����߳� */
	protected final ServerCoreThread serverCoreThread;

	/** ��������Ľű�ȫ·�� */
	private String taskBootFileFullPath;

	/** �������task����task���������� */
	protected final Map<String, TaskContext> tasks = new ConcurrentHashMap<String, TaskContext>();

	/** �������ɾ��ʱʹ�� */
	private final Lock lock = new ReentrantLock();

	public AbstractTaskManager(ServerCoreThread serverCoreThread) {
		this.serverCoreThread = serverCoreThread;
	}

	/**
	 * ��ȡִ������Ľű��ļ�·��
	 */
	@Override
	public void init() {
		this.taskBootFileFullPath = ServerUtil.getTaskBootFileFullPath(this.getServerConfig());

		if (log.isDebugEnabled()) {
			log.debug("[jingwei-server] init task manager, set task boot file path  : " + taskBootFileFullPath);
		}
	}

	@Override
	public Set<String> getTasks() {
		return tasks.keySet();
	}

	@Override
	public void startTask(String taskName) {
		if (log.isDebugEnabled()) {
			log.debug("[jingwei-server] start task " + taskName);
		}
	}

	@Override
	public void startTask(String taskName, String lockIndex, String groupName) {
		if (log.isDebugEnabled()) {
			log.debug("[jingwei-server] start task " + taskName + ", index " + lockIndex + ", group : " + groupName);
		}
	}

	/**
	 * ��ȡ����������
	 * 
	 * @return
	 */
	public Map<String, TaskContext> getContexts() {
		return tasks;
	}

	@Override
	public void addContext(TaskContext ctx) {
		this.tasks.put(ctx.getTaskName(), ctx);

		log.warn("[jingwei-server] add task context for task  : " + ctx.getTaskName());

	}

	@Override
	public void removeContext(String taskName) {
		// �������
		tasks.remove(taskName);

		log.warn("[jingwei-server] remove task context for task  : " + taskName);

	};

	@Override
	public void stopTask(String taskName) {
		// NO-OP ���������stop�������Լ�ͣ��������Ҫmanager����
	}

	/**
	 * ��ȡjava opt e.g. /jingwei/tasks/**task
	 * @param taskName
	 * @return <code>null<code> ��������ڻ���task�ڵ��ȡʧ��
	 */
	protected String getJavaOpt(String taskName) {
		SyncTaskNode syncTaskNode = TaskUtil.getSyncTaskNode(serverCoreThread.getConfigManager(), taskName);

		if (null == syncTaskNode) {
			return DEFAULT_JAVA_OPT;
		}

		String javaOpt = syncTaskNode.getJavaOpt();
		if (StringUtil.isBlank(javaOpt)) {
			return DEFAULT_JAVA_OPT;
		} else {
			return javaOpt;
		}
	}

	public ConfigManager getConfigManager() {
		return this.serverCoreThread.getConfigManager();
	}

	public ServerConfig getServerConfig() {
		return this.serverCoreThread.getServerConfig();
	}

	public ServerCoreThread getServerCoreThread() {
		return serverCoreThread;
	}

	public String getTaskBootFileFullPath() {
		return taskBootFileFullPath;
	}

	public Lock getLock() {
		return lock;
	}
}
