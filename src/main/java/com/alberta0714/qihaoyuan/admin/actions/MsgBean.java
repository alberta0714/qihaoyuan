package com.alberta0714.qihaoyuan.admin.actions;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MsgBean implements Serializable {
	private static final long serialVersionUID = 1L;

	Map<String, Object> msgs = new HashMap<String, Object>();
	public boolean error = false;

	public boolean getError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public Map<String, Object> getMsgs() {
		return msgs;
	}
}
