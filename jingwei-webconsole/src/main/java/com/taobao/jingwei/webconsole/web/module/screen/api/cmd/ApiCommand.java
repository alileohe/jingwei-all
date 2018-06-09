package com.taobao.jingwei.webconsole.web.module.screen.api.cmd;

import org.json.JSONException;

public interface ApiCommand {
	/**
	 * 结果都是已json格式返回的
	 * 
	 * @throws JSONException
	 */
	void invoke() throws JSONException;
}
