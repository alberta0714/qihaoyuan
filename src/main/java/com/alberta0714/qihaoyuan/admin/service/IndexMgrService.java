package com.alberta0714.qihaoyuan.admin.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alberta0714.common.lucene.IndexInfo;
import com.alberta0714.common.lucene.IndexDao;
import com.alberta0714.qihaoyuan.admin.actions.MsgBean;
import com.alberta0714.qihaoyuan.lucene.DocumentInfo;

public class IndexMgrService {
	private static final Logger logger = LoggerFactory.getLogger(IndexMgrService.class);
	static IndexDao indexDao = IndexDao.inst();

	public void createIndex(HttpServletRequest request, HttpServletResponse response, boolean isRedirect, MsgBean msg) throws UnsupportedEncodingException, IOException {
		String indexName = request.getParameter("indexName");
		boolean ok = false;
		try {
			ok = indexDao.createIndex(indexName, OpenMode.CREATE);
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
	}

	public void addDocument(HttpServletRequest request, HttpServletResponse response, boolean isRedirect, MsgBean msg) {
		String[] fdNames = request.getParameterValues("fdName");
		String[] fdContents = request.getParameterValues("fdContent");
		String[] fdTypes = request.getParameterValues("fdType");
		String indexName = request.getParameter("indexName");
		try {
			indexDao.addDocument(indexName, fdNames, fdContents, fdTypes);
			msg.getMsgs().put("ok", true);
			if (isRedirect) {
				StringBuilder sb = new StringBuilder("../admin/addDocument.jsp?ok=true");
				response.sendRedirect(sb.toString());
			}
		} catch (IOException e) {
			msg.error = true;
			msg.getMsgs().put("exception", e);
		}
	}

	public void showIndex(HttpServletRequest request, HttpServletResponse response, boolean isRedirect, MsgBean msg) {
		showIndex(request, response, isRedirect, msg, null);
	}

	public void showIndex(HttpServletRequest request, HttpServletResponse response, boolean isRedirect, MsgBean msg, String indexName) {
		if (StringUtils.isEmpty(indexName)) {
			List<IndexInfo> indexList = indexDao.showIndexList();
			msg.getMsgs().put("indexList", indexList);
		} else {
			List<IndexInfo> indexList = indexDao.showIndexList();
			IndexInfo r = null;
			for (IndexInfo info : indexList) {
				if (indexName.equals(info.getName())) {
					r = info;
					break;
				}
			}
			msg.getMsgs().put("indexInfo", r);
		}
	}

	public void showDocuments(HttpServletRequest request, HttpServletResponse response, boolean isRedirect, MsgBean msg, String indexName) {
		if (StringUtils.isEmpty(indexName)) {
			msg.error = true;
			msg.getMsgs().put("info", "indexName is empty!");
			return;
		}
		List<DocumentInfo> docs = indexDao.showDocument(indexName);
		msg.getMsgs().put("docs", docs);
	}
}
