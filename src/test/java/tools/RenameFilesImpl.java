package tools;

import java.io.File;
import java.io.FileInputStream;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Sets;

public class RenameFilesImpl extends RenameFiles {
	static Set<String> names = Sets.newHashSet();
	static int count = 0;

	private static File renameFile(File dir, File f) throws Exception {
		System.out.println("f:" + f.getAbsolutePath());
		File rename = new File(dir, f.getName() + ".jpg");
		System.out.println("r:" + rename.getAbsolutePath());
		// f.renameTo(rename);
		FileInputStream fis = new FileInputStream(f);
		byte[] head = new byte[8];
		fis.read(head);
		Integer i = new Integer(head[0]);

		String sb = "";
		for (byte b : head) {
			System.out.println(b);
			String item = Integer.toHexString(b & 0xff).toString();
			item = StringUtils.leftPad(item, 2, "0").toUpperCase();
			sb += item+"_";
		}
		System.out.println("head:" + sb);

		names.add(sb);

		count++;
		System.out.println("count:" + count);
		System.out.println(names);
		fis.close();
		return rename;
	}

	public static void main(String[] args) throws Exception {
		for (File md5 : dir.listFiles()) {
			if (md5.isFile()) {
				continue;
			}
			System.out.println("md5:" + md5.getAbsolutePath());
			for (File md2 : md5.listFiles()) {
				if (md2.isFile()) {
					continue;
				}
				System.out.println("md2:" + md2.getAbsolutePath());
				for (File f : md2.listFiles()) {
					if (f.isDirectory()) {
						continue;
					}
					renameFile(md2, f);
				}
			}
		}
		System.out.println(names);
		System.out.println("文件总数:" + count);
	}

}
