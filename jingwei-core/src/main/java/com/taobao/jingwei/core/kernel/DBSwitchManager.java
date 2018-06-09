package com.taobao.jingwei.core.kernel;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.DiamondHelper;
import com.taobao.jingwei.common.DiamondHelper.DataListener;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.JingWeiUtil;
import com.taobao.tddl.dbsync.DbsyncException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
*
<p>
description:���ݿ��л������࣬��JADE���ϸ���JADE�������Ƿ�������л��л�
Ӳ�л���DBSYNC�ڲ�����
<p>
*
* SwitchManager.java Create on Sep 11, 2012 5:52:19 PM
*
* Copyright (c) 2011 by qihao.
*
*@author
<a href="mailto:qihao@taobao.com">qihao</a>
*@version 1.0
*/
public class DBSwitchManager {

	private static final Log logger = LogFactory.getLog(DBSwitchManager.class);
	/**
	 * JADE��̬���ݵ�DataId��Ӧ��FORMAT
	 */
	private static final String GLOBA_PREFIX = "com.taobao.tddl.atom.global.";
	private static final MessageFormat JADE_GLOBAL_FORMAT = new MessageFormat(GLOBA_PREFIX + "{0}");
	private static final MessageFormat JADE_GROUP_FORMAT = new MessageFormat("com.taobao.tddl.jdbc.group_V2.4.1_{0}");

	/**
	 * globa��IP��PORT��Ӧ��key
	 */
	private static final String GLOBA_IP_KEY = "ip";
	private static final String GLOBA_PORT_KEY = "port";
	private static final String GLOBA_STATUS_KEY = "dbStatus";
	private static final String GLOBA_NA_STATUS = "NA";

	private final AbstractJingWeiCore jingWeiCore;

	private final ReentrantLock lock = new ReentrantLock();

	private Map<String/*dbkey*/, DBInfo> dbMaps;

	private String groupName;

	private final DataListener groupListener = new DataListener() {

		public void receiveConfigInfo(String dataId, String configInfo) {
			if (StringUtil.isBlank(configInfo)) {
				//������Ϳ���ܽ�
				logger.warn("[DBSwitchManager] groupData notify empty! groupName: " + groupName);
				return;
			}
			Set<String> dbkeys = DBSwitchManager.this.parserGroupContext(configInfo);
			//����globa����ȥ�أ���ֹdbkey��ͬ����������ͬ
			dbkeys = uniqueDbMap(dbkeys).keySet();

			lock.lock();
			try {
				Set<String> dbKeyCache = DBSwitchManager.this.dbMaps.keySet();
				boolean needRestart = false;
				if (dbkeys.size() != dbKeyCache.size()) {
					//dbkey������ͬ,˵��group�е�dbkey����һ�������仯,ֱ������
					needRestart = true;
				} else {
					if (!dbkeys.isEmpty()) {
						//�Ƚ�����dbkey�Ƿ���ȫ��ͬ,�����ֱͬ������
						for (String nDbkey : dbkeys) {
							if (!dbKeyCache.contains(nDbkey)) {
								needRestart = true;
								break;
							}
						}
					}
				}
				if (needRestart) {
					String nDbkeys = getPrintStr(dbkeys);
					String oDbkeys = getPrintStr(dbKeyCache);
					StringBuilder sb = new StringBuilder("[DBSwitchManager] Group Context Change");
					sb.append(" oldDbkeys: ").append(oDbkeys);
					sb.append(" newDbkeys: ").append(nDbkeys);
					logger.warn(sb.toString());
					DBSwitchManager.this.restartDbsync();
				}
			} finally {
				lock.unlock();
			}
		}
	};

