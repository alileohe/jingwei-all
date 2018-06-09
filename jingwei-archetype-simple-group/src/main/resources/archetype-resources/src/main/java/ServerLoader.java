package $package;

import com.taobao.jingwei.server.core.ServerTaskCore;
import com.taobao.jingwei.common.JingWeiUtil;

/**
*
<p>
description: ����̨��������, ����LOADER
Ҫ���ָ���������Ƶ���������������Ϣ���Ѿ��ڿ���̨���������
ZKʹ�õ�ǰ����������ZK������ZK���ֲ�������
<p>
*
* QuickLoader.java Create on Nov 6, 2012 4:56:03 PM
*
* Copyright (c) 2011 by qihao.
*
*@author
<a href="mailto:qihao@taobao.com">qihao</a>
*@version 1.0
*/
public class ServerLoader {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		String taskName = JingWeiUtil.getJingweiTaskName();

		//����jingweCore
		ServerTaskCore jingWeiCore = new ServerTaskCore();
		jingWeiCore.setTaskName(taskName);
		// �Զ����extractor��applier, �������ʹ���˾������õ�, �뽫��Ӧ�Ĵ���ע�͵�
        jingweiCore.setExtractor(new SimpleExtractor());
        jingWeiCore.setApplier(new SimpleApplier());
		jingWeiCore.init();
	}

}
