package com.taobao.jingwei.common.timer;

import java.util.concurrent.TimeUnit;

/**
 * A task which is executed after the delay specified with
 * {@link Timer#newTimeout(TimerTask, long, TimeUnit)}.
 * 
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 */
public interface TimerTask {

	/**
	 * Executed after the delay specified with
	 * {@link Timer#newTimeout(TimerTask, long, TimeUnit)}.
	 * 
	 * @param timeout
	 *            a handle which is associated with this task
	 */
	void run(Timeout timeout) throws Exception;
}