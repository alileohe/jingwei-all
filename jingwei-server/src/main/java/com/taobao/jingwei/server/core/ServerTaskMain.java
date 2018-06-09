package com.taobao.jingwei.server.core;

import com.taobao.jingwei.common.JingWeiUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ����buildin���񣬵��õ�main��������һ��������ʾ���������ڶ���������ʾ�����ļ���·��
 * 
 * @author shuohailhl
 * 
 */
public class ServerTaskMain {

	private static Log log = LogFactory.getLog(ServerTaskMain.class);

	public static void main(String[] args) {

		// ��ʼ������
		ServerTaskCore core = new ServerTaskCore();

		try {
			core.init();
		} catch (Exception e) {
			log.error("[jingwei-server] start buildin task  core error!");
			JingWeiUtil.destroyZkAndExit(null, -1);
		}

		log.warn("[jingwei server] class ServerTaskMainhas has invoke : ");
	}

}
