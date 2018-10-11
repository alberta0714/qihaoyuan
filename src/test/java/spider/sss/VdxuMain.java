package spider.sss;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import spider.utils.DownLoadUtils;
import spider.utils.JobLogUtils;

public class VdxuMain {
	private static JobLogUtils logger = new JobLogUtils(VdxuMain.class);

	public static void main(String[] args) {
		// https://xpu.vdxuxb.club:8067/pages/1150/index.html
		String url = "http://xying.tianxinlicai.com/mp4/{c}/{id}.MP4";
		ExecutorService pool = Executors.newFixedThreadPool(5);
		for (int c = 2; c < 19999; c++) {
			pool.execute(new Dwn3(url, Integer.toString(c)));
		}
		pool.shutdown();
	}
}

class Dwn3 implements Runnable {
	private static final JobLogUtils logger = new JobLogUtils(Dwn3.class);
	private static final DownLoadUtils spider = new DownLoadUtils(new File("D:\\tmp\\mp4"));
	private String c, url;

	public Dwn3(String uri, String c) {
		this.c = c;
		this.url = uri;
	}

	@Override
	public void run() {
		for (int i = 1; i < 19999; i++) {
			String uri = url.replace("{c}", c).replace("{id}", Integer.toString(i));
			logger.info("spider: {}", uri);
			File f = new File("D:\\sex\\mp4", c + "_" + i + ".mp4");
			int code = spider.downLoadToTmpFile(uri, f, false);
			if (code == 404 || code == 302) {
				logger.warn("X404XXX {} {}", code, uri);
				break;
			}
			logger.info("compeleted {}", uri);

		}
	}
}