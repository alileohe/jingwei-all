package com.taobao.jingwei.monitor.core;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @desc 
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Aug 9, 2012 1:44:38 PM
 */

final public class RunningHeartCount {
	/** �ϴη��������澯��ʱ�� */
	private volatile Long lastAlarmHeartBeatTime = Long.valueOf(0L);

	/** ��һ��heartBeatʱ��� */
	private volatile Long lastHeartBeatTime = Long.valueOf(0L);

	/** �������ʧ�ܵĴ������ڻ����HEART_BEAT_ALARM_LIMIT��ʱ��ŷ����澯 ��������ʱʹ�� */
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
