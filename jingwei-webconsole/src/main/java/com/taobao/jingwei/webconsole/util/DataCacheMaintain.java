package com.taobao.jingwei.webconsole.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.config.ChildChangeListener;
import com.taobao.jingwei.common.config.ConfigManager;
import com.taobao.jingwei.webconsole.biz.ao.JingweiGroupAO;
import com.taobao.jingwei.webconsole.biz.ao.JingweiMonitorAO;
import com.taobao.jingwei.webconsole.biz.ao.JingweiServerAO;
import com.taobao.jingwei.webconsole.biz.ao.JingweiTaskAO;
import com.taobao.jingwei.webconsole.biz.manager.JingweiZkConfigManager;
import com.taobao.jingwei.webconsole.model.JingweiAssembledMonitor;
import com.taobao.jingwei.webconsole.model.JingweiMonitorCriteria;

public class DataCacheMaintain implements JingWeiConstants {

	private static Log log = LogFactory.getLog(DataCacheMaintain.class);
	
	private static final String JINGWEI_SERVER = "servers";

	@Autowired
	private JingweiZkConfigManager jwConfigManager;
	@Autowired
	private JingweiTaskAO jwTaskAO;
	@Autowired
	private JingweiServerAO jwServerAO;
	@Autowired
	private JingweiMonitorAO jwMonitorAO;
	@Autowired
	private JingweiGroupAO jwGroupAO;
	@Autowired
	private EnvDataCache envDataCache;

	public void init() {

		build();

		//		注册watcher节点到zk上
		for (String envKey : JingweiZkConfigManager.getKeys()) {
			ConfigManager configManager = jwConfigManager.getZkConfigManager(envKey);
			configManager.addChildChangesListener(JingWeiConstants.JINGWEI_TASK_ROOT_PATH, new ChildChangeListenerImpl(
					JingWeiConstants.JINGWEI_SERVER_TASKS_NAME, envKey));
			configManager.addChildChangesListener(JingWeiConstants.JINGWEI_SERVER_ROOT_PATH,
					new ChildChangeListenerImpl(JINGWEI_SERVER, envKey));

			configManager.addChildChangesListener(JingWeiConstants.JINGWEI_GROUP_ROOT_PATH,
					new ChildChangeListenerImpl(JingWeiConstants.JINGWEI_GROUPS_NAME, envKey));

			// e.g. /jingwei/monitors/tasks
			String path = JINGWEI_MONITOR_ROOT_PATH + ZK_PATH_SEP + JINGWEI_MONITOR_TASKS_NAME;
			configManager.addChildChangesListener(path, new ChildChangeListenerImpl(
					JingWeiConstants.JINGWEI_MONITOR_MONITORS_NODE_NAME, envKey));
		}

	}

	public void build() {
		log.info("init datacache");

		for (String envKey : JingweiZkConfigManager.getKeys()) {

			//			taskList
			List<String> taskChildList = jwConfigManager.getChildList(envKey, JingWeiConstants.JINGWEI_TASK_ROOT_PATH);
			envDataCache.getZkPathCache(envKey).put(DataCacheType.JingweiAssembledTask.toString(), taskChildList);

			//			server

			List<String> serverChildList = jwConfigManager.getChildList(envKey,
					JingWeiConstants.JINGWEI_SERVER_ROOT_PATH);
			envDataCache.getZkPathCache(envKey).put(DataCacheType.JingweiAssembledServer.toString(), serverChildList);

			//			monitor
			JingweiMonitorCriteria monitorCriteria = new JingweiMonitorCriteria();
			List<JingweiAssembledMonitor> monitors = jwMonitorAO.getMonitors(monitorCriteria, envKey);
			List<String> monitorChildList = new ArrayList<String>();
			for (JingweiAssembledMonitor monitor : monitors) {
				monitorChildList.add(monitor.getName());
			}
			envDataCache.getZkPathCache(envKey).put(DataCacheType.JingweiAssembledMonitor.toString(), monitorChildList);

			//			group

			ConfigManager configManager = jwConfigManager.getZkConfigManager(envKey);
			if (!configManager.exists(JINGWEI_GROUP_ROOT_PATH)) {
				try {
					configManager.publishData(JINGWEI_GROUP_ROOT_PATH, null, true);
				} catch (Exception e) {
					e.printStackTrace();
					log.error("create " + JINGWEI_GROUP_ROOT_PATH + " erroe!" + e);
				}
			}
			List<String> groupChildList = jwConfigManager
					.getChildList(envKey, JingWeiConstants.JINGWEI_GROUP_ROOT_PATH);
			envDataCache.getZkPathCache(envKey).put(DataCacheType.JingweiAssembledGroup.toString(), groupChildList);
		}

		log.info("datacache inited");
	}

	class ChildChangeListenerImpl extends ChildChangeListener {

		private String nodeName;
		private String envKey;

		public ChildChangeListenerImpl(String nodeName, String envKey) {
			this.nodeName = nodeName;
			this.envKey = envKey;
		}

		//更新缓存
		@Override
		public void handleChild(String parentPath, List<String> currentChilds) {

			if (JingWeiConstants.JINGWEI_SERVER_TASKS_NAME.equals(nodeName)) {

				envDataCache.getZkPathCache(envKey).put(DataCacheType.JingweiAssembledTask.toString(), currentChilds);

			} else if (JINGWEI_SERVER.equals(nodeName)) {

				envDataCache.getZkPathCache(envKey).put(DataCacheType.JingweiAssembledServer.toString(), currentChilds);

			} else if (JingWeiConstants.JINGWEI_GROUPS_NAME.equals(nodeName)) {

				envDataCache.getZkPathCache(envKey).put(DataCacheType.JingweiAssembledGroup.toString(), currentChilds);

			} else if (JingWeiConstants.JINGWEI_MONITOR_MONITORS_NODE_NAME.equals(nodeName)) {
				List<JingweiAssembledMonitor> list = jwMonitorAO.getMonitors(new JingweiMonitorCriteria(), envKey);
				List<String> tasks = new ArrayList<String>(list.size());
				for (JingweiAssembledMonitor t : list) {
					tasks.add(t.getName());
				}

				envDataCache.getZkPathCache(envKey).put(DataCacheType.JingweiAssembledMonitor.toString(), tasks);
			}
		}
	}

	public JingweiZkConfigManager getJwConfigManager() {
		return jwConfigManager;
	}

	public void setJwConfigManager(JingweiZkConfigManager jwConfigManager) {
		this.jwConfigManager = jwConfigManager;
	}

	public JingweiTaskAO getJwTaskAO() {
		return jwTaskAO;
	}

	public void setJwTaskAO(JingweiTaskAO jwTaskAO) {
		this.jwTaskAO = jwTaskAO;
	}

	public JingweiServerAO getJwServerAO() {
		return jwServerAO;
	}

	public void setJwServerAO(JingweiServerAO jwServerAO) {
		this.jwServerAO = jwServerAO;
	}

	public JingweiMonitorAO getJwMonitorAO() {
		return jwMonitorAO;
	}

	public void setJwMonitorAO(JingweiMonitorAO jwMonitorAO) {
		this.jwMonitorAO = jwMonitorAO;
	}

	public JingweiGroupAO getJwGroupAO() {
		return jwGroupAO;
	}

	public void setJwGroupAO(JingweiGroupAO jwGroupAO) {
		this.jwGroupAO = jwGroupAO;
	}

	public EnvDataCache getEnvDataCache() {
		return envDataCache;
	}

	public void setEnvDataCache(EnvDataCache envDataCache) {
		this.envDataCache = envDataCache;
	}

}
