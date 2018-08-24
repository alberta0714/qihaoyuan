package test.jdk;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
	public static void main(String[] args) throws Exception {
		String input = "afasd<fasdf<iMg src=\"./imgs/a9/3b/a93b4ba834cc8a5d266b\n4975b1e958c7.gif\" alt=\"亲亲\" title=\"亲亲\" />asdfasfas>dfdasdfasf";
		Pattern pattern = Pattern.compile("(?is)<img.*?>");
		Matcher m = pattern.matcher(input);
		if (m.find()) {
			System.out.println(m.group());
		}
	}
}