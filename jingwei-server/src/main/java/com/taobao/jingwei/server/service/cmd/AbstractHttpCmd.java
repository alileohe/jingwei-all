package com.taobao.jingwei.server.service.cmd;

import com.taobao.jingwei.server.service.ServiceUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @desc ��Ӧ���ɵ�����
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Mar 15, 2013 8:35:26 PM
 * 
 */
public abstract class AbstractHttpCmd implements HttpCmd {
	private static Log log = LogFactory.getLog(AbstractHttpCmd.class);

	/** ������� */
	protected final Map<String, String> params;

	/** netty http request */
	protected final HttpRequest request;

	/** netty http response */
	protected final HttpResponse response;

	/** �ʺ� */
	public static final String QUESTION_MARK = "?";

	/** &���� */
	public static final String AND_MARK = "&";

	/** �ʺ� */
	public static final String EQUAL_MARK = "=";

	/** �ո� */
	public static final String BLANK = " ";

	public AbstractHttpCmd(HttpRequest request, HttpResponse response) {
		this.request = request;
		this.response = response;

		String uri = request.getUri();
		log.warn("request uri : " + uri);

		if (uri.indexOf(QUESTION_MARK) != -1) {
			String msg = uri.substring(uri.indexOf(QUESTION_MARK) + 1);
			this.params = this.getParams(msg);
		} else {
			this.params = Collections.emptyMap();
		}
		log.warn("params is : " + this.params);
	}

	public Map<String, String> getMap() {
		return params;
	}

	@Override
	public void invoke() {

		// ��ȡjson�ṹ�Ĳ������
		JSONObject jsonObj = new JSONObject();

		// catch jsonObj.put() if throws
		try {
			this.getJsonResponse(jsonObj);
		} catch (JSONException e) {
			log.error(e);
		}

		// ����client
		ServiceUtil.write2Client(jsonObj, response);
	}

	protected abstract void getJsonResponse(JSONObject jsonObj) throws JSONException;

	/**
	 * ��ȡkey-value��ʽ�Ĳ���
	 * 
	 * @param msg k1=v1&k2=v2&...
	 * @return
	 */
	public Map<String, String> getParams(String msg) {
		Map<String, String> map = new HashMap<String, String>();

		String[] paramPairs = msg.split(AND_MARK);
		for (String paramPair : paramPairs) {
			int index = paramPair.indexOf(EQUAL_MARK);
			String key = paramPair.substring(0, index);
			String value = paramPair.substring(index + 1);
			map.put(key, value);
		}

		return map;
	}

	public static void main(String[] args) {

	}

}
