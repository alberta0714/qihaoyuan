package com.alberta0714.common.lucene;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexInfo implements Serializable {
	private static final Logger logger = LoggerFactory.getLogger(IndexInfo.class);

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