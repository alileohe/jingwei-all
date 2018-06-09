package com.taobao.jingwei.server.core;

import com.taobao.jingwei.common.JingWeiUtil;
import com.taobao.jingwei.common.node.server.ServerTaskNode.PluginTaskTargetStateEnum;
import com.taobao.jingwei.common.node.server.ServerTaskNode.PluginTaskWorkStateEnum;
import com.taobao.jingwei.common.node.server.ServerTaskNode.TaskTypeEnum;
import com.taobao.jingwei.server.config.TaskManifestConfig;
import com.taobao.jingwei.server.plugin.IPluginNotifier;
import com.taobao.jingwei.server.plugin.PluginScanner;
import com.taobao.jingwei.server.plugin.PluginScannerHelper;
import com.taobao.jingwei.server.util.ServerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @desc �������������
 * 
 * @author <a href="mailto:shuohai.lhl@taobao.com">˷�� shuohailhl</a>
 * 
 * @date 2011-12-7����4:12:47
 */
public class CustomerTaskManager extends AbstractTaskManager implements IPluginNotifier {
	private Log log = LogFactory.getLog(this.getClass());

	/** plugin�ļ�ɨ���� */
	private final PluginScanner pluginScanner;

	/** plugin�ļ�ɨ���������� */
	private final PluginScannerHelper pluginScannerHelper;

	/** ��ʱɨ��pluginĿ¼��ִ���� */
	private final ScheduledExecutorService executorService;

	/** ���gz���������� */
	private static final int INTEGRETY_CHECK_INTERVAL = 30000;

	public CustomerTaskManager(ServerCoreThread serverCoreThread) {
		super(serverCoreThread);

		this.pluginScanner = new PluginScanner(serverCoreThread);

		this.pluginScannerHelper = this.pluginScanner.getPluginScannerHelper();

		this.executorService = Executors.newSingleThreadScheduledExecutor(new PluginThreadFactory());
	}

	/**
	 * ��ʱɨ���ļ���
	 */
	public void beginScanPlugin() {

		log.warn("[jingwei server] begin scan plugin");

		// ��ʼ��ɨ����
		this.pluginScanner.init();

		// register self as plugin listener.
		this.pluginScanner.setNotifier(this);

		// ����ɨ���߳�
		this.executorService.scheduleAtFixedRate(this.pluginScanner, 0, // NL
				JingWeiUtil.DEFAULT_SCAN_PLUGIN_PERIOD, TimeUnit.MILLISECONDS);
	}

	/**
	 * <ol>
	 * <li>���work����target��ѹ����work
	 * <li>ע��opListenr
	 * <li>����opListener
	 * <li>����target��work��lastModified
	 * <li>ԭ����zk�ϵ�,���Ǵ�����û�е�task��,��״̬TARGET_DELETE_WORK_DELETE_STATE���͵�zk
	 * </ol>
	 * 
	 * @param taskNames server����ʱ,zk���Ѿ����ڵ�customer���͵����񼯺�
	 */
	public void preparePlugin(Set<String> taskNames) {
		// ���workĿ¼

		this.pluginScannerHelper.clearWorkDir();

		File[] targets = this.pluginScannerHelper.getPluginTargets();

		if (null == targets) {
			log.error("[]jingwei server] please set correct plugin directory path!");
			JingWeiUtil.destroyZkAndExit(null, -1);
			return;
		}

		for (File target : targets) {

			// ��ѹtarget��work
			this.pluginScannerHelper.unTarGz(target.getAbsolutePath());

			// �����ļ���ȡ��task��
			String taskName = this.pluginScannerHelper.getTaskNameByTargetFileName(target.getName());

			// �ж��Ƿ�Ϊgroup��, �����, ��дzk�ͻ���, added by leiwen.zh 2013-1-31
			if (isGroupTarget(taskName)) {
				log.warn("[jingwei-server] " + taskName + " is group target, do not write zk and cache");
				continue;
			}

			this.addTask(taskName, taskNames, PluginTaskTargetStateEnum.TARGET_NORM_STATE,
					PluginTaskWorkStateEnum.WORK_NORM_STATE);
		}

		// ȡ��zk�ϴ��ڣ����Ǵ�����û�е���Щ����������
		Set<String> justInZkTaskSet = this.getJustInZkTaskSet(taskNames);
		log.warn("[jingwei server] get customer tasks just at zk but not in work : " + justInZkTaskSet);

		// ������Щԭ����zk�ϵ�,���Ǵ�����û�е�task��,��״̬���͵�zk
		for (String taskName : justInZkTaskSet) {
			this.addTask(taskName, taskNames, PluginTaskTargetStateEnum.TARGET_DELETE_STATE,
					PluginTaskWorkStateEnum.WORK_DELETE_STATE);
		}

	}

