package com.taobao.jingwei.common;

import com.taobao.diamond.manager.DiamondManager;
import com.taobao.diamond.manager.ManagerListener;
import com.taobao.diamond.manager.impl.DefaultDiamondManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

/**
 * description:diamond�����࣬��ǿ��װDiamondManager
 * <p/>
 * <p/>
 * DiamondHelper.java Create on Sep 7, 2012 3:33:32 PM
 * <p/>
 * Copyright (c) 2011 by qihao.
 *
 * @author <a href="mailto:qihao@taobao.com">qihao</a>
 * @version 1.0
 */
public class DiamondHelper {

	private static final Log logger = LogFactory.getLog(DiamondHelper.class);

	private final static Map<String, DiamondContext> contexts = new HashMap<String, DiamondContext>();

	private final static Executor executor = Executors.newSingleThreadScheduledExecutor();

	private final static ReentrantLock lock = new ReentrantLock();

	private final static String DEFAULT_GROUP = "DEFAULT_GROUP";

	private final static long DEFAULT_READ_TIMOUT = 3000;

	/**
	 * ��ȡָ��dataId�Ŀ�������
	 *
	 * @param dataId  ����dataId
	 * @return
	 */
	public static String getData(String dataId) {
		return getData(dataId, DEFAULT_READ_TIMOUT);
	}

	/**
	 * ������ʱ��ȡָ��dataId������
	 *
	 * @param dataId
	 * @param timeOut
	 * @return
	 */
	public static String getData(String dataId, long timeOut) {
		DiamondContext context = getContext(dataId);
		return context.getManager().getAvailableConfigureInfomation(timeOut);
	}

	/**
	 * ��ȡָ��dataId��ȫ��Ψһԭ����DiamondManager
	 *
	 * @param dataId
	 * @return
	 */
	public static DiamondManager getDiamondManager(String dataId) {
		DiamondContext context = getContext(dataId);
		return context.getManager();
	}

	/**
	 * ָ��dataId������ݼ�����
	 *
	 * @param dataId
	 * @param dataListener
	 */
	public static void addListener(String dataId, DataListener dataListener) {
		if (null != dataListener) {
			DiamondContext context = getContext(dataId);
			context.getListener().addListener(dataListener);
		}
	}

	/**
	 * ɾ��ָ��dataId��ָ��������
	 *
	 * @param dataId
	 * @param dataListener
	 */
	public static void removeListener(String dataId, DataListener dataListener) {
		if (null != dataListener) {
			DiamondContext context = getContext(dataId);
			context.getListener().removeListener(dataListener);
		}
	}

	/**
	 * ���ָ��dataId�����м�����
	 *
	 * @param dataId
	 */
	public static void cleanListener(String dataId) {
		DiamondContext context = getContext(dataId);
		context.getListener().cleanListener();
	}

	/**
	 * �������ע��ļ�����
	 */
	public static void cleanListener() {
		lock.lock();
		try {
			for (Map.Entry<String, DiamondContext> entry : contexts.entrySet()) {
				DiamondContext diamondContext = entry.getValue();
				//�Ƴ������dataId��Ӧ��listener
				diamondContext.getListener().cleanListener();
			}
		} finally {
			lock.unlock();
		}
	}

	private static DiamondContext buildContext(String dataId) {
		DiamondContext context = contexts.get(dataId);
		if (null == context) {
			lock.lock();
			try {
				context = contexts.get(dataId);
				if (null == context) {
					ControlListener controlListener = new ControlListener(dataId);
					DiamondManager diamondManager = new DefaultDiamondManager(DEFAULT_GROUP, dataId, controlListener);
					context = new DiamondContext(diamondManager, controlListener);
					contexts.put(dataId, context);
				}
			} finally {
				lock.unlock();
			}
		}
		return context;
	}

	private static DiamondContext getContext(String dataId) {
		DiamondContext context = contexts.get(dataId);
		if (null == context) {
			context = buildContext(dataId);
		}
		return context;
	}

	static class DiamondContext {
		private final ControlListener listener;
		private final DiamondManager manager;

		public DiamondContext(DiamondManager manager, ControlListener listener) {
			this.listener = listener;
			this.manager = manager;
		}

		public ControlListener getListener() {
			return listener;
		}

		public DiamondManager getManager() {
			return manager;
		}
	}

	/**
	 * <p/>
	 * description:������ʵ��diamond��Listener�ϲ�listener�����ṩ��̬��ɾ�Ĺ���
	 * <p/>
	 * <p/>
	 * DamindUtil.java Create on Sep 10, 2012 12:07:09 PM
	 * <p/>
	 * Copyright (c) 2011 by qihao.
	 *
	 * @author <a href="mailto:qihao@taobao.com">qihao</a>
	 * @version 1.0
	 */
	static class ControlListener implements ManagerListener {

		private final String dataId;

		private CopyOnWriteArrayList<DataListener> dataListeners = new CopyOnWriteArrayList<DataListener>();

		public ControlListener(String dataId) {
			this.dataId = dataId;
		}

		public void addListener(DataListener listener) {
			dataListeners.addIfAbsent(listener);
		}

		public void removeListener(DataListener listener) {
			this.dataListeners.remove(listener);
		}

		public void cleanListener() {
			this.dataListeners.clear();
		}

		public void receiveConfigInfo(String configInfo) {
			for (DataListener listener : dataListeners) {
				try {
					listener.receiveConfigInfo(dataId, configInfo);
				} catch (Exception e) {
					logger.error("call dataListener Error dataId: " + dataId, e);
				}
			}
		}

		@Override
		public Executor getExecutor() {
			return DiamondHelper.executor;
		}
	}

	/**
	 * <p>��װ�����ݼ��������ṩdataId������
	 * description:
	 * <p/>
	 * <p/>
	 * DamindManager.java Create on Sep 10, 2012 10:53:25 AM
	 * <p/>
	 * Copyright (c) 2011 by qihao.
	 *
	 * @author <a href="mailto:qihao@taobao.com">qihao</a>
	 * @version 1.0
	 */
	public interface DataListener {
		public void receiveConfigInfo(String dataId, String configInfo);
	}

	public static void main(String[] args) {
		String dataId = "qihao.qihao.qihao";
		String data = DiamondHelper.getData(dataId);
		System.out.println(data);
		DiamondHelper.addListener(dataId, new DataListener() {
			public void receiveConfigInfo(String dataId, String configInfo) {
				System.out.println(dataId + " " + configInfo + " " + this.toString());
			}
		});
		DiamondHelper.addListener(dataId, new DataListener() {
			public void receiveConfigInfo(String dataId, String configInfo) {
				System.out.println(dataId + " " + configInfo + " " + this.toString());
			}
		});
	}
}