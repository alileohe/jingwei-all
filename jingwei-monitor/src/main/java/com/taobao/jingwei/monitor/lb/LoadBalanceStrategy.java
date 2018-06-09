package com.taobao.jingwei.monitor.lb;

import java.util.List;

/**
 * @desc ָ�������߿��Էֵ��ķ��� ����monitor�monitor�������ߣ���ص������group�Ƿ�����
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Jun 13, 2012 5:39:42 PM
 */

public interface LoadBalanceStrategy {
	/**
	 * 
	 * @param consumerId  monitor������
	 * @param curPartitions ��������task��group���ͣ�
	 * @return
	 */
	List<String> getPartitions(String consumerId, final List<String> curPartitions);
}
