package com.taobao.jingwei.common.timer;

import java.util.Iterator;

/**
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 */
public interface ReusableIterator<E> extends Iterator<E> {
	void rewind();
}