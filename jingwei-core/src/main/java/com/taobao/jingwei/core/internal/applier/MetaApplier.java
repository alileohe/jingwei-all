package com.taobao.jingwei.core.internal.applier;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.node.applier.EventFilterNode;
import com.taobao.jingwei.common.node.applier.MetaApplierNode;
import com.taobao.jingwei.core.util.DBMSEventFileFilter;
import com.taobao.jingwei.core.util.DBMSEventFileFilter.FilterCondition;
import com.taobao.jingwei.core.util.TaskCoreUtil;
import com.taobao.tddl.dbsync.DbsyncException;
import com.taobao.tddl.dbsync.applier.Applier;
import com.taobao.tddl.dbsync.applier.ApplierBrokenException;
import com.taobao.tddl.dbsync.applier.ApplierException;
import com.taobao.tddl.dbsync.dbms.DBMSEvent;
import com.taobao.tddl.dbsync.dbms.DBMSRowChange;
import com.taobao.tddl.dbsync.plugin.AbstractPlugin;
import com.taobao.tddl.dbsync.plugin.DbsyncPlugin;
import com.taobao.tddl.dbsync.plugin.PluginContext;
import com.taobao.tddl.dbsync.plugin.PluginException;
import com.taobao.tddl.dbsync.tx.TxAware;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

/**   
 * <p>description:精卫包装过的MetaApplier<p> 
 *
 * @{#} MetaApplier.java Create on Jan 17, 2012 3:27:21 PM   
 *   
 * Copyright (c) 2012 by qihao.
 *
 *@author <a href="mailto:qihao@taobao.com">qihao</a> 
 *@version 1.0   
 */
public class MetaApplier extends AbstractPlugin implements Applier, TxAware, DbsyncPlugin {
	private Log log = LogFactory.getLog(this.getClass());

	private final com.taobao.tddl.dbsync.meta.MetaApplier metaApplier = new com.taobao.tddl.dbsync.meta.MetaApplier();

	private MetaApplierNode metaApplierNode;

	private DBMSEventFileFilter dbmsEventFileFilter = null;

	public MetaApplier(MetaApplierNode metaApplierNode) {
		this.metaApplierNode = metaApplierNode;
	}

	public void init(String name, PluginContext context) throws PluginException, InterruptedException {
		super.init(name, context);
		this.metaApplier.setTopic(this.metaApplierNode.getMetaTopic());
		this.metaApplier.setShardColumn(this.metaApplierNode.getShardColumn());
		this.metaApplier.setSplitTxEvent(this.metaApplierNode.isSplitTxEvent());
		this.metaApplier.setMaxEventSize(this.metaApplierNode.getMaxEventSize());
		this.metaApplier.setTimeout(this.metaApplierNode.getSendTimeOut());
		this.metaApplier.setCompression(this.metaApplierNode.getCompressionType());
		try {
			this.initEventFilter(this.metaApplierNode.getEventFilterData());
		} catch (Exception e) {
			throw new PluginException(e);
		}
		this.metaApplier.init(name, context);
	}

	public void destory() throws PluginException, InterruptedException {
		this.metaApplier.destory();
	}

	@Override
	public void apply(DBMSEvent event) throws ApplierException, InterruptedException {
		if (null == event || !(event instanceof DBMSRowChange)) {
			return;
		}

		DBMSRowChange dbmsRowChange = (DBMSRowChange) event;

		try {
			// 如果没有配置过滤条件则直接过滤
			if (null == this.dbmsEventFileFilter) {
				this.metaApplier.apply(event);
			} else {
				DBMSRowChange filteredEvent;

				filteredEvent = this.dbmsEventFileFilter.convert(dbmsRowChange);
				if (null != filteredEvent) {
					this.metaApplier.apply(filteredEvent);
				}
			}
		} catch (Exception e) {
			TaskCoreUtil.errorApplierExceptionEvent(event, log);
			throw new ApplierBrokenException(e);
		}
	}

	@Override
	public void commit() throws DbsyncException, InterruptedException {
		this.metaApplier.commit();

	}

	@Override
	public void rollback() throws DbsyncException, InterruptedException {
		this.metaApplier.rollback();

	}

	/**
	 * 初始化DBMSEventFilter
	 * @param eventFilterString
	 * @throws Exception 
	 */
	private void initEventFilter(String eventFilterString) throws Exception {
		EventFilterNode eventFilterNode = new EventFilterNode();

		if (StringUtil.isBlank(eventFilterString)) {
			return;
		}
		try {
			eventFilterNode.jsonStringToNodeSelf(eventFilterString);
		} catch (JSONException e) {
			log.error(e);
			throw new PluginException("applier event filter data error!");
		}
		FilterCondition filterCondition = new FilterCondition(eventFilterNode);
		this.dbmsEventFileFilter = new DBMSEventFileFilter(filterCondition);
	}
}
