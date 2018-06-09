package com.taobao.jingwei.webconsole.web.module.screen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import jodd.util.Wildcard;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.config.ConfigManager;
import com.taobao.jingwei.webconsole.biz.ao.JingweiGroupAO;
import com.taobao.jingwei.webconsole.biz.manager.JingweiRightManeger;
import com.taobao.jingwei.webconsole.biz.manager.JingweiZkConfigManager;
import com.taobao.jingwei.webconsole.model.JingweiAssembledGroup;
import com.taobao.jingwei.webconsole.model.JingweiGroupCriteria;
import com.taobao.jingwei.webconsole.util.DataCacheType;
import com.taobao.jingwei.webconsole.util.EnvDataCache;
import com.taobao.jingwei.webconsole.util.PageFilter;
import com.taobao.jingwei.webconsole.util.PageUtil;
import com.taobao.jingwei.webconsole.web.filter.JingweiSecurityFilter;

public class JingweiGroups implements JingWeiConstants {

	private static Log log = LogFactory.getLog(JingweiGroups.class);
	@Autowired
	private JingweiGroupAO jwGroupAO;

	@Autowired
	private JingweiZkConfigManager jwConfigManager;

	@Autowired
	private EnvDataCache envDataCache;

	@Autowired
	private JingweiRightManeger jwRightManeger;

	@Autowired
	private HttpServletRequest request;

	public void execute(Context context, @Param(name = "host") String host,
			@Param(name = "groupNameCriteria") String groupName, @Param(name = "page") String page,
			@Param(name = "pageSize") String pageSize, @Param(name = "withRights") Boolean withRights) {

		JingweiGroupCriteria criteria = new JingweiGroupCriteria();

		criteria.setGroupName(groupName);

		String zkKey = StringUtil.isNotBlank(host) ? host : JingweiZkConfigManager.getDefaultKey();

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

		// 改用新缓存
		List<String> groupNameList = envDataCache.getZkPathCache(host).get(
				DataCacheType.JingweiAssembledGroup.toString(), new PageFilter(criteria) {
					@Override
					public boolean filter(Object target) {
						try {
							JingweiGroupCriteria src = (JingweiGroupCriteria) this.getSrc();
							String groupName = (String) target;
							if (StringUtils.isBlank(src.getGroupName())) {
								return true;
							} else {
								return Wildcard.match(groupName, src.getGroupName());
							}
						} catch (Exception e) {
							log.error("page filter error, target must istanceof JingweiAssembledTask, return false!", e);
							return false;
						}
					}
				});

		List<String> pagedGroupNameList = PageUtil.pagingList(pageSizeInt, currentPage, groupNameList);

		List<JingweiAssembledGroup> groups = new ArrayList<JingweiAssembledGroup>();
		for (String groupName1 : pagedGroupNameList) {
			criteria.setGroupName(groupName1);
			groups.addAll(jwGroupAO.getJingweiAssembledGroups(criteria, zkKey));
		}

		context.put("currentPage", currentPage);
		context.put("pageCount", (groupNameList.size() % pageSizeInt != 0 ? groupNameList.size() / pageSizeInt + 1
				: groupNameList.size() / pageSizeInt));
		criteria.setGroupName(groupName);
		context.put("criteria", criteria);
		context.put("groups", groups);
		context.put("pageSizeInt", pageSizeInt);

		// 所有的task
		Set<String> allTasks = jwGroupAO.getTasks(host);

		Set<String> allGroups = jwGroupAO.getGroups(host);

		for (String group : allGroups) {
			// 这个group已经有的task
			Set<String> groupTasks = jwGroupAO.getTasks(group, host);
			allTasks.removeAll(groupTasks);
		}

		// 任务列表
		Set<String> candidates = allTasks;

		Set<String> runningTasks = new HashSet<String>();

		for (String taskName : candidates) {
			if (!this.getTaskLocksCount(jwConfigManager.getZkConfigManager(host), taskName).isEmpty()) {
				runningTasks.add(taskName);
			}
		}

		candidates.removeAll(runningTasks);

		List<String> list = new ArrayList<String>();
		list.addAll(candidates);
		Collections.sort(list);

		context.put("allTasks", list);
		context.put("host", zkKey);

		String loginNickName = (String) request.getAttribute(JingweiSecurityFilter.NICK_NAME_PARAM);
		if (jwRightManeger.getSuperUserSet().contains(loginNickName)) {
			context.put("withRights", true);
		}

	}

	/**
	 * e.g. /jingwei/tasks/**task/t-locks子节点
	 * 
	 * @param configManager
	 * @param taskName
	 * @return
	 */
	public Map<String, String> getTaskLocksCount(ConfigManager configManager, String taskName) {
		String path = this.getTaskLocksPath(taskName);
		return configManager.getChildDatas(path, null);
	}

	/**
	 * e.g. /jingwei/tasks/**task/t-locks子节点
	 * 
	 * @param groupName
	 * @param taskName
	 * @return
	 */
	private String getTaskLocksPath(String taskName) {
		StringBuilder path = new StringBuilder(JINGWEI_TASK_ROOT_PATH);

		path.append(ZK_PATH_SEP).append(taskName);
		path.append(ZK_PATH_SEP).append(JINGWEI_INSTANCE_TASK_LOCKS_NODE_NAME);

		return path.toString();
	}

}
