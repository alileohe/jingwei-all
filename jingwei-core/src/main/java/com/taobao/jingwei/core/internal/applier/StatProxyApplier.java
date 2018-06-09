package com.taobao.jingwei.core.internal.applier;

import com.taobao.jingwei.common.stats.StatsUnit;
import com.taobao.tddl.dbsync.DbsyncException;
import com.taobao.tddl.dbsync.applier.Applier;
import com.taobao.tddl.dbsync.applier.ApplierBrokenException;
import com.taobao.tddl.dbsync.applier.ApplierException;
import com.taobao.tddl.dbsync.dbms.DBMSAction;
import com.taobao.tddl.dbsync.dbms.DBMSEvent;
import com.taobao.tddl.dbsync.dbms.DBMSRowChange;
import com.taobao.tddl.dbsync.plugin.AbstractPlugin;
import com.taobao.tddl.dbsync.plugin.DbsyncPlugin;
import com.taobao.tddl.dbsync.plugin.PluginContext;
import com.taobao.tddl.dbsync.plugin.PluginException;
import com.taobao.tddl.dbsync.tx.Tx;
import com.taobao.tddl.dbsync.tx.TxAware;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Timestamp;

/**   
 * <p>description:<p>  
 * 
 * ������ͳ�ƴ���Applier
 *
 * @{#} StatProxyApplier.java Create on Dec 9, 2011 12:40:14 PM   
 *   
 * Copyright (c) 2011 by qihao.
 *
 *@author <a href="mailto:qihao@taobao.com">qihao</a> 
 *@version 1.0   
 */
public class StatProxyApplier extends AbstractPlugin implements Applier, TxAware {

	private static final Log logger = LogFactory.getLog(StatProxyApplier.class);

	private Applier applier;

	private StatsUnit statsUnit;

	public StatProxyApplier(Applier applier, StatsUnit statsUnit) {
		this.applier = applier;
		this.statsUnit = statsUnit;
	}

	@Override
	public void apply(DBMSEvent event) throws ApplierException, InterruptedException {
		if (event instanceof DBMSRowChange) {
			DBMSAction action = event.getAction();
			//��¼event��extractorʱ��
			Tx tx = Tx.local();
			if (null != tx) {
				Timestamp t = tx.getExtractTimestamp();
				if (null != t) {
					statsUnit.addExtractorDelay(System.currentTimeMillis() - t.getTime());
				}
			}
			DBMSRowChange rowChangeEvent = (DBMSRowChange) event;
			int dataCount = rowChangeEvent.getRowSize();
			//��¼action��Ӧ����¼����
			statActionCount(action, dataCount);
			long applyStart = System.currentTimeMillis();
			try {
				this.applier.apply(event);
				long applyEnd = System.currentTimeMillis();
				//��¼apply���ӳ�
				statApplyDelay(action, applyEnd - applyStart);
			} catch (Throwable e) {
				logger.error(" StatProxyApplier apply Error! ", e);
				//ʧ�ܼ�¼ͳ��
				statExceptionCount(action);
				//�쳣�����׳�
				if (e instanceof ApplierException) {
					throw (ApplierException) e;
				} else if (e instanceof InterruptedException) {
					throw (InterruptedException) e;
				} else {
					throw new ApplierBrokenException(e);
				}
			}
		}
	}

	private void statApplyDelay(DBMSAction action, long applyDelay) {
		if (DBMSAction.INSERT == action) {
			statsUnit.addInsertDelay(applyDelay);
		} else if (DBMSAction.UPDATE == action) {
			statsUnit.addUpdateDelay(applyDelay);
		} else if (DBMSAction.DELETE == action) {
			statsUnit.addDeleteDelay(applyDelay);
		}
	}

	private void statActionCount(DBMSAction action, int dataCount) {
		if (DBMSAction.INSERT == action) {
			statsUnit.addInsertCount(dataCount);
		} else if (DBMSAction.UPDATE == action) {
			statsUnit.addUpdateCount(dataCount);
		} else if (DBMSAction.DELETE == action) {
			statsUnit.addDeleteCount(dataCount);
		}
	}

	private void statExceptionCount(DBMSAction action) {
		if (DBMSAction.INSERT == action) {
			statsUnit.incrementInsertExceptionCount();
		} else if (DBMSAction.UPDATE == action) {
			statsUnit.incrementUpdateExceptionCount();
		} else if (DBMSAction.DELETE == action) {
			statsUnit.incrementDeleteExceptionCount();
		}
	}

	@Override
	public void init(String name, PluginContext context) throws PluginException, InterruptedException {
		if (applier instanceof DbsyncPlugin) {
			((DbsyncPlugin) this.applier).init(name, context);
		}
	}

	@Override
	public void destory() throws PluginException, InterruptedException {
		if (applier instanceof DbsyncPlugin) {
			((DbsyncPlugin) this.applier).destory();
		}
	}

	@Override
	public void commit() throws InterruptedException, DbsyncException {
		if (this.applier instanceof TxAware) {
			((TxAware) this.applier).commit();
		}
	}

	@Override
	public void rollback() throws InterruptedException, DbsyncException {
		if (this.applier instanceof TxAware) {
			((TxAware) this.applier).rollback();
		}
	}
}
