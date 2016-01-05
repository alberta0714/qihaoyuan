<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%><%@include
	file="/include/base.jsp"%><%@page
 import="com.alberta0714.qihaoyuan.IndexDir" %>
<div class="crate">
	<div class="title">
		添加文档
	</div>

	<ul>
		<li>
			<span>选择索引库</span>
			<select>
				<option value="default" selected="true">
					默认
				</option>
			</select>
		</li>
		<li class="bd pd10">
			<ul>
				<li>
					<span>字段名</span>
					<input value="" type="text" />
					<span>字段内容</span>
					<input value="" type="text" />
					<span>字段类型</span>
					<select>
						<option value="default" selected="true">
							StringField
						</option>
					</select>
				</li>
				<li><button>添加行</button></li>
			</ul>
		</li>
		<li>
			<button>
				添加到索引库
			</button>
		</li>
	</ul>
</div>