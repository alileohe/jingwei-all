package com.taobao.jingwei.webconsole.web.module.screen.api.cmd;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import com.alibaba.citrus.util.StringUtil;
import com.taobao.jingwei.webconsole.biz.dao.JwPermissionDao;
import com.taobao.jingwei.webconsole.biz.dao.JwResourceDao;
import com.taobao.jingwei.webconsole.biz.dao.model.JwPermission;
import com.taobao.jingwei.webconsole.model.config.util.ConfigUtil;
import com.taobao.jingwei.webconsole.util.ApiErrorCode;
import com.taobao.jingwei.webconsole.web.module.screen.api.JsonUtil;

/**
 * @author shuohai.lhl@taobao.com
 *
 * @time Jun 10, 2013 10:35:08 AM
 *
 * @desc 保存任务,参数taskNames=逗号分隔的任务列表
 */
public class SaveResourcesCommand extends AbstractConsoleCommand {
	private static final Log log = LogFactory.getLog(SaveResourcesCommand.class);

	/** command */
	public static final String CMD_STR = "saveResources";

	private JwResourceDao jwResourceDao;

	private JwPermissionDao jwPermissionDao;

	public SaveResourcesCommand() {
		super(null, null);
	}

	public SaveResourcesCommand(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	@Override
	public boolean process(JSONObject json, String hostIndex) throws JSONException {
		String resource = request.getParameter(RESOURCE_NAME_PARAM);

		if (StringUtil.isBlank(resource)) {
			JsonUtil.writeFailed2Response(json, ApiErrorCode.RESOURCE_IS_NULL_OR_EMPTY, response);
			return false;
		}

		//  1:任务 2:机器 3:GROUP
		String resourceType = request.getParameter(RESOURCE_TYPE_PARAM);

		if (StringUtil.isBlank(resourceType)) {
			JsonUtil.writeFailed2Response(json, ApiErrorCode.RESOURCE_TYPE_IS_NULL_OR_EMPTY, response);
			return false;
		}
		
		// 角色名
		String role = request.getParameter("role");
		if (StringUtil.isBlank(resourceType)) {
			JsonUtil.writeFailed2Response(json, ApiErrorCode.RESOURCE_TYPE_IS_NULL_OR_EMPTY, response);
			return false;
		}

		Set<String> resourceSet = ConfigUtil.commaSepString2Set(resource);
		
		
		
		// String nickName = (String) request.getAttribute(JingweiSecurityFilter.NICK_NAME_PARAM);
		for (String e : resourceSet) {
			JwPermission jwPermission = new JwPermission();
			jwPermission.setRoleName(role);
			jwPermission.setResourceName(e);
			jwPermission.setResourceType(Integer.valueOf(resourceType));
			jwPermission.setZkEnv(hostIndex);
			try {
				this.jwPermissionDao.save(jwPermission);
			} catch (Exception ex) {
				log.error("insert permission failed : ", ex);
			}

		}

		return true;
	}

	@Override
	public void success(JSONObject json) throws JSONException {

	}

	public JwResourceDao getJwResourceDao() {
		return jwResourceDao;
	}

	public void setJwResourceDao(JwResourceDao jwResourceDao) {
		this.jwResourceDao = jwResourceDao;
	}

	public JwPermissionDao getJwPermissionDao() {
		return jwPermissionDao;
	}

	public void setJwPermissionDao(JwPermissionDao jwPermissionDao) {
		this.jwPermissionDao = jwPermissionDao;
	}

}
