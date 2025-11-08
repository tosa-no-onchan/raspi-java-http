package node_app_test;
/**
 * Raspberry Pi Smart agri system
 * v1.0
 * node_ap_test1.java
 * http://192.168.1.x/class-mod/node_app_test.node_ap_test1
 * cgi_mod 対応の LED & 温度センサープログラムです。
 * 処理の結果をtext/plain で報告します。
 */

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import user_cgi_lib.CommandExec;
import user_cgi_lib.userCgiMod;

public class node_ap_test1 extends userCgiMod{
	static final String GPIO_OUT = "out";
	static final String GPIO_ON = "1";  
	static final String GPIO_OFF = "0";  
	static String[] GpioChannels = { "24" };

	public node_ap_test1() {
		// TODO 自動生成されたコンストラクター・スタブ
		super();
	}

	@Override
	public void start() {
		int led_count=2;
		if(parm_hash.get("led_count")!=null && parm_hash.get("led_count") != ""){
			led_count=Integer.valueOf(parm_hash.get("led_count"));
		}
		// TODO 自動生成されたメソッド・スタブ
		setHeader();	//ヘッダーは、自分で送って下さい。
		ht_print("Content-Type: text/plain\r\n\r\n");
		led_blink(led_count);
		Date now = new Date();
		SimpleDateFormat datef=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String temp=check_temp();
		ht_print(datef.format(now)+","+temp+"\n");
	}
	/*
	 * LED blink
	 */
	public void led_blink(int led_count){
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
	       while (lct < led_count) {  
	           // HIGH: Set GPIO port ON  
	           commandChannel.write(GPIO_ON);  
	           commandChannel.flush();          
	           java.lang.Thread.sleep(period);  
	       
	           // LOW: Set GPIO port OFF  
	           commandChannel.write(GPIO_OFF);  
	           commandChannel.flush();  
	           java.lang.Thread.sleep(period2);
	           lct++;
	   		System.out.print("led blink lct="+lct+"<br />\n");
	   		System.out.flush();
	       }    
	    } catch (Exception exception) {  
	       exception.printStackTrace();  
	     }		
	}
	/*
	 * read temp senser
	 */
	public String check_temp(){
		String temp_rc="";
		String sys_bus="/sys/bus/w1/devices";
		CommandExec cmde = new CommandExec();
		String cmd_s[] = {"ls",sys_bus};
		int rc;
		rc = cmde.command_exec(cmd_s);
		System.out.println("TempSensor_cgi_mod::check_temp() : #1 rc="+rc);
		if(rc==0){
			//ht_print("execute ok<br />\n");
			for(int i=0;i<cmde.result.size();i++){
				String s = cmde.result.get(i);
				//ht_print(s+"<br />\n");
			}
			String cmd_s2[] = {"cat",sys_bus+"/"+cmde.result.get(0)+"/w1_slave"};
			rc = cmde.command_exec(cmd_s2);
			if(rc==0){
				for(int i=0;i<cmde.result.size();i++){
					String s = cmde.result.get(i);
					//ht_print(s+"<br />\n");
				}
				String temp_line=cmde.result.get(1);
				int i=temp_line.indexOf("t=");
				if(i > 0){
					String temp=temp_line.substring(i+2);
					if(temp != null){
						float temp_f=(float) (Float.parseFloat(temp)/1000.0);
						//ht_print("temp="+temp_f+" C\n");
						//temp_rc="temp="+temp_f+" C";
						//temp_rc="temp="+temp_f;
						temp_rc=String.valueOf(temp_f);
					}
				}
			}
			else{
				for(int i=0;i<cmde.errors.size();i++){
					String s = cmde.errors.get(i);
					ht_print(s+"\n");
				}
			}
		}
		else if(rc == -1){
			ht_print("execute error<br />\n");
			for(int i=0;i<cmde.errors.size();i++){
				String s = cmde.errors.get(i);
				ht_print(s+"\n");
			}
		}
		return temp_rc;
	}
}
