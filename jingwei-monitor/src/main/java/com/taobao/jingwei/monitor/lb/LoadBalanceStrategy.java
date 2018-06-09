package com.taobao.jingwei.monitor.lb;

import java.util.List;

/**
 * @desc 指定消费者可以分担的分区 （在monitor里，monitor是消费者，监控的任务和group是分区）
 * 
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Jun 13, 2012 5:39:42 PM
 */

public interface LoadBalanceStrategy {
	/**
	 * 
	 * @param consumerId  monitor主机名
	 * @param curPartitions 任务名（task和group类型）
	 * @return
	 */
	List<String> getPartitions(String consumerId, final List<String> curPartitions);
}
