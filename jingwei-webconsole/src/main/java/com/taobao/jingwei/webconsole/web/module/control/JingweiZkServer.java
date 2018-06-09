package com.taobao.jingwei.webconsole.web.module.control;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.taobao.jingwei.webconsole.biz.manager.JingweiZkConfigManager;

public class JingweiZkServer {
	public void execute(Context context, @Param(name = "host") String host) {
		context.put("host", host);
		context.put("hosts", JingweiZkConfigManager.getKeys());
	}
}
