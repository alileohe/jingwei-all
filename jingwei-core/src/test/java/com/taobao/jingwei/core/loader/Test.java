package com.taobao.jingwei.core.loader;


/**
 * @desc 
 * 
 * @author Ë·º£ <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Jul 13, 2012 5:39:06 PM
 */

public class Test {

	public static void main(String[] args) {
		byte b = (byte) 0xff;
		byte b1 = (byte) -1;
		byte b2 = (byte) 1;

		System.out.println(b1);     					// -1
		System.out.println(Integer.toHexString(b1));  	// ffffffff
		System.out.println(b);							// -1
		System.out.println(b1 & 0xff);					// 255
		System.out.println(b2 << 1);					// 2
		System.out.println(b2 << 8 );					// 256
		System.out.println(Long.toHexString(0x100000000L + 0xcafebabe));		// cafebabe
		System.out.println(Integer.toHexString(0xcafebabe));  					// cafebabe
		System.out.println(Long.toHexString(0xcafebabe));						// ffffffffcafebabe
		System.out.println(0xcafebabe);  										// -889275714
		System.out.println((long)0xcafebabe); 									// -889275714
		System.out.println(Long.toHexString((long)0xcafebabe)); 				// ffffffffcafebabe
		System.out.println(Long.toHexString(0x100000000L));						// 100000000
		
		System.out.println(Byte.toString((byte)0xff));			// -1
		System.out.println((byte)0xff + (byte)1);				// 0
	}
}
