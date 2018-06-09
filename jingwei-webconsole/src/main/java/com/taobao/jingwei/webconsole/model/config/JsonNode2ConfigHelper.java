package com.taobao.jingwei.webconsole.model.config;

import java.util.ArrayList;
import java.util.List;

import com.taobao.jingwei.common.node.tasks.SyncTaskNode;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode.GroupingSetting;

/**
 * @desc 把zk的Node格式转换成Config的实体类的帮助类
 * 
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Jan 11, 2013 7:34:23 PM
 */

public class JsonNode2ConfigHelper {
	/**
	 * SyncTaskNode 获取CommonConig
	 * @param syncTaskNode
	 * @return
	 */
	public static CommonConfig getCommonConfig(SyncTaskNode syncTaskNode) {
		CommonConfig commonConfig = new CommonConfig();

		commonConfig.setComitLogCount(syncTaskNode.getComitLogCount());
		commonConfig.setComitLogPeriod(syncTaskNode.getComitLogPeriod());
		commonConfig.setDescription(syncTaskNode.getDesc());
		commonConfig.setJavaOpt(syncTaskNode.getJavaOpt());
		commonConfig.setMaxThreadCount(syncTaskNode.getMaxThreadCount());
		commonConfig.setMultiThread(syncTaskNode.isMultiThread());
		commonConfig.setQueueCapacity(syncTaskNode.getQueueCapacity());
		commonConfig.setStatsPeriod(syncTaskNode.getStatsPeriod());
		commonConfig.setSummaryPeriod(syncTaskNode.getSummaryPeriod());
		commonConfig.setTaskInstanceCount(syncTaskNode.getTaskInstanceCount());
		commonConfig.setTaskName(syncTaskNode.getName());
		commonConfig.setUseLastPosition(syncTaskNode.isUseLastPosition());

		List<GroupingSetting> groupringSettings = syncTaskNode.getGroupingSettings();

		List<GroupingConfig> groupingConfigs = new ArrayList<GroupingConfig>();

		if (groupringSettings != null) {
			for (GroupingSetting groupingSetting : groupringSettings) {
				GroupingConfig groupingConfig = new GroupingConfig();

				groupingConfig.setFields(groupingSetting.getFields());
				groupingConfig.setSchemaReg(groupingSetting.getSchemaReg());
				groupingConfig.setTableReg(groupingSetting.getTableReg());

				groupingConfigs.add(groupingConfig);
			}

			commonConfig.setGroupingSettings(groupingConfigs);
		}

		return commonConfig;
	}
}
