package tools;

import org.joda.time.DateTime;

/***
 * *   功能描述：	请在此处填写文件的功能
 * *   Author:Sun Zhanchao  Date:2020-06-15
 ***/
public class WeeksCount {

    public static void main(String[] args) {
        DateTime dt = new DateTime();
        int weekOfYear = dt.getWeekOfWeekyear();
        System.out.println("当前是第 " + weekOfYear + " 周");
    }

}
