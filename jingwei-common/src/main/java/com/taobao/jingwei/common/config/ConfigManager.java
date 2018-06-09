package com.taobao.jingwei.common.config;

import java.util.Map;

/**
 * 配置信息管理器，可以使用zk或者dimond实现
 *
 * @author qihao
 */
public interface ConfigManager {

	public void init();

	public void destory();

	/**
	 * 指定zk的path或者dataId获取数据
	 *
	 * @param dataIdOrPath
	 * @return
	 */
	public String getData(String dataIdOrPath);

	/**
	 * 获取该路径下的各子节点的信息
	 *
	 * @param dataParentIdOrPath
	 * @param pattern
	 * @return Map<childName,dataString>
	 */
	public Map<String, String> getChildDatas(String dataParentIdOrPath, String pattern);

	/**
	 * 判断指定zk的path或者dataId是否存在 注：是指判断指定zk的path或者dataId是否存在而非数据的存在
	 *
	 * @param path
	 * @return
	 */
	public boolean exists(String dataIdOrPath);

	/**发布数据
	 * @param dataIdOrPath
	 * @param data
	 * @param isPersistent
	 * @throws Exception
	 */
	public void publishData(String dataIdOrPath, String data, boolean isPersistent) throws Exception;

	/**
	 * 指定zk的path或者dataId更新数据 注：如果数据不存在则创建指定数据
	 *
	 * @param dataIdOrPath
	 * @param data
	 * @throws Exception
	 */
	public void updateData(String dataIdOrPath, String data, boolean isPersistent) throws Exception;

	/**
	 * 发布或者更新指定zk的path或者dataId的数据 注：由于多调用了exists会多一次网络交互
	 *
	 * @param path
	 * @param data
	 * @throws Exception
	 */
	public void publishOrUpdateData(String dataIdOrPath, String data, boolean isPersistent) throws Exception;

	/**发布带序列的数据
	 * @param path
	 * @param data
	 * @throws Exception
	 */
	public String publishDataSequential(String path, String data, boolean isPersistent) throws Exception;

	/**
	 * 添加zk的path或者data的数据监听器
	 *
	 * @param dataIdOrPath
	 * @param listener
	 */
	public void addDataListener(String dataIdOrPath, final ConfigDataListener listener);

	/**
	 * 移除数据监听器
	 *
	 * @param path
	 * @param listener
	 */
	public void removeDataListener(String path, final ConfigDataListener listener);

	/**
	 * 添加指定父节点下的子节点监听
	 *
	 * @param path
	 * @param childChangeListener
	 */
	public void addChildChangesListener(String path, final ChildChangeListener childChangeListener);

	/**
	 * 删除指定父节点下的子节点监听
	 *
	 * @param path
	 * @param childChangeListener
	 */
	public void removeChildChangesListener(String path, final ChildChangeListener childChangeListener);

	/**
	 * 删除指定zk的path或者dataId的数据
	 *
	 * @param dataIdOrPath
	 * @throws Exception
	 */
	public void delete(String dataIdOrPath) throws Exception;

	/**设置Session监听器
	 * @param sessionStateListener
	 */
	public void addSessionStateListener(final SessionStateListener sessionStateListener) ;
	
	/**删除设置的sessionStateListener
	 * @param sessionStateListener
	 */
	public void removeSessionStateListener( SessionStateListener sessionStateListener);
}
