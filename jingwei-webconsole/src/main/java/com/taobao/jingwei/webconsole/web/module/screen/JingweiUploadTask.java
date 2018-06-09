package com.taobao.jingwei.webconsole.web.module.screen;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.taobao.jingwei.webconsole.util.upload.UploadFileService;
import com.taobao.jingwei.webconsole.util.upload.UploadPathHelper;

public class JingweiUploadTask {

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private UploadFileService uploadFileService;

	public void execute(Context context, @Param(name = "host") String host) {

		String absParentPath = UploadPathHelper.getUploadPath(request);

		Map<String, String> tarNames = this.uploadFileService.getAllFilesMap2Ip(absParentPath, 5000);

		context.put("tarNames", tarNames);
	}
}
