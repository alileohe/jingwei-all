package com.taobao.jingwei.monitor.lb;

import com.taobao.util.RandomUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @desc 
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Jun 14, 2012 10:03:55 AM
 */

public class ConsisHashStrategyTest {

	private static String CONSUMER_1 = "asfg";
	private static String CONSUMER_2 = "dfgds";
	private static String CONSUMER_3 = "tirtyu";

	@Test
	public void test() {

		List<String> consumers = new ArrayList<String>();
		consumers.add(CONSUMER_1);
		consumers.add(CONSUMER_2);
		consumers.add(CONSUMER_3);

		ConsisHashStrategy consisHashStrategy = new ConsisHashStrategy(consumers);

		int a = 0;
		int b = 0;
		int c = 0;
		for (int i = 0; i < 1000; i++) {

			String partition = RandomUtil.getRandomString(20);
			String hitConsumer = consisHashStrategy.findConsumerByPartition(partition);

			if (hitConsumer.equals(CONSUMER_1)) {
				a++;
			} else if (hitConsumer.equals(CONSUMER_2)) {
				b++;
			} else if (hitConsumer.equals(CONSUMER_3)) {
				c++;
			}
		}

		System.out.println(a + "\t" + b + "\t" + c);

	}
}
