function warnMessage(obj) {
	if (!obj.checked) {
		alert("如果关闭该选项则每次启动都从最新的位点开始可能造成数据丢失！");
	}
}

function chgCheckBoxValue(obj) {
	var v = document.getElementById(obj.id + "_value");
	v.value = obj.checked;
}

function chgSingleInstance(obj) {
	document.getElementById("singleInstanceDiv").style.display = obj.checked ? "none"
			: "";
}

function chgGlobalFilterMode(obj) {
	chgCheckBoxValue(obj);
	document.getElementById("appGlobalColumnFilterSim").style.display = obj.checked ? "none"
			: "";
	document.getElementById("appGlobalColumnFilterAdv").style.display = obj.checked ? ""
			: "none";
}

function closeWindow() {
	window.opener = null;
	window.open('', '_self');
	window.close();
}

function chgBinlogMode(obj) {
	var auto = document.getElementById("autoSwitch_box");
	document.getElementById("complexity").value = obj.checked;

	if (auto.checked) {
		document.getElementById("extractorData").innerHTML = obj.checked ? document
				.getElementById("extractor_binlog_com_auto").innerHTML
				: document.getElementById("extractor_binlog_auto").innerHTML;
	} else {
		document.getElementById("extractorData").innerHTML = obj.checked ? document
				.getElementById("extractor_binlog_com").innerHTML
				: document.getElementById("extractor_binlog").innerHTML;
	}
}

function chgBinlogAutoSwitch(obj) {
	var adMode = document.getElementById("complexity_box");
	document.getElementById("autoSwitch").value = obj.checked;
	if (adMode.checked) {
		document.getElementById("extractorData").innerHTML = obj.checked ? document
				.getElementById("extractor_binlog_com_auto").innerHTML
				: document.getElementById("extractor_binlog_com").innerHTML;
	} else {
		document.getElementById("extractorData").innerHTML = obj.checked ? document
				.getElementById("extractor_binlog_auto").innerHTML
				: document.getElementById("extractor_binlog").innerHTML;
	}
}

function chgApplierFilter(obj) {
	document.getElementById("appGolbalFilter_div").style.display = obj.checked ? ""
			: "none";
	document.getElementById(obj.id + "_value").value = obj.checked;
}

function sbForm() {
	var applier = document.getElementById("applier").selectedIndex + 1;
	if (applier == 1) {
		// 拼装DATABASE_APPLIER
		var tab = document.getElementById("applier_db_table");
		var tableMapping = "";
		var columnMapping = "";
		var ignoreTableList = "";
		for ( var i = 4; i < tab.rows.length - 1; i++) {
			var tr = tab.rows[i];
			var index = tr.id.split("_")[1];
			var tm = document.getElementById("orgTable_" + index).value
					+ " -> "
					+ (document.getElementById("desTable_" + index).value == "" ? document
							.getElementById("orgTable_" + index).value
							: document.getElementById("desTable_" + index).value)
			tableMapping += "|" + tm;
			var cm = getAll(document.getElementById("columnMapping_" + index));
			columnMapping += "|" + cm;
			var itl = getAll(document
					.getElementById("ignoreTableList_" + index));
			var colFilter = document.getElementById("filterFlag_" + index);
			ignoreTableList += "|" + itl;
			if (itl != null && itl != "") {
				ignoreTableList += "#"
						+ colFilter.options[colFilter.selectedIndex].value;
			}
		}

		document.getElementById("tableMapping").value = tableMapping == "" ? ""
				: tableMapping.substring(1);
		document.getElementById("columnMapping").value = columnMapping == "" ? ""
				: columnMapping.substring(1);
		document.getElementById("ignoreTableList").value = ignoreTableList == "" ? ""
				: ignoreTableList.substring(1);
	} else if (applier == 2) {
		// 拼装MULTI_META_APPLIER字段过滤的值
		document.getElementById("column_filter_value_-1").value = getAll(document
				.getElementById("column_filter_-1"));
	} else if (applier == 3) {
		// 拼装MULTI_META_APPLIER字段过滤的值
		var filterTab = document.getElementById("multi_meta_table");
		for ( var i = 0; i < filterTab.rows.length - 1; i++) {
			var tr = filterTab.rows[i];
			var index = tr.id.split("_")[1];
			document.getElementById("column_filter_value_" + index).value = getAll(document
					.getElementById("column_filter_" + index));
		}
	} else if (applier == 5) {
		// 拼装ANOR_COMMAND_APPLIER字段过滤的值
		// 拼装结果为：t1|i1 -> c1,c2;i2 -> c1,c2?t2|i1 -> c1,c2;i2 -> c1......
		var filterTab = document.getElementById("applier_andorCommand_table");
		var filterTabValue = "";
		for ( var i = 1; i < filterTab.rows.length - 1; i++) {
			var tr = filterTab.rows[i];
			var index = tr.id.split("_")[1];
			filterTabValue += "?"
					+ document.getElementById("andorTableName_" + index).value
					+ "|"
					+ getAll(document.getElementById("indexMaping_" + index));
		}
		document.getElementById("andorTableMapping").value = filterTabValue == "" ? ""
				: filterTabValue.substring(1);
	}

	var appGolbalApplierCheckBox = document
			.getElementById("enableApplierFilterController");
	if (appGolbalApplierCheckBox.checked) {
		var filterString = getAll(document.getElementById("column_filter_-2"));

		document.getElementById("column_filter_value_-2").value = filterString == "" ? ""
				: filterString;
	}

	return true;
}

