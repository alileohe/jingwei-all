package com.taobao.jingwei.server.core;

import com.taobao.jingwei.common.config.ConfigDataListener;
import com.taobao.jingwei.common.node.server.ServerTaskNode.PluginTaskTargetStateEnum;
import com.taobao.jingwei.common.node.server.ServerTaskNode.PluginTaskWorkStateEnum;

/**
 * �û������������target������޸��¼���work������޸�ʱ������������
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com">shuohailhl</a>
 * 
 * @date 2011-12-31����3:51:14
 */
public class CustomerTaskContext extends TaskContext {

	private volatile Long targetModified = Long.valueOf(0L);

	private volatile Long workLastModified = Long.valueOf(0L);

	private volatile PluginTaskTargetStateEnum targetState;

	private volatile PluginTaskWorkStateEnum workState;

	public CustomerTaskContext(String taskName, ConfigDataListener opListener) {
		super(taskName, opListener);
	}

	public Long getTargetModified() {
		return targetModified;
	}

	public void setTargetModified(Long targetModified) {
		this.targetModified = targetModified;
	}

	public Long getWorkLastModified() {
		return workLastModified;
	}

	public void setWorkLastModified(Long workLastModified) {
		this.workLastModified = workLastModified;
	}

	public PluginTaskTargetStateEnum getTargetState() {
		return targetState;
	}

	public void setTargetState(PluginTaskTargetStateEnum targetState) {
		this.targetState = targetState;
	}

	public PluginTaskWorkStateEnum getWorkState() {
		return workState;
	}

	public void setWorkState(PluginTaskWorkStateEnum workState) {
		this.workState = workState;
	}

}
