package $package;

import java.util.concurrent.atomic.AtomicLong;

import com.taobao.tddl.dbsync.applier.Applier;
import com.taobao.tddl.dbsync.applier.ApplierException;
import com.taobao.tddl.dbsync.dbms.DBMSEvent;
import com.taobao.tddl.dbsync.dbms.DBMSHelper;
import com.taobao.tddl.dbsync.plugin.AbstractPlugin;

/**   
 * <p>description: 简单的Applier 用来接收到event后打印event<p> 
 *
 * @{#} SimpleApplier.java Create on Dec 31, 2011 3:59:23 PM   
 *   
 *@version 1.0   
 */
public class SimpleApplier extends AbstractPlugin implements Applier {

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

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.taobao.tddl.dbsync.applier.Applier#apply(com.taobao.tddl.dbsync.dbms.DBMSEvent)
	 */
	public void apply(DBMSEvent event) throws ApplierException, InterruptedException {
		if (logger.isWarnEnabled()) {
			StringBuilder builder = new StringBuilder("applying: \n");
			DBMSHelper.printDBMSEvent(builder, event);
			logger.warn(builder.toString());
		}
		applied.incrementAndGet();
		lastEvent = event;
	}
}
