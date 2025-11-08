/**
 * host_ap_test2_mod.java
 * node_ap_test2_mod.java 経由で、node_ap_test2.java を起動＆停止して
 * 温度データの報告を、host_ap_test2_rcv_mod.java へ行わせます。また、そのデータを表示します。
 */
package host_app_test;

import host_app_lib.DBManager;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import app_lib.Defs_ap_test2;
import app_lib.Url_connect;
import user_cgi_lib.CommandExec;
import user_cgi_lib.userCgiMod;

/**
 * @author nishi
 *
 */
public class host_ap_test2_mod extends userCgiMod{
	//プログラム終了 signファイル
	File stop_f=new File(Defs_ap_test2.stop_f);
	//プログラム runnig signファイル
	File run_f=new File(Defs_ap_test2.run_f);
	//データ収集開始、停止 signファイル
	File act_f=new File(Defs_ap_test2.act_f);
	
	boolean start_idle;
	boolean stop_idle;
	
	/**
	 * 
	 */
	public host_ap_test2_mod() {
		// TODO 自動生成されたコンストラクター・スタブ
		super();
	}

	@Override
	public void start() {
		String msg="";
		start_idle=false;
		stop_idle=false;
		if(parm_hash.get("start_pg") != null){
			if(startPg()==true){
				start_idle=true;
			}
			else{
				msg="収集プログラム起動 エラー";
			}
		}
		else if(parm_hash.get("stop_pg") != null){
			if(stopPg()==true){
				stop_idle=true;
			}
			else{
				msg="収集プログラム終了 エラー";
			}
		}
		//起動処理依頼中です
		else if(parm_hash.get("start_idle") != null){
			start_idle=true;
			//node_ap_test2_tmp は稼働中です
			if(statusPg().equals("run")==true){
				//プログラムrunnig sign ファイルが無ければ作成
				if(run_f.exists()==false){
					try {
						run_f.createNewFile();
					}
					catch (IOException e) {
						// TODO 自動生成された catch ブロック
						//e.printStackTrace();
						System.out.println("host_ap_test2_mod::start() : #2 e="+e);
					}
				}
				start_idle=false;
			}
		}
		//停止処理依頼中です
		else if(parm_hash.get("stop_idle") != null){
			stop_idle=true;
			//node_ap_test2_tmp は停止中です
			if(statusPg().equals("stop")==true){
				//プログラムrunnig sign ファイルが有れば、削除
				if(run_f.exists()==true){
					run_f.delete();
				}
				stop_idle=false;
			}
		}
		disp(msg);
	}
	/*
	 * startPg()
	 */
	public boolean startPg(){
		String[] recs;
		boolean rc_ok=false;
		Url_connect urlc=new Url_connect();
		if(urlc.doPost(Defs_ap_test2.stater, "req=pg_start&led_count=3")==true){
			recs = urlc.htmls.trim().split(",");
			System.out.println("host_ap_test2_mod::startPg() #1 "+recs[0]+","+recs[1]);
			rc_ok=true;
		}
		return rc_ok;
	}
	/*
	 * stopPg()
	 */
	public boolean stopPg(){
		String[] recs;
		boolean rc_ok=true;
		Url_connect urlc=new Url_connect();
		if(urlc.doPost(Defs_ap_test2.stater, "req=pg_stop")==true){
			recs = urlc.htmls.trim().split(",");
			System.out.println("host_ap_test2_mod::stopPg() #1 "+recs[0]+","+recs[1]);
			rc_ok=true;
		}
		return rc_ok;
	}
	/*
	 * statusPg()
	 */
	public String statusPg(){
		String[] recs;
		String rc_s="";
		Url_connect urlc=new Url_connect();
		if(urlc.doPost(Defs_ap_test2.stater, "req=pg_status")==true){
			recs = urlc.htmls.trim().split(",");
			System.out.println("host_ap_test2_mod::statusPg #1 "+recs[0]+","+recs[1]);
			rc_s=recs[1];
		}
		return rc_s;
	}
	/*
	 * execDb()
	 */
	public void execDb(){
		Connection con = null;
		Statement smt = null;
		ResultSet rs = null;
		String sql,host,dtime,tmp,line_s;
		try {
			con = new DBManager(Defs_ap_test2.db).createConnection();
			smt = con.createStatement();
			// SQL文の実行
			sql = "SELECT * from node_tmp order by dtime desc limit 20";
			rs = smt.executeQuery(sql);
			   // 検索結果の取り出し
		    while(rs.next()) {
		      host = rs.getString("host");
		      dtime = rs.getString("dtime");
		      tmp = rs.getString("tmp");
		      line_s="    <tr>\n"
		    		  +"      <td width=\"17%\">"+dtime+"</td>\n"
		    		  +"      <td width=\"17%\">"+host+"</td>\n"
		    		  +"      <td width=\"17%\">"+tmp+"</td>\n"
		    		  +"    </tr>\n";
		      ht_print(line_s);
		    }
		    // 検索結果のクローズ
		    rs.close();
		    // SQLコンテナのクローズ 
		    smt.close();
		}
		catch (ClassNotFoundException | SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			System.out.println("host_ap_test2_mod::execDb() : #1 e="+e);
		}
		
	}
	/*
	 * disp(String msg)
	 */
	public void disp(String msg){
		// TODO 自動生成されたメソッド・スタブ
		setHeader();	//ヘッダーは、必ず自分で送って下さい。
		// ここからは、 html を Client マシン(ブラウザー)へ送っています。
		ht_print("Content-Type: text/html\r\n\r\n");	// 最後の、"\r\n\r\n" を忘れずに。
		String htmls="<html>\n"
+"<head>\n"
+"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n"
+"<meta http-equiv=\"Content-Language\" content=\"ja\" />\n"
+"<title>host_ap_test2_mod</title>\n"
+"</head>\n"
+"<body>\n"
+"<p>host_ap_test2_mod</p>\n"
+"Raspberry Pi 温度センサーの表示<br />\n"
+"<script Language=\"JavaScript\">\n"
+"<!--\n"
+"function FrontPage_Form1_Validator(theForm)\n"
+"{\n"
+"    //alert(\" passed #1 \");\n"
+"  return (true);\n"
+"}\n"
+"//-->\n"
+"</script>\n"
+"<form method=\"POST\" action=\"/class-mod/host_app_test.host_ap_test2_mod\" onsubmit=\"return FrontPage_Form1_Validator(this)\" name=\"FrontPage_Form1\">\n";
		if(msg != ""){
			htmls+="<p>"+msg+"</p>\n";
		}
		ht_print(htmls);
		
		//収集プログラム起動＆停止処理中です
		if(start_idle==true || stop_idle==true){
			htmls="<p style=\"color:red\">只今、収集プログラムの起動、終了処理中なのでしばらくしてから、再表示して下さい。</p>\n";
			ht_print(htmls);
		}
		//収集プログラム起動＆停止処理中ではありません
		else{
			//プログラムrunnig sign ファイルが無ければ収集プログラムは、停止中
			if(run_f.exists()==false){
				htmls="<p style=\"color:red\">収集プログラム終了中です&nbsp;&gt;&gt;&nbsp;<input type=\"submit\" value=\"収集プログラム起動\" name=\"start_pg\" style=\"color:red;\" /></p>\n";
			}
			else{
				htmls="<p style=\"color:blue\">収集プログラム稼働中です&nbsp;&gt;&gt;&nbsp;<input type=\"submit\" value=\"収集プログラム終了\" name=\"stop_pg\" style=\"color:blue;\" /></p>\n";
			}
			ht_print(htmls);
		}
htmls="  <table border=\"0\" cellpadding=\"2\" cellspacing=\"0\" width=\"62%\">\n"
+"    <tr>\n"
+"      <td width=\"17%\">date time</td>\n"
+"      <td width=\"17%\">host</td>\n"
+"      <td width=\"17%\">temp</td>\n"
+"    </tr>\n";
	ht_print(htmls);

	execDb();

	htmls="    <tr>\n"
+"      <td width=\"17%\"></td>\n"
+"      <td width=\"17%\"></td>\n"
+"      <td width=\"17%\"></td>\n"
+"    </tr>\n"
+"  </table>\n"
+"  <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"24%\" style=\"margin: 20px 0 0 40px;\">\n"
+"    <tr>\n"
+"      <td align=\"center\">\n"
+"        <input type=\"submit\" value=\"Go disp\" name=\"go_disp\" />\n"
+"      </td>\n"
+"      <td align=\"center\">\n"
+"        <input type=\"reset\" value=\"Reset\" name=\"B2\" />\n"
+"      </td>\n"
+"    </tr>\n"
+"  </table>\n"
+"  <input type=\"hidden\" name=\"begin\" value=\"1\" />\n";
	if(start_idle==true){
		htmls+="  <input type=\"hidden\" name=\"start_idle\" value=\"1\" />\n";
	}
	else if(stop_idle==true){
		htmls+="  <input type=\"hidden\" name=\"stop_idle\" value=\"1\" />\n";
	}
	htmls+="</form>\n"
+"<p><a href=\"/\">Site Top</a></p>\n"
+"</body>\n"
+"</html>\n";
		ht_print(htmls);
	}
}
