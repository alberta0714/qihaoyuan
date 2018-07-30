package googleauth;

import org.junit.jupiter.api.Test;

/* 
 * Not really a unit test- but it shows usage 
 */  
public class GoogleAuthTest {  
      
    @Test  
    public void genSecretTest() {  
        String secret = GoogleAuthenticator.generateSecretKey();  
        secret="GJRFNDKRJFNIRLWL";
        String url = GoogleAuthenticator.getQRBarcodeURL("sunzhanchao", "relay.op.xywy.com", secret);  
        System.out.println("Please register " + url);  
        System.out.println("Secret key is " + secret);  
    }  
      
    // Change this to the saved secret from the running the above test.   
    static String savedSecret = "GJRFNDKRJFNIRLWL";  
      
    @Test  
    public void authTest() {  
        // enter the code shown on device. Edit this and run it fast before the code expires!  
        long code = 349394;  
        long t = System.currentTimeMillis();  
        GoogleAuthenticator ga = new GoogleAuthenticator();  
        ga.setWindowSize(5); //should give 5 * 30 seconds of grace...  
        boolean r = ga.check_code(savedSecret, code, t);  
        System.out.println("Check code = " + r);  
    }  
}  