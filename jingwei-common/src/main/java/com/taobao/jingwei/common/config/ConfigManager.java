package com.taobao.jingwei.common.config;

import java.util.Map;

/**
 * ������Ϣ������������ʹ��zk����dimondʵ��
 *
 * @author qihao
 */
public interface ConfigManager {

	public void init();

	public void destory();

	/**
	 * ָ��zk��path����dataId��ȡ����
	 *
	 * @param dataIdOrPath
	 * @return
	 */
	public String getData(String dataIdOrPath);

	/**
	 * ��ȡ��·���µĸ��ӽڵ����Ϣ
	 *
	 * @param dataParentIdOrPath
	 * @param pattern
	 * @return Map<childName,dataString>
	 */
	public Map<String, String> getChildDatas(String dataParentIdOrPath, String pattern);

	/**
	 * �ж�ָ��zk��path����dataId�Ƿ���� ע����ָ�ж�ָ��zk��path����dataId�Ƿ���ڶ������ݵĴ���
	 *
	 * @param path
	 * @return
	 */
	public boolean exists(String dataIdOrPath);

	/**��������
	 * @param dataIdOrPath
	 * @param data
	 * @param isPersistent
	 * @throws Exception
	 */
	public void publishData(String dataIdOrPath, String data, boolean isPersistent) throws Exception;

	/**
	 * ָ��zk��path����dataId�������� ע��������ݲ������򴴽�ָ������
	 *
	 * @param dataIdOrPath
	 * @param data
	 * @throws Exception
	 */
	public void updateData(String dataIdOrPath, String data, boolean isPersistent) throws Exception;

	/**
	 * �������߸���ָ��zk��path����dataId������ ע�����ڶ������exists���һ�����罻��
	 *
	 * @param path
	 * @param data
	 * @throws Exception
	 */
	public void publishOrUpdateData(String dataIdOrPath, String data, boolean isPersistent) throws Exception;

	/**���������е�����
	 * @param path
	 * @param data
	 * @throws Exception
	 */
	public String publishDataSequential(String path, String data, boolean isPersistent) throws Exception;

	/**
	 * ���zk��path����data�����ݼ�����
	 *
	 * @param dataIdOrPath
	 * @param listener
	 */
	public void addDataListener(String dataIdOrPath, final ConfigDataListener listener);

	/**
	 * �Ƴ����ݼ�����
	 *
	 * @param path
	 * @param listener
	 */
	public void removeDataListener(String path, final ConfigDataListener listener);

	/**
	 * ���ָ�����ڵ��µ��ӽڵ����
	 *
	 * @param path
	 * @param childChangeListener
	 */
	public void addChildChangesListener(String path, final ChildChangeListener childChangeListener);

	/**
	 * ɾ��ָ�����ڵ��µ��ӽڵ����
	 *
	 * @param path
	 * @param childChangeListener
	 */
	public void removeChildChangesListener(String path, final ChildChangeListener childChangeListener);

	/**
	 * ɾ��ָ��zk��path����dataId������
	 *
	 * @param dataIdOrPath
	 * @throws Exception
	 */
	public void delete(String dataIdOrPath) throws Exception;

	/**����Session������
	 * @param sessionStateListener
	 */
	public void addSessionStateListener(final SessionStateListener sessionStateListener) ;
	
	/**ɾ�����õ�sessionStateListener
	 * @param sessionStateListener
	 */
	public void removeSessionStateListener( SessionStateListener sessionStateListener);
}
