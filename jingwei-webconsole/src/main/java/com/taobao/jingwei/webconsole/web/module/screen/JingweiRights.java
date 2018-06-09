package com.taobao.jingwei.webconsole.web.module.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.webconsole.biz.dao.JwPermissionDao;
import com.taobao.jingwei.webconsole.biz.dao.JwUserRoleDao;
import com.taobao.jingwei.webconsole.biz.dao.model.JwPermission;
import com.taobao.jingwei.webconsole.biz.dao.model.JwUserRole;
import com.taobao.jingwei.webconsole.biz.manager.JingweiRightManeger;
import com.taobao.jingwei.webconsole.biz.manager.JingweiZkConfigManager;
import com.taobao.jingwei.webconsole.web.filter.JingweiSecurityFilter;

/**
 * @author shuohai.lhl@taobao.com
 *
 * @time Jun 3, 2013 11:41:50 AM
 *
 * @desc 权限，展示角色（用户名）拥有的资源（机器名、任务名、group名）
 */
public class JingweiRights {
	@Autowired
	private HttpServletRequest request;

	@Autowired
	private HttpServletResponse response;

	@Autowired
	private JwPermissionDao jwPermissionDao;

	@Autowired
	private JwUserRoleDao jwUserRoleDao;

	@Autowired
	private JingweiRightManeger jwRightManeger;

	public synchronized void execute(Context context, @Param(name = "host") String host) {
		// zk环境
		host = StringUtil.isNotBlank(host) ? host : JingweiZkConfigManager.getDefaultKey();

		String loginNickName = (String) request.getAttribute(JingweiSecurityFilter.NICK_NAME_PARAM);

		// ark系统校验登陆成功后
		// 1 如果userRole表中没有这个用户，则向jw_user_role添加用户
		JwUserRole role = jwUserRoleDao.selectByUserAndRole(loginNickName, loginNickName, host);
		if (role == null) {
			// 增加一个角色
			this.addRole(loginNickName, host);
		}

		//		if (nickName.equals("朔海")) {
		//			list = jwPermissionDao.getAll();
		//		}

		// 查出这个花名的用户拥有多少个角色
		List<JwUserRole> userRolesByNickName = jwUserRoleDao.selectByNickName(loginNickName, host);

		List<String> rolesByNickName = new ArrayList<String>();

		for (JwUserRole jwUserRole : userRolesByNickName) {
			rolesByNickName.add(jwUserRole.getRoleName());
		}

		// 如果是超级用户
		if (this.isSuper(loginNickName)) {
			rolesByNickName = jwUserRoleDao.getAllDistinctRoleNames(host);
		}

		Map<String, JingweiPermissionVO> permissionVos = new HashMap<String, JingweiPermissionVO>();

		// 迭代所有角色，获取角色拥有的所有资源
		for (String roleNameByNickName : rolesByNickName) {

			// 该用户拥有的所有角色
			List<JwPermission> list = jwPermissionDao.selectByRoleName(roleNameByNickName, host);
			if (!permissionVos.containsKey(roleNameByNickName)) {
				JingweiPermissionVO vo = new JingweiPermissionVO();
				permissionVos.put(roleNameByNickName, vo);
				vo.setRole(roleNameByNickName);
				vo.setRightLevel(RightLevel.SUPER_RIGHT);
			}

			for (JwPermission permission : list) {

				String roleName = permission.getRoleName();

				if (roleName == null) {
					continue;
				}

				JingweiPermissionVO vo = permissionVos.get(roleName);
				vo.setRole(roleName);

				int type = permission.getResourceType();
				if (type == ResourceType.TASK_TYPE.getIndex()) {
					vo.getTasks().put(permission.getId(), permission.getResourceName());
				} else if (type == ResourceType.SREVER_TYPE.getIndex()) {
					vo.getServers().put(permission.getId(), permission.getResourceName());
				} else if (type == ResourceType.GROUP_TYPE.getIndex()) {
					vo.getGroups().put(permission.getId(), permission.getResourceName());
				}

				List<JwUserRole> userRoles = jwUserRoleDao.selectByRoleName(roleName, host);

				for (JwUserRole e : userRoles) {
					// 如果花名和角色名一样，则不显示了
					if (vo.getRole().equals(e.getNickName())) {
						continue;
					}
					vo.getUsers().put(e.getId(), e.getNickName());
				}

				// 如果是超级用户，则所有按钮都有效；如果用户和角色名一样，则添加用户按钮有效；如果用户拥有
				if (this.isSuper(loginNickName)) {
					vo.setRightLevel(RightLevel.SUPER_RIGHT);
				} else if (loginNickName.equals(roleName)) {
					vo.setRightLevel(RightLevel.OWNER_RIGHT);
				} else {
					vo.setRightLevel(RightLevel.GROUP_RIGHT);
				}

			}
		}

		context.put("permissionVos", permissionVos);
		context.put("host", host);
	}

