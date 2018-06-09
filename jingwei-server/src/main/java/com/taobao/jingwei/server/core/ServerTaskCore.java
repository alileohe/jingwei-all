package com.taobao.jingwei.server.core;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiUtil;
import com.taobao.jingwei.common.TaskUtil;
import com.taobao.jingwei.common.config.ConfigDataListener;
import com.taobao.jingwei.common.node.OperateNode;
import com.taobao.jingwei.common.node.OperateNode.OperateEnum;
import com.taobao.jingwei.common.node.StatusNode;
import com.taobao.jingwei.common.node.server.ServerTaskNode;
import com.taobao.jingwei.core.kernel.AbstractJingWeiCore;
import com.taobao.jingwei.server.util.GroupUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

import java.util.Map;

public class ServerTaskCore extends AbstractJingWeiCore {

	private Log log = LogFactory.getLog(ServerTaskCore.class);

	/** ���groupName�ǿգ������ /jingwei/servers/**server/tasks/**task/operate�ڵ��stop��ֹͣ����
	 * ���groupName���ǿգ������/jingwei/groups/**group/tasks/**task/operate�ڵ��stop��ֹͣ���� */
	private String groupName;

	private String lockIndex;

	@Override
	public void afterInitProcessor() {
		// NO-OP
	}

	/** ���groupName�ǿգ������ /jingwei/servers/**server/tasks/**task/operate�ڵ��stop��ֹͣ����
	 * ���groupName���ǿգ������/jingwei/groups/**group/tasks/**task/operate�ڵ��stop��ֹͣ����
	 *  */
	@Override
	public void beforeInitProcessor() {
		this.passParamFromGroupLoader();

		if (StringUtil.isBlank(this.getGroupName())) {
			this.addServerStopListener();
		} else {
			this.addGroupStopListener();
		}

		// ������standby״̬���˳�
		this.addStandbyExistListener();
	}

	/**
	 * ������standby״̬���˳�
	 */
	private void addStandbyExistListener() {
		final String statusPath = TaskUtil.getStatusNodePath(this.getTaskName(), this.getServerName());

		this.configManager.addDataListener(statusPath, new ConfigDataListener() {

			@Override
			public void handleData(String dataIdOrPath, String data) {
				if (StringUtil.isNotBlank(data)) {

					StatusNode statusNode = new StatusNode();
					try {
						statusNode.jsonStringToNodeSelf(data);
					} catch (JSONException e) {
						log.error("[jingwei server] get status state for task: " + getTaskName()
								+ ", status data to json string error : " + statusPath, e);
					}

					if (StatusNode.StatusEnum.STANDBY == statusNode.getStatusEnum()) {
						log.error("destroy standby task : " + getTaskName());
						JingWeiUtil.destroyZkAndExit(ServerTaskCore.this.configManager, 0);
					}
				}

			}
		});
	}

	/**
	 * ע����� ���˳����� e.g /jingwei/servers/**server/tasks/**task/operate,
	 */
	private void addServerStopListener() {
		String serverName = this.getServerName();
		final String taskName = this.getTaskName();

		String opPath = ServerTaskNode.getDataIdOrNodePathByServerTaskName(serverName, taskName);
		StringBuilder sb = new StringBuilder(opPath);
		sb.append(JingWeiUtil.ZK_PATH_SEP).append(JingWeiUtil.JINGWEI_OPERATE_NODE_NAME);

		this.addZkStopListener(sb.toString());

		log.warn("add server stop listener : " + sb.toString());
	}

	/**
	 * ���groupName���ǿգ������/jingwei/groups/**group/tasks/**task/operate�ڵ��stop��ֹͣ����
	 */
	private void addGroupStopListener() {
		String path = GroupUtil.getGroupTaskOpPath(this.getGroupName(), this.getTaskName());
		this.addZkStopListener(path);

		log.warn("add group stop listener : " + path);
	}

	/**
	 * ��zkע��stop�ļ���
	 * @param path
	 */
	private void addZkStopListener(String path) {
		this.configManager.addDataListener(path, new ConfigDataListener() {

			public void handleData(String dataIdOrPath, String data) {
				if (StringUtil.isNotBlank(data)) {
					OperateNode op = new OperateNode();
					try {
						op.jsonStringToNodeSelf(data);
						if (OperateEnum.NODE_STOP == op.getOperateEnum()) {

							log.warn("[jingwei-core] lisener stop operate for task : "
									+ ServerTaskCore.this.getTaskName() + " stopped this task, exit(0) !");

							ServerTaskCore.this.destory();
							this.deleteRunning();
							this.deleteInstanceLock();
							JingWeiUtil.destroyZkAndExit(ServerTaskCore.this.configManager, 0);
						}
					} catch (JSONException e) {
						log.error("get json string error, node path is : " + dataIdOrPath + ", data is : " + data);
					}
				}
			}

			// e.g. /jingwei/tasks/**task/t-locks/lock_i
			private void deleteInstanceLock() {
				String path = GroupUtil.getGroupTaskEephemeralLockPath(configManager, getTaskName(), lockIndex);
				log.warn("delete task instance lock : " + path);
				try {
					configManager.delete(path);
				} catch (Exception e) {
					log.error(e);
				}
			}

			private void deleteRunning() {
				// ��ȡstatus�ڵ� �������running�ͱ���
				String statusPath = TaskUtil.getStatusNodePath(getTaskName(), getServerName());

				try {
					ServerTaskCore.this.configManager.delete(statusPath);
				} catch (Exception e) {
					log.error(e);
				}
			}
		});
	}

	/**
	 * �����group���͵�������Ҫͨ��JingweiUtil���о�̬������Щ�����Loader���ݸ�Core������ConfigManager��serverName��taskName
	 */
	private void passParamFromGroupLoader() {

		// ����configManager
		if (this.configManager == null) {

			if (JingWeiUtil.getConfigManager() == null) {
				log.warn("[jingwei-core] zk config manager not exist. " + this.getTaskName());

				JingWeiUtil.destroyZkAndExit(ServerTaskCore.this.configManager, 0);
			}

			this.configManager = JingWeiUtil.getConfigManager();
		}

		// ���ݲ���������serverName��taskName
		String argString = JingWeiUtil.getArgString();

		if (StringUtil.isBlank(argString)) {
			log.warn("[jingwei-core] pass  args is blank exit. ");

			JingWeiUtil.destroyZkAndExit(ServerTaskCore.this.configManager, 0);
		} else {
			Map<String, String> args = JingWeiUtil.handleArgs(argString);
			log.warn("pass args : " + argString);
			if (StringUtil.isBlank(this.getGroupName())) {
				if (StringUtil.isBlank(this.getServerName())) {
					String serverName = args.get("serverName");
					this.setServerName(serverName);
				}

				if (StringUtil.isBlank(this.getTaskName())) {
					String taskName = args.get("taskName");
					this.setTaskName(taskName);
				}

				if (StringUtil.isBlank(this.getGroupName())) {
					String groupName = args.get("groupName");
					this.setGroupName(groupName);
				}
			}
			this.lockIndex = args.get("lockIndex");
		}

	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
}
