/**
 * 
 */
package com.taobao.jingwei.webconsole.model;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.taobao.jingwei.common.config.ChildChangeListener;
import com.taobao.jingwei.common.config.ConfigDataListener;

/**
 * 精卫信息存储类
 * 
 * @author qingren
 * 
 */
public class JingweiInfoHolder {

	/**
	 * 全局锁
	 */
	public static Lock lock = new ReentrantLock();

	/**
	 * 是否第一次被使用（用于同步数据）
	 */
	public static boolean firstTime = true;

	public final static ConcurrentHashMap<String/* node name */, JingweiAssembledTask> taskInfo = new ConcurrentHashMap<String, JingweiAssembledTask>();

	/**
	 * 本节点数据监听器
	 */
	public final static ConcurrentHashMap<String/* node path */, ConfigDataListener> dataListeners = new ConcurrentHashMap<String, ConfigDataListener>();

	/**
	 * 子节点数量监听器
	 */
	public final static ConcurrentHashMap<String/* node path */, ChildChangeListener> childChangeListeners = new ConcurrentHashMap<String, ChildChangeListener>();

}