	private final DataListener dbListener = new DataListener() {

		public void receiveConfigInfo(String dataId, String configInfo) {
			String dbKey = StringUtil.substringAfter(dataId, GLOBA_PREFIX);
			if (StringUtil.isBlank(configInfo)) {
				//������Ϳ���ܽ�
				logger.warn("[DBSwitchManager] globaData notify empty! dbKey: " + dbKey);
				return;
			}
			DBInfo newDbInfo = DBSwitchManager.this.parserDBContext(configInfo);
			lock.lock();
			try {
				DBInfo oldDbInfo = DBSwitchManager.this.dbMaps.get(dbKey);
				if (!newDbInfo.equals(oldDbInfo)) {
					StringBuilder sb = new StringBuilder("[DBSwitchManager] Globa Context Change");
					sb.append(" dbkey: ").append(dbKey);
					sb.append(" oldContext: ").append(oldDbInfo);
					sb.append(" newContext: ").append(newDbInfo);
					logger.warn(sb.toString());
					DBSwitchManager.this.restartDbsync();
				}
			} finally {
				lock.unlock();
			}
		}
	};

	private void restartDbsync() {
		try {
			logger.warn("[DBSwitchManager]========Restart DbSync!=========");
			//�Ƴ��������Ѿ�ע���listener�ȴ�����������ע��
			DiamondHelper.cleanListener();
			DBSwitchManager.this.jingWeiCore.destoryDbsync();
			if (!DBSwitchManager.this.jingWeiCore.syncTaskNode.isSingleTask()) {
				DBSwitchManager.this.jingWeiCore.startDbsync();
			}
		} catch (Exception e) {
			logger.error("[DBSwitchManager] try restart Dbsync Error!", e);
		}
	}

	private String getPrintStr(Collection<String> c) {
		StringBuilder sb = new StringBuilder();
		Iterator<String> it = c.iterator();
		while (it.hasNext()) {
			sb.append(it.next());
			if (it.hasNext()) {
				sb.append(JingWeiConstants.COMMA_STR);
			}
		}
		return sb.toString();
	}

	public DBSwitchManager(AbstractJingWeiCore jingWeiCore) {
		this.jingWeiCore = jingWeiCore;
	}

	public String[] loadAvailableDataBaseConf(String groupName) throws DbsyncException {
		return this.loadDataBaseConf(groupName, true);
	}

	public String[] loadAllDataBaseConf(String groupName) throws DbsyncException {
		return this.loadDataBaseConf(groupName, false);
	}

	/**�����غ���Ե��øķ�����ȡIP��PORT���ַ������������Ϊ���ŷָ�
	 * ���磺
	 * String[0]="ip1,ip2,..."
	 * String[1]="port1,port2,..."
	 * @return
	 */
	private String[] loadDataBaseConf(String groupName, boolean onlyAvailable) throws DbsyncException {
		if (StringUtil.isBlank(groupName)) {
			throw new DbsyncException("[DBSwitchManager] AutoSwitch GroupName Is Empty!");
		}
		this.groupName = groupName;
		String groupData = getGroupData(groupName);
		if (StringUtil.isBlank(groupData)) {
			throw new DbsyncException("[DBSwitchManager] AutoSwitch GroupData Is Empty! groupName: " + groupName);
		}
		//��group���������ݽ�����dbkey�б�
		Set<String> dbkeys = parserGroupContext(groupData);

		//����globa����ȥ�أ���ֹdbkey��ͬ����������ͬ
		Map<String/*dbkey*/, DBInfo> dbMaps = uniqueDbMap(dbkeys);

		String result[] = new String[2];
		lock.lock();
		try {
			this.dbMaps = dbMaps;
			StringBuffer ipSb = new StringBuffer();
			StringBuffer portSb = new StringBuffer();
			Iterator<String> it = this.dbMaps.keySet().iterator();
			while (it.hasNext()) {
				DBInfo dbInfo = this.dbMaps.get(it.next());
				//�����ȫ����������ֻ����Ч���Ҳ���NA
				if (!onlyAvailable || (onlyAvailable && !dbInfo.isNa())) {
					if (!dbInfo.isNa()) {
						ipSb.append(dbInfo.getIp());
						portSb.append(dbInfo.getPort());
						if (it.hasNext()) {
							ipSb.append(JingWeiConstants.COMMA_STR);
							portSb.append(JingWeiConstants.COMMA_STR);
						}
					}
				}
			}
			result[0] = ipSb.toString();
			result[1] = portSb.toString();
		} finally {
			lock.unlock();
		}
		return result;
	}

