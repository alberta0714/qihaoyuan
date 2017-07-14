package com.alberta0714.p85.spider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SpiderMain {
	private static void outToFile(Response response) throws IOException {
		FileWriter fw = new FileWriter(new File(
				"src/test/java/com/alberta0714/p85/spider/1.html"));
		fw.write(response.body());
		fw.flush();
		fw.close();
	}

	private static void text(Response response) {
		String body = Jsoup.parse(response.body()).text().replace("\\s*", "")
				.replace("　", "");

		int length = 80;
		while (body.length() > length) {
			System.out.println(body.substring(0, length));
			body = body.substring(length, body.length());
		}

	}

	public static void toString(Object obj) {
		System.out.println(ToStringBuilder.reflectionToString(obj).replace(",",
				"\n,"));
	}

	public static void main(String[] args) {
		String urlStr = "http://www.85porn.net/albums";

		try {
			Document doc = Jsoup.connect(urlStr).get();
			Elements divThumboverlay = doc.select("div[class=thumb-overlay]");
			for (int i = 0; i < divThumboverlay.size(); i++) {
				Element thum = divThumboverlay.get(i);
				Elements imgs = thum.select("img");
				System.out.println(imgs.outerHtml());
				if (null != imgs && 0 > imgs.size()) {
					String link = imgs.get(0).attr("src");
					System.out.println(link);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
