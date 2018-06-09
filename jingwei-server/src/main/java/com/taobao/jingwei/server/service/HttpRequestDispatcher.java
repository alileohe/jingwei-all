package com.taobao.jingwei.server.service;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.server.config.ServerConfig;
import com.taobao.jingwei.server.service.cmd.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

/**
 * 
 * @desc ��������
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Mar 15, 2013 8:41:58 PM
 * 
 */
public class HttpRequestDispatcher {
	private static Log log = LogFactory.getLog(HttpRequestDispatcher.class);

	public static void dispatch(HttpRequest request, HttpResponse response) {

		String msg = request.getUri();

		log.warn("receive request url : " + msg);

		// ���������
		if (!msg.contains(ServerConfig.CTX_PATH)) {
			log.error("maybe request wrong uri, please check it.");
			return;
		}

		msg = msg.substring(msg.indexOf(ServerConfig.CTX_PATH) + ServerConfig.CTX_PATH.length() + 1);
		log.warn("receive request get msg : " + msg);

		int sepIndex = msg.indexOf("?");

		String action = StringUtil.EMPTY_STRING;
		if (sepIndex == -1) {
			action = msg;
		} else {
			action = msg.substring(0, sepIndex);
		}

		HttpCmd cmd = null;

		if (action.equalsIgnoreCase(TestAliveCmd.CMD_STR)) {
			cmd = new TestAliveCmd(request, response);
		} else if (action.equalsIgnoreCase(GetCustomerTarCmd.CMD_STR)) {
			cmd = new GetCustomerTarCmd(request, response);
		} else if (action.equalsIgnoreCase(GetLocatTarCmd.CMD_STR)) {
			cmd = new GetLocatTarCmd(request, response);
		} else if (action.equalsIgnoreCase(DeleteTarUnderTargetAndWorkCmd.CMD_STR)) {
			cmd = new DeleteTarUnderTargetAndWorkCmd(request, response);
		} else if (action.equalsIgnoreCase(CopyTar2BakCmd.CMD_STR)) {
			cmd = new CopyTar2BakCmd(request, response);
		} else if (action.equalsIgnoreCase(GetBakTarCmd.CMD_STR)) {
			cmd = new GetBakTarCmd(request, response);
		} else if (action.equalsIgnoreCase(DeleteBackTarCmd.CMD_STR)) {
			cmd = new DeleteBackTarCmd(request, response);
		} else if (action.equals(RevertTarCmd.CMD_STR)) {
			// ��ԭtar��
			cmd = new RevertTarCmd(request, response);
		}

		if (cmd != null) {
			cmd.invoke();
		}

	}
}
