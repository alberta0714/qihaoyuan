package spider.main;

import java.io.File;
import java.nio.charset.Charset;

import org.jsoup.nodes.Document;

import spider.utils.DownLoadUtilsV2;

public class TestLogin {
	public static void main(String[] args) throws Exception {
		String uri = "https://boss.chengxz.com/index";
		String cookies = "Hm_lvt_2c7d665f2deeec5a76e00df14a7a45a0=1529636690,1529648504,1529648982,1529651235; JSESSIONID=4f7722a1-996f-4e20-a32e-f4bad6d952b4";
		DownLoadUtilsV2 spider = new DownLoadUtilsV2(new File("D:\\tmp\\haodf"), cookies).isWithImages(true)
				.setSleep(1000);
		Document doc = spider.getHtmlDocumentWithCache(uri, Charset.forName("UTF-8"));
		System.out.println(doc.outerHtml());
	}
}
