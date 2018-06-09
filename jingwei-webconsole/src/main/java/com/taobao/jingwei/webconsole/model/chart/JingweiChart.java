package com.taobao.jingwei.webconsole.model.chart;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JingweiChart {
	private String caption;
	private String xAxisName;
	private String yAxisName;
	private String max;
	private String min;
	private int chartType;
	private Map<String/* label */, String/* x */> category = new LinkedHashMap<String, String>();
	private List<JingweiChartDataset> dataset = new ArrayList<JingweiChartDataset>();

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getxAxisName() {
		return xAxisName;
	}

	public void setxAxisName(String xAxisName) {
		this.xAxisName = xAxisName;
	}

	public String getyAxisName() {
		return yAxisName;
	}

	public void setyAxisName(String yAxisName) {
		this.yAxisName = yAxisName;
	}

	public int getChartType() {
		return chartType;
	}

	public void setChartType(int chartType) {
		this.chartType = chartType;
	}

	public Map<String, String> getCategory() {
		return category;
	}

	public void setCategory(Map<String, String> category) {
		this.category = category;
	}

	public void addCategory(String label, String x) {
		this.category.put(label, x);
	}

	public List<JingweiChartDataset> getDataset() {
		return dataset;
	}

	public void setDataset(List<JingweiChartDataset> dataset) {
		this.dataset = dataset;
	}

	public void addDataset(JingweiChartDataset e) {
		this.dataset.add(e);
	}

	public String getMax() {
		return max;
	}

	public void setMax(String max) {
		this.max = max;
	}

	public String getMin() {
		return min;
	}

	public void setMin(String min) {
		this.min = min;
	}

}