function getAll(selector) {
	var value = "";
	for ( var i = 0; i < selector.options.length; i++) {
		value += ";" + selector.options[i].value;
	}
	return value == "" ? "" : value.substring(1);
}

function isItemExist(selector, item) {
	var exist = false;
	for ( var i = 0; i < selector.options.length; i++) {
		if (selector.options[i].text == item) {
			exist = true;
			break;
		}
	}
	return exist;
}

function addItem(selector, item, value) {
	var succ = false;
	if (!isItemExist(selector, item)) {
		var op = new Option(item, value);
		selector.options.add(op);
		succ = true;
	}
	return succ;
}

function removeItem(selector) {
	for ( var i = selector.options.length - 1; i >= 0; i--) {
		if (selector.options[i].selected) {
			selector.remove(i);
		}
	}
}

function addAndorTableMappingDiv() {

	var tab = document.getElementById("applier_andorCommand_table");
	var tr = tab.insertRow(tab.rows.length - 1);
	var num = document.getElementById("andorNumber").value;
	var row = num == "" ? 0 : parseInt(num);
	row++;
	document.getElementById("andorNumber").value = row;
	tr.id = "andorTr_" + row;
	var td = tr.insertCell(0);
	td.colSpan = 4;

	var ht = "<table style='border: 1px solid #fff' onmouseover='this.style.border=\"1px solid #4f6b72\"' onmouseout='this.style.border=\"1px solid #fff\"'>";

	ht += "<th colspan='3'>添加过滤表</th>";
	ht += "<th style='text-align:right'><a href='javascript:void(0)' onclick='delAnorTableMappingDiv(\""
			+ row + "\")'>X</a></th>";
	ht += "</tr>";

	ht += "<tr><td >表名</td><td ><input id='andorTableName_" + row
			+ "'/></td></tr>";
	ht += "<tr><td >indexName</td><td ><input id='andorIndexName_" + row
			+ "'/></td>";
	ht += "<td >columnNameList</td ><td ><input id='andorColumnList_" + row
			+ "'/></td></tr>";
	ht += "<tr><td >&nbsp;&nbsp;&nbsp;&nbsp;<input type='button' value='添加' onclick='addAndorIndexMapping("
			+ row + ")' /></td>";
	ht += "<td><input type='button' value='删除' onclick='delAndorIndexMapping("
			+ row + ")' /></td></tr>";
	ht += "<tr>"
	ht += "<td rowspan='2' colspan='2' ><select multiple style='width:300px' id='indexMaping_"
			+ row + "' ></select></td>";
	ht += "</tr>";
	ht += "</table>";

	td.innerHTML = ht;

}

