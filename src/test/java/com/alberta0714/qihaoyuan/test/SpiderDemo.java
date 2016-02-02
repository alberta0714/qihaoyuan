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
		String urlStr = "http://friend.renren.com/managefriends";
		Connection con = Jsoup.connect(urlStr);
		Response response = con.execute();
		Map<String, String> cookies = response.cookies();
		System.out.println(cookies);
		outToFile(response);
		text(response);
	}

	private static void text(Response response) {
		String body = Jsoup.parse(response.body()).text().replace("\\s*", "").replace("　", "");

		int length = 80;
		while (body.length() > length) {
			System.out.println(body.substring(0, length));
			body = body.substring(length, body.length());
		}

	}

	private static void outToFile(Response response) throws IOException {
		FileWriter fw = new FileWriter(new File("src/test/java/com/alberta0714/qihaoyuan/test/1.html"));
		fw.write(response.body());
		fw.flush();
		fw.close();
	}

	public static void toString(Object obj) {
		System.out.println(ToStringBuilder.reflectionToString(obj).replace(",", "\n,"));
	}
}
