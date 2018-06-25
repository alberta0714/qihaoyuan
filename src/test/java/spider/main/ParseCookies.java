package spider.main;

import java.util.HashMap;
import java.util.Map;

import spider.utils.JobLogUtils;

public class ParseCookies {
	static JobLogUtils log = new JobLogUtils(ParseCookies.class);

	public static void main(String[] args) {
		String cookiesHeader = "g=HDF.79.5b29c68b5a069; UM_distinctid=1641b2f93a3704-0d818a8be07385-393d5f0e-100200-1641b2f93a42d0; _ga=GA1.2.1640071909.1529464461; __jsluid=33ec294a20eb0a336bb997088890b2e8; _gid=GA1.2.197721853.1529893817; Hm_lvt_dfa5478034171cc641b1639b2a5b717d=1529464461,1529893817; PHPSESSID=cl29o8bggj5hllcfdm5kimatn5; CNZZDATA1915189=cnzz_eid%3D702026912-1529462794-https%253A%252F%252Fluodianzhong.haodf.com%252F%26ntime%3D1529893590; sdmsg=1; userinfo[id]=3348029546; userinfo[name]=hdfvvk8n; userinfo[key]=VShSYQIzXDgFZwA3ADAFbgRvBj5SZFR7Aj9bOAU1XCcNLgNoVG5Va1VzCT5TeA%3D%3D; userinfo[time]=1529895899; userinfo[ver]=1.0.2; userinfo[hostid]=0; CNZZDATA1256706712=373824218-1529895154-https%253A%252F%252Fluodianzhong.haodf.com%252F%7C1529895154; _gat=1; Hm_lpvt_dfa5478034171cc641b1639b2a5b717d=1529896466";
		Map<String, String> cookies = convertToCookiesMap(cookiesHeader);
		System.out.println(cookies);
	}

	public static Map<String, String> convertToCookiesMap(String cookiesHeader) {
		Map<String, String> cookies = new HashMap<String, String>();
		if (cookiesHeader == null) {
			return cookies;
		}
		String cookiesArr[] = cookiesHeader.split(";");
		for (String cookieStr : cookiesArr) {
			String[] item = cookieStr.split("=");
			if (item.length != 2) {
				log.error("【{}】无法解析", cookieStr);
				continue;
			}
			cookies.put(item[0], item[1]);
		}
		return cookies;
	}
}
