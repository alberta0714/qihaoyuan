package spider.haodf.main;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import spider.haodf.bean.InteractBean;
import spider.haodf.bean.PageItem;
import spider.haodf.bean.PageListBean;
import spider.haodf.bean.PatientProfile;
import spider.main.beans.Conversation;
import spider.utils.DownLoadUtilsV2;
import spider.utils.JobLogUtils;

public class GanjiMain {
	static String cookies = "statistics_clientid=me; ganji_xuuid=a89fb2b9-7326-4d10-edc2-989b9fa8ce1d.1532332821163; ganji_uuid=2822131025933727926986; _gl_tracker=%7B%22ca_source%22%3A%22www.baidu.com%22%2C%22ca_name%22%3A%22-%22%2C%22ca_kw%22%3A%22-%22%2C%22ca_id%22%3A%22-%22%2C%22ca_s%22%3A%22seo_baidu%22%2C%22ca_n%22%3A%22-%22%2C%22ca_i%22%3A%22-%22%2C%22sid%22%3A57621264694%7D; cityDomain=bj; _wap__utmganji_wap_newCaInfo_V2=%7B%22ca_n%22%3A%22-%22%2C%22ca_s%22%3A%22self%22%2C%22ca_i%22%3A%22-%22%7D; xxzl_deviceid=lQ3B6LCN4znnrTw3essQxuMYHku9tiacR0FMh4UUj%2BDbEqRjy5mfSqFHnPWVMqru; lg=1; EXPAND_LIST_FILTER=1; username_login_n=15210187765; GanjiLoginType=0; xxzl_smartid=64c4e7e7ffa9101723410c15e3922e87; citydomain=gz; Hm_lvt_655ab0c3b3fdcfa236c3971a300f3f29=1532333200; Hm_lvt_acb0293cec76b2e30e511701c9bf2390=1532333201; Hm_lvt_8da53a2eb543c124384f1841999dcbb8=1532333217; __utmganji_v20110909=a0ce105e-fcd3-4436-a385-3c5e43071894; Hm_lvt_6f6548400acfbcc44a302e67525049f7=1532335361; Hm_lpvt_6f6548400acfbcc44a302e67525049f7=1532335361; __utma=32156897.347269043.1532335361.1532335361.1532335361.1; __utmc=32156897; __utmz=32156897.1532335361.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); Hm_lpvt_655ab0c3b3fdcfa236c3971a300f3f29=1532337255; mobversionbeta=3g; _wap__utmganji_wap_caInfo_V2=%7B%22ca_name%22%3A%22-%22%2C%22ca_source%22%3A%22-%22%2C%22ca_id%22%3A%22-%22%2C%22ca_kw%22%3A%22-%22%7D; firstopen=on; cainfo=%7B%22ca_source%22%3A%22-%22%2C%22ca_name%22%3A%22-%22%7D; GANJISESSID=aescs6hptan0mb8hlakr49lktl; sscode=LH0ydXwYClrQr5yiLHzOSiXP; GanjiEmail=sunzhanchao%40126.com; GanjiUserName=sufernet; GanjiUserInfo=%7B%22user_id%22%3A73944299%2C%22email%22%3A%22sunzhanchao%40126.com%22%2C%22username%22%3A%22sufernet%22%2C%22user_name%22%3A%22sufernet%22%2C%22nickname%22%3A%22beufvw409%22%7D; bizs=%5B%5D; supercookie=AmZ5AQDlBGxxBGNjZmR3Lwt5ZTExMwywAwp2ZTHjLzRmAwuzBGOuZJAvMQL2ZGAzMD%3D%3D; ganji_login_act=1532337543155; Hm_lpvt_8da53a2eb543c124384f1841999dcbb8=1532337543; Hm_lpvt_acb0293cec76b2e30e511701c9bf2390=1532337543; __utmb=32156897.25.10.1532335361";
	static DownLoadUtilsV2 spider = null;
	static Charset charset = Charset.forName("UTF-8");
	static JobLogUtils logger = new JobLogUtils(GanjiMain.class);
	static File baseDir = new File("D:\\tmp\\ganji");

	static int cur = 0;

	public static void main(String[] args) throws Exception {
		spider = new DownLoadUtilsV2(baseDir, cookies).isWithImages(true).setSleep(1000 * 3);
		for (int i = 1; i <= 109; i++) {
			try {
				String lk = "http://gz.ganji.com/qzshichangyingxiao/s1o" + i + "/";
				System.out.println(lk);
				Document doc = spider.getHtmlDocumentWithCache(lk, charset, true);
				Elements list = doc.select(".qz-resume-list").select("dl");
				for (Element item : list) {
					processDetail(item);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void processDetail(Element item) {
		try {
			String link = "http://gz.ganji.com" + item.select("a").get(0).attr("href");
			Document doc = spider.getHtmlDocumentWithCache(link, charset, true);
			System.out.println(link);
			System.out.println(doc.outerHtml());

			String imgSrc = doc.select(".photocon div div").get(0).attr("data-imgs");
			imgSrc = imgSrc.substring(imgSrc.indexOf("[\"") + 2, imgSrc.indexOf("\"]"));
			imgSrc = imgSrc.replace("\"", "");
			imgSrc = imgSrc.replace("\\/", "/");
			String[] imgs = imgSrc.split(",");
			for (String img : imgs) {
				img = img.substring(0, img.indexOf("_")) + ".jpg";
				cur++;
				File imgDir = new File(baseDir, "data");
				imgDir.mkdirs();
				spider.downLoadToTmpFile(img, new File(imgDir, cur + ".jpg"));
			}
		} catch (Exception e) {
		}
	}

	public static String getHost(String url) {
		if (url.contains("://") && url.contains(".")) {
			try {
				String tmp = url.substring(url.indexOf("://") + 3, url.length());
				int endIndex = tmp.indexOf("/");
				if (endIndex != -1) {
					tmp = tmp.substring(0, endIndex);
				}
				tmp = url.substring(0, url.indexOf("://") + 3) + tmp;
				return tmp;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return url;
	}
}