package com.taobao.jingwei.webconsole.model.config;

/**
 * @desc 
 * 
 * @author Ë·º£ <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Jan 8, 2013 12:23:37 PM
 */

public interface JingweiBatch {
	public enum BatchPolicy {
		DEFAULT_POLICY(1);

		private int ploicy;

		BatchPolicy(int ploicy) {
			this.ploicy = ploicy;
		}

		public int getPlicy() {
			return ploicy;
		}

		public static BatchPolicy getEnumByPolicy(int ploicy) {
			BatchPolicy batchPolicy = null;
			for (BatchPolicy et : BatchPolicy.values()) {
				if (et.getPlicy() == ploicy) {
					batchPolicy = et;
					break;
				}
			}
			return batchPolicy;
		}
	}
}
