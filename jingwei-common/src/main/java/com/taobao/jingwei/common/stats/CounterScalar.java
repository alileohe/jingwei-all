package com.taobao.jingwei.common.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class CounterScalar {
	/** key e.g. 区间[0,10) value 区间的值*/
	private HashMap<Integer, AtomicInteger> scalar;

	private static final int DEFAULT_SCALE = 10;

	private static final String SPLIT_MARK = "-";

	// 不在range范围之内的值
	private static final int OUT_OF_RANGE = -1;

	// 模式的长度
	private static final int PATTERN_LEN = 3;

	// OUT_END
	private static final String OUT_END = "000";

	// 刻度
	private int scale;

	// 范围
	private int range;

	// 数量
	private int count;

	/**
	 * 
	 * @param scale 5
	 * @param range 1000
	 */
	public CounterScalar(int scale, int range) {
		this.scale = scale;
		this.range = range;

		if (this.scale <= 0) {
			this.scale = DEFAULT_SCALE;
		}

		this.scalar = new HashMap<Integer, AtomicInteger>();

		this.count = range / scale;

		for (int i = 0; i < count; i++) {
			this.scalar.put(i, new AtomicInteger(0));
		}

		this.scalar.put(OUT_OF_RANGE, new AtomicInteger(0));
	}

	public void put(int value) {
		// 统计量在范围之外
		if (value < 0 || value >= this.range) {
			scalar.get(OUT_OF_RANGE).incrementAndGet();
			return;
		}

		int position = value / this.scale;

		scalar.get(position).incrementAndGet();
	}

	/**
	 * 
	 * @param index 
	 * @return <code>[start, end)</code>
	 */
	public String getKeyString(int index) {
		if (OUT_OF_RANGE == index) {
			return new StringBuilder().append(this.range).append(SPLIT_MARK).append(OUT_END).toString();
		}

		int start = index * this.scale;
		int end = start + this.scale;

		String startPattern = this.formatData(start);
		String endPattern = this.formatData(end);

		return new StringBuilder().append(startPattern).append(SPLIT_MARK).append(endPattern).toString();
	}

	/**
	 * 
	 * @return
	 */
	public HashMap<Integer, Integer> getAndReset() {

		HashMap<Integer, Integer> old = new HashMap<Integer, Integer>();

		for (Map.Entry<Integer, AtomicInteger> entry : this.scalar.entrySet()) {
			int value = entry.getValue().get();
			old.put(entry.getKey(), value);
			entry.getValue().addAndGet(-value);
		}

		return old;
	}

	private String formatData(int value) {
		int len = Integer.valueOf(value).toString().length();
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < PATTERN_LEN - len; i++) {
			sb.append(0);
		}
		sb.append(value);

		return sb.toString();
	}
}
