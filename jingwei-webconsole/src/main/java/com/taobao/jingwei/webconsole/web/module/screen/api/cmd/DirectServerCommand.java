package com.taobao.jingwei.webconsole.web.module.screen.api.cmd;

import java.io.IOException;
import java.text.MessageFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.node.StatusNode;
import com.taobao.jingwei.server.config.ServerConfig;
import com.taobao.jingwei.server.service.cmd.TestAliveCmd;
import com.taobao.jingwei.server.util.HttpPost;
import com.taobao.jingwei.webconsole.biz.ao.JingweiServerAO;
import com.taobao.jingwei.webconsole.util.ApiErrorCode;
import com.taobao.jingwei.webconsole.util.ConsoleUtil;
import com.taobao.jingwei.webconsole.web.module.screen.api.JsonUtil;

/**
 * @desc 把对console的请求转发给jingwei-server
 * 
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Mar 19, 2013 11:50:12 AM
 * 
 */
public abstract class DirectServerCommand extends AbstractApiCommand {
	private static final Log log = LogFactory.getLog(DirectServerCommand.class);

	/** 参数hostName */
	public static final String HOST_NAME_PARAM = "hostName";

	/** 参数zookeeper列表，通过zeekeeper获取状态 */
	public static final String ZK_KEY = "zkKey";

	/** 精卫server context root path partten */
	// http://10.232.11.143:8080/jingwei-server-api/
	public static final String JINGWEI_SERVER_CXT_ROOT_PATH = "http://{0}:" + ServerConfig.DEFAULT_SERVICE_PORT + "/"
			+ ServerConfig.CTX_PATH + "/";

	/** 测试精卫server是否好使的url */
	// http://10.232.11.143:8080/jingwei-server-api/testAlive
	public static final String TEST_URL_PATTERN = JINGWEI_SERVER_CXT_ROOT_PATH + TestAliveCmd.CMD_STR;

	private static final int REQUEST_SERVER_TIMEOUT = 20000;

	/** 用于获取server相关信息 */
	private JingweiServerAO jwServerAO;

	public DirectServerCommand() {
		super(null, null);
	}

	public DirectServerCommand(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	public DirectServerCommand(HttpServletRequest request, HttpServletResponse response, JingweiServerAO jwServerAO) {
		super(request, response);
	}

	@Override
	public void invoke() {
		String hostName = request.getParameter(HOST_NAME_PARAM);
		if (!this.checkHostNameParam(hostName)) {
			log.warn("request param hostName is empty.");
			JsonUtil.writeFailed2Response(ApiErrorCode.SERVER_PARAM_IS_EMPTY, response);
			return;
		}

		String targetIp = this.getTargetServerIp(hostName);
		if (StringUtil.isBlank(targetIp)) {
			log.warn("get host ip from host'name error : " + hostName);
			return;
		}

		if (!this.checkJingweiServerStatus(hostName, targetIp)) {
			log.warn("jingwei server in under stop status : " + hostName);
			return;
		}

		// http://10.232.11.143:8080/jingwei-server-api/
		String ctxRootPath = MessageFormat.format(JINGWEI_SERVER_CXT_ROOT_PATH, targetIp);
		StringBuilder sb = new StringBuilder(ctxRootPath);

		this.getRedirectUrl(sb);

		String redirectUrl = sb.toString();

		try {
			log.warn("send request to server : " + redirectUrl);
			String result = HttpPost.doGet(redirectUrl, 20000);
			log.warn("get result : " + result);
			JsonUtil.writeStr2Client(result, response);
		} catch (IOException e) {
			log.error(e);
			JsonUtil.writeFailed2Response(ApiErrorCode.SERVER_IS_NOT_ALIVE, response);
		}
	}

	/**
	 * 
	 * @param rootPath
	 */
	protected abstract void getRedirectUrl(StringBuilder rootPath);

	/**
	 * 检查server是否可用
	 * 
	 * @return <code>true</code>表示server是running; <code>false</code> 表示server是stop状态
	 */
	public boolean checkJingweiServerStatus(String hostName, String targetIp) {

		String zkKey = this.request.getParameter(ZK_KEY);

		// 不判断是否可用了，请求不到结果则认为失败即可（不使用zk获取server状态了）
		if (StringUtil.isBlank(zkKey)) {
			try {

				String url = MessageFormat.format(TEST_URL_PATTERN, targetIp);
				String ok = HttpPost.doGet(url);

				if (StringUtil.equalsIgnoreCase(ok, TestAliveCmd.OK_STR)) {
					return true;
				} else {
					JsonUtil.writeFailed2Response(ApiErrorCode.SERVER_IS_NOT_ALIVE, response);
					return false;
				}
			} catch (IOException e) {
				JsonUtil.writeFailed2Response(ApiErrorCode.SERVER_IS_NOT_ALIVE, response);
				return false;
			}
		} else {
			StatusNode node = jwServerAO.getServerStatus(hostName, zkKey);
			if (null == node) {
				JsonUtil.writeFailed2Response(ApiErrorCode.SERVER_IS_STOP, response);
				return false;
			}
		}

		return true;
	}

	/**
	 * 检查参数是否为空或null
	 * 
	 * @param hostName
	 * @return <code>true</code>表示参数正常; <code>false</code> 表示参数为空或者null
	 */
	public boolean checkHostNameParam(String hostName) {

		if (StringUtil.isBlank(hostName)) {
			JsonUtil.writeFailed2Response(ApiErrorCode.SERVER_PARAM_IS_EMPTY, response);
			return false;
		}
		return true;
	}

	/**
	 * 获取serve的ip
	 * 
	 * @param hostName
	 * @return 空字符串表示失败
	 */
	public String getTargetServerIp(String hostName) {
		String targetServerIp = StringUtil.EMPTY_STRING;

		try {
			targetServerIp = ConsoleUtil.getIpFromRomoteHostName(hostName);
		} catch (IOException e) {
			JsonUtil.writeFailed2Response(ApiErrorCode.GET_SERVER_IP_FAILED, response);
		}

		return targetServerIp;
	}

	public JingweiServerAO getJwServerAO() {
		return jwServerAO;
	}

	public void setJwServerAO(JingweiServerAO jwServerAO) {
		this.jwServerAO = jwServerAO;
	}

}