	private boolean isGroupTarget(String fileName) {
		boolean result = false;
		// JINGWEI.MC �ļ��ľ���·��
		String jwMCFilePath = this.pluginScannerHelper.getMenifestFileFullName(fileName);
		TaskManifestConfig taskMFConfig = TaskManifestConfig.getTaskManifestConfig(jwMCFilePath);
		String groupTargetFlag = taskMFConfig.getGroupTargetFlag();
		if (groupTargetFlag != null && groupTargetFlag.equalsIgnoreCase(TaskManifestConfig.GROUP_TARGET_FLAG_TRUE)) {
			result = true;
		}
		return result;
	}

	static class PluginThreadFactory implements ThreadFactory {

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "[plugin-scanner-thread]");
		}
	}

	/**
	 * 
	 * @param taskNames zk������customer���͵�������
	 */
	public Set<String> getJustInZkTaskSet(Set<String> taskNames) {
		Set<String> justInZkTaskSet = new HashSet<String>();
		justInZkTaskSet.addAll(taskNames);

		// targetĿ¼�µ�task��
		Set<String> targetTaskNames = this.pluginScannerHelper.getTaskNamesFromTargetDir();

		justInZkTaskSet.removeAll(targetTaskNames);

		return justInZkTaskSet;
	}

	@Override
	public void onUpdateTarget(List<File> updateTargetFileNames) {
		for (File file : updateTargetFileNames) {
			String taskName = this.pluginScannerHelper.getTaskNameByTargetFileName(file.getName());

			// �Թ�group��, added by leiwen.zh
			if (this.isGroupTarget(taskName)) {
				log.warn("[jingwei-server]update group target, skip, file name:" + taskName);
				continue;
			}

			PluginTaskWorkStateEnum workState = ((CustomerTaskContext) tasks.get(taskName)).getWorkState();

			this.pluginScannerHelper.addOrUpdateCustomerServerTaskNode(taskName,
					PluginTaskTargetStateEnum.TARGET_UPDATE_STATE, workState);
			((CustomerTaskContext) tasks.get(taskName)).setTargetState(PluginTaskTargetStateEnum.TARGET_UPDATE_STATE);

			((CustomerTaskContext) tasks.get(taskName)).setTargetModified(file.lastModified());
		}
	}

	@Override
	public void onUpdateWorkTask(List<File> updateWorkDirs) {
		for (File file : updateWorkDirs) {
			String taskName = this.pluginScannerHelper.getTaskNameByWorkDirName(file.getName());

			// �Թ�groupĿ¼, added by leiwen.zh
			if (this.isGroupTarget(taskName)) {
				log.warn("[jingwei-server]update group work, skip, dir name:" + taskName);
				continue;
			}

			PluginTaskTargetStateEnum targetState = ((CustomerTaskContext) tasks.get(taskName)).getTargetState();

			// task����work��,��ѹ��
			this.pluginScannerHelper.addOrUpdateCustomerServerTaskNode(taskName, targetState,
					PluginTaskWorkStateEnum.WORK_UPDATE_STATE);

			((CustomerTaskContext) tasks.get(taskName)).setWorkState(PluginTaskWorkStateEnum.WORK_UPDATE_STATE);

			((CustomerTaskContext) tasks.get(taskName)).setWorkLastModified(file.lastModified());
		}
	}

	@Override
	public void onAddedTarget(List<File> addedTargetFileNames) {
		for (File file : addedTargetFileNames) {
			String taskName = this.pluginScannerHelper.getTaskNameByTargetFileName(file.getName());

			File workTaskDir = this.pluginScannerHelper.taskInWork(taskName);

			// task����work��,��ѹ��
			if (workTaskDir == null) {

				// ��������ԣ������������ȴ�10���ٳ���1�Σ����������ͷ��ز���ѹ�� ����־ ˵ѹ���ļ��д���
				boolean isInteger = false;
				try {
					isInteger = this.pluginScannerHelper.isIntegrity(file);

					if (!isInteger) {
						Thread.sleep(INTEGRETY_CHECK_INTERVAL);
						isInteger = this.pluginScannerHelper.isIntegrity(file);
					}
				} catch (IOException e) {
					log.error(e);
				} catch (InterruptedException e) {
					log.error(e);
					return;
				}

				if (!isInteger) {
					log.error("[jingwei server] file integrity check failed : " + file.getAbsolutePath());
					return;
				}

				// ��ѹ��work
				this.pluginScannerHelper.unTarGz(file.getAbsolutePath());
			}

			// �Թ�group�� added by leiwen.zh
			if (this.isGroupTarget(taskName)) {
				log.warn("[jingwei-server] add group target, skip, file name:" + taskName);
				continue;
			}

			try {
				this.getLock().lock();
				if (tasks.containsKey(taskName)) {
					// target����
					PluginTaskWorkStateEnum workState = ((CustomerTaskContext) tasks.get(taskName)).getWorkState();

					this.pluginScannerHelper.addOrUpdateCustomerServerTaskNode(taskName,
							PluginTaskTargetStateEnum.TARGET_UPDATE_STATE, workState);
					((CustomerTaskContext) tasks.get(taskName))
							.setTargetState(PluginTaskTargetStateEnum.TARGET_UPDATE_STATE);

					((CustomerTaskContext) tasks.get(taskName)).setTargetModified(file.lastModified());
				}
			} finally {
				this.getLock().unlock();
			}
		}
	}

	@Override
	public void onAddedWorkTask(List<File> addedWorkDirs) {

		for (File file : addedWorkDirs) {
			String taskName = this.pluginScannerHelper.getTaskNameByWorkDirName(file.getName());

			// �Թ�group�� added by leiwen.zh
			if (this.isGroupTarget(taskName)) {
				log.warn("[jingwei-server] add group work, skip, dir name:" + taskName);
				continue;
			}

			try {
				this.getLock().lock();

				if (this.getTasks().contains(taskName)) {

					PluginTaskTargetStateEnum targetState = ((CustomerTaskContext) tasks.get(taskName))
							.getTargetState();

					// ����zk���Ѿ����ڣ������target״̬˵��target��work������;���zk��û�д��ڣ��������д��zk��
					this.pluginScannerHelper.addOrUpdateCustomerServerTaskNode(taskName, targetState,
							PluginTaskWorkStateEnum.WORK_UPDATE_STATE);

					((CustomerTaskContext) tasks.get(taskName)).setWorkState(PluginTaskWorkStateEnum.WORK_UPDATE_STATE);

					((CustomerTaskContext) tasks.get(taskName)).setWorkLastModified(file.lastModified());
				} else {
					this.addTask(taskName, this.getTasks(), PluginTaskTargetStateEnum.TARGET_NORM_STATE,
							PluginTaskWorkStateEnum.WORK_NORM_STATE);
				}

			} finally {
				this.getLock().unlock();
			}
		}
	}

	@Override
	public void onDeleteTarget(List<File> deleteTargetFiles) {

		for (File file : deleteTargetFiles) {
			log.warn("[jingwei-server]delete  target, file :" + file);
			String taskName = this.pluginScannerHelper.getTaskNameByTargetFileName(file.getName());

			log.warn("[jingwei-server]delete  target, task name :" + taskName);

			String serverName = pluginScanner.getPluginScannerHelper().getServerCoreThread().getServerConfig()
					.getServerName();

			// �����zk�ϴ����ļ�
			if (null != ServerUtil.getServerTaskNodeByServerTaskNameFromZk(pluginScannerHelper.getServerCoreThread()
					.getConfigManager(), serverName, taskName)) {
				try {
					this.getLock().lock();
					if (this.getTasks().contains(taskName)) {
						PluginTaskWorkStateEnum workState = ((CustomerTaskContext) tasks.get(taskName)).getWorkState();

						this.pluginScannerHelper.addOrUpdateCustomerServerTaskNode(taskName,
								PluginTaskTargetStateEnum.TARGET_DELETE_STATE, workState);
						((CustomerTaskContext) tasks.get(taskName))
								.setTargetState(PluginTaskTargetStateEnum.TARGET_DELETE_STATE);
					}
				} finally {
					this.getLock().unlock();
				}
			} else {
				// �Թ�group��, added by leiwen.zh,�ļ��п��ܲ����ڣ������쳣
				try {
					if (this.isGroupTarget(taskName)) {
						log.warn("[jingwei-server]delete group target, skip, file name:" + taskName);
						continue;
					}
				} catch (Exception e) {
					log.error(e);
				}

			}
		}
	}

	@Override
	public void onDeleteWorkTask(List<File> deleteWorkDirs) {
		for (File file : deleteWorkDirs) {
			String taskName = this.pluginScannerHelper.getTaskNameByWorkDirName(file.getName());

			// �����zk�ϴ����ļ�
			String serverName = pluginScanner.getPluginScannerHelper().getServerCoreThread().getServerConfig()
					.getServerName();

			// �����zk�ϴ����ļ�
			if (null != ServerUtil.getServerTaskNodeByServerTaskNameFromZk(pluginScannerHelper.getServerCoreThread()
					.getConfigManager(), serverName, taskName)) {
				try {
					this.getLock().lock();
					if (this.getTasks().contains(taskName)) {
						PluginTaskTargetStateEnum targetState = ((CustomerTaskContext) tasks.get(taskName))
								.getTargetState();

						// task����work��,��ѹ��
						this.pluginScannerHelper.addOrUpdateCustomerServerTaskNode(taskName, targetState,
								PluginTaskWorkStateEnum.WORK_DELETE_STATE);

						((CustomerTaskContext) tasks.get(taskName))
								.setWorkState(PluginTaskWorkStateEnum.WORK_DELETE_STATE);
					}
				} finally {
					this.getLock().unlock();
				}
			} else {
				try {
					// �Թ�group��, added by leiwen.zh
					if (this.isGroupTarget(taskName)) {
						log.warn("[jingwei-server]delete group work, skip, dir name:" + taskName);
						continue;
					}
				} catch (Exception e) {
					log.error(e);
				}
			}
		}
	}

	private void addTask(String taskName, Set<String> taskNames, PluginTaskTargetStateEnum targetState,
			PluginTaskWorkStateEnum workState) {
		// ���zk��û�д��ڣ��������д��zk��,��task��Ӧ��opд��zk��
		if (!taskNames.contains(taskName)) {
			try {
				ServerUtil.publishStopTaskOperate(this.getConfigManager(), // NL
						this.getServerConfig().getServerName(), taskName);
			} catch (Exception e) {
				log.error(this.getClass() + " publish zk task op node data error! taskName is: " + taskName, e);
				return;
			}
			log.warn("[jingwei server] publish operate node for customer task: " + taskName);
		}

		// ����zk���Ѿ����ڣ������target״̬˵��target��work������;���zk��û�д��ڣ��������д��zk��
		this.pluginScannerHelper.addOrUpdateCustomerServerTaskNode(taskName, targetState, workState);

		// �����������Context
		CustomerTaskContext ctx = new CustomerTaskContext(taskName, null);

		File workDir = this.pluginScannerHelper.taskInWork(taskName);

		if (null != workDir) {
			ctx.setWorkLastModified(workDir.lastModified());
		}

		File targetFile = this.pluginScannerHelper.taskInTarget(taskName);

		if (null != targetFile) {
			ctx.setTargetModified(targetFile.lastModified());
		}

		ctx.setTargetState(targetState);
		ctx.setWorkState(workState);

		log.warn("[jingwei server] store in mem customer task context, task name : " + taskName
				+ "; target lastmodified  : " + ctx.getTargetModified() + "; work lastmodified :"
				+ ctx.getWorkLastModified() + "; target state : " + ctx.getTargetState() + "; work state : "
				+ ctx.getWorkState());

		this.addContext(ctx);
	}

	public PluginScannerHelper getPluginScannerHelper() {
		return pluginScannerHelper;
	}

	@Override
	public void startTask(String taskName, String lockIndex, String groupName) {
		super.startTask(taskName, lockIndex, groupName);

		// java opt
		String javaOpt = super.getJavaOpt(taskName);

		// plugin��������ȫ·����
		// modified by leiwen,zh 2013-1-31 , ����group��Ѱ��main-class���߼�
		String mainClass = this.pluginScanner.getPluginScannerHelper().getMainClassByTaskName(taskName, groupName);

		// workĿ¼�¶�Ӧ��Ŀ¼��, ������group��, Ҳ������task��
		String workDir = taskName;
		if (!this.pluginScannerHelper.getPluginWorkFileNames().contains(taskName)) {
			workDir = groupName;
		}

		log.warn("[jingwei server] start customer task : " + taskName + ", main class is : " + mainClass
				+ ", lock index : " + lockIndex + ", work dir : " + workDir);

		// MAIN_CLASS=$1 TASK_NAME=$2 TASK_TYPE=$3 SERVER_NAME=$4 LOCK_INDEX=$5 GROUP_NAME=$6 WORK_PATH=$7(added by leiwen.zh)
		// JAVA_OPT=$8
		// ���ýű�����, ����group���͵��������� GROUP_NAME=$6 ��һ������ʹ��Ĭ��ֵDEFAULT_GROUP
		ServerUtil.startJingweiTask(this.getTaskBootFileFullPath(), mainClass, taskName, TaskTypeEnum.CUSTOMER, this
				.getServerConfig().getServerName(), lockIndex, groupName, workDir, javaOpt);
	}
}