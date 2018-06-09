package com.taobao.jingwei.webconsole.model.config;

import java.util.List;

import com.taobao.jingwei.webconsole.model.config.exception.BatchConfigException;

/**
 * @desc �������������������Ϣ��Ҫʹ��
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Jan 8, 2013 9:01:51 AM
 */

public interface BatchFactory {
	List<SyncTaskConfig> getSyncTaskConfigs() throws BatchConfigException;

	List<SyncTaskConfig> getSyncTaskConfigs(SyncTaskConfig syncTaskConfigTemplate) throws BatchConfigException;
}
