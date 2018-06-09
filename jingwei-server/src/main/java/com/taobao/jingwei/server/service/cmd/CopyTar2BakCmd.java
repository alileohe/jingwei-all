package com.taobao.jingwei.server.service.cmd;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.server.service.ServerErrorCode;
import com.taobao.jingwei.server.util.ServerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @desc ��ָ����$SERVER_HOME/plugin/target/xx.tar������$SERVER_HOME/plugin/bakĿ¼��
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Mar 18, 2013 5:34:09 PM
 * 
 */
public class CopyTar2BakCmd extends AbstractHttpCmd {
	private static final Log log = LogFactory.getLog(DeleteBackTarCmd.class);

	/** ����tar */
	public static final String CMD_STR = "copyTar2Bak";

	/** Ҫ�������ļ��� */
	public static final String TAR_NAME = "tarName";

	public CopyTar2BakCmd(HttpRequest request, HttpResponse response) {
		super(request, response);
	}

	@Override
	protected void getJsonResponse(JSONObject jsonObj) throws JSONException {
		// ��ȡ������Ҫɾ����tar��
		String tar = super.params.get(TAR_NAME);
		log.warn("tar name is " + tar);

		// ����Ϊ���쳣
		if (StringUtil.isEmpty(tar)) {
			log.error(ServerErrorCode.DELETE_TAR_NAME_IS_EMPTY.getDesc());

			jsonObj.put("isSuccess", Boolean.FALSE);
			jsonObj.put("cause", ServerErrorCode.DELETE_TAR_NAME_IS_EMPTY.getDesc());
			jsonObj.put("errCode", ServerErrorCode.DELETE_TAR_NAME_IS_EMPTY);

			return;
		}

		boolean success = ServerUtil.copyTarget2Bak(tar);

		jsonObj.put("isSuccess", success);

		if (!success) {
			log.error(ServerErrorCode.COPY_TAR2BACK_FAILED.getDesc());

			jsonObj.put("cause", ServerErrorCode.COPY_TAR2BACK_FAILED.getDesc());
			jsonObj.put("errCode", ServerErrorCode.COPY_TAR2BACK_FAILED);
		}
	}
}