	private void addRole(String role, String zkEnv) {
		JwUserRole userRole = new JwUserRole();
		userRole.setNickName(role);
		userRole.setRoleName(role);
		userRole.setZkEnv(zkEnv);

		this.jwUserRoleDao.save(userRole);
	}

	private boolean isSuper(String loginNickName) {
		return this.jwRightManeger.getSuperUserSet().contains(loginNickName);
	}

	public static class JingweiPermissionVO {
		private String role;
		private String id;
		private RightLevel rightLevel;

		// 又有这个角色的用户集合
		private Map<Long, String> users = new HashMap<Long, String>();

		private Map<Long, String> tasks = new HashMap<Long, String>();
		private Map<Long, String> servers = new HashMap<Long, String>();
		private Map<Long, String> groups = new HashMap<Long, String>();

		public String getRole() {
			return role;
		}

		public void setRole(String role) {
			this.role = role;
		}

		public Map<Long, String> getTasks() {
			return tasks;
		}

		public void setTasks(Map<Long, String> tasks) {
			this.tasks = tasks;
		}

		public Map<Long, String> getServers() {
			return servers;
		}

		public void setServers(Map<Long, String> servers) {
			this.servers = servers;
		}

		public Map<Long, String> getGroups() {
			return groups;
		}

		public void setGroups(Map<Long, String> groups) {
			this.groups = groups;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public Map<Long, String> getUsers() {
			return users;
		}

		public void setUsers(Map<Long, String> users) {
			this.users = users;
		}

		public RightLevel getRightLevel() {
			return rightLevel;
		}

		public void setRightLevel(RightLevel rightLevel) {
			this.rightLevel = rightLevel;
		}

	}

	/**
	 * 权限的种类 
	 * @author shuohai.lhl@taobao.com
	 *
	 * @time Jul 14, 2013 9:23:38 AM
	 *
	 * @desc 如果是超级用户，则所有按钮都有效；如果用户和角色名一样，则添加用户按钮有效；如果用户拥有组权限，则不能
	 */
	public static enum RightLevel {
		SUPER_RIGHT("SUPER_RIGHT"), OWNER_RIGHT("OWNER_RIGHT"), GROUP_RIGHT("GROUP_RIGHT");

		private String type;

		private RightLevel(String type) {
			this.type = type;
		}
	}

	public static enum ResourceType {
		TASK_TYPE("任务", 1), SREVER_TYPE("主机", 2), GROUP_TYPE("Group", 3);
		// 成员变量   
		private String name;
		private int index;

		// 构造方法   
		private ResourceType(String name, int index) {
			this.name = name;
			this.index = index;
		}

		// 普通方法   
		public static String getName(int index) {
			for (ResourceType c : ResourceType.values()) {
				if (c.getIndex() == index) {
					return c.name;
				}
			}
			return null;
		}

		// get set 方法   
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}
	}
}
