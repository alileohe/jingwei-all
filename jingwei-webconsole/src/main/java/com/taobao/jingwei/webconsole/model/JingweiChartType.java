package com.taobao.jingwei.webconsole.model;

public enum JingweiChartType {
	LINE(0), BAR(1), PIE(2);

	private int type;

	private JingweiChartType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public JingweiChartType getType(int type) {
		for (JingweiChartType t : JingweiChartType.values()) {
			if (t.getType() == type) {
				return t;
			}
		}
		return null;
	}
}
