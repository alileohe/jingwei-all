package com.taobao.jingwei.webconsole.util.position;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.server.util.HttpPost;
import com.taobao.jingwei.webconsole.model.config.util.ConfigUtil;

public class MasterPositionHelper {
	private static Log logger = LogFactory.getLog(MasterPositionHelper.class);

	private static String GROUP_CONDITION = "act=queryJadeGroup&type=groupKey&keyWord=";
	private static String DBKEY_CONDITION = "act=queryGlobal&type=dbKey&keyWord=";

	/**
	 * jade cgi 字符串前缀
	 * 
	 * <pre>
	 * <li>daily http://ops.jm.taobao.net/rtools/api/jade_gate_way.do? 
	 * <li>producthttp://ops.jm.taobao.org:9999/rtools/api/jade_gate_way.do?
	 * </pre>
	 */
	private String action;

	/**
	 * show master status;show server id 005877:24951435#16162943.0 index:offset#serverid.0
	 * 
	 * @param groupName tddl groupname
	 * @param user username
	 * @param password password
	 * @param envId zookeeper id,参考jade-api
	 * 
	 *            <pre>
	 * #jade 1 : 正式环境
	 * jade.env.map1=${environment.key1}:1
	 * #jade 2 : 预发环境
	 * jade.env.map2=${environment.key2}:2
	 * 
	 * jade.env.map=${jade.env.map1},${jade.env.map2}
	 * </pre>
	 * @return <code>null</code>表示获取失败
	 * @throws GetAtomInfoFromTddlGroupFailedException
	 */
	public PositionInfo getPosition(String groupName, String user, String password, String envId)
			throws GetAtomInfoFromTddlGroupFailedException {
		List<String> dbkeys = getGroupDbKeyInfo(groupName, envId);

		// 根据group name获取ip和端口，标记是否失败；如果失败则抛异常GetAtomInfoFromTddlGroupFailedException
		boolean success = false;

		for (String dbkey : dbkeys) {
			dbkey = dbkey.substring(0, dbkey.indexOf(":"));
			AtomInfo atomInfo = getAtomInfoByDbKey(dbkey, envId);

			if (atomInfo != null) {
				// 成功获取ip、port信息
				success = true;
				try {

					String ip = atomInfo.getIp();
					int port = atomInfo.getPort();

					String position = getMysqlPosition(ip, port, user, password);

					PositionInfo positionInfo = new PositionInfo();
					positionInfo.setIp(ip);
					positionInfo.setPort(port);
					positionInfo.setPosition(position);

					return positionInfo;
				} catch (Exception e) {
					logger.error("get position error. ip = " + atomInfo.getIp(), e);
				}
			}

			// 根据group name获取ip和端口，标记是否失败；如果失败则抛异常GetAtomInfoFromTddlGroupFailedException
			if (!success) {
				throw new GetAtomInfoFromTddlGroupFailedException(groupName);
			}
		}

		return null;

	}

	public static class PositionInfo {
		String ip;
		int port;
		String position;

		public String getIp() {
			return ip;
		}

		public void setIp(String ip) {
			this.ip = ip;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public String getPosition() {
			return position;
		}

		public void setPosition(String position) {
			this.position = position;
		}
	}

	/**
	 * http://ops.jm.taobao.net/rtools/api/jade_gate_way.do?act=queryGlobal&keyWord=MYSQL_13_UIC_00&envId=3&type=dbKey
	 * 
	 * @param groupName
	 * @return
	 */
	public String getQueryGroupUrl(String groupName, String envId) {
		StringBuilder url = new StringBuilder(this.getQueryGroup());
		url.append(groupName).append("&envId=").append(envId);
		return url.toString();
	}

	/**
	 * http://ops.jm.taobao.net/rtools/api/jade_gate_way.do?act=queryJadeGroup&type=groupKey&keyWord=REBATE_APP_GROUP&envId=2
	 * 
	 * @param groupName
	 * @return
	 */
	public String getDbkeyUrl(String dbkey, String envId) {
		return new StringBuilder(this.getQueryDbkey()).append(dbkey).append("&envId=").append(envId).toString();
	}

	/**
	 * @param url
	 * @return <code>null</code>表示获取失败，或不存在
	 */
	public List<String> getGroupDbKeyInfo(String groupName, String envId) {
		String url = getQueryGroupUrl(groupName, envId);
		logger.warn("get group url : " + url);
		List<String> dbKeys = new ArrayList<String>();

		String response = null;
		try {
			response = HttpPost.doPost(url);
		} catch (IOException e1) {
			logger.error(e1);
			e1.printStackTrace();
		}

		if (StringUtil.isBlank(response)) {
			return null;
		}

		JSONObject obj;
		try {

			obj = new JSONObject(response);

			Boolean isSuccess = Boolean.valueOf(obj.getBoolean("isSuccess"));

			if (isSuccess) {
				JSONArray resultArray = obj.getJSONArray("result");

				for (int i = 0; i < resultArray.length(); i++) {
					JSONObject result = (JSONObject) resultArray.get(i);

					JSONArray dataArray = result.getJSONArray("data");

					for (int j = 0; j < dataArray.length(); j++) {
						JSONObject data = (JSONObject) dataArray.get(j);
						String content = data.getString("content");
						if (StringUtil.isNotBlank(content)) {
							dbKeys.addAll(ConfigUtil.commaSepString2List(content));
						}
					}
				}
			}
		} catch (JSONException e) {
			logger.error("convert group string error", e);
			e.printStackTrace();
		}

		return dbKeys;
	}

