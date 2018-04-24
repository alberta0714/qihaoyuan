package com.smartoa.service.impl.screen.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartoa.service.model.screen.map.Areas;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpUtils {
	HttpURLConnection conn = null;
	String url = null;
	String charset = "UTF-8";

	public HttpUtils(String url) throws Exception {
		this.url = url;
		URL urlObj = new URL(this.url);
		conn = (HttpURLConnection) urlObj.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
		conn.setRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
	}

	public String getHtmlText() throws Exception {
		log.info("connecting [{}]", this.url);
		conn.connect();
		BufferedReader br = new BufferedReader(
				new InputStreamReader(conn.getInputStream(), Charset.forName(this.charset)));
		StringBuffer content = new StringBuffer();
		String line = null;
		while ((line = br.readLine()) != null) {
			content.append(line).append("\n");
		}
		return content.toString();
	}

	public HttpUtils CharSet(String charset) {
		this.charset = charset;
		return this;
	}

	public HttpUtils setOriginAliyunDataV() {
		conn.setRequestProperty("Origin", "http://datav.aliyun.com");
		conn.setRequestProperty("Referer", "http://datav.aliyun.com/static/tools/atlas/?spm=5176.doc53843.2.3.aegIL9");
		return this;
	}
	// 可以继续设置timeout , User-Agent, Get,Post方法等
	// ...

	public static void main(String[] args) throws Exception {
		// String jsonBody = new
		// HttpUtils("http://datavmap-public.oss-cn-hangzhou.aliyuncs.com/areas/bound/110100.json")
		// .setOriginAliyunDataV().getHtmlText();
		String jsonBody = new HttpUtils(
				"http://datavmap-public.oss-cn-hangzhou.aliyuncs.com/areas/children/110100.json").setOriginAliyunDataV()
						.getHtmlText();

		ObjectMapper om = new ObjectMapper();
		Areas areas = om.readValue(jsonBody, Areas.class);
		System.out.println(areas);
	}
}
