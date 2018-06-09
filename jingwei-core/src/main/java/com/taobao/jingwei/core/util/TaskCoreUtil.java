package com.taobao.jingwei.core.util;

import com.taobao.tddl.dbsync.dbms.DBMSEvent;
import com.taobao.tddl.dbsync.dbms.DBMSHelper;
import org.apache.commons.logging.Log;

/**
 * @desc ������
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Jul 20, 2012 2:37:56 PM
 */

public final class TaskCoreUtil {
	private TaskCoreUtil() {
	}

	public static void errorApplierExceptionEvent(DBMSEvent event, Log log) {
		StringBuilder builder = new StringBuilder("applying error: \n");
		DBMSHelper.printDBMSEvent(builder, event);
		log.error(builder.toString());
	}
}
