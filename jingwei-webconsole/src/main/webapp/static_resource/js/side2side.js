/*
  multiselect control
*/
(function($) {
	jQuery.fn.side2side = function (o) {

		o = jQuery.extend({
			maxResult: 500,
            url: '',
			confirmText: 'confirm',
			searchText: 'search',
			//callback: null,
			onConfirm: null,
			onClose: null,
		}, o);

		return this.each(function () {
		
		    var size = 10;
		    var nameDx = "nameDx";
		
		    // div id
		    var container_id = $(this).attr('id'); 
			var input_id = 'input_' + container_id;
			var confirm_btn_id = 'confirm_' + container_id;
			var left_select = 'left_sel_' + container_id;
			var right_select = 'right_sel_' + container_id;	 
		
           var htmlToAdd = 		
		    "<div style='margin-left:10px; margin-top:10px;'>" +
	    		o.searchText + "<input id='" + input_id + "'/>" +
	    	"</div>" +
			
	    	"<div class='side2side_sel'  >" +
	    		"<select id='" + left_select + "' size='" + size + "' multiple='multiple' style='width:250px'></select>" +
	    	"</div>" +
	    	
			/*
	    	"<div class='side2side_sel'  >" + 
	    	     "<div >" + 
	    	     "<input type='button' style='height:150px; display:none;' style='padding-top:150px;'/>" + 
	    	     "<input type='button' class='btn btn-primary btn-small' style='width:55px; margin-bottom:10px;'  value='全选>>' /> " + 
	    		 "<br> <input type='button' class='btn btn-primary btn-small' style='width:55px; margin-bottom:10px; ' value='全消<<' /> " + 
	    		 "<br> <input type='button' class='btn btn-primary btn-small' style='width:55px; margin-bottom:10px; ' value='选择>' /> " + 
	    		 "<br> <input  type='button' class='btn btn-primary btn-small' style='width:55px; margin-bottom:10px; ' value='取消<' /> " + 
	    		 "</div>" +
	    	"</div>" +
	    	*/
	    
	        "<div class='side2side_sel'>" +
	    		"<select id='" + right_select + "' size='" + size + "' multiple='multiple' style='width:250px'></select>" +
	    	"</div>" + 
	    	
	        "<div  >" +
	        	"<input name='aa' type='button'  id='" + confirm_btn_id + "' class='btn btn-primary btn-small' style='width:55px; margin-bottom:10px; margin-right:20px; float: right; ' value='" + o.confirmText +"' /> " + 
	        "</div>";
	    	
	    	$(this).append(htmlToAdd);
			
			var leftSel = $('#' + left_select);
			var rightSel = $('#' + right_select);
		    // DOUBLE CLICK ON LEFT SELECT OPTION
			leftSel.dblclick(function () {
				$(this).find("option:selected").each(function(i, selected){
					$(this).remove().appendTo(rightSel);
				});
				
				
				
				//$(this).trigger('change');
				rightSel.trigger('change');
			});
			
			// DOUBLE CLICK ON RIGHT SELECT OPTION
			rightSel.dblclick(function () {
				$(this).find("option:selected").each(function(i, selected){
					$(this).remove().appendTo(leftSel);
				});
				
				$(this).get(0).selectedIndex = 1;
				
				//$(this).trigger('change');
				leftSel.trigger('change');
			});		
			
			// right on change
			rightSel.change(function(){
				var t = '#' + rightSel.attr("id");
				
                $(t + " option").sort(function(a,b){
                    var aText = $(a).text();
                    var bText = $(b).text();
                    if(aText>bText) return 1;
                    if(aText<bText) return -1;
                    return 0;
                }).appendTo(rightSel) ;			    
			});
			
			// left on change
			leftSel.change(function(){
				var t = '#' + leftSel.attr("id");
				
                $(t + " option").sort(function(a,b){
                    var aText = $(a).text();
                    var bText = $(b).text();
                    if(aText>bText) return 1;
                    if(aText<bText) return -1;
                    return 0;
                }).appendTo(leftSel) ;
			});	
			
			//leftSel.click(function(){
			//    alert($(this).find("option:selected").text());
			//});
			
		    if (jQuery.isFunction(o.callback)) { 
               // alert("为什么没有执行啊"); 
              //  o.callback(); //问题出在这里
            }; 
			
			$("#" + confirm_btn_id).click(function() {
			    if (jQuery.isFunction(o.onConfirm)) { 
				    var data = "";
					$('#' + right_select + ' option').each(function() {
						data += $(this).val();
						data += ",";
					});
					
				    o.onConfirm(data);
						
				    closeDlg();
				}
			});
					
			$("#" + input_id).bind("click paste change keyup", function(){
			    //alert(1);
			    var element = this;
                setTimeout(function () {
                    var text = $(element).val();   
					if (text != "") {
					    request_tasks(text);
					}
                }, 50);
			});
			
			var request_tasks = function(text) {
				text = "*" + text + "*";
				var path = o.url;
     	        jQuery.ajax({
                    url: path,
                    type: "POST",
                    dataType:'json',
     	            data:{"resourceName" : text, 'maxResult' : o.maxResult},
                    success:function(json) {
                        var isSuccess = json.isSuccess;
     		            // alert(json.content.length);
					    reset_left(json.content);
                    },
                    error:function(XMLHttpRequest, textStatus, errorThrown) {
                       // alert(XMLHttpRequest.status);
					   //alert( XMLHttpRequest.responseText);
					   //alert(textStatus);
					   alert(errorThrown);
                    }
               });	 
			}
			
			// 清空左边的select
			var reset_left = function(data) {
			     //alert($('#'+left_select).attr("id"));
				  $('#'+left_select).empty();
			    for (var i = 0; i < data.length; i++) {
				 	
				    var exist = test(data[i]);
					//alert(exist);
					
					if (exist != true) {
					     $('#'+left_select).append('<option>' + data[i] + '</option>');
					}
			    }
			}
			
			
			function test(data){
                var success = false;
 		        var t = '#' + rightSel.attr("id");
		    
		        var  existOptText = "";
                existOptText = $(t + " option").each(function() {  
                    if ($(this).text() == data) { 
                        success = true;
                        return false;
                    } 
                });
                
				return success ; 
            } 
			
	        $(this).dialog({
		    	width: 570,
		    	modal: true,
				close: function() {
				    o.onClose();
				    dlg.dialog("close");
				},
		    });
			
			var dlg = $(this);
			var closeDlg = function(){
			    dlg.dialog("close");
			};
		});
	};
})(jQuery);