package com.alberta0714.common.lucene;

import static com.alberta0714.common.lucene.IndexUtilsAlber.VERSION;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
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

	public static void main(String[] args) {
		inst().showIndexList();
	}

	public class IndexInfo implements Serializable {
		private static final long serialVersionUID = 1L;
		private String name = "";
		private String indexPath;
		private double fileSize = 0l;
		private int maxDocNum = -1;

		public IndexInfo(String name) {
			this.name = name;
			File indexDir = new File(IndexUtilsAlber.baseIndexPath, name);
			indexPath = indexDir.getAbsolutePath();

			long length = indexDir.length();
			BigDecimal size = new BigDecimal(length);
			if (0 != length) {
				size = size.divide(new BigDecimal(1024 * 1024));
			}
			size.setScale(2, RoundingMode.HALF_UP);
			fileSize = size.doubleValue();

			try {
				IndexReader ir = DirectoryReader.open(FSDirectory.open(indexDir));
				maxDocNum = ir.maxDoc();
			} catch (IOException e) {
				logger.error("", e);
			}
		}

		public String getName() {
			return name;
		}

		public String getIndexPath() {
			return indexPath;
		}

		public int getMaxDocNum() {
			return maxDocNum;
		}

		public double getFileSize() {
			return fileSize;
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}
	}
}
