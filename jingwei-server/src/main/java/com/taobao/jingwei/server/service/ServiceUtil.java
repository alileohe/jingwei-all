package com.taobao.jingwei.server.service;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.server.config.ServerConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

/**
 * 
 * @desc ������
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Mar 15, 2013 8:29:56 PM
 * 
 */
public class ServiceUtil implements JingWeiConstants {
	private static final Log log = LogFactory.getLog(ServiceUtil.class);

	/**
	 * get target absolute path.
	 * 
	 * @return e.g. /home/admin/jingwei-server/plugin/target
	 */
	public static String getTargetDirRealPath() {

		StringBuilder sb = new StringBuilder(ServerConfig.getInstance().getTaskPluginDirPath());
		sb.append(FILE_SEP).append(ServerConfig.TARGET_DIR_NAME);
		return sb.toString();
	}

	/**
	 * get bak target absolute path.
	 * 
	 * @return e.g. /home/admin/jingwei-server/plugin/bak
	 */
	public static String getBakDirRealPath() {

		StringBuilder sb = new StringBuilder(ServerConfig.getInstance().getTaskPluginDirPath());
		sb.append(FILE_SEP).append(ServerConfig.BAK_DIR_NAME);
		return sb.toString();
	}

	/**
	 * get target absolute path.
	 * 
	 * @return e.g. /home/admin/jingwei-server/plugin/work
	 */
	public static String getWorkDirRealPath() {

		StringBuilder sb = new StringBuilder(ServerConfig.getInstance().getTaskPluginDirPath());
		sb.append(FILE_SEP).append(ServerConfig.WORK_DIR_NAME);
		return sb.toString();
	}

	/**
	 * get target absolute path.
	 * 
	 * @return e.g. /home/admin/jingwei-server/bin/wget.sh
	 */
	public static String getWgetRealPath() {

		StringBuilder sb = new StringBuilder(ServerConfig.getInstance().getServerBaseHome());
		sb.append(FILE_SEP).append("bin").append(FILE_SEP).append("wget.sh");
		return sb.toString();
	}

	/**
	 * ��json��ʽ�Ľ�����ظ�client
	 * 
	 * @param jsonObj ��������Ľ��
	 * @param response ����д����
	 */
	public static void write2Client(JSONObject jsonObj, HttpResponse response) {
		String jsonStr = jsonObj.toString();

		if (StringUtil.isNotBlank(jsonStr)) {
			write2Client(jsonObj.toString(), response);
		}
	}

	/**
	 * ��json��ʽ�Ľ�����ظ�client
	 * 
	 * @param jsonObj ��������Ľ��
	 * @param response ����д����
	 */
	public static void write2Client(String str, HttpResponse response) {

		ChannelBuffer cb = ChannelBuffers.dynamicBuffer();

		try {
			cb.writeBytes(str.getBytes("utf-8"));
			response.setContent(cb);
		} catch (UnsupportedEncodingException e) {
			log.error(e);
		}

		response.setHeader(CONTENT_TYPE, "application/json;charset=utf-8");
		response.setHeader(CONTENT_LENGTH, response.getContent().writerIndex());
	}

}