function addTableMappingDiv() {
	var tab = document.getElementById("applier_db_table");
	var tr = tab.insertRow(tab.rows.length - 1);
	var num = document.getElementById("number").value;
	var row = num == "" ? 0 : parseInt(num);
	row++;
	document.getElementById("number").value = row;
	tr.id = "tr_" + row;
	var td = tr.insertCell(0);
	td.colSpan = 4;

	var ht = "<table style='border: 1px solid #fff' onmouseover='this.style.border=\"1px solid #4f6b72\"' onmouseout='this.style.border=\"1px solid #fff\"'>";
	ht += "<tr>";
	ht += "<th colspan='3'>表映射</th>";
	ht += "<th style='text-align:right'><a href='javascript:void(0)' onclick='delTableMappingDiv(\""
			+ row + "\")'>X</a></th>";
	ht += "</tr>";
	ht += "<tr>";
	ht += "<td>源表名</td>";
	ht += "<td><input type='text' id='orgTable_" + row + "' /></td>";
	ht += "<td>目标表名</td>";
	ht += "<td><input type='text' id='desTable_" + row + "' /></td>";
	ht += "</tr>";
	ht += "<tr>";
	ht += "<td>源列名</td>";
	ht += "<td><input type='text' id='orgColumn_" + row + "' /></td>";
	ht += "<td><input type='button' value='添加' onclick='addColumnMapping("
			+ row + ")' /></td>";
	ht += "<td rowspan='2'>";
	ht += "<select multiple style='width:170px' id='columnMapping_" + row
			+ "'>";
	ht += "</select>";
	ht += "</td>";
	ht += "</tr>";
	ht += "<tr>";
	ht += "<td>目标列名</td>";
	ht += "<td><input type='text' id='desColumn_" + row + "' /></td>";
	ht += "<td><input type='button' value='删除' onclick='delColumnMapping("
			+ row + ")' /></td>";
	ht += "</tr>";
	ht += "<tr>";
	ht += "<td>列过滤</td>";
	ht += "<td><input type='text' id='ignoreColumn_" + row + "' />";
	ht += "<select id='filterFlag_" + row + "' style='width:60px'>";
	ht += "<option value='true'>包含</option>";
	ht += "<option value='false'>排除</option>";
	ht += "</select>";
	ht += "</td>";
	ht += "<td><p><input type='button' value='添加' onclick='addIgnoreTable("
			+ row
			+ ")' /></p><p><input type='button' value='删除' onclick='delIgnoreTable("
			+ row + ")' /></p></td>";
	ht += "<td>";
	ht += "<select multiple style='width:170px' id='ignoreTableList_" + row
			+ "'>";
	ht += "</select>";
	ht += "</td>";
	ht += "</tr>";
	ht += "</table>";

	td.innerHTML = ht;
}

function addGroupEntryDiv() {
	var tab = document.getElementById("mutil_group_table");
	var tr = tab.insertRow(tab.rows.length - 1);
	var num = document.getElementById("mutil_group_number").value;

	var row = num == "" ? 0 : parseInt(num);
	row++;
	document.getElementById("mutil_group_number").value = row;
	tr.id = "tr_" + row;
	var td = tr.insertCell(0);
	td.colSpan = 6;

	var ht = "<table style='border: 1px solid #fff' onmouseover='this.style.border=\"1px solid #4f6b72\"' onmouseout='this.style.border=\"1px solid #fff\"'>";
	ht += "<tr>";
	ht += "<th colspan='5'>grou setting (多个字段可以使用逗号分隔)</th>";
	ht += "<th style='text-align:right'><a href='javascript:void(0)' onclick='delGroupEntryDiv(\""
			+ row + "\")'>X</a></th>";
	ht += "</tr>";
	ht += "<tr>";
	ht += "<td>库名reg</td>";
	ht += "<td><input type='text' id='schemaReg_" + row + "' name='schemaReg' "
			+ " /></td>";
	ht += "<td>表名reg</td>";
	ht += "<td><input type='text' id='tableReg_" + row + "' name='tableReg' "
			+ " /></td>";
	ht += "<td>字段</td>";
	ht += "<td><input type='text' id='fields_" + row + "' name='fields' "
			+ " /></td>";
	ht += "</tr>";
	ht += "<tr>";
	ht += "</tr>";
	ht += "</table>";

	td.innerHTML = ht;
}

function delGroupEntryDiv(row) {
	var tab = document.getElementById("mutil_group_table");
	var tr = document.getElementById("tr_" + row);
	tab.deleteRow(tr.rowIndex);
}

function delTableMappingDiv(row) {
	var tab = document.getElementById("applier_db_table");
	var tr = document.getElementById("tr_" + row);
	tab.deleteRow(tr.rowIndex);
}

function delAnorTableMappingDiv(row) {
	var tab = document.getElementById("applier_andorCommand_table");
	var tr = document.getElementById("andorTr_" + row);
	tab.deleteRow(tr.rowIndex);
}

