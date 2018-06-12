package spider.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

public class DownLoadImages {
	private static Logger logger = LoggerFactory.getLogger(DownLoadImages.class);

	public static void main(String[] args) throws Exception {
		// URL url = new URL("http://120.77.101.252/pt10.tar.gz");
		// URL url = new URL(
		// "http://www.pantum.com.cn/tsinghuadatabase/upload/drive/Wisdom/Pantum%20M6700-M6800%20Series%20Windows%20Driver%20V1.3.0.zip");
		// URL url = new
		// URL("http://www.techhero.com.cn/var/upload/image/2018/02/2018022803540940827_1920x700.jpg");

		String sUrl = "https://github.com/oldj/SwitchHosts/releases/download/v3.3.11/SwitchHosts-win32-x64_v3.3.11.5347.zip";

		Stopwatch wt = Stopwatch.createStarted();
		String fileName = sUrl.substring(sUrl.lastIndexOf("/") + 1, sUrl.length());
		File out = new File(fileName);
		URL url = new URL(sUrl);
		InputStream is = url.openStream();
		FileOutputStream fos = new FileOutputStream(out);
		byte[] buf = new byte[1024 * 1024 * 1024];
		int length = 0;
		while ((length = is.read(buf)) != -1) {
			fos.write(buf, 0, length);
			logger.info("---- write: {} \t time:{}", length, wt);
		}
		fos.close();
		is.close();
		logger.info("下载完毕:{}\t{}", out.getAbsolutePath(), wt);
	}
}
