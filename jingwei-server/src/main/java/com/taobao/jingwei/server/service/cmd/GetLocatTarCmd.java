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
 * 
 * @desc ��ȡָ��Ŀ¼�µ�tar�ļ�. $JINGWEI_SERVER_HOME/plugin/target
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Mar 15, 2013 8:23:01 PM
 * 
 */
public class GetLocatTarCmd extends AbstractHttpCmd {
	
	public static final String CMD_STR = "getLocalTar";
	
	private static final Log log = LogFactory.getLog(GetLocatTarCmd.class);

	public GetLocatTarCmd(HttpRequest request, HttpResponse response) {
		super(request, response);
	}

	@Override
	protected void getJsonResponse(JSONObject jsonObj) throws JSONException {
		// ��ȡ���е�tar
		List<String> list = ServerUtil.getTarNamesAtServer();
		log.warn("get server tars : " + list);

		JSONArray jsonArray = new JSONArray(list);

		jsonObj.put("isSuccess", Boolean.TRUE);
		jsonObj.put("tars", jsonArray);
	}
}
