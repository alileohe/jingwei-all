package com.taobao.jingwei.webconsole.model.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONException;

import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.config.ConfigManager;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode.GroupingSetting;
import com.taobao.jingwei.webconsole.model.config.applier.ApplierConfig;
import com.taobao.jingwei.webconsole.model.config.exception.BatchConfigException;
import com.taobao.jingwei.webconsole.model.config.extractor.ExtractorConfig;

/**
 * @desc
 * 
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Jan 5, 2013 7:40:27 PM
 */

public class ConfigHandler {
	// private static Log log = LogFactory.getLog(ConfigHandler.class);

	private static ConfigHandler instance = new ConfigHandler();

	public static ConfigHandler getInstance() {
		return instance;
	}

	public SyncTaskNode getSyncTaskNode(SyncTaskConfig syncTaskConfig) throws JSONException, BatchConfigException {
		SyncTaskNode syncTaskNode = new SyncTaskNode();

		// common config
		CommonConfig commonConfig = syncTaskConfig.getCommonConfig();
		syncTaskNode.setComitLogCount(commonConfig.getComitLogCount());
		syncTaskNode.setComitLogPeriod(commonConfig.getComitLogPeriod());
		syncTaskNode.setDesc(commonConfig.getDescription());
		syncTaskNode.setJavaOpt(commonConfig.getJavaOpt());
		syncTaskNode.setMaxThreadCount(commonConfig.getMaxThreadCount());
		syncTaskNode.setMultiThread(commonConfig.isMultiThread());
		syncTaskNode.setName(commonConfig.getTaskName());
		syncTaskNode.setTaskInstanceCount(commonConfig.getTaskInstanceCount());
		syncTaskNode.setQueueCapacity(commonConfig.getQueueCapacity());
		syncTaskNode.setStatsPeriod(commonConfig.getStatsPeriod());
		syncTaskNode.setSummaryPeriod(commonConfig.getSummaryPeriod());
		syncTaskNode.setUseLastPosition(commonConfig.isUseLastPosition());
		syncTaskNode.setGroupingSettings(this.getGroupSetting(commonConfig.getGroupingSettings()));

		// extractor
		ExtractorConfig extractorConfig = syncTaskConfig.getExtractorConfig();
		syncTaskNode.setExtractorType(extractorConfig.getExtractorType());
		try {
			syncTaskNode.setExtractorData(extractorConfig.getExtarctorNode().toJSONString());

		} catch (JSONException e) {
			throw new JSONException("extractor config convert to json err!");
		}

		// applier
		ApplierConfig applierConfig = syncTaskConfig.getApplierConfig();
		syncTaskNode.setApplierType(applierConfig.getApplierType());
		if (null != applierConfig) {
			try {
				syncTaskNode.setApplierData(applierConfig.getApplierNode().toJSONString());
			} catch (JSONException e) {
				throw new JSONException("applier config convert to json err!");
			}
		}

		// filter
		CommonFilterConfig commonFilterConfig = syncTaskConfig.getCommonFilterConfig();
		if (null != commonFilterConfig) {
			try {

				syncTaskNode.setApplierFilterData(commonFilterConfig.getEventFilterNode().toJSONString());
			} catch (JSONException e) {
				throw new JSONException("common filter config convert to json err!");
			}
		}

		return syncTaskNode;
	}

	/**
	 * 获取任务节点， /jingwei/tasks/**task
	 * 
	 * @param configManager
	 * @param taskName
	 * @return <code>null</code>获取失败或节点不存在
	 */
	public static void publishSyncTaskNode(ConfigManager configManager, SyncTaskNode syncTaskNode)
			throws JSONException, Exception {
		String taskName = syncTaskNode.getName();

		StringBuilder path = new StringBuilder(JingWeiConstants.JINGWEI_TASK_ROOT_PATH);
		path.append(JingWeiConstants.ZK_PATH_SEP).append(taskName);

		String data = syncTaskNode.toJSONString();

		configManager.publishData(path.toString(), data, true);

	}

	/**
	 * 获取任务节点， /jingwei/tasks/**task
	 * 
	 * @param configManager
	 * @param taskName
	 * @return <code>null</code>获取失败或节点不存在
	 */
	public static void publishSyncTaskNode(ConfigManager configManager, SyncTaskConfig syncTaskConfig) throws Exception {
		SyncTaskNode syncTaskNode = ConfigHandler.getInstance().getSyncTaskNode(syncTaskConfig);

		publishSyncTaskNode(configManager, syncTaskNode);
	}

	private List<GroupingSetting> getGroupSetting(List<GroupingConfig> groupingConfigs) {
		if (groupingConfigs == null || groupingConfigs.isEmpty()) {
			return Collections.emptyList();
		}

		List<GroupingSetting> settings = new ArrayList<GroupingSetting>(groupingConfigs.size());

		for (GroupingConfig config : groupingConfigs) {
			GroupingSetting setting = new GroupingSetting();
			setting.setFields(config.getFields());
			setting.setSchemaReg(config.getSchemaReg());
			setting.setTableReg(config.getTableReg());

			settings.add(setting);
		}

		return settings;
	}
}
