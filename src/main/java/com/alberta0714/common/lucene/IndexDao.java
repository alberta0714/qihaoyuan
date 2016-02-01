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
import org.apache.lucene.facet.index.FacetFields;
import org.apache.lucene.facet.params.FacetSearchParams;
import org.apache.lucene.facet.search.CountFacetRequest;
import org.apache.lucene.facet.search.FacetResult;
import org.apache.lucene.facet.search.FacetsCollector;
import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.MultiCollector;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.joda.time.DateTime;
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
		// TODO 详细的验证
		File indexPath = new File(IndexUtilsAlber.baseIndexPath, name);
		if (indexPath.exists()) {
			throw new RuntimeException("不能重复创建索引目录!");
		}
		logger.debug("createindex dir ({})", indexPath.getAbsolutePath());

		// init indexDir
		IndexWriterConfig ixConf = new IndexWriterConfig(VERSION, new StandardAnalyzer(VERSION));
		ixConf.setOpenMode(mode);
		IndexWriter iw = new IndexWriter(this.getFSIndexDirectory(name), ixConf);
		DirectoryTaxonomyWriter taxoWriter = new DirectoryTaxonomyWriter(this.getFSTaxoIndexDirectory(name), OpenMode.CREATE);

		taxoWriter.commit();
		iw.commit();
		iw.close();
		taxoWriter.close();
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
		// iwConf.setMergePolicy(new LogDocMergePolicy());
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
		File indexDataDir = new File(IndexUtilsAlber.baseIndexPath, indexName);
		File indexDir = new File(indexDataDir, "index");
		return FSDirectory.open(indexDir);
	}

	private FSDirectory getFSTaxoIndexDirectory(String indexName) throws IOException {
		File indexDataDir = new File(IndexUtilsAlber.baseIndexPath, indexName);
		File taxoDir = new File(indexDataDir, "taxo");
		FSDirectory dir = FSDirectory.open(taxoDir);
		return dir;
	}

	public void addDocument(String indexName, String[] fdNames, String[] fdContents, String[] fdTypes) throws IOException {
		IndexWriter iw = null;
		TaxonomyWriter taxoWriter = null;
		Document doc = new Document();
		try {
			iw = IndexDao.inst().createIndexWriter(indexName);
			// Writes facet ords to a separate directory from the main index
			taxoWriter = new DirectoryTaxonomyWriter(this.getFSTaxoIndexDirectory(indexName), OpenMode.APPEND);
			if (null == fdNames) {
				// TODO verify it
			}
			List<CategoryPath> paths = new ArrayList<CategoryPath>();
			// Reused across documents, to add the necessary facet fields
			FacetFields facetFields = new FacetFields(taxoWriter);
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
				paths.add(new CategoryPath(name + "/" + value));
			}

			if (0 != doc.getFields().size()) {
				addMustedFields(doc, paths);

				iw.addDocument(doc);
				facetFields.addFields(doc, paths);
				// http://www.cnblogs.com/huangfox/p/4177848.html
			}
			taxoWriter.commit();
			iw.forceMerge(1);
			iw.commit();
			logger.debug("add to index({}),doc({}) ok!", indexName, doc);
		} finally {
			IOUtils.closeQuietly(iw);
			IOUtils.closeQuietly(taxoWriter);
		}
	}

	private void addMustedFields(Document doc, List<CategoryPath> paths) {
		// 添加一些必要字段
		String datetime = new DateTime().toString("yyyy/MM/dd HH:mm:ss");
		StringField createTime = new StringField("createTime", datetime, Store.YES);
		paths.add(new CategoryPath("createTime/" + datetime));
		doc.add(createTime);

		StringField all = new StringField("all", "all", Store.YES);
		paths.add(new CategoryPath("all/all"));
		doc.add(all);
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
		String indexName = "qihaoyuan";
		IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(inst().getFSIndexDirectory(indexName)));
		TaxonomyReader taxoReader = new DirectoryTaxonomyReader(inst().getFSTaxoIndexDirectory(indexName));

		// http://www.cnblogs.com/huangfox/p/4177750.html
		// http://www.cnblogs.com/huangfox/p/4177848.html
		// Count both "Publish Date" and "Author" dimensions
		FacetSearchParams fsp = new FacetSearchParams(new CountFacetRequest(new CategoryPath("all"), 10));
		// Aggregatses the facet counts
		FacetsCollector fc = FacetsCollector.create(fsp, searcher.getIndexReader(), taxoReader);

		// MatchAllDocsQuery is for "browsing" (counts facets
		// for all non-deleted docs in the index); normally
		// you'd use a "normal" query, and use MultiCollector to
		// wrap collecting the "normal" hits and also facets:
		TopScoreDocCollector tdc = TopScoreDocCollector.create(10, true);
		searcher.search(new MatchAllDocsQuery(), MultiCollector.wrap(tdc, fc));
		// Retrieve results
		List<FacetResult> facetResults = fc.getFacetResults();

		System.out.println("total hits:" + tdc.getTotalHits());
		System.out.println("matchingDocs:" + fc.getMatchingDocs().size());
		System.out.println(facetResults);

		taxoReader.close();
		searcher.getIndexReader().close();
	}
}