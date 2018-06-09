package com.taobao.jingwei.webconsole.web.module.screen.api.cmd;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.taobao.jingwei.server.service.cmd.CopyTar2BakCmd;
import com.taobao.jingwei.webconsole.biz.ao.JingweiServerAO;

/**
 * @desc http://10.232.11.143:8080/jingwei-server-api/copyTar2Bak?tarName=daily-FeedEmergencySendConverter.tar.gz
 * 
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Mar 19, 2013 11:38:09 AM
 * 
 */
public class CopyTar2BakCommand extends DirectServerCommand {

	public static final String CMD_STR = CopyTar2BakCmd.CMD_STR;

	/** 请求server执行的命令 */
	public static final String ACT_TO_SERVER = CMD_STR;

	public CopyTar2BakCommand() {

	}

	public CopyTar2BakCommand(HttpServletRequest request, HttpServletResponse response, JingweiServerAO jwServerAO) {
		super(request, response);
		super.setJwServerAO(jwServerAO);
	}

	@Override
	protected void getRedirectUrl(StringBuilder rootPath) {
		String tarName = request.getParameter(TAR_NAME_PARAM);

		rootPath.append(ACT_TO_SERVER).append(QUESTION_MARK).append("tarName=").append(tarName);
	}
}
