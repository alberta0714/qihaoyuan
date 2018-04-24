package com.smartoa.service.impl.screen.beans;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.commons.httpclient.protocol.Protocol;

import com.smartoa.service.impl.screen.MySSLSocketFactory;

public class TestMain {

	public static void main(String[] args) throws Exception {
		Protocol myhttps = new Protocol("https", new MySSLSocketFactory(), 443);
		Protocol.registerProtocol("https", myhttps);

		URL url = new URL(
				"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1524230095490&di=db762725247ef13974044cd979624077&imgtype=0&src=http%3A%2F%2Fimgsrc.baidu.com%2Fimgad%2Fpic%2Fitem%2Fc83d70cf3bc79f3d093d1737b0a1cd11738b29dd.jpg");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		InputStream is = conn.getInputStream();
		byte[] buf = new byte[1024 * 1024 * 1024 * 10];
		int len = 0;
		while ((len = is.read(buf)) != -1) {
			String line = new String(buf, 0, len, Charset.forName("utf8"));
			System.out.println(line);
		}
	}
	
      
    private static void trustAllHttpsCertificates() throws Exception {  
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];  
        javax.net.ssl.TrustManager tm = new miTM();  
        trustAllCerts[0] = tm;  
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext  
                .getInstance("SSL");  
        sc.init(null, trustAllCerts, null);  
        javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc  
                .getSocketFactory());  
    }  
  
    static class miTM implements javax.net.ssl.TrustManager,  
            javax.net.ssl.X509TrustManager {  
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {  
            return null;  
        }  
  
        public boolean isServerTrusted(  
                java.security.cert.X509Certificate[] certs) {  
            return true;  
        }  
  
        public boolean isClientTrusted(  
                java.security.cert.X509Certificate[] certs) {  
            return true;  
        }  
  
        public void checkServerTrusted(  
                java.security.cert.X509Certificate[] certs, String authType)  
                throws java.security.cert.CertificateException {  
            return;  
        }  
  
        public void checkClientTrusted(  
                java.security.cert.X509Certificate[] certs, String authType)  
                throws java.security.cert.CertificateException {  
            return;  
        }  
    }  
}
