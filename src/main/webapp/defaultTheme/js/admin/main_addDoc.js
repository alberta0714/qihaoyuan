// need jquery
// 0-9:48-57 a-z:97-122 A-Z:65-90
var randomA = 65
var random1 = 1;
$(function(){
	$("#bn_addrow").click(function(){
		$("#fieldRowContent input[name=fdName]").attr("value",String.fromCharCode(randomA++));
		$("#fieldRowContent input[name=fdContent]").attr("value",(random1++) + getRandomLowerString(10));
		$("#fieldinfolist").append($("#fieldRowContent").html());
		addDelClick();
	});
	$("#bn_addrow").click();
	$("#bn_addrow").click();
	$("#bn_addrow").click();
	$("#bn_addrow").click();
	$("#bn_addrow").click();
	$("#bn_addrow").click();
	// $("#bn_addtoindex").click(function(){});
});

function addDelClick(){
	$("div[name=delRow]").click(function(){
		$(this).parent().remove();
	});
}

function getRandomNum(min ,max){
	return Math.floor(Math.random()*(max-min))+min+1;
}

function getRandomUpperString(len){
	var r = "";
	for(var i = 0; i < len; i++){
		r += String.fromCharCode(getRandomNum(65,90));
	}
	return r;
}

function getRandomLowerString(len){
	var r = "";
	for(var i = 0; i < len; i++){
		r += String.fromCharCode(getRandomNum(97,122));
	}
	return r;
}
