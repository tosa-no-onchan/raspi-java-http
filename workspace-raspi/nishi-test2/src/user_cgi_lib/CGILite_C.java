/**
 * Raspberry Pi java http server
 * v1.7 update 2015.1.19
 * java User cgi program Common Class
 * user_cgi_lib.CGILite_C.java
 *  use this class for GET,POST & non enctype="multipart/form-data" data receive,such as perl cgi-lib.pl
 *  use for httpServer_t
 */
package user_cgi_lib;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CGILite_C {
	public Map<String, String> env;
	public String method="";
	public String parm="";
	public Map<String,String> parms = new HashMap<String,String>();

	DataOutputStream outs;

	public boolean dump_f=false;
	private static boolean DEBG=false;
	private MyLog mlog;

	/*
	 * CGILite()
	 */
	public CGILite_C() {
		// TODO 自動生成されたコンストラクター・スタブ
		outs = new DataOutputStream(System.out);
		env = System.getenv();
		mlog = new MyLog();
	}
	/*
	 * in()
	 */
	public void in(){
		//REQUEST_METHOD
		if(env.get("REQUEST_METHOD")!= null){
			method=env.get("REQUEST_METHOD");
		}
		if(method.equals("GET")==true){
			get_req();
		}
		else if(method.equals("POST")==true){
			HttpInputStreamCGI hin = new HttpInputStreamCGI(System.in);
			// c server からのコールは、hin.setCgiUse(false)
			hin.setCgiUse(false); 	// set CGI called from C server
			if(dump_f){
				dump_din(hin);				
			}
			else{
				// POST req
				post_req(hin);
				setParms();
			}
		}
	}
	/*
	 * get_req()
	 */
	private void get_req(){
		//QUERY_STRING
		if(env.get("QUERY_STRING")!= null){
			parm=env.get("QUERY_STRING");
			setParms();
		}
	}
	/*
	 *  post_req(HttpInputStreamCGI hin_r)
	 */
	private void post_req(HttpInputStreamCGI hin_r){
		// POST req
		mlog.put(DEBG,"CGILite::post_req() :#1 read POST data start!");
		int n=0;
		String p_line="";
		parm="";

		String content_type;
		boolean multipart_f=false;
		String boundary;
		String boundary1;
		String boundary2;
		
		String[] spl_d;
		
		if(env.get("CONTENT_TYPE") != null){
			content_type=env.get("CONTENT_TYPE");
			if(content_type.startsWith("multipart/form-data")==true){
				multipart_f=true;
				spl_d = content_type.split(" ");
				if(spl_d.length>1){
					boundary = spl_d[1].substring(9);
					boundary1 = "--"+boundary;
					boundary2 = boundary1+"--";
					mlog.put(DEBG,"boundary="+boundary+",length="+boundary.length());
					mlog.put(DEBG,"boundary1="+boundary1+",length="+boundary1.length());
					mlog.put(DEBG,"boundary2="+boundary2+",length="+boundary2.length());
				}
			}
		}
		// multipart ではありません
		if(!multipart_f){
			mlog.put(DEBG,"CGILite::post_req() :#2 get POST data!!");
			try {
				while (hin_r.ready() == true && n < 20000){
					p_line=hin_r.readline();
					parm+=p_line;
					n++;
				}
			}
			catch (IOException e) {
				// TODO 自動生成された catch ブロック
				mlog.put(DEBG,"CGILite::post_req() :#90 error ="+e+"<br />");
			}
		}
		else{
			mlog.put(DEBG,"CGILite::post_req() :#10 request is POST with multipart/form-data, stop processing!<br />");
			int len;
			try {
				len = System.in.available();
				if(len <= 0){
					len=2048;
				}
				byte[] buf=new byte[len];
				// POST データを cgi の標準入力に転送
				while (hin_r.ready() == true && n < 20000){
					int data_l=hin_r.readBlock(buf,buf.length);
					n++;
				}
			}
			catch (IOException e) {
				// TODO 自動生成された catch ブロック
				mlog.put(DEBG,"CGILite::post_req() :#91 error ="+e+"<br />");
			}	
		}
	}
	/*
	 * dump_din()
	 * ダンプ表示 for debug
	 */
	private void dump_din(HttpInputStreamCGI din_r){
		//HttpDataInputStream din_r = new HttpDataInputStream(in);
		try{
			//String in_s;
			String p_line="";
			int n=0;
			// ここでは、Httpヘッダーは、ありません。
			//req = din_r.readline();
			//System.out.println("HttpReaderCGI::dump_din() :#1 recve socket stream="+req+"<br />");
			//while((in_s=din_r.readline()).length() > 0){
			//	System.out.println(in_s+"<br />");
			//}
			// データ部分をダンプします。
			while (din_r.ready() == true && n < 10){
				p_line=din_r.readline();
				if(p_line.length()>0){
					System.out.println("HttpReaderCGI::dump_din() :#1 p_line="+p_line+"\n");
				}
				else{
					System.out.println("HttpReaderCGI::dump_din() :#2 p_line.length()="+p_line.length()+"\n");
					break;
				}
				n++;
			}
		}
		catch(IOException e){
			System.out.println("HttpReaderCGI::read_din() :#90 error ="+e);
		}
	}		
	/*
	 * setParms()
	 */
	private void setParms(){
		String[] parmx = parm.split("&");
		for(int i=0;i< parmx.length;i++){
			String[] _parmx = parmx[i].split("=");
			if(_parmx.length>=2){
				//System.out.print(_parmx[0]+"="+_parmx[1]+"<br />\n");
				if(parms.containsKey(_parmx[0])){
					String s=parms.get(_parmx[0])+0x00+_parmx[1];
					parms.put(_parmx[0], s);
				}
				else{
					parms.put(_parmx[0], _parmx[1]);
				}
			}
			else{
				//System.out.print(parmx[i]+"<br />\n");
			}
		}
	}
	
	/*
	 * pr_as(String line)
	 *  ascii データ の Client への送信
	 */
	public void pr_as(String line){
		try {
			outs.writeBytes(line);
		}
		catch (IOException e) {
			System.out.println("CGI::pr_as() :#1 DataOutputStream writeBytes() error="+e+"<br />");
		}
		
	}
	/*
	 * pr_kj(String line)
	 *  Kanji データ の Client への送信
	 */
	public void pr_kj(String line){
		try {
			byte[] line_b = line.getBytes("utf-8");
			outs.write(line_b);
		}
		catch (IOException e) {
			System.out.println("CGI::pr_kj() :#1 DataOutputStream writeBytes() error="+e+"<br />");
		}
	}
	/*
	 * pr(String line)
	 *  String の Client への送信
	 *  半角、倍角自動判定します
	 */
	public void pr(String line){
		try{
			char[] chars = line.toCharArray();
			boolean kanj_f=false;
			for (int i = 0; i < chars.length; i++) {
				if (String.valueOf(chars[i]).getBytes().length >= 2) {
					// 倍角
					kanj_f=true;
					byte[] line_b =line.getBytes("utf-8");
					//System.out.println("CGI::pr() :#1 passed!!<br />");
					outs.write(line_b);
					break;
				}
			}
			if(kanj_f==false){
				//System.out.println("CGI::pr() :#2 passed!!<br />");
				outs.writeBytes(line);
			}
		}
		catch(IOException e){
			System.out.println("CGI::pr() :#3 DataOutputStream writeBytes() error="+e+"<br />");
		}
	}
}
