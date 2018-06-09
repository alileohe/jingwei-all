package com.taobao.jingwei.webconsole.model;

public class JingweiAssembledMonitor implements Comparable<JingweiAssembledMonitor> {

	/** �������������ļ�� ��ΪSTART��������STOP */
	public static final String START = "START";
	public static final String STOP = "STOP";

	/** task����group�� */
	private final String name;

	/** �Ƿ���group���͵����� */
	private final boolean isGroup;

	private String operate;

	/** �������������õ�monitor�� */
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
