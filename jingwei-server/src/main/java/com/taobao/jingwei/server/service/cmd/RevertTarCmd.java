package com.taobao.jingwei.server.service.cmd;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.server.service.ServerErrorCode;
import com.taobao.jingwei.server.util.ServerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

/**
 * @desc <pre>
 * (1) ���targetĿ¼�´���tar������ʾɾ��ԭ����tar�ļ�
 * (2) ��ָ����tar.gz��bakĿ¼��ԭ��targetĿ¼��
 * </pre>
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Apr 18, 2013 10:56:32 AM
 * 
 */
public class RevertTarCmd extends AbstractHttpCmd {
	private final Log log = LogFactory.getLog(RevertTarCmd.class);

	// http://10.232.11.143:8080/jingwei-server-api/revertTar?bakFileName=XXX
	public static final String CMD_STR = "revertTar";

	/** Ҫ��ԭ���ļ��� */
	public static final String BAK_FILT_NAME_PARAM = "bakFileName";

	/***/
	public static final String TAR_GZ = "tar.gz";

	public RevertTarCmd() {
		this(null, null);
	}

	public RevertTarCmd(HttpRequest request, HttpResponse response) {
		super(request, response);
	}

	@Override
	protected void getJsonResponse(JSONObject jsonObj) throws JSONException {
		String bakFileName = params.get(BAK_FILT_NAME_PARAM);
		// ����Ϊ���쳣
		if (StringUtil.isEmpty(bakFileName)) {
			log.error(ServerErrorCode.BAK_TAR_FILE_IS_EMPTY.getDesc());

			jsonObj.put("isSuccess", Boolean.FALSE);
			jsonObj.put("cause", ServerErrorCode.BAK_TAR_FILE_IS_EMPTY.getDesc());
			jsonObj.put("errCode", ServerErrorCode.BAK_TAR_FILE_IS_EMPTY);

			return;
		}

		log.warn("bak file name is : " + bakFileName);

		String tarFileName = this.getTarNameFromBakFileName(bakFileName);
		log.warn("targz file name is : " + tarFileName);

		String targetTarRealPath = ServerUtil.getTargetTarRealPath(tarFileName);
		String bakTarRealPath = ServerUtil.getBakTarRealPath(bakFileName);

		File targetTarFile = new File(targetTarRealPath);
		// ����ļ���������ʾɾ��ԭ����tar
		if (targetTarFile.exists()) {
			jsonObj.put("isSuccess", Boolean.FALSE);
			jsonObj.put("cause", "tar file exists at target dir, please remove it first : " + tarFileName);
			return;
		}

		File bakTarFile = new File(bakTarRealPath);

		try {
			ServerUtil.copyForChannel(bakTarFile, targetTarFile);
			log.warn("copy file :" + bakTarFile + " to " + targetTarFile);
		} catch (IOException e) {
			log.error(e);
		}

		File newTarget = new File(targetTarRealPath);
		if (!newTarget.exists()) {
			jsonObj.put("isSuccess", Boolean.FALSE);
			jsonObj.put("cause", "revert file failed" + bakFileName);
		} else {
			jsonObj.put("isSuccess", Boolean.TRUE);
		}
	}

	/**
	 * 
	 * @param bakFileName
	 * @return bak�ļ������ʱ��
	 */
	private String getTarNameFromBakFileName(String bakFileName) {
		String tarFileName = bakFileName.substring(0, bakFileName.indexOf(TAR_GZ) + TAR_GZ.length());

		return tarFileName;
	}

	public static void main(String[] args) {
		// RevertTarCmd cmd = new RevertTarCmd();
		// System.out.println(getTarNameFromBakFileName("asdf.tar.gz.fasfas"));
	}
}
