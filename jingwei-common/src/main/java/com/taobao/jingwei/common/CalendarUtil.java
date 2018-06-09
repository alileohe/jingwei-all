package com.taobao.jingwei.common;

import com.alibaba.common.lang.StringUtil;
import org.apache.log4j.Logger;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class CalendarUtil {
	private static final Logger logger = Logger.getLogger(CalendarUtil.class);

	/** new a Calendar instance */
	static GregorianCalendar cldr = new GregorianCalendar();

	/** the milli second of a day */
	public static final long DAYMILLI = 24 * 60 * 60 * 1000;

	/** the milli seconds of an hour */
	public static final long HOURMILLI = 60 * 60 * 1000;

	/** the milli seconds of a minute */
	public static final long MINUTEMILLI = 60 * 1000;

	/** the milli seconds of a second */
	public static final long SECONDMILLI = 1000;

	/** added time */
	public static final String TIMETO = " 23:59:59";

	/**
	 * set the default time zone
	 */
	static {
		cldr.setTimeZone(java.util.TimeZone.getTimeZone("GMT+9:00"));
	}

	/** flag before */
	public static final transient int BEFORE = 1;

	/** flag after */
	public static final transient int AFTER = 2;

	/** flag equal */
	public static final transient int EQUAL = 3;

	/** date format dd/MMM/yyyy:HH:mm:ss +0900 */
	public static final String TIME_PATTERN_LONG = "dd/MMM/yyyy:HH:mm:ss +0900";

	/** date format dd/MM/yyyy:HH:mm:ss +0900 */
	public static final String TIME_PATTERN_LONG2 = "dd/MM/yyyy:HH:mm:ss +0900";

	/** date format yyyy-MM-dd HH:mm:ss */
	public static final String TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

	/** date format YYYY-MM-DD HH24:MI:SS */
	public static final String DB_TIME_PATTERN = "YYYY-MM-DD HH24:MI:SS";

	/** date format dd/MM/yy HH:mm:ss */
	public static final String TIME_PATTERN_SHORT = "dd/MM/yy HH:mm:ss";

	/** date format dd/MM/yy HH24:mm */
	public static final String TIME_PATTERN_SHORT_1 = "yyyy/MM/dd HH:mm";

	/** date format yyyyMMddHHmmss */
	public static final String TIME_PATTERN_SESSION = "yyyyMMddHHmmss";

	/** date format yyyyMMdd */
	public static final String DATE_FMT_0 = "yyyyMMdd";

	/** date format yyyy/MM/dd */
	public static final String DATE_FMT_1 = "yyyy/MM/dd";

	/** date format yyyy/MM/dd hh:mm:ss */
	public static final String DATE_FMT_2 = "yyyy/MM/dd hh:mm:ss";

	/** date format yyyy-MM-dd */
	public static final String DATE_FMT_3 = "yyyy-MM-dd";

	/**
	 * change string to date
	 * ��String���͵�����ת��Date����
	 * @param sDate the date string
	 * @param sFmt the date format
	 *
	 * @return Date object
	 */
	public static java.util.Date toDate(String sDate, String sFmt) {
		if (StringUtil.isBlank(sDate) || StringUtil.isBlank(sFmt)) {
			return null;
		}

		SimpleDateFormat sdfFrom = null;
		java.util.Date dt = null;
		try {
			sdfFrom = new SimpleDateFormat(sFmt);
			dt = sdfFrom.parse(sDate);
		} catch (Exception ex) {
			logger.error("toDate", ex);
			return null;
		} finally {
			sdfFrom = null;
		}

		return dt;
	}

	/**
	 * change date to string
	 * ���������͵Ĳ���ת��String����
	 * @param dt a date
	 *
	 * @return the format string
	 */
	public static String toString(java.util.Date dt) {
		return toString(dt, DATE_FMT_0);
	}

	/**
	 * change date object to string
	 * ��String���͵�����ת��Date����
	 * @param dt date object
	 * @param sFmt the date format
	 *
	 * @return the formatted string
	 */
	public static String toString(java.util.Date dt, String sFmt) {
		if (null == dt || StringUtil.isBlank(sFmt)) {
			return null;
		}

		SimpleDateFormat sdfFrom = null;
		String sRet = null;
		try {
			sdfFrom = new SimpleDateFormat(sFmt);
			sRet = sdfFrom.format(dt).toString();
		} catch (Exception ex) {
			logger.error("toString", ex);
			return null;
		} finally {
			sdfFrom = null;
		}

		return sRet;
	}

	/**
	 * ��ȡDate�����µ����һ������
	 * @param date
	 * @return Date Ĭ��null
	 */
	public static Date getMonthLastDate(Date date) {
		if (null == date) {
			return null;
		}

		Calendar ca = Calendar.getInstance();
		ca.setTime(date);
		ca.set(Calendar.HOUR_OF_DAY, 23);
		ca.set(Calendar.MINUTE, 59);
		ca.set(Calendar.SECOND, 59);
		ca.set(Calendar.DAY_OF_MONTH, 1);
		ca.add(Calendar.MONTH, 1);
		ca.add(Calendar.DAY_OF_MONTH, -1);

		Date lastDate = ca.getTime();
		return lastDate;
	}

	/**
	 * ��ȡDate�����µ����һ������
	 * @param date
	 * @param pattern
	 * @return String Ĭ��null
	 */
	public static String getMonthLastDate(Date date, String pattern) {
		Date lastDate = getMonthLastDate(date);
		if (null == lastDate) {
			return null;
		}

		if (StringUtil.isBlank(pattern)) {
			pattern = TIME_PATTERN;
		}

		return toString(lastDate, pattern);
	}

	/**
	 * ��ȡDate�����µĵ�һ������
	 * @param date
	 * @return Date Ĭ��null
	 */
	public static Date getMonthFirstDate(Date date) {
		if (null == date) {
			return null;
		}

		Calendar ca = Calendar.getInstance();
		ca.setTime(date);
		ca.set(Calendar.HOUR_OF_DAY, 0);
		ca.set(Calendar.MINUTE, 0);
		ca.set(Calendar.SECOND, 0);
		ca.set(Calendar.DAY_OF_MONTH, 1);

		Date firstDate = ca.getTime();
		return firstDate;
	}

	/**
	 * ��ȡDate�����µĵ�һ������
	 * @param date
	 * @param pattern
	 * @return String Ĭ��null
	 */
	public static String getMonthFirstDate(Date date, String pattern) {
		Date firstDate = getMonthFirstDate(date);
		if (null == firstDate) {
			return null;
		}

		if (StringUtil.isBlank(pattern)) {
			pattern = TIME_PATTERN;
		}

		return toString(firstDate, pattern);
	}

	/**
	 * �����������ڼ��������
	 * 
	 * @param firstDate С��
	 * @param lastDate ����
	 * @return int Ĭ��-1
	 */
	public static int getIntervalDays(java.util.Date firstDate, java.util.Date lastDate) {
		if (null == firstDate || null == lastDate) {
			return -1;
		}

		long intervalMilli = lastDate.getTime() - firstDate.getTime();
		return (int) (intervalMilli / (24 * 60 * 60 * 1000));
	}

	/**
	 * �����������ڼ����Сʱ��
	 * 
	 * @param firstDate С��
	 * @param lastDate ����
	 * @return int Ĭ��-1
	 */
	public static int getTimeIntervalHours(Date firstDate, Date lastDate) {
		if (null == firstDate || null == lastDate) {
			return -1;
		}

		long intervalMilli = lastDate.getTime() - firstDate.getTime();
		return (int) (intervalMilli / (60 * 60 * 1000));
	}

	/**
	 * �����������ڼ���ķ�����
	 * 
	 * @param firstDate С��
	 * @param lastDate ����
	 * @return int Ĭ��-1
	 */
	public static int getTimeIntervalMins(Date firstDate, Date lastDate) {
		if (null == firstDate || null == lastDate) {
			return -1;
		}

		long intervalMilli = lastDate.getTime() - firstDate.getTime();
		return (int) (intervalMilli / (60 * 1000));
	}

	/**
	 * format the date in given pattern ��ʽ������
	 * 
	 * @param d date
	 * @param pattern time pattern
	 * @return the formatted string
	 */
	public static String formatDate(java.util.Date d, String pattern) {
		if (null == d || StringUtil.isBlank(pattern)) {
			return null;
		}

		SimpleDateFormat formatter = (SimpleDateFormat) DateFormat.getDateInstance();

		formatter.applyPattern(pattern);
		return formatter.format(d);
	}

	/**
	 * �Ƚ��������ڵ��Ⱥ�˳��
	 * @param src
	 * @param desc
	 * @return
	 */
	public static int compareDate(java.util.Date src, java.util.Date desc) {
		if ((src == null) && (desc == null)) {
			return EQUAL;
		} else if (desc == null) {
			return BEFORE;
		} else if (src == null) {
			return AFTER;
		} else {
			long timeSrc = src.getTime();
			long timeDesc = desc.getTime();

			if (timeSrc == timeDesc) {
				return EQUAL;
			} else {
				return (timeDesc > timeSrc) ? AFTER : BEFORE;
			}
		}
	}

	/**
	 * �Ƚ��������ڵ��Ⱥ�˳��
	 *
	 * @param first date1
	 * @param second date2
	 *
	 * @return EQUAL  - if equal BEFORE - if before than date2 AFTER  - if over than date2
	 */
	public static int compareTwoDate(Date first, Date second) {
		if ((first == null) && (second == null)) {
			return EQUAL;
		} else if (first == null) {
			return BEFORE;
		} else if (second == null) {
			return AFTER;
		} else if (first.before(second)) {
			return BEFORE;
		} else if (first.after(second)) {
			return AFTER;
		} else {
			return EQUAL;
		}
	}

	/**
	 * �Ƚ������Ƿ��������֮��
	 * @param date the specified date
	 * @param begin date1
	 * @param end date2
	 *
	 * @return true  - between date1 and date2 false - not between date1 and date2
	 */
	public static boolean isDateBetween(Date date, Date begin, Date end) {
		int c1 = compareTwoDate(begin, date);
		int c2 = compareTwoDate(date, end);

		return (((c1 == BEFORE) && (c2 == BEFORE)) || (c1 == EQUAL) || (c2 == EQUAL));
	}

	/**
	 * �Ƚ������Ƿ���ڵ�ǰ���ڵ�ǰ��������
	 * @param myDate
	 * @param begin
	 * @param end
	 * @return
	 */
	public static boolean isDateBetween(java.util.Date myDate, int begin, int end) {
		return isDateBetween(myDate, getCurrentDateTime(), begin, end);
	}

	/**
	 * �Ƚ������Ƿ����ָ�����ڵ�ǰ��������
	 * @param utilDate
	 * @param dateBaseLine
	 * @param begin
	 * @param end
	 * @return
	 */
	public static boolean isDateBetween(java.util.Date utilDate, java.util.Date dateBaseLine, int begin, int end) {
		String pattern = TIME_PATTERN;

		String my = toString(utilDate, pattern);
		Date myDate = parseString2Date(my, pattern);

		String baseLine = toString(dateBaseLine, pattern);

		//		Date baseLineDate = parseString2Date(baseLine, pattern);
		String from = addDays(baseLine, begin);
		Date fromDate = parseString2Date(from, pattern);

		String to = addDays(baseLine, end);
		Date toDate = parseString2Date(to, pattern);

		return isDateBetween(myDate, fromDate, toDate);
	}

	/**
	 * change string to Timestamp
	 * �ַ�������ת��Timestamp
	 * ��������String�����ָ�ʽ�������Ȼ�ת����Long����Date
	 * @deprecated plz use <code>Calendar.toDate(String sDate, String sFmt)</code>
	 * 
	 * @param str formatted timestamp string
	 * @param sFmt string format
	 *
	 * @return timestamp
	 */
	public static Timestamp parseString2Timestamp(String str, String sFmt) {
		if ((str == null) || str.equals("")) {
			return null;
		}

		try {
			long time = Long.parseLong(str);

			return new Timestamp(time);
		} catch (Exception ex) {
			try {
				DateFormat df = new SimpleDateFormat(sFmt);
				java.util.Date dt = df.parse(str);

				return new Timestamp(dt.getTime());
			} catch (Exception pe) {
				try {
					return Timestamp.valueOf(str);
				} catch (Exception e) {
					return null;
				}
			}
		}
	}

	/**
	 * parse a string into date  in a patter
	 * ��������String�����ָ�ʽ�������Ȼ�ת����Long����Date
	 * @deprecated plz use <code>Calendar.toDate(String sDate, String sFmt)</code>
	 *
	 * @param str string
	 * @param sFmt date pattern
	 *
	 * @return date
	 */
	public static Date parseString2Date(String str, String sFmt) {
		if ((str == null) || str.equals("")) {
			return null;
		}

		try {
			long time = Long.parseLong(str);

			return new Date(time);
		} catch (Exception ex) {
			try {
				DateFormat df = new SimpleDateFormat(sFmt);
				java.util.Date dt = df.parse(str);

				return new Date(dt.getTime());
			} catch (Exception pe) {
				logger.warn("parseString2Date", pe);
				try {
					return new Date(str);
				} catch (Exception e) {
					return null;
				}
			}
		}
	}

	/**
	 * ��������
	 * @param date
	 * @param day
	 * @return Date
	 */
	public static java.util.Date addDate(java.util.Date date, int day) {
		if (null == date) {
			return null;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + day);
		return calendar.getTime();
	}

	/**
	 * ��������
	 * @param date
	 * @param day
	 * @param pattern
	 * @return
	 */
	public static String addDays(java.util.Date date, int day, String pattern) {
		return addDays(toString(date, pattern), day, pattern);
	}

	/**
	 * ��������
	 * @param date
	 * @param day
	 * @return
	 */
	public static String addDays(java.util.Date date, int day) {
		return addDays(toString(date, TIME_PATTERN), day);
	}

	/**
	 * ��������
	 * @param date
	 * @param day
	 * @return
	 */
	public static String addDays(String date, int day) {
		return addDays(date, day, TIME_PATTERN);
	}

	/**
	 * get the time of the specified date after given days
	 *
	 * @param date the specified date
	 * @param day day distance
	 *
	 * @return the format string of time
	 */
	public static String addDays(String date, int day, String pattern) {
		if (date == null) {
			return "";
		}

		if (date.equals("")) {
			return "";
		}

		if (day == 0) {
			return date;
		}

		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
			Calendar calendar = dateFormat.getCalendar();

			calendar.setTime(dateFormat.parse(date));
			calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + day);
			return dateFormat.format(calendar.getTime());
		} catch (Exception ex) {
			logger.error("addDays", ex);
			return "";
		}
	}

	/**
	 * change timestamp to formatted string
	 *
	 * @param t Timestamp
	 * @param sFmt date format
	 *
	 * @return formatted string
	 */
	public static String formatTimestamp(Timestamp t, String sFmt) {
		if (t == null || StringUtil.isBlank(sFmt)) {
			return "";
		}

		t.setNanos(0);

		DateFormat ft = new SimpleDateFormat(sFmt);
		String str = "";

		try {
			str = ft.format(t);
		} catch (NullPointerException ex) {
			logger.error("formatTimestamp", ex);
		}

		return str;
	}

	/**
	 * change string to Timestamp
	 * ��������String�����ָ�ʽ�������Ȼ�ת����Long����Date
	 * @deprecated plz use <code>Calendar.toDate(String sDate, String sFmt)</code>
	 *
	 * @param str formatted timestamp string
	 * @param sFmt string format
	 *
	 * @return timestamp
	 */
	public static Timestamp parseString(String str, String sFmt) {
		if ((str == null) || str.equals("")) {
			return null;
		}

		try {
			long time = Long.parseLong(str);

			return new Timestamp(time);
		} catch (Exception ex) {
			try {
				DateFormat df = new SimpleDateFormat(sFmt);
				java.util.Date dt = df.parse(str);

				return new Timestamp(dt.getTime());
			} catch (Exception pe) {
				try {
					return Timestamp.valueOf(str);
				} catch (Exception e) {
					return null;
				}
			}
		}
	}

	/**
	 * return current date
	 *
	 * @return current date
	 */
	public static Date getCurrentDate() {
		return new Date(System.currentTimeMillis());
	}

	/**
	 * return current calendar instance
	 *
	 * @return Calendar
	 */
	public static Calendar getCurrentCalendar() {
		return Calendar.getInstance();
	}

	/**
	 * return current time
	 *
	 * @return current time
	 */
	public static Timestamp getCurrentDateTime() {
		return new Timestamp(System.currentTimeMillis());
	}

	/**
	 * ��ȡ���
	 * @param date Date
	 * @return int
	 */
	public static final int getYear(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.YEAR);
	}

	/**
	 * ��ȡ���
	 * @param millis long
	 * @return int
	 */
	public static final int getYear(long millis) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(millis);
		return calendar.get(Calendar.YEAR);
	}

	/**
	 * ��ȡ�·�
	 * @param date Date
	 * @return int
	 */
	public static final int getMonth(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.MONTH) + 1;
	}

	/**
	 * ��ȡ�·�
	 * @param millis long
	 * @return int
	 */
	public static final int getMonth(long millis) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(millis);
		return calendar.get(Calendar.MONTH) + 1;
	}

	/**
	 * ��ȡ����
	 * @param date Date
	 * @return int
	 */
	public static final int getDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.DATE);
	}

	/**
	 * ��ȡ����
	 * @param millis long
	 * @return int
	 */
	public static final int getDate(long millis) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(millis);
		return calendar.get(Calendar.DATE);
	}

	/**
	 * ��ȡСʱ
	 * @param date Date
	 * @return int
	 */
	public static final int getHour(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.HOUR_OF_DAY);
	}

	/**
	 * ��ȡСʱ
	 * @param millis long
	 * @return int
	 */
	public static final int getHour(long millis) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(millis);
		return calendar.get(Calendar.HOUR_OF_DAY);
	}

	/**
	 * �����ں��ʱ���0 ���(yyyy-MM-dd 00:00:00:000)
	 * @param date Date
	 * @return Date
	 */
	public static final Date zerolizedTime(Date fullDate) {
		Calendar cal = Calendar.getInstance();

		cal.setTime(fullDate);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
}
