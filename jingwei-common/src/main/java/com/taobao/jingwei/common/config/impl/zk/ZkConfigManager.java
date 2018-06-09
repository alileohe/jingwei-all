package com.taobao.jingwei.common.config.impl.zk;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.DiamondHelper;
import com.taobao.jingwei.common.config.ChildChangeListener;
import com.taobao.jingwei.common.config.ConfigDataListener;
import com.taobao.jingwei.common.config.ConfigManager;
import com.taobao.jingwei.common.config.SessionStateListener;
import jodd.util.Wildcard;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.Watcher.Event.KeeperState;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * configManager��zkʵ��
 *
 * @author qihao
 */
public class ZkConfigManager implements ConfigManager {

	private static Log logger = LogFactory.getLog(ZkConfigManager.class);

	/**
	 * zk�Ŀͻ���
	 */
	private ZkClient zkClient;

	/**
	 * zk�ͻ���������Ϣ
	 */
	private volatile ZkConfig zkConfig;

	/**
	 * zk�������л���
	 */
	private ZkSerializer zkSerializer;

	private Map<SessionStateListener, IZkStateListener> sessionStateListenerMap = new ConcurrentHashMap<SessionStateListener, IZkStateListener>();

	/**
	 * Ĭ��DIAMOND��ȡ���ݳ�ʱ
	 */
	private static final long DIAMOND_GET_DATA_TIMEOUT = 10 * 1000;
	private static final String ZK_DIAMOND_DATA_ID = "com.taobao.taokeeper.serverlist";

