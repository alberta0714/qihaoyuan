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

import spider.main.beans.Conversation;
import spider.main.beans.Qa;
import spider.utils.DownLoadUtilsV2;
import spider.utils.JobLogUtils;

public class HaodfHongYanMain {
	static String cookies = "g=HDF.79.5b29c68b5a069; __jsluid=eae05e3a626cb3300ca144bb7d90b26c; UM_distinctid=1641b2f93a3704-0d818a8be07385-393d5f0e-100200-1641b2f93a42d0; _ga=GA1.2.1640071909.1529464461; sdmsg=1; newaskindex=1; userinfo[id]=2440151096; userinfo[name]=maggijhy; userinfo[ver]=1.0.2; userinfo[hosttype]=Doctor; userinfo[hostid]=1433501314; CNZZDATA1256706712=317343819-1529911477-https%253A%252F%252Fwww.haodf.com%252F%7C1530004233; CNZZDATA1915189=cnzz_eid%3D1316942010-1529462794-%26ntime%3D1531134171; userinfo[unreadcasecount]=10; _gid=GA1.2.566915156.1531135670; _gat=1; Hm_lvt_dfa5478034171cc641b1639b2a5b717d=1529979635,1530005512,1530152293,1531135670; userinfo[key]=BXhSYFxqA2cGbAM1CD8GZV8xBjNbbVB%2FUGhVM1dmDGYHOwRuVD4ELQUjATcIPlQ1B2oGP1pqWzlSNFBmBWcJLQ%3D%3D; userinfo[time]=1531135671; Hm_lpvt_dfa5478034171cc641b1639b2a5b717d=1531135686";
	static DownLoadUtilsV2 spider = null;
	static Charset charset = Charset.forName("UTF-8");
	static JobLogUtils logger = new JobLogUtils(HaodfHongYanMain.class);

	public static void main(String[] args) throws Exception {
		spider = new DownLoadUtilsV2(new File("D:\\tmp\\haodf"), cookies).isWithImages(true).setSleep(1000);

		Document doc = spider.getHtmlDocumentWithCache(
				"https://maggijhy.haodf.com/adminpatient/groupuser?groupId=2440156561&p_type=old&gp=1&shareType=&p=23",
				charset);
		System.out.println(doc.text());
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
		spiderConversations(qa, doc);

		String nextLink = null;
		try {
			System.out.println(doc.select(".page_turn_a").outerHtml());
			Elements tns = doc.select(".page_turn_a");
			for (int i = 0; i < tns.size(); i++) {
				Element tn = tns.get(i);
				if (tn.text().contains("下一页")) {
					nextLink = tn.attr("href");
					break;
				}
			}
			if (nextLink.startsWith("/")) {
				String host = qa.getLink().substring(0, qa.getLink().indexOf("/", 8));
				nextLink = host + nextLink;
			}
			if (StringUtils.isNotEmpty(nextLink)) {
				qa.setLink(nextLink);
				spiderDetail(qa);
			}
		} catch (Exception e) {
		}
	}

	private static void spiderConversations(Qa qa, Document doc) {
		try {
			Elements streams = doc.select(".pb20").get(0).select(".zzx_yh_stream");
			try {
				String title = doc.select(".clearfix.zzx_yh_h1 h1").text();
				if (StringUtils.isNotEmpty(title)) {
					qa.setTitle(title);
				}
			} catch (Exception e) {
			}
			List<Conversation> list = qa.getConversationList();
			for (int i = 0; i < streams.size(); i++) {
				Element stream = streams.get(i);
				Conversation con = new Conversation();
				String doctorName = null;
				String state = null;
				Conversation.AnsType type = Conversation.AnsType.assistant;
				try {
					doctorName = stream.select(".yh_l_doctor a").text();
					type = Conversation.AnsType.doctor;
					con.setDoc(doctorName);
				} catch (Exception e) {
				}
				if (StringUtils.isEmpty(doctorName)) {
					try {
						state = stream.select(".yh_l_states span").text();
						type = Conversation.AnsType.patient;
						con.setState(state);
					} catch (Exception e) {
					}
				}
				con.setType(type);
				try {
					String content = stream.select(".h_s_cons_info").get(0).outerHtml();
					qa.setQuestion(content);
					con.setContent(content);
				} catch (Exception e) {
				}
				try {
					// String level = stream.select(".yh_l_doctor").get(0).text();
					// System.out.println(level);
				} catch (Exception e) {
				}
				try {
					String content = stream.select(".h_s_cons").get(0).outerHtml();
					con.setContent(content);
				} catch (Exception e) {
				}
				try {
					String content = stream.select(".h_s_cons_docs").get(0).outerHtml();
					con.setContent(content);
				} catch (Exception e) {
				}
				try {
					con.setDate(stream.select(".h_s_time ").get(0).text());
				} catch (Exception e) {
				}
				try {
					con.setPatient(stream.select(".yh_l_huan").get(0).text());
				} catch (Exception e) {
				}
				try {
					con.setImg(stream.select(".yh_l_pics").get(0).text());
				} catch (Exception e) {
				}
				list.add(con);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}