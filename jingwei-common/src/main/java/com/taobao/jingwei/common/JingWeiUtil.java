package com.taobao.jingwei.common;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.config.ConfigManager;
import com.taobao.jingwei.common.node.type.ExtractorType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * ���õĹ�����
 *
 * @author qihao
 */
public class JingWeiUtil implements JingWeiConstants {
	private static final Log logger = LogFactory.getLog(JingWeiUtil.class);
	public static final Charset GBK_CHARSET = Charset.forName("GBK");
	private static volatile ConfigManager configManager = null;
	private static volatile String argString = null;

	private static ThreadLocal<Map<String, SimpleDateFormat>> THREAD_LOCAL_FORMATS = new ThreadLocal<Map<String, SimpleDateFormat>>();

	/**
	 * �жϵ�ǰϵͳ�Ƿ���windows
	 *
	 * @return
	 */
	public static boolean isWinOs() {
		return OS_NAME.startsWith("Windows");
	}

	public synchronized static List<String> getJingWeiTaskList() {
		List<String> taskList = new ArrayList<String>();
		List<String> processList = getProcessList();
		for (String processName : processList) {
			if (StringUtil.contains(processName, "java") && StringUtil.contains(processName, "-Djingwei.type=task")) {
				String temp = StringUtil.substringAfter(processName, "-Djingwei.task.name=");
				if (StringUtil.isNotBlank(temp)) {
					String taskName = StringUtil.substringBefore(temp, " ");
					if (StringUtil.isNotBlank(taskName)) {
						taskList.add(taskName);
					}
				}
			}
		}
		return taskList;
	}

	public synchronized static int getJingWeiTaskCount() {
		int taskCount = 0;
		List<String> processList = getProcessList();
		for (String processName : processList) {
			if (StringUtil.contains(processName, "java") && StringUtil.contains(processName, "-Djingwei.type=task")) {
				taskCount++;
			}
		}
		return taskCount;
	}

	public synchronized static int getJingWeiGroupTaskCount() {
		int taskCount = 0;
		List<String> processList = getProcessList();
		for (String processName : processList) {
			if (StringUtil.contains(processName, "java") && StringUtil.contains(processName, "-Djingwei.group=")) {
				taskCount++;
			}
		}
		return taskCount;
	}

