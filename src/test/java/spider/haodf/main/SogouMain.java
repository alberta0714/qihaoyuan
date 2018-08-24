package spider.haodf.main;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.NumberUtils;
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

public class SogouMain {
	static String cookies = "SUV=00E91787DE8009E95ACC5EB7C0BCF466; CXID=B4F824E69EF3325C3D2EBC718E2D4685; SUID=0C35786A4C238B0A5AE86F2500034EE1; wuid=AAE4fCSKIAAAAAqGGWwbbw4AGwY=; SMYUV=1533887330425587; UM_distinctid=16522cf3aa0a5-00c8b060bb5194-393d5f0e-1fa400-16522cf3aa1b0d; IPLOC=CN1100; CNZZDATA1272994932=1248822157-1534406263-https%253A%252F%252Fpinyin.sogou.com%252F%7C1534406263; SNUID=4EDFA5176B6E18B323E605DB6B07DFBC; sct=1; ld=Tkllllllll2bt2C9lllllVHfBxUlllllKGM1Jyllll9lllllVllll5@@@@@@@@@@; ad=VulOFyllll2zfpDOlllllVHdli7lllllzXkCRlllllwlllllVAoll5@@@@@@@@@@; PHPSESSID=rc292t7enb9pt9cv9sv980ohp2; CNZZDATA1253526839=1436965606-1533885653-https%253A%252F%252Fwww.baidu.com%252F%7C1534989333";
	static DownLoadUtilsV2 spider = null;
	static Charset charset = Charset.forName("UTF-8");
	static JobLogUtils logger = new JobLogUtils(SogouMain.class);
	static File baseDir = new File("D:\\tmp\\sogou");

	static int cur = 0;

	public static void main(String[] args) throws Exception {
		System.out.println(1);
		// spiderSogouDics();
	}

	private static void spiderSogouDics() throws Exception, IOException, UnsupportedEncodingException {
		spider = new DownLoadUtilsV2(baseDir, cookies).isWithImages(false).setSleep(1000 * 1);
		for (int i = 133; i <= 153; i++) {
			String lk = "https://pinyin.sogou.com/dict/cate/index/" + i;
			logger.info("正在采集:{}", lk);
			Document doc = spider.getHtmlDocumentWithCache(lk, charset, true);
			String dicName = doc.select("title").text();
			Elements dictPageListLi = doc.select("#dict_page_list li");
			int pages = 1;
			if (dictPageListLi.size() > 0) {
				pages = NumberUtils.toInt(dictPageListLi.get(dictPageListLi.size() - 2).text().trim());
			}
			logger.info("共有{}页", pages);
			for (int pg = 1; pg <= pages; pg++) {
				String pgLink = lk + "/default/" + pg;
				if (pg == 1) {
					pgLink = lk;
				}
				logger.info("开始采集{}", pgLink);
				Document docPg = spider.getHtmlDocumentWithCache(pgLink, charset, true);
				Elements downLinks = docPg.select("#dict_detail_list .dict_dl_btn a");
				logger.info("下载链接个数为:{}", downLinks.size());
				for (Element downLink : downLinks) {
					String downLk = downLink.attr("href");
					logger.info("{}", downLk);
					String fName = downLk.substring(downLk.lastIndexOf("&name=") + 6, downLk.length());
					fName = URLDecoder.decode(fName, "UTF-8") + ".scel";
					logger.info("文件名:{}", fName);
					File dicDir = new File("E:\\xywy\\source_code\\data\\auto_depart\\sogou_dic", dicName);
					File tmp = new File(dicDir, fName);
					spider.downLoadToTmpFile(downLk, tmp, false);
				}
			}
			// break;
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