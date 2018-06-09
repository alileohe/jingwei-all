package com.taobao.jingwei.webconsole.model;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class JingweiHttpCondition {
	private final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private int statServer;
	private String key1;
	private String key2;
	private String key3;
	private String startTime;
	private String endTime;
	private String hostName;
	private String interval;
	private int week;
	private boolean noYesterday;
	private int chartType;
	private int timeout;

	private long summaryPeriod;

	public int getStatServer() {
		return statServer;
	}

	public void setStatServer(int statServer) {
		this.statServer = statServer;
	}

	public String getKey1() {
		return key1;
	}

	public void setKey1(String key1) {
		this.key1 = key1;
	}

	public String getKey2() {
		return key2;
	}

	public void setKey2(String key2) {
		this.key2 = key2;
	}

	public String getKey3() {
		return key3;
	}

	public void setKey3(String key3) {
		this.key3 = key3;
	}

	public String getStartTime() {
		return startTime;
	}

	public String getStartTimeEncoded() {
		try {
			return URLEncoder.encode(startTime + " 00:00:00", "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return startTime;
	}

	public long getStartTimestamp() {
		try {
			return DF.parse(startTime + " 00:00:00").getTime();
		} catch (ParseException e) {
			//ºöÂÔ´íÎó¡£²»¿ÉÄÜ´íÎó
		}
		return 0L;
	}

	public long getEndTimestamp() {
		try {
			return DF.parse(endTime + " 23:59:59").getTime();
		} catch (ParseException e) {
			//ºöÂÔ´íÎó¡£²»¿ÉÄÜ´íÎó
		}
		return 0L;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public String getEndTimeEncoded() {
		try {
			return URLEncoder.encode(endTime + " 23:59:59", "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getInterval() {
		return interval;
	}

	public void setInterval(String interval) {
		this.interval = interval;
	}

	public boolean isNoYesterday() {
		return noYesterday;
	}

	public void setNoYesterday(boolean noYesterday) {
		this.noYesterday = noYesterday;
	}

	public int getChartType() {
		return chartType;
	}

	public void setChartType(int chartType) {
		this.chartType = chartType;
	}

	public int getWeek() {
		return week;
	}

	public void setWeek(int week) {
		this.week = week;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public long getSummaryPeriod() {
		return summaryPeriod;
	}

	public void setSummaryPeriod(long summaryPeriod) {
		this.summaryPeriod = summaryPeriod;
	}

}
