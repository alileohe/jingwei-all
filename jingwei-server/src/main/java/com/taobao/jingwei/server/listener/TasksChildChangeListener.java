package com.taobao.jingwei.server.listener;

import com.taobao.jingwei.common.config.ChildChangeListener;
import com.taobao.jingwei.common.node.server.ServerTaskNode;
import com.taobao.jingwei.common.node.server.ServerTaskNode.TaskTypeEnum;
import com.taobao.jingwei.server.core.BuildinTaskManager;
import com.taobao.jingwei.server.core.CustomerTaskManager;
import com.taobao.jingwei.server.core.ServerCoreThread;
import com.taobao.jingwei.server.core.TaskContext;
import com.taobao.jingwei.server.util.ServerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @desc ����/jingwei/servers/**server/tasks �ڵ㣬�����ͼ��ٵ�task
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com">shuohailhl</a>
 * 
 * @date 2011-11-16����6:33:59
 */
public class TasksChildChangeListener extends ChildChangeListener {
	private Log log = LogFactory.getLog(this.getClass());

	/** �����߳� */
	private final ServerCoreThread serverCoreThread;

	public TasksChildChangeListener(ServerCoreThread serverCoreThread) {
		this.serverCoreThread = serverCoreThread;
	}

	@Override
	public void handleChild(String parentPath, List<String> currentChilds) {

		// fix 2012-5-12 ���ַ���currentChilds Ϊnull���쳣
		if (null == currentChilds) {
			currentChilds = new ArrayList<String>();
			log.warn("[jingwei server] zk push child data for path : " + parentPath + " : null !");
		}

		Set<String> addTasks = this.getAddedTasks(currentChilds);
		Set<String> deleteTasks = this.getDeletedTasks(currentChilds);

		CustomerTaskManager customerTaskManager = serverCoreThread.getCustomerTaskManager();
		BuildinTaskManager buildinTaskManager = serverCoreThread.getBuildinTaskManager();

		// ������task
		for (String taskName : addTasks) {

			log.warn("[jingwei server] server observer that zk added one task : " + taskName);

			ServerTaskNode serverTaskNode = ServerUtil.getServerTaskNodeByServerTaskNameFromZk(
					serverCoreThread.getConfigManager(), serverCoreThread.getServerConfig().getServerName(), taskName);

			if (null == serverTaskNode) {
				continue;
			}

			if (TaskTypeEnum.CUSTOMER == serverTaskNode.getTaskType()) {
				// no-op 
			} else if (TaskTypeEnum.BUILDIN == serverTaskNode.getTaskType()) {
				// ����op�ڵ�
				try {
					ServerUtil.publishStopTaskOperate(serverCoreThread.getConfigManager(), serverCoreThread
							.getServerConfig().getServerName(), taskName);
				} catch (Exception e) {
					log.error("[jingwei server] creare operate node error for task : " + taskName);
					continue;
				}

				// ���������
				TaskContext ctx = new TaskContext(taskName, null);
				buildinTaskManager.addContext(ctx);
			}

		}

		// ���ٵ�task
		for (String taskName : deleteTasks) {

			log.warn("[jingwei server] server observer that zk deleted one task : " + taskName);

			try {
				customerTaskManager.getLock().lock();
				if (customerTaskManager.getTasks().contains(taskName)) {

					// ɾ�������������
					customerTaskManager.removeContext(taskName);

					return;
				}
			} finally {
				customerTaskManager.getLock().unlock();
			}

			try {
				buildinTaskManager.getLock().lock();
				if (buildinTaskManager.getTasks().contains(taskName)) {

					// ɾ�������������
					buildinTaskManager.removeContext(taskName);
				}
			} finally {
				buildinTaskManager.getLock().unlock();
			}

		}
	}

	/**
	 * ��ȡ������task�б�
	 * 
	 * @param currentChilds ��ǰzk�����б�
	 * @return ������task�б�
	 */
	private Set<String> getAddedTasks(List<String> currentChilds) {
		Set<String> addTasks = new HashSet<String>();
		addTasks.addAll(currentChilds);
		addTasks.removeAll(serverCoreThread.getBuildinTaskManager().getTasks());
		addTasks.removeAll(serverCoreThread.getCustomerTaskManager().getTasks());

		return addTasks;
	}

	/**
	 * ��ȡ���ٵ�task�б�
	 * 
	 * @param currentChilds  ��ǰzk�����б�
	 * @return ���ٵ�task�б�
	 */
	private Set<String> getDeletedTasks(List<String> currentChilds) {
		Set<String> deleteTasks = new HashSet<String>();
		deleteTasks.addAll(serverCoreThread.getBuildinTaskManager().getTasks());
		deleteTasks.addAll(serverCoreThread.getCustomerTaskManager().getTasks());
		deleteTasks.removeAll(currentChilds);

		return deleteTasks;
	}

}
