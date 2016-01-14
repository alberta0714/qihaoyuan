package com.alberta0714.common.lucene;

import static com.alberta0714.common.lucene.IndexUtilsAlber.VERSION;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.management.Query;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alberta0714.qihaoyuan.lucene.DocumentInfo;
import com.alberta0714.qihaoyuan.lucene.FieldInfo;

public class IndexDao {
	private static final Logger logger = LoggerFactory.getLogger(IndexDao.class);

	private static IndexDao service = new IndexDao();

	public static IndexDao inst() {
		return service;
	}

	IndexDao() {
	}

	public boolean createIndex(String name, OpenMode mode) throws IOException {
		File indexPath = new File(IndexUtilsAlber.baseIndexPath, name);
		if (indexPath.exists()) {
			throw new RuntimeException("不能重复创建索引目录!");
		}
		logger.debug("createindex dir ({})", indexPath.getAbsolutePath());
		IndexWriterConfig ixConf = new IndexWriterConfig(VERSION, new StandardAnalyzer(VERSION));
		ixConf.setOpenMode(mode);
		IndexWriter iw = new IndexWriter(this.getFSIndexDirectory(name), ixConf);

		iw.close();
		return true;
	}

	public List<IndexInfo> showIndexList() {
		List<IndexInfo> indexList = new ArrayList<IndexInfo>();
		if (!IndexUtilsAlber.baseIndexPath.exists()) {
			IndexUtilsAlber.baseIndexPath.mkdirs();
			return indexList;
		}
		for (File file : IndexUtilsAlber.baseIndexPath.listFiles()) {
			IndexInfo index = new IndexInfo(file.getName());
			indexList.add(index);
		}
		return indexList;
	}

	public IndexWriter createIndexWriter(String name) throws IOException {
		IndexWriter iw = null;
		Analyzer analyzer = new StandardAnalyzer(IndexUtilsAlber.VERSION);
		IndexWriterConfig iwConf = new IndexWriterConfig(IndexUtilsAlber.VERSION, analyzer);
		iwConf.setOpenMode(OpenMode.APPEND);
		iw = new IndexWriter(getFSIndexDirectory(name), iwConf);
		return iw;
	}

	public List<DocumentInfo> showDocument(String indexName) {
		List<DocumentInfo> docList = new ArrayList<DocumentInfo>();
		IndexReader ir = null;
		try {
			ir = DirectoryReader.open(getFSIndexDirectory(indexName));
			for (int i = 0; i < ir.maxDoc(); i++) {
				Document doc = ir.document(i);

				DocumentInfo docInfo = new DocumentInfo();
				docInfo.setDoc(i);

				docInfo.setFieldsInfos(buildFieldsInfo(doc));

				docList.add(docInfo);
			}
		} catch (IOException e) {
			logger.error("", e);
		} finally {
			IOUtils.closeQuietly(ir);
		}
		return docList;
	}

	private List<FieldInfo> buildFieldsInfo(Document doc) {
		List<FieldInfo> fdInfos = new ArrayList<FieldInfo>();

		List<IndexableField> fds = doc.getFields();
		for (IndexableField fd : fds) {
			FieldInfo fdInfo = new FieldInfo();
			fdInfo.setName(fd.name());
			fdInfo.setFieldType(fd.fieldType().toString());
			fdInfo.setStringValue(fd.stringValue());
			fdInfos.add(fdInfo);
		}
		return fdInfos;
	}

	private FSDirectory getFSIndexDirectory(String indexName) throws IOException {
		FSDirectory dir;
		dir = FSDirectory.open(new File(IndexUtilsAlber.baseIndexPath, indexName));
		return dir;
	}

	public void addDocument(String indexName, String[] fdNames, String[] fdContents, String[] fdTypes) throws IOException {
		IndexWriter iw = null;
		Document doc = new Document();
		try {
			iw = IndexDao.inst().createIndexWriter(indexName);
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
			logger.debug("add to index({}),doc({}) ok!", indexName, doc);
		} finally {
			IOUtils.closeQuietly(iw);
		}
	}
	
	/**
	 * TODO
	 * 
	 * @param indexName
	 * @param query
	 * @return
	 * @throws IOException
	 */
	private boolean deleteDocument(String indexName, Query query) throws IOException {
		
		return false;
	}

	/**
	 * TODO
	 * 
	 * @param indexName
	 * @param query
	 * @return
	 * @throws IOException
	 */
	private List<DocumentInfo> queryIndex(String indexName, Query query) throws IOException {
		
		return null;
	}

	public static void main(String[] args) throws Exception {
		// inst().showIndexList();
		inst().showDocument("qihaoyuan");
	}
}