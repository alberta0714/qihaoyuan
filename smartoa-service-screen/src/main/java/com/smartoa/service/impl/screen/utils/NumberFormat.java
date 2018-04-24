package com.smartoa.service.impl.screen.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumberFormat {

	public static String formatDecimalStr(Double d) {
		if (d == null) {
			return "-";
		}
		return new BigDecimal(d).setScale(2, RoundingMode.HALF_UP).toString();
	}

	public static Double formatDecimal(Double d) {
		if (d == null) {
			return null;
		}
		return new BigDecimal(d).setScale(2, RoundingMode.HALF_UP).doubleValue();
	}
}
