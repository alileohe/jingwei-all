package com.taobao.jingwei.webconsole.web.module.screen.api.cmd;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.taobao.jingwei.webconsole.util.upload.UploadFileService;
import com.taobao.jingwei.webconsole.util.upload.UploadPathHelper;

/**
 * @desc 获取console上所有的tar包，包括所有ha的机器上的tar
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * @date Mar 23, 2013 10:52:33 AM
 */
public class GetLocalTarCommand extends AbstractConsoleCommand {
	private static Log log = LogFactory.getLog(GetLocalTarCommand.class);

	private UploadFileService uploadFileService;

	public UploadFileService getUploadFileService() {
		return uploadFileService;
	}

	public void setUploadFileService(UploadFileService uploadFileService) {
		this.uploadFileService = uploadFileService;
	}

	public GetLocalTarCommand() {
		this(null, null);
	}

	public GetLocalTarCommand(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	@Override
	public boolean process(JSONObject json, String hostIndex) throws JSONException {
		String absParentPath = UploadPathHelper.getUploadPath(request);

		List<String> list = uploadFileService.getLocalFileNames(absParentPath);

		if (log.isInfoEnabled()) {
			log.info("tar at console are : " + list);
		}

		JSONArray jsonArray = new JSONArray(list);

		json.put("tars", jsonArray);
		return true;
	}

	@Override
	public void success(JSONObject json) throws JSONException {

	}
}
