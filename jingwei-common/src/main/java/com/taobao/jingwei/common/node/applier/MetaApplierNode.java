package com.taobao.jingwei.common.node.applier;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.node.type.CompressionType;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class MetaApplierNode 将数据写入到META中的applier
 * 的NODE节点
 *
 * @author qihao <dqylyln@gmail.com>
 * @since 11-11-15
 */
public class MetaApplierNode extends AbstractApplierNode {

	private String metaTopic;
	private String shardColumn;
	private boolean splitTxEvent;
	private int maxEventSize = 6144;
	private long sendTimeOut = 3000L;
	private String compressionType = "NONE";
	private String eventFilterData;
	private boolean enableColumnfilter;

	private final static String META_TOPIC = "metaTopic";
	private final static String SHARD_COLUMN = "shardColumn";
	private final static String SPLIT_TX_EVENT = "splitTxEvent";
	private final static String MAX_EVENT_SIZE = "maxEventSize";
	private final static String EVENT_FILTER_DATA = "eventFilterData";
	private final static String SEND_TIMEOUT = "sendTimeOut";
	private final static String ENABLE_COLUMN_FILTER = "enableColumnfilter";
	private final static String COMPRESSION_TYPE = "compressionType";

	@Override
	protected void specilizeAttributeToJsonObject(JSONObject jsonObject) throws JSONException {
		jsonObject.put(META_TOPIC, StringUtil.defaultIfBlank(metaTopic));
		jsonObject.put(SHARD_COLUMN, StringUtil.defaultIfBlank(shardColumn));
		jsonObject.put(SPLIT_TX_EVENT, splitTxEvent);
		jsonObject.put(MAX_EVENT_SIZE, maxEventSize);
		jsonObject.put(EVENT_FILTER_DATA, StringUtil.defaultIfBlank(this.eventFilterData));
		jsonObject.put(SEND_TIMEOUT, sendTimeOut);
		jsonObject.put(ENABLE_COLUMN_FILTER, enableColumnfilter);
		jsonObject.put(COMPRESSION_TYPE, compressionType);
	}

	public MetaApplierNode() {
	}

	public MetaApplierNode(String applierData) {
		try {
			this.jsonStringToNodeSelf(applierData);
		} catch (JSONException e) {
			logger.error("new MetaApplierNode  paser applierData Error!", e);
		}
	}

	@Override
	protected void jsonObjectToSpecilizeAttribute(JSONObject jsonObject) throws JSONException {
		this.setMetaTopic(jsonObject.getString(META_TOPIC));
		this.setShardColumn(jsonObject.getString(SHARD_COLUMN));
		this.setSplitTxEvent(jsonObject.getBoolean(SPLIT_TX_EVENT));
		this.setMaxEventSize(jsonObject.getInt(MAX_EVENT_SIZE));
		if (jsonObject.has(EVENT_FILTER_DATA)) {
			this.setEventFilterData(jsonObject.getString(EVENT_FILTER_DATA));
		}
		if (jsonObject.has(SEND_TIMEOUT)) {
			this.setSendTimeOut(jsonObject.getLong(SEND_TIMEOUT));
		}
		if (jsonObject.has(ENABLE_COLUMN_FILTER)) {
			this.setEnableColumnfilter(jsonObject.getBoolean(ENABLE_COLUMN_FILTER));
		}
		if (jsonObject.has(COMPRESSION_TYPE)) {
			this.setCompressionType(jsonObject.getString(COMPRESSION_TYPE));
		}
	}

	public String getMetaTopic() {
		return metaTopic;
	}

	public void setMetaTopic(String metaTopic) {
		this.metaTopic = metaTopic;
	}

	public String getShardColumn() {
		return shardColumn;
	}

	public void setShardColumn(String shardColumn) {
		this.shardColumn = shardColumn;
	}

	public boolean isSplitTxEvent() {
		return splitTxEvent;
	}

	public void setSplitTxEvent(boolean splitTxEvent) {
		this.splitTxEvent = splitTxEvent;
	}

	public int getMaxEventSize() {
		return maxEventSize;
	}

	public void setMaxEventSize(int maxEventSize) {
		this.maxEventSize = maxEventSize;
	}

	public String getEventFilterData() {
		return eventFilterData;
	}

	public void setEventFilterData(String eventFilterData) {
		this.eventFilterData = eventFilterData;
	}

	public long getSendTimeOut() {
		return sendTimeOut;
	}

	public void setSendTimeOut(long sendTimeOut) {
		this.sendTimeOut = sendTimeOut;
	}

	public boolean isEnableColumnfilter() {
		return enableColumnfilter;
	}

	public void setEnableColumnfilter(boolean enableColumnfilter) {
		this.enableColumnfilter = enableColumnfilter;
	}

	public String getCompressionType() {
		return compressionType.toString();
	}

	public void setCompressionType(String compressionType) {
		CompressionType type = CompressionType.valueOf(compressionType);
		if (null != type) {
			this.compressionType = compressionType;
		}
	}
}