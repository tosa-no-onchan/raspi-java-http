/**
 * Raspberry Pi java http server
 * v1.9 update 2015.11.13
 * java User cgi Module Base Class
 *  user_cgi_lib.userCgiMod2.java
 */

package user_cgi_lib;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.Map;

/**
 * java User cgi Module Base Class 2 です。
 *  java User cgi module は、この java User cgi Module Base Class 2
 *  を継承して、start() メソッドを
 *  オーバーライトして、自分の Java User CGI モジュールを作成すると簡単です。
 *  又、全てを自分で作成する事もできます。
 *  その場合も、 この、userCgiMod2.java が参考になるでしょう。
 *  
 *  v1.9 の追加機能 2015.11.13 by nishi
 *  C Light http server からのコールにも対応させます。
 *  C Light http server からは、 cgi_go2() がエントリーとなります。
 *  この場合は、STDOUT へ http レスポンスを出力します。
 */
public abstract class userCgiMod2 {
	public DataOutputStream outs;
	public String method;
	public String parm;	// パラメータの保存
	//public Hashtable<String, String> parm_hash;	// パラメータのハッシュ
	public Map<String,String> parm_hash;

	// STDOUTのバックアップ& リダイレクト
	private PrintStream sysOutBk;
	private boolean c_server;
	public CGILite cgi;
	//private MyLog mlog;
	//private static boolean DEBG=true;
	
	public userCgiMod2(){
	}
	/* クライアントへヘッダーを送信します。
	 * setHeader()
	 *  make http header and send
	 */
	public void setHeader(){
		if(c_server==false){
			Date now = new Date();
			String http_hd_ok=
				"HTTP/1.1 200 OK\r\n"
				+"Date: "+now+"\r\n"
				+"Server: raspberry-pi\r\n"
				//+"Keep-Alive: timeout=5, max=10\r\n"
				//+"Connection: Keep-Alive\r\n"
				//+"Content-Length: 10\r\n"
				//+"Content-type: text/html\r\n\r\n" // 注）最後は \r\nが２個必要
				;
			try{
				outs.writeBytes(http_hd_ok);
			}
			catch(IOException e){
				System.out.println("cgi_Mod::setHeader() :#1 DataOutputStream write error="+e);
			}
		}
	}
	/*
	 * ht_print(String line)
	 *  String の Client への送信
	 *  半角、倍角自動判定します
	 */
	public void ht_print(String line){
		try{
			char[] chars = line.toCharArray();
			boolean kanj_f=false;
			for (int i = 0; i < chars.length; i++) {
				if (String.valueOf(chars[i]).getBytes().length >= 2) {
					// 倍角
					kanj_f=true;
					byte[] line_b =line.getBytes("utf-8");
					outs.write(line_b);
					break;
				}
			}
			if(kanj_f==false){
				outs.writeBytes(line);
			}
		}
		catch(IOException e){
			System.out.println("cgi_Mod::ht_print() :#1 DataOutputStream writeBytes() error="+e);
		}
	}
	/*
	 * ht_write(byte[] buff,int len)
	 *  byte[] の Client への送信
	 *  バイナリー、イメージデータの出力に使用
	 */
	public void ht_write(byte[] buff,int len){
		try{
			outs.write(buff, 0, len);
		}
		catch(IOException e){
			System.out.println("cgi_Mod::ht_write() :#1 DataOutputStream write() error="+e);
		}
	}
	public void ht_flush(){
		try{
			outs.flush();
		}
		catch(IOException e){
			System.out.println("cgi_Mod::ht_flush() :#1 DataOutputStream flush() error="+e);
		}
	}

	/* Java User CGI クラスの java Httpサーバーからの、エントリーメソッドです。
	 * cgi_go()
	 *  httServer-> classProcessor::class_ld() からの entry point
	 *  入力情報を取り込んでから、Java User CGI の start() メッソドをコールします。
	 */
	public void cgi_go(DataOutputStream outs,String method,String parm){
		c_server=false;
		this.outs=outs;
		this.method=method;
		this.parm=parm;
		/* パラメータをハッシュにします
		 * CGILinte.setParms() call
		 *  parm をハッシュテーブルに変換します。-> parm_hash<String ,String>
		 *  1) multi-select の場合
		 *   parm1=neko&parm1=yagi&parm1=ushi -> parm1="neko"+0x00+"yagi"+0x00+"ushi"
		 *   2) ファイルアップロードは、
		 *   <input type="file" name="simg" />  でファイルに netosa-banaer.jpg を指定すると
		 *   サーバーからのパラメータは、下記となります。
		 *   simg=netosa-barnaer.jpg&simg_netosa-banaer.jpg=tmp-DDHHMMSS-mmm
		 *   /tmp/ras-pi-upload/tmp-DDHHMMSS-mmm でファイルがあるので、自分でコピーして下さい。
		 */
		//cgi.parm=parm;
		//cgi.setParms();
		//parm_hash=cgi.parms;
		// user cgi call
		start();
		try{
			outs.close();
		}
		catch(IOException e){
			System.out.println("cgi_Mod::cgi_go() :#9 DataOutputStream close() error="+e);
		}
	}
	/* Java User CGI クラスの C Light Httpサーバーからの、エントリーメソッドです。
	 * cgi_go2()
	 *  httServer-> classProcessor::class_ld() からの entry point
	 *  入力情報を取り込んでから、Java User CGI の start() メッソドをコールします。
	 */
	//public void cgi_go2(String method,String parm){
	public void cgi_go2(){
		c_server=true;
		// STDOUTのバックアップ& リダイレクト
		sysOutBk = System.out;
		outs= new DataOutputStream(System.out);
		//this.method=method;
		//this.parm=parm;
		//convParm_toHash(parm);

		cgi = new CGILite();	// use for GET,POST(non multipart/form-data)
		//mlog = new MyLog();
		
		cgi.in();
		parm_hash=cgi.parms;

		// user cgi call
		start();
		try{
			outs.close();
		}
		catch(IOException e){
			System.out.println("cgi_Mod::cgi_go2() :#9 DataOutputStream close() error="+e);
		}
	}
	/*
	 * cgi method start
	 * java User CGI は、このメッソドをオーバーライトして、必要な処理を実行して下さい。
	 */
	public abstract void start();
	// {
	//   setHeader();  <-  必ず実行
	//   ht_print("Content-Type: text/html\r\n\r\n");  <-  "\r\n\r\n" を忘れずに
	//   ht_print("<html>\n");
	//   ht_print("<head>\n");
	//   ht_print("<title>test1_cgi_mod.class</title>\n");
	//    ....
	// }
}
