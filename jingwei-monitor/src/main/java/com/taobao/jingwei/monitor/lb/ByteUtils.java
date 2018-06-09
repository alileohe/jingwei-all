package com.taobao.jingwei.monitor.lb;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * Utilities for byte process
 * 
 * @author shuohai.lhl
 * 
 */
public final class ByteUtils {

	public static final String DEFAULT_CHARSET_NAME = "utf-8";
	public static final Charset DEFAULT_CHARSET = Charset.forName(DEFAULT_CHARSET_NAME);

	private ByteUtils() {
	}

	public static final byte[] getBytes(final String k) {
		if (k == null || k.length() == 0) {
			return null;
		}
		try {
			return k.getBytes(DEFAULT_CHARSET_NAME);
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
