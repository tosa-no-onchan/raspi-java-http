/**
 * Raspberry Pi java http server
 * v1.8 update 2015.2.25
 * classProcessor.java
 * 外部 perl のプロセスを生成&実行します
 */
package http_pi;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.ProcessBuilder.Redirect;
import java.net.Socket;
import java.util.Date;
import java.util.Map;

/**
 * @author nishi
 *
 */
public class cgiProcessor {
	private http_comLib _http_comLib;
	private boolean _ka;
	private byte[] buff;
	//public cgiProcessor(){
	//	buff = new byte[1024];
	//	_http_comLib = new http_comLib(buff);
	//}
	public cgiProcessor(boolean ka,byte[] _buff){
		buff=_buff;
		_ka=ka;
		_http_comLib = new http_comLib(_ka,buff);
	}

	public void exec(Socket sock,String cmd,HttpReader hrd,HttpInputStream hin_r){
		System.out.println("cgiProcessor :#1 cmd="+cmd);
		String prog="";
		String dir_s="";
		String[] cmd_d=cmd.split("\\/");
		for (int i=0;i < cmd_d.length-1;i++){
			if(dir_s.equals("")==true){
				dir_s=cmd_d[i];
			}
			else{
				dir_s+="/"+cmd_d[i];
			}
		}
		prog=cmd_d[cmd_d.length-1];
		String parm=hrd.parm;
		
		File dir = new File(Defs.cgi_rootDir+"/"+dir_s);
		//エラー出力ログ
		File err_log;
		//エラー出力ログ
		if(Defs.error_log.equals("")==false){
			err_log = new File(Defs.error_log);
			try {
				System.setErr(new PrintStream(err_log));
			} catch (FileNotFoundException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
		else{
			err_log = new File("/dev/null");
		}
		
		boolean f=false;
		try{
			// http response の送信 stream
			OutputStream outs = sock.getOutputStream();
			
			//ProcessBuilder pb = new ProcessBuilder("perl",prog);
			ProcessBuilder pb = new ProcessBuilder("./"+prog);
			Map<String, String> env = pb.environment();

			env.clear();
			
			if(hrd.http_hd.get("Content-Type:") != null){
				env.put("CONTENT_TYPE", hrd.http_hd.get("Content-Type:"));
			}
			if(hrd.http_hd.get("Content-Length:") != null){
				env.put("CONTENT_LENGTH", hrd.http_hd.get("Content-Length:"));
			}
			
//--- start put env
			//SCRIPT_NAME ： /cgi-bin/net-tosa/hello.cgi
			//SERVER_NAME ： www6.net-tosa.cxm
			//SERVER_ADMIN ： webmaster@net-tosa.cxm
			//HTTP_ACCEPT_ENCODING ： gzip, deflate
			if(hrd.http_hd.get("Accept-Encoding:") != null){
				env.put("HTTP_ACCEPT_ENCODING", hrd.http_hd.get("Accept-Encoding:"));
			}
			//HTTP_CONNECTION ： keep-alive
			if(hrd.http_hd.get("Connection:") != null){
				env.put("HTTP_CONNECTION", hrd.http_hd.get("Connection:"));
			}
			//REQUEST_METHOD ： GET
			env.put("REQUEST_METHOD", hrd.method);

			//HTTP_ACCEPT ： text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
			if(hrd.http_hd.get("Accept:") != null){
				env.put("HTTP_ACCEPT", hrd.http_hd.get("Accept:"));
			}
			//SCRIPT_FILENAME ： /var/www/cgi-bin/net-tosa/hello.cgi
			//SERVER_SOFTWARE ： Apache
			env.put("SERVER_SOFTWARE", "Http_pi");
			//QUERY_STRING ： parm=ok
			env.put("QUERY_STRING", parm);
			//REMOTE_PORT ： 34985
			//HTTP_USER_AGENT ： Mozilla/5.0 (X11; Linux x86_64; rv:31.0) Gecko/20100101 Firefox/31.0
			if(hrd.http_hd.get("User-Agent:") != null){
				env.put("HTTP_USER_AGENT", hrd.http_hd.get("User-Agent:"));
			}
			//SERVER_PORT ： 80
			//SERVER_SIGNATURE ：
			//HTTP_ACCEPT_LANGUAGE ： ja,en-us;q=0.7,en;q=0.3
			if(hrd.http_hd.get("Accept-Language:") != null){
				env.put("HTTP_ACCEPT_LANGUAGE", hrd.http_hd.get("Accept-Language:"));
			}
			//REMOTE_ADDR ： 192.168.1.150
			//SERVER_PROTOCOL ： HTTP/1.1
			if(hrd.req_line.get("version").equals("")==false){
				env.put("SERVER_PROTOCOL", hrd.req_line.get("version"));
			}
			//PATH ： /sbin:/usr/sbin:/bin:/usr/bin
			//REQUEST_URI ： /cgi-bin/net-tosa/hello.cgi?parm=ok
			//GATEWAY_INTERFACE ： CGI/1.1
			//SERVER_ADDR ： 192.168.1.150
			//DOCUMENT_ROOT ： /var/www/html
			env.put("DOCUMENT_ROOT", Defs.doc_rootDir);
			//HTTP_HOST ： www6.net-tosa.cxm
			if(hrd.http_hd.get("Host:") != null){
				env.put("HTTP_HOST", hrd.http_hd.get("Host:"));
			}
//-- end put env
			
			if(hrd.http_hd.get("Cookie:") != null){
				env.put("HTTP_COOKIE", hrd.http_hd.get("Cookie:"));
			}
			
			pb.directory(dir);

			//エラー出力を標準出力とをマージ出力します
			//pb.redirectErrorStream(true);

			//エラー出力をファイルに出力
			pb.redirectError(Redirect.appendTo(err_log));
			
			Process p = pb.start();
			
			// cgi 出力
			InputStream is = p.getInputStream();
			// エラー出力
			//InputStream es = p.getErrorStream();

			//System.out.println("cgiProcessor :#2 passed");
			//int len=System.in.available();
			//if(len <= 0){
			//	len=2048;
			//}
			//System.out.println("cgiProcessor :#3 len="+len);
			//byte[] buf=new byte[len];
			byte[] buf = buff;

			if(hrd.method.equals("POST")==true){
				// cgi 入力
				DataOutputStream os = new DataOutputStream(p.getOutputStream());
				int n=0;
				int data_l;
				// POST データを cgi の標準入力に転送
				while (hin_r.ready() == true && n < 20000){
					data_l=hin_r.readBlock(buf,buf.length);
					os.write(buf,0,data_l);
					n++;
				}
				os.close();
			}
			// エラー出力の受け取り
			//while ((len = es.read(buf)) != -1 ) {
			//	System.out.println("cgiProcessor :#4 passed");
			//	if(f == false){
			//		outs.writeBytes(http_hd_ng);
			//		f=true;
			//	}
			//    outs.write(buf,0,len);;
			//}

			if(f == false){
				System.out.println("cgiProcessor :#6 passed");
				// context出力の受け取り & http response の送信
				_http_comLib.putHttpResponse(is,outs);
			}
			System.out.println("cgiProcessor :#7 passed");
		}
		catch(IOException e){
			//プログラムロードエラー
			System.err.println("cgiProcessor :#90 error ="+e);
		}
	}
}
