<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%><%@include
	file="/include/base.jsp"%><%@ page import="com.alberta0714.common.lucene.IndexServices,com.alberta0714.common.lucene.IndexInfo" %>
<link rel="stylesheet" href="${basePath}defaultTheme/indexMgr.css"
	type="text/css"></link>
<div class="crate createIndexDir">
	<form action="${basePath}actions/indexMgrAction?isRedirect=true" method="post">
		<div class="title">
			创建索引仓库
		</div>
		<ul>
			<li>
				<span>仓库索引目录:</span>
				<input type="text" value="qihaoyuan"
					style="width: 594px;" class="indexName" name="indexName" />
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
		<%
			List<IndexInfo> list = IndexServices.inst().showIndexList();
			for(int i = 0 ; i < list.size(); i++){
				IndexInfo info = list.get(i);
		%>	
		<ul class="clearfix">
			<li class="r1">
				<%=info.getName()%>
			</li>
			<li class="r2">
				<%=info.getIndexPath()%>
			</li>
			<li class="r3">
				<%=info.getMaxDocNum()%>
			</li>
			<li class="r4">
				删除
			</li>
		</ul>
		<%	
			}
		%>
	</div>
</div>


