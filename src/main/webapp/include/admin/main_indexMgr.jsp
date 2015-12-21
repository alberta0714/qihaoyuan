<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%><%@include
	file="/include/base.jsp"%>
<link rel="stylesheet" href="${basePath}defaultTheme/indexMgr.css"
	type="text/css"></link>
<div class="crate createIndexDir">
	<form action="${basePath}actions/indexMgrAction" method="post">
		<div class="title">
			创建索引仓库
		</div>
		<ul>
			<li>
				<span>仓库索引目录:</span>
				<input type="text" value="D:\qihaoyuanData\index"
					style="width: 594px;" class="indexDir" name="indexDir" />
				<input type="hidden" name="m" value="createIndexDir" />
			</li>
			<li>
				<input type="submit" value="创建" class="bn" />
			</li>
		</ul>
	</form>
</div>
<div class="crate">
	<div class="title">
		索引库列表
	</div>
	<div class="rtb">
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
		<ul class="clearfix">
			<li class="r1">
				&nbsp;
			</li>
			<li class="r2">
				&nbsp;
			</li>
			<li class="r3">
				&nbsp;
			</li>
			<li class="r4">
				详情|删除
			</li>
		</ul>
		<ul class="clearfix">
			<li class="r1">
				&nbsp;
			</li>
			<li class="r2">
				&nbsp;
			</li>
			<li class="r3">
				&nbsp;
			</li>
			<li class="r4">
				&nbsp;
			</li>
		</ul>
		<ul class="clearfix">
			<li class="r1">
				&nbsp;
			</li>
			<li class="r2">
				&nbsp;
			</li>
			<li class="r3">
				&nbsp;
			</li>
			<li class="r4">
				&nbsp;
			</li>
		</ul>
		<ul class="clearfix">
			<li class="r1">
				&nbsp;
			</li>
			<li class="r2">
				&nbsp;
			</li>
			<li class="r3">
				&nbsp;
			</li>
			<li class="r4">
				&nbsp;
			</li>
		</ul>
		<ul class="clearfix">
			<li class="r1">
				&nbsp;
			</li>
			<li class="r2">
				&nbsp;
			</li>
			<li class="r3">
				&nbsp;
			</li>
			<li class="r4">
				&nbsp;
			</li>
		</ul>
	</div>
</div>


