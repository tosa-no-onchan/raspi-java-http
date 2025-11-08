/**
 * Raspberry Pi Smart agri system
 * v1.1
 * MyLed_cgi.java
 * http://192.168.1.x/class-bin/MyLed_cgi
 * cgi 対応の MyLed です。
 * 処理の結果をhttp で報告します。
 */
import java.io.File;  
import java.io.FileWriter;

//
// Control a LED with your Raspberry Pi by using Java SE Embedded
// http://adf4beginners.blogspot.jp/2013/06/control-led-with-your-raspberry-pi-by.html
//
public class MyLed_cgi {

	static final String GPIO_OUT = "out";
	static final String GPIO_ON = "1";  
	static final String GPIO_OFF = "0";  
	static String[] GpioChannels = { "24" };
 
	public static void main(String[] args) throws InterruptedException {

		// TODO 自動生成されたメソッド・スタブ
		System.out.print("Content-Type: text/html\r\n\r\n");

		System.out.print("<!DOCTYPE html>\n");
		System.out.print("<html lang=\"ja\">\n");
		System.out.print("<head>\n");
		System.out.print("<meta charset=\"shift_jis\">\n");
		System.out.print("<meta name=\"viewport\" content=\"width=device-width\">\n");
		System.out.print("<title>MyLed_cgi.class</title>\n");
		System.out.print("<style type=\"text/css\">\n");
		System.out.print("<!--\n");
		System.out.print("body{\n");
		System.out.print(" font-size:16px;\n");
		System.out.print("}\n");
		System.out.print("-->\n");
		System.out.print("</style>\n");
		
		System.out.print("</head>\n");
		System.out.print("<body>\n");
		System.out.print("/class-bin/MyLed_cgi.class Excuting<br />\n");
		System.out.print("<hr />\n");
		System.out.print("led brink start<br />\n");		
		// TODO 自動生成されたメソッド・スタブ
		FileWriter[] commandChannels;  
		try {  
			// Open file handles to GPIO port unexport and export controls  
			FileWriter unexportFile =   
				new FileWriter("/sys/class/gpio/unexport");  
	       FileWriter exportFile =   
	           new FileWriter("/sys/class/gpio/export");  
	   
	       // Loop through all ports if more than 1  
	       for (String gpioChannel : GpioChannels) {
	    	   System.out.print("gpioChannel="+gpioChannel+"<br />\n");
	    	   System.out.flush();
	     
	    	   // Reset the port, if needed  
	    	   File exportFileCheck = new File("/sys/class/gpio/gpio"+gpioChannel);  
	    	   if (exportFileCheck.exists()) {    
	    		   unexportFile.write(gpioChannel);  
	    		   unexportFile.flush();  
	         	}  
	         
	         // Set the port for use  
	         exportFile.write(gpioChannel);    
	         exportFile.flush();  
	   
	         // Open file handle to port input/output control  
	         FileWriter directionFile =  
	           new FileWriter("/sys/class/gpio/gpio" + gpioChannel +   
	             "/direction");  
	         
	         // Set port for output  
	         directionFile.write(GPIO_OUT);  
	         directionFile.flush();  
	       }  
	       // Set up a GPIO port as a command channel  
	       FileWriter commandChannel = new   
	                FileWriter("/sys/class/gpio/gpio" +  
	             GpioChannels[0] + "/value");  
	         
	       int period = 200; // Sleep time in milliseconds  
	       int period2 = 100; // Sleep time in milliseconds  
	       int lct=0;
	       while (lct < 10) {  
	           // HIGH: Set GPIO port ON  
	           commandChannel.write(GPIO_ON);  
	           commandChannel.flush();          
	           java.lang.Thread.sleep(period);  
	       
	           // LOW: Set GPIO port OFF  
	           commandChannel.write(GPIO_OFF);  
	           commandChannel.flush();
	           java.lang.Thread.sleep(period2);
	           lct++;
	   		System.out.print("led brink lct="+lct+"<br />\n");
	   		System.out.flush();
	       }    
	    } catch (Exception exception) {  
	       exception.printStackTrace();  
	     }
  		System.out.print("led brink end<br />\n");		
		System.out.print("</body>\n");
		System.out.print("</html>\n");
		System.out.close();
	}

}
