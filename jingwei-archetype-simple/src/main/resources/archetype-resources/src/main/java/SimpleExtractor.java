package $package;

import java.sql.Timestamp;
import java.util.Random;

import com.taobao.tddl.dbsync.DbsyncException;
import com.taobao.tddl.dbsync.dbms.DBMSEvent;
import com.taobao.tddl.dbsync.dbms.DefaultQueryLog;
import com.taobao.tddl.dbsync.dbms.DBMSHelper;
import com.taobao.tddl.dbsync.extractor.Extractor;
import com.taobao.tddl.dbsync.extractor.ExtractorBrokenException;
import com.taobao.tddl.dbsync.extractor.ExtractorException;
import com.taobao.tddl.dbsync.extractor.Transferer;
import com.taobao.tddl.dbsync.plugin.AbstractPlugin;
import com.taobao.tddl.dbsync.tx.Tx;

/**  
 * <p>description:简单的EXTRACTOR 定时产生event<p> 
 *
 * @{#} SimpleExtractor.java Create on Dec 31, 2011 3:52:10 PM   
 *   
 * Copyright (c) 2011 
 *
 *@version 1.0   
 */
public class SimpleExtractor extends AbstractPlugin implements Extractor {

	public static final long SLEEPING = 1000;
	public static final long HEARTBEAT = 5000;

	protected final Random rand = new Random();

	protected long lastPosition = 0L;
	protected long lastHeartbeat = System.currentTimeMillis();

	protected int eventOfTx = 3;
	protected long extractedTx = 0;
	protected long extracted = 0;

	/**
	 * Change event number of transaction.
	 * 
	 * @param eventOfTx The event number.
	 */
	public void setEventOfTx(int eventOfTx) {
		this.eventOfTx = eventOfTx;
	}

	/**
	 * Return last event position.
	 * 
	 * @return The last event position
	 */
	public long getLastPosition() {
		return lastPosition;
	}

	/**
	 * Return generated transactions.
	 * 
	 * @return The generated tx number.
	 */
	public long getExtractedTx() {
		return extractedTx;
	}

	/**
	 * Return generated events.
	 * 
	 * @return The generated event number.
	 */
	public long getExtracted() {
		return extracted;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.taobao.tddl.dbsync.extractor.Extractor#extract(com.taobao.tddl.dbsync.extractor.Transferer)
	 */
	public void extract(Transferer transferer) throws ExtractorException, InterruptedException {
		try {
			if (lastPosition <= 0) {
				lastPosition = System.currentTimeMillis();

				// Restore from transferer last position.
				String position = transferer.getLastPosition();
				if (position != null) {
					if (logger.isInfoEnabled())
						logger.info("Extractor " + super.toString() + " change position to: " + position);
					lastPosition = Long.parseLong(position, 16);
				}
			}

			// Blocking if current position not reach.
			long milliseconds = System.currentTimeMillis() - lastPosition;
			while (milliseconds < SLEEPING) {
				Thread.sleep(SLEEPING - milliseconds);
				milliseconds = System.currentTimeMillis() - lastPosition;
			}
			lastPosition = lastPosition + SLEEPING;
			String position = String.format("%012x", lastPosition);

			// Do heartbeat if timing reached.
			long currentMillis = System.currentTimeMillis();
			if (currentMillis - lastHeartbeat > HEARTBEAT) {
				transferer.heartbeat(position);
				lastHeartbeat = currentMillis;
			}

			// Begin transaction to event.
			@SuppressWarnings("deprecation")
			Tx tx = transferer.begin(position);
			tx.setSourceId(name);
			tx.setSourceTimestamp(new Timestamp(lastPosition));

			for (int number = 0; number < eventOfTx; number++) {
				// Extracting the next DBMSEvent event.
				DBMSEvent event = new DefaultQueryLog("dummy", "UPDATE example SET number = " + number + " WHERE id = "
						+ rand.nextInt(1024), new Timestamp(System.currentTimeMillis()), 0);
				if (logger.isInfoEnabled()) {
					StringBuilder builder = new StringBuilder("extractor: \n");
					DBMSHelper.printDBMSEvent(builder, event);
					logger.info(builder.toString());
				}
				extracted++;
				tx.offer(event);
			}
			// Commit transaction.
			extractedTx++;
			tx.commit();
		} catch (DbsyncException e) {
			throw new ExtractorBrokenException("transfer error: " + e.getMessage(), e);
		}
	}
}