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

public class HaodfHongYanMain {
	static String cookies = "g=HDF.79.5b29c68b5a069; UM_distinctid=1641b2f93a3704-0d818a8be07385-393d5f0e-100200-1641b2f93a42d0; _ga=GA1.2.1640071909.1529464461; sdmsg=1; newaskindex=1; userinfo[id]=2440151096; userinfo[name]=maggijhy; userinfo[ver]=1.0.2; userinfo[hosttype]=Doctor; userinfo[hostid]=1433501314; __jsluid=67c6d0d0dce5d6c607403d449e464a69; _telorder_lastview_id=6398301038; CNZZDATA1256706712=598709849-1530004233-https%253A%252F%252Fwww.haodf.com%252F%7C1532077819; userinfo[unreadcasecount]=12; _gid=GA1.2.977029647.1532308921; _gat=1; Hm_lvt_dfa5478034171cc641b1639b2a5b717d=1531307008,1531913231,1532078058,1532308921; Hm_lpvt_dfa5478034171cc641b1639b2a5b717d=1532308921; userinfo[key]=VShQYlRiUDRUPgk%2FUWZWNVQ6AzZSZFt0AzsGYFBhC2EBPQFrBW8HLldxBTMFMwJjUTxRaFRkBWdQNgcxBWdafg%3D%3D; userinfo[time]=1532308918";
	static DownLoadUtilsV2 spider = null;
	static Charset charset = Charset.forName("UTF-8");
	static JobLogUtils logger = new JobLogUtils(HaodfHongYanMain.class);
	static File baseDir = new File("D:\\tmp\\haodf");

	public static void main(String[] args) throws Exception {
		spider = new DownLoadUtilsV2(baseDir, cookies).isWithImages(true).setSleep(1000);
		List<PageListBean> list = new ArrayList<PageListBean>();
		// int pages = 77;
		int pages = 77;
		for (int i = 1; i <= pages; i++) {
			PageListBean pageBean = new PageListBean();
			list.add(pageBean);
			String pageUrl = "https://maggijhy.haodf.com/adminpatient/signinpatient?shareType=&p={page}"
					.replace("{page}", Integer.toString(i));
			logger.info("======= 正在采集分页:" + pageUrl);
			pageBean.setPageLink(pageUrl);
			// logger.info(pageUrl);
			processMainPage(pageBean, pageUrl);
		}
		//

		File data = new File(baseDir, "data");
		if (!data.exists()) {
			data.mkdirs();
		}
		int usernameCount = 0;
		for (PageListBean page : list) {
			for (PageItem item : page.getPageItemList()) {
				String username = item.getUserName();
				usernameCount++;
				System.out.println(usernameCount + "\t:\t" + username);
				File userDir = new File(data, filterCharByFileName(username));
				if (!userDir.exists()) {
					if (!userDir.mkdirs()) {
						logger.error("创建目录失败");
					}
				}
				// 基本信息
				File baseInfoFile = new File(userDir, "用户基本信息.txt");
				if (baseInfoFile.exists()) {
					baseInfoFile.delete();
				}
				StringBuffer baseInfo = new StringBuffer();
				baseInfo.append(item.getUserName()).append("\t");
				baseInfo.append(item.getBaseInfo()).append("\t");
				if (item.getPatientProfile() != null) {
					baseInfo.append(item.getPatientProfile().getBirthDay()).append("\t");
					baseInfo.append(item.getPatientProfile().getPhone()).append("\t");
				}
				baseInfo.append(item.getDiseases()).append("\t");
				FileUtils.write(baseInfoFile, baseInfo.toString());
				// 病例
				if (CollectionUtils.isNotEmpty(item.getCaseLinkList())) {
					File caseDir = new File(userDir, "病例图片");
					if (!caseDir.exists()) {
						if (!caseDir.mkdirs()) {
							logger.error("创建目录失败");
						}
					}
					for (String lk : item.getCaseLinkList()) {
						if (lk.startsWith("./")) {
							lk = lk.substring(2, lk.length());
						}
						File imgFile = new File(baseDir, lk);
						File cpTo = new File(caseDir, imgFile.getName());
						if (cpTo.exists()) {
							continue;
						}
						try {
							FileUtils.copyFile(imgFile, cpTo);
						} catch (Exception e) {
							logger.error("[{}] to [{}]", imgFile.getAbsolutePath(), cpTo.getAbsolutePath(), e);
						}
					}
				}
				// 对话过程
				List<InteractBean> interActList = item.getInterActList();
				if (CollectionUtils.isNotEmpty(interActList)) {
					File actDir = new File(userDir, "对话记录");
					if (!actDir.exists()) {
						actDir.mkdirs();
					}
					buildExcel(interActList, actDir);
				}
			}
		}
	}

