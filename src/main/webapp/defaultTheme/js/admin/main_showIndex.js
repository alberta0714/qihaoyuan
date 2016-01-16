// need jquery
// 0-9:48-57 a-z:97-122 A-Z:65-90
$(function(){
	var indexName = $.getUrlParam('indexName');
	
	var basePath = $("#basePath").html();
	var showIndexUrl = basePath + "actions/indexMgrAction?m=showIndexes&indexName=" + indexName;
	$("#info_1").html("loading ... <a target=_blank href=\""+showIndexUrl+"\">"+showIndexUrl+"</a>");
	$.post(
		showIndexUrl,
		{},
		function(data){
			$("#info_1").html("loading compeleted! <a target=_blank href=\""+showIndexUrl+"\">"+showIndexUrl+"</a>");
			if(data == null || data.error == true){
				$("#info_1").html("loading error! <a target=_blank href=\""+showIndexUrl+"\">"+showIndexUrl+"</a>");	
				return;	
			}
			var name = data.msgs.indexInfo.name;
			var indexPath = data.msgs.indexInfo.indexPath;
			var fileSize = data.msgs.indexInfo.fileSize;
			var maxDocNum = data.msgs.indexInfo.maxDocNum;

			var r = "";	
			r += "<ul class=\"clearfix\">";
			r += "	<li class=\"r1\">";
			r += name;
			r += "	</li>";
			r += "	<li class=\"r2\">";
			r += indexPath;
			r += "	</li>";;
			r += "	<li class=\"r3\">";
			r += maxDocNum;
			r += "	</li>";
			r += "	<li class=\"r4\">";
			r += "&nbsp;";
			r += "	</li>";
			r += "</ul>";
			$("div[name=indexInfo] ul:eq(0)").after(r);
		}
	);
	
	var showDocumentsUrl = basePath + "actions/indexMgrAction?m=showDocuments&indexName=" + indexName;
	$("#info_2").html("loading ... <a target=_blank href=\""+showDocumentsUrl+"\">"+showDocumentsUrl+"</a>");
	$.post(
		showDocumentsUrl,
		{},
		function(data){
			$("#info_2").html("loading compeleted! <a target=_blank href=\""+showDocumentsUrl+"\">"+showDocumentsUrl+"</a>");
			if(data == null || data.error == true){
				$("#info_2").html("loading error! <a target=_blank href=\""+showDocumentsUrl+"\">"+showDocumentUrl+"</a>");	
				return;	
			}
			var docs = data.msgs.docs;
			var r = "";
			for(var i = 0; i < docs.length; i++){
				var document = docs[i];
				var doc = document.doc;
				var fieldsInfos = document.fieldsInfos;
				// 显示字段名	
				r += "<ul class=\"clearfix fieldNames\" style=\"background:#f3f3f3;\">";
				r += "	<li style=\"width:30px;text-align:center; overflow:hidden;\">";
				r += "ID";
				r += "	</li>";
				
				for(var j = 0; j<fieldsInfos.length; j++){
					var info = fieldsInfos[j];
					r += "	<li style=\"width:60px;overflow:hidden;\">";
					r += info.name
					r += "	</li>";
				}
				r += "</ul>";
				// 显示字段内容
				r += "<ul class=\"clearfix fieldValues\">";
				r += "	<li style=\"min-width:24px;text-align:center;overflow:hidden;\" class=\"docId\">";
				r += doc;
				r += "	</li>";
				
				for(var j = 0; j<fieldsInfos.length; j++){
					var info = fieldsInfos[j];
					r += "	<li style=\"max-width:100px;overflow:hidden; height:24px;\">";
					r += info.stringValue
					r += "	</li>";
				}
				r += "</ul>";
			}
			// 字段名自动隐藏
			$("div[name=docList]").html(r);
			$(".fieldNames").css("cursor","pointer");
			$(".fieldNames").slideUp();
			$(".fieldNames").click(function(){
				$(this).slideToggle();
			});
			// 序号 对字段行的控制
			$(".docId").css("cursor","pointer");
			$(".docId").click(function(){
				$(this).parent().prev().slideToggle();
			});
		}
	);
	
	// $("#info_1").css("display","none");
});