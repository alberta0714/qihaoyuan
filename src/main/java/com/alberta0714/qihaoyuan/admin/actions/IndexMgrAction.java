package com.alberta0714.qihaoyuan.admin.actions;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alberta0714.qihaoyuan.admin.service.IndexMgrService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("serial")
public class IndexMgrAction extends HttpServlet {
	private static final Logger logger = LoggerFactory.getLogger(IndexMgrAction.class);
	IndexMgrService mgrService = new IndexMgrService();

	ObjectMapper om = new ObjectMapper();

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String method = StringUtils.trimToEmpty(request.getParameter("m")).toLowerCase();
		boolean isRedirect = BooleanUtils.toBoolean(request.getParameter("isRedirect"));

		MsgBean msg = new MsgBean();
		msg.getMsgs().put("m", method);
		msg.getMsgs().put("isRedirect", isRedirect);
		M m = null;
		try {
			m = M.valueOf(method.toUpperCase());
		} catch (Exception e) {
			logger.error("", e);
			msg.getMsgs().put("exception", e);
		}
		if (null == m) {
			msg.error = true;
		} else {
			switch (m) {
			case ADDDOCUMENT: {
				mgrService.addDocument(request, response, isRedirect, msg);
				break;
			}
			case CREATEINDEXDIR: {
				mgrService.createIndex(request, response, isRedirect, msg);
				break;
			}
			case SHOWDOCUMENTS: {
				String indexName = request.getParameter("indexName");
				mgrService.showDocuments(request, response, isRedirect, msg, indexName);
				break;
			}
			case SHOWINDEXES: {
				String indexName = request.getParameter("indexName");
				mgrService.showIndex(request, response, isRedirect, msg, indexName);
				break;
			}
			}
		}
		response.setContentType("application/json");
		response.getWriter().append(om.writeValueAsString(msg));
	}

	enum M {
		CREATEINDEXDIR("createIndexDir"), //
		SHOWINDEXES("showIndexes"), //
		ADDDOCUMENT("addDocument"), //
		SHOWDOCUMENTS("showDocuments");

		String methodName;

		private M(String methodName) {
			this.methodName = methodName.toLowerCase();
		}
	}
}