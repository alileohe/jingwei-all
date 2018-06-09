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

import com.taobao.jingwei.webconsole.biz.ao.JingweiGroupAO;

/**
 * @author shuohai.lhl@taobao.com
 *
 * @time Jun 9, 2013 10:48:17 AM
 *
 * @desc 获取组group名
 */
public class GetGroupsCommand extends AbstractConsoleCommand {
	private static final Log log = LogFactory.getLog(GetGroupsCommand.class);
	/** command */
	public static final String CMD_STR = "getGroupsCommand";

	// 返回结果最大数量
	private static final String MAX_RESULT = "maxResult";

	private static final String CONTENT_LITERAL = "content";

	private JingweiGroupAO jingweiGroupAO;

	public GetGroupsCommand() {
		super(null, null);
	}

	public GetGroupsCommand(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	@Override
	public boolean process(JSONObject json, String hostIndex) throws JSONException {

		// GROUP_NAME_PARAM = "resourceName"
		String groupName = request.getParameter(RESOURCE_NAME_PARAM);

		int maxResult = Integer.valueOf(request.getParameter(MAX_RESULT));

		Set<String> groups = this.jingweiGroupAO.getGroups(hostIndex);
		Set<String> resultGroups = new TreeSet<String>();

		// 过滤掉非法字符，有的任务含有$符号
		Iterator<String> it = groups.iterator();
		while (it.hasNext()) {
			String e = it.next();
			if (e.contains("$") || !Wildcard.match(e,  groupName )) {
				it.remove();
			}
		}

		if (maxResult == -1) {
			json.put(CONTENT_LITERAL, groups);

		} else {
			if (resultGroups.size() <= maxResult) {
				json.put(CONTENT_LITERAL, groups);
			} else {
				for (String e : groups) {
					resultGroups.add(e);
				}
				json.put(CONTENT_LITERAL, resultGroups);
			}
		}

		log.warn("get groups : " + resultGroups);

		return true;
	}

	@Override
	public void success(JSONObject json) throws JSONException {

	}

	public JingweiGroupAO getJingweiGroupAO() {
		return jingweiGroupAO;
	}

	public void setJingweiGroupAO(JingweiGroupAO jingweiGroupAO) {
		this.jingweiGroupAO = jingweiGroupAO;
	}

}
