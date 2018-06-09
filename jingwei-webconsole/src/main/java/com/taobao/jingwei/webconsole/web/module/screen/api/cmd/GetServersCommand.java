package com.taobao.jingwei.webconsole.web.module.screen.api.cmd;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jodd.util.Wildcard;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import com.taobao.jingwei.webconsole.biz.ao.JingweiServerAO;

/**
 * @author shuohai.lhl@taobao.com
 *
 * @time Jun 9, 2013 10:48:17 AM
 *
 * @desc 获取组server名
 */
public class GetServersCommand extends AbstractConsoleCommand {
	private static final Log log = LogFactory.getLog(GetServersCommand.class);
	/** command */
	public static final String CMD_STR = "getServersCommand";

	// 返回结果最大数量
	private static final String MAX_RESULT = "maxResult";

	private static final String CONTENT_LITERAL = "content";

	private JingweiServerAO jingweiServerAO;

	public GetServersCommand() {
		super(null, null);
	}

	public GetServersCommand(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	@Override
	public boolean process(JSONObject json, String hostIndex) throws JSONException {

		// HOST_NAME_PARAM = "hostName"
		String serverName = request.getParameter(RESOURCE_NAME_PARAM);

		int maxResult = Integer.valueOf(request.getParameter(MAX_RESULT));

		Set<String> servers = this.jingweiServerAO.getServerNames(hostIndex);
		Set<String> resultServers = new TreeSet<String>();

		// 过滤掉非法字符，有的任务含有$符号
		Iterator<String> it = servers.iterator();
		while (it.hasNext()) {
			String e = it.next();
			if (e.contains("$") || !Wildcard.match(e, serverName)) {
				it.remove();
			}
		}

		if (maxResult == -1) {
			json.put(CONTENT_LITERAL, servers);

		} else {
			if (resultServers.size() <= maxResult) {
				json.put(CONTENT_LITERAL, servers);
			} else {
				for (String e : servers) {
					resultServers.add(e);
				}
				json.put(CONTENT_LITERAL, resultServers);
			}
		}

		log.warn("get groups : " + resultServers);

		return true;
	}

	@Override
	public void success(JSONObject json) throws JSONException {

	}

	public JingweiServerAO getJingweiServerAO() {
		return jingweiServerAO;
	}

	public void setJingweiServerAO(JingweiServerAO jingweiServerAO) {
		this.jingweiServerAO = jingweiServerAO;
	}

}
