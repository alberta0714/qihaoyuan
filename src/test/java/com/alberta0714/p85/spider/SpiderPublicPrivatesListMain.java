package com.alberta0714.p85.spider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import com.alberta0714.common.Constant;

public class SpiderPublicPrivatesListMain {
	public static int min = 8000;
	public static int max = min + 5000;

	public static void main(String[] args) throws InterruptedException {
		ExecutorService pool = Executors.newFixedThreadPool(50);

		String link = "http://www.85porn.me/media/photos/tmb/{id}.jpg";
		for (int i = min; i < max; i++) {
			String imgUrl = link.replace("{id}", Integer.toString(i));
			System.out.println(imgUrl);
			pool.execute(new Downloader(imgUrl, i));
		}
		pool.shutdown();
	}
}

class Downloader implements Runnable {
	public static Map<String, String> cookies = new HashMap<String, String>();
	public static final String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0";

	private String url;
	private int i;

	public Downloader(String url, int i) {
		this.url = url;
		this.i = i;
	}

	@Override
	public void run() {
		this.downLoad(url, i);
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
					// .header("Host", "www.85porn.me")//
					.cookies(cookies)//
					.ignoreContentType(true)//
					.timeout(1000 * 20)//
					.execute();
			cookies = res.cookies();
			FileOutputStream fos = new FileOutputStream(file);
			byte[] b = res.bodyAsBytes();
			fos.write(b, 0, b.length);
			fos.close();
			System.out.println("download compeleted!" + imgUrl);
		} catch (SocketTimeoutException e) {
			System.out.println("SocketTimeoutException " + imgUrl);
		} catch (Exception e) {
			ExceptionUtils.getRootCause(e).printStackTrace();
			System.out.println("download error!" + imgUrl);
		}
	}
}
