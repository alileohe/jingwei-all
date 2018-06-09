package com.taobao.jingwei.monitor.listener;

import com.taobao.jingwei.common.config.ChildChangeListener;
import com.taobao.jingwei.monitor.core.MonitorCoreThread;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * @desc /jingwei/monitors/monitors���ӽڵ�仯
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Jun 13, 2012 5:14:46 PM
 */

public class MonitorChildListener extends ChildChangeListener {
	private final Log log = LogFactory.getLog(MonitorChildListener.class);

	private final MonitorCoreThread monitorCoreThread;

	public MonitorChildListener(MonitorCoreThread monitorCoreThread) {
		this.monitorCoreThread = monitorCoreThread;
	}

	@Override
	public void handleChild(String parentPath, List<String> currentChilds) {

		if (currentChilds == null || currentChilds.isEmpty()) {
			//  ȫ����ղ�ֹͣɨ������ɾ�����ؾ�����
			this.monitorCoreThread.deleteAllMonitorTasks();
			log.warn("all monitor are disappeared!");

			return;
		}

		this.monitorCoreThread.balance(currentChilds);

	}
}
