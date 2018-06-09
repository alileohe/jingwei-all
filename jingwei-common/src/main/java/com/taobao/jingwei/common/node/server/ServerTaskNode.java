package com.taobao.jingwei.common.node.server;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.node.AbstractNode;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class ServerTaskNode
 * 
 * @author qihao <dqylyln@gmail.com>
 * @since 11-11-17
 */
public class ServerTaskNode extends AbstractNode {
	/**
	 * ��Ӧ�������������
	 */
	private String taskName;
	/**
	 * �����Ǹ�server�ڵ�
	 */
	private String serverName;

	/** �������� */
	private TaskTypeEnum taskType;

	/** �����ڼ�task��work��pluginĿ¼�µ�״̬ */
	private PluginTaskTargetStateEnum pluginTaskTargetStateEnum = PluginTaskTargetStateEnum.TARGET_NORM_STATE;

	private PluginTaskWorkStateEnum pluginTaskWorkStateEnum = PluginTaskWorkStateEnum.WORK_NORM_STATE;

	private static final String TASK_NAME_KEY = "taskName";
	private static final String SERVER_NAME_KEY = "serverName";
	private static final String TASK_TYPE_KEY = "taskType";
	private static final String PLUGIN_TASK_TARGET_STATE_KEY = "pluginTaskTargetState";
	private static final String PLUGIN_TASK_WORK_STATE_KEY = "pluginTaskWorkState";

