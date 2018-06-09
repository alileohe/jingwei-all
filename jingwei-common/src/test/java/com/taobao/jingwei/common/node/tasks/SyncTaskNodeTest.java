package com.taobao.jingwei.common.node.tasks;

import com.taobao.jingwei.common.node.tasks.SyncTaskNode.GroupingSetting;
import org.json.JSONException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @desc 
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Aug 24, 2012 9:18:15 AM
 */

public class SyncTaskNodeTest {

	// test extractor grouping setting
	@Test
	@Ignore
	public void testGrouping() {
		SyncTaskNode node = new SyncTaskNode();

		List<GroupingSetting> groupingSettings = new ArrayList<GroupingSetting>();
		List<String> fileds = new ArrayList<String>();
		fileds.add("field");

		for (int i = 0; i < 3; i++) {
			GroupingSetting setting = new GroupingSetting();
			
			setting.setSchemaReg("schema");
			setting.setTableReg("table");
			groupingSettings.add(setting);
		}

		node.setGroupingSettings(groupingSettings);
		SyncTaskNode other = new SyncTaskNode();

		try {
			String jsonStr = node.toJSONString();

			other.jsonStringToNodeSelf(jsonStr);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		assertEquals(other.getGroupingSettings().size(), 3);
		assertEquals(other.getGroupingSettings().get(0).getFields(), "field");
	}
}
