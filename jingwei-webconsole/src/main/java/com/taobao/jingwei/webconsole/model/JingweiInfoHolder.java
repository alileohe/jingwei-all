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
 * ������Ϣ�洢��
 * 
 * @author qingren
 * 
 */
public class JingweiInfoHolder {

	/**
	 * ȫ����
	 */
	public static Lock lock = new ReentrantLock();

	/**
	 * �Ƿ��һ�α�ʹ�ã�����ͬ�����ݣ�
	 */
	public static boolean firstTime = true;

	public final static ConcurrentHashMap<String/* node name */, JingweiAssembledTask> taskInfo = new ConcurrentHashMap<String, JingweiAssembledTask>();

	/**
	 * ���ڵ����ݼ�����
	 */
	public final static ConcurrentHashMap<String/* node path */, ConfigDataListener> dataListeners = new ConcurrentHashMap<String, ConfigDataListener>();

	/**
	 * �ӽڵ�����������
	 */
	public final static ConcurrentHashMap<String/* node path */, ChildChangeListener> childChangeListeners = new ConcurrentHashMap<String, ChildChangeListener>();

}
