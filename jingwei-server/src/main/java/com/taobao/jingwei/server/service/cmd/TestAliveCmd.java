package com.taobao.jingwei.server.service.cmd;

import com.taobao.jingwei.server.service.ServiceUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

/**
 * @desc ���Է����Ƿ��ṩhttp����
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Mar 19, 2013 1:24:31 PM
 * 
 */
public class TestAliveCmd implements HttpCmd {

	public static final String CMD_STR = "testAlive";

	/** �൱��200 */
	public static final String OK_STR = "ok";

	private static Log log = LogFactory.getLog(TestAliveCmd.class);

	/** netty http request */
	protected final HttpRequest request;

	/** netty http response */
	protected final HttpResponse response;

	public TestAliveCmd(HttpRequest request, HttpResponse response) {
		this.request = request;
		this.response = response;
	}

	@Override
	public void invoke() {
		log.warn("receive request : " + request.getUri());
		ServiceUtil.write2Client(OK_STR, response);
	}

}
