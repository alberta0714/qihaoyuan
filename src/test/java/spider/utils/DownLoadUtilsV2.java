package spider.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.Stopwatch;

import spider.main.HaodfMain;
import spider.main.IgnoreSSL;
import spider.main.ParseCookies;

public class DownLoadUtilsV2 {
	JobLogUtils log = new JobLogUtils("job", DownLoadUtilsV2.class).colseLog();

	private File tmpDir = null;// default
	private File imgDir = null;// default
	boolean isWithImages = false;
	private long sleep = 0;

	Map<String, String> cookies = new LinkedHashMap<String, String>();

	public DownLoadUtilsV2(File tmpDirecory) throws Exception {
		IgnoreSSL.trustAll();
		tmpDir = new File(tmpDirecory, "htmls");
		imgDir = new File(tmpDirecory, "imgs");
	}

	public DownLoadUtilsV2 isWithImages(boolean isWithImages) {
		this.isWithImages = isWithImages;
		return this;
	}

	public DownLoadUtilsV2(File tmpDirecory, String cookies) throws Exception {
		IgnoreSSL.trustAll();
		tmpDir = new File(tmpDirecory, "htmls");
		imgDir = new File(tmpDirecory, "imgs");
		this.cookies = ParseCookies.convertToCookiesMap(cookies);
	}

	public DownLoadUtilsV2 setSleep(long millisecond) {
		this.sleep = millisecond;
		return this;
	}

	public Document getHtmlDocumentWithCache(String itemUrl, Charset charset) throws IOException {
		return Jsoup.parse(this.getHtmlBodyWithCache(itemUrl, charset));
	}

	public String getHtmlBodyWithCache(String itemUrl, Charset charset) throws IOException {
		return getHtmlBodyWithCache(itemUrl, charset, false);
	}

	public Document getHtmlDocumentWithCache(String itemUrl, Charset charset, boolean overWrite) throws IOException {
		return Jsoup.parse(this.getHtmlBodyWithCache(itemUrl, charset, overWrite));
	}

