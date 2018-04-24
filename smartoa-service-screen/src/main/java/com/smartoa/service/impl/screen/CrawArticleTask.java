package com.smartoa.service.impl.screen;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.smartoa.common.util.MD5Util;
import com.smartoa.service.mapper.Articles;
import com.smartoa.service.mapper.ArticlesMapper;
import com.smartoa.service.utils.JobLogUtils;

public class CrawArticleTask implements Runnable {
	JobLogUtils log = new JobLogUtils("job", CrawArticleTask.class);
	ArticlesMapper mapper;

	Articles article;
	DownLoadUtils downLoadUtils = new DownLoadUtils();

	public CrawArticleTask(Articles article, ArticlesMapper mapper) {
		this.article = article;
		this.mapper = mapper;
	}

	@Override
	public void run() {
		this.crawArticle(article);
		// try {
		// Thread.sleep(10);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		this.saveArticle(article, mapper);// 单条存储,孩子因为一条异常,而影响到了后面的记录
	}

	public void crawArticle(Articles article) {
		Document doc = null;
		try {
			// log.info("craw article:{}", article.getArticleSourceUrl());
			doc = Jsoup
					.parse(downLoadUtils.getHtmlBodyWithCache(article.getArticleSourceUrl(), Charset.forName("gbk")));
			if (doc.select("body").html().length() <= 10) {
				log.info("文件太短, 可能 是未下载成功. 重新下载刷新缓存 ");
				doc = Jsoup.parse(downLoadUtils.getHtmlBodyWithCache(article.getArticleSourceUrl(),
						Charset.forName("gbk"), true));
			}
			// TODO
			Elements error = doc.select("#error-page");
			if (error.size() > 0) { // FIXME 这些文件需要重新下载
				log.info(">>>>>>>>>>> 403 error  正在重试 {}", article.getArticleSourceUrl());
				return;
				// doc =
				// Jsoup.parse(downLoadUtils.getHtmlBodyWithCache(article.getArticleSourceUrl(),
				// Charset.forName("gbk"), true));
				// error = doc.select("#error-page");
				// if (error.size() > 0) { // FIXME 这些文件需要重新下载
				// log.info(">>>>>>>>>>> 403 error 重试失败 {}", article.getArticleSourceUrl());
				// return;
				// }
			}
			buildAuthor(article, doc);
			buildArticleDesc(article, doc);
			buildArticleContent(article, doc);
			buildArticleClasses(article, doc);
		} catch (Exception e) {
			e.printStackTrace();
			log.info("{}\n{} {}", article.getArticleSourceUrl(), doc.outerHtml().length());
		}
	}

	private void buildArticleClasses(Articles article, Document doc) {
		Element nav = doc.select("div[class='box mb15 mt10']").get(0);
		Elements items = nav.select("a");

		for (int i = 1; i < items.size(); i++) {
			Element item = items.get(i);
			switch (i) {
			case 1: {
				article.setClass1(item.text());
				break;
			}
			case 2: {
				article.setClass2(item.text());
				break;
			}
			case 3: {
				article.setClass3(item.text());
				break;
			}
			case 4: {
				article.setClass4(item.text());
				break;
			}
			case 5: {
				article.setClass5(item.text());
				break;
			}
			default: {
			}
			}
		}
	}

	private void buildArticleContent(Articles article, Document doc) {
		Element content = doc.select("#content").get(0);
		content.select(".art_xg").remove();

		Elements ps = content.select("p");
		for (int i = 0; i < ps.size(); i++) {
			if (ps.get(i).html().contains("原文链接")) {
				ps.get(i).remove();
			}
		}
		// FIXME 移除所有的图片
		Elements imgs = content.select("img");
		for (int i = 0; i < imgs.size(); i++) {
			imgs.get(i).remove();
		}

		changeImgSrc(content);
		article.setContent(content.html());
	}

	private void changeImgSrc(Element content) {
		Elements imgs = content.select("img");
		for (Element img : imgs) {
			// img.attr("src","");
			String imgUrl = img.attr("src");

			String imgMd5 = null;
			try {
				imgMd5 = MD5Util.getEncryptedPwd(imgUrl);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			if (imgUrl.contains("?")) {
				imgUrl = imgUrl.substring(0, imgUrl.lastIndexOf("?"));
			}
			imgMd5 += imgUrl.substring(imgUrl.lastIndexOf("."), imgUrl.length());
			File imgTmp = new File(
					new File(new File(DownLoadUtils.imgDir, imgMd5.substring(0, 2)), imgMd5.substring(2, 4)), imgMd5);
			downLoadUtils.downLoadToTmpFile(imgUrl, imgTmp);

			String rPath = imgTmp.getAbsolutePath().replace(DownLoadUtils.imgDir.getAbsolutePath(), "").replace("\\",
					"/");
			img.attr("src", rPath);
		}
	}

	private void buildArticleDesc(Articles article, Document doc) {
		String demo = doc.select("#art_demo").get(0).html();
		article.setArticleDesc(demo);
	}

	private void buildAuthor(Articles article, Document doc) {
		Element author = doc.select(".title").get(0).select("p").get(0);
		Matcher m = Pattern.compile("作者：(.+?) &nbsp;").matcher(author.html());
		if (m.find()) {
			article.setAuthor(m.group(1));
		}
	}

	private void saveArticle(Articles article, ArticlesMapper mapper) {
		// int count = mapper.selectCount(null);
		// article.setId(count + 1);
		article.setCreateTime(new Date());
		int flag = mapper.insert(article);
		log.info("save article {}", flag);
	}
}
