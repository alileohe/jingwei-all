package com.taobao.jingwei.webconsole.util;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.server.service.ServerErrorCode;

/**
 * @desc ���������ʹ���������������ʹ���ַ�����ʾ
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * @date Mar 19, 2013 11:51:33 AM
 */
public enum ApiErrorCode {

	/** �ɹ���û�д���Ҳ����������API_STATUS���� */
	SUCCESS("success"),

	/** server ������Ϊ�� */
	SERVER_PARAM_IS_EMPTY("jingwei server param is empty."),

	/** ͨ����������ȡ����ip error */
	GET_SERVER_IP_FAILED("get jingwei server ip failed."),

	/** server �� stop ���ܷ��� ����zookeeper��ȡ��״̬ */
	SERVER_IS_STOP("jingwei server is under stop status."),

	/** server ���ܷ��� ��ͨ������server��alive�����ȡ,������zookeeper */
	SERVER_IS_NOT_ALIVE("jingwei server is not alive."),

	/** ����jade api��Ҫ֪����Ӧ�Ļ������ճ������ϡ�Ԥ��������ȡʧ�� */
	GET_GADE_ENV_ERROR("get gade env error."),

	/** ����jade api����group ��ȡip portʧ�ܣ���ȡʧ�� */
	GET_ATOM_INFO_FROM_TDDLGROUP_FAILED("get atom info from tddl group failed."),

	/** ��ȡλ��ʧ�� show master status;show variables like "read_only" */
	GET_POSITION_FAILED("get position failed."),

	/** �ļ������� */
	FILE_NOT_EXIST("file is not exist."),

	/** ɾ���ļ�ʧ�� */
	DELETE_FILE_FAILED("delete file failed"),

	/** task name is empry */
	TASK_NAME_IS_EMPTY("task name should not be empty."),

	/** Ҫ�޸ĵ�λ������Ϊ�� */
	POSITION_CONTENT_IS_EMPTY("position content should not be empty."),

	/** Ҫ�޸ĵ�λ�����ݸ�ʽ���� */
	POSITION_CONTENT_FORMAT_ERROR("position content format error."),

	/** task extractor data is empty */
	EXTRACTOR_DATA_IS_EMPTY("task extractor data should not be empty."),

	/** extractor type should be mysqlbinlog */
	EXTRACTOR_TYPE_IS_NOT_MYSQLBINLOG(" extractor type should be mysqlbinlog."),

	/** only auto switch binlog extractor */
	ONLY_SUPPORT_AUTO_SWITCH_MYSQL_EXTRACTOR(" only auto switch binlog extractor."),

	/** group name is empty or null */
	GROUP_NAME_IS_NULL_OR_EMPTY(" group name is empty or null."),

	/** running task should not change commit position. */
	RUNNING_SHOULD_NOT_CHANGE_POSITION("running task should not change commit position."),

	/** can not update empty position. */
	CAN_NOT_UPDTE_EMPTY_POSITION("can not update empty position."),

	/** ����λ��ʧ��. */
	UPDTE_POSITION_FAILED("update position failed."),

	/** task list is null or empty */
	TASK_LIST_IS_NULL_OR_EMPTY("task list is null or empty."),
	
	/** resources is null or empty */
	RESOURCE_IS_NULL_OR_EMPTY("resource is null or empty."),
	
	/** resources type is null or empty */
	RESOURCE_TYPE_IS_NULL_OR_EMPTY("resource type is null or empty."),

	/** config arrtibute not same */
	CONFIG_ATTRIBUTE_NOT_SAME("config arrtibute not same."),

	/** param can't be empty */
	EMPTY_PARAM_ERR("param can't be empty ."),

	/** update jingwei group failed */
	UPDATE_JINGWEI_GROUP_NODE_FAILED("update jingwei group failed : "),

	/** ��ȡha��consoleʧ�� */
	GET_HA_CONSOLE_FAILED("get ha console failed."),

	/** �޸�group����start��stopʧ�� */
	TOGGLE_GROUP_NAME_FAILED("toggle group name failed."),

	/** ɾ��permissionʧ�� */
	DELETE_PERMISSION_FAILED("delete permission failed.");

	/** ���� */
	private String desc;

	/** ѡ����Ϣ�� �����desc�� */
	private String option;

	ApiErrorCode(String desc) {
		this.desc = desc;
	}

	public String getDescAndReset() {

		if (StringUtil.isNotBlank(this.getOption())) {
			this.setOption(StringUtil.EMPTY_STRING);
			return desc + this.getOption();
		}
		return desc;
	}

	public String getOption() {
		return option;
	}

	public ApiErrorCode setOption(String option) {
		this.option = option;
		return this;
	}

	public static ServerErrorCode getErrCodeByTypeString(String typeString) {
		if (StringUtil.isBlank(typeString)) {
			return null;
		}

		ServerErrorCode type = null;
		for (ServerErrorCode typeEnum : ServerErrorCode.values()) {
			if (StringUtil.equals(typeEnum.toString(), typeString)) {
				type = typeEnum;
				break;
			}
		}

		return type;
	}

	public static void main(String[] args) {
		System.out.println(ApiErrorCode.EXTRACTOR_DATA_IS_EMPTY.setOption("asdfApiErrorCode").getDescAndReset());
	}
}