	private static List<String> getProcessList() {
		Process process = null;
		List<String> processList = new ArrayList<String>();
		try {
			process = Runtime.getRuntime().exec(isWinOs() ? "cmd.exe   /c   tasklist" : "ps -aux");
			BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = StringUtil.EMPTY_STRING;
			while ((line = input.readLine()) != null) {
				processList.add(line);
			}
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return processList;
	}

	/**
	 * �������
	 *
	 * @param arg
	 * @return
	 * @throws RuntimeException
	 */
	public static Map<String, String> handleArgs(String arg) {
		Map<String, String> propMap = new HashMap<String, String>(1);
		String[] argpiece = arg.split(",");
		for (String argstr : argpiece) {
			String[] kv = argstr.split("=");
			if (kv.length == 2) {
				propMap.put(kv[0], kv[1]);
			} else if (kv.length == 1) {
				propMap.put(kv[0], StringUtil.EMPTY_STRING);
			} else {
				throw new RuntimeException("������ʽ�������� key1=value1,key2=value2,...");
			}
		}
		return propMap;
	}

	/**
	 * ��jsonarrayת����List
	 *
	 * @param array
	 * @return
	 * @throws JSONException
	 */
	@SuppressWarnings("unchecked")
	public static <T> Set<T> jsonArray2Set(JSONArray array) throws JSONException {
		if (array != null) {
			int len = array.length();
			Set<T> strs = new HashSet<T>(len);
			for (int i = 0; i < len; i++) {
				strs.add((T) array.get(i));
			}
			return strs;
		} else {
			return Collections.emptySet();
		}
	}

	/**
	 * ��jsonarrayת����List
	 *
	 * @param array
	 * @return
	 * @throws JSONException
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> jsonArray2List(JSONArray array) throws JSONException {
		if (array != null) {
			int len = array.length();
			List<T> strs = new ArrayList<T>(len);
			for (int i = 0; i < len; i++) {
				strs.add((T) array.get(i));
			}
			return strs;
		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * ��ȡINI�ļ���ָ��Section������VALUE������MAP��ʽ����
	 *
	 * @param filePath
	 * @param sectionName
	 * @param charset
	 * @return
	 */
	public static Map<String, String> getIniValuesFromFile(String filePath, String sectionName, Charset charset) {
		Map<String, Section> sectionsMap = JingWeiUtil.getIniSectionsFormFile(filePath, charset);
		Section section = sectionsMap.get(sectionName);
		if (null != section) {
			Map<String, String> valueMap = new HashMap<String, String>(section.size());
			for (Map.Entry<String, String> entry : section.entrySet()) {
				valueMap.put(entry.getKey(), entry.getValue());
			}
			return valueMap;
		} else {
			return Collections.emptyMap();
		}
	}

	/**
	 * ��ȡINI�ļ���ָ��sectionName��Section
	 *
	 * @param filePath
	 * @param sectionName
	 * @param charset
	 * @return
	 */
	public static Section getSingleSectionFormFile(String filePath, String sectionName, Charset charset) {
		Map<String, Section> sections = JingWeiUtil.getIniSectionsFormFile(filePath, charset);
		return sections.get(sectionName);
	}

	/**
	 * ��ȡINI�ļ������е�Section,����ҲMAP��ʽ����
	 *
	 * @param filePath
	 * @param charset
	 * @return
	 */
	public static Map<String, Section> getIniSectionsFormFile(String filePath, Charset charset) {
		Ini ini = new Ini();
		Config config = ini.getConfig();
		config.setFileEncoding(charset != null ? charset : GBK_CHARSET);
		File iniFile = new File(filePath);
		if (iniFile.exists() && iniFile.isFile()) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(iniFile);
				ini.load(fis);
				Map<String, Section> sectionMap = new HashMap<String, Section>(ini.size());
				for (Map.Entry<String, Section> entry : ini.entrySet()) {
					sectionMap.put(entry.getKey(), entry.getValue());
				}
				return sectionMap;
			} catch (Exception e) {
				logger.error("getIniFile Error!", e);
			} finally {
				if (null != fis) {
					try {
						fis.close();
					} catch (IOException e) {
						logger.error("getIniFile close FileInputStream Error!", e);
					}
				}
			}
		}
		return Collections.emptyMap();
	}

	/**
	 * ��ȡINI�ַ��������е�Section,����ҲMAP��ʽ����
	 *
	 * @param filePath
	 * @param charset
	 * @return
	 */
	public static Map<String, Section> getIniSectionsFormString(String data, Charset charset) {
		Ini ini = new Ini();
		Config config = ini.getConfig();
		config.setFileEncoding(charset != null ? charset : GBK_CHARSET);
		if (StringUtil.isNotBlank(data)) {
			ByteArrayInputStream byteArrayInputStream = null;
			try {
				byteArrayInputStream = new ByteArrayInputStream((data).getBytes());
				ini.load(byteArrayInputStream);
				Map<String, Section> sectionMap = new HashMap<String, Section>(ini.size());
				for (Map.Entry<String, Section> entry : ini.entrySet()) {
					sectionMap.put(entry.getKey(), entry.getValue());
				}
				return sectionMap;
			} catch (Exception e) {
				logger.error("getIniFormString Error!", e);
			} finally {
				if (null != byteArrayInputStream) {
					byteArrayInputStream.close();
				}
			}
		}
		return Collections.emptyMap();
	}

	/**
	 * ��ȡINI�ַ�����ָ��sectionName��Section
	 *
	 * @param filePath
	 * @param sectionName
	 * @param charset
	 * @return
	 */
	public static Section getSingleSectionFormString(String data, String sectionName, Charset charset) {
		Map<String, Section> sections = JingWeiUtil.getIniSectionsFormString(data, charset);
		return sections.get(sectionName);
	}

	/**
	 * ��ȡINI�ַ�����ָ��Section������VALUE������MAP��ʽ����
	 *
	 * @param filePath
	 * @param sectionName
	 * @param charset
	 * @return
	 */
	public static Map<String, String> getIniValuesFromString(String data, String sectionName, Charset charset) {
		Map<String, Section> sectionsMap = JingWeiUtil.getIniSectionsFormString(data, charset);
		Section section = sectionsMap.get(sectionName);
		if (null != section) {
			Map<String, String> valueMap = new HashMap<String, String>(section.size());
			for (Map.Entry<String, String> entry : section.entrySet()) {
				valueMap.put(entry.getKey(), entry.getValue());
			}
			return valueMap;
		} else {
			return Collections.emptyMap();
		}
	}

	/**
	 * ָ���ļ�·������ȡProperties����
	 *
	 * @param filePath
	 * @return
	 */
	public static Properties getPropFromFile(String filePath) {
		Properties properties = null;
		if (StringUtil.isNotBlank(filePath)) {
			File propFile = new File(filePath);
			if (propFile.exists() && propFile.isFile()) {
				FileInputStream fis = null;
				try {
					fis = new FileInputStream(propFile);
					properties = new Properties();
					properties.load(fis);
				} catch (Exception e) {
					logger.error("getPropFromFile Error!", e);
				} finally {
					if (null != fis) {
						try {
							fis.close();
						} catch (IOException e) {
							logger.error("getPropFromFile close FileInputStream Error!", e);
						}
					}
				}
			}
		}
		return properties;
	}

	public static Properties getPropFromString(String data) {
		Properties prop = new Properties();
		if (StringUtil.isNotBlank(data)) {
			ByteArrayInputStream byteArrayInputStream = null;
			try {
				byteArrayInputStream = new ByteArrayInputStream((data).getBytes());
				prop.load(byteArrayInputStream);
			} catch (IOException e) {
				logger.error("getPropFromString Error", e);
			} finally {
				if (null != byteArrayInputStream) {
					byteArrayInputStream.close();
				}
			}
		}
		return prop;
	}

	/**
	 * @param manager
	 * @param exitCode
	 */
	public static void destroyZkAndExit(ConfigManager manager, int exitCode) {
		logger.warn("try to destroy ZkConfigManager and Exit system!");
		if (null != manager) {
			try {
				manager.destory();
			} catch (Exception e) {
				logger.error("zk destory error!", e);
			}
		}
		logger.error(new Exception("system exit trace!"));
		Runtime.getRuntime().halt(exitCode);
	}

	public static void initJingWeiRootPath(ConfigManager manager) throws Exception {
		String[] rootPaths = new String[] { JingWeiConstants.JINGWEI_ROOT_PATH,
				JingWeiConstants.JINGWEI_TASK_ROOT_PATH, JingWeiConstants.JINGWEI_GROUP_ROOT_PATH,
				JingWeiConstants.JINGWEI_SERVER_ROOT_PATH, JingWeiConstants.JINGWEI_MONITOR_ROOT_PATH };
		for (String path : rootPaths) {
			if (!manager.exists(path)) {
				try {
					manager.publishData(path, StringUtil.EMPTY_STRING, Boolean.TRUE);
				} catch (Exception e) {
					logger.error("init  " + path + " root path error:", e);
					throw e;
				}
			}
		}
	}

	/**
	 * ��ȡ����������
	 *
	 * @return
	 */
	public static String getLocalHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			logger.error("get localhost name eccor!", e);
		}

		return StringUtil.EMPTY_STRING;
	}

	/**
	 * �Ƚ�����set �Ƿ���ȣ��������ַ���
	 *
	 * @param first  ��һ��set
	 * @param second �ڶ���set
	 * @return <code>true</code> �������ϵĴ�С�����ݶ����
	 */
	public static boolean isSetEquals(Set<String> first, Set<String> second) {

		if (first == second) {
			return true;
		}

		if ((first == null && second != null) || (first != null && second == null)) {
			return false;
		}

		if (first.size() != second.size()) {
			return false;
		}

		List<String> firstList = new ArrayList<String>(first);
		List<String> secondList = new ArrayList<String>(second);

		Collections.sort(firstList);
		Collections.sort(secondList);

		int size = firstList.size();
		for (int i = 0; i < size; i++) {
			if (!firstList.get(i).equals(secondList.get(i))) {
				return false;
			}
		}

		return true;
	}

	/**
	 * ��ȡָ�����ڵ�����
	 *
	 * @return int ����
	 */
	public static int getSecondOfDate() {
		java.util.Calendar c = java.util.Calendar.getInstance();
		c.setTime(new Date());
		int secondOfDay = c.get(java.util.Calendar.SECOND);
		secondOfDay += c.get(java.util.Calendar.MINUTE) * 60;
		secondOfDay += c.get(java.util.Calendar.HOUR_OF_DAY) * 60 * 60;

		return secondOfDay;
	}

	/**
	 * ���ӵ�Ԫ��
	 *
	 * @param currentChilds ��ǰ����
	 * @param orignSet      ԭ������
	 * @return
	 */
	public static Set<String> getAddedElement(Set<String> currentChilds, Set<String> orignSet) {
		Set<String> addElements = new HashSet<String>();
		addElements.addAll(currentChilds);
		addElements.removeAll(orignSet);

		return addElements;
	}

	/**
	 * ���ٵ�Ԫ��
	 *
	 * @param currentChilds ��ǰ����
	 * @param orignSet      ԭ������
	 * @return
	 */
	public static Set<String> getDeletedElements(Set<String> currentChilds, Set<String> orignSet) {
		Set<String> deletEelements = new HashSet<String>();
		deletEelements.addAll(orignSet);
		deletEelements.removeAll(currentChilds);

		return deletEelements;
	}

	public static String getArgString() {
		return argString;
	}

	public static void setArgString(String argString) {
		JingWeiUtil.argString = argString;
	}

	public static ConfigManager getConfigManager() {
		return configManager;
	}

	public static void setConfigManager(ConfigManager configManager) {
		JingWeiUtil.configManager = configManager;
	}

	public static String date2String(Date date) {
		return date2String(date, JingWeiConstants.DEFAULT_DATE_FORMAT);
	}

	public static Date string2Date(String dateStr) {
		return string2Date(dateStr, JingWeiConstants.DEFAULT_DATE_FORMAT);
	}

	public static SimpleDateFormat getSimpleDateFormat(String formatStr) {
		String formatKey = StringUtil.isBlank(formatStr) ? DEFAULT_DATE_FORMAT : formatStr;
		Map<String, SimpleDateFormat> formatMap = THREAD_LOCAL_FORMATS.get();
		if (null == formatMap) {
			formatMap = new HashMap<String, SimpleDateFormat>();
			THREAD_LOCAL_FORMATS.set(formatMap);
		}
		SimpleDateFormat simpleDateFormat = formatMap.get(formatKey);
		if (null == simpleDateFormat) {
			simpleDateFormat = new SimpleDateFormat(formatKey);
		}
		return simpleDateFormat;
	}

	public static String date2String(Date date, String formatStr) {
		SimpleDateFormat simpleDateFormat = getSimpleDateFormat(formatStr);
		return simpleDateFormat.format(date).toString();
	}

	public static Date string2Date(String dateStr, String formatStr) {
		SimpleDateFormat simpleDateFormat = getSimpleDateFormat(formatStr);
		Date date = null;
		try {
			date = simpleDateFormat.parse(dateStr);
		} catch (ParseException e) {
			logger.error("string2Date Error!", e);
		}
		return date;
	}

	/**
	 * ��DRC���ַ���λ��ת���ַ�������
	 * 002580:0269723123#10.232.31.67-3306^1354007096$123
	 * 1.file
	 * 2.offset
	 * 3.ip-port ����:10_232_10_12-3306
	 * 4.timestemp
	 * 5.metadataVersion
	 */
	public static String[] parseDrcStrPosition(String str) {
		//002580:0269723123#10.232.31.67-3306^1354007096$123
		String[] res = new String[5];
		res[4] = "0";

		if (StringUtil.isBlank(str)) {
			return res;
		}

		String fileAndOffset = StringUtil.substringBefore(str, JingWeiConstants.POUND_STR);
		if (StringUtil.isNotBlank(fileAndOffset)) {
			String file = StringUtil.substringBefore(fileAndOffset, JingWeiConstants.COLON_STR);
			if (StringUtil.isNotBlank(file) && StringUtil.isNumeric(file.trim())) {
				res[0] = file.trim();
			}
			String offset = StringUtil.substringAfter(fileAndOffset, JingWeiConstants.COLON_STR);
			if (StringUtil.isNotBlank(offset) && StringUtil.isNumeric(offset.trim())) {
				res[1] = offset.trim();
			}
		}
		String serverIdAndTime = StringUtil.substringAfter(str, JingWeiConstants.POUND_STR);
		if (StringUtil.isNotBlank(serverIdAndTime)) {
			serverIdAndTime = StringUtil.substringBeforeLast(serverIdAndTime, JingWeiConstants.DOLLAR_STR);
			res[2] = StringUtil.substringBefore(serverIdAndTime, JingWeiConstants.TIP_STR).trim();
			String timestamp = StringUtil.substringAfter(serverIdAndTime, JingWeiConstants.TIP_STR);
			if (StringUtil.isNotBlank(timestamp) && StringUtil.isNumeric(timestamp.trim())) {
				res[3] = timestamp.trim();
			}
		}
		String metaDataVersion = StringUtil.substringAfterLast(str, JingWeiConstants.DOLLAR_STR);
		if (StringUtil.isNotBlank(metaDataVersion) && StringUtil.isNumeric(metaDataVersion.trim())) {
			res[4] = metaDataVersion.trim();
		}
		return res;
	}

	/**
	 * ��ȡDRC���ַ�����ʽλ�� ���磺002580:0269723123#10.232.31.67-3306^1354007096$123
	 *
	 * @param file
	 * @param offset
	 * @param serverId
	 * @param timestamp
	 * @param metaDataVersion
	 * @return
	 */

	public static String getDrcStrPosition(String file, String offset, String serverId, String timestamp,
			String metaDataVersion) {
		StringBuilder sb = new StringBuilder();
		sb.append(StringUtil.defaultIfBlank(file)).append(JingWeiConstants.COLON_STR);
		sb.append(StringUtil.defaultIfBlank(offset)).append(JingWeiConstants.POUND_STR);
		sb.append(StringUtil.defaultIfBlank(serverId)).append(JingWeiConstants.TIP_STR);
		sb.append(StringUtil.defaultIfBlank(timestamp)).append(JingWeiConstants.DOLLAR_STR);
		sb.append(StringUtil.defaultIfBlank(metaDataVersion));
		return sb.toString();
	}

	/**
	 * �����ַ�����λ�㷵���ַ�������
	 * 1.file,2.offset,3.server_id,4.timestamp 5.metaDataVersion
	 * <p/>
	 * ���磺002580:0269723123#1521223144.1354007096$123
	 * 1.file:002580
	 * 2.offset:0269723123
	 * 3.server_id:1521223144
	 * 4.timestamp:1354007096
	 * 5.metaDataVersion:1
	 *
	 * @return
	 */
	public static String[] parseStrPosition(String str) {
		String[] res = new String[4];
		//file
		res[0] = StringUtil.EMPTY_STRING;
		//offset
		res[1] = "0";
		//server_id
		res[2] = "-1";
		//timestamp
		res[3] = "-1";
		if (StringUtil.isBlank(str)) {
			return res;
		}
		String fileAndOffset = StringUtil.substringBefore(str, JingWeiConstants.POUND_STR);
		if (StringUtil.isNotBlank(fileAndOffset)) {
			String file = StringUtil.substringBefore(fileAndOffset, JingWeiConstants.COLON_STR);
			if (StringUtil.isNotBlank(file)) {
				res[0] = file.trim();
			}
			String offset = StringUtil.substringAfter(fileAndOffset, JingWeiConstants.COLON_STR);
			if (StringUtil.isNotBlank(offset) && StringUtil.isNumeric(offset.trim())) {
				res[1] = offset.trim();
			}
		}
		String serverIdAndTime = StringUtil.substringAfter(str, JingWeiConstants.POUND_STR);
		if (StringUtil.isNotBlank(serverIdAndTime)) {
			String serverId = StringUtil.substringBefore(serverIdAndTime, JingWeiConstants.POINT_STR);
			if (StringUtil.isNotBlank(serverId) && StringUtil.isNumeric(serverId.trim())) {
				res[2] = serverId.trim();
			}
			String timestamp = StringUtil.substringAfter(serverIdAndTime, JingWeiConstants.POINT_STR);
			if (StringUtil.isNotBlank(timestamp) && StringUtil.isNumeric(timestamp.trim())) {
				res[3] = timestamp.trim();
			}
		}
		return res;
	}

	/**
	 * ��ȡ�ַ�����ʽλ��
	 * ����:002580:0269723123#1521223144.1354007096
	 *
	 * @param file
	 * @param offset
	 * @param serverId
	 * @param timestamp
	 * @return
	 */
	public static String getStrPosition(String file, String offset, String serverId, String timestamp) {
		StringBuilder sb = new StringBuilder();
		sb.append(StringUtil.defaultIfBlank(file, StringUtil.EMPTY_STRING)).append(JingWeiConstants.COLON_STR);
		sb.append(StringUtil.defaultIfBlank(offset, StringUtil.EMPTY_STRING)).append(JingWeiConstants.POUND_STR);
		sb.append(StringUtil.defaultIfBlank(serverId, "-1")).append(JingWeiConstants.POINT_STR);
		sb.append(StringUtil.defaultIfBlank(timestamp, "-1"));
		return sb.toString();
	}

	public static String getJingweiTaskName() {
		String bootArgString = JingWeiUtil.getArgString();
		Map<String, String> bootArgs = JingWeiUtil.handleArgs(bootArgString);
		String taskName = bootArgs.get("taskName");
		return taskName;
	}

	public static Long getPositionMillis(String positionStr, ExtractorType extractorType) {
		long millis = -1L;
		String millisStr = StringUtil.EMPTY_STRING;
		if (StringUtil.isNotBlank(positionStr)) {
			if (ExtractorType.BINLOG_EXTRACTOR == extractorType || ExtractorType.ORACLE_EXTRACTOR == extractorType) {
				//BINLOG:   001253:0187913468#2.1374683793
				//ORACLE:    384:51954472#-1.1374676777000
				millisStr = StringUtil.substringAfterLast(positionStr, POINT_STR);
			} else if (ExtractorType.DRC_EXTRACTOR == extractorType) {
				//DRC:      006002:306479476#10.232.31.144-3306^1374560901$2
				millisStr = StringUtil.substringAfterLast(positionStr, TIP_STR);
				millisStr = StringUtil.substringBeforeLast(millisStr, DOLLAR_STR);
			}
		}
		if (StringUtil.isNotBlank(millisStr) && StringUtil.isNumeric(millisStr)) {
			millis = ExtractorType.ORACLE_EXTRACTOR == extractorType ? Long.valueOf(millisStr) : Long
					.valueOf(millisStr) * 1000;
		}
		return millis;
	}

	/*
	 *���������׼ȷ����������õ�EXTRACTOR�����ͣ������JingWeiUtil�е�
	 *public static Long getPositionMillis(String positionStr, ExtractorType extractorType)
	 *����
	*/
	public static Long getPositionMillis(String positionStr) {
		long second = -1L;
		boolean isOracle = false;
		if (StringUtil.isNotBlank(positionStr)) {
			String subTemp = StringUtil.substringAfterLast(positionStr, POUND_STR);
			if (StringUtil.isNotBlank(subTemp)) {
				String subTmp2 = StringUtil.substringAfterLast(subTemp, TIP_STR);
				if (StringUtil.isNotBlank(subTmp2)) {
					//DRC:      006002:306479476#10.232.31.144-3306^1374560901$2
					subTemp = StringUtil.substringBeforeLast(subTmp2, DOLLAR_STR);
				} else {
					//DBSYNC:   001253:0187913468#2.1374683793
					//EROSA:    384:51954472#-1.1374676777000
					//-1.1374676777000
					isOracle = "-1".equals(StringUtil.substringBefore(subTemp, POINT_STR));
					subTemp = StringUtil.substringAfterLast(subTemp, POINT_STR);
				}
			}
			//is number
			if (StringUtil.isNotBlank(subTemp) && StringUtil.isNumeric(subTemp)) {
				second = isOracle ? Long.valueOf(subTemp) : Long.valueOf(subTemp) * 1000;
			}
		}
		return second;
	}

	public static void main(String[] args) {
		System.out.println(getPositionMillis("001253:0187913468#2.1374683793", ExtractorType.BINLOG_EXTRACTOR));
		System.out.println(getPositionMillis("384:51954472#-1.1374676777000", ExtractorType.ORACLE_EXTRACTOR));
		System.out.println(getPositionMillis("006002:306479476#10.232.31.144-3306^1374560901$2",
				ExtractorType.DRC_EXTRACTOR));
		System.out.println(date2String(new Date(1375329109453L)));
		System.out.println(string2Date("2013-08-01 11:51:49.453"));
	}
}
