package com.taobao.jingwei.server.service.cmd;

import com.taobao.jingwei.server.util.ServerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * @desc ��ȡ$SERVER_HOME/plugin/bakĿ¼�µ�tar.gz��
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Mar 18, 2013 6:01:13 PM
 * 
 */
public class GetBakTarCmd extends AbstractHttpCmd {

	private static final Log log = LogFactory.getLog(GetBakTarCmd.class);
	
	/** ��ȡָ��server�����ϵ�tar */
	public static final String CMD_STR = "getBakTar";

	public GetBakTarCmd(HttpRequest request, HttpResponse response) {
		super(request, response);
	}

	@Override
	protected void getJsonResponse(JSONObject jsonObj) throws JSONException {
		// ��ȡ���е�tar
		List<String> list = ServerUtil.getBakTarNamesAtServer();
		log.warn("get server bak tars : " + list);

		JSONArray jsonArray = new JSONArray(list);

		jsonObj.put("isSuccess", Boolean.TRUE);
		jsonObj.put("tars", jsonArray);
	}

}
