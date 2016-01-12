package com.alberta0714.qihaoyuan.admin.actions;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alberta0714.common.lucene.FieldTypes;
import com.alberta0714.common.lucene.IndexInfo;
import com.alberta0714.common.lucene.IndexServices;
import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("serial")
public class IndexMgrAction extends HttpServlet {
	private static final Logger logger = LoggerFactory.getLogger(IndexMgrAction.class);
	static IndexServices indexService = IndexServices.inst();

	public static final String m_CREATEINDEXDIR = "createIndexDir".toLowerCase();
	public static final String m_SHOWINDEXES = "showIndexes".toLowerCase();
	public static final String m_ADDDOCUMENT = "addDocument".toLowerCase();

	ObjectMapper om = new ObjectMapper();

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String m = StringUtils.trimToEmpty(request.getParameter("m")).toLowerCase();
		boolean isRedirect = BooleanUtils.toBoolean(request.getParameter("isRedirect"));

		MsgBean msg = new MsgBean();
		if (m.equals(m_CREATEINDEXDIR)) {// 创建索引
			String indexName = request.getParameter("indexName");
			boolean ok = false;
			try {
				ok = indexService.createIndex(indexName, OpenMode.CREATE);
			} catch (Exception e) {
				msg.getMsgs().put("exception", e);
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
		} else if (m.equals(m_ADDDOCUMENT)) {
			String[] fdNames = request.getParameterValues("fdName");
			String[] fdContents = request.getParameterValues("fdContent");
			String[] fdTypes = request.getParameterValues("fdType");

			String indexName = request.getParameter("indexName");

			IndexWriter iw = null;
			Document doc = new Document();
			try {
				iw = IndexServices.inst().createIndexWriter(indexName);
				if (null == fdNames) {
					// TODO verify it
				}
				for (int i = 0; i < fdNames.length; i++) {
					Field fd = null;
					String name = fdNames[i];
					String value = fdContents[i];
					Store store = Store.YES;
					FieldTypes fdType = FieldTypes.valueOf(fdTypes[i]);

					switch (fdType) {
					case StringField: {
						fd = new StringField(name, value, store);
						doc.add(fd);
						break;
					}
					case TextField: {
						fd = new TextField(name, value, store);
						doc.add(fd);
						break;
					}
					}
				}
				if (0 != doc.getFields().size()) {
					iw.addDocument(doc);
				}
				msg.getMsgs().put("ok", true);
				msg.getMsgs().put("doc", doc.toString());
				logger.debug("add to index({}),doc({}) ok!", indexName, doc);
				
				if (isRedirect) {
					StringBuilder sb = new StringBuilder("../admin/addDocument.jsp?ok=true");
					response.sendRedirect(sb.toString());
				}
			} catch (Exception e) {
				msg.error = true;
				msg.getMsgs().put("exception", e);
				logger.error("exception", e);
			} finally {
				try {
					iw.close();
				} catch (Exception e) {
				}
			}
		} else {
			msg.error = true;
		}
		response.setContentType("application/json");
		response.getWriter().append(om.writeValueAsString(msg));
	}
}