	public synchronized void init() {
		if (null == zkConfig) {
			this.zkConfig = new ZkConfig();
		}
		// ���û�������л�����������Ĭ�ϵ�
		if (null == zkSerializer) {
			zkSerializer = new ZkConfigManager.StringSerializer();
			logger.warn("[ZkConfigManager] init use default StringSerializer!");
		}
		String zkHost = this.zkConfig.zkHosts;
		if (StringUtil.isBlank(zkHost)) {
			//�Ȼ�ȡ�������ϵĵ�ַ�б�			
			zkHost = DiamondHelper.getData(ZK_DIAMOND_DATA_ID, DIAMOND_GET_DATA_TIMEOUT);
			if (StringUtil.isBlank(zkHost)) {
				throw new RuntimeException("[ZkConfigManager] init error!  get zkList form Diamond empty!");
			}
		}
		this.zkClient = new ZkClient(zkHost, zkConfig.zkSessionTimeoutMs, zkConfig.zkConnectionTimeoutMs, zkSerializer);
		//���hook ȷ��zkһ��������
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				ZkConfigManager.this.destory();
			}
		});
	}

	public synchronized void destory() {
		if (null != this.zkClient) {
			this.zkClient.close();
			this.zkClient = null;
		}
	}

	public String getData(String path) {
		return this.zkClient.readData(path, true);
	}

	public boolean exists(String path) {
		return this.zkClient.exists(path);
	}

	@Override
	public void publishData(String dataIdOrPath, String data, boolean isPersistent) throws Exception {
		if (isPersistent) {
			publishPersistentData(dataIdOrPath, data);
		} else {
			publishData(dataIdOrPath, data);
		}
	}

	private void publishPersistentData(String path, String data) throws Exception {
		try {
			// create�־ýڵ�����
			this.zkClient.createPersistent(path, data);
		} catch (ZkNoNodeException e) {
			// ����־ýڵ�ĸ��ڵ㲻���ڣ��ȴ������ڵ��ٷ��־�����
			createParentPath(path);
			this.zkClient.createPersistent(path, data);
		}

	}

	private void publishData(String path, String data) throws Exception {
		try {
			this.zkClient.createEphemeral(path, data);
		} catch (ZkNoNodeException e) {
			// ����ǳ־ýڵ㸸�ڵ㲻���ڣ��������־õĸ��ڵ�Ȼ���ٷ��ǳ־�����
			// ע���ǳ־�����һ�������ڳ־ýڵ���
			createParentPath(path);
			this.zkClient.createEphemeral(path, data);
		}
	}

	@Override
	public String publishDataSequential(String path, String data, boolean isPersistent) throws Exception {
		return isPersistent ? publisPersistentSequential(path, data) : publishDataSequential(path, data);
	}

	private String publisPersistentSequential(String path, String data) throws Exception {
		return this.zkClient.createPersistentSequential(path, data);
	}

	private String publishDataSequential(String path, String data) throws Exception {

		return this.zkClient.createEphemeralSequential(path, data);
	}

	@Override
	public void updateData(String dataIdOrPath, String data, boolean isPersistent) throws Exception {
		if (isPersistent) {
			updatePersistentData(dataIdOrPath, data);
		} else {
			updateData(dataIdOrPath, data);
		}
	}

	private void updateData(String path, String data) throws Exception {
		try {
			this.zkClient.writeData(path, data);
		} catch (ZkNoNodeException e) {
			// ����ǳ־ýڵ㸸�ڵ㲻���ڣ��������־õĸ��ڵ�Ȼ���ٷ��ǳ־�����
			// ע���ǳ־�����һ�������ڳ־ýڵ���
			createParentPath(path);
			this.zkClient.createEphemeral(path, data);
		} catch (Exception e) {
			throw e;
		}
	}

	private void updatePersistentData(String path, String data) throws Exception {
		try {
			this.zkClient.writeData(path, data);
		} catch (ZkNoNodeException e) {
			// ����־ýڵ�ĸ��ڵ㲻���ڣ��ȴ������ڵ��ٷ��־�����
			createParentPath(path);
			this.zkClient.createPersistent(path, data);
		} catch (Exception e) {
			throw e;
		}
	}

	public void publishOrUpdateData(String path, String data, boolean isPersistent) throws Exception {
		if (this.zkClient.exists(path)) {
			// ���path���������update
			this.zkClient.writeData(path, data);
		} else {
			// ��������������create
			if (isPersistent) {
				publishPersistentData(path, data);
			} else {
				publishData(path, data);
			}
		}
	}

	private void createParentPath(String path) throws Exception {
		String parentDir = path.substring(0, path.lastIndexOf('/'));
		if (parentDir.length() != 0) {
			this.zkClient.createPersistent(parentDir, true);
		}
	}

	public void addDataListener(String path, final ConfigDataListener listener) {
		if (null == listener || StringUtil.isBlank(path) || null == listener) {
			return;
		}
		this.zkClient.subscribeDataChanges(path, listener);
	}

	public void removeDataListener(String path, ConfigDataListener listener) {
		if (null == listener || StringUtil.isBlank(path) || null == listener) {
			return;
		}
		this.zkClient.unsubscribeDataChanges(path, listener);
	}

	public void addChildChangesListener(String path, ChildChangeListener childChangeListener) {
		if (null == childChangeListener || StringUtil.isBlank(path) || null == childChangeListener) {
			return;
		}
		this.zkClient.subscribeChildChanges(path, childChangeListener);
	}

	public void removeChildChangesListener(String path, ChildChangeListener childChangeListener) {
		if (null == childChangeListener || StringUtil.isBlank(path) || null == childChangeListener) {
			return;
		}
		this.zkClient.unsubscribeChildChanges(path, childChangeListener);
	}

	public void delete(String path) throws Exception {
		try {
			this.zkClient.deleteRecursive(path);
		} catch (ZkNoNodeException e) {
			logger.info("[ZkConfigManager] " + path + " deleted during connection loss; this is ok");
		} catch (Exception e) {
			throw e;
		}
	}

	public Map<String/* childrenName */, String/* dataString */> getChildDatas(String dataParentIdOrPath,
			String pattern) {
		@SuppressWarnings("unchecked")
		Map<String, String> dataMap = Collections.EMPTY_MAP;
		if (StringUtil.isBlank(dataParentIdOrPath)) {
			return dataMap;
		}
		ZkClient client = this.zkClient;
		List<String> children = null;

		try {
			children = client.getChildren(dataParentIdOrPath);
		} catch (ZkNoNodeException e) {
			return dataMap;
		}
		if (null != children && !children.isEmpty()) {
			dataMap = new HashMap<String, String>(children.size());
		}
		if (StringUtil.isBlank(pattern)) {
			for (String element : children) {
				if (StringUtil.isNotBlank(element)) {
					dataMap.put(element, StringUtil.defaultIfBlank(this.getData(dataParentIdOrPath + "/" + element),
							StringUtil.EMPTY_STRING));
				}
			}
		} else {
			if (null != children) {
				for (String element : children) {
					if (Wildcard.match(element, pattern)) {
						dataMap.put(element, this.getData(dataParentIdOrPath + "/" + element));
					}
				}
			}
		}
		return dataMap;
	}

	public ZkClient getZkClient() {
		return this.zkClient;
	}

	public ZkConfig getZkConfig() {
		return this.zkConfig;
	}

	public void setZkConfig(ZkConfig zkConfig) {
		this.zkConfig = zkConfig;
	}

	public ZkSerializer getZkSerializer() {
		return this.zkSerializer;
	}

	public void setZkSerializer(ZkSerializer zkSerializer) {
		this.zkSerializer = zkSerializer;
	}

	public void addSessionStateListener(final SessionStateListener sessionStateListener) {
		if (null != sessionStateListener) {
			IZkStateListener iZkStateListener = new IZkStateListener() {
				@Override
				public void handleStateChanged(KeeperState state) throws Exception {
					sessionStateListener.handleStateChanged(state);
				}

				@Override
				public void handleNewSession() throws Exception {
					logger.warn("[SessionStateListener] session expired!");
					if (null != sessionStateListener) {
						sessionStateListener.handleNewSession();
					}
				}
			};
			this.zkClient.subscribeStateChanges(iZkStateListener);
			this.sessionStateListenerMap.put(sessionStateListener, iZkStateListener);
		}
	}

	public void removeSessionStateListener(SessionStateListener sessionStateListener) {
		if (null != sessionStateListener) {
			IZkStateListener iZkStateListener = sessionStateListenerMap.get(sessionStateListener);
			if (null != iZkStateListener) {
				this.zkClient.unsubscribeStateChanges(iZkStateListener);
				this.sessionStateListenerMap.remove(sessionStateListener);
			}
		}
	}

	public static class StringSerializer implements ZkSerializer {
		public Object deserialize(byte[] bytes) throws ZkMarshallingError {
			try {
				return new String(bytes, "utf-8");
			} catch (UnsupportedEncodingException e) {
				throw new ZkMarshallingError(e);
			}
		}

		public byte[] serialize(Object data) throws ZkMarshallingError {
			try {
				return ((String) data).getBytes("utf-8");
			} catch (UnsupportedEncodingException e) {
				throw new ZkMarshallingError(e);
			}
		}
	}

	public static void main(String[] args) throws InterruptedException {
		ZkConfigManager zk = new ZkConfigManager();
		zk.init();
		zk.addSessionStateListener(new SessionStateListener() {
			@Override
			public void handleStateChanged(KeeperState state) throws Exception {
				System.out.println(state);
			}

			@Override
			public void handleNewSession() throws Exception {
				System.out.println("handleNewSession");
			}
		});
		//	Thread.sleep(Long.MAX_VALUE);
	}
}