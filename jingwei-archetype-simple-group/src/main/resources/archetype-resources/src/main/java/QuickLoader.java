package $package;

import com.taobao.jingwei.server.core.ServerTaskCore;

/**
*
<p>
description: 控制台配置任务快速启动LOADER
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
public class QuickLoader {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// 本地调试使用
        // String taskName = ${artifactId}

		String taskName = JingWeiUtil.getJingweiTaskName();

		//创建jingweCore
		ServerTaskCore jingWeiCore = new ServerTaskCore();
		jingWeiCore.setTaskName(taskName);
		//如果使用自定义APPLIER这里要设置进去
		jingWeiCore.setApplier(new SimpleApplier());
		jingWeiCore.init();
	}

}
