/**
 * Raspberry Pi Smart agri system
 * v1.0
 * TempSensor_cgi.java
 * http://192.168.1.x/class-mod/TempSensor_cgi_mod
 * java cgi 対応の 温度センサープログラムです。
 * 処理の結果をhttp で報告します。
 */

import user_cgi_lib.CommandExec;

public class TempSensor_cgi{
	public void start() {
		// TODO 自動生成されたメソッド・スタブ
		System.out.print("Content-Type: text/html\r\n\r\n");

		System.out.println("TempSensor_cgi::start() : #1 <br />");
		
		
		System.out.print("<!DOCTYPE html>\n"
			+"<html lang=\"ja\">\n"
			+"<head>\n"
			+"<meta charset=\"utf-8\">\n"
			+"<meta name=\"viewport\" content=\"width=device-width\">\n"
			+"<title>TempSensor2_cgi_mod.class</title>\n"
			+"<style type=\"text/css\">\n"
			+"<!--\n"
			+"body{\n"
			+" font-size:16px;\n"
			+"}\n"
			+"-->\n"
			+"</style>\n"
			+"</head>\n"
			+"<body>\n"
			+"/class-bin/TempSensor_cgi を実行しています。<br />\n"
			+"<hr />\n");
		check_temp();
		System.out.print("<hr />\n"
			+"</body>\n"
			+"</html>\n");
	}
	
	public void check_temp(){
		String sys_bus="/sys/bus/w1/devices";
		CommandExec cmde = new CommandExec();
		String cmd_s[] = {"ls",sys_bus};
		int rc;
		rc = cmde.command_exec(cmd_s);
		System.out.println("TempSensor_cgi::check_temp() : #1 rc="+rc);
		if(rc==0){
			System.out.print("execute ok<br />\n");
			for(int i=0;i<cmde.result.size();i++){
				String s = cmde.result.get(i);
				System.out.print(s+"<br />\n");
			}
			String cmd_s2[] = {"cat",sys_bus+"/"+cmde.result.get(0)+"/w1_slave"};
			rc = cmde.command_exec(cmd_s2);
			if(rc==0){
				for(int i=0;i<cmde.result.size();i++){
					String s = cmde.result.get(i);
					System.out.print(s+"<br />\n");
				}
				String temp_line=cmde.result.get(1);
				int i=temp_line.indexOf("t=");
				if(i > 0){
					String temp=temp_line.substring(i+2);
					if(temp != null){
						float temp_f=(float) (Float.parseFloat(temp)/1000.0);
						System.out.print("Now temp is "+temp_f+" C<br />\n");
					}
				}
			}
			else{
				for(int i=0;i<cmde.errors.size();i++){
					String s = cmde.errors.get(i);
					System.out.print(s+"<br />\n");
				}
			}
		}
		else if(rc == -1){
			System.out.print("execute error<br />\n");
			for(int i=0;i<cmde.errors.size();i++){
				String s = cmde.errors.get(i);
				System.out.print(s+"<br />\n");
			}
		}
	}
	public static void main(String[] args) throws InterruptedException {
		TempSensor_cgi cgi=new TempSensor_cgi();
		cgi.start();
	}
}
