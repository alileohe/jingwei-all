package com.taobao.jingwei.webconsole.util.upload;

import com.taobao.jingwei.common.JingWeiConstants;

/**
 * 
 * @author shuohailhl
 *
 */
public interface UploadFileConst {
	// ��$WEB_CONTEXTΪ��·��
	String FILE_DIR = "uploads";
	
	// �Զ���tar��·��
	String TAR_DIR = "tars";
	
	// �ϴ����Զ���tar���洢��·�� /jingwei/uploads/tars/
	String TAR_DIR_PATH = FILE_DIR + JingWeiConstants.FILE_SEP + TAR_DIR+ JingWeiConstants.FILE_SEP;
}
