package $package;

import com.taobao.jingwei.server.core.ServerTaskCore;
import com.taobao.jingwei.common.JingWeiUtil;

/**
*
<p>
description: 控制台配置任务, 启动LOADER
要求该指定任务名称的任务所有配置信息都已经在控制台上配置完毕
ZK使用当前环境精卫的ZK，所以ZK部分不用设置
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

		//创建jingweCore
		ServerTaskCore jingWeiCore = new ServerTaskCore();
		jingWeiCore.setTaskName(taskName);
		// 自定义的extractor和applier, 如果二者使用了精卫内置的, 请将对应的代码注释掉
        jingweiCore.setExtractor(new SimpleExtractor());
        jingWeiCore.setApplier(new SimpleApplier());
		jingWeiCore.init();
	}

}
