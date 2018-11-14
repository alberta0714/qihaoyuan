package spider.haodf.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import spider.utils.DownLoadUtilsV2;
import spider.utils.JobLogUtils;

public class XinhuaDicMain {
	static String cookies = "SUV=00E91787DE8009E95ACC5EB7C0BCF466; CXID=B4F824E69EF3325C3D2EBC718E2D4685; SUID=0C35786A4C238B0A5AE86F2500034EE1; wuid=AAE4fCSKIAAAAAqGGWwbbw4AGwY=; SMYUV=1533887330425587; UM_distinctid=16522cf3aa0a5-00c8b060bb5194-393d5f0e-1fa400-16522cf3aa1b0d; IPLOC=CN1100; CNZZDATA1272994932=1248822157-1534406263-https%253A%252F%252Fpinyin.sogou.com%252F%7C1534406263; SNUID=4EDFA5176B6E18B323E605DB6B07DFBC; sct=1; ld=Tkllllllll2bt2C9lllllVHfBxUlllllKGM1Jyllll9lllllVllll5@@@@@@@@@@; ad=VulOFyllll2zfpDOlllllVHdli7lllllzXkCRlllllwlllllVAoll5@@@@@@@@@@; PHPSESSID=rc292t7enb9pt9cv9sv980ohp2; CNZZDATA1253526839=1436965606-1533885653-https%253A%252F%252Fwww.baidu.com%252F%7C1534989333";
	static DownLoadUtilsV2 spider = null;
	static Charset charset = Charset.forName("UTF-8");
	static JobLogUtils logger = new JobLogUtils(XinhuaDicMain.class);
	static File baseDir = new File("D:\\tmp\\xinhua");

	static int cur = 0;

	public static void main(String[] args) throws Exception {
		spiderSogouDics();
	}

	private static void spiderSogouDics() throws Exception, IOException, UnsupportedEncodingException {
		spider = new DownLoadUtilsV2(baseDir, cookies).isWithImages(false).setSleep(1000 * 1);
		Set<String> set = new LinkedHashSet<String>();
		for (int i = 1; i <= 1269; i++) {
			String lk = "http://hanyu.xiexingcun.com/index.asp?page=" + i;
			logger.info("正在采集:\t{}", lk);
			Document doc = spider.getHtmlDocumentWithCache(lk, charset, false);
			Elements tds = doc.select("#table1").get(0).select("td[colspan=6").remove();
			tds = doc.select("#table1").get(0).select("td a");
			for (Element td : tds) {
				String dic = td.text();
				set.add(dic);
			}
		}
		System.out.println("set.size=" + set.size());
		FileWriter fw = new FileWriter(new File(baseDir, "xinhua.dic"));
		for (String dic : set) {
			fw.write(dic + "\n");
		}
		fw.close();
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