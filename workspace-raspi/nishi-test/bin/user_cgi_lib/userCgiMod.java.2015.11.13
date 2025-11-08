/**
 * Raspberry Pi java http server
 * v1.0
 * java User cgi Module Base Class
 *  user_cgi_lib.userCgiMod.java
 */

package user_cgi_lib;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;

/**
 * java User cgi Module Base Class です。
 *  java User cgi module は、この java User cgi Module Base Class
 *  を継承して、start() メソッドを
 *  オーバーライトして、自分の Java User CGI モジュールを作成すると簡単です。
 *  又、全てを自分で作成する事もできます。
 *  その場合も、 この、userCgiMod.java が参考になるでしょう。
 */
public abstract class userCgiMod {
	public DataOutputStream outs;
	public String method;
	public String parm;	// パラメータの保存
	public Hashtable<String, String> parm_hash;	// パラメータのハッシュ
	public userCgiMod(){
	}
	/* クライアントへヘッダーを送信します。
	 * setHeader()
	 *  make http header and send
	 */
	public void setHeader(){
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
	/* パラメータをハッシュにします
	 * convParm_toHash(String parm)
	 *  parm をハッシュテーブルに変換します。-> parm_hash<String ,String>
	 *  1) multi-select の場合
	 *   parm1=neko&parm1=yagi&parm1=ushi -> parm1="neko"+0x00+"yagi"+0x00+"ushi"
	 *   2) ファイルアップロードは、
	 *   <input type="file" name="simg" />  でファイルに netosa-banaer.jpg を指定すると
	 *   サーバーからのパラメータは、下記となります。
	 *   simg=netosa-barnaer.jpg&simg_netosa-banaer.jpg=tmp-DDHHMMSS-mmm
	 *   /tmp/ras-pi-upload/tmp-DDHHMMSS-mmm でファイルがあるので、自分でコピーして下さい。
	 */
	public void convParm_toHash(String parm){
		parm_hash = new Hashtable<String, String>();
		if(parm.length()>0){
			String[] recs = parm.split("\\&");
			for (int i=0;i < recs.length;i++){
				String key="";
				String val="";
				String fs[] = recs[i].split("=");
				if(fs.length>0){
					key=fs[0];
				}
				if(fs.length>1){
					val=fs[1];
				}
				//System.out.println("cgi_Mod::convParm_toHash() :#2 key="+key+",val="+val);
				if(key != ""){
					if(parm_hash.containsKey(key)==false){
						parm_hash.put(key, val);
					}
					else{
						String old_v=parm_hash.get(key);
						old_v+=0x00+val;
						parm_hash.put(key, old_v);
					}
				}
			}			
		}
	}
	/* Java User CGI クラスの java Httpサーバーからの、エントリーメソッドです。
	 * cgi_go()
	 *  httServer-> classProcessor::class_ld() からの entry point
	 *  入力情報を取り込んでから、Java User CGI の start() メッソドをコールします。
	 */
	public void cgi_go(DataOutputStream outs,String method,String parm){
		this.outs=outs;
		this.method=method;
		this.parm=parm;
		convParm_toHash(parm);
		// user cgi call
		start();
		try{
			outs.close();
		}
		catch(IOException e){
			System.out.println("cgi_Mod::cgi_go() :#9 DataOutputStream close() error="+e);
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
