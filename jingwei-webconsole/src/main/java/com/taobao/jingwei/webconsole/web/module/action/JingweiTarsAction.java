package com.taobao.jingwei.webconsole.web.module.action;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.server.util.HttpPost;
import com.taobao.jingwei.webconsole.util.ConsoleServerHosts;
import com.taobao.jingwei.webconsole.util.ConsoleUtil;
import com.taobao.jingwei.webconsole.web.module.screen.api.cmd.DeleteBackTarCommand;
import com.taobao.jingwei.webconsole.web.module.screen.api.cmd.DeleteServerTarCommand;

/**
 * @desc
 * 
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Mar 17, 2013 2:37:29 PM
 * 
 */
public class JingweiTarsAction {
	@Autowired
	public ConsoleServerHosts consoleServerHosts;

	private static Log log = LogFactory.getLog(JingweiTaskAction.class);

	public void doDeleteServerTar(Context context, Navigator navigator, @Param(name = "host") String host,
			@Param(name = "serverNameCriteria") String serverNameCriteria, @Param(name = "pageCount") String pageCount,
			@Param(name = "currentPage") String currentPage, @Param(name = "pageSizeInt") String pageSizeInt,
			@Param(name = "serverName") String serverName, @Param(name = "tarName") String tarName,
			@Param(name = "type") String type) {

		String targetServerIp = StringUtil.EMPTY_STRING;

		try {
			targetServerIp = ConsoleUtil.getIpFromRomoteHostName(serverName);
		} catch (UnknownHostException e) {
			log.error(e);
			return;
		} catch (IOException e) {
			log.error(e);
			return;
		}

		String url = null;
		if (type.equalsIgnoreCase("bakTar")) {
			url = DeleteBackTarCommand.getUrl(targetServerIp, tarName);
		} else if (type.equalsIgnoreCase("tar")) {
			url = DeleteServerTarCommand.getUrl(targetServerIp, tarName);
		}

		if (StringUtil.isNotBlank(url)) {
			try {
				HttpPost.doGet(url);
			} catch (IOException e) {
				log.error(e);
			}
		}

		context.put("pageCount", pageCount);
		context.put("currentPage", currentPage);
		context.put("pageSizeInt", pageSizeInt);
		context.put("serverNameCriteria", serverNameCriteria);
	}

	/**
	 * 删除console上的tar，如果不在本机，就调用ha的另一台console的接口
	 * 
	 * @param context
	 * @param navigator
	 * @param host
	 * @param pageCount
	 * @param currentPage
	 * @param pageSizeInt
	 * @param tarName
	 * @param targetConsoleIp
	 */
	public void doDeleteTar(Context context, Navigator navigator, @Param(name = "host") String host,
			@Param(name = "pageCount") String pageCount, @Param(name = "currentPage") String currentPage,
			@Param(name = "pageSizeInt") String pageSizeInt, @Param(name = "tarName") String tarName,
			@Param(name = "targetConsoleIp") String targetConsoleIp) {

		// http://10.13.43.86/jingwei/api/JingweiGateWay.do?act=deleteTar&tarName=balbabala
		String url = "http://" + targetConsoleIp + ":" + consoleServerHosts.getConsolePort()
				+ "/jingwei/api/JingweiGateWay.do?act=deleteTar&tarName=" + tarName + "&targetConsoleIp="
				+ targetConsoleIp;

		if (StringUtil.isNotBlank(url)) {
			try {
				HttpPost.doGet(url);
			} catch (IOException e) {
				log.error(e);
			}
		}

		context.put("pageCount", pageCount);
		context.put("currentPage", currentPage);
		context.put("pageSizeInt", pageSizeInt);
	}
}