function addColumnMapping(row) {
	var usableTable = document.getElementById("orgTable_" + row);
	var orgColumn = document.getElementById("orgColumn_" + row);
	var desColumn = document.getElementById("desColumn_" + row);
	if (usableTable.value == "") {
		alert("请填写源表名！");
	} else if (orgColumn.value == "") {
		alert("请填写源列名！");
	} else {
		var selector = document.getElementById("columnMapping_" + row);
		var item = orgColumn.value;
		item += " -> "
				+ (desColumn.value == "" ? orgColumn.value : desColumn.value);
		addItem(selector, usableTable.value + "." + item, usableTable.value
				+ "." + item);
	}
}

function delColumnMapping(row) {
	removeItem(document.getElementById('columnMapping_' + row));
}

function addAndorIndexMapping(row) {
	var usableIndex = document.getElementById("andorIndexName_" + row);
	// column 以,为分割
	var orgColumnList = document.getElementById("andorColumnList_" + row);
	if (usableIndex.value == "") {
		alert("请填写indexName！");
	} else {
		var selector = document.getElementById("indexMaping_" + row);
		var item = usableIndex.value;
		item += " -> " + (orgColumnList.value == "" ? "" : orgColumnList.value);
		addItem(selector, item, item);
	}
}

function delAndorIndexMapping(row) {
	removeItem(document.getElementById('indexMaping_' + row));
}

function addIgnoreTable(row) {
	var usableTable = document.getElementById("orgTable_" + row);
	var col = document.getElementById("ignoreColumn_" + row);
	if (usableTable.value == "") {
		alert("请填写源表名！");
	} else if (col.value == "") {
		alert("请填写需要忽略的列名");
	} else {
		var ls = document.getElementById("ignoreTableList_" + row);
		addItem(ls, usableTable.value + "." + col.value, usableTable.value
				+ "." + col.value);
	}
}

function delIgnoreTable(row) {
	removeItem(document.getElementById("ignoreTableList_" + row));
}

function chgExtractorType(extractor) {
	var selected = extractor.options[extractor.selectedIndex].value;
	
	var extractorDataRow = document.getElementById(extractor.id + "_row");
	if (selected == 1/* BINLOG_EXTRACTOR */) {
		extractorDataRow.style.display = "";
		document.getElementById("admode").style.display = "";
		if (document.getElementById("autoSwitch_box").checked) {
			document.getElementById("extractorData").innerHTML = document
					.getElementById("extractor_binlog_auto").innerHTML;
		} else {
			document.getElementById("extractorData").innerHTML = document
					.getElementById("extractor_binlog").innerHTML;
		}
	} else if (selected == 2/* META_EXTRACTOR */) {
		extractorDataRow.style.display = "";
		document.getElementById("admode").style.display = "none";
		document.getElementById("extractorData").innerHTML = document
				.getElementById("extractor_meta").innerHTML;
	} else if (selected == 4/* ORACLE_EXTRACTOR */||selected == 5/* DRC_EXTRACTOR */) {
		extractorDataRow.style.display = "";
		document.getElementById("admode").style.display = "none";
		document.getElementById("extractorData").innerHTML = document.getElementById("extractor_prop_ad").innerHTML;
	} else {
		extractorDataRow.style.display = "none";
		document.getElementById("admode").style.display = "none";
	}
}

function isNumber(obj) {
	// 先把非数字的都替换掉，除了数字和.
	obj.value = obj.value.replace(/[^\d.]/g, "");
	// 必须保证第一个为数字而不是.
	obj.value = obj.value.replace(/^\./g, "");
	// 保证只有出现一个.而没有多个.
	obj.value = obj.value.replace(/\.{2,}/g, ".");
	// 保证.只出现一次，而不能出现两次以上
	obj.value = obj.value.replace(".", "$#$").replace(/\./g, "").replace("$#$",
			".");
}

function chgApplierType(applier) {
	var selected = applier.options[applier.selectedIndex].value;
	var content = document.getElementById("applierData");
	if (selected == 1) {
		document.getElementById(applier.id + "_row").style.display = "";
		content.innerHTML = document.getElementById("applier_database").innerHTML;
	} else if (selected == 2) {
		document.getElementById(applier.id + "_row").style.display = "";
		content.innerHTML = document.getElementById("applier_meta").innerHTML;
	} else if (selected == 4) {
		document.getElementById(applier.id + "_row").style.display = "";
		content.innerHTML = document.getElementById("multi_applier_meta").innerHTML;
	} else if (selected == 5) {
		document.getElementById(applier.id + "_row").style.display = "";
		content.innerHTML = document.getElementById("applier_andorCommand").innerHTML;
	} else {
		document.getElementById(applier.id + "_row").style.display = "none";
	}
}

