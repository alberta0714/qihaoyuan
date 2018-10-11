package spider.sss;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import spider.utils.DownLoadUtils;
import spider.utils.JobLogUtils;

public class Teensfromtokyo {

	public static void main(String[] args) {
		String url = "https://images.asiansex.pics/p/teensfromtokyo{c}/{id}.jpg";
		ExecutorService pool = Executors.newFixedThreadPool(10);
		for (int c = 1; c < 19999; c++) {
			pool.execute(new TeensfromtokyoDwn(url, Integer.toString(c), Teensfromtokyo.class.getName()));
		}
		pool.shutdown();
	}
}

class TeensfromtokyoDwn implements Runnable {
	private static JobLogUtils logger = new JobLogUtils(JapanhandjobDwn.class);
	private static DownLoadUtils spider = null;
	private String c, url;
	String dirName;

	public TeensfromtokyoDwn(String uri, String c, String dirName) {
		this.c = c;
		this.url = uri;
		this.dirName = dirName;
		spider = new DownLoadUtils(new File("D:\\tmp", this.dirName));
	}

	@Override
	public void run() {
		for (int i = 1; i < 99999; i++) {
			// String uri = url.replace("{c}", c).replace("{id}", String.format("%02d", i));
			String uri = url.replace("{c}", c).replace("{id}", Integer.toString(i));
			logger.info("spider: {}", uri);

			// File f = new File(new File("D:\\sex\\", dirName), c + "_" +
			// String.format("%02d", i) + ".jpg");
			File f = new File(new File("D:\\sex\\", dirName), c + "_" + Integer.toString(i) + ".jpg");
			int code = spider.downLoadToTmpFile(uri, f, false);
			if (code == 404 || code == 302) {
				logger.warn(" X404XXX {} {}", code, uri);
				break;
			}
			logger.info("{} {}", code, uri);
		}
	}
}
