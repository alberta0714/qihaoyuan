package com.alberta0714.qihaoyuan.test;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.lang.builder.ToStringBuilder;

public class TestSpider {
	public static void main(String[] args) throws Exception {
		URL url = new URL("http://pptv.com");
		URLConnection con = url.openConnection();
		System.out.println(ToStringBuilder.reflectionToString(con).replace(",", "\n"));
		System.out.println(1);
		System.out.println(url);
	}
}
