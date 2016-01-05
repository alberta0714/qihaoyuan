package com.alberta0714.qihaoyuan.admin.actions;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MsgBean implements Serializable {
	private static final long serialVersionUID = 1L;

	Map<String, Object> msgs = new HashMap<String, Object>();
	String error = null;

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public Map<String, Object> getMsgs() {
		return msgs;
	}
}
