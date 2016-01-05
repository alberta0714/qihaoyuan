package com.alberta0714.common.lucene;

import java.io.File;

import org.apache.lucene.util.Version;

import com.alberta0714.common.Constant;

public class IndexUtilsAlber {
	public static Version VERSION = Version.LUCENE_43;
	public static final File baseIndexPath = new File(Constant.BASEDIR, "index");

}
