package com.taobao.jingwei.webconsole.model.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @desc 
 * 
 * @author Ë·º£ <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Jan 8, 2013 9:55:50 AM
 */

public class SequenceSuffixPolicy implements SuffixPolicy {

	private Integer startIndex;

	private Integer step;

	private Integer count;

	@Override
	public List<Integer> createSuffixes() {
		List<Integer> list = new ArrayList<Integer>();
		int cur = this.getStartIndex();

		for (int i = 0; i < this.getCount(); i++) {
			list.add(cur);
			cur += step;
		}
		return list;
	}

	public Integer getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(Integer startIndex) {
		this.startIndex = startIndex;
	}

	public Integer getStep() {
		return step;
	}

	public void setStep(Integer step) {
		this.step = step;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

}
