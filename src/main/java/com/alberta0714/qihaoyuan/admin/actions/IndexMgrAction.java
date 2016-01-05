package com.alberta0714.qihaoyuan.admin.actions;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alberta0714.common.lucene.IndexServices;
import com.alberta0714.common.lucene.IndexServices.IndexInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("serial")
public class IndexMgrAction extends HttpServlet {
	private static final Logger logger = LoggerFactory.getLogger(IndexMgrAction.class);
	static IndexServices indexService = IndexServices.inst();

	public static final String m_CREATEINDEXDIR = "createIndexDir".toLowerCase();
	public static final String m_SHOWINDEXES = "showIndexes".toLowerCase();

	ObjectMapper om = new ObjectMapper();

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String indexName = request.getParameter("indexName");
		String m = StringUtils.trimToEmpty(request.getParameter("m")).toLowerCase();
		boolean isRedirect = BooleanUtils.toBoolean(request.getParameter("isRedirect"));

		MsgBean msg = new MsgBean();
		if (m.equals(m_CREATEINDEXDIR)) {// 创建索引
			boolean ok = false;
			try {
				ok = indexService.createIndex(indexName, OpenMode.CREATE);
			} catch (Exception e) {
				msg.getMsgs().put("exception", e.getMessage());
				logger.error("", e);
			}
			msg.getMsgs().put("status", ok);
			msg.getMsgs().put("indexName", indexName);
			if (isRedirect) {
				StringBuilder sb = new StringBuilder("../admin/indexMgr.jsp?ok=" + ok);
				if (!ok) {
					sb.append("&exception=" + java.net.URLEncoder.encode(msg.getMsgs().get("exception").toString(), Charset.forName("UTF-8").name()));
				}
				response.sendRedirect(sb.toString());
			}
		} else if (m.equals(m_SHOWINDEXES)) {// 显示索引
			List<IndexInfo> indexList = indexService.showIndexList();
			msg.getMsgs().put("indexList", indexList);
		} else {
			msg.setError("unsupported " + m);
		}
		response.setContentType("application/json");
		response.getWriter().append(om.writeValueAsString(msg));
	}
}