package com.taobao.jingwei.webconsole.web.module.screen;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jodd.util.Wildcard;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.node.StatusNode;
import com.taobao.jingwei.common.node.StatusNode.StatusEnum;
import com.taobao.jingwei.server.util.HttpPost;
import com.taobao.jingwei.webconsole.biz.ao.JingweiServerAO;
import com.taobao.jingwei.webconsole.biz.dao.JwUserDao;
import com.taobao.jingwei.webconsole.biz.manager.JingweiZkConfigManager;
import com.taobao.jingwei.webconsole.model.JingweiServerCriteria;
import com.taobao.jingwei.webconsole.util.ConsoleUtil;
import com.taobao.jingwei.webconsole.util.DataCacheType;
import com.taobao.jingwei.webconsole.util.EnvDataCache;
import com.taobao.jingwei.webconsole.util.PageFilter;
import com.taobao.jingwei.webconsole.util.PageUtil;
import com.taobao.jingwei.webconsole.web.module.screen.api.cmd.DirectServerCommand;
import com.taobao.jingwei.webconsole.web.module.screen.api.cmd.GetBakTarCommand;
import com.taobao.jingwei.webconsole.web.module.screen.api.cmd.GetServerTarCommand;

/**
 * @desc 显示每个server上的自定义tar
 * 
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Mar 17, 2013 12:12:21 PM
 * 
 */
public class JingweiTars {
	private Log log = LogFactory.getLog(JingweiTars.class);

	@Autowired
	private JingweiServerAO jwServerAO;

	@Autowired
	private EnvDataCache envDataCache;

	@Autowired
	private HttpServletResponse response;

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private JwUserDao jwUserDao;

	public void execute(Context context, @Param(name = "host") String host, @Param(name = "page") String page,
			@Param(name = "pageSize") String pageSize, @Param(name = "serverNameCriteria") String serverNameCriteria) {


		JingweiServerCriteria criteria = new JingweiServerCriteria();

		if (StringUtils.isNotBlank((String) context.get("groupCriteria"))) {
			criteria.setGroupType((String) context.get("groupCriteria"));
		}

		if (StringUtils.isNotBlank((String) context.get("criteriaServerName"))) {
			criteria.setServerName((String) context.get("criteriaServerName"));
		}

		if (StringUtils.isNotBlank((String) context.get("criteriaTaskName"))) {
			criteria.setTaskName((String) context.get("criteriaTaskName"));
		}

		String zkKey = StringUtil.isNotBlank(host) ? host : JingweiZkConfigManager.getDefaultKey();
		context.put("host", zkKey);
		int currentPage;
		if (StringUtils.isNumeric(page)) {
			currentPage = StringUtils.isBlank(page) ? 1 : Integer.parseInt(page);
		} else {
			currentPage = 1;
		}

		int pageSizeInt = StringUtils.isBlank(pageSize) ? PageUtil.DEFAULT_PAGE_SIZE : Integer.parseInt(pageSize);

		if (StringUtils.isNotBlank((String) context.get("currentPage"))) {
			currentPage = Integer.valueOf((String) context.get("currentPage"));
		}

		if (StringUtils.isNotBlank((String) context.get("pageSizeInt"))) {
			pageSizeInt = Integer.valueOf((String) context.get("pageSizeInt"));
		}

		String actionPassServerNameCriteria = (String) context.get("serverNameCriteria");

		final String serverCriteria = StringUtils.isBlank(actionPassServerNameCriteria) ? serverNameCriteria
				: actionPassServerNameCriteria;
		List<String> serverAll = envDataCache.getZkPathCache(host).get(DataCacheType.JingweiAssembledServer.toString(),
				new PageFilter(criteria) {
					@Override
					public boolean filter(Object target) {
						try {

							String serverName = (String) target;
							if (StringUtils.isBlank(serverCriteria)) {
								return true;
							} else {
								return Wildcard.match(serverName, serverCriteria);
							}
						} catch (Exception e) {
							log.error("page filter error, target must istanceof JingweiAssembledTask, return false!");
							e.printStackTrace();
							return false;
						}
					}
				});

		List<String> pageServers = PageUtil.pagingList(pageSizeInt, currentPage, serverAll);
		context.put("pageCount",
				(serverAll.size() % pageSizeInt != 0 ? serverAll.size() / pageSizeInt + 1 : serverAll.size()
						/ pageSizeInt));

		List<ServerTarResult> srevers = new ArrayList<ServerTarResult>();

		for (String serverName : pageServers) {
			srevers.add(this.getServerTars(serverName, zkKey));
		}

		context.put("currentPage", currentPage);
		context.put("pageSizeInt", pageSizeInt);
		context.put("criteria", criteria);

		context.put("serverNameCriteria", serverCriteria);
		context.put("servers", srevers);
	}

	private ServerTarResult getServerTars(String serverName, String zkKey) {
		//	http://10.13.43.86/jingwei/api/JingweiGateWay.do?act=getServerTar&hostName
		StatusNode node = jwServerAO.getServerStatus(serverName, zkKey);

		ServerTarResult result = new ServerTarResult();
		result.setServerName(serverName);

		if (null != node) {
			result.setStatus(StatusEnum.RUNNING.toString());
		} else {
			result.setStatus("stop");
		}

		List<String> targetTars = this.getTars(true, serverName);
		List<String> bakTars = this.getTars(false, serverName);

		result.setBakTars(bakTars);
		result.setTars(targetTars);

		return result;
	}

	/**
	 * 
	 * @param targetOrBak <code>true</code>表示target的，<code>false</code>表示bak的tar
	 * @return
	 */
	private List<String> getTars(Boolean targetOrBak, String hostName) {

		String targetIp = null;
		try {
			targetIp = ConsoleUtil.getIpFromRomoteHostName(hostName);
		} catch (IOException e) {
			log.error("get local host ip error!" + e);
		}

		if (StringUtils.isBlank(targetIp)) {
			return Collections.emptyList();
		}

		String ctxRootPath = MessageFormat.format(DirectServerCommand.JINGWEI_SERVER_CXT_ROOT_PATH, targetIp);
		if (targetOrBak) {
			ctxRootPath += GetServerTarCommand.ACT_TO_SERVER;
		} else {
			ctxRootPath += GetBakTarCommand.ACT_TO_SERVER;
		}

		String json = null;
		try {
			json = HttpPost.doGet(ctxRootPath);
		} catch (IOException e) {
			log.error(e);
			return Collections.emptyList();
		}

		List<String> tars = new ArrayList<String>();
		if (StringUtils.isNotBlank(json)) {
			try {
				JSONObject obj = new JSONObject(json);

				Boolean success = obj.getBoolean("isSuccess");

				if (success == Boolean.TRUE) {
					JSONArray array = obj.getJSONArray("tars");

					if (null != array) {
						for (int i = 0; i < array.length(); i++) {
							tars.add(array.getString(i));
						}

					}
				}
			} catch (JSONException e) {
				log.error(e);
			}
		}

		return tars;
	}

	public static class ServerTarResult {

		private String serverName;

		// server 上的tar
		private List<String> tars;

		// server 上bak的tar
		private List<String> bakTars;

		// server 是否是运行状态
		private String status;

		public List<String> getTars() {
			return tars;
		}

		public void setTars(List<String> tars) {
			this.tars = tars;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getServerName() {
			return serverName;
		}

		public void setServerName(String serverName) {
			this.serverName = serverName;
		}

		public List<String> getBakTars() {
			return bakTars;
		}

		public void setBakTars(List<String> bakTars) {
			this.bakTars = bakTars;
		}
	}
}
