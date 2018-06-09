package com.taobao.jingwei.webconsole.web.module.screen;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.taobao.jingwei.webconsole.biz.ao.JingweiGroupAO;
import com.taobao.jingwei.webconsole.biz.ao.JingweiServerAO;

public class JingweiDeployTask {
	@Autowired
	private JingweiServerAO jwServerAO;

	@Autowired
	private JingweiGroupAO jwGroupAO;

	@Autowired
	private HttpServletResponse response;

	public void execute(Context context, @Param(name = "host") String host,
			@Param(name = "serverName") String serverName) {

		Set<String> serverTasks = jwServerAO.getTaskNames(serverName, host);

		// 任务列表
		Set<String> tasks = jwServerAO.getBuildinTaskNames(host);

		tasks.removeAll(serverTasks);

		// 如果任务是group类型的，则删除掉
		Set<String> allGroupTasks = jwGroupAO.getAllGroupTasks(host);
		Set<String> removeGroupTask = new TreeSet<String>();
		for (String task : tasks) {
			if (!allGroupTasks.contains(task)) {
				removeGroupTask.add(task);
			}
		}

		PrintWriter writer = null;
		JSONObject jsonObj = new JSONObject();

		JSONArray jsonArray = new JSONArray(removeGroupTask);

		try {
			jsonObj.put("candidateTasks", jsonArray);
			writer = response.getWriter();
			response.setContentType("application/json;charset=utf-8");
			if (null != writer) {
				writer.write(jsonObj.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			if (null != writer) {
				writer.flush();
				writer.close();
			}
		}
	}
}
