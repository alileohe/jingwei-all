package $package;

import com.taobao.jingwei.server.core.ServerTaskCore;

/**
*
<p>
description: ����̨���������������LOADER
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
public class QuickLoader {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// ���ص���ʹ��
        // String taskName = ${artifactId}

		String taskName = JingWeiUtil.getJingweiTaskName();

		//����jingweCore
		ServerTaskCore jingWeiCore = new ServerTaskCore();
		jingWeiCore.setTaskName(taskName);
		//���ʹ���Զ���APPLIER����Ҫ���ý�ȥ
		jingWeiCore.setApplier(new SimpleApplier());
		jingWeiCore.init();
	}

}
