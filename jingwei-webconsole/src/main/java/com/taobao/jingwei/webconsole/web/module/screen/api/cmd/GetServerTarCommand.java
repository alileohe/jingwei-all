package com.taobao.jingwei.webconsole.web.module.screen.api.cmd;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.taobao.jingwei.webconsole.biz.ao.JingweiServerAO;

/**
 * @desc ��ָ����server(hostName��ʾ)���󣬲�ѯ�����ж���tar e.g. // http://10.13.43.86/jingwei/api /JingweiGateWay.do?act=getServerTar&hostName
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Feb 26, 20134:07:52 PM
 */
public class GetServerTarCommand extends DirectServerCommand {

	/** ��ȡָ��server�����ϵ�tar */
	public static final String CMD_STR = "getServerTar";

	/** ����serverִ�е����� */
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
