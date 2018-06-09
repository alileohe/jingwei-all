package com.taobao.jingwei.monitor.conf;

/**
 * @desc 启动配置对象，由指定的INI文件生成
 * @author <a href="mailto:shuohai.lhl@taobao.com">shuohailhl</a>
 * @date 2011-11-14下午2:28:13
 */

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.JingWeiUtil;

public class MonitorConfig implements JingWeiConstants {

	private String monitorName;

	public MonitorConfig(String monitorName) {
		this.monitorName = monitorName;
	}

	public static MonitorConfig getMonitorConfig() {
		MonitorConfig monitorConfig = null;

		String serverName = JingWeiUtil.getLocalHostName();

		if (StringUtil.isNotEmpty(serverName)) {
			monitorConfig = new MonitorConfig(serverName);
		}

		return monitorConfig;

	}

	public String getMonitorName() {
		return monitorName;
	}

	public void setMonitorName(String monitorName) {
		this.monitorName = monitorName;
	}
}