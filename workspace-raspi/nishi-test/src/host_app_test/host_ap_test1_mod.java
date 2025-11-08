/**
 * host_ap_test1_mod.java
 * host_ap_test1.java の収集データの表示
 */
package host_app_test;

import host_app_lib.DBManager;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import user_cgi_lib.CommandExec;
import user_cgi_lib.userCgiMod;

/**
 * @author nishi
 *
 */
public class host_ap_test1_mod extends userCgiMod{
	//プログラム終了 signファイル
	File stop_f=new File(Defs_ap_test1.stop_f);
	//プログラム runnig signファイル
	File run_f=new File(Defs_ap_test1.run_f);
	//データ収集開始、停止 signファイル
	File act_f=new File(Defs_ap_test1.act_f);
	
	boolean start_stop_idle;
	
	/**
	 * 
	 */
	public host_ap_test1_mod() {
		// TODO 自動生成されたコンストラクター・スタブ
		super();
	}

	@Override
	public void start() {
		String msg="";
		start_stop_idle=false;
		if(parm_hash.get("start_pg") != null){
			if(startPg()==true){
				
			}
			else{
				msg="収集プログラム起動 エラー";
			}
		}
		else if(parm_hash.get("stop_pg") != null){
			if(stopPg()==true){
				
			}
			else{
				msg="収集プログラム終了 エラー";
			}
		}
		//収集開始指示
		else if(parm_hash.get("start_corect") != null){
			if(act_f.exists()==false){
				try {
					act_f.createNewFile();
				} catch (IOException e) {
					// TODO 自動生成された catch ブロック
					//e.printStackTrace();
					System.out.println("host_ap_test1_mod::start() : #1 e="+e);
				}
			}
		}
		//収集停止指示
		else if(parm_hash.get("stop_corect") != null){
			if(act_f.exists()==true){
				act_f.delete();
			}
		}
		disp(msg);
	}
	public boolean startPg(){
		boolean rc_ok=false;
		start_stop_idle=true;
		CommandExec cmde = new CommandExec();
		String cmd_s[] = {"sh",Defs_ap_test1.pg_bat};
		int rc;
		rc = cmde.command_exec_noresp(cmd_s);
		System.out.println("host_ap_test1_mod::startPg() : #1 rc="+rc);
		if(rc==0){
			System.out.println("host_ap_test1_mod::startPg #2 cmd ok");
			rc_ok=true;
		}
		return rc_ok;
	}
	public boolean stopPg(){
		boolean rc_ok=true;
		start_stop_idle=true;
		//プログラムrunnig sign ファイルが有れば停止させます
		if(run_f.exists()==true){
			try {
				stop_f.createNewFile();
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				//e.printStackTrace();
				System.out.println("host_ap_test1_mod::stopPg() : #1 e="+e);
				rc_ok=false;
			}
		}
		return rc_ok;
	}
	public void execDb(){
		Connection con = null;
		Statement smt = null;
		ResultSet rs = null;
		String sql,host,dtime,tmp,line_s;
		try {
			con = new DBManager(Defs_ap_test1.db).createConnection();
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
			System.out.println("host_ap_test1_mod::execDb() : #1 e="+e);
		}
		
	}
	public void disp(String msg){
		// TODO 自動生成されたメソッド・スタブ
		setHeader();	//ヘッダーは、必ず自分で送って下さい。
		// ここからは、 html を Client マシン(ブラウザー)へ送っています。
		ht_print("Content-Type: text/html\r\n\r\n");	// 最後の、"\r\n\r\n" を忘れずに。
		String htmls="<html>\n"
+"<head>\n"
+"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n"
+"<meta http-equiv=\"Content-Language\" content=\"ja\" />\n"
+"<title>host_ap_test1_mod</title>\n"
+"</head>\n"
+"<body>\n"
+"<p>host_ap_test1_mod</p>\n"
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
+"<form method=\"POST\" action=\"/class-mod/host_app_test.host_ap_test1_mod\" onsubmit=\"return FrontPage_Form1_Validator(this)\" name=\"FrontPage_Form1\">\n";
		if(msg != ""){
			htmls+="<p>"+msg+"</p>\n";
		}
		ht_print(htmls);
		
		//収集プログラム起動＆停止処理中ではありません
		if(start_stop_idle==false){
			//プログラムrunnig sign ファイルが無ければ収集プログラムは、停止中
			if(run_f.exists()==false){
				htmls="<p style=\"color:red\">収集プログラム終了中です&nbsp;&gt;&gt;&nbsp;<input type=\"submit\" value=\"収集プログラム起動\" name=\"start_pg\" style=\"color:red;\" /></p>\n";
			}
			else{
				htmls="<p style=\"color:blue\">収集プログラム稼働中です&nbsp;&gt;&gt;&nbsp;<input type=\"submit\" value=\"収集プログラム終了\" name=\"stop_pg\" style=\"color:blue;\" /></p>\n";
			}
			ht_print(htmls);
		}
		else{
			htmls="<p style=\"color:red\">只今、収集プログラムの起動、終了処理中なのでしばらくしてから、再表示して下さい。</p>\n";
			ht_print(htmls);
		}
		if(act_f.exists()==true){
			htmls="<p style=\"color:blue\">データ収集を行っています。&nbsp;"
			+"<input type=\"submit\" value=\"収集停止\" name=\"stop_corect\" /></p>\n";
		}
		else{
			htmls="<p style=\"color:blue\">データ収集を停止しています。&nbsp;"
			+"<input type=\"submit\" value=\"収集開始\" name=\"start_corect\" /></p>\n";
		}
htmls+="  <table border=\"0\" cellpadding=\"2\" cellspacing=\"0\" width=\"62%\">\n"
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
+"  <input type=\"hidden\" name=\"begin\" value=\"1\" />\n"
+"</form>\n"
+"<p><a href=\"/\">Site Top</a></p>\n"
+"</body>\n"
+"</html>\n";
		ht_print(htmls);
		
	}

}
