package com.taobao.jingwei.core.loader;

import com.taobao.jingwei.core.kernel.JingWeiCore;
import com.taobao.jingwei.core.util.RowChangeHelper;
import com.taobao.tddl.dbsync.applier.Applier;
import com.taobao.tddl.dbsync.applier.ApplierException;
import com.taobao.tddl.dbsync.dbms.DBMSEvent;
import com.taobao.tddl.dbsync.dbms.DBMSHelper;
import com.taobao.tddl.dbsync.dbms.DBMSRowChange;
import com.taobao.tddl.dbsync.dbms.DefaultRowChange;

/**
 * description:
 * <p/>
 * <p/>
 * JwSyncLoader.java Create on Jan 15, 2013 8:40:58 PM
 * <p/>
 * Copyright (c) 2011 by qihao
 *
 * @author <a href="mailto:qihao@taobao.com">qihao</a>
 * @version 1.0
 */
public class JwSyncLoader {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String taskName = "DRC_TEST";
		final JingWeiCore core = new JingWeiCore();
		core.setTaskName(taskName);
		core.setApplier(new Applier() {
			@Override
			public void apply(DBMSEvent event) throws ApplierException, InterruptedException {
				if (!(event instanceof DefaultRowChange)) {
					return;
				}
				DBMSRowChange changeEvent = (DBMSRowChange) event;
				RowChangeHelper.MapData mapData = RowChangeHelper.convertEvent2Map(changeEvent);
				System.out.println(mapData);
				StringBuilder builder = new StringBuilder("applying: \n");
				DBMSHelper.printDBMSEvent(builder, event);
				System.out.println(builder.toString());
			}
		});
		core.init();
	}
}