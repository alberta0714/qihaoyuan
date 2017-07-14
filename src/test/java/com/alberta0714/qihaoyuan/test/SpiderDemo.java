package com.alberta0714.qihaoyuan.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;

public class SpiderDemo {
	public static void main(String[] args) throws Exception {
		String urlStr = "http://www.85porn.net/login?login_remember=on&password=bihkfh123&submit_login=&username=feier87";
		Map<String, String> cookies = new HashMap<String, String>();
		Response response = Login(urlStr, cookies);
		cookies = response.cookies();
		System.out.println(cookies);

		response = showAlbums("http://www.85porn.net/albums?page=1", cookies);
		cookies = response.cookies();
		System.out.println(cookies);

		// http://www.85porn.net/ajax/invite_friend?message=&user_id=62964
		response = getUrl("http://www.85porn.net/ajax/invite_friend?message=&user_id=56258", cookies);
		cookies = response.cookies();
		System.out.println(cookies);

		outToFile(response);
		text(response);
	}

	private static Response showAlbums(String urlStr,
			Map<String, String> cookies) throws IOException {
		Connection con = Jsoup
				.connect(urlStr)
				.header("Accept",
						"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")//
				.header("Accept-Encoding", "gzip, deflate")//
				.header("Accept-Language",
						"zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")//
				.header("Connection", "keep-alive")//
				.header("Cookie",
						"_ga=GA1.2.151721014.1449017678; __atuvc=83%7C6; AVS=5mdatk5shrq96pcnoo7lvl4rt5; __atuvs=56bb3b7ea55b7a34013; _gat=1; splashWeb-1815938-42=1; juicy-js-cookie=juicy-js-cookie")//
				.header("Host", "www.85porn.net")//
				.header("Referer", "http://www.85porn.net/")//
				.header("User-Agent",
						"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0")//
				.cookies(cookies)//
				.followRedirects(false)//
		;
		Response response = con.execute();
		return response;
	}

	private static Response getUrl(String urlStr, Map<String, String> cookies)
			throws IOException {
		Connection con = Jsoup
				.connect(urlStr)
				.header("Accept",
						"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")//
				.header("Accept-Encoding", "gzip, deflate")//
				.header("Accept-Language",
						"zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")//
				.header("Connection", "keep-alive")//
				.header("Cookie",
						"_ga=GA1.2.151721014.1449017678; __atuvc=83%7C6; AVS=5mdatk5shrq96pcnoo7lvl4rt5; __atuvs=56bb3b7ea55b7a34013; _gat=1; splashWeb-1815938-42=1; juicy-js-cookie=juicy-js-cookie")//
				.header("Host", "www.85porn.net")//
				.header("Referer", "http://www.85porn.net/")//
				.header("User-Agent",
						"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0")//
				.cookies(cookies)//
				.followRedirects(false)//
		;
		Response response = con.execute();
		return response;
	}

	private static Response Login(String urlStr, Map<String, String> cookies)
			throws IOException {
		Connection con = Jsoup
				.connect(urlStr)
				.header("Accept",
						"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")//
				.header("Accept-Encoding", "gzip, deflate")//
				.header("Accept-Language",
						"zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")//
				.header("Connection", "keep-alive")//
				.header("Cookie",
						"_ga=GA1.2.151721014.1449017678; __atuvc=83%7C6; AVS=5mdatk5shrq96pcnoo7lvl4rt5; __atuvs=56bb3b7ea55b7a34013; _gat=1; splashWeb-1815938-42=1; juicy-js-cookie=juicy-js-cookie")//
				.header("Host", "www.85porn.net")//
				.header("Referer", "http://www.85porn.net/")//
				.header("User-Agent",
						"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0")//
				.cookies(cookies)//
				.followRedirects(false)//
		;
		Response response = con.execute();
		return response;
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

	private static void outToFile(Response response) throws IOException {
		FileWriter fw = new FileWriter(new File(
				"src/test/java/com/alberta0714/qihaoyuan/test/1.html"));
		fw.write(response.body());
		fw.flush();
		fw.close();
	}

	public static void toString(Object obj) {
		System.out.println(ToStringBuilder.reflectionToString(obj).replace(",",
				"\n,"));
	}
}
