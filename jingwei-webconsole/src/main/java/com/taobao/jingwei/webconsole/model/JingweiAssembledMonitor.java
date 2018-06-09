package com.taobao.jingwei.webconsole.model;

public class JingweiAssembledMonitor implements Comparable<JingweiAssembledMonitor> {

	/** 如果启动对任务的监控 则为START，否则是STOP */
	public static final String START = "START";
	public static final String STOP = "STOP";

	/** task或者group名 */
	private final String name;

	/** 是否是group类型的配置 */
	private final boolean isGroup;

	private String operate;

	/** 运行这个监控配置的monitor名 */
	private String monitorName;

	public JingweiAssembledMonitor(String name, boolean isGroup) {
		this.name = name;
		this.isGroup = isGroup;
	}

	@Override
	public int compareTo(JingweiAssembledMonitor o) {

		return name.compareTo(o.getName());
	}

	public String getOperate() {
		return operate;
	}

	public void setOperate(String operate) {
		this.operate = operate;
	}

	public String getName() {
		return name;
	}

	public boolean isGroup() {
		return isGroup;
	}

	public String getMonitorName() {
		return monitorName;
	}

	public void setMonitorName(String monitorName) {
		this.monitorName = monitorName;
	}
}
