package com.taobao.jingwei.webconsole.web.module.screen.api.cmd;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.server.util.HttpPost;
import com.taobao.jingwei.webconsole.util.ApiErrorCode;
import com.taobao.jingwei.webconsole.util.ConsoleServerHosts;
import com.taobao.jingwei.webconsole.util.ConsoleUtil;
import com.taobao.jingwei.webconsole.util.upload.UploadPathHelper;
import com.taobao.jingwei.webconsole.web.module.screen.api.JsonUtil;

/**
 * @desc 删除指定web console上的指定tar
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * @date Mar 4, 20134:29:32 PM
 */
public class DeleteTarCommand extends AbstractConsoleCommand {
	/** 获取指定server机器上的tar */
	public static final String CMD_STR = "deleteTar";

	/** 参数tarName 要删除的文件名 */
	public static final String TAR_NAME = "tarName";

	/** 要删除的人tar所在的机器 */
	public static final String TARGET_CONSOLE_IP = "targetConsoleIp";

	@Autowired
	public ConsoleServerHosts consoleServerHosts;

	public DeleteTarCommand() {
		this(null, null);
	}

	public DeleteTarCommand(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	@Override
	public boolean process(JSONObject json, String hostIndex) throws JSONException {
		// 存储路径 e.g. /jingwei/uploads/tars/
		String absParentPath = UploadPathHelper.getUploadPath(request);

		String targetConsoleIp = super.request.getParameter(TARGET_CONSOLE_IP);

		String fileName = super.request.getParameter(TAR_NAME);

		try {
			if (StringUtil.equals(ConsoleUtil.getLocalHostIp(), targetConsoleIp)) {
				// 文件名

				File f = new File(absParentPath + JingWeiConstants.FILE_SEP + fileName);
				boolean success = false;
				success = f.delete();
				if (success) {
					return true;
				} else {
					JsonUtil.writeFailed2Response(json, ApiErrorCode.DELETE_FILE_FAILED, response);
					return false;
				}
			} else {

				// 去另一台机器上删除文件

				String port = consoleServerHosts.getConsolePort();

				// http://10.13.43.86:8080/jingwei/api/JingweiGateWay.do?act=deleteTar&tarName=balbabala&targetConsoleIp=XXX
				StringBuilder sb = new StringBuilder("http://");
				sb.append(targetConsoleIp).append(":").append(port)
						.append("/jingwei/api/JingweiGateWay.do?act=deleteTar&").append("tarName=").append(fileName);
				sb.append("&").append(TARGET_CONSOLE_IP).append("=").append(targetConsoleIp);

				try {
					String result = HttpPost.doGet(sb.toString());

					JSONObject obj = new JSONObject(result);

					boolean success = obj.getBoolean("isSuccess");

					if (success) {
						return true;
					}

				} catch (IOException e) {
					log.error(e);
				}
			}
		} catch (UnknownHostException e) {
			log.error(e);
		}
		JsonUtil.writeFailed2Response(json, ApiErrorCode.DELETE_FILE_FAILED, response);
		return false;
	}

	@Override
	public void success(JSONObject json) throws JSONException {
		// 只填充isSuccess字段即可，父类已经实现
	}

	public ConsoleServerHosts getConsoleServerHosts() {
		return consoleServerHosts;
	}

	public void setConsoleServerHosts(ConsoleServerHosts consoleServerHosts) {
		this.consoleServerHosts = consoleServerHosts;
	}
}
