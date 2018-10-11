package spider.sss;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import spider.utils.DownLoadUtils;
import spider.utils.JobLogUtils;

public class Uralesbian {

	public static void main(String[] args) {
		String url = "https://asiansex.pics/p/uralesbian{c}/{id}.jpg";
		// url = "https://asiansex.pics/p/teenfilipina_{c}/{id}.jpg";
		url = "https://asiansex.pics/p/asiansexdiary_{c}/{id}.jpg";

		ExecutorService pool = Executors.newFixedThreadPool(10);
		for (int c = 1; c < 19999; c++) {
			pool.execute(new UralesbianDwn(url, Integer.toString(c), "asiansexdiary", true));
		}
		pool.shutdown();
	}
}

class UralesbianDwn implements Runnable {
	private static JobLogUtils logger = new JobLogUtils(JapanhandjobDwn.class);
	private static DownLoadUtils spider = null;
	private String c, url;
	String dirName;
	boolean ifTwo;

	public UralesbianDwn(String uri, String c, String dirName, boolean ifTwo) {
		this.c = c;
		this.url = uri;
		this.dirName = dirName;
		spider = new DownLoadUtils(new File("D:\\tmp", this.dirName));
		this.ifTwo = ifTwo;
	}

	@Override
	public void run() {
		int max = 999999;
		if (ifTwo) {
			max = 100;
		}
		for (int i = 1; i < max; i++) {
			String uri = null;
			if (ifTwo) {
				uri = url.replace("{c}", c).replace("{id}", String.format("%02d", i));
			} else {
				uri = url.replace("{c}", c).replace("{id}", Integer.toString(i));
			}
			logger.info("spider: {}", uri);

			File f = new File(new File("D:\\sex\\", dirName), c + "_" + String.format("%02d", i) + ".jpg");
			// File f = new File(new File("D:\\sex\\", dirName), c + "_" +
			// Integer.toString(i) + ".jpg");
			int code = spider.downLoadToTmpFile(uri, f, false);
			if (code == 404 || code == 302) {
				logger.warn(" X404XXX {} {}", code, uri);
				break;
			}
			logger.info("{} {}", code, uri);
		}
	}
}
