package com.alberta0714.qihaoyuan.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;

public class SpiderDemo {
	public static void main(String[] args) throws Exception {
		String urlStr = "http://www.pptv.com/pc/1.html?plt=web&t=13320&pt=0.1.0.1.2.0.4.0&sadr=&n=&adr=http://pub.aplus.pptv.com/wwwpub/weblogin/?tab=login%26from=web_topnav%26app=&radr=http://www.pptv.com/&puid=64af667ce5da75bcfb39c18e714d9c4f&uid=&vip=0&o=&ro=1&r=0.04782998646277681";
		urlStr = "http://passport.pptv.com/v3/login/login.do?format=jsonp&from=web_topnav&cb=jQuery183004162067418180637_1454052348526&username=sunzhanchao%40126.com&password=15210187765&CheckboxSaveInfo=on&_=1454052354926";
		// Connection con =
		// Jsoup.connect("http://passport.pptv.com/v3/login/login.do?format=jsonp&from=web_topnav&cb=jQuery183004162067418180637_1454052348526&username=sunzhanchao%40126.com&password=15210187765&CheckboxSaveInfo=on&_=1454052354926");
		Connection con = Jsoup.connect(urlStr);
		Response response = con.execute();
		Map<String, String> cookies = response.cookies();

		response = Jsoup.connect("http://pptv.com").cookies(cookies).execute();
		cookies = response.cookies();

		outToFile(response);
		text(response);
	}

	private static void text(Response response) {
		String body = Jsoup.parse(response.body()).text().replace("\\s*", "").replace("　", "");

		int length = 100;
		while (body.length() > length) {
			System.out.println(body.substring(0, length));
			body = body.substring(length, body.length());
		}

	}

	private static void outToFile(Response response) throws IOException {
		FileWriter fw = new FileWriter(new File("spider/spiderCookiesSession/tmp.html"));
		fw.write(response.body());
		fw.flush();
		fw.close();
	}

	public static void toString(Object obj) {
		System.out.println(ToStringBuilder.reflectionToString(obj).replace(",", "\n,"));
	}
}
