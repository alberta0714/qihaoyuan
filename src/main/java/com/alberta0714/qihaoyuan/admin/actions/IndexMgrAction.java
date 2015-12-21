package com.alberta0714.qihaoyuan.admin.actions;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;

import com.alberta0714.common.lucene.IndexServices;
import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("serial")
public class IndexMgrAction extends HttpServlet {
	public static final String m_CREATEINDEXDIR = "createIndexDir".toLowerCase();
	ObjectMapper om = new ObjectMapper();

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String indexDir = request.getParameter("indexDir");
		String m = StringUtils.trimToEmpty(request.getParameter("m")).toLowerCase();

		MsgBean msg = new MsgBean();
		if (m.equals(m_CREATEINDEXDIR)) {
			boolean ok = IndexServices.createIndex(indexDir, OpenMode.CREATE);
			msg.getMsgs().put("status", ok);
		} else {
			msg.setError("unsupported " + m);
		}
		response.getWriter().append(om.writeValueAsString(msg));
	}
}
