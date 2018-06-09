package com.taobao.jingwei.webconsole.web.module.action;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.webconsole.model.config.util.ConfigUtil;
import com.taobao.jingwei.webconsole.util.upload.UploadFileService;
import com.taobao.jingwei.webconsole.util.upload.UploadPathHelper;
import com.taobao.jingwei.webconsole.web.module.screen.api.JsonUtil;

/**
 * 管理tar包，
 * （1）从用户电脑上传到console
 * （2）检索console的tar包
 * （3）
 * @author shuohailhl
 *
 */
public class JingweiCustomerTarAction {

	private final static Log log = LogFactory.getLog(JingweiCustomerTarAction.class);
	@Autowired
	private HttpServletRequest request;

	@Autowired
	private HttpServletResponse response;

	@Autowired
	private UploadFileService uploadFileService;

	/** 参数tarName 要删除的文件名 */
	public static final String TAR_NAME = "tarName";

	public void doReceiveTar(Context context, @Param(name = "host") String host,
			@Param(name = "batchConfig") String batchConfigStr) {
	}

	/**
	 * 检索console上的tar包
	 * @param context
	 */
	public void doGetTars(Context context) {
		String absParentPath = UploadPathHelper.getUploadPath(request);
		List<String> list = this.uploadFileService.getAllFileNames(absParentPath, 5000);
		PrintWriter writer = null;
		JSONObject jsonObj = new JSONObject();

		JSONArray jsonArray = new JSONArray(list);

		try {
			jsonObj.put("candidates", jsonArray);
			writer = response.getWriter();
			response.setContentType("application/json;charset=utf-8");
			if (null != writer) {
				writer.write(jsonObj.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			if (null != writer) {
				writer.flush();
				writer.close();
			}
		}
	}

	public void doDeleteTar(Context context, @Param(name = "tarName") String tarName) {
		// 存储路径 e.g. /jingwei/uploads/tars/
		String absParentPath = UploadPathHelper.getUploadPath(request);

		// 文件名
		String fileName = this.request.getParameter(TAR_NAME);
		File f = new File(absParentPath + JingWeiConstants.FILE_SEP + fileName);

		if (!f.exists()) {
			context.put("messages", "文件不存在 : " + tarName);
			return;
		} 
		
		boolean success = false;
		success = f.delete();
		if (!success) {
			context.put("messages", "删除失败 : " + tarName);
		}
	}
}
