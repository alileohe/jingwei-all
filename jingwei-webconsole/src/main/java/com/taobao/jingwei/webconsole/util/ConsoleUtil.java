package com.taobao.jingwei.webconsole.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.node.StatusNode;
import com.taobao.jingwei.webconsole.model.config.exception.GetHostIpException;

/**
 * 
 * @desc
 * 
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Feb 26, 20133:45:19 PM
 */
public final class ConsoleUtil {
	private static Log log = LogFactory.getLog(ConsoleUtil.class);

	/**
	 * 根据主机的名字查询主机ip
	 * 
	 * @param hostName
	 * @return
	 * @throws GetHostIpException 获取ip失败，或者指定host不存在
	 * @throws IOException 执行脚本错误
	 */
	public static String getIpFromRomoteHostName(String hostName) throws UnknownHostException, IOException {
		if (StringUtil.isBlank(hostName)) {
			throw new NullPointerException("host-name");
		}

		String ip = InetAddress.getByName(hostName).getHostAddress().toString();
		log.warn("resolve host name" + hostName + " get ip : " + ip);

		return ip;
	}

	public static String getLocalHostIp() throws UnknownHostException {
		InetAddress addr = InetAddress.getLocalHost();
		String ip = addr.getHostAddress().toString();
		return ip;
	}

	public static void main(String[] args) throws UnknownHostException {
		//System.out.println(getLocalHostIp());
		String hostName = "v175184.sqa.cm6";
		try {
			String ip = InetAddress.getByName(hostName).getHostAddress().toString();
			System.out.println(ip);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
