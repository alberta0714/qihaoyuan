package spider.haodf.main;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import spider.haodf.bean.PageItem;
import spider.haodf.bean.PageListBean;
import spider.haodf.bean.PatientProfile;
import spider.main.beans.Conversation;
import spider.main.beans.Qa;
import spider.utils.DownLoadUtilsV2;
import spider.utils.JobLogUtils;

public class HaodfHongYanMain {
	static String cookies = "g=HDF.79.5b29c68b5a069; UM_distinctid=1641b2f93a3704-0d818a8be07385-393d5f0e-100200-1641b2f93a42d0; _ga=GA1.2.1640071909.1529464461; sdmsg=1; newaskindex=1; userinfo[id]=2440151096; userinfo[name]=maggijhy; userinfo[ver]=1.0.2; userinfo[hosttype]=Doctor; userinfo[hostid]=1433501314; __jsluid=67c6d0d0dce5d6c607403d449e464a69; _gid=GA1.2.566915156.1531135670; _telorder_lastview_id=6398301038; CNZZDATA1256706712=598709849-1530004233-https%253A%252F%252Fwww.haodf.com%252F%7C1531219199; d_app_ban1_2440151096=0; d_app_ban2_2440151096=0; d_app_ban3_2440151096=0; userinfo[unreadcasecount]=11; Hm_lvt_dfa5478034171cc641b1639b2a5b717d=1531135670,1531137700,1531221204,1531307008; _gat=1; userinfo[key]=USwDMVZgAWVWPAk%2FBjFaOV8xUWRRZwEuBDwHYVNiD2VSblY8UjhReAIkU2UIPlMyAG1SawU1VzVRN1BmDG5afg%3D%3D; userinfo[time]=1531307127; Hm_lpvt_dfa5478034171cc641b1639b2a5b717d=1531307129";
	static DownLoadUtilsV2 spider = null;
	static Charset charset = Charset.forName("UTF-8");
	static JobLogUtils logger = new JobLogUtils(HaodfHongYanMain.class);

	public static void main(String[] args) throws Exception {
		spider = new DownLoadUtilsV2(new File("D:\\tmp\\haodf"), cookies).isWithImages(true).setSleep(1000);

		List<PageListBean> list = new ArrayList<PageListBean>();
		int pages = 77;
		for (int i = 1; i <= pages; i++) {
			PageListBean pageBean = new PageListBean();
			list.add(pageBean);
			String pageUrl = "https://maggijhy.haodf.com/adminpatient/signinpatient?shareType=&p={page}"
					.replace("{page}", Integer.toString(i));
			pageBean.setPageLink(pageUrl);
			// logger.info(pageUrl);
			processMainPage(pageBean, pageUrl);
		}
	}

	private static void processMainPage(PageListBean pageBean, String pageUrl) {
		// 组装 页面 项
		try {
			Document doc = spider.getHtmlDocumentWithCache(pageUrl, charset);
			Element section = doc.select("#formqunfa").get(0);
			section.select("td[rowspan]").remove();
			Elements rows = section.select("tr");
			for (int cur = 0; cur < rows.size(); cur++) {
				PageItem item = new PageItem();
				Element tr = rows.get(cur);
				Elements tds = tr.select("td");
				if (tds.size() == 5) {
					item.setUserName(tds.get(0).text());
					item.setBaseInfo(tds.get(1).text());
					item.setDiseases(tds.get(2).text());
					item.setDetailLink("https://maggijhy.haodf.com" + tds.get(4).select("a").get(0).attr("href"));
					item.setInteractLink("https://maggijhy.haodf.com" + tds.get(4).select("a").get(1).attr("href"));
				}
				pageBean.getPageItemList().add(item);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 抓取患者详情页
		for (PageItem item : pageBean.getPageItemList()) {
			try {
				if (item.getDetailLink() == null || !item.getDetailLink().contains("patientId")) {
					continue;
				} else {
				}
				String link = item.getDetailLink();
				String patientId = link.substring(link.lastIndexOf("=") + 1, link.length());
				String uri = "https://maggijhy.haodf.com/adminpatient/patientupload?patientCaseId=&patientId="
						+ patientId;
				Document doc = spider.getHtmlDocumentWithCache(uri, charset);
				processPatientProfile(item, doc);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// 进入病例
		for (PageItem item : pageBean.getPageItemList()) {
			PatientProfile pro = item.getPatientProfile();
			System.out.println(pro);
		}
		
		// 抓取互动
		
	}

	private static void processPatientProfile(PageItem item, Document doc) {
		Elements tds = doc.select(".visited_superman").get(0).select("tr td");
		// 病例详情链接
		// 互动记录
		// 手机号 出生年月
		PatientProfile profile = new PatientProfile();
		item.setPatientProfile(profile);
		for (int i = 0; i < tds.size(); i++) {
			Element td = tds.get(i);
			if (td.text().contains("出生日期")) {
				profile.setBirthDay(tds.get(i + 1).text());
				i++;
				continue;
			} else if (td.text().contains("联系方式")) {
				profile.setPhone(tds.get(i + 1).text());
				i++;
				continue;
			} else if (td.text().contains("患者上传的病历")) {
				String lk = tds.get(i + 1).select("a").get(0).attr("href");
				if (lk.startsWith("java")) {
					lk = null;
				} else if (lk.startsWith("/")) {
					lk = "https://maggijhy.haodf.com" + lk;
				}
				profile.setCaseLink(lk);
				i++;
				continue;
			} else if (td.text().contains("医患互动记录")) {
				String lk = tds.get(i + 1).select("a").get(0).attr("href");
				if (lk.startsWith("java")) {
					lk = null;
				} else if (lk.startsWith("/")) {
					lk = "https://maggijhy.haodf.com" + lk;
				}
				profile.setInteractLink(lk);
				i++;
				continue;
			}
		}
	}
}