	public String getHtmlBodyWithCache(String itemUrl, Charset charset, boolean overWrite) throws IOException {
		String md5 = null;
		try {
			md5 = MD5Util.getEncryptedPwd(itemUrl) + ".html";
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		File tmp = new File(new File(new File(tmpDir, md5.substring(0, 2)), md5.substring(2, 4)), md5);
		// log.info("{} read from {}", itemUrl, tmp.getAbsolutePath());
		if (overWrite) {
			downLoadToTmpFile(itemUrl, tmp, true);
		} else if (!tmp.exists()) {
			downLoadToTmpFile(itemUrl, tmp);
		}
		try {
			return FileUtils.readFileToString(tmp, charset);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void downLoadToTmpFile(String itemUrl, File tmp) throws IOException {
		downLoadToTmpFile(itemUrl, tmp, false);
	}

	public void downLoadToTmpFile(String itemUrl, File tmp, boolean overWrite) throws IOException {
		if (!overWrite && tmp.exists()) {
			return;
		}
		if (itemUrl.startsWith("//")) {
			itemUrl = "http:" + itemUrl;
		}
		if (tmp.getParentFile() != null && !tmp.getParentFile().exists()) {
			boolean mkdirs = tmp.getParentFile().mkdirs();
			log.info("创建目录{} {}", mkdirs, tmp.getParentFile().getAbsolutePath());
			if (!mkdirs) {
				return;
			}
		}

		Stopwatch wt = Stopwatch.createStarted();
		byte[] bodyBytes = getResponseBodyByJsoup(itemUrl);
		if (bodyBytes != null) {
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(tmp);
				fos.write(bodyBytes);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					fos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		log.info("下载完毕{}, 写入文件:{}", wt, tmp.getAbsolutePath());
	}

	/**
	 * TODO cookies做支持
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private byte[] getResponseBodyByHttpClient(String itemUrl) {
		byte[] bodyBytes = null;
		log.info("正在下载: {}", itemUrl);
		HttpClient client = new HttpClient();
		HttpMethod m = new GetMethod(itemUrl);
		String uae = "Mozilla/5.0 (compatible; Baiduspider/2.0;+http://www.baidu.com/search/spider.html) ";
		m.setRequestHeader("User-Agent", uae);
		try {
			client.executeMethod(m);
			bodyBytes = m.getResponseBody();
		} catch (Exception e) {
			log.warn("下载异常:{} {}", itemUrl, e);
		} finally {
			try {
				m.releaseConnection();
			} catch (Exception e) {
				log.error("release connection exception:", e);
			}
		}
		return bodyBytes;
	}

	private byte[] getResponseBodyByJsoup(String itemUrl) throws IOException {
		if (itemUrl == null || StringUtils.isEmpty(itemUrl)) {
			return null;
		}
		log.info("正在下载: {}", itemUrl);
		Connection conn = Jsoup.connect(itemUrl);
		conn.ignoreContentType(true);
		conn.timeout(1000 * 60 * 10);
		conn.maxBodySize(1024 * 1024 * 1024);
		conn.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")//
				.header("Accept-Encoding", "gzip, deflate, br")//
				.header("Accept-Language", "en-US,en;q=0.9,zh;q=0.8,zh-CN;q=0.7")//
				.header("Cache-Control", "max-age=0")//
				.header("Connection", "keep-alive")//
				.header("Upgrade-Insecure-Requests", "1")//
				.userAgent(
						"Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")//
		;
		conn.cookies(cookies);

		// TODO 抽取出来referer, host 设置GET，POST方法
		// conn.header("Referer", "https://passport.haodf.com/index/mycenter");
		// conn.header("Host", "passport.haodf.com");
		conn.method(Method.GET);

		Response response = conn.execute();
		Map<String, String> rCookies = response.cookies();
		if (rCookies != null && rCookies.size() > 0) {
			this.cookies.putAll(rCookies);
		}

		String contentType = response.contentType();
		if (contentType != null && contentType.toLowerCase().contains("text") && isWithImages) {
			Document doc = response.parse();
			this.changeImgSrc(doc);
			if (sleep > 0) {
				try {
					Thread.sleep(sleep);
				} catch (Exception e) {
					log.error("", e);
				}
			}
			return doc.outerHtml().getBytes();
		} else {
			log.info("contentType:{}", contentType);
		}
		return response.bodyAsBytes();
	}

	private void changeImgSrc(Element content) throws IOException {
		Elements imgs = content.select("img");
		for (Element img : imgs) {
			String link = null;
			try {
				// img.attr("src","");
				String imgUrl = img.attr("src");
				link = imgUrl;
				// FIXME 特殊处理
				if (imgUrl.contains("_100_100_1.jpg")) {
					imgUrl = imgUrl.replace("_100_100_1.jpg", ".jpg");
				}
				String imgMd5 = null;
				try {
					imgMd5 = MD5Util.getEncryptedPwd(imgUrl);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				String fileSuffix = "";
				if (imgUrl.contains(".")) {
					if (imgUrl.contains("?")) {
						fileSuffix = imgUrl.substring(imgUrl.lastIndexOf("."), imgUrl.lastIndexOf("?"));
					} else {
						fileSuffix = imgUrl.substring(imgUrl.lastIndexOf("."), imgUrl.length());
					}
				}
				imgMd5 += fileSuffix;
				File imgTmp = new File(new File(new File(this.imgDir, imgMd5.substring(0, 2)), imgMd5.substring(2, 4)),
						imgMd5);
				link = imgUrl;
				this.downLoadToTmpFile(imgUrl, imgTmp);

				String rPath = "." + imgTmp.getAbsolutePath().replace(this.imgDir.getParentFile().getAbsolutePath(), "")
						.replace("\\", "/");
				img.attr("src", rPath);
			} catch (Exception e) {
				log.error("{}", link, e);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		// String sUrl =
		// "https://github.com/oldj/SwitchHosts/releases/download/v3.3.11/SwitchHosts-win32-x64_v3.3.11.5347.zip";
		//
		// Stopwatch wt = Stopwatch.createStarted();
		// String fileName = sUrl.substring(sUrl.lastIndexOf("/") + 1, sUrl.length());
		// File out = new File(fileName);
		//
		// new DownLoadUtilsV2(new File("D:\\tmp\\htmls")).downLoadToTmpFile(sUrl, out,
		// true);
		// logger.info("下载完毕:{}\t{}", out.getAbsolutePath(), wt);
		HaodfMain.main(args);
	}
}
