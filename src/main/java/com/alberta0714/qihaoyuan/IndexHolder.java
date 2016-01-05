package com.alberta0714.qihaoyuan;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alberta0714.common.lucene.IndexUtilsAlber;

public class IndexHolder {
	private static final Logger logger = LoggerFactory.getLogger(IndexHolder.class);

	private static IndexHolder holder = new IndexHolder();

	private IndexHolder() {
		reload();
	}

	public static IndexHolder inst() {
		return holder;
	}

	public Map<String, IndexDir> indexes = new HashMap<String, IndexDir>();

	private void reload() {
		for (File file : IndexUtilsAlber.baseIndexPath.listFiles()) {
			IndexDir dir = new IndexDir();
			indexes.put(file.getName(), dir);
		}
	}
}
