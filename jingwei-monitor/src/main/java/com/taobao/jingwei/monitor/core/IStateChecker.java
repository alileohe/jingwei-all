package com.taobao.jingwei.monitor.core;

import java.util.List;

/**
 * У����������״̬
 * @author shuohailhl
 *
 */
public interface IStateChecker {

	boolean check(String hostName, List<String> hostNames);
}