	private static void buildExcel(List<InteractBean> interActList, File actDir) throws IOException {
		for (InteractBean bean : interActList) {
			HSSFWorkbook workbook = null;
			try {
				workbook = new HSSFWorkbook();
				File actFile = new File(actDir, filterCharByFileName(bean.getTitle()) + ".xls");
				if (actFile.exists()) {
					actFile.delete();
				}
				actFile.createNewFile();

				HSSFSheet sheet1 = workbook.createSheet("患者对话");
				HSSFCellStyle dataCellStyle = workbook.createCellStyle();
				for (int i = 0; i < bean.getConversationList().size(); i++) {
					Conversation qa = bean.getConversationList().get(i);
					HSSFRow row = sheet1.createRow(i);
					row.setRowStyle(dataCellStyle);

					row.createCell(0).setCellValue(qa.getType().toString());
					row.createCell(1).setCellValue(qa.getDate());
					String content = qa.getContent();
					row.createCell(2).setCellValue(Jsoup.parse(content).text());
				}
				workbook.write(actFile);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					workbook.close();
				} catch (Exception e) {
				}
			}
		}
	}

	static String filterCharByFileName(String input) {
		char[] filterChs = new char[] { '\\', '/', ':', '*', '?', '"', '<', '>', '|', '.' };
		StringBuilder sb = new StringBuilder();
		for (char ch : input.toCharArray()) {
			boolean flag = false;
			for (char fl : filterChs) {
				if (ch == fl) {
					flag = true;
					break;
				}
			}
			if (flag) {
				continue;
			}
			sb.append(ch);
		}
		return sb.toString();
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
					pageBean.getPageItemList().add(item);
				}
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
				logger.info("患者详情:{}", link);
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
			if (pro == null) {
				continue;
			}
			String link = pro.getCaseLink();
			if (StringUtils.isEmpty(link)) {
				continue;
			}
			try {
				logger.info("正在采集病例:{}", link);
				Document caseDoc = spider.getHtmlDocumentWithCache(link, charset);
				Elements imgs = caseDoc.select(".picList").get(0).select("img");
				List<String> caseLinkList = new ArrayList<String>();
				item.setCaseLinkList(caseLinkList);
				for (Element img : imgs) {
					caseLinkList.add(img.attr("src"));
				}
			} catch (Exception e) {
				try {
					// FIXME 尝试处理 文字 形式的病例
				} catch (Exception e2) {
					logger.error("", e2);
				}
			}
		}
		// 抓取互动
		for (PageItem item : pageBean.getPageItemList()) {
			PatientProfile pro = item.getPatientProfile();
			if (pro == null) {
				continue;
			}
			String link = pro.getInteractLink();
			if (StringUtils.isEmpty(link)) {
				continue;
			}
			// 获取互动列表
			try {
				List<InteractBean> list = new ArrayList<>();
				item.setInterActList(list);
				logger.info("采集互动:{}", link);
				Document interActDoc = spider.getHtmlDocumentWithCache(link, charset);
				Elements rows = interActDoc.select(".patient_case_con").select("table").get(0).select("tr");
				for (int i = 1; i < rows.size(); i++) {
					Element row = rows.get(i);
					try {
						Elements tds = row.select("td");
						InteractBean act = new InteractBean();
						act.setTitle(tds.get(0).text());
						act.setLink(tds.get(0).select("a").get(0).attr("href"));
						act.setRelatedDiseases(tds.get(1).text());
						act.setUpdateDate(tds.get(3).text());
						list.add(act);
					} catch (Exception e) {
						logger.error("{}", row.outerHtml(), e);
					}
				}
			} catch (Exception e) {
				logger.error("{}", link, e);
			}
			// 进一步获取互动详情
			for (InteractBean qa : item.getInterActList()) {
				String lk = qa.getLink();
				if (lk == null) {
					continue;
				}
				try {
					spiderInteractList(qa);
				} catch (Exception e) {
					logger.error("{}", lk, e);
				}
			}
		}
	}

	private static void spiderInteractList(InteractBean act) throws Exception {
		Document doc = spider.getHtmlDocumentWithCache(act.getLink(), charset);
		List<Conversation> list = new ArrayList<Conversation>();
		act.setConversationList(list);
		try {
			Elements streams = doc.select(".pb20").get(0).select(".zzx_yh_stream");
			try {
				String title = doc.select(".clearfix.zzx_yh_h1 h1").text();
				if (StringUtils.isNotEmpty(title)) {
					act.setTitle(title);
				}
			} catch (Exception e) {
			}
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
					act.setQuestion(content);
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
