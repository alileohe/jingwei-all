package com.taobao.jingwei.common.log;

import org.apache.log4j.Level;

/**
 * This class define log configrations.
 * 
 * @author <a href="mailto:shuohai.lhl@taobao.com">Ë·º£  shuohailhl</a>
 * @date 2011-12-6ÏÂÎç2:29:55
 */
public class JingweiLogConfig {

	public static final String DEFAULT_LOG_PATH = "./logs";
	public static final String DEFAULT_FILE_LOG_ENCODING = "UTF-8";
	public static final String DEFAULT_LOG_LAYOUT = "%m%n";
	public static final Level DEFAULT_LOG_LEVEL = Level.WARN;
	public static final String DEFAULT_MAX_LOG_FILE_SIZE = "10MB";
	public static final int DEFAULT_MAX_BACKUP_INDEX = 1;

	/** log file path. */
	private String logPath = DEFAULT_LOG_PATH;

	/** log file encoding. */
	private String fileLogEncoding = DEFAULT_FILE_LOG_ENCODING;

	/** log file pattern layout. */
	private String patternLogLayout = DEFAULT_LOG_LAYOUT;

	/** log file log level. */
	private Level logLevel = DEFAULT_LOG_LEVEL;

	/** log file log file size. */
	private String maxLogFileSize = DEFAULT_MAX_LOG_FILE_SIZE;

	private Integer maxBackupIndex = DEFAULT_MAX_BACKUP_INDEX;

	public Integer getMaxBackupIndex() {
		return maxBackupIndex;
	}

	public void setMaxBackupIndex(Integer maxBackupIndex) {
		this.maxBackupIndex = maxBackupIndex;
	}

	public String getFileLogEncoding() {
		return fileLogEncoding;
	}

	public void setFileLogEncoding(String fileLogEncoding) {
		this.fileLogEncoding = fileLogEncoding;
	}

	public Level getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(Level logLevel) {
		this.logLevel = logLevel;
	}

	public String getMaxLogFileSize() {
		return maxLogFileSize;
	}

	public void setMaxLogFileSize(String maxLogFileSize) {
		this.maxLogFileSize = maxLogFileSize;
	}

	public String getLogPath() {
		return logPath;
	}

	public void setLogPath(String logPath) {
		this.logPath = logPath;
	}

	public String getPatternLogLayout() {
		return patternLogLayout;
	}

	public void setPatternLogLayout(String patternLogLayout) {
		this.patternLogLayout = patternLogLayout;
	}

}
