package spider.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

public class DownLoadUtils {
	private static Logger logger = LoggerFactory.getLogger(DownLoadUtils.class);
	JobLogUtils log = new JobLogUtils("job", DownLoadUtils.class);

	static File tmpDir = new File("D:\\tmp\\htmls");
	static File imgDir = new File(tmpDir.getParentFile(), "imgs");

	public DownLoadUtils(File tmpDirecory) {
		tmpDir = tmpDirecory;
	}

	static {
		// 处理https的请求
		Protocol myhttps = new Protocol("https", new MySSLSocketFactory(), 443);
		Protocol.registerProtocol("https", myhttps);
	}

	public Document getHtmlDocumentWithCache(String itemUrl, Charset charset) {
		return Jsoup.parse(this.getHtmlBodyWithCache(itemUrl, charset));
	}

	public String getHtmlBodyWithCache(String itemUrl, Charset charset) {
		return getHtmlBodyWithCache(itemUrl, charset, false);
	}

	public Document getHtmlDocumentWithCache(String itemUrl, Charset charset, boolean overWrite) {
		return Jsoup.parse(this.getHtmlBodyWithCache(itemUrl, charset, overWrite));
	}

	public String getHtmlBodyWithCache(String itemUrl, Charset charset, boolean overWrite) {
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
		// try {
		// return IOUtils.read(new InputStreamReader(new FileInputStream(tmp),
		// charset));
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		try {
			return FileUtils.readFileToString(tmp, charset);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void downLoadToTmpFile(String itemUrl, File tmp) {
		downLoadToTmpFile(itemUrl, tmp, false);
	}

	public void downLoadToTmpFile(String itemUrl, File tmp, boolean overWrite) {
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
		log.info("正在下载: {}", itemUrl);
		byte[] bodyBytes = null;
		HttpClient client = new HttpClient();
		HttpMethod m = new GetMethod(itemUrl);
		String uae = "Mozilla/5.0 (compatible; Baiduspider/2.0;+http://www.baidu.com/search/spider.html) ";
		m.setRequestHeader("User-Agent", uae);
		try {
			client.executeMethod(m);
			bodyBytes = m.getResponseBody();
		} catch (Exception e) {
			e.printStackTrace();
			log.warn("下载异常:{}", itemUrl);
		} finally {
			try {
				m.releaseConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

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

	public static void main(String[] args) {
		String sUrl = "https://github.com/oldj/SwitchHosts/releases/download/v3.3.11/SwitchHosts-win32-x64_v3.3.11.5347.zip";

		Stopwatch wt = Stopwatch.createStarted();
		String fileName = sUrl.substring(sUrl.lastIndexOf("/") + 1, sUrl.length());
		File out = new File(fileName);

		new DownLoadUtils(new File("D:\\tmp\\htmls")).downLoadToTmpFile(sUrl, out, true);
		logger.info("下载完毕:{}\t{}", out.getAbsolutePath(), wt);
	}
}
