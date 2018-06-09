package com.taobao.jingwei.monitor.core;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @desc 
 * 
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Aug 9, 2012 1:44:38 PM
 */

final public class RunningHeartCount {
	/** 上次发出心跳告警的时间 */
	private volatile Long lastAlarmHeartBeatTime = Long.valueOf(0L);

	/** 上一次heartBeat时间戳 */
	private volatile Long lastHeartBeatTime = Long.valueOf(0L);

	/** 心跳检测失败的次数大于或等于HEART_BEAT_ALARM_LIMIT的时候才发出告警 ，单任务时使用 */
	private AtomicInteger heartBeartContinueCnt = new AtomicInteger(0);

	public Long getLastHeartBeatTime() {
		return lastHeartBeatTime;
	}

	public void setLastHeartBeatTime(Long lastHeartBeatTime) {
		this.lastHeartBeatTime = lastHeartBeatTime;
	}

	public AtomicInteger getHeartBeartContinueCnt() {
		return heartBeartContinueCnt;
	}

	public void setHeartBeartContinueCnt(AtomicInteger heartBeartContinueCnt) {
		this.heartBeartContinueCnt = heartBeartContinueCnt;
	}

	public Long getLastAlarmHeartBeatTime() {
		return lastAlarmHeartBeatTime;
	}

	public void setLastAlarmHeartBeatTime(Long lastAlarmHeartBeatTime) {
		this.lastAlarmHeartBeatTime = lastAlarmHeartBeatTime;
	}
}
