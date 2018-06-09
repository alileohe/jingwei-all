package com.taobao.jingwei.core.loader;

import com.taobao.tddl.dbsync.applier.Applier;
import com.taobao.tddl.dbsync.applier.ApplierException;
import com.taobao.tddl.dbsync.dbms.DBMSEvent;
import com.taobao.tddl.dbsync.dbms.DBMSHelper;
import com.taobao.tddl.dbsync.dbms.DefaultRowChange;
import com.taobao.tddl.dbsync.plugin.AbstractPlugin;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.atomic.AtomicLong;

/**   
 * <p>description: �򵥵�Applier �������յ�event���ӡevent<p> 
 *
 * @{#} SimpleApplier.java Create on Dec 31, 2011 3:59:23 PM   
 *   
 *@version 1.0   
 */
public class SimpleApplier extends AbstractPlugin implements Applier {
	private static Log log = LogFactory.getLog(SimpleApplier.class);

	protected AtomicLong applied = new AtomicLong();

	protected volatile DBMSEvent lastEvent;

	/**
	 * Return last applying event.
	 * 
	 * @return The last applying event.
	 */
	public DBMSEvent getLastEvent() {
		return lastEvent;
	}

	/**
	 * Return applied events number.
	 * 
	 * @return The applied events.
	 */
	public long getApplied() {
		return applied.longValue();
	}

	private long lastTime = 0;

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.taobao.tddl.dbsync.applier.Applier#apply(com.taobao.tddl.dbsync.dbms.DBMSEvent)
	 */
	public void apply(DBMSEvent event) throws ApplierException, InterruptedException {

		applied.incrementAndGet();

		if (!(event instanceof DefaultRowChange)) {
			return;
		}

		//	DBMSRowChange changeEvent = (DBMSRowChange) event;

		StringBuilder builder = new StringBuilder("applying: \n");
		DBMSHelper.printDBMSEvent(builder, event);
		System.out.println(builder.toString());
		//logger.warn(builder.toString());

		stat();
		lastEvent = event;
	}

	public void stat() {
		long currentCount = applied.get();
		if (currentCount % 10000 == 0) {
			long current = System.currentTimeMillis();
			String msg = "total count : " + currentCount + "\t extract from meta qps : " + 10000
					/ ((double) (current - lastTime) / 1000);
			System.out.println(msg);
			log.error(msg);
			lastTime = current;
		}
	}
}
