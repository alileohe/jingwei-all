package com.taobao.jingwei.common.node.extractor;

import com.taobao.jingwei.common.node.AbstractNode;

/**
 * Class AbstractApplierNode
 *
 * @author qihao <dqylyln@gmail.com>
 * @since 11-11-15
 */
public abstract class AbstractExtractorNode extends AbstractNode {
	
	private String extractorData;

    @Override
    public boolean isPersistent() {
        return true;
    }

    @Override
    public String getDataIdOrNodePath() {
        //NO-OP: Extractor无需实现该方法
        return null;
    }

	public String getExtractorData() {
		return extractorData;
	}

	public void setExtractorData(String extractorData) {
		this.extractorData = extractorData;
	}
}
