package com.taobao.jingwei.webconsole.web.module.screen.api.cmd;

import org.json.JSONException;

public interface ApiCommand {
	/**
	 * ���������json��ʽ���ص�
	 * 
	 * @throws JSONException
	 */
	void invoke() throws JSONException;
}
