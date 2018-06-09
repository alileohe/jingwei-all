package com.taobao.jingwei.webconsole.biz.manager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.config.impl.zk.ZkConfig;
import com.taobao.jingwei.common.config.impl.zk.ZkConfigManager;
import com.taobao.jingwei.webconsole.common.JingweiWebConsoleConstance;

public class JingweiZkConfigManager {
	public static final String SESSION_KEY = "zkHost";

	private static final Log log = LogFactory.getLog(JingweiWebConsoleConstance.JINGWEI_LOG);
	private static Map<String/* key */, ZkConfigManager/* hosts */> hosts;
	private static List<String> index;
	private static Map<String, String> statServer;
	private static Set<String> keys;
	private String keyHosts;
	private String zkSessionTimeoutMs = "30000";
	private String zkConnectionTimeoutMs = "30000";
	private String zkStatServers = "";
	private String zkStatContextPath = "";

	public JingweiZkConfigManager(String keyHosts) {
		if (keyHosts == null) {
			throw new NullPointerException("Missing config of 'zk.server.hosts'. ZK Host can not be null");
		}
		this.keyHosts = keyHosts;
	}

	public static Set<String> getKeys() {
		return keys;
	}

	public static String getDefaultKey() {
		return "1";
	}

	public static String getHostName(int host) {
		if (host == 0) {
			return index.get(host);
		}
		return index.get(host - 1);
	}

	public static String getStatServer(int host) {
		if (host == 0) {
			return statServer.get(index.get(0));
		}
		String server = statServer.get(index.get(host - 1));
		log.info("Connect to TLog Http API address: " + server);
		return server;
	}

	public ZkConfigManager getZkConfigManager(Object key) {
		if (key == null) {
			key = keys.iterator().next();
		}
		if (NumberUtils.isDigits(key.toString())) {
			key = index.get(Integer.parseInt(key.toString()) - 1);
		}
		return hosts.get(key);
	}
	
	/**
	 * 返回child节点列表
	 * @param path
	 * @return
	 */
	public List<String> getChildList(Object key,String path) {
		
		if (StringUtil.isBlank(path)) {
			return null;
		}
		return this.getZkConfigManager(key).getZkClient().getChildren(path);
	}

	public synchronized void init() {
		String[] keyString = StringUtil.split(keyHosts, ";");
		hosts = new LinkedHashMap<String, ZkConfigManager>(keyString.length);
		index = new ArrayList<String>(keyString.length);
		for (int i = 0; i < keyString.length; i++) {
			String[] hostString = StringUtil.split(keyString[i], "|");

			if (hostString.length != 2) {
				log.error("CAN NOT parse jingwei zk server setter!");
				continue;
			}

			try {
				ZkConfig config = new ZkConfig(hostString[1]);
				config.setZkSessionTimeoutMs(Integer.parseInt(zkSessionTimeoutMs));
				config.setZkConnectionTimeoutMs(Integer.parseInt(zkConnectionTimeoutMs));
				ZkConfigManager zk = new ZkConfigManager();
				zk.setZkConfig(config);
				zk.init();
				String hostName = hostString[0];
				hosts.put(hostName, zk);
				index.add(hostName);
			} catch (Exception e) {
				log.error("Can not create connection of server: " + hostString[1], e);
			}
		}
		if (hosts.isEmpty()) {
			throw new NullPointerException("Can not create any connections from jingwei zk server host");
		}
		keys = hosts.keySet();
		String[] servers = StringUtil.split(zkStatServers, ";");
//		if (servers.length != index.size()) {
//			throw new NullPointerException("jingwei.zk.server.stat has illegal setting.");
//		}
		statServer = new LinkedHashMap<String, String>(servers.length);
		for (String server : servers) {
			String[] s = StringUtil.split(server, "|");
			statServer.put(s[0], "http://" + s[1]
					+ (zkStatContextPath.startsWith("/") ? zkStatContextPath : "/" + zkStatContextPath));
		}
	}

	public synchronized void destroy() {
		if (!hosts.isEmpty()) {
			Iterator<ZkConfigManager> iter = hosts.values().iterator();
			while (iter.hasNext()) {
				iter.next().destory();
			}
		}
	}

	public String getZkSessionTimeoutMs() {
		return zkSessionTimeoutMs;
	}

	public void setZkSessionTimeoutMs(String zkSessionTimeoutMs) {
		this.zkSessionTimeoutMs = zkSessionTimeoutMs;
	}

	public String getZkConnectionTimeoutMs() {
		return zkConnectionTimeoutMs;
	}

	public void setZkConnectionTimeoutMs(String zkConnectionTimeoutMs) {
		this.zkConnectionTimeoutMs = zkConnectionTimeoutMs;
	}

	public String getZkStatServers() {
		return zkStatServers;
	}

	public void setZkStatServers(String zkStatServers) {
		this.zkStatServers = zkStatServers;
	}

	public String getZkStatContextPath() {
		return zkStatContextPath;
	}

	public void setZkStatContextPath(String zkStatContextPath) {
		this.zkStatContextPath = zkStatContextPath;
	}
}
