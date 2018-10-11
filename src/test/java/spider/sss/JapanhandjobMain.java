package spider.sss;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import spider.utils.DownLoadUtils;
import spider.utils.JobLogUtils;

public class JapanhandjobMain {

	public static void main(String[] args) {
		String url = "https://asiansex.pics/p/handjobjapan{c}/{id}.jpg";
		ExecutorService pool = Executors.newFixedThreadPool(10);
		for (int c = 1; c < 19999; c++) {
			pool.execute(new JapanhandjobDwn(url, Integer.toString(c), "handjobjapan"));
		}
		pool.shutdown();
	}
}

class JapanhandjobDwn implements Runnable {
	private static JobLogUtils logger = new JobLogUtils(JapanhandjobDwn.class);
	private static DownLoadUtils spider = null;
	private String c, url;
	String dirName;

	public JapanhandjobDwn(String uri, String c, String dirName) {
		this.c = c;
		this.url = uri;
		this.dirName = dirName;
		spider = new DownLoadUtils(new File("D:\\tmp", this.dirName));
	}

	@Override
	public void run() {
		for (int i = 1; i < 100; i++) {
			String uri = url.replace("{c}", c).replace("{id}", String.format("%02d", i));
			logger.info("spider: {}", uri);

			File f = new File(new File("D:\\sex\\", dirName), c + "_" + String.format("%02d", i) + ".jpg");
			int code = spider.downLoadToTmpFile(uri, f, false);
			if (code == 404 || code == 302) {
				logger.warn(" X404XXX {} {}", code, uri);
				break;
			}
			logger.info("{} {}", code, uri);
		}
	}
}
