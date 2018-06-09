package com.taobao.jingwei.server.service.cmd;

import com.taobao.jingwei.server.service.ServerErrorCode;
import com.taobao.jingwei.server.service.ServiceUtil;
import com.taobao.jingwei.server.util.ServerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * �ȱ����ļ���/plugin/bakĿ¼�� ִ����ű�����Ϊ�ɹ� http://10.232.11.143:8080/jingwei-server-api/getTar?path=10.13.43.86/jingwei/
 * uploads/tars/DAILY-UNION-CPS-XO.tar.gz
 * ��1�� ɾ��targetĿ¼�µ�tar��
 * ��2���˴�workĿ¼�µĽ�ѹ��İ�
 * ��3����console����tar�� 
 * 
 * @author shuohailhl
 * 
 */
public class GetCustomerTarCmd extends AbstractHttpCmd {
	public static final String CMD_STR = "getTar";

	private static Log log = LogFactory.getLog(GetCustomerTarCmd.class);

	private static String BAK_FILE_SUFFIX = "yyyyMMdd-HH-mm";

	public static int BAK_FILE_SUFFIX_LENGTH = BAK_FILE_SUFFIX.length();

	public GetCustomerTarCmd(HttpRequest request, HttpResponse response) {
		super(request, response);
	}

	/**
	 * e.g. wget http://10.13.43.86/jingwei/uploads/tars/DAILY-UNION-CPS-XO.tar.gz -P /home/admin/jingwei-server/plugin/target
	 * 
	 */
	private String getTarFromConsole(String url, String savePath) {
		StringBuilder sb = new StringBuilder("curl ");
		sb.append(url).append(BLANK);
		sb.append(" -o ").append(savePath).append(BLANK);

		return sb.toString();
	}

	@Override
	protected void getJsonResponse(JSONObject jsonObj) throws JSONException {
		String savePath = ServiceUtil.getTargetDirRealPath();

		String requestUri = super.params.get("path");

		String fileName = super.params.get("fileName");

		savePath += "/" + fileName;

		File targetTarFile = new File(savePath);

		if (targetTarFile.exists()) {
			// ������bakĿ¼
			/** �����ļ��ĸ�ʽ */
			SimpleDateFormat myFmt1 = new SimpleDateFormat(BAK_FILE_SUFFIX);
			String suffix = myFmt1.format(new Date(targetTarFile.lastModified()));

			String bakFileName = fileName + suffix;

			String bakTarRealPath = ServerUtil.getBakTarRealPath(bakFileName);

			log.warn("move tar file : " + savePath + " to " + bakTarRealPath);
			ServerUtil.callShell(" mv " + savePath + " " + bakTarRealPath);

			File targetFile = new File(bakTarRealPath);
			if (!targetFile.exists()) {
				log.warn("move file failed " + bakTarRealPath);
				jsonObj.put("isSuccess", false);
				jsonObj.put("cause", "back file failed!");
				return;
			}
		}

		// ɾ��workĿ¼�µ��ļ�
		String workDirName = fileName.substring(0, fileName.indexOf("."));
		boolean success = ServerUtil.removeWork(workDirName);

		if (!success) {
			log.error(ServerErrorCode.DELETE_WORK_FAILED + " " + fileName);

			jsonObj.put("isSuccess", Boolean.FALSE);
			jsonObj.put("cause", ServerErrorCode.DELETE_WORK_FAILED);
			jsonObj.put("errCode", ServerErrorCode.DELETE_WORK_FAILED);

			return;
		}

		requestUri = "http://" + requestUri;
		//
		String cmd = this.getTarFromConsole(requestUri, savePath);
		//
		// String cmd = this.getTarFromConsole(url, savePath);
		ServerUtil.callShell(cmd);
		log.warn("after shell  :  " + cmd);

		jsonObj.put("isSuccess", Boolean.TRUE);
		jsonObj.put("cmd", cmd);
	}

	public static void main(String[] args) {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd-HH:mm");
		System.out.println(fmt.format(new Date()));
	}
}
