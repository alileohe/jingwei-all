/**
 * 
 */
package com.taobao.jingwei.webconsole.model;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.biz.command.result.ResultSupport;
import com.alibaba.common.lang.StringUtil;

/**
 * 用于存放结果信息以及错误消息
 * 
 * @author qingren 2011-8-28
 */
public class JingWeiResult extends ResultSupport {
	private static final long serialVersionUID = 471623667113910910L;
	/**
	 * 不允许为null
	 */
	public static final String JINGWEI_ERROR_CODE_FORBIDDEN_NULL = "01";

	/**
	 * 目标对象已存在
	 */
	public static final String JINGWEI_ERROR_CODE_EXISTED = "02";

	/**
	 * 目标对象不存在
	 */
	public static final String JINGWEI_ERROR_CODE_NOT_EXISTED = "03";

	/**
	 * 在{0}操作之前，请将{1}的当前操作置为{2}
	 */
	public static final String JINGWEI_ERROR_CODE_OPT_UNMEETS = "04";

	/**
	 * {0}已被使用.
	 */
	public static final String JINGWEI_ERROR_CODE_USED = "05";

	/**
	 * {0}处于运行状态，不能移除。
	 */
	public static final String JINGWEI_ERROR_CODE_TASK_RUNNING = "06";

	/**
	 * task[{0}]未与agent关联。
	 */
	public static final String JINGWEI_ERROR_CODE_TASK_NOT_QUOTED = "07";

	/**
	 * agent[{0}]未启动。
	 */
	public static final String JINGWEI_ERROR_CODE_AGENT_NOT_STARTED = "08";

	/**
	 * 节点创建失败
	 */
	public static final String JINGWEI_ERROR_CODE_PATH_CREATE_FAILED = "09";

	/**
	 * 节点删除失败
	 */
	public static final String JINGWEI_ERROR_CODE_PATH_DELETE_FAILED = "10";

	/**
	 * 解析extractor data错误
	 */
	public static final String JINGWEI_ERROR_PARSE_EXTRACTOR_DATA = "11";

	public static final String JINGWEI_ERROR_EXCEPTION = "12";
	
	/** /jingwei/servers/**server/tasks/**task节点已经存在 */
	public static final String JINGWEI_ERROR_SERVER_TASK_EXIST = "13";
	
	/** 批量更新的类型不匹配*/
	public static final String UPDATE_TYPE_UNMATCH_ERROR = "14";

	/**
	 * 服务器发生异常
	 */
	public static final String JINGWEI_ERROR_CODE_OPERATION_FAILED = "100";

	public static final Map<String, String> msgHolder;

	static {
		msgHolder = new HashMap<String, String>();
		msgHolder.put(JINGWEI_ERROR_CODE_FORBIDDEN_NULL, "{0}不能为空。");
		msgHolder.put(JINGWEI_ERROR_CODE_EXISTED, "{0}已存在。");
		msgHolder.put(JINGWEI_ERROR_CODE_NOT_EXISTED, "{0}不存在。");
		msgHolder.put(JINGWEI_ERROR_CODE_OPERATION_FAILED, "服务器内部发生异常，操作失败。");
		msgHolder.put(JINGWEI_ERROR_CODE_OPT_UNMEETS,
				"在{0}操作之前，请将{1}的当前操作置为{2}。");
		msgHolder.put(JINGWEI_ERROR_CODE_USED, "{0}已被使用。");
		msgHolder.put(JINGWEI_ERROR_CODE_TASK_RUNNING, "{0}处于运行状态，无法进行此操作");
		msgHolder.put(JINGWEI_ERROR_CODE_TASK_NOT_QUOTED, "task[{0}]未与agent关联");
		msgHolder.put(JINGWEI_ERROR_CODE_AGENT_NOT_STARTED, "agent[{0}]未启动");
		msgHolder.put(JINGWEI_ERROR_CODE_PATH_CREATE_FAILED, "节点{0}创建失败");
		msgHolder.put(JINGWEI_ERROR_CODE_PATH_DELETE_FAILED, "节点{0}删除失败");
		msgHolder.put(JINGWEI_ERROR_PARSE_EXTRACTOR_DATA,
				"Binlog Extractor数据格式错误！");
		msgHolder.put(JINGWEI_ERROR_EXCEPTION, "{0}");
		msgHolder.put(JINGWEI_ERROR_SERVER_TASK_EXIST, "主机上{0}已经存在{1}任务");
		msgHolder.put(UPDATE_TYPE_UNMATCH_ERROR, "要更新的类型是{0},实际是{1}");
	}

	/**
	 * 错误代码
	 */
	private String errorCode;

	/**
	 * 消息中的替代参数
	 */
	private String[] replaceInfo;

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * 获取错误消息
	 * 
	 * @return
	 */
	@SuppressWarnings("all")
	public String getMessage() {
		String msg = "";
		if (StringUtil.isBlank(errorCode)) {
			;
		} else if (replaceInfo == null || replaceInfo.length == 0) {
			msg = msgHolder.get(this.errorCode);
		} else {
			msg = MessageFormat.format(msgHolder.get(errorCode), replaceInfo);
		}
		return msg;
	}

	/**
	 * 获取错误消息
	 * 
	 * @return
	 */
	@SuppressWarnings("all")
	public static String getMessage(String errorCode, String... replaceInfo) {
		String msg = "";
		if (StringUtil.isBlank(errorCode)) {
			;
		} else if (replaceInfo == null || replaceInfo.length == 0) {
			msg = msgHolder.get(errorCode);
		} else {
			msg = MessageFormat.format(msgHolder.get(errorCode), replaceInfo);
		}
		return msg;
	}

	public String[] getReplaceInfo() {
		return replaceInfo;
	}

	public void setReplaceInfo(String[] replaceInfo) {
		this.replaceInfo = replaceInfo;
	}
}
