/**
 * Raspberry Pi Smart agri system
 * v1.0
 * TempSensor_cgi_mod.java
 * http://192.168.1.x/class-mod/TempSensor_cgi_mod
 * cgi_mod 対応の 温度センサープログラムです。
 * 処理の結果をhttp で報告します。
 */

import user_cgi_lib.CommandExec;
import user_cgi_lib.userCgiMod;

public class TempSensor_cgi_mod extends userCgiMod{

	public TempSensor_cgi_mod() {
		// TODO 自動生成されたコンストラクター・スタブ
		super();
	}

	@Override
	public void start() {
		// TODO 自動生成されたメソッド・スタブ
		setHeader();	//ヘッダーは、自分で送って下さい。
		ht_print("Content-Type: text/html\r\n\r\n");

		ht_print("<!DOCTYPE html>\n");
		ht_print("<html lang=\"ja\">\n");
		ht_print("<head>\n");
		ht_print("<meta charset=\"utf-8\">\n");
		ht_print("<meta name=\"viewport\" content=\"width=device-width\">\n");
		ht_print("<title>TempSensor_cgi_mod.class</title>\n");
		ht_print("<style type=\"text/css\">\n");
		ht_print("<!--\n");
		ht_print("body{\n");
		ht_print(" font-size:16px;\n");
		ht_print("}\n");
		ht_print("-->\n");
		ht_print("</style>\n");
		
		ht_print("</head>\n");
		ht_print("<body>\n");
		ht_print("/class-mod/TempSensor_cgi_mod を実行しています。<br />\n");
		ht_print("<hr />\n");
		check_temp();
		ht_print("<hr />\n");
		ht_print("</body>\n");
		ht_print("</html>\n");
	}
	
	public void check_temp(){
		String sys_bus="/sys/bus/w1/devices";
		CommandExec cmde = new CommandExec();
		String cmd_s[] = {"ls",sys_bus};
		int rc;
		rc = cmde.command_exec(cmd_s);
		System.out.println("TempSensor_cgi_mod::check_temp() : #1 rc="+rc);
		if(rc==0){
			ht_print("execute ok<br />\n");
			for(int i=0;i<cmde.result.size();i++){
				String s = cmde.result.get(i);
				ht_print(s+"<br />\n");
			}
			String cmd_s2[] = {"cat",sys_bus+"/"+cmde.result.get(0)+"/w1_slave"};
			rc = cmde.command_exec(cmd_s2);
			if(rc==0){
				for(int i=0;i<cmde.result.size();i++){
					String s = cmde.result.get(i);
					ht_print(s+"<br />\n");
				}
				String temp_line=cmde.result.get(1);
				int i=temp_line.indexOf("t=");
				if(i > 0){
					String temp=temp_line.substring(i+2);
					if(temp != null){
						float temp_f=(float) (Float.parseFloat(temp)/1000.0);
						ht_print("Now temp is "+temp_f+" C<br />\n");
					}
				}
			}
			else{
				for(int i=0;i<cmde.errors.size();i++){
					String s = cmde.errors.get(i);
					ht_print(s+"<br />\n");
				}
			}
		}
		else if(rc == -1){
			ht_print("execute error<br />\n");
			for(int i=0;i<cmde.errors.size();i++){
				String s = cmde.errors.get(i);
				ht_print(s+"<br />\n");
			}
		}
	}
}
