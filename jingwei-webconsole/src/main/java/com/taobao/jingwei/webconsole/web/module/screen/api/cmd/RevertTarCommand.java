package com.taobao.jingwei.webconsole.web.module.screen.api.cmd;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.taobao.jingwei.server.service.cmd.RevertTarCmd;

/**
 * @desc 请求server还原tar包
 * 
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Apr 19, 2013 12:34:37 PM
 * 
 */
public class RevertTarCommand extends DirectServerCommand {
	public static final String CMD_STR = RevertTarCmd.CMD_STR;

	/** 请求server执行的命令 */
	public static final String ACT_TO_SERVER = CMD_STR;

	public RevertTarCommand() {
	}

	public RevertTarCommand(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	@Override
	protected void getRedirectUrl(StringBuilder rootPath) {

		String queryStr = request.getQueryString();
		String bakFileName = request.getParameter(RevertTarCmd.BAK_FILT_NAME_PARAM);

		rootPath.append(ACT_TO_SERVER).append(QUESTION_MARK).append(RevertTarCmd.BAK_FILT_NAME_PARAM)
				.append(EQUAL_MARK).append(bakFileName);
	}

}
