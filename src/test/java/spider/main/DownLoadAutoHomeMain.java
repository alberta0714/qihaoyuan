package spider.main;

import java.io.File;
import java.nio.charset.Charset;

import org.jsoup.nodes.Document;

import com.google.common.base.Stopwatch;

import spider.utils.DownLoadUtils;
import spider.utils.JobLogUtils;

public class DownLoadAutoHomeMain {
	private static JobLogUtils logger = new JobLogUtils(DownLoadAutoHomeMain.class).setJobName("SPIDER");
	private static DownLoadUtils spider = new DownLoadUtils(new File("D:\\tmp\\cars\\htmls"));

	public static void main(String[] args) {
		logger.info("任务开始");
		Stopwatch wt = Stopwatch.createStarted();
		Document doc = spider.getHtmlDocumentWithCache(
				"https://jiage.autohome.com.cn/price/carlist/p-32456-1-0-0-110000-110100-1", Charset.forName("UTF-8"));

		System.out.println(doc);
		
		logger.info("完成 {}", wt);
	}
}