	/**
	 * ������������ȡ�ڵ�·��
	 * 
	 * @param taskName
	 * @return
	 */
	public static String getDataIdOrNodePathByServerTaskName(String serverName, String taskName) {
		if (StringUtil.isBlank(taskName)) {
			return StringUtil.EMPTY_STRING;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(JINGWEI_SERVER_ROOT_PATH).append(ZK_PATH_SEP);
		sb.append(serverName).append(ZK_PATH_SEP);
		sb.append(JINGWEI_SERVER_TASKS_NAME).append(ZK_PATH_SEP);
		sb.append(taskName);
		return sb.toString();
	}

	@Override
	protected void specilizeAttributeToJsonObject(JSONObject jsonObject) throws JSONException {
		jsonObject.put(TASK_NAME_KEY, StringUtil.defaultIfBlank(this.getTaskName()));
		jsonObject.put(SERVER_NAME_KEY, StringUtil.defaultIfBlank(this.getServerName()));
		jsonObject.put(TASK_TYPE_KEY, StringUtil.defaultIfNull(this.getTaskType().toString()));

		jsonObject.put(PLUGIN_TASK_TARGET_STATE_KEY,
				StringUtil.defaultIfBlank(this.getPluginTaskTargetStateEnum().toString()));

		jsonObject.put(PLUGIN_TASK_WORK_STATE_KEY,
				StringUtil.defaultIfBlank(this.getPluginTaskWorkStateEnum().toString()));
	}

	@Override
	protected void jsonObjectToSpecilizeAttribute(JSONObject jsonObject) throws JSONException {
		this.setTaskName(jsonObject.getString(TASK_NAME_KEY));
		this.setServerName(jsonObject.getString(SERVER_NAME_KEY));
		this.setTaskType(TaskTypeEnum.getTaskTypeEnumByString(StringUtil.defaultIfNull(jsonObject
				.getString(TASK_TYPE_KEY))));
		this.setPluginTaskTargetStateEnum(PluginTaskTargetStateEnum.getPluginTaskStateEnumByString(StringUtil
				.defaultIfNull(jsonObject.getString(PLUGIN_TASK_TARGET_STATE_KEY))));
		this.setPluginTaskWorkStateEnum(PluginTaskWorkStateEnum.getPluginTaskStateEnumByString(StringUtil
				.defaultIfNull(jsonObject.getString(PLUGIN_TASK_WORK_STATE_KEY))));
	}

	@Override
	public String getDataIdOrNodePath() {
		StringBuilder sb = new StringBuilder(JINGWEI_SERVER_ROOT_PATH);
		sb.append(ZK_PATH_SEP).append(this.getServerName()).append(ZK_PATH_SEP);
		sb.append(JINGWEI_SERVER_TASKS_NAME).append(ZK_PATH_SEP);
		sb.append(this.getTaskName());
		return sb.toString();
	}

	@Override
	public boolean isPersistent() {
		return true;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public TaskTypeEnum getTaskType() {
		return taskType;
	}

	public void setTaskType(TaskTypeEnum taskType) {
		this.taskType = taskType;
	}

	public PluginTaskTargetStateEnum getPluginTaskTargetStateEnum() {
		return pluginTaskTargetStateEnum;
	}

	public void setPluginTaskTargetStateEnum(PluginTaskTargetStateEnum pluginTaskTargetStateEnum) {
		this.pluginTaskTargetStateEnum = pluginTaskTargetStateEnum;
	}

	public PluginTaskWorkStateEnum getPluginTaskWorkStateEnum() {
		return pluginTaskWorkStateEnum;
	}

	public void setPluginTaskWorkStateEnum(PluginTaskWorkStateEnum pluginTaskWorkStateEnum) {
		this.pluginTaskWorkStateEnum = pluginTaskWorkStateEnum;
	}

	public static enum TaskTypeEnum {
		/**
		 * ���ƽڵ����������ֵ
		 */
		BUILDIN("Buildin"),

		CUSTOMER("Customer");

		private String taskTypeString;

		TaskTypeEnum(String taskTypeString) {
			this.taskTypeString = taskTypeString;
		}

		public String getTaskTypeString() {
			return taskTypeString;
		}

		/**
		 * @param taskTypeString
		 * 
		 * @return
		 */
		public static TaskTypeEnum getTaskTypeEnumByString(String taskTypeString) {
			if (StringUtil.isBlank(taskTypeString)) {
				return null;
			}
			TaskTypeEnum retEnum = null;
			for (TaskTypeEnum taskType : TaskTypeEnum.values()) {
				if (StringUtil.equals(taskType.toString(), taskTypeString)) {
					retEnum = taskType;
					break;
				}
			}
			return retEnum;
		}
	}

	/**
	 * ��ʾ������targetĿ¼��״̬ ,ɾ��,����,һ��
	 * @author shuohailhl
	 *
	 */
	public static enum PluginTaskTargetStateEnum {

		/** ���������ʱ�汾�ʹ��̰汾һ�� */
		TARGET_NORM_STATE("TARGET_NORM_STATE"),

		/** ���������ʱ�汾�ʹ��̰汾��һ�� */
		TARGET_UPDATE_STATE("TARGET_UPDATE_STATE"),

		/** task������,���Ƕ�Ӧ��target��ɾ�� */
		TARGET_DELETE_STATE("TARGET_DELETE_STATE");

		private String stateString;

		PluginTaskTargetStateEnum(String stateString) {
			this.stateString = stateString;
		}

		public String getPluginTaskStateString() {
			return stateString;
		}

		/**
		 * @param taskTypeString
		 * 
		 * @return
		 */
		public static PluginTaskTargetStateEnum getPluginTaskStateEnumByString(String stateString) {
			if (StringUtil.isBlank(stateString)) {
				return null;
			}
			PluginTaskTargetStateEnum retEnum = null;
			for (PluginTaskTargetStateEnum targetState : PluginTaskTargetStateEnum.values()) {
				if (StringUtil.equals(targetState.toString(), stateString)) {
					retEnum = targetState;
					break;
				}
			}
			return retEnum;
		}
	}

	/***
	 * ������workĿ¼��״̬,ɾ��,����,һ��
	 * @author shuohailhl
	 *
	 */
	public static enum PluginTaskWorkStateEnum {
		/** ���������ʱ�汾�ʹ��̰汾һ�� */
		WORK_NORM_STATE("WORK_NORM_STATE"),

		/** ���������ʱ�汾�ʹ��̰汾��һ�� */
		WORK_UPDATE_STATE("WORK_UPDATE_STATE"),

		/** task������,���Ƕ�Ӧ��target��ɾ�� */
		WORK_DELETE_STATE("WORK_DELETE_STATE");

		private String stateString;

		PluginTaskWorkStateEnum(String stateString) {
			this.stateString = stateString;
		}

		public String getPluginTaskStateString() {
			return stateString;
		}

		/**
		 * @param taskTypeString
		 * 
		 * @return
		 */
		public static PluginTaskWorkStateEnum getPluginTaskStateEnumByString(String stateString) {
			if (StringUtil.isBlank(stateString)) {
				return null;
			}
			PluginTaskWorkStateEnum retEnum = null;
			for (PluginTaskWorkStateEnum targetState : PluginTaskWorkStateEnum.values()) {
				if (StringUtil.equals(targetState.toString(), stateString)) {
					retEnum = targetState;
					break;
				}
			}
			return retEnum;
		}
	}

}
