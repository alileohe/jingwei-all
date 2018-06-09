package com.taobao.jingwei.webconsole.web.module.screen.api.cmd;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.taobao.jingwei.server.service.cmd.GetBakTarCmd;
import com.taobao.jingwei.webconsole.biz.ao.JingweiServerAO;

/**
 * @desc 获取精卫server上的备份的tar
 * 
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Mar 19, 2013 11:39:10 AM
 * 
 */
public class GetBakTarCommand extends DirectServerCommand {

	public static final String CMD_STR = GetBakTarCmd.CMD_STR;

	/** 请求server执行的命令 */
	public static final String ACT_TO_SERVER = CMD_STR;

	public GetBakTarCommand() {

	}

	public GetBakTarCommand(HttpServletRequest request, HttpServletResponse response, JingweiServerAO jwServerAO) {
		super(request, response);
		super.setJwServerAO(jwServerAO);
	}

	@Override
	protected void getRedirectUrl(StringBuilder rootPath) {
		rootPath.append(ACT_TO_SERVER);
	}
}