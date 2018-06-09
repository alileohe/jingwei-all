package com.taobao.jingwei.server.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpPost {
	private static int CONNECTION_TIMEOUT = 5000;
	private static Log log = LogFactory.getLog(HttpPost.class);

	public static String doPost(String urlStr) throws IOException {
		return doRequest(urlStr, "POST", CONNECTION_TIMEOUT);
	}

	public static String doGet(String urlStr, int timeoutMils) throws IOException {
		return doRequest(urlStr, "GET", timeoutMils);
	}

	public static String doGet(String urlStr) throws IOException {
		return doRequest(urlStr, "GET", CONNECTION_TIMEOUT);
	}

	public static String doRequest(String urlStr, String requestMethod, int timeoutMils) throws IOException {
		HttpURLConnection httpURLConn = null;
		try {
			String temp = new String();
			URL url = new URL(urlStr);
			httpURLConn = (HttpURLConnection) url.openConnection();

			httpURLConn.setConnectTimeout(timeoutMils);
			httpURLConn.setReadTimeout(timeoutMils);

			httpURLConn.setDoOutput(true);
			httpURLConn.setRequestMethod(requestMethod);
			httpURLConn.setIfModifiedSince(999999999);
			httpURLConn.connect();
			InputStream in = httpURLConn.getInputStream();
			BufferedReader bd = new BufferedReader(new InputStreamReader(in));

			StringBuilder sb = new StringBuilder();
			while ((temp = bd.readLine()) != null) {
				sb.append(temp);
				//	System.out.println(temp);
			}

			return sb.toString();
		} finally {
			if (httpURLConn != null) {
				httpURLConn.disconnect();
			}
		}
	}

	public static void main(String[] args) {
		HttpPost post = new HttpPost();
		//post.doPost("http://ops.jm.taobao.net/rtools/api/jade_gate_way.do?act=queryJadeGroup&type=groupKey&keyWord=JW_TMALL_INVENTORY_02_GROUP&envId=3");
		try {
		//	post.doGet("http://localhost:8080/getTar?path=10.13.43.86/jingwei/uploads/tars/DAILY-UNION-PAY-XO.tar.gz");
			String result = post.doGet("http://10.232.11.143:8080/jingwei-server-api/getLocalTar");
			System.out.println(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
