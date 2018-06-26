package spider.main;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import spider.main.beans.Qa;
import spider.utils.DownLoadUtilsV2;
import spider.utils.JobLogUtils;

public class HaodfMain {
	static String cookies = "g=HDF.79.5b29c68b5a069; UM_distinctid=1641b2f93a3704-0d818a8be07385-393d5f0e-100200-1641b2f93a42d0; _ga=GA1.2.1640071909.1529464461; __jsluid=33ec294a20eb0a336bb997088890b2e8; _gid=GA1.2.197721853.1529893817; Hm_lvt_dfa5478034171cc641b1639b2a5b717d=1529464461,1529893817; PHPSESSID=cl29o8bggj5hllcfdm5kimatn5; CNZZDATA1915189=cnzz_eid%3D702026912-1529462794-https%253A%252F%252Fluodianzhong.haodf.com%252F%26ntime%3D1529893590; sdmsg=1; userinfo[id]=3348029546; userinfo[name]=hdfvvk8n; userinfo[key]=VShSYQIzXDgFZwA3ADAFbgRvBj5SZFR7Aj9bOAU1XCcNLgNoVG5Va1VzCT5TeA%3D%3D; userinfo[time]=1529895899; userinfo[ver]=1.0.2; userinfo[hostid]=0; CNZZDATA1256706712=373824218-1529895154-https%253A%252F%252Fluodianzhong.haodf.com%252F%7C1529895154; _gat=1; Hm_lpvt_dfa5478034171cc641b1639b2a5b717d=1529896466";
	static DownLoadUtilsV2 spider = null;
	static Charset charset = Charset.forName("UTF-8");
	static JobLogUtils logger = new JobLogUtils(HaodfMain.class);

	public static void main(String[] args) throws Exception {
		spider = new DownLoadUtilsV2(new File("D:\\tmp\\haodf"), cookies).isWithImages(true).setSleep(1000);

		String pageUri = "https://luodianzhong.haodf.com/zixun/list.htm?type=&p={page}";
		int pages = 118;
		for (int i = 1; i <= pages; i++) {
			String url = pageUri.replace("{page}", Integer.toString(i));
			Document doc = spider.getHtmlDocumentWithCache(url, charset);
			List<Qa> list = new ArrayList<Qa>();
			spiderPage(doc, list);
			// System.out.println(i);
			logger.info("spider page url {}, size:{}", url, list.size());
			break;// TODO
		}
	}

	private static void spiderPage(Document doc, List<Qa> qaList) {
		Element list = doc.select(".zixun_list").get(0);
		Elements rows = list.select("tr");
		for (int i = 1; i < rows.size(); i++) {
			Element tr = rows.get(i);
			try {
				Qa qa = new Qa();
				qa.setPatient(tr.select("td").get(1).select("p").get(0).html());
				qa.setLink(tr.select("td").get(2).select("a").get(0).attr("href"));
				qa.setTitle(tr.select("td").get(2).select("a").get(0).html());
				qa.setRelatedDiseases(tr.select("td").get(3).text());
				qa.setLastUpdate(tr.select("td").get(5).select("span").get(0).text());
				// qa.setLastUpdateBy(tr.select("td").get(5).select("span").get(1).text());
				spiderDetail(qa);
				qaList.add(qa);
				// System.out.println(qa);
				break;
			} catch (Exception e) {
				logger.warn(tr.outerHtml());
				System.out.println("========");
				continue;
			}
		}
	}

	private static void spiderDetail(Qa qa) throws IOException {
		logger.info("spider page {}", qa.getLink());
		Document doc = spider.getHtmlDocumentWithCache(qa.getLink(), charset);
		try {
			doc.select(".mt20.w670.bg_w.zzx_t_repeat").remove();
		} catch (Exception e) {
			logger.warn("", e);
		}
		
		Elements streams = doc.select(".pb20").get(0).select(".zzx_yh_stream");
		try {
		} catch (Exception e) {
		}
		try {
		} catch (Exception e) {
		}
		try {
		} catch (Exception e) {
		}
		try {
		} catch (Exception e) {
		}
		try {
		} catch (Exception e) {
		}
		try {
		} catch (Exception e) {
		}
		try {
		} catch (Exception e) {
		}
		try {
		} catch (Exception e) {
		}
		try {
		} catch (Exception e) {
		}
		try {
		} catch (Exception e) {
		}
		try {
		} catch (Exception e) {
		}
		try {
		} catch (Exception e) {
		}

	}
}