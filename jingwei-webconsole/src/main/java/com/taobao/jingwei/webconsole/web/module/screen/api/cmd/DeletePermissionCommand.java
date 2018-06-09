package com.taobao.jingwei.webconsole.web.module.screen.api.cmd;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import com.taobao.jingwei.webconsole.biz.dao.JwPermissionDao;
import com.taobao.jingwei.webconsole.util.ApiErrorCode;
import com.taobao.jingwei.webconsole.web.module.screen.api.JsonUtil;

/**
 * @author shuohai.lhl@taobao.com
 *
 * @time Jun 3, 2013 7:37:47 PM
 *
 * @desc É¾³ýpermission
 * 
 */
public class DeletePermissionCommand extends AbstractConsoleCommand {

	private static final Log log = LogFactory.getLog(DeletePermissionCommand.class);
	/** command */
	public static final String CMD_STR = "deletePermissionById";

	private JwPermissionDao jwPermissionDao;

	public DeletePermissionCommand() {
		super(null, null);
	}

	public DeletePermissionCommand(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	@Override
	public boolean process(JSONObject json, String hostIndex) throws JSONException {

		long id = Long.valueOf(request.getParameter("id"));

		int effectCount = 0;
		try {
			effectCount = jwPermissionDao.deleteById(id, hostIndex);
		} catch (Exception e) {
			log.error("delete permission failed id =  " + id, e);
		}

		if (effectCount == 0) {
			JsonUtil.writeFailed2Response(json, ApiErrorCode.DELETE_PERMISSION_FAILED, response);
			log.warn("delete permission effect rows : 0.");
			return false;
		}

		return true;
	}

	@Override
	public void success(JSONObject json) throws JSONException {

	}

	public JwPermissionDao getJwPermissionDao() {
		return jwPermissionDao;
	}

	public void setJwPermissionDao(JwPermissionDao jwPermissionDao) {
		this.jwPermissionDao = jwPermissionDao;
	}

}
