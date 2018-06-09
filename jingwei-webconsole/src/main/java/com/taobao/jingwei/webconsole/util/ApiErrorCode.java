package com.taobao.jingwei.webconsole.util;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.server.service.ServerErrorCode;

/**
 * @desc 定义错误码和错误描述；错误码使用字符串表示
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * @date Mar 19, 2013 11:51:33 AM
 */
public enum ApiErrorCode {

	/** 成功，没有错误，也许类名叫做API_STATUS更好 */
	SUCCESS("success"),

	/** server 名参数为空 */
	SERVER_PARAM_IS_EMPTY("jingwei server param is empty."),

	/** 通过机器名获取机器ip error */
	GET_SERVER_IP_FAILED("get jingwei server ip failed."),

	/** server 是 stop 不能访问 ，从zookeeper获取的状态 */
	SERVER_IS_STOP("jingwei server is under stop status."),

	/** server 不能访问 ，通过访问server的alive服务获取,不依赖zookeeper */
	SERVER_IS_NOT_ALIVE("jingwei server is not alive."),

	/** 调用jade api需要知道对应的环境（日常、线上、预发），获取失败 */
	GET_GADE_ENV_ERROR("get gade env error."),

	/** 调用jade api根据group 获取ip port失败，获取失败 */
	GET_ATOM_INFO_FROM_TDDLGROUP_FAILED("get atom info from tddl group failed."),

	/** 获取位点失败 show master status;show variables like "read_only" */
	GET_POSITION_FAILED("get position failed."),

	/** 文件不存在 */
	FILE_NOT_EXIST("file is not exist."),

	/** 删除文件失败 */
	DELETE_FILE_FAILED("delete file failed"),

	/** task name is empry */
	TASK_NAME_IS_EMPTY("task name should not be empty."),

	/** 要修改的位点内容为空 */
	POSITION_CONTENT_IS_EMPTY("position content should not be empty."),

	/** 要修改的位点内容格式错误 */
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

	/** 更新位点失败. */
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

	/** 获取ha的console失败 */
	GET_HA_CONSOLE_FAILED("get ha console failed."),

	/** 修改group任务start、stop失败 */
	TOGGLE_GROUP_NAME_FAILED("toggle group name failed."),

	/** 删除permission失败 */
	DELETE_PERMISSION_FAILED("delete permission failed.");

	/** 描述 */
	private String desc;

	/** 选项信息， 添加在desc后 */
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
