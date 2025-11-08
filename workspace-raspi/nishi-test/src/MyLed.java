import java.io.File;  
import java.io.FileWriter;

//
// Control a LED with your Raspberry Pi by using Java SE Embedded
// http://adf4beginners.blogspot.jp/2013/06/control-led-with-your-raspberry-pi-by.html
//
public class MyLed {

	static final String GPIO_OUT = "out";
	static final String GPIO_ON = "1";  
	static final String GPIO_OFF = "0";  
	static String[] GpioChannels = { "24" };
 
	public static void main(String[] args) throws InterruptedException {
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
	    	  System.out.println(gpioChannel);  
	     
	    	   // Reset the port, if needed  
	    	   File exportFileCheck =   
	    			   new File("/sys/class/gpio/gpio"+gpioChannel);  
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
	         
	       int period = 2000; // Sleep time in milliseconds  
	         
	       while (true) {  
	           // HIGH: Set GPIO port ON  
	           commandChannel.write(GPIO_ON);  
	           commandChannel.flush();          
	           java.lang.Thread.sleep(period);  
	       
	           // LOW: Set GPIO port OFF  
	           commandChannel.write(GPIO_OFF);  
	           commandChannel.flush();  
	           java.lang.Thread.sleep(period);  
	       }    
	    } catch (Exception exception) {  
	       exception.printStackTrace();  
	     }  
		
	}

}
