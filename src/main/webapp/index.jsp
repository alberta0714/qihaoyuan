<%@ page language="java" import="java.util.*,java.io.*"
	pageEncoding="UTF-8"%><%@include file="/include/base.jsp"%>
<!DOCTYPE HTML>
<html>
<head>
<title>hello</title>
<link rel="stylesheet" href="sources/dist/style/weui.css" type="text/css"></link>
<link rel="stylesheet" href="sources/dist/example/example.css" type="text/css"></link>
</head>
	<body>
		<div class="home">
			<div class="hd">
				<h1 class="page_title">
					Hello World!
				</h1>
				<p class="page_desc">
					page description!
				</p>
			</div>
			<div class="bd">
				<div class="weui_grids">
					<ul class="">
						<li>
							<h4 class="weui_media_title"><a target="_blank" href="${basePath}admin/admin.jsp">admin</a></h4>
						</li>
						<li>
							<a target="_blank" href="${basePath}sources/dist/example/index.html">WE-UI</a>
						</li>
					</ul>
					<p>
					<%
						Object obj = new File(this.getServletContext().getRealPath("")).getParentFile().getAbsolutePath();
						out.print(obj);
					%>
					</p>

					<div class="weui_media_box weui_media_text">
						<h4 class="weui_media_title">
							<a href="#">标题一</a>
						</h4>
						<h4 class="weui_media_title">
							标题一
						</h4>
						<h4 class="weui_media_title">
							标题一
						</h4>
						<h4 class="weui_media_title">
							标题一
						</h4>
						<p class="weui_media_desc">
							由各种物质组成的巨型球状天体，叫做星球。星球有一定的形状，有自己的运行轨道。
						</p>
						<ul class="weui_media_info">
							<li class="weui_media_info_meta">
								文字来源
							</li>
							<li class="weui_media_info_meta">
								时间
							</li>
							<li class="weui_media_info_meta weui_media_info_meta_extra">
								其它信息
							</li>
						</ul>
					</div>
					
				</div>
			</div>
		</div>
	</body>
</html>