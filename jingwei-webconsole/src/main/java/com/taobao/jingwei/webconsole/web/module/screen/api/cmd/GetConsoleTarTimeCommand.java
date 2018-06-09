package com.taobao.jingwei.webconsole.web.module.screen.api.cmd;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.webconsole.util.ApiErrorCode;
import com.taobao.jingwei.webconsole.util.upload.UploadPathHelper;
import com.taobao.jingwei.webconsole.web.module.screen.api.JsonUtil;

/**
 * @desc 获取指定web console上的指定tar的文件创建时间，就是上次上传的时间
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * @date Mar 4, 20134:29:32 PM
 */
public class GetConsoleTarTimeCommand extends AbstractConsoleCommand {
	/** 获取指定server机器上的tar */
	public static final String CMD_STR = "getConsoleTarTime";

	/** 参数tarName 要删除的文件名 */
	public static final String TAR_NAME = "tarName";

	public GetConsoleTarTimeCommand() {
		this(null, null);
	}

	public GetConsoleTarTimeCommand(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	@Override
	public boolean process(JSONObject json, String hostIndex) throws JSONException {
		// 存储路径 e.g. /jingwei/uploads/tars/
		String absParentPath = UploadPathHelper.getUploadPath(request);

		String fileName = super.request.getParameter(TAR_NAME);

		File f = new File(absParentPath + JingWeiConstants.FILE_SEP + fileName);

		if (f.exists()) {
			long lastModified = f.lastModified();
			json.put("LAST_MODIFIED", lastModified);
			return true;
		} else {
			JsonUtil.writeFailed2Response(json, ApiErrorCode.FILE_NOT_EXIST, response);
			return false;
		}

	}

	@Override
	public void success(JSONObject json) throws JSONException {
		// 只填充isSuccess字段即可，父类已经实现
	}

}
