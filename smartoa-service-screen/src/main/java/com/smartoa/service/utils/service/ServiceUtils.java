package com.smartoa.service.utils.service;

import java.util.Date;
import java.util.Map;

import org.joda.time.DateTime;

public class ServiceUtils {
	public static void extDate(Map<String, Object> rs, String key) {
		rs.put(key+"Format", null);
		Object executiveTime = rs.get(key);
		if (executiveTime == null) {
			return;
		}
		try {
			rs.put(key + "Format", new DateTime((Date) executiveTime).toString("yyyy-MM-dd"));
		} catch (Exception e) {
			return;
		}
	}
}
