//package com.taobao.jingwei.core.internal.applier;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//
//import com.taobao.jingwei.common.node.applier.AndorCommandApplierNode;
//import com.taobao.tddl.dbsync.applier.Applier;
//import com.taobao.tddl.dbsync.applier.ApplierException;
//import com.taobao.tddl.dbsync.dbms.DBMSAction;
//import com.taobao.tddl.dbsync.dbms.DBMSEvent;
//import com.taobao.tddl.dbsync.dbms.DBMSRowChange;
//import com.taobao.tddl.dbsync.plugin.AbstractPlugin;
//import com.taobao.tddl.dbsync.plugin.PluginContext;
//import com.taobao.tddl.dbsync.plugin.PluginException;
//import com.taobao.ustore.client.andor.executor.andor_executor.AndOrClient;
//import com.taobao.ustore.client.andor.utils.AndOrUtils;
//import com.taobao.ustore.common.config.ConfigContext;
//import com.taobao.ustore.common.exception.EmptyResultRestrictionException;
//import com.taobao.ustore.common.inner.bean.IDataNodeExecutor;
//
//public class AndorCommandApplier extends AbstractPlugin implements Applier {
//	
//	private static Log logger = LogFactory.getLog(AndorCommandApplier.class);
//
//	private AndorCommandApplierNode andorCommandApplierNode;
//	private AndOrClient client;
//	
//	
//	public AndorCommandApplier(AndorCommandApplierNode andorCommandApplierNode) {
//		this.andorCommandApplierNode = andorCommandApplierNode;
//	}
//	
//	
//	@Override
//	public void init(String name, PluginContext context)
//			throws PluginException, InterruptedException {
//		super.init(name, context);
//		client = new AndOrClient();
//		ConfigContext config = new ConfigContext();
//		config.setAppName(andorCommandApplierNode.getAppName());
//		client.setConfigContext(config);
//		try {
//			client.init();
//		} catch (Exception e) {
//			e.printStackTrace();
//			logger.error("andorClient init error:", e);
//			throw new PluginException("andorClient init Error", e);
//		}
//		
//	}
//	
//	
//	@Override
//	public void apply(DBMSEvent event) throws ApplierException,
//			InterruptedException {
//		if (null != event && event instanceof DBMSRowChange) {
//			DBMSRowChange rowChangeEvent = (DBMSRowChange) event;
//			if(!needCascadeUpdate(rowChangeEvent)){
//				return;
//			}
//			List<IDataNodeExecutor> putList = AndOrUtils.convertToPut(rowChangeEvent, client,this.andorCommandApplierNode.getCascadeIndexNameMap());
////			改为并发
////			TODO 这里面的group这么实现，list怎么分割？
//			CountDownLatch countDownLatch = new CountDownLatch(putList.size());
//			ExecutorService executorService = Executors.newFixedThreadPool(putList.size());
//			for(IDataNodeExecutor dataNode:putList){
//				executorService.execute(new DataNodeExecutor(dataNode, countDownLatch, client));
//			}
//			countDownLatch.await();
//		}
//
//	}
//	
//	
//	private boolean needCascadeUpdate(DBMSRowChange event) {
//		String tableName = event.getTable();
//		DBMSAction action = event.getAction();
//		if(!this.andorCommandApplierNode.getCascadeIndexNameMap().containsKey(tableName)){
//			return false;
//		}
//		
//		switch(action){
//		case DELETE:
//			return true;
//		case INSERT:
//			return true;
//		case UPDATE:
//			return true;
//		}
//		return false;
//	}
//	
//	
//	class DataNodeExecutor implements Runnable{
//		IDataNodeExecutor dataNode;
//		CountDownLatch countDownLatch;
//		AndOrClient client;
//		public DataNodeExecutor(IDataNodeExecutor dataNode, CountDownLatch countDownLatch,AndOrClient client) {
//			this.countDownLatch = countDownLatch;
//			this.dataNode = dataNode;
//			this.client = client;
//		}
//		
//		@Override
//		public void run() {
//			try {
//				dataNode = client.getAndOrExecutor().getClientContext().getDataNodeChooser().shard(dataNode, null, null);
//			} catch (EmptyResultRestrictionException e) {
//				logger.error("dataNode parse wrong!"+ e.getMessage());
//				e.printStackTrace();
//			}
//			try {
//				client.getAndOrExecutor().execByExecPlanNode(null, dataNode,
//						Collections.EMPTY_LIST);
//			} catch (Exception e) {
//				logger.error("andor execute error:"+e.getMessage());
//				countDownLatch.countDown();
//				return;
//			}
//			logger.error("andor execute dataNode:"+dataNode.toString());
//			countDownLatch.countDown();
//		}
//		
//	}
//
//}
