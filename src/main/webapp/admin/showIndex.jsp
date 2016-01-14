<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%><%String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
pageContext.setAttribute("basePath",basePath);
pageContext.setAttribute("baseSPath",request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+"/");
pageContext.setAttribute("proName","qihaoyuan");
%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<title>IndexMgr - 索引库管理</title>
		<meta http-equiv="keywords" content="创建索引">
		<meta http-equiv="description" content="this is my page">
		<meta http-equiv="content-type" content="text/html; charset=UTF-8">
		<jsp:include page="../include/admin/base.jsp"></jsp:include>
	</head>
	<body>
<div class="box clearfix">
	<div class="ptop">
		<jsp:include page="../include/admin/ptop.jsp"></jsp:include>
	</div>
	<!-- end ptop -->
	<div class="pmain clearfix">
		<div class="mleft">
			<jsp:include page="../include/admin/mleft.jsp"></jsp:include>
		</div>
		<!-- end mleft -->
		<div class="main">
			<jsp:include page="../include/admin/main_showIndex.jsp"></jsp:include>
		</div>
		<!-- end main -->
	</div>
	<!-- end pmain -->
	<div class="pfoot">
		<jsp:include page="../include/admin/pfoot.jsp"></jsp:include>
	</div>
</div>
</pre>
</body>
</html>
