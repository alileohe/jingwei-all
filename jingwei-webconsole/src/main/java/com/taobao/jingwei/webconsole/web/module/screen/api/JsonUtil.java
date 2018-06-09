package com.taobao.jingwei.webconsole.web.module.screen.api;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import com.taobao.jingwei.webconsole.util.ApiErrorCode;

public class JsonUtil {
	private static Log log = LogFactory.getLog(JsonUtil.class);

	public static void writeJson2Client(JSONObject jsonObj, HttpServletResponse response) {
		JsonUtil.writeStr2Client(jsonObj.toString(), response);
	}

	public static void writeStr2Client(String str, HttpServletResponse response) {
		PrintWriter writer = null;
		try {
			writer = response.getWriter();
			response.setContentType("application/json;charset=utf-8");
			if (null != writer) {
				writer.write(str);
			}
		} catch (IOException e) {
			log.error(e);
		} finally {
			if (null != writer) {
				writer.flush();
				writer.close();
			}
		}
	}

	/**
	 * 
	 * @param response
	 */
	public static void writeFailJson2Client(HttpServletResponse response) {
		JSONObject jsonObj = new JSONObject();

		try {
			jsonObj.put("isSuccess", Boolean.FALSE);
		} catch (JSONException e) {
			e.printStackTrace();
			log.error(e);
		}
		writeStr2Client(jsonObj.toString(), response);
	}

	/**
	 * ∑µªÿ ß∞‹
	 * 
	 * @param cause  ß∞‹‘≠“Ú
	 * @param response
	 */
	public static void writeFailJson2Client(String cause, HttpServletResponse response) {
		JSONObject jsonObj = new JSONObject();

		try {
			jsonObj.put("isSuccess", Boolean.FALSE);
			jsonObj.put("cause", cause);
		} catch (JSONException e) {
			e.printStackTrace();
			log.error(e);
		}

		writeStr2Client(jsonObj.toString(), response);
	}

	/**
	 * 
	 * @param apiErrorCode
	 * @param response
	 */

	public static void writeFailed2Response(JSONObject jsonObj, ApiErrorCode apiErrorCode, HttpServletResponse response) {
		String cause = apiErrorCode.getDescAndReset();
		log.error(cause);

		try {
			jsonObj.put("isSuccess", Boolean.FALSE);
			jsonObj.put("cause", cause);
			jsonObj.put("errCode", apiErrorCode);
		} catch (JSONException e) {
			log.error(e);
		}

	}

	/**
	 * 
	 * @param apiErrorCode
	 * @param response
	 */

	public static void writeFailed2Response(ApiErrorCode apiErrorCode, HttpServletResponse response) {
		String cause = apiErrorCode.getDescAndReset();
		log.error(cause);
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("isSuccess", Boolean.FALSE);
			jsonObj.put("cause", cause);
			jsonObj.put("errCode", apiErrorCode);
		} catch (JSONException e) {
			log.error(e);
		}
	}
}
