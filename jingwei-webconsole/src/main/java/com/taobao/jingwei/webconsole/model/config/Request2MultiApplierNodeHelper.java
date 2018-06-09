package com.taobao.jingwei.webconsole.model.config;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ini4j.InvalidFileFormatException;
import org.json.JSONException;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode;
import com.taobao.jingwei.common.node.type.CompressionType;
import com.taobao.jingwei.webconsole.model.config.applier.MetaApplierConfig;
import com.taobao.jingwei.webconsole.model.config.applier.MultiMetaApplierConfig;
import com.taobao.jingwei.webconsole.model.config.exception.BatchConfigException;

public class Request2MultiApplierNodeHelper {
	private static Log log = LogFactory.getLog(Request2MultiApplierNodeHelper.class);

	public static void updateMultiApplierConfig(HttpServletRequest request, SyncTaskNode syncTaskNode)
			throws JSONException, InvalidFileFormatException, BatchConfigException {

		// topic name ������
		String[] topics = request.getParameterValues("multiMetaTopic");

		// �ֿ��
		String[] shardColumns = request.getParameterValues("multiShardColumn");
		if (shardColumns.length == 0) {
			shardColumns = new String[] { StringUtil.EMPTY_STRING };
		}

		// ���ͳ�ʱ
		String[] sendTimeOut = request.getParameterValues("multiSendTimeOut");

		// �Ƿ�������
		String[] multiSplitTxEventValueCheck = request.getParameterValues("multiSplitTxEvent");

		// ����¼�����
		String[] maxEventSize = request.getParameterValues("multiMaxEventSize");

		// �������ʽ
		String[] srcSchemaReg = request.getParameterValues("multiSrcSchemaReg");

		// �������ʽ
		String[] srcTableReg = request.getParameterValues("multiSrcTableReg");

		// Action
		String[] insertAction = request.getParameterValues("multiInsert");
		String[] updateAction = request.getParameterValues("multiUpdate");
		String[] deleteAction = request.getParameterValues("multiDelete");

		// ѹ������
		String[] compressionType = request.getParameterValues("compressionType");

		// �����ֶι���
		String[] multiEnableColumnFilter = request.getParameterValues("multiEnableColumnFilter");

		// ���������ų�
		String[] columnFilterUseInclude = request.getParameterValues("columnFilterUseInclude");

		// �й�������
		String[] columnFilterCondition = request.getParameterValues("columnFilterCondition");

		// �Ƿ�ʹ�ø߼�ģʽ
		String[] multiColumnFilterAdvEnabled = request.getParameterValues("multiColumnFilterAdvEnabled");
		if (multiColumnFilterAdvEnabled.length == 0) {
			multiColumnFilterAdvEnabled = new String[] { "" };
		}

		// �Ƿ�̬����
		String[] multiDynaCode = request.getParameterValues("multiColumnFilterAdv");
		if (multiDynaCode.length == 0) {
			multiDynaCode = new String[] { "" };
		}

		List<MetaApplierConfig> metaApplierConfigs = new ArrayList<MetaApplierConfig>();
		for (int i = 0; i < topics.length; i++) {
			MetaApplierConfig subMetaApplierConfig = new MetaApplierConfig();

			// meta topic
			subMetaApplierConfig.setMetaTopic(topics[i]);

			// �ֿ��
			subMetaApplierConfig.setShardColumn(shardColumns[i]);

			subMetaApplierConfig.setCompressionType(CompressionType.NONE.toString());

			subMetaApplierConfig.setSendTimeOut(Integer.valueOf(sendTimeOut[i]));

			// �������
			subMetaApplierConfig.setSplitTxEvent(Boolean.valueOf(multiSplitTxEventValueCheck[i]));

			// ���ʱ�䳤��
			subMetaApplierConfig.setMaxEventSize(Integer.valueOf(maxEventSize[i]));

			// ѹ������
			subMetaApplierConfig.setCompressionType(compressionType[i]);
			// �Ƿ������ֶι���
			Boolean EnableColumnFilter = Boolean.valueOf(multiEnableColumnFilter[i]);

			subMetaApplierConfig.setEnableColumnFilter(EnableColumnFilter);

			CommonFilterConfig commonFilterConfig = new CommonFilterConfig();

			if (Boolean.valueOf(multiEnableColumnFilter[i])) {
				if (Boolean.valueOf(multiColumnFilterAdvEnabled[i])) {
					commonFilterConfig.setColumnFilterDynaCode(multiDynaCode[i]);
				} else {
					commonFilterConfig.setColumnFilterCondition(columnFilterCondition[i]);
					// �����Ƿ�ʹ��include
					commonFilterConfig.setColumnFilterUseInclude(Boolean.valueOf(columnFilterUseInclude[i]));
				}
			}

			commonFilterConfig.setEnableColumnFilter(Boolean.valueOf(multiEnableColumnFilter[i]));
			commonFilterConfig.setEnableColumnFilterAdv(Boolean.valueOf(multiColumnFilterAdvEnabled[i]));

			commonFilterConfig.setIncludeInsert(Boolean.valueOf(insertAction[i]));
			commonFilterConfig.setIncludeUpdate(Boolean.valueOf(updateAction[i]));
			commonFilterConfig.setIncludeDelete(Boolean.valueOf(deleteAction[i]));

			commonFilterConfig.setSrcSchemaReg(srcSchemaReg[i]);
			commonFilterConfig.setSrcTableReg(srcTableReg[i]);

			commonFilterConfig.setColumnFilterUseInclude(Boolean.valueOf(columnFilterUseInclude[i]));

			subMetaApplierConfig.setCommonFilterConfig(commonFilterConfig);

			metaApplierConfigs.add(subMetaApplierConfig);
		}

		MultiMetaApplierConfig multiMetaApplierConfig = new MultiMetaApplierConfig();
		multiMetaApplierConfig.setMetaApplierConfigs(metaApplierConfigs);

		multiMetaApplierConfig.getApplierNode();
		syncTaskNode.setApplierData(multiMetaApplierConfig.getApplierNode().toJSONString());
	}

	public static boolean isChecked(String check) {
		return "on".equalsIgnoreCase(check);
	}

}
