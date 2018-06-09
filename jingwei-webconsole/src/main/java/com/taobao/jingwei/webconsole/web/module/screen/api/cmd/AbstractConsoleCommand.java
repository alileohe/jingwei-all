package com.taobao.jingwei.webconsole.web.module.screen.api.cmd;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.taobao.jingwei.webconsole.web.module.screen.api.JsonUtil;

/**
 * @desc console本地处理的命令
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * @date Mar 23, 2013 10:34:49 AM
 */
public abstract class AbstractConsoleCommand extends AbstractApiCommand {

	/** mysql binlog extractor prop key user */
	public static final String DBSYNC_USER_PROP_KEY = "replicator.global.db.user";

	/** mysql binlog extractor prop key password */
	public static final String DBSYNC_PASSWORD_PROP_KEY = "replicator.global.db.password";

	/** mysql binlog extractor prop key charset */
	public static final String DBSYNC_AUTO_SWITCH_CHARSET_PROP_KEY = "replicator.extractor.mysql.charset";

	/** 库名表达式 */
	public static final String DBSYNC_SCHEMA_REGEX_PROP_KEY = "replicator.global.filter.dbRegex";

	/** 表名表达式 */
	public static final String DBSYNC_TABLE_REGEX_PROP_KEY = "replicator.global.filter.tabRegex";

	/** json结果 是否成功标记 */
	public static final String IS_SUCCESS = "isSuccess";

	/** json结果 失败原因 */
	public static final String CAUSE = "cause";

	/** 多个结果 内容key */
	public static final String CONTENT = "content";

	/** task name key */
	public static final String TAR_NAME_KEY = "taskName";

	public AbstractConsoleCommand(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	@Override
	public void invoke() throws JSONException {

		// 精卫的 zookeeper 环境标识
		String hostIndex = request.getParameter("host");

		JSONObject json = new JSONObject();
		// 遇到异常直接返回
		boolean success = this.process(json, hostIndex);

		// 如果失败则返回
		if (!success) {
			if (log.isInfoEnabled()) {
				log.info("pocess cmd failed : " + request.getQueryString());
			}
			JsonUtil.writeJson2Client(json, response);
			return;
		}

		json.put("isSuccess", true);
		this.success(json);

		JsonUtil.writeJson2Client(json, response);
	}

	/**
	 * @param json 返回的json对象
	 * @Param zookeeper 环境标识
	 * @return <code>true</code>表示处理成功,<code>false</code>表示处理失败
	 */
	public abstract boolean process(JSONObject json, String hostIndex) throws JSONException;

	public abstract void success(JSONObject json) throws JSONException;
}
