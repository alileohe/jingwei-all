package com.taobao.jingwei.webconsole.util;

import java.net.UnknownHostException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.jingwei.webconsole.model.config.util.ConfigUtil;

/**
 * @desc 保存所有console机器的ip
 * 
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Mar 4, 20134:55:17 PM
 */
public class ConsoleServerHosts {

	private static final Log log = LogFactory.getLog(ConsoleServerHosts.class);

	private String consoleIps;

	private List<String> serverIps;

	/** ha后面的web-console的端口 */
	private String consolePort;

	private String serverPort;

	public List<String> getServerIps() {
		return serverIps;
	}

	public void setServerIps(List<String> serverIps) {
		this.serverIps = serverIps;
	}

	/**
	 * 只有两台console,获取另一台的ip
	 * 
	 * @return <code>null</code>表示获取失败
	 */
	public String getPeerConsoleIp() {
		String localIp;
		try {
			localIp = ConsoleUtil.getLocalHostIp();
		} catch (UnknownHostException e) {
			log.error(e);
			return null;
		}
		for (String ip : serverIps) {
			if (!localIp.equals(ip)) {
				return ip;
			}
		}

		return null;
	}

	public String getConsolePort() {
		return consolePort;
	}

	public void setConsolePort(String consolePort) {
		this.consolePort = consolePort;
	}

	public String getServerPort() {
		return serverPort;
	}

	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}

	public String getConsoleIps() {
		return consoleIps;
	}

	public void setConsoleIps(String consoleIps) {
		this.setServerIps(ConfigUtil.commaSepString2List(consoleIps));
		this.consoleIps = consoleIps;
	}
}
