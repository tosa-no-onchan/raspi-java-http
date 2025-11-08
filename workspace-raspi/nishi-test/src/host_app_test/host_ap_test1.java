/**
 * host_ap_test1.java
 * node_ap_test1.java をHTTPリクエストでコールして、
 * 温度データの受け取りとDB保存を行います。
 */
package host_app_test;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import app_lib.Url_connect;
import host_app_lib.DBManager;

public class host_ap_test1 {
	//String	urlName="http://192.168.1.150/class-mod/mod_test.test1_cgi_mod";
	String	urlName="http://192.168.1.180/class-mod/node_app_test.node_ap_test1";
	
	boolean deamon_f=false;

	public void test1(){
		boolean end_f=false;
		int cnt=1;
		print_log("host_ap_test1::test1 : #1 start "+urlName);
		// TODO 自動生成されたメソッド・スタブ
		while(end_f==false){
			Url_connect urlc=new Url_connect();
			if(urlc.doPost(urlName, "led_count=2&cnt="+cnt)==true){
				print_log(urlc.htmls);
				//print_log("host_ap_test1 : #2 cnt="+cnt);
				cnt++;
			}
			else{
				print_log("host_ap_test1::test1 : #4 "+urlc.errors);
				end_f=true;
			}
			try {
				//Thread.sleep(1000*180);
				Thread.sleep(1000*12);
			}
			catch (InterruptedException e) {
			}
		}
	}
	/*
	 * 温度収集＆DB(SQLite3)へ保存します。
	 */
	public void test2(){
		String[] recs;
		String date_s;
		String temps;
		String sql;
		Connection con = null;
		Statement smt = null;
		ResultSet rs = null;
		DBManager dbm=null;
		//プログラム終了 signファイル
		File stop_f=new File(Defs_ap_test1.stop_f);
		//プログラム runnig signファイル
		File run_f=new File(Defs_ap_test1.run_f);
		//データ収集開始、停止 signファイル
		File act_f=new File(Defs_ap_test1.act_f);

		boolean end_f=false;
		int cnt=1;

		//DBアクセスクラス作成
		try {
			dbm = new DBManager(Defs_ap_test1.db);
		}
		catch (ClassNotFoundException e1) {
			// TODO 自動生成された catch ブロック
			//e1.printStackTrace();
			print_log("host_ap_test1::test2 : #1 e1="+e1);
		}
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
				print_log("host_ap_test1::test2 : #2 e="+e);
			}
		}
		print_log("host_ap_test1::test2 : #3 start "+urlName);
		// TODO 自動生成されたメソッド・スタブ
		while(end_f==false){
			//プログラム終了 sign ファイルが有れば、終わり
			if(stop_f.exists()==true){
				break;
			}
			if(act_f.exists()==true){
				Url_connect urlc=new Url_connect();
				if(urlc.doPost(urlName, "led_count=3&cnt="+cnt)==true){
					print_log(urlc.htmls);

					recs = urlc.htmls.trim().split(",");
					if(recs.length ==2){
						date_s=recs[0];
						temps=recs[1];
						//String sql="UPDATE node_tmp SET dtime = \""+date_s+"\",tmp=\""+temps+"\"";
						sql = "INSERT INTO node_tmp(dtime,tmp) VALUES('"+date_s+"','"+temps+"')";
						
						try {
							con = dbm.createConnection();
							dbm.insert(con, sql);
							//print_log("host_ap_test2 : #2 cnt="+cnt);
							cnt++;
							con.close();
							//print_log("host_ap_test1::test2 : #3 date_s="+date_s+",temp="+temps);
						}
						catch (SQLException e) {
							// TODO 自動生成された catch ブロック
							e.printStackTrace();
							print_log("host_ap_test1::test2 : #5 e="+e);
						}
					}
				}
				else{
					print_log("host_ap_test1::test2 : #6 "+urlc.errors);
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
	public static void main(String[] args) {
		host_ap_test1 hap1 = new host_ap_test1();
		//hap1.test1();
		hap1.deamon_f=true;
		if(hap1.deamon_f==true){
			System.out.close();
			System.err.close();
			try {
				System.in.close();
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
		hap1.test2();
	}
	public void print_log(String s){
		if(deamon_f==false){
			System.out.println(s);
		}
	}
}
