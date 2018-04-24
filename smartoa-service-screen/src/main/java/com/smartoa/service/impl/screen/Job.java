package com.smartoa.service.impl.screen;

import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Stopwatch;
import com.smartoa.service.impl.screen.Job.Car;
import com.smartoa.service.mapper.Articles;
import com.smartoa.service.mapper.ArticlesMapper;
import com.smartoa.service.utils.JobLogUtils;

@Service("job")
public class Job {
	static JobLogUtils log = new JobLogUtils("job", Job.class);
	ExecutorService pool = Executors.newFixedThreadPool(5);
	int curStart = 0;
	int cur = 0;

	@Autowired
	ArticlesMapper mapper;

	DownLoadUtils downLoadUtils = new DownLoadUtils();

	public void run() {
		Stopwatch wt = Stopwatch.createStarted();
		log.info("start");
		try {
			this.craw();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			log.info("用时:{}", wt);
			System.exit(1);
		}
	}

	public static void main(String[] args) throws Exception {
		log.info("start");
		// new Job().craw();
		new Job().tBit();
		log.info("compeleted!");
	}

	private void tBit() {
		try {
			Document doc = Jsoup.parse(downLoadUtils.getHtmlBodyWithCache("http://car.bitauto.com/brandlist.html",
					Charset.forName("utf-8")));
			Elements secs = doc.select("div[class=name]");
			List<Car> lst = new ArrayList<Car>();
			for (int i = 0; i < secs.size(); i++) {
				Element sec = secs.get(i);

				Element span = sec.selectFirst("span");
				if (span != null && span.hasClass("new")) {
					// String yyyy = sec.selectFirst("img[class=ico_shijia]").attr("title");
					String yyyy = "新车";
					String car = sec.select("a").get(0).text();
					String type = sec.select("a").get(1).text();
					String price = sec.nextElementSibling().text();
					// if (type.equals("[紧凑型]") || type.equals("[小型车]") || type.equals("[中大型]") ||
					// type.equals("[中型车]")) {
					// lst.add(new Car(yyyy, type, car, price));
					// }
				}

				if (sec.select("img[class=ico_shijia]").size() > 0) {
					String yyyy = sec.selectFirst("img[class=ico_shijia]").attr("title");
					String car = sec.select("a").get(0).text();
					String type = sec.select("a").get(1).text();
					String price = sec.nextElementSibling().text();
					if (type.equals("[紧凑型]") || type.equals("[小型车]") || type.equals("[中大型]") || type.equals("[中型车]")) {
						lst.add(new Car(yyyy, type, car, price));
					}
				}
			}
			Collections.sort(lst);
			for (Car c : lst) {
				System.out.println(c);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class Car implements Comparable<Car> {
		String yyyy, type, car, price;

		public Car(String yyyy, String type, String car, String price) {
			this.yyyy = yyyy;
			this.type = type;
			this.car = car;
			this.price = price;
		}

		@Override
		public int compareTo(Car c) {
			int a = c.type.compareTo(this.type);
			int b = c.yyyy.compareTo(this.yyyy);
			int d = c.price.compareTo(this.price);
			// if (a == 0) {
			// return b;
			// }
			// return a;
			if (b == 0) {
				return a;
			}
			return b;
		}

		@Override
		public String toString() {
			return this.type + "\t" + this.yyyy + "\t" + this.car + "\t" + this.price;
		}
	}

	private void craw() throws Exception {
		try {
			this.crawJb51(6, 22);
			this.crawJb51(3, 1012);
			this.crawJb51(15, 378);
		} finally {
			pool.shutdown();
		}
	}

	private void crawJb51(int type, int maxPage) throws ParseException {
		try {
			String source = "http://www.jb51.net/list/list_" + type + "_${page}.htm";
			for (int i = 1; i <= maxPage; i++) {
				String pageUrl = source.replace("${page}", Integer.toString(i));
				// log.info("列表页:"+pageUrl);
				try {
					List<Articles> articles = new ArrayList<Articles>();
					assetsArticleUrls(pageUrl, articles); // 装载页面url
					crawPages(articles);// 采集此页
				} catch (Exception e) {
					e.printStackTrace();
					log.warn("下载此页面出错:{}", pageUrl);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void crawPages(List<Articles> articles) {
		for (Articles article : articles) {
			try {
				cur = cur + 1;
				if (cur < curStart) {
					continue;
				}
				article.setId(cur);
				new CrawArticleTask(article, mapper).run();
				// pool.execute(new CrawArticleTask(article, mapper));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void assetsArticleUrls(String pageUrl, List<Articles> articles) throws ParseException {
		String body = downLoadUtils.getHtmlBodyWithCache(pageUrl, Charset.forName("gbk"));
		Document listDoc = Jsoup.parse(body);
		Elements error = listDoc.select("#error-page");
		if (error.size() > 0) {
			log.info(">>>>>>>>>>> 403 error 正在重试 {}", pageUrl);
			body = downLoadUtils.getHtmlBodyWithCache(pageUrl, Charset.forName("gbk"), true);
			listDoc = Jsoup.parse(body);
			error = listDoc.select("#error-page");
			if (error.size() > 0) {
				log.info(">>>>>>>>>>>  重试后依然 403 error {}", pageUrl);
				return;
			}
		}
		Elements artList = listDoc.select(".artlist");
		if (artList.size() == 0) {
			log.info("查找不到内容列表, 可能是下载失败; 需要重新初始化缓存 ");
			body = downLoadUtils.getHtmlBodyWithCache(pageUrl, Charset.forName("gbk"), true);
			listDoc = Jsoup.parse(body);
			error = listDoc.select("#error-page");
			if (error.size() > 0) { // FIXME 这些文件需要重新下载
				log.info(">>>>>>>>>>> 403 error {}", pageUrl);
				return;
			}
			artList = listDoc.select(".artlist");
			if (artList.size() == 0) {
				log.info("重试失败");
			}
		}
		Elements items = artList.get(0).select("dt");
		for (int k = 0; k < items.size(); k++) {
			Element item = items.get(k);
			String pubDate = item.select("span").get(0).html().replace("日期:", "");
			String listTitle = item.select("a").get(0).html();
			String link = item.select("a").get(0).attr("href");
			// System.out.println(pubDate);
			// System.out.println(listTitle);
			// System.out.println(link);
			Articles article = new Articles();
			article.setArticleSourceUrl("http://www.jb51.net" + link);
			article.setTitle(listTitle);
			article.setPubDate(new SimpleDateFormat("yyyy-MM-dd").parse(pubDate));
			articles.add(article);
		}
	}
}
