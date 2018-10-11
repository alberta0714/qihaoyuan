package spider.sss;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import spider.utils.DownLoadUtils;
import spider.utils.JobLogUtils;

public class CqbrightMain {
	private static JobLogUtils logger = new JobLogUtils(CqbrightMain.class).setJobName("SPIDER");

	public static void main(String[] args) {
		ExecutorService pool = Executors.newFixedThreadPool(5);
		// https://movie.cqbright.net:890/index.html
		String url = "http://cdn.yxdzkt.net:36150/html5/xin/vip{c}/{id}.mp4";
		for (int c = 2; c < 20; c++) {
			pool.execute(new Dwn2(url, Integer.toString(c)));
		}
		pool.shutdown();
	}

}

class Dwn2 implements Runnable {
	private static final JobLogUtils logger = new JobLogUtils(Dwn2.class);
	private static final DownLoadUtils spider = new DownLoadUtils(new File("D:\\tmp\\mp42"));
	private String c, url;

	public Dwn2(String uri, String c) {
		this.c = c;
		this.url = uri;
	}

	@Override
	public void run() {
		for (int i = 1; i < 19999; i++) {
			String uri = url.replace("{c}", c).replace("{id}", Integer.toString(i));
			logger.info("spider: {}", uri);
			File f = new File("D:\\sex\\mp42", c + "_" + i + ".mp4");
			int code = spider.downLoadToTmpFile(uri, f, false);
			if (code == 404 || code == 302) {
				logger.warn("X404XXX {} {}", code, uri);
				break;
			}
			logger.info("compeleted {}", uri);
		}
	}
}
