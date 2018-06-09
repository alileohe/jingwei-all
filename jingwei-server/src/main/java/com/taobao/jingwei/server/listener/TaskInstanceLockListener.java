package com.taobao.jingwei.server.listener;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.config.ConfigDataListener;
import com.taobao.jingwei.server.group.CandidateTaskCoreLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @desc jingwei/tasks/**task/t-locks/lock1 �ڵ���ʧ���߽ڵ�����ݲ��Ǳ�sever����ʱ��ص��������˳�
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date May 24, 2012 5:20:42 PM
 */

public class TaskInstanceLockListener extends ConfigDataListener {

	private Log log = LogFactory.getLog(this.getClass());

	private final CandidateTaskCoreLoader candidateTaskCoreLoader;

	private final String taskName;

	public TaskInstanceLockListener(CandidateTaskCoreLoader groupTaskCoreLoader, String taskName) {
		this.candidateTaskCoreLoader = groupTaskCoreLoader;
		this.taskName = taskName;
	}

	@Override
	public void handleData(String dataIdOrPath, String data) {
		if (StringUtil.isBlank(data)) {
			log.warn("[jingwei server] group task lock data is blank " + data + " for task : " + taskName);
			log.error("destroy zk manager and exit jingwei task ");
			candidateTaskCoreLoader.finish();
		} else {
			if (!data.equals(candidateTaskCoreLoader.getServerName())) {
				log.warn("[jingwei server] group task lock data is other server " + data + " for task : " + taskName);
				log.error("destroy zk manager and exit jingwei task ");
				candidateTaskCoreLoader.finish();
			}
		}

	}

}
