package spider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

public class DownLoadImages {
	public static void main(String[] args) throws Exception {
		 URL url = new URL("http://120.77.101.252/pt10.tar.gz");
//		URL url = new URL("http://www.techhero.com.cn/var/upload/image/2018/02/2018022803540940827_1920x700.jpg");
		InputStream is = url.openStream();
		FileOutputStream fos = new FileOutputStream(new File("pt10.tar.gz"));
		byte[] buf = new byte[1024 * 1024 * 1024];
		int length = 0;
		while ((length = is.read(buf)) != -1) {
			fos.write(buf, 0, length);
			System.out.println("write:" + length + "byte");
		}
		fos.close();
		is.close();
	}
}
