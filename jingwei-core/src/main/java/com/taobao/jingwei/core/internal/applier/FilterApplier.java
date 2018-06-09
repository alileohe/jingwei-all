package com.taobao.jingwei.core.internal.applier;

import com.taobao.jingwei.common.node.applier.ApplierFilterNode;
import com.taobao.jingwei.core.util.DBMSEventFileFilter;
import com.taobao.jingwei.core.util.DBMSEventFileFilter.FilterCondition;
import com.taobao.tddl.dbsync.DbsyncException;
import com.taobao.tddl.dbsync.applier.Applier;
import com.taobao.tddl.dbsync.applier.ApplierBrokenException;
import com.taobao.tddl.dbsync.applier.ApplierException;
import com.taobao.tddl.dbsync.dbms.DBMSEvent;
import com.taobao.tddl.dbsync.dbms.DBMSHelper;
import com.taobao.tddl.dbsync.dbms.DBMSRowChange;
import com.taobao.tddl.dbsync.plugin.DbsyncPlugin;
import com.taobao.tddl.dbsync.plugin.PluginContext;
import com.taobao.tddl.dbsync.plugin.PluginException;
import com.taobao.tddl.dbsync.tx.TxAware;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @desc 
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Apr 16, 2012 2:27:16 PM
 */

public class FilterApplier implements Applier, TxAware, DbsyncPlugin {

	private Log log = LogFactory.getLog(this.getClass());

	private final Applier applier;

	private ApplierFilterNode applierFilterNode = null;

	private Pattern srcSchemaPattern;

	private Pattern srcTablePattern;

	private DBMSEventFileFilter dbmsEventFileFilter = null;

	public FilterApplier(Applier applier) {
		this.applier = applier;
	}

	@Override
	public void apply(DBMSEvent event) throws ApplierException, InterruptedException {

		if (null == event || !(event instanceof DBMSRowChange)) {
			return;
		}

		DBMSRowChange dbmsRowChange = (DBMSRowChange) event;

		// ��������
		Matcher schemaMacher = this.getSrcSchemaPattern().matcher(dbmsRowChange.getSchema());

		if (!schemaMacher.matches()) {
			return;
		}

		String table = dbmsRowChange.getTable();

		// ��������
		Matcher tableMacher = this.getSrcTablePattern().matcher(table);

		if (!tableMacher.matches()) {
			return;
		}

		// ���û�������С�action����������apply
		if (null == this.dbmsEventFileFilter) {
			this.applier.apply(event);
		} else {
			// �ֶΡ�action����
			DBMSRowChange filteredEvent;
			try {
				filteredEvent = this.dbmsEventFileFilter.convert(dbmsRowChange);
				if (null != filteredEvent) {
					this.applier.apply(filteredEvent);
					if (log.isInfoEnabled()) {
						StringBuilder builder = new StringBuilder("after filter, event : \n");
						DBMSHelper.printDBMSEvent(builder, filteredEvent);
						log.info(builder.toString());
					}
				}
			} catch (Exception e) {
				throw new ApplierBrokenException(e);
			}
		}
	}

	@Override
	public void init(String name, PluginContext context) throws PluginException, InterruptedException {
		// ��������
		this.setSrcSchemaPattern(Pattern.compile(this.getApplierFilterNode().getSrcSchemaReg()));

		// ��������
		this.setSrcTablePattern(Pattern.compile(this.getApplierFilterNode().getSrcTableReg()));

		// �ֶι��� ��action����
		FilterCondition filterCondition = new FilterCondition(this.getApplierFilterNode().getEventFilterNode());

		try {
			this.dbmsEventFileFilter = new DBMSEventFileFilter(filterCondition);
		} catch (Exception e) {
			log.error("init DBMSEventFileFilter Error ", e);
			throw new PluginException(e);
		}

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

	public ApplierFilterNode getApplierFilterNode() {
		return applierFilterNode;
	}

	public void setApplierFilterNode(ApplierFilterNode applierFilterNode) {
		this.applierFilterNode = applierFilterNode;
	}

	public Pattern getSrcSchemaPattern() {
		return srcSchemaPattern;
	}

	public void setSrcSchemaPattern(Pattern srcSchemaPattern) {
		this.srcSchemaPattern = srcSchemaPattern;
	}

	public Pattern getSrcTablePattern() {
		return srcTablePattern;
	}

	public void setSrcTablePattern(Pattern srcTablePattern) {
		this.srcTablePattern = srcTablePattern;
	}
}
