package com.taobao.jingwei.common.stats;

/**
 * @desc 对应dbsync的统计数据
 * @author <a href="mailto:shuohai.lhl@taobao.com">朔海 shuohailhl</a>
 * @date 2011-12-8下午1:16:15
 */
public final class TransactionStats {

	private volatile Long txCount = Long.valueOf(0L);
	private volatile Float txTps = Float.valueOf(0F);
	private volatile Long millisMinLatency = Long.valueOf(0L);
	private volatile Long millisMaxLatency = Long.valueOf(0L);
	private volatile Long millisAvgLatency = Long.valueOf(0L);

	public Long getTxCount() {
		return txCount;
	}

	public void setTxCount(Long txCount) {
		this.txCount = txCount;
	}

	public Long getMillisMinLatency() {
		return millisMinLatency;
	}

	public void setMillisMinLatency(Long millisMinLatency) {
		this.millisMinLatency = millisMinLatency;
	}

	public Long getMillisMaxLatency() {
		return millisMaxLatency;
	}

	public void setMillisMaxLatency(Long millisMaxLatency) {
		this.millisMaxLatency = millisMaxLatency;
	}

	public Long getMillisAvgLatency() {
		return millisAvgLatency;
	}

	public void setMillisAvgLatency(Long millisAvgLatency) {
		this.millisAvgLatency = millisAvgLatency;
	}

	public Float getTxTps() {
		return txTps;
	}

	public void setTxTps(Float txTps) {
		this.txTps = txTps;
	}
}