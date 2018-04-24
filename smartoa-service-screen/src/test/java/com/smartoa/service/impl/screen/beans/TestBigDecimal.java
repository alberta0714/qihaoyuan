package com.smartoa.service.impl.screen.beans;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TestBigDecimal {
	public static void main(String[] args) {
		double a = 16635029.76;
		Object r = new BigDecimal(a).setScale(2, RoundingMode.HALF_UP).toString();
		System.out.println(r);
	}
}