// multi
function chgControls(e) {
	document.getElementById("thread_control").style.display = e.checked ? ""
			: "none";
	document.getElementById("thread_control_group").style.display = e.checked ? ""
			: "none";
}
function chgMetaFilter(e) {
	document.getElementById(e.id + "_value").value = e.checked;
	document.getElementById(e.id + "_div").style.display = e.checked ? ""
			: "none";
}
function chgMultiMetaFilter(e) {
	var v = document.getElementById(e.id + "_value");
	v.value = e.checked;
	document.getElementById(e.id + "_mode_sim").style.display = e.checked ? ""
			: "none";
	document.getElementById(e.id + "_mode_div").style.display = e.checked ? ""
			: "none";
	document.getElementById(e.id + "_mode").checked = false;
	document.getElementById(e.id + "_mode_value").value = "false";
	document.getElementById(e.id + "_mode_adv").style.display = "none";
}

function chgMultiMetaFilterMode(e) {
	chgCheckBoxValue(e);
	document.getElementById(e.id + "_sim").style.display = e.checked ? "none"
			: "";
	document.getElementById(e.id + "_adv").style.display = e.checked ? ""
			: "none";
}

function delMultiMetaApplierDiv(row) {
	var tab = document.getElementById("multi_meta_table");
	var tr = document.getElementById("multiFilterTr_" + row);
	tab.deleteRow(tr.rowIndex);
}
function addMultiMeta() {
	var tab = document.getElementById("multi_meta_table");
	var tr = tab.insertRow(tab.rows.length - 1);
	var num = document.getElementById("multi_meta_number").value;
	var row = num == "" ? 0 : parseInt(num);

	row++;
	document.getElementById("multi_meta_number").value = row;
	tr.id = "multiFilterTr_" + row;

	var td = tr.insertCell(0);
	td.colSpan = 4;

	var ht = "";
	ht += "<table style='border:1px solid #fff' onmouseover='this.style.border=\"1px solid #4f6b72\"' onmouseout='this.style.border=\"1px solid #fff\"'>";
	ht += "<tr>";
	ht += "<th colspan='3'>META Applier</th>";
	ht += "<th style='text-align:right'><a href='javascript:void(0)' onclick='delMultiMetaApplierDiv("
			+ row + ")'>X</a></th>";
	ht += "</tr>";
	ht += "<tr>";
	ht += "<td>主题</td><td><input type='text' name='multiMetaTopic' /></td>";
	ht += "<td>分库键</td><td><input type='text' name='multiShardColumn' /></td>";
	ht += "</tr>";
	ht += "<tr>";
	ht += "<td>库名表达式</td>";
	ht += "<td><input type='text' name='multiSrcSchemaReg' /></td>";
	ht += "<td>表名表达式</td>";
	ht += "<td><input type='text' name='multiSrcTableReg' /></td>";
	ht += "</tr>";
	ht += "<tr>";
	ht += "<td>发送超时时间</td>";
	ht += "<td><input type='text' name='multiSendTimeOut' value='3000' /></td>";
	ht += "<td>压缩类型</td>";
	ht += "<td>";
	ht += document.getElementById("compTypeOptions").innerHTML;
	ht += "</td>";
	ht += "</tr>";
	ht += "<tr>";
	ht += "<td>是否拆分事务</td><td><input type='checkbox' id='multiSplitTxEvent_"
			+ row
			+ "' onclick='chgCheckBoxValue(this)' /><input type='hidden' id='multiSplitTxEvent_"
			+ row + "_value' name='multiSplitTxEvent' value='false' /></td>";
	ht += "<td>最大事件长度</td><td><input type='text' name='multiMaxEventSize' value='4096' /></td>";
	ht += "</tr>";
	ht += "<tr>";
	ht += "<td>Action过滤</td>";
	ht += "<td colspan='3'>";
	ht += "<label><input type='checkbox' id='multiInsert_" + row
			+ "' onclick='chgCheckBoxValue(this)' checked />INSERT</label>";
	ht += "<input type='hidden' id='multiInsert_" + row
			+ "_value' name='multiInsert' value='true' />";
	ht += "<label><input type='checkbox' id='multiUpdate_" + row
			+ "' onclick='chgCheckBoxValue(this)' checked />UPDATE</label>";
	ht += "<input type='hidden' id='multiUpdate_" + row
			+ "_value' name='multiUpdate' value='true' />";
	ht += "<label><input type='checkbox' id='multiDelete_" + row
			+ "' onclick='chgCheckBoxValue(this)' checked />DELETE</label>";
	ht += "<input type='hidden' id='multiDelete_" + row
			+ "_value' name='multiDelete' value='true' />";
	ht += "</td>";
	ht += "</tr>";
	ht += "<tr>";
	ht += "<td>启动字段过滤</td>";
	ht += "<td colspan='3'><input id='multiMetaFilterRadio_"
			+ row
			+ "' type='checkbox' onclick='chgMultiMetaFilter(this)' /><input type='hidden' id='multiMetaFilterRadio_"
			+ row + "_value' name='multiEnableColumnFilter' value='false' />";
	ht += "<span id='multiMetaFilterRadio_" + row
			+ "_mode_div' style='display:none'>高级模式";
	ht += "<input id='multiMetaFilterRadio_"
			+ row
			+ "_mode' type='checkbox' onclick='chgMultiMetaFilterMode(this)' />";
	ht += "<input type='hidden' id='multiMetaFilterRadio_"
			+ row
			+ "_mode_value' name='multiColumnFilterAdvEnabled' value='false' /></span>";
	ht += "</td>";
	ht += "</tr>";
	ht += "<tr id='multiMetaFilterRadio_" + row
			+ "_mode_sim' style='display:none'>";
	ht += "<td colspan='4'>";
	ht += "<table>";
	ht += "<tr>";
	ht += "<th colspan='4'>字段过滤</th>";
	ht += "</tr>";
	ht += "<tr>";
	ht += "<td>库名过滤条件</td>";
	ht += "<td><input type='text' id='schema_" + row + "' /></td>";
	ht += "<td rowspan='3'><select id='column_filter_condition_"
			+ row
			+ "' style='width:50px'><option value='include'>包含</option><option value='exclude'>排出</option></select><p></p><input type='button' value='添加' onclick='addColumnFilter("
			+ row
			+ ")' /><p></p><input type='button' value='刪除' onclick='delColumnFilter("
			+ row + ")' /></td>";
	ht += "<td rowspan='3'>";
	ht += "<select id='column_filter_" + row
			+ "' multiple style='width:180px;height:120px'></select>";
	ht += "<input type='hidden' name='multiColumnFilterString' id='column_filter_value_"
			+ row + "' value='' />";
	ht += "</td>";
	ht += "</tr>";
	ht += "<tr>";
	ht += "<td>逻辑表名</td>";
	ht += "<td><input type='text' id='table_" + row + "' /></td>";
	ht += "</tr>";
	ht += "<tr>";
	ht += "<td>字段名</td>";
	ht += "<td><input type='text' id='column_" + row + "' /></td>";
	ht += "</tr>";
	ht += "</table>";
	ht += "</td>";
	ht += "</tr>";
	ht += "<tr id='multiMetaFilterRadio_" + row
			+ "_mode_adv' style='display:none'>";
	ht += "<td colspan='4'>";
	ht += "<textarea name='multiColumnFilterAdv' cols='80' rows='12'></textarea>";
	ht += "</td>";
	ht += "</tr>";
	ht += "</table>";
	td.innerHTML = ht;
}

function addColumnFilter(index) {
	var schema = document.getElementById("schema_" + index).value;
	var table = document.getElementById("table_" + index).value;
	var column = document.getElementById("column_" + index).value;
	if (schema == "") {
		alert("请填写库名！");
		return;
	}
	if (table == "") {
		alert("请填写表名！");
		return;
	}
	if (column == "") {
		alert("请填写字段名！");
		return;
	}
	var condition = document.getElementById("column_filter_condition_" + index);
	condition.disabled = true;
	var cond = condition.value;
	var filter = document.getElementById("column_filter_" + index);
	addItem(filter, cond + ":" + schema + "." + table + "." + column, cond
			+ ":" + schema + "." + table + "." + column);
}

function delColumnFilter(index) {
	var filter = document.getElementById("column_filter_" + index);
	removeItem(filter);
	if (filter.options.length == 0) {
		var condition = document.getElementById("column_filter_condition_"
				+ index);
		condition.disabled = false;
	}
}
