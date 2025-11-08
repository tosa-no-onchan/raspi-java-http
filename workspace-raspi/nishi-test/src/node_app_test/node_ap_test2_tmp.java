package node_app_test;
/**
 * Raspberry Pi Smart agri system
 * v1.0
 * node_ap_test2_tmp.java
 * http://192.168.1.x/class-mod/node_app_test.node_ap_test2_mod
 * からデーモン起動＆停止されます。
 * 起動中は、LED点灯してから温度を測定し、温度を
 * http://192.168.1.150/class-mod/host_app_test.host_ap_test2_rcv
 * へ、POSTします。
 * 処理の結果は、text/plain で報告します。
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import app_lib.Defs_ap_test2;
import app_lib.Url_connect;
import user_cgi_lib.CommandExec;

public class node_ap_test2_tmp {
	static final String GPIO_OUT = "out";
	static final String GPIO_ON = "1";  
	static final String GPIO_OFF = "0";  
	static String[] GpioChannels = { "24" };

	boolean deamon_f=false;
	boolean test_f=false;
	
	public node_ap_test2_tmp(){
		
	}
	public void exec_tmp(){
		int led_count=2;
		boolean end_f=false;
		int cnt=1;
		String temp="";
		String post_dt="";
		//プログラム終了 signファイル
		File stop_f=new File(Defs_ap_test2.nd_stop_f);
		//プログラム runnig signファイル
		File run_f=new File(Defs_ap_test2.nd_run_f);
		//データ収集開始、停止 signファイル
		File act_f=new File(Defs_ap_test2.nd_act_f);

		print_log("node_ap_test2_tmp::exec_tmp():#1 start");
		
		//プログラム終了 sign ファイルが有れば削除
		if(stop_f.exists()==true){
			stop_f.delete();
		}
		//プログラムrunnig sign ファイルが無ければ作成
		if(run_f.exists()==false){
			try {
				run_f.createNewFile();
			}
			catch (IOException e) {
				// TODO 自動生成された catch ブロック
				//e.printStackTrace();
				print_log("node_ap_test2_tmp::exec_tmp():#2 e="+e);
			}
		}
		// TODO 自動生成されたメソッド・スタブ
		while(end_f==false){
			//プログラム終了 sign ファイルが有れば、終わり
			if(stop_f.exists()==true){
				break;
			}
			if(act_f.exists()==true){
				if(test_f==false){
					led_blink(led_count);
				}
				Date now = new Date();
				SimpleDateFormat datef=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				if(test_f==false){
					temp=check_temp();
					post_dt=datef.format(now)+","+temp;
				}
				else{
					post_dt=datef.format(now)+",0.0";
				}
				Url_connect urlc=new Url_connect();
				if(urlc.doPost(Defs_ap_test2.recever, post_dt)==true){
					print_log(urlc.htmls);
				}
				else{
					print_log("node_ap_test2_tmp::exec_tmp():#6 "+urlc.errors);
					end_f=true;
				}				
			}
			try {
				//Thread.sleep(1000*180);
				Thread.sleep(1000*12);
			}
			catch (InterruptedException e) {
			}
		}
		//プログラムrunnig sign ファイルが有れば、削除
		if(run_f.exists()==true){
			run_f.delete();
		}		
	}
	/*
	 * main()
	 */
	public static void main(String[] args) {
		node_ap_test2_tmp nap2=new node_ap_test2_tmp();

		//nap2.test_f=true;
		nap2.deamon_f=true;
		if(nap2.deamon_f==true){
			System.out.close();
			System.err.close();
			try {
				System.in.close();
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
		nap2.exec_tmp();
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
	    	   print_log("gpioChannel="+gpioChannel+"<br />\n");
	     
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
	           print_log("led blink lct="+lct+"<br />\n");
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
		print_log("node_ap_test2_tmp::check_temp() : #1 rc="+rc);
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
					print_log(s+"\n");
				}
			}
		}
		else if(rc == -1){
			print_log("execute error<br />\n");
			for(int i=0;i<cmde.errors.size();i++){
				String s = cmde.errors.get(i);
				print_log(s+"\n");
			}
		}
		return temp_rc;
	}
	public void print_log(String s){
		if(deamon_f==false){
			System.out.println(s);
		}
	}
}
