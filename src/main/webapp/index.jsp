<%@ page language="java" import="java.util.*,java.io.*"
	pageEncoding="UTF-8"%><%@include file="/include/base.jsp"%>
<!DOCTYPE HTML>
<html>
	<body>
		<h2>
			Hello World!
		</h2>
	</body>
</html>
<ul>
	<li>
		<a target="_blank" href="${basePath}admin/admin.jsp">admin</a>
	</li>
	<li>
		<a target="_blank" href="${basePath}weixin_imgs/index.html">weixin_imgs</a>
	</li>
</ul>

<%
	Object obj = new File(this.getServletContext().getRealPath("")).getParentFile().getAbsolutePath();
	out.print(obj);
%>