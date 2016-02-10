package com.alberta0714.p85.spider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import com.alberta0714.common.Constant;

public class CopyOfSpiderListMain {
	public static final String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0";
	public static int max = 12538;

	public static void main(String[] args) throws InterruptedException {
		// while (true) {
		max = 76693;
		int min = 0;
		String link = "http://www.85porn.me/media/photos/tmb/{id}.jpg";
		for (int i = max; i > min; i--) {
			String imgUrl = link.replace("{id}", Integer.toString(i));
			System.out.println(imgUrl);
			downLoad(imgUrl, i);
		}
		// }
	}

	private static void downLoad(String imgUrl, int i) {
		try {
			File file = new File(Constant.BASEDIR, "privates");
			if (!file.exists()) {
				file.mkdirs();
			}
			file = new File(file, i + ".jpg");
			if (file.exists()) {
				return;
			}

			Response res = Jsoup
					.connect(imgUrl)
					.header("User-Agent", userAgent)
					.header("Accept",
							"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
					.header("Accept-Encoding", "gzip, deflate")
					.header("Accept-Language",
							"zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
					.header("Connection", "keep-alive")//
					.header("Host", "www.85porn.me")//
					.ignoreContentType(true)//
					// .timeout(arg0)
					.execute();

			System.out.println("status:" + res.statusCode());

			FileOutputStream fos = new FileOutputStream(file);
			byte[] b = res.bodyAsBytes();
			fos.write(b, 0, b.length);
			fos.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
