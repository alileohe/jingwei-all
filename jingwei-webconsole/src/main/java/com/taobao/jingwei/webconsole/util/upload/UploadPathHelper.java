package com.taobao.jingwei.webconsole.util.upload;

import javax.servlet.http.HttpServletRequest;

/**
 * ��HttpServletRequest request��ȡ�����ϴ��ļ�����·��
 * e.g. 
 * @author shuohailhl
 *
 */
public class UploadPathHelper {
	public static String getUploadPath(HttpServletRequest request) {
		
		String contextPath = request.getSession().getServletContext().getRealPath(UploadFileConst.TAR_DIR_PATH);
		return contextPath;
	}
}
