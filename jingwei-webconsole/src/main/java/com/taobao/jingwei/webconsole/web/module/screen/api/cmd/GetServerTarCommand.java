package com.taobao.jingwei.webconsole.web.module.screen.api.cmd;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.taobao.jingwei.webconsole.biz.ao.JingweiServerAO;

/**
 * @desc 向指定的server(hostName表示)请求，查询上面有多少tar e.g. // http://10.13.43.86/jingwei/api /JingweiGateWay.do?act=getServerTar&hostName
 * 
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Feb 26, 20134:07:52 PM
 */
public class GetServerTarCommand extends DirectServerCommand {

	/** 获取指定server机器上的tar */
	public static final String CMD_STR = "getServerTar";

	/** 请求server执行的命令 */
	public static final String ACT_TO_SERVER = "getLocalTar";

	public GetServerTarCommand() {
	}

	public GetServerTarCommand(HttpServletRequest request, HttpServletResponse response, JingweiServerAO jwServerAO) {
		super(request, response);
		super.setJwServerAO(jwServerAO);
	}

	@Override
	protected void getRedirectUrl(StringBuilder rootPath) {
		rootPath.append(ACT_TO_SERVER);
	}

}
