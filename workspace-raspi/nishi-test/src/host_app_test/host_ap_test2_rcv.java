/**
 * host_ap_test2_rcv.java
 * node_ap_test2_tmp.java からの温度データの受け取りとDB保存
 */
package host_app_test;

import host_app_lib.DBManager;

import java.sql.Connection;
import java.sql.SQLException;

import app_lib.Defs_ap_test2;
import user_cgi_lib.userCgiMod;

/**
 * @author nishi
 *
 */
public class host_ap_test2_rcv extends userCgiMod{
	/**
	 * 
	 */
	public host_ap_test2_rcv() {
		// TODO 自動生成されたコンストラクター・スタブ
		super();
	}

	@Override
	public void start() {
		String data="";
		String msg="OK";
		System.out.println("host_ap_test2_rcv::start():#1 parm="+parm);
		// TODO 自動生成されたメソッド・スタブ
		if(parm != ""){
			saveData(parm);
		}
		else{
			msg="NG";
		}
		setHeader();	//ヘッダーは、自分で送って下さい。
		ht_print("Content-Type: text/plain\r\n\r\n");
		ht_print(msg);
	}
	public void saveData(String data){
		String[] recs;
		String date_s;
		String temps;
		String sql;
		Connection con = null;
		DBManager dbm=null;
		//DBアクセスクラス作成
		try {
			dbm = new DBManager(Defs_ap_test2.db);
		}
		catch (ClassNotFoundException e1) {
			// TODO 自動生成された catch ブロック
			//e1.printStackTrace();
			System.out.println("host_ap_test2_rcv_mod::saveData() : #1 e1="+e1);
		}
		recs = data.trim().split(",");
		if(recs.length ==2){
			date_s=recs[0];
			temps=recs[1];
			//String sql="UPDATE node_tmp SET dtime = \""+date_s+"\",tmp=\""+temps+"\"";
			sql = "INSERT INTO node_tmp(dtime,tmp) VALUES('"+date_s+"','"+temps+"')";
			
			try {
				con = dbm.createConnection();
				dbm.insert(con, sql);
				//print_log("host_ap_test2 : #2 cnt="+cnt);
				con.close();
				//print_log("host_ap_test1::test2 : #3 date_s="+date_s+",temp="+temps);
			}
			catch (SQLException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
				System.out.println("host_ap_test2_rcv_mod::saveData() : #5 e="+e);
			}
		}
		
	}
}
