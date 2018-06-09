package com.taobao.jingwei.webconsole.web.module.screen.api.cmd;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jodd.util.StringUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import com.taobao.jingwei.webconsole.biz.dao.JwUserRoleDao;
import com.taobao.jingwei.webconsole.biz.dao.model.JwUserRole;

/**
 * @author shuohai.lhl@taobao.com
 *
 * @time Jun 13, 2013 3:10:59 PM
 * <pre>
 * @desc type : insert 添加role -- user
 *              delete 删除 根据id
 *              selectByNickName 根据花名获取角色
 *              selectByRoleName 根据角色名获取拥有这个角色的花名
 */
public class JwUserRoleCommand extends AbstractConsoleCommand {
	private static final Log log = LogFactory.getLog(JwUserRoleCommand.class);

	/** command */
	public static final String CMD_STR = "userRoleCommand";

	/** 用于区分 */
	public static final String TYPE_PARAM = "type";

	public static final String NICK_NAME_PARAM = "nickName";
	public static final String ROLE_NAME_PARAM = "roleName";

	private static final String SAVE_TYPE = "save";

	private static final String DELETE_EYPE = "delete";

	private JwUserRoleDao jwUserRoleDao;

	public JwUserRoleCommand() {
		super(null, null);
	}

	//http://10.13.43.86/jingwei/api/JingweiGateWay.do?act=userRoleCommand&type=save&host=1&nickName=XXX&roleName=XXX
	//http://10.13.43.86/jingwei/api/JingweiGateWay.do?act=userRoleCommand&type=delete&host=1&id=XXX
	public JwUserRoleCommand(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	@Override
	public boolean process(JSONObject json, String hostIndex) throws JSONException {
		String type = request.getParameter(TYPE_PARAM);

		if (StringUtil.equals(type, SAVE_TYPE)) {
			String roleName = request.getParameter(ROLE_NAME_PARAM);
			String nickName = request.getParameter(NICK_NAME_PARAM);

			JwUserRole jwUserRole = new JwUserRole();

			jwUserRole.setNickName(nickName);
			jwUserRole.setRoleName(roleName);
			jwUserRole.setZkEnv(hostIndex);
			try {
				jwUserRoleDao.save(jwUserRole);
			} catch (Exception e) {
				log.error("insert user role failed : " + e);
			}
		} else if (StringUtil.equals(type, DELETE_EYPE)) {
			long id = Long.valueOf(request.getParameter("id"));
			try {
				jwUserRoleDao.deleteById(id, hostIndex);
			} catch (Exception e) {
				log.error("insert user role failed : " + e);
			}
		}

		return true;
	}

	@Override
	public void success(JSONObject json) throws JSONException {

	}

	public JwUserRoleDao getJwUserRoleDao() {
		return jwUserRoleDao;
	}

	public void setJwUserRoleDao(JwUserRoleDao jwUserRoleDao) {
		this.jwUserRoleDao = jwUserRoleDao;
	}

}
