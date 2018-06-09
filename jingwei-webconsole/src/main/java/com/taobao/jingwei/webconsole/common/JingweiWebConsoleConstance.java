package com.taobao.jingwei.webconsole.common;


public interface JingweiWebConsoleConstance {
	/**
	 * log 名称
	 */
	static String JINGWEI_LOG = "JINGWEI_LOG";

	static String NOT_JINGWEI_TASK = "该任务不是由精卫控制台创建，修改可能会导致不可预期的错误！";

	/**
	 * 默认每页显示条目数量：20
	 */
	static int JINGWEI_DEFAULT_ITEM_CNT = 20;

	static String DEFAULT_ERROR_ITEM = "——";

	static String DEFAULT_SHOW_STATUS = "——";

	static String COMMA = ",";

	static String DATE_FORMATOR = "yyyy-MM-dd HH:mm:ss";
	static String DATE_FORMATOR_WITHOUT_SECONDS = "yyyy-MM-dd HH:mm";
	static String DATE_SHORT_FORMATOR = "yyyy-MM-dd";
}
