package com.taobao.jingwei.webconsole.util.upload;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.server.util.HttpPost;
import com.taobao.jingwei.webconsole.biz.exception.TimeoutException;
import com.taobao.jingwei.webconsole.util.ApiErrorCode;
import com.taobao.jingwei.webconsole.util.ConsoleServerHosts;
import com.taobao.jingwei.webconsole.util.ConsoleUtil;
import com.taobao.jingwei.webconsole.web.module.screen.api.JsonUtil;

public class UploadFileImpl implements UploadFileService {

	private static final Log log = LogFactory.getLog(UploadFileImpl.class);

	// ha的其他对等console的ip列表
	public ConsoleServerHosts consoleServerHosts;

	@Override
	public List<String> getLocalFileNames(String ctxPath) {

		// 绝对路径
		List<String> tarNames = listFileNames(ctxPath);

		return tarNames;
	}

	@Override
	public List<String> getAllFileNames(String ctxPath, int timeout) {

		String localIp = this.getLocalIp();

		if (StringUtil.isBlank(localIp)) {
			return Collections.emptyList();
		}

		List<String> list = new ArrayList<String>();

		for (String ip : this.getPeerConsoleIpList()) {
			if (ip.equals(localIp)) {
				// 本地tar包
				list.addAll(this.getLocalFileNames(ctxPath));
			} else {
				if (StringUtil.isBlank(ip)) {
					continue;
				}
				// 远程tar包
				list.addAll(this.getPeerConsoleFileNames(ip, timeout));
			}
		}

		return list;
	}

	@Override
	public List<String> getPeerConsoleFileNames(String ip, int timeoutMils) {
		String urlStr = "http://" + ip + ":" + consoleServerHosts.getConsolePort()
				+ "/jingwei/api/JingweiGateWay.do?act=getLocalTars";

		String result = null;
		try {
			// 格式 {"tars":["daily-FeedEmergencySendConverter.tar.gz"],"isSuccess":true}
			result = HttpPost.doGet(urlStr, timeoutMils);
		} catch (IOException e) {
			e.printStackTrace();
			log.error(e);
		}

		if (StringUtil.isBlank(result)) {
			return Collections.emptyList();
		}

		List<String> list = new ArrayList<String>();

		JSONObject obj;
		try {

			obj = new JSONObject(result);

			Boolean isSuccess = Boolean.valueOf(obj.getBoolean("isSuccess"));

			if (isSuccess) {
				JSONArray resultArray = obj.getJSONArray("tars");

				for (int i = 0; i < resultArray.length(); i++) {
					String tar = (String) resultArray.get(i);
					list.add(tar);
				}
				return list;
			}
		} catch (JSONException e) {
			log.error("convert group string error", e);
			e.printStackTrace();
		}

		return Collections.emptyList();
	}

	@Override
	public List<UploadStatus> sendToJwServer(List<String> fileList, List<String> serverIpList) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UploadStatus> sendToJwServer(String file, List<String> serverIpList) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UploadStatus> sendToJwServer(String file, String md5, List<String> serverIpList) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<File> listSpecifFile(String path, final String suffix) {

		List<File> files = new ArrayList<File>();

		LinkedList<File> list = new LinkedList<File>();
		File dir = new File(path);
		list.add(dir);

		File tmp;
		while (!list.isEmpty()) {
			tmp = (File) list.removeFirst();

			File[] file = tmp.listFiles(new FileFilter() {
				public boolean accept(File pathname) {
					return pathname.getName().endsWith(suffix);
				}
			});

			if (file == null) {
				continue;
			}
			for (int i = 0; i < file.length; i++) {
				if (file[i].isDirectory())
					list.add(file[i]);
				else
					System.out.println(file[i].getAbsolutePath());
				files.add(file[i]);
			}
		}

		return files;
	}

	/**
	 * 获取所有tar.gz的包
	 * 
	 * @param path
	 * @param suffix
	 * @return
	 */
	private List<String> listFileNames(String path) {
		List<String> tarNames = new ArrayList<String>();
		List<File> tars = this.listSpecifFile(path, "tar.gz");
		for (File f : tars) {
			tarNames.add(f.getName());
		}

		return tarNames;
	}

	public List<String> getPeerConsoleIpList() {
		return this.consoleServerHosts.getServerIps();
	}

	@Override
	public Map<String, String> getAllFilesMap2Ip(String parentPath, int timeoutMils) {
		String localIp = this.getLocalIp();

		if (StringUtil.isBlank(localIp)) {
			return Collections.emptyMap();
		}

		Map<String, String> map = new TreeMap<String, String>();

		for (String ip : this.getPeerConsoleIpList()) {
			List<String> files = Collections.emptyList();
			if (ip.equals(localIp)) {
				// 本地tar包
				files = this.getLocalFileNames(parentPath);
			} else {
				if (StringUtil.isBlank(ip)) {
					continue;
				}
				// 远程tar包
				files = this.getPeerConsoleFileNames(ip, timeoutMils);
			}
			for (String f : files) {
				map.put(f, ip);
			}
		}

		return map;
	}

	private String getLocalIp() {
		String localIp = null;
		try {
			localIp = InetAddress.getLocalHost().getHostAddress().toString();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			log.error(e);
		}
		return localIp;
	}

	public ConsoleServerHosts getConsoleServerHosts() {
		return consoleServerHosts;
	}

	public void setConsoleServerHosts(ConsoleServerHosts consoleServerHosts) {
		this.consoleServerHosts = consoleServerHosts;
	}

	@Override
	public Boolean delPeerConsoleFile(String ip, int port, String tar, int timeoutMils) throws TimeoutException {

		// 去另一台机器上删除文件

		// http://10.13.43.86:8080/jingwei/api/JingweiGateWay.do?act=deleteTar&tarName=balbabala&targetConsoleIp=XXX
		boolean success = false;
		String url = this.getDeletePeerConsoleUrl(ip, port, tar);
		try {
			String result = HttpPost.doGet(url);

			JSONObject obj = new JSONObject(result);

			success = obj.getBoolean("isSuccess");

		} catch (IOException e) {
			log.error("delete peer file failed " + tar, e);
			throw new TimeoutException("delete peer file failed " + tar);
		} catch (JSONException e) {
			log.error("json exception. delete peer file failed " + tar, e);
			success = false;
		}

		if (success) {
			return true;
		} else {
			return false;
		}
	}

	/** 要删除的人tar所在的机器 */
	public static final String TARGET_CONSOLE_IP = "targetConsoleIp";

	private String getDeletePeerConsoleUrl(String ip, int port, String tar) {
		StringBuilder sb = new StringBuilder("http://");
		sb.append(ip).append(":").append(port).append("/jingwei/api/JingweiGateWay.do?act=deleteTar&")
				.append("tarName=").append(tar);
		sb.append("&").append(TARGET_CONSOLE_IP).append("=").append(ip);
		return sb.toString();
	}

}
