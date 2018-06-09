package com.taobao.jingwei.common.log;

import com.taobao.jingwei.common.JingWeiConstants;
import org.apache.commons.logging.Log;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import java.io.File;
import java.io.IOException;

/**
 * This class define a <code>Log</code> impliment.
 * 
 * @author <a href="mailto:shuohai.lhl@taobao.com">shuohailhl</a>
 * @date 2011-12-6����2:29:55
 */
class JingweiLogImpl implements Log {
	private final static Log log = org.apache.commons.logging.LogFactory.getLog(LogFactory.class);

	private Logger logger;

	private final JingweiLogConfig jingweiLogConfig;

	public JingweiLogImpl(LogType type, JingweiLogConfig jingweiLogConfig, String name) {
		this.jingweiLogConfig = jingweiLogConfig;
		this.logger = JingweiLogImpl.createLogger(type, jingweiLogConfig, name);
	}

	/**
	 * constract a <code>Logger</code> for specified type and configration.
	 * 
	 * @param type          log type.
	 * @param logConfig log configration.
	 * @return <code>Logger</code>
	 */
	private static Logger createLogger(LogType type, JingweiLogConfig logConfig, String name) {

		String patternLayout = JingweiLogConfig.DEFAULT_LOG_LAYOUT;

		String logFilePath = JingweiLogConfig.DEFAULT_LOG_PATH;

		String maxFileSize = JingweiLogConfig.DEFAULT_MAX_LOG_FILE_SIZE;

		String encoding = JingweiLogConfig.DEFAULT_FILE_LOG_ENCODING;

		Level logLevel = JingweiLogConfig.DEFAULT_LOG_LEVEL;

		int maxBackupIndex = JingweiLogConfig.DEFAULT_MAX_BACKUP_INDEX;

		if (logConfig != null) {
			patternLayout = logConfig.getPatternLogLayout() == null ? JingweiLogConfig.DEFAULT_LOG_LAYOUT // NL
					: logConfig.getPatternLogLayout();

			logFilePath = logConfig.getLogPath() == null ? JingweiLogConfig.DEFAULT_LOG_PATH // NL
					: logConfig.getLogPath();

			maxFileSize = logConfig.getMaxLogFileSize() == null ? JingweiLogConfig.DEFAULT_MAX_LOG_FILE_SIZE // NL
					: logConfig.getMaxLogFileSize();

			encoding = logConfig.getFileLogEncoding() == null ? JingweiLogConfig.DEFAULT_FILE_LOG_ENCODING // NL
					: logConfig.getFileLogEncoding();

			logLevel = logConfig.getLogLevel() == null ? JingweiLogConfig.DEFAULT_LOG_LEVEL // NL
					: logConfig.getLogLevel();

			maxBackupIndex = logConfig.getMaxBackupIndex() == null ? JingweiLogConfig.DEFAULT_MAX_BACKUP_INDEX // NL
					: logConfig.getMaxBackupIndex();
		}

		PatternLayout layout = new PatternLayout(patternLayout);
		StringBuilder sb = new StringBuilder(logFilePath);
		sb.append(JingWeiConstants.FILE_SEP);

		sb.append(type.getTypeName()).append("-").append(name).append(".log");

		File file = new File(sb.toString());
		log.info("[jingwei common] create log file : " + file.toString());

		Logger logger = Logger.getLogger(type.toString().toLowerCase());
		RollingFileAppender fileAppender;

		try {
			fileAppender = new RollingFileAppender(layout, sb.toString());

			fileAppender.setMaxBackupIndex(maxBackupIndex);
			fileAppender.setMaxFileSize(maxFileSize);
			fileAppender.setAppend(true);
			fileAppender.setEncoding(encoding);
			logger.removeAllAppenders();
			logger.setAdditivity(false);
			logger.addAppender(fileAppender);
			logger.setLevel(logLevel);

		} catch (IOException e) {
			log.error("[jingwei common] create log file error!");
		}
		return logger;
	}

	@Override
	public boolean isDebugEnabled() {
		return logger.isEnabledFor(Level.DEBUG);
	}

	@Override
	public boolean isErrorEnabled() {
		return logger.isEnabledFor(Level.ERROR);
	}

	@Override
	public boolean isFatalEnabled() {
		return logger.isEnabledFor(Level.FATAL);
	}

	@Override
	public boolean isInfoEnabled() {
		return logger.isEnabledFor(Level.INFO);
	}

	@Override
	public boolean isTraceEnabled() {
		return logger.isEnabledFor(Level.TRACE);
	}

	@Override
	public boolean isWarnEnabled() {
		return logger.isEnabledFor(Level.WARN);
	}

	@Override
	public void trace(Object message) {
		logger.trace(message);
	}

	@Override
	public void trace(Object message, Throwable t) {
		logger.trace(message, t);
	}

	@Override
	public void debug(Object message) {
		logger.debug(message);
	}

	@Override
	public void debug(Object message, Throwable t) {
		logger.debug(message, t);
	}

	@Override
	public void info(Object message) {
		logger.info(message);
	}

	@Override
	public void info(Object message, Throwable t) {
		logger.info(message, t);
	}

	@Override
	public void warn(Object message) {
		logger.warn(message);
	}

	@Override
	public void warn(Object message, Throwable t) {
		logger.warn(message, t);
	}

	@Override
	public void error(Object message) {
		logger.error(message);
	}

	@Override
	public void error(Object message, Throwable t) {
		logger.error(message, t);
	}

	@Override
	public void fatal(Object message) {
		logger.fatal(message);
	}

	@Override
	public void fatal(Object message, Throwable t) {
		logger.fatal(message, t);
	}

	public JingweiLogConfig getJingweiLogConfig() {

		return jingweiLogConfig;
	}
}
