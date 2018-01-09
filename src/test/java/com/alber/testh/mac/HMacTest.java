package com.alber.testh.mac;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class HMacTest {

	public static void main(String[] args) throws Exception {
		byte[] keyBytes = getSecretKey();
		String key = byte2hex(keyBytes);
		System.out.println(key);
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

	public static byte[] getSecretKey() throws Exception {
		KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacMD5"); // 可填入 HmacSHA1，HmacSHA256 等
		SecretKey key = keyGenerator.generateKey();
		byte[] keyBytes = key.getEncoded();
		return keyBytes;
	}

}
