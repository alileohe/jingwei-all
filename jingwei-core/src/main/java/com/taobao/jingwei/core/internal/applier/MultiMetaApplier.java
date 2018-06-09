package com.taobao.jingwei.core.internal.applier;

import com.taobao.jingwei.common.node.applier.MetaApplierNode;
import com.taobao.jingwei.common.node.applier.MultiMetaApplierNode;
import com.taobao.jingwei.common.node.applier.SubMetaApplierNode;
import com.taobao.tddl.dbsync.DbsyncException;
import com.taobao.tddl.dbsync.applier.Applier;
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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @desc 解决一个db抓取数据后根据不同的表投递到不同的Applier
 * 
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Mar 20, 2012 3:12:17 PM
 */

public class MultiMetaApplier extends AbstractPlugin implements Applier, TxAware, DbsyncPlugin {
	private Log log = LogFactory.getLog(this.getClass());

	/**  对应一个META TOPIC的Applier */
	private List<SubMetaApplier> subMetaAppliers = new ArrayList<SubMetaApplier>();

	/** 存储了meta applier 正常工作的元数据、属性*/
	private final MultiMetaApplierNode multiMetaApplierNode;

	public MultiMetaApplier(MultiMetaApplierNode multiMetaApplierNode) {
		this.multiMetaApplierNode = multiMetaApplierNode;
	}

	public void init(String name, PluginContext context) throws PluginException, InterruptedException {
		int size = this.multiMetaApplierNode.getSubMetaApplierNodes().size();

		for (int i = 0; i < size; i++) {
			SubMetaApplierNode subMetaApplierNode = this.multiMetaApplierNode.getSubMetaApplierNodes().get(i);
			SubMetaApplier subMetaApplier = new SubMetaApplier(subMetaApplierNode);

			subMetaApplier.setSrcSchemaPattern(subMetaApplierNode.getSrcSchemaReg());
			subMetaApplier.setSrcTablePattern(subMetaApplierNode.getSrcTableReg());

			subMetaApplier.init(name, context);
			subMetaAppliers.add(subMetaApplier);
		}
	}

	@Override
	public void commit() throws DbsyncException, InterruptedException {
		for (SubMetaApplier applier : subMetaAppliers) {
			applier.commit();
		}
	}

	@Override
	public void rollback() throws DbsyncException, InterruptedException {
		for (SubMetaApplier applier : subMetaAppliers) {
			applier.rollback();
		}
	}

	@Override
	public void apply(DBMSEvent event) throws ApplierException, InterruptedException {
		if (!(event instanceof DBMSRowChange)) {
			if (log.isWarnEnabled()) {
				log.warn("not dbmsRowChange event, is : " + event.getSchema() + event.toString());
			}
			return;
		}

		DBMSRowChange rowChange = (DBMSRowChange) event;

		String schema = event.getSchema();

		for (SubMetaApplier applier : subMetaAppliers) {

			Matcher schemaMacher = applier.getSrcSchemaPattern().matcher(schema);

			if (!schemaMacher.matches()) {
				continue;
			}

			String table = rowChange.getTable();

			Matcher tableMacher = applier.getSrcTablePattern().matcher(table);

			if (tableMacher.matches()) {
				applier.apply(event);
			}
		}
	}

	static class SubMetaApplier extends AbstractPlugin implements Applier, TxAware, DbsyncPlugin {

		private final MetaApplier metaApplier;

		private Pattern srcSchemaPattern;

		private Pattern srcTablePattern;

		public SubMetaApplier(MetaApplierNode metaApplierNode) {
			this.metaApplier = new MetaApplier(metaApplierNode);
		}

		public void init(String name, PluginContext context) throws PluginException, InterruptedException {
			this.metaApplier.init(name, context);
		}

		public void destory() throws PluginException, InterruptedException {
			this.metaApplier.destory();
		}

		@Override
		public void apply(DBMSEvent event) throws ApplierException, InterruptedException {
			this.metaApplier.apply(event);
		}

		@Override
		public void commit() throws DbsyncException, InterruptedException {
			this.metaApplier.commit();

		}

		@Override
		public void rollback() throws DbsyncException, InterruptedException {
			this.metaApplier.rollback();
		}

		public Pattern getSrcTablePattern() {
			return srcTablePattern;
		}

		public void setSrcSchemaPattern(String srcSchemaRex) {
			this.srcSchemaPattern = Pattern.compile(srcSchemaRex);
		}

		public Pattern getSrcSchemaPattern() {
			return srcSchemaPattern;
		}

		public void setSrcTablePattern(String srcTableRex) {
			this.srcTablePattern = Pattern.compile(srcTableRex);
		}
	}

}
