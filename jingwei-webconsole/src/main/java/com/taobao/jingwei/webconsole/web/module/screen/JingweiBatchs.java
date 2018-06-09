package com.taobao.jingwei.webconsole.web.module.screen;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.webconsole.biz.ao.JingweiBatchAO;
import com.taobao.jingwei.webconsole.model.config.ConfigHolder;

public class JingweiBatchs implements JingWeiConstants {

	private static Log log = LogFactory.getLog(JingweiBatchs.class);

	@Autowired
	private JingweiBatchAO jwBatchAO;

	public void execute(Context context, @Param(name = "host") String host,
			@Param(name = "groupNameCriteria") String groupName, @Param(name = "page") String page,
			@Param(name = "pageSize") String pageSize) {

		context.put("configHolder", ConfigHolder.getInstance());
	}

}
