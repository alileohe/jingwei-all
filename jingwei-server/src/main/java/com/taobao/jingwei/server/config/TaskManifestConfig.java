package com.taobao.jingwei.server.config;

import com.taobao.jingwei.common.JingWeiUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Properties;

/**
 * @desc pulgin�����������
 * 
 * @author <a href="mailto:shuohai.lhl@taobao.com">shuohailhl</a>
 * 
 * @date 2011-11-30����1:45:18
 */
public class TaskManifestConfig {

	private static Log log = LogFactory.getLog(TaskManifestConfig.class);

	/** task��main�������ڵ��� */
	private String taskMainClass;
	
	/** group���ı�� added by leiwen.zh */
	private String groupTargetFlag;

	private static final String TASK_MAIN_CLASS_KEY = "Main-Class";
	
	private static final String GROUP_TARGET_FLAG_KEY = "Group-Target";
	
	public static final String GROUP_TARGET_FLAG_TRUE = "True";

	public static TaskManifestConfig getTaskManifestConfig(String filePath) {

		TaskManifestConfig taskManifestConfig = new TaskManifestConfig();
		Properties properties = JingWeiUtil.getPropFromFile(filePath);
		taskManifestConfig.setTaskMainClass(properties.getProperty(TASK_MAIN_CLASS_KEY));
		taskManifestConfig.setGroupTargetFlag(properties.getProperty(GROUP_TARGET_FLAG_KEY));

		log.warn("[jingwei server] load customer task main class : " + taskManifestConfig.getTaskMainClass());

		return taskManifestConfig;
	}

	public String getTaskMainClass() {
		return taskMainClass;
	}

	public void setTaskMainClass(String taskMainClass) {
		this.taskMainClass = taskMainClass;
	}

    public String getGroupTargetFlag() {
        return groupTargetFlag;
    }

    public void setGroupTargetFlag(String groupTargetFlag) {
        this.groupTargetFlag = groupTargetFlag;
    }
	
	

}
