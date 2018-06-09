package com.taobao.jingwei.webconsole.web.filter;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.server.util.HttpPost;
import com.taobao.jingwei.webconsole.biz.exception.TimeoutException;
import com.taobao.jingwei.webconsole.util.ConsoleServerHosts;
import com.taobao.jingwei.webconsole.util.upload.UploadFileConst;
import com.taobao.jingwei.webconsole.util.upload.UploadFileService;
import com.taobao.jingwei.webconsole.util.upload.UploadPathHelper;

public class UploadFileFilter implements UploadFileConst, Filter {
	private static Log log = LogFactory.getLog(UploadFileFilter.class);

	@Autowired
	private UploadFileService uploadFileService;

	@Autowired
	public ConsoleServerHosts consoleServerHosts;

	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain arg2) throws IOException,
			ServletException {

		String savePath = UploadPathHelper.getUploadPath((HttpServletRequest) request);

		File f1 = new File(savePath);
		//System.out.println(savePath);
		if (!f1.exists()) {
			f1.mkdirs();
		}
		DiskFileItemFactory fac = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(fac);
		upload.setHeaderEncoding("utf-8");
		List<FileItem> fileList = null;
		try {
			fileList = upload.parseRequest((HttpServletRequest) request);
		} catch (FileUploadException ex) {
			return;
		}
		Iterator<FileItem> it = fileList.iterator();
		String name = "";
		while (it.hasNext()) {
			FileItem item = it.next();
			if (!item.isFormField()) {
				name = item.getName();

				long size = item.getSize();
				String type = item.getContentType();
				System.out.println(size + " byte " + " " + type);
				if (name == null || name.trim().equals("")) {
					continue;
				}

			//	this.preUploadFile(name);
				File saveFile = new File(savePath + JingWeiConstants.FILE_SEP + name);
				try {
					item.write(saveFile);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 *  上传之前先删掉原来已经存在的文件，可能部署在另外别的机器上，也删掉
	 */
	private void preUploadFile(String tar) {

		List<String> ips = this.consoleServerHosts.getServerIps();

		int port = Integer.valueOf(this.consoleServerHosts.getConsolePort());

		for (String ip : ips) {
			try {
				this.uploadFileService.delPeerConsoleFile(ip, port, tar, 5000);
			} catch (TimeoutException e) {
				log.error("delete failed " + ip + "\t" + tar, e);
			}
		}
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}

	public static class RequestThread extends Thread {
		final HttpServletRequest request;

		public RequestThread(HttpServletRequest request) {
			this.request = request;
		}

		@Override
		public void run() {
			try {
				String url = request.getSession().getServletContext().getRealPath("jingwei_upload_task.htm");
				HttpPost.doGet(url, 1000);
				//((HttpServletResponse) response).sendRedirect("/jingwei/jingwei_tasks.htm");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
