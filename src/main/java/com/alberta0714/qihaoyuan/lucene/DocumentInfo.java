package com.alberta0714.qihaoyuan.lucene;

import java.util.List;

public class DocumentInfo {
	List<FieldInfo> fieldInfos;
	int doc;

	public List<FieldInfo> getFieldsInfos() {
		return fieldInfos;
	}

	public void setFieldsInfos(List<FieldInfo> fds) {
		this.fieldInfos = fds;
	}

	public int getDoc() {
		return doc;
	}

	public void setDoc(int doc) {
		this.doc = doc;
	}

}
