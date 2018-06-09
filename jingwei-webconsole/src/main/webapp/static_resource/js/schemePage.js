<script  text="javascript/text" >
	//跳转到页面
	function gotoPages(page){
		if(validate(page) ){
			document.forms['filterPageForm'].elements['pageNO'].value = page;
			document.forms['filterPageForm'].submit();
		}
	}
	function gotoPagesNow(page){
		gotoPages(page);
		return false;
	}
	function IsInteger(varString){
		return /^[0-9]+$/i.test(varString);
	}
	#if($search.currentPage)
		var current_page = ${search.currentPage};
	#else
		var current_page = 1;
	#end
	#if($search.totalPage)
		var max_page = ${search.totalPage};
	#else
		var max_page = 1;
	#end
	function validate(page){
		if(page == null || page == ""){
			alert("页号无效！");
			return false;
		}
		if(!IsInteger(page)){
			alert("页号必须为整数！");
			return false;
		}
		if(page == current_page){
			return false;
		}
		if(page > max_page){
			alert("页号超出最大页号！");
			return false;
		}
		if(page < 1){
			alert("页号超出最小页号！");
			return false;
		}
		return true;
	}
</script>