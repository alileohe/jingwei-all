package com.taobao.jingwei.webconsole.model;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

public class JingweiBinlogExtractor {
	private static final String REPLICATOR = "replicator.properties";
	private static final String NEW_REPLICATOR = "replicator-new.properties";

	private static final String template;
	private static final String newTemplate;
	private static final Properties property = new Properties();
	private static final Properties newProperty = new Properties();

	private String master;
	private int port;
	private String user;
	private String password;
	private String dbRegex;
	private String tabRegex;
	private String charset;

	private boolean complexity;
	private String binData;
	private String newBinData;

	private boolean binlogAutoSwitch;
	private String binlogSwitchPolicy;
	private String binlogGroupName;

	static {
		template = readTemplateString(REPLICATOR);
		newTemplate = readTemplateString(NEW_REPLICATOR);
		initProperty(REPLICATOR);
		initProperty(NEW_REPLICATOR);
	}

	public static String getTemplate() {
		return template;
	}

	public static String getNewTemplate() {
		return newTemplate;
	}

	private static String readTemplateString(String filename) {
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(
					JingweiBinlogExtractor.class.getClassLoader()
							.getResource(filename).getFile()));
			String tmp = null;
			while ((tmp = reader.readLine()) != null) {
				sb.append(tmp);
				sb.append(System.getProperty("line.separator"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
			}
		}
		return sb.toString();
	}

	private static void initProperty(String filename) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(JingweiBinlogExtractor.class
					.getClassLoader().getResource(filename).getFile());
			if (filename.equals(REPLICATOR)) {
				property.load(fis);
			} else {
				newProperty.load(fis);
			}
		} catch (Exception e) {
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (Exception e2) {
			}
		}
	}

	public String getExtractorData() {
		if (complexity) {
			if (binlogAutoSwitch) {
				return newBinData;
			} else {
				return binData;
			}
		} else {
			return getDataFromProperties(binlogAutoSwitch ? newProperty
					: property);
		}
	}

	public String getDataFromProperties(Properties orgPro) {
		Properties pro = (Properties) orgPro.clone();
		StringBuilder sb = new StringBuilder();

		if (binlogAutoSwitch) {
			pro.setProperty("replicator.extractor.mysql.charset",
					charset == null ? "" : charset);
		} else {
			pro.setProperty("replicator.global.db.port", port + "");
			pro.setProperty("replicator.global.master", master == null ? ""
					: master);
			pro.setProperty("replicator.plugin.directRelay.charset",
					charset == null ? "" : charset);
		}
		pro.setProperty("replicator.global.db.user", user == null ? "" : user);
		pro.setProperty("replicator.global.db.password", password == null ? ""
				: password);
		pro.setProperty("replicator.global.filter.dbRegex",
				dbRegex == null ? "" : dbRegex);
		pro.setProperty("replicator.global.filter.tabRegex",
				tabRegex == null ? "" : tabRegex);

		Iterator<Entry<Object, Object>> iter = pro.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Object, Object> entry = iter.next();
			sb.append(entry.getKey() + "=" + entry.getValue());
			sb.append(System.getProperty("line.separator"));
		}
		return sb.toString();
	}

	public String getNewBinData() {
		return newBinData;
	}

	public void setNewBinData(String newBinData) {
		this.newBinData = newBinData;
	}

	public boolean isBinlogAutoSwitch() {
		return binlogAutoSwitch;
	}

	public void setBinlogAutoSwitch(boolean binlogAutoSwitch) {
		this.binlogAutoSwitch = binlogAutoSwitch;
	}

	public String getBinlogSwitchPolicy() {
		return binlogSwitchPolicy;
	}

	public void setBinlogSwitchPolicy(String binlogSwitchPolicy) {
		this.binlogSwitchPolicy = binlogSwitchPolicy;
	}

	public String getBinlogGroupName() {
		return binlogGroupName;
	}

	public void setBinlogGroupName(String binlogGroupName) {
		this.binlogGroupName = binlogGroupName;
	}

	public String getMaster() {
		return master;
	}

	public void setMaster(String master) {
		this.master = master;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDbRegex() {
		return dbRegex;
	}

	public void setDbRegex(String dbRegex) {
		this.dbRegex = dbRegex;
	}

	public String getTabRegex() {
		return tabRegex;
	}

	public void setTabRegex(String tabRegex) {
		this.tabRegex = tabRegex;
	}

	public boolean isComplexity() {
		return complexity;
	}

	public void setComplexity(boolean complexity) {
		this.complexity = complexity;
	}

	public String getBinData() {
		return binData;
	}

	public void setBinData(String binData) {
		this.binData = binData;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

}
