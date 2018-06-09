package com.taobao.jingwei.monitor.lb;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * ����һ���Թ�ϣ�ĸ��ؾ�����ԣ�</br>
 * <ul>
 * <li>������consumer��֯��һ����</li>
 * <li>�����з�������hashֵ���뵽����</li>
 * <li>��ȡָ��consumerǰ�棬ǰһ��consumer֮��ķ����б���Ϊ���</li>
 * </ul>
 * 
 * @author boyan(boyan@taobao.com)
 * @author shuohailhl
 * @date 2011-11-29
 * 
 */
public class ConsisHashStrategy implements LoadBalanceStrategy {
	// ����ڵ���Ŀ
	private static final int NUM_REPS = 160;

	/** hashֵ��ʵ�ʽڵ��ӳ���ϵ,����������б�仯-->consumerMap��֮�仯-->�������ҲҪ�仯 */
	private final TreeMap<Long, String> consumerMap;

	/** �������б� */
	@SuppressWarnings("unused")
	private final Collection<String> curConsumers;

	public ConsisHashStrategy(final Collection<String> curConsumers) {
		this.curConsumers = curConsumers;
		this.consumerMap = this.buildConsumerMap(curConsumers);

	}

	/**
	 * Get the md5 of the given key.
	 */
	public byte[] computeMd5(final String k) {
		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (final NoSuchAlgorithmException e) {
			throw new RuntimeException("MD5 not supported", e);
		}
		md5.reset();
		md5.update(ByteUtils.getBytes(k));
		return md5.digest();
	}

	@Override
	public List<String> getPartitions(final String consumerId, final List<String> curPartitions) {

		final Set<String> rt = new HashSet<String>();
		// ����partition���Ҷ�Ӧ��consumer
		for (final String partition : curPartitions) {
			final String targetConsumer = this.findConsumerByPartition(partition);
			// ���汾consumer��Ҫ���صķ���
			if (consumerId.equals(targetConsumer)) {
				rt.add(partition);
			}
		}
		return new ArrayList<String>(rt);
	}

	public String findConsumerByPartition(final String partition) {
		final Long hash = this.katamaHash(partition);
		Long target = hash;
		if (!consumerMap.containsKey(hash)) {
			target = consumerMap.ceilingKey(hash);
			if (target == null && !consumerMap.isEmpty()) {
				target = consumerMap.firstKey();
			}
		}
		final String targetConsumer = consumerMap.get(target);
		return targetConsumer;
	}

	private TreeMap<Long, String> buildConsumerMap(final Collection<String> curConsumers) {
		final TreeMap<Long/* hash */, String/* consumerId */> consumerMap = new TreeMap<Long, String>();
		for (final String consumer : curConsumers) {
			for (int i = 0; i < NUM_REPS / 4; i++) {
				final byte[] digest = this.computeMd5(consumer + "-" + i);
				for (int h = 0; h < 4; h++) {
					final long k = (long) (digest[3 + h * 4] & 0xFF) << 24 | (long) (digest[2 + h * 4] & 0xFF) << 16
							| (long) (digest[1 + h * 4] & 0xFF) << 8 | digest[h * 4] & 0xFF;
					consumerMap.put(k, consumer);
				}
			}
		}
		return consumerMap;
	}

	private Long katamaHash(String k) {

		final byte[] bKey = this.computeMd5(k);
		long rv = (long) (bKey[3] & 0xFF) << 24 | (long) (bKey[2] & 0xFF) << 16 | (long) (bKey[1] & 0xFF) << 8
				| bKey[0] & 0xFF;

		return rv;
	}
}
