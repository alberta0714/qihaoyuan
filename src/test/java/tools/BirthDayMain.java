package tools;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BirthDayMain {
	private static final Logger logger = LoggerFactory.getLogger(BirthDayMain.class);

	public static void main(String[] args) {
		showAge("2003-04-05");
		System.out.println("---");
		showAge("2013-03-12", "相识");
		showAge("2015-05-04", "领证");
		showAge("2015-10-10", "买房");
		showAge("2016-04-30", "婚礼");

		System.out.println("---");
		int i = showAge("1998-04-07", "敏 三月十一");//
		int g = showAge("1987-06-30", "Sun 六月初五"); //
		int l = showAge("1959-08-02", "Mum 六月廿八");
		int h = showAge("1990-10-07", "Wife 八月十九"); //
		int k = showAge("1981-10-31", "勇 十月初四");

		System.out.println("---");
		int d = showAge("2016-02-12", "M猴 马佳悦(高新娜) 正月初五"); //
		// int e = showAge("2017-02-03", "M Zyy 王萧颖(兜兜) 正月初七 "); //
		int f = showAge("2016-08-28", "M猴 梁源溪(晨)  七月廿十六 "); //
		int c = showAge("2016-09-30", "M猴 赵姗迪 八月三十"); //
		int b = showAge("2018-10-26", "G狗 辛逍洋 九月十八");//
		int a = showAge("2017-12-16", "G鸡 佳毅 十月廿十九"); //

		// showDiff(a, b);
	}

	private static void showDiff(int a, int b) {
		int diff = Math.abs(a - b);
		System.out.println("年龄差：" + (diff / 365) + "岁 " + (diff % 365 / 30) + "个月 " + (diff % 365 % 30) + "天");
	}

	private static int showAge(String birth) {
		return showAge(birth, null);
	}

	private static int showAge(String birth, String name) {
		DateTime birthDay = new DateTime(birth);
		DateTime now = new DateTime();
		Period years = new Period(birthDay, now, PeriodType.years());
		Period months = new Period(birthDay, now, PeriodType.months());
		Period days = new Period(birthDay, now, PeriodType.days());
		logger.info("出生年月: {}     \t年龄:{} 岁 {} 月 {} 天\t{}", birthDay.toString("yyyy-MM-dd"), years.getYears(),
				months.getMonths() % 12, days.getDays() % 30, StringUtils.trimToEmpty(name));
		return days.getDays();
	}
}