package com.taobao.jingwei.webconsole.model.config;

import java.util.ArrayList;
import java.util.List;

import com.taobao.jingwei.webconsole.model.config.exception.BatchConfigException;
import com.taobao.jingwei.webconsole.model.config.extractor.MysqlExtractorConfig;
import com.taobao.jingwei.webconsole.model.config.util.ConfigUtil;

/**
 * @desc ���������GROUP�����б�������һϵ��������ֻ����������ͬ
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Jan 8, 2013 9:03:11 AM
 */

public class DefaultBatchFactory implements BatchFactory {

	// ����ģ�����ô���һЩ�е��������ö���
	private SyncTaskConfig syncTaskConfigTemplate;

	private String taskNamePrefix;

	private SuffixPolicy suffixPolicy;

	// ���ŷָ����ַ�����������TDDL��GROUP
	private String groupNames;

	private static final String BAR = "-";

	@Override
	/**
	 * @throws BatchConfigException, ��׺���и�����group���и�����һ�������쳣
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
	 * һЩ���ļ����ĺ�׺���У�����Ӧ�ú�GROUP���������
	 * @return
	 */
	protected List<Integer> getSuffixIndexList() {
		return this.getSuffixPolicy().createSuffixes();
	}

	/**
	 *   �˴���Groupָ����TDDL��������Դ��GROUP NAME
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
