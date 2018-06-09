package com.taobao.jingwei.webconsole.web.module.screen.api.cmd;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.taobao.jingwei.server.service.cmd.DeleteBackTarCmd;
import com.taobao.jingwei.webconsole.biz.ao.JingweiServerAO;

/**
 * @desc 删除server的备份的tar
 * 
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Mar 19, 2013 11:37:50 AM
 * 
 */
public class DeleteBackTarCommand extends DirectServerCommand {

	public static final String CMD_STR = DeleteBackTarCmd.CMD_STR;

	/** 请求server执行的命令 */
	public static final String ACT_TO_SERVER = CMD_STR;

	public DeleteBackTarCommand() {

	}

	public DeleteBackTarCommand(HttpServletRequest request, HttpServletResponse response, JingweiServerAO jwServerAO) {
		super(request, response);
		super.setJwServerAO(jwServerAO);
	}

	@Override
	protected void getRedirectUrl(StringBuilder rootPath) {
		String tarName = request.getParameter(TAR_NAME_PARAM);

		rootPath.append(ACT_TO_SERVER).append(QUESTION_MARK).append("tarName=").append(tarName);

	}
	
	/**
	 * http://10.232.11.143:8080/jingwei-server-api/deleteServerTar?tarName=xx. tar.gz
	 * 
	 * @param targetIp
	 */
	public static String getUrl(String targetServerIp, String tarName) {
		StringBuilder sb = new StringBuilder("http://").append(targetServerIp).append(COLON).append(SERVER_PORT);
		sb.append(SLASH).append(JINGWEI_SERVER_API).append(SLASH).append(ACT_TO_SERVER).append(QUESTION_MARK);
		sb.append(TAR_NAME_PARAM).append(EQUAL_MARK).append(tarName);

		return sb.toString();
	}
}
