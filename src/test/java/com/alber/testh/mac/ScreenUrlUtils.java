package com.alber.testh.mac;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Calendar;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class ScreenUrlUtils {
	public static void main(String[] args) throws Exception {
		String screenId = "f93bd46add7c2760f3d6e83c5f47bbdd";
		String token = "aPK0y2vgIwkfcnPROsiYbvYgVAa7eNJo";
		String url = buildUrl(screenId, token);
		System.out.println(url);

	}

	public static String buildUrl(String screenId, String token) throws UnsupportedEncodingException {
		String urlPre = "http://datav.aliyun.com/share/";
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		String time = Long.toString(cal.getTimeInMillis());
		String sign = screenId + "|" + time;
		byte[] hMacSha256 = hMacSha256(sign.getBytes(), token.getBytes());
		String signature = URLEncoder.encode(Base64.getEncoder().encodeToString(hMacSha256), "UTF-8");
		String url = urlPre + screenId + "?_datav_time=" + time + "&_datav_signature=" + signature;
		return url;
	}

	public static byte[] hMacSha256(byte[] data, byte[] key) {
		try {
			SecretKeySpec signingKey = new SecretKeySpec(key, "HmacSHA256");
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(signingKey);
			return mac.doFinal(data);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String byte2hex(byte[] b) {
		StringBuilder hs = new StringBuilder();
		String stmp;
		for (int n = 0; b != null && n < b.length; n++) {
			stmp = Integer.toHexString(b[n] & 0XFF);
			if (stmp.length() == 1)
				hs.append('0');
			hs.append(stmp);
		}
		return hs.toString().toUpperCase();
	}
}
