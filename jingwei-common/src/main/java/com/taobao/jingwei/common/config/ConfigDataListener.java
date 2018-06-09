package com.taobao.jingwei.common.config;

import com.alibaba.common.lang.StringUtil;
import org.I0Itec.zkclient.IZkDataListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**���ݱ仯������
 * @author qihao
 *
 */
public abstract class ConfigDataListener implements IZkDataListener {

	private static Log logger = LogFactory.getLog(ConfigDataListener.class);

	/**���ݱ仯�ص�����
	 * @param dataIdOrPath ָ��zk��path����dataId
	 * @param data �仯�����ݣ�������ݱ�ɾ����Ϊnull
	 */
	public abstract void handleData(String dataIdOrPath, String data);

	public void handleDataChange(String dataPath, Object data) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("[ConfigDataListener] " + dataPath + " dataChange new data:"
					+ StringUtil.defaultIfBlank((String) data, "NULL"));
		}

		this.handleData(dataPath, (String) data);
	}

	public void handleDataDeleted(String dataPath) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("[ConfigDataListener] " + dataPath + " has bean delete!");
		}
		this.handleData(dataPath, null);
	}
}