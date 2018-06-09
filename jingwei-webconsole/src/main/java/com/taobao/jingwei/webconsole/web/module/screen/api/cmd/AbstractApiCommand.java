package com.taobao.jingwei.webconsole.web.module.screen.api.cmd;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @desc 处理http请求的抽象command
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * @date Mar 23, 2013 10:22:15 AM
 */
public abstract class AbstractApiCommand implements ApiCommand, IRequestParamContant {

	protected final Log log = LogFactory.getLog(AbstractApiCommand.class);

	protected HttpServletRequest request;

	protected HttpServletResponse response;

	/** server开服务的端口 */
	public static final String SERVER_PORT = "9090";

	/** 斜线 */
	public static final String SLASH = "/";

	/** 冒号 */
	public static final String COLON = ":";

	/** 问号 */
	public static final String QUESTION_MARK = "?";

	/** 等号 */
	public static final String EQUAL_MARK = "=";

	/** 与 */
	public static final String AND_MARK = "&";

	/** 空格 */
	public static final String BLANK = " ";

	/** jingwei-server-api */
	public static final String JINGWEI_SERVER_API = "jingwei-server-api";

	public AbstractApiCommand(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}
}
