<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%><%@include
	file="/include/base.jsp"%><link rel="stylesheet" href="${basePath}defaultTheme/indexMgr.css"
	type="text/css"></link>
<div class="crate">
	<div class="title b">
		索引库信息
	</div>
	<div id="info_1" class="clearfix"></div>
	<div class="rtb" name="indexInfo">
		<ul class="rtb_title clearfix">
			<li class="r1">
				索引库名
			</li>
			<li class="r2">
				路径
			</li>
			<li class="r3">
				文档数
			</li>
			<li class="r4">
				操作
			</li>
		</ul>
	</div>
	<div class="title mt10">
		<span class="b">文档列表</span><span class="tips ml10 gray">(单击序号，可查看字段名)</span>
	</div>
	<div id="info_2">&nbsp;#info_2</div>
	<div class="rtb" name="docList">
	</div>
</div>
<script type="text/javascript" src="../defaultTheme/js/admin/main_showIndex.js"></script>