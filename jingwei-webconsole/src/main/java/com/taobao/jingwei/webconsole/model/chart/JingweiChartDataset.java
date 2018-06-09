package com.taobao.jingwei.webconsole.model.chart;

import java.util.List;

public class JingweiChartDataset {
	private String seriesName;
	private List<JingweiChartDataItem> data;

	public String getSeriesName() {
		return seriesName;
	}

	public void setSeriesName(String seriesName) {
		this.seriesName = seriesName;
	}

	public List<JingweiChartDataItem> getData() {
		return data;
	}

	public void setData(List<JingweiChartDataItem> data) {
		this.data = data;
	}

}
