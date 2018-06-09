package com.taobao.jingwei.webconsole.util.upload;

import com.taobao.jingwei.common.JingWeiConstants;

/**
 * 
 * @author shuohailhl
 *
 */
public interface UploadFileConst {
	// 以$WEB_CONTEXT为根路径
	String FILE_DIR = "uploads";
	
	// 自定义tar包路径
	String TAR_DIR = "tars";
	
	// 上传的自定义tar包存储的路径 /jingwei/uploads/tars/
	String TAR_DIR_PATH = FILE_DIR + JingWeiConstants.FILE_SEP + TAR_DIR+ JingWeiConstants.FILE_SEP;
}
