<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%><%String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
pageContext.setAttribute("basePath",basePath);
pageContext.setAttribute("baseSPath",request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+"/");
pageContext.setAttribute("proName","qihaoyuan");
%>