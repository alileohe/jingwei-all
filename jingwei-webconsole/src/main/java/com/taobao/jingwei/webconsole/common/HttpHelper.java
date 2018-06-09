package com.taobao.jingwei.webconsole.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * @description HTTPClient
 * @author �ݳ� jiechen.qzm@taobao.com
 * @create 2011-9-20 ����11:38:47
 * @modify 2011-9-20 ����11:38:47 jiechen
 */
public class HttpHelper {

	/**
	 * ����GET����
	 * 
	 * @param urlString
	 *            URL��ַ
	 * @return ��Ӧ����
	 * @throws IOException
	 */
	public static String sendGet(String urlString) throws IOException {
		return send(urlString, "GET", null, null);
	}

	/**
	 * ����GET����
	 * 
	 * @param urlString
	 *            URL��ַ
	 * @param params
	 *            ��������
	 * @return ��Ӧ����
	 * @throws IOException
	 */
	public static String sendGet(String urlString, Map<String, String> params) throws IOException {
		return sendGet(urlString, params, 0);
	}

	public static String sendGet(String urlString, Map<String, String> params, int timeout) throws IOException {
		return send(urlString, "GET", params, null, timeout);
	}

	/**
	 * ����GET����
	 * 
	 * @param urlString
	 *            URL��ַ
	 * @param params
	 *            ��������
	 * @param propertys
	 *            ��������
	 * @return ��Ӧ����
	 * @throws IOException
	 */
	public static String sendGet(String urlString, Map<String, String> params, Map<String, String> propertys)
			throws IOException {
		return send(urlString, "GET", params, propertys);
	}

	/**
	 * ����POST����
	 * 
	 * @param urlString
	 *            URL��ַ
	 * @return ��Ӧ����
	 * @throws IOException
	 */
	public static String sendPost(String urlString) throws IOException {
		return send(urlString, "POST", null, null);
	}

	/**
	 * ����POST����
	 * 
	 * @param urlString
	 *            URL��ַ
	 * @param params
	 *            ��������
	 * @return ��Ӧ����
	 * @throws IOException
	 */
	public static String sendPost(String urlString, Map<String, String> params) throws IOException {
		return send(urlString, "POST", params, null);
	}

	/**
	 * ����POST����
	 * 
	 * @param urlString
	 *            URL��ַ
	 * @param params
	 *            ��������
	 * @param propertys
	 *            ��������
	 * @return ��Ӧ����
	 * @throws IOException
	 */
	public static String sendPost(String urlString, Map<String, String> params, Map<String, String> propertys)
			throws IOException {
		return send(urlString, "POST", params, propertys);
	}

	/**
	 * ����HTTP����
	 * 
	 * @param urlString
	 * @return ��ӳ������ַ���(���ڵ��ô��ر���)
	 * @throws IOException
	 */
	private static String send(String urlString, String method, Map<String, String> parameters,
			Map<String, String> propertys) throws IOException {
		return send(urlString, method, parameters, propertys, 0);
	}

	private static String send(String urlString, String method, Map<String, String> parameters,
			Map<String, String> propertys, int timeout) throws IOException {
		HttpURLConnection urlConnection = null;

		if (method.equalsIgnoreCase("GET") && parameters != null) {
			StringBuffer param = new StringBuffer();
			int i = 0;
			for (String key : parameters.keySet()) {
				if (i == 0)
					param.append("?");
				else
					param.append("&");
				param.append(key).append("=").append(parameters.get(key));
				i++;
			}
			urlString += param;
		}
		URL url = new URL(urlString);
		urlConnection = (HttpURLConnection) url.openConnection();
		if (timeout > 0) {
			urlConnection.setReadTimeout(timeout);
		}

		urlConnection.setRequestMethod(method); /*
												 * urlConnection.setRequestProperty
												 * ("Content-Type",
												 * "text/plain");
												 * urlConnection.setRequestProperty
												 * ("Accept-Encoding", "gzip");
												 */
		urlConnection.setDoOutput(true);
		urlConnection.setDoInput(true);
		urlConnection.setUseCaches(false);

		if (propertys != null)
			for (String key : propertys.keySet()) {
				urlConnection.addRequestProperty(key, propertys.get(key));
			}

		if (method.equalsIgnoreCase("POST") && parameters != null) {
			StringBuffer param = new StringBuffer();
			for (String key : parameters.keySet()) {
				param.append("&");
				param.append(key).append("=").append(parameters.get(key));
			}
			urlConnection.getOutputStream().write(param.toString().getBytes());
			urlConnection.getOutputStream().flush();
			urlConnection.getOutputStream().close();
		}
		String contextString;
		InputStream inputStream = urlConnection.getInputStream();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
			StringBuffer buffer = new StringBuffer();
			String line = "";
			while ((line = in.readLine()) != null) {
				buffer.append(line).append("\r\n");
			}
			contextString = buffer.toString();
		} catch (IOException e) {
			throw e;
		} finally {
			urlConnection.disconnect();
		}
		return contextString;
	}
}