	/**
	 * @param dbKey
	 * @return
	 */
	public AtomInfo getAtomInfoByDbKey(String dbKey, String envId) {

		String url = getDbkeyUrl(dbKey, envId);

		String response = null;
		try {
			response = HttpPost.doPost(url);
		} catch (IOException e1) {
			logger.error(e1);
			e1.printStackTrace();
		}

		if (StringUtil.isBlank(response)) {
			return null;
		}

		JSONObject obj;
		try {

			obj = new JSONObject(response);

			Boolean isSuccess = Boolean.valueOf(obj.getBoolean("isSuccess"));

			if (isSuccess) {
				JSONArray resultArray = obj.getJSONArray("result");

				for (int i = 0; i < resultArray.length(); i++) {
					JSONObject result = (JSONObject) resultArray.get(i);

					JSONArray dataArray = result.getJSONArray("data");

					if (dataArray.length() > 0) {
						JSONObject data = (JSONObject) dataArray.get(0);

						int port = data.getInt("port");
						String ip = data.getString("ip");
						String status = data.getString("status");

						AtomInfo atomInfo = new AtomInfo();

						atomInfo.setIp(ip);
						atomInfo.setPort(port);
						atomInfo.setStatus(status);

						return atomInfo;
					}
				}
			}
		} catch (JSONException e) {
			logger.error("convert group string error", e);
			e.printStackTrace();
		}

		return null;
	}

	public static void main(String[] args) {
		// String url = getQueryGroupUrl("JW_TMALL_INVENTORY_02_GROUP", "3");
		// System.out.println(getGroupDbKeyInfo(url));

		// String url = getDbkeyUrl("MYSQL_13_UIC_00", "3");
		// System.out.println(getAtomInfoByDbKey("MYSQL_13_UIC_00", "3"));
		// System.out.println(getPosition("UNION_PAYMENT_00_GROUP", "jw_sync", "jw_sync", "3"));
	}

	/*
	* Find the last event ID from master db.
	*/
	public static final String getMysqlPosition(String ip, int port, String user, String password) throws SQLException,
			ClassNotFoundException, ShowMasterStatusEmptyException, ShowServerIdEmptyException {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = getConnection(ip, port, user, password);
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SHOW MASTER STATUS");
			if (!rs.next()) {
				throw new ShowMasterStatusEmptyException("Run 'SHOW MASTER STATUS' return empty!");
			}

			String fileName = rs.getString("File"); // File
			final long binlogOffset = rs.getLong("Position"); // Position

			if (rs != null) {
				rs.close();
			}

			rs = stmt.executeQuery("show variables like '%server_id%'");
			if (!rs.next()) {
				throw new ShowServerIdEmptyException("Run 'show variables like '%server_id%' return empty!");
			}

			Long masterId = rs.getLong("Value");

			return ConfigUtil.getMysqlBinlogPosition(fileName, binlogOffset, masterId);
		} finally {
			closeResources(conn, stmt, rs);
		}

	}

	/**
	 * 鑾峰彇mysql jdbc杩炴帴
	 * 
	 * @param ip
	 * @param port
	 * @param user
	 * @param password
	 * @return
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public static Connection getConnection(String ip, int port, String user, String password) throws SQLException,
			ClassNotFoundException {
		StringBuilder url = new StringBuilder("jdbc:mysql://");
		url.append(ip).append(":");
		url.append(port).append("?");
		url.append("user=").append(user);
		url.append("&password=").append(password);

		Class.forName("com.mysql.jdbc.Driver");
		Connection con = DriverManager.getConnection(url.toString());

		return con;
	}

	/**
	 * Close open resources like connection, statement, resultset.
	 */
	public static void closeResources(Connection conn, Statement stmt, ResultSet rs) {
		try {
			if (rs != null)
				rs.close();
		} catch (SQLException e) {
			logger.warn("Release JDBC result-set failed.", e);
		}

		try {
			if (stmt != null)
				stmt.close();
		} catch (SQLException e) {
			logger.warn("Release JDBC statement failed.", e);
		}

		try {
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
			logger.warn("Release JDBC connection set failed.", e);
		}
	}

	static class AtomInfo {
		private String ip;
		private int port;
		private String status;

		public String getIp() {
			return ip;
		}

		public void setIp(String ip) {
			this.ip = ip;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}
	}

	public String getQueryGroup() {
		return this.getAction() + GROUP_CONDITION;
	}

	public String getQueryDbkey() {
		return this.getAction() + DBKEY_CONDITION;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
}
