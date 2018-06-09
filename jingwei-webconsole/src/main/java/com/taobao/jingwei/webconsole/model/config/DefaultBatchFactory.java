package com.taobao.jingwei.webconsole.model.config;

import java.util.ArrayList;
import java.util.List;

import com.taobao.jingwei.webconsole.model.config.exception.BatchConfigException;
import com.taobao.jingwei.webconsole.model.config.extractor.MysqlExtractorConfig;
import com.taobao.jingwei.webconsole.model.config.util.ConfigUtil;

/**
 * @desc 根据输入的GROUP名字列表创建任务，一系列任务中只有任务名不同
 * 
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Jan 8, 2013 9:03:11 AM
 */

public class DefaultBatchFactory implements BatchFactory {

	// 根据模板配置创建一些列的任务配置对象
	private SyncTaskConfig syncTaskConfigTemplate;

	private String taskNamePrefix;

	private SuffixPolicy suffixPolicy;

	// 逗号分隔的字符串，内容是TDDL的GROUP
	private String groupNames;

	private static final String BAR = "-";

	@Override
	/**
	 * @throws BatchConfigException, 后缀序列个数和group序列个数不一样，抛异常
	 */
	public List<SyncTaskConfig> getSyncTaskConfigs() throws BatchConfigException {

		List<Integer> suffixIndexList = this.getSuffixIndexList();

		List<String> groupNameList = this.getGroupNameList();

		int suffixIndexListSize = suffixIndexList.size();
		int groupNameListSize = groupNameList.size();

		if (suffixIndexList.isEmpty()) {
			throw new BatchConfigException("suffix is empty!");
		}

		if (groupNameList.isEmpty()) {
			throw new BatchConfigException("group names is empty!");
		}

		if (suffixIndexListSize != groupNameListSize) {
			throw new BatchConfigException("suffix count is more than group count provided!");
		}

		List<SyncTaskConfig> syncTaskConfigList = new ArrayList<SyncTaskConfig>();

		for (int i = 0; i < suffixIndexListSize; i++) {
			int suffix = suffixIndexList.get(i);
			String taskName = new StringBuilder(this.getTaskNamePrefix()).append(BAR).append(suffix).toString();

			String groupName = groupNameList.get(i);

			SyncTaskConfig syncTaskConfig;
			try {
				syncTaskConfig = (SyncTaskConfig) this.getSyncTaskConfigTemplate().deepClone();
			} catch (Exception e) {
				throw new BatchConfigException("deep clone exception!");
			}
			syncTaskConfig.getCommonConfig().setTaskName(taskName);
			MysqlExtractorConfig mysqlExtractorConfig = (MysqlExtractorConfig) syncTaskConfig.getExtractorConfig();
			mysqlExtractorConfig.setGroupName(groupName);

			syncTaskConfigList.add(syncTaskConfig);
		}

		return syncTaskConfigList;
	}

	public List<SyncTaskConfig> getSyncTaskConfigs(SyncTaskConfig syncTaskConfigTemplate) throws BatchConfigException {
		this.setSyncTaskConfigTemplate(syncTaskConfigTemplate);

		return this.getSyncTaskConfigs();
	}

	/**
	 * 一些列文件名的后缀序列，数量应该和GROUP的数量相等
	 * @return
	 */
	protected List<Integer> getSuffixIndexList() {
		return this.getSuffixPolicy().createSuffixes();
	}

	/**
	 *   此处的Group指的是TDDL三层数据源的GROUP NAME
	 */
	protected List<String> getGroupNameList() {
		return ConfigUtil.commaSepString2List(this.getGroupNames());
	}

	public String getTaskNamePrefix() {
		return taskNamePrefix;
	}

	public void setTaskNamePrefix(String taskNamePrefix) {
		this.taskNamePrefix = taskNamePrefix;
	}

	public SyncTaskConfig getSyncTaskConfigTemplate() {
		return syncTaskConfigTemplate;
	}

	public void setSyncTaskConfigTemplate(SyncTaskConfig syncTaskConfigTemplate) {
		this.syncTaskConfigTemplate = syncTaskConfigTemplate;
	}

	public SuffixPolicy getSuffixPolicy() {
		return suffixPolicy;
	}

	public void setSuffixPolicy(SuffixPolicy suffixPolicy) {
		this.suffixPolicy = suffixPolicy;
	}

	public String getGroupNames() {
		return groupNames;
	}

	public void setGroupNames(String groupNames) {
		this.groupNames = groupNames;
	}
}
