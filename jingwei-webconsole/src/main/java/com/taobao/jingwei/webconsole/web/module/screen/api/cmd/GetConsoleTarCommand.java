package com.taobao.jingwei.webconsole.web.module.screen.api.cmd;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.taobao.jingwei.webconsole.biz.ao.JingweiServerAO;
import com.taobao.jingwei.webconsole.util.ConsoleServerHosts;

/**
 * 
 * @desc ��server��������serverִ�нű�����console��tar ���͵����� e.g. http://10.232.11.143:8080/
 *       jingwei-server-api/getTar?path=10.13.43.86/jingwei /uploads/tars/DAILY-UNION-CPS-XO.tar.gz <li>10.232.11.143 ����
 *       jingwei-server��ip <li>10.13.43.86 ���� ���� console��web-server��ip <li>DAILY-UNION-CPS-XO.tar.gz ���� Ҫ�����tar���� ���յ���uri e.g.
 *       http://10.13.43.86/jingwei /api/JingweiGateWay.do?act=publishTar&tarName= DAILY-UNION-CPS-XO.tar.gz&hostName=
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Mar 11, 2013 12:06:55 PM
 * 
 */
public class GetConsoleTarCommand extends DirectServerCommand {

	public static final String CMD_STR = "publishTar";

	/** ����serverִ�е����� */
	public static final String ACT_TO_SERVER = "getTar";

	public static final String TARGET_CONSOLE_IP = "targetConsoleIp";

	private ConsoleServerHosts consoleServerHosts;

	public GetConsoleTarCommand() {

	}

	public GetConsoleTarCommand(HttpServletRequest request, HttpServletResponse response, JingweiServerAO jwServerAO) {
		super(request, response);
		super.setJwServerAO(jwServerAO);
	}

	/**
	 * e.g. http://10.13.43.86/jingwei/api/JingweiGateWay.do
	 * 
	 * @return
	 */
	private String getConsoleIp() {
		return request.getParameter(TARGET_CONSOLE_IP);
	}

	@Override
	protected void getRedirectUrl(StringBuilder rootPath) {
		String tarName = request.getParameter(TAR_NAME_PARAM);

		String consoleIp = this.getConsoleIp();

		rootPath.append(ACT_TO_SERVER).append(QUESTION_MARK).append("path=").append(consoleIp).append(":")
				.append(consoleServerHosts.getConsolePort()).append("/jingwei/uploads/tars/").append(tarName)
				.append("&fileName=").append(tarName);

	}

	public ConsoleServerHosts getConsoleServerHosts() {
		return consoleServerHosts;
	}

	public void setConsoleServerHosts(ConsoleServerHosts consoleServerHosts) {
		this.consoleServerHosts = consoleServerHosts;
	}
}
