package com.taobao.jingwei.webconsole.util.upload;

import javax.servlet.http.HttpServletRequest;

/**
 * 从HttpServletRequest request获取保留上传文件绝对路径
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
