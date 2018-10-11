package spider.sss;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import spider.utils.DownLoadUtils;
import spider.utils.JobLogUtils;

public class JapanhdvMain {
	private static JobLogUtils logger = new JobLogUtils(JapanhdvMain.class).setJobName("SPIDER");

	public static void main(String[] args) {
		String url = "https://asiansex.pics/p/japanhdv{c}/{id}.jpg";
		ExecutorService pool = Executors.newFixedThreadPool(5);
		for (int c = 2; c < 19999; c++) {
			pool.execute(new Dwn(url, Integer.toString(c)));
		}
		pool.shutdown();
	}
}

class Dwn implements Runnable {
	private static JobLogUtils logger = new JobLogUtils(Dwn.class).setJobName("Dwn");
	private static DownLoadUtils spider = new DownLoadUtils(new File("D:\\tmp\\japand"));
	private String c, url;

	public Dwn(String uri, String c) {
		this.c = c;
		this.url = uri;
	}

	@Override
	public void run() {
		for (int i = 1; i < 100; i++) {
			String uri = url.replace("{c}", c).replace("{id}", String.format("%02d", i));
			logger.info("spider: {}", uri);

			File f = new File("D:\\sex\\pic_japanhd", c + "_" + String.format("%02d", i) + ".jpg");
			int code = spider.downLoadToTmpFile(uri, f, false);
			if (code == 404 || code == 302) {
				logger.warn(" X404XXX {} {}", code, uri);
				break;
			}
			logger.info("{} {}", code, uri);
		}
	}
}
