package com.alberta0714.common.lucene;

import static com.alberta0714.common.lucene.IndexUtilsAlber.VERSION;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;

public class IndexServices {

	public static boolean createIndex(String indexDir, OpenMode mode) {
		IndexWriterConfig ixConf = new IndexWriterConfig(VERSION, new StandardAnalyzer(VERSION));
		try {
			IndexWriter iw = new IndexWriter(FSDirectory.open(new File(indexDir)), ixConf);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}
