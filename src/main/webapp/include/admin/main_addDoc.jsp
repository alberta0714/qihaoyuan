<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%><%@include
	file="/include/base.jsp"%><%@page
 import="com.alberta0714.qihaoyuan.IndexDir,com.alberta0714.common.lucene.*" %>
<div class="crate">
	<form action="${basePath}actions/indexMgrAction" method="post">
		<div class="title">
			添加文档
			<input type="hidden" name="m" value="addDocument"/>
			<input type="hidden" name="isRedirect" value="true"/>
		</div>
		<ul>
			<li>
				<span>选择索引库</span>
				<select name="indexName">
					<%
						List<IndexInfo> indexList = IndexServices.inst().showIndexList();
						for (IndexInfo index : indexList) {
					%>
						<option value="<%=index.getName()%>" selected="">
							<%=index.getName()%>
						</option>
					<%
						}
					%>
				</select>
			</li>
			<li class="bd pd10">
				<ul id="fieldinfolist">
					
				</ul>
				<ul>
					<li><div id="bn_addrow" class="bn">添加行</div></li>
				</ul>
			</li>
			<li>
				<input type="submit" value="添加文档到索引库" id="bn_addtoindex"/>
			</li>
		</ul>
	</form>
	<div style="display:none;" id="fieldRowContent">
		<li class="fieldinfo" style="">
			<span>字段名</span>
			<input value="fdName..." type="text" name="fdName" />
			<span>字段内容</span>
			<input value="" type="text" name="fdContent" />
			<span>字段类型</span>
			<select name="fdType">
				<%
					FieldTypes[] fts = FieldTypes.values();
					for (FieldTypes ft : fts) {
				%>
				<option value="<%=ft.name()%>" selected="">
					<%=ft.name()%>
				</option>
				<%
					}
				%>
			</select>
			<div class="bn" style="margin-top:-6px;" name="delRow">删除</div>
		</li>
	</div>
	<script type="text/javascript" src="../defaultTheme/js/admin/main_addDoc.js"></script>
</div>
