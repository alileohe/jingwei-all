package com.taobao.jingwei.webconsole.web.module.screen.api.cmd;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import com.taobao.jingwei.webconsole.biz.ao.JingweiTaskAO;
import com.taobao.jingwei.webconsole.model.JingweiTaskCriteria;

/**
 * @author shuohai.lhl@taobao.com
 *
 * @time Jun 9, 2013 10:48:17 AM
 *
 * @desc 获取任务名
 */
public class GetTasksCommand extends AbstractConsoleCommand {
	private static final Log log = LogFactory.getLog(GetTasksCommand.class);
	/** command */
	public static final String CMD_STR = "getTasksCommand";

	// 返回结果最大数量
	private static final String MAX_RESULT = "maxResult";

	private static final String CONTENT_LITERAL = "content";

	private JingweiTaskAO jwTaskAO;

	public GetTasksCommand() {
		super(null, null);
	}

	public GetTasksCommand(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	@Override
	public boolean process(JSONObject json, String hostIndex) throws JSONException {

		// TASK_NAME_PARAM = "taskName"
		String taskName = request.getParameter(TASK_NAME_PARAM);

		int maxResult = Integer.valueOf(request.getParameter(MAX_RESULT));

		JingweiTaskCriteria criteria = new JingweiTaskCriteria();
		criteria.setTaskId(taskName);

		Set<String> tasks = this.jwTaskAO.getTaskSet(criteria, hostIndex);
		Set<String> resultTasks = new TreeSet<String>();

		// 过滤掉非法字符，有的任务含有$符号,在zookeeper上删不掉
		Iterator<String> it = tasks.iterator();
		while (it.hasNext()) {
			if (it.next().contains("$")){
				it.remove();
			}
		}

		if (maxResult == -1) {
			json.put(CONTENT_LITERAL, tasks);
		} else {
			if (resultTasks.size() <= maxResult) {
				json.put(CONTENT_LITERAL, tasks);
			} else {
				for (String e : tasks) {
					resultTasks.add(e);
				}
				json.put(CONTENT_LITERAL, resultTasks);
			}
		}

		return true;
	}

	@Override
	public void success(JSONObject json) throws JSONException {

	}

	public JingweiTaskAO getJwTaskAO() {
		return jwTaskAO;
	}

	public void setJwTaskAO(JingweiTaskAO jwTaskAO) {
		this.jwTaskAO = jwTaskAO;
	}

}
