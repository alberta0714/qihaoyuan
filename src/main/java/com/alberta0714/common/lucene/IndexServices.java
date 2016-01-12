package com.alberta0714.common.lucene;

import static com.alberta0714.common.lucene.IndexUtilsAlber.VERSION;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexServices {
	private static final Logger logger = LoggerFactory.getLogger(IndexServices.class);

	private static IndexServices service = new IndexServices();

	public static IndexServices inst() {
		return service;
	}

	IndexServices() {
	}

	public boolean createIndex(String name, OpenMode mode) throws IOException {
		File indexPath = new File(IndexUtilsAlber.baseIndexPath, name);
		if (indexPath.exists()) {
			throw new RuntimeException("不能重复创建索引目录!");
		}
		logger.debug("createindex dir ({})", indexPath.getAbsolutePath());
		IndexWriterConfig ixConf = new IndexWriterConfig(VERSION, new StandardAnalyzer(VERSION));
		ixConf.setOpenMode(mode);
		IndexWriter iw = new IndexWriter(FSDirectory.open(indexPath), ixConf);

		iw.close();
		return true;
	}

	public List<IndexInfo> showIndexList() {
		List<IndexInfo> indexList = new ArrayList<IndexInfo>();
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
		iw = new IndexWriter(FSDirectory.open(new File(IndexUtilsAlber.baseIndexPath, name)), iwConf);
		return iw;
	}
	
	

	public static void main(String[] args) {
		inst().showIndexList();
	}
}