	private Map<String/*dbkey*/, DBInfo> uniqueDbMap(Set<String> dbkeys) {
		Map<String/*dbkey*/, DBInfo> dbMaps = new HashMap<String/*dbkey*/, DBInfo>(dbkeys.size());
		for (String dbKey : dbkeys) {
			String globaData = getDBData(dbKey);
			DBInfo dbInfo = parserDBContext(globaData);
			//������ظ������Ҳ���NA����ӽ�ȥ
			if (!dbMaps.containsValue(dbInfo)) {
				dbMaps.put(dbKey, dbInfo);
			}
		}
		return dbMaps;
	}

	public String getGroupData(String groupName) {
		String dataId = JADE_GROUP_FORMAT.format(new Object[] { groupName });
		return DiamondHelper.getData(dataId);
	}

	public String getDBData(String dbKey) {
		String dataId = JADE_GLOBAL_FORMAT.format(new Object[] { dbKey });
		return DiamondHelper.getData(dataId);
	}

	public Set<String> parserGroupContext(String groupData) {
		String[] dbkeys = StringUtil.split(groupData, JingWeiConstants.COMMA_STR);
		Set<String> dbKeySet = new HashSet<String>(dbkeys.length);
		for (String dbkey : dbkeys) {
			dbKeySet.add(StringUtil.substringBefore(dbkey, JingWeiConstants.COLON_STR));
		}
		return dbKeySet;
	}

	public DBInfo parserDBContext(String globaData) {
		Properties prop = JingWeiUtil.getPropFromString(globaData);
		DBInfo dbInfo = new DBInfo(prop.getProperty(GLOBA_IP_KEY), prop.getProperty(GLOBA_PORT_KEY),
				prop.getProperty(GLOBA_STATUS_KEY));
		return dbInfo;
	}

	public String getGroupName() {
		return groupName;
	}

	public void listenerDataBaseConf() {
		String groupId = JADE_GROUP_FORMAT.format(new Object[] { groupName });
		DiamondHelper.addListener(groupId, this.groupListener);
		for (String dbkey : this.dbMaps.keySet()) {
			String dbId = JADE_GLOBAL_FORMAT.format(new Object[] { dbkey });
			DiamondHelper.addListener(dbId, this.dbListener);
		}
	}

	/**
	*
	<p>
	description:���ݿ�ȫ��IP��PORT��Ϣ��
	<p>
	*
	* SwitchManager.java Create on Sep 11, 2012 3:56:21 PM
	*
	* Copyright (c) 2011 by qihao.
	*
	*@author
	<a href="mailto:qihao@taobao.com">qihao</a>
	*@version 1.0
	*/
	class DBInfo {
		private final String ip;
		private final String port;
		private final String status;

		public DBInfo(String ip, String port, String status) {
			this.ip = ip;
			this.port = port;
			this.status = status;
		}

		public String getIp() {
			return ip;
		}

		public String getPort() {
			return port;
		}

		public String getStatus() {
			return status;
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("ip: ").append(this.ip).append(" ");
			sb.append("port: ").append(this.port).append(" ");
			sb.append("status: ").append(this.status);
			return sb.toString();
		}

		public boolean isNa() {
			return StringUtil.equalsIgnoreCase(GLOBA_NA_STATUS, StringUtil.trim(this.getStatus()));
		}

		public boolean equals(Object obj) {
			DBInfo targetObj = (DBInfo) obj;
			if (null == targetObj) {
				return false;
			}
			return StringUtil.equals(this.ip, targetObj.getIp()) && StringUtil.equals(this.port, targetObj.getPort())
					&& (this.isNa() == targetObj.isNa());
		}
	}
}