package com.taobao.jingwei.core.other;

import com.taobao.tddl.jdbc.group.TGroupDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicLong;

public class MultiDBInsert {

	private CyclicBarrier barrier;

	private final JdbcTemplate jt;

	private final AtomicLong pkValue;

	private final String QUERY_MAX_PK_SQL = "SELECT MAX(pk) FROM test_c";

	private final String INSERT_SQL = "INSERT INTO test_c(text) VALUES (?)";

	private final int threadCount;
	private final int period;

	private final AtomicLong rowCount = new AtomicLong(0);
	private final long startTime = System.currentTimeMillis();

	public MultiDBInsert(int threadCount, int parties, int period, String groupName, String appName) {

		DataSource dataSource = this.createDataSource(groupName, appName);
		this.jt = new JdbcTemplate(dataSource);
		pkValue = new AtomicLong(this.getMaxPk() + 1);
		this.threadCount = threadCount;
		this.period = period;
	}

	public void init() {
		if (this.period > 1) {
			this.barrier = new CyclicBarrier(this.period);
		}
		for (int i = 0; i < this.threadCount; i++) {
			new Task(i).start();
		}
	}

	private DataSource createDataSource(String groupName, String appName) {
		TGroupDataSource dataSource = new TGroupDataSource(groupName, appName);
        dataSource.init();
		return dataSource;
	}

	private long getMaxPk() {
		return this.jt.queryForLong(QUERY_MAX_PK_SQL);
	}

	private void insertData() {
		jt.update(INSERT_SQL, new Object[] { this.pkValue.get() });
		this.pkValue.getAndIncrement();
		long totalCount = rowCount.incrementAndGet();
		if (totalCount % 10000 == 0) {
			long costTime = (System.currentTimeMillis() - startTime) / 1000;
			long tps = totalCount / costTime;
			System.out.println("==================TPS: " + tps + "=================");
		}
	}

	class Task extends Thread {
		private int id;

		public Task(int id) {
			this.id = id;
		}

		public void run() {
			while (true) {
				try {
					if (null != barrier) {
						barrier.await();
					}
					insertData();
					if (period > 0) {
						Thread.sleep(period);
					}
				} catch (Throwable e) {
					System.out.println("insert error id: " + this.id + " cause: " + e);
				}
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MultiDBInsert mdbi = new MultiDBInsert(4, 2, 0, "JW_AT_TEST_GROUP_W", "JW_AT_TEST");
		mdbi.init();
	}
}
