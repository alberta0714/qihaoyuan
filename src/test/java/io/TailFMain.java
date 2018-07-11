package io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.google.common.base.Charsets;

public class TailFMain {
	public static void main(String[] args) throws Exception {
		String srcFilename = "E:\\tmp2\\1.log";
		InputStream fileInputStream = new FileInputStream(srcFilename);
//		fileInputStream.skip(10); // skip n bytes
		Reader fileReader = new InputStreamReader(fileInputStream, Charsets.UTF_8);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String singleLine;
		while (true) {
			if ((singleLine = bufferedReader.readLine()) != null) {
				System.out.println(singleLine);
				continue;
			}
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}
		bufferedReader.close();
	}
}
