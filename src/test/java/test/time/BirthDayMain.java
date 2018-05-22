package test.time;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BirthDayMain {
	private static final Logger logger = LoggerFactory.getLogger(BirthDayMain.class);

	public static void main(String[] args) {
		int a = showAge("2017-12-16"); // Jiayi

		// int b = showAge("2016-09-30"); // ZhaoShandi
		// int b = showAge("2016-02-12"); // MaJiayue M
		// int b = showAge("2017-02-03"); // WangXiaoying Zyy
		// int b = showAge("2016-08-28"); // LiangYuanxi Chen

		// showDiff(a, b);
	}

	private static void showDiff(int a, int b) {
		int diff = Math.abs(a - b);
		System.out.println("年龄差：" + (diff / 365) + "岁 " + (diff % 365 / 30) + "个月 " + (diff % 365 % 30) + "天");
	}

	private static int showAge(String birth) {
		DateTime birthDay = new DateTime(birth);
		DateTime now = new DateTime();
		Period years = new Period(birthDay, now, PeriodType.years());
		Period months = new Period(birthDay, now, PeriodType.months());
		Period days = new Period(birthDay, now, PeriodType.days());
		logger.info("出生年月: {}     \t年龄:{} 岁 {} 月 {} 天", birthDay.toString("yyyy-MM-dd"), years.getYears(),
				months.getMonths() % 12, days.getDays() % 30);
		return days.getDays();
		// Period p = new Period(birthDay, now, PeriodType.days());
		// int yearDiff = p.getDays() / 365;
		// int monthDiff = p.getDays() % 365 / 30;
		// int dayDiff = p.getDays() % 365 % 30;
		// System.out.println("出生年月： " + birthDay.toString("yyyy-MM-dd") + " 年龄：" +
		// yearDiff + "岁" + monthDiff + "月"
		// + dayDiff + "天");
		// return p.getDays();
	}
}