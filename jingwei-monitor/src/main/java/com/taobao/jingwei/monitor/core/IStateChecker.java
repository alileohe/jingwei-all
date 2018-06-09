package com.taobao.jingwei.monitor.core;

import java.util.List;

/**
 * 校验任务运行状态
 * @author shuohailhl
 *
 */
public interface IStateChecker {

	boolean check(String hostName, List<String> hostNames);
}
