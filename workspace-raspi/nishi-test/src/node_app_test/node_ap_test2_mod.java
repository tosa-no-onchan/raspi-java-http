package node_app_test;
/**
 * Raspberry Pi Smart agri system
 * v1.0
 * node_ap_test2_mod.java
 * http://192.168.1.x/class-mod/node_app_test.node_ap_test2_mod
 * host_ap_test2_mod.java からのリクエストに応じて、
 * node_ap_test2.java デーモンの起動、停止を行います。
 * 処理の結果をtext/plain で報告します。
 * pg_start->
 * http://192.168.1.180/class-mod/node_app_test.node_ap_test2_mod?req=pg_start
 * pg_stop->
 * http://192.168.1.180/class-mod/node_app_test.node_ap_test2_mod?req=pg_stop
 */

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import app_lib.Defs_ap_test2;
import user_cgi_lib.CommandExec;
import user_cgi_lib.userCgiMod;

public class node_ap_test2_mod extends userCgiMod{
	//プログラム終了 signファイル
	File stop_f=new File(Defs_ap_test2.nd_stop_f);
	//プログラム runnig signファイル
	File run_f=new File(Defs_ap_test2.nd_run_f);
	//データ収集開始、停止 signファイル
	File act_f=new File(Defs_ap_test2.nd_act_f);

	public node_ap_test2_mod() {
		// TODO 自動生成されたコンストラクター・スタブ
		super();
	}

	@Override
	public void start() {
		int led_count=2;
		String msg="";
		System.out.println("node_ap_test2_mod::start():#1 parm="+parm);
		if(parm_hash.get("req") != null){
			if(parm_hash.get("req").equals("pg_start") == true){
				if(parm_hash.get("led_count")!=null && parm_hash.get("led_count") != ""){
					led_count=Integer.valueOf(parm_hash.get("led_count"));
				}
				startPg();
				msg="start req accecept";
			}
			else if(parm_hash.get("req").equals("pg_stop") == true){
				stopPg();
				msg="stop req accecept";
			}
			else if(parm_hash.get("req").equals("pg_status") == true){
				msg=statusPg();
			}
		}
		// TODO 自動生成されたメソッド・スタブ
		setHeader();	//ヘッダーは、自分で送って下さい。
		ht_print("Content-Type: text/plain\r\n\r\n");
		Date now = new Date();
		SimpleDateFormat datef=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ht_print(datef.format(now)+","+msg);
	}
	/*
	 * startPg()
	 */
	public boolean startPg(){
		boolean rc_ok=false;
		//start_stop_idle=true;
		if(act_f.exists()==false){
			try {
				act_f.createNewFile();
			}
			catch (IOException e) {
				// TODO 自動生成された catch ブロック
				//e.printStackTrace();
				System.out.println("node_ap_test2_mod::startPg():#1 e="+e);
			}
		}
		if(run_f.exists()==false){
			CommandExec cmde = new CommandExec();
			String cmd_s[] = {"sh",Defs_ap_test2.nd_pg_bat};
			int rc;
			rc = cmde.command_exec_noresp(cmd_s);
			System.out.println("node_ap_test2_mod::startPg():#2 rc="+rc);
			if(rc==0){
				System.out.println("node_ap_test2_mod::startPg():#3 cmd ok");
				rc_ok=true;
			}
		}
		else{
			System.out.println("node_ap_test2_mod::startPg():#4 node_ap_test2_tmp is already running");
			rc_ok=true;
		}
		return rc_ok;
	}
	/*
	 * stopPg()
	 */
	public boolean stopPg(){
		boolean rc_ok=true;
		//start_stop_idle=true;
		if(act_f.exists()==true){
			act_f.delete();
		}
		//プログラムrunnig sign ファイルが有れば停止させます
		if(run_f.exists()==true){
			try {
				stop_f.createNewFile();
			}
			catch (IOException e) {
				// TODO 自動生成された catch ブロック
				//e.printStackTrace();
				System.out.println("node_ap_test2_mod::stopPg():#1 e="+e);
				rc_ok=false;
			}
			//try {
				//Thread.sleep(1000*180);
				//Thread.sleep(1000*2);
			//}
			//catch (InterruptedException e) {
			//}
		}
		return rc_ok;
	}
	/*
	 * statusPg()
	 */
	public String statusPg(){
		String rc_s="run";
		//プログラムrunnig sign ファイルが無ければ
		if(run_f.exists()==false){
			rc_s="stop";
		}
		return rc_s;
	}
}
