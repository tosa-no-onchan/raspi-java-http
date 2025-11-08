/**
 * Raspberry Pi java http server
 * built in class test program
 * built_in_app.hello_bin.java
 * http://192.168.1.x/built-in/hello or hello2
 */

package built_in_app;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;

import user_cgi_lib.userBuiltin;

/**
 * @author nishi
 *
 */
public class hello_bin extends userBuiltin {

	public hello_bin(){
		super();
	}
	/*
	 * hello()
	 * sample for own coding.
	 */
	public void hello(DataOutputStream outs,String method,String parm) {
		// TODO 自動生成されたコンストラクター・スタブ
		System.out.println("hello_bin::hello() :#1 called");
		try{
			Date now = new Date();
			String http_hd_ok=
					"HTTP/1.1 200 OK\r\n"
					+"Date: "+now+"\r\n"
					+"Server: raspberry-pi\r\n"
					//+"Content-type: text/html\r\n\r\n" // 注）最後は \r\nが２個必要
					;
			//DataOutputStream outs = new DataOutputStream(sock.getOutputStream());
			//Writer out = new OutputStreamWriter(outs);

			outs.writeBytes(http_hd_ok);
			outs.writeBytes("Content-Type: text/html\r\n\r\n");
			outs.writeBytes("<html>\n");
			outs.writeBytes("<head>\n");
			outs.writeBytes("<title>Built in hello_bin:hello</title>\n");
			outs.writeBytes("</head>\n");
			outs.writeBytes("<body>\n");
			outs.writeBytes("hello java<br />\n");
			outs.writeBytes("Built in app built_in_app.hello_bin:hello excuting<br />\n");
			outs.writeBytes("method="+method+"<br />\n");
			outs.writeBytes("parm="+parm+"<br />\n");

			//for(int ii=0;ii<args.length;ii++){
			//	outs.writeBytes("args["+ii+"]="+args[ii]+"<br />\n");
			//}

			outs.writeBytes("</body>\n");
			outs.writeBytes("</html>\n");
			outs.flush();
		}
		catch(IOException e){
			System.out.println("hello_bin::hello() :#90 error ="+e);
		}
	}
	/*
	 * hello2()
	 * sample for using userBuiltin.class method
	 */
	public void hello2(DataOutputStream outs,String method,String parm) {
		// TODO 自動生成されたコンストラクター・スタブ
		System.out.println("hello_bin::hello2() :#1 called");

		setParm(outs,method,parm);	// set calling parameter to parent class
		setHeader();	//ヘッダーは、必ず自分で送って下さい
		ht_print("Content-Type: text/html\r\n\r\n");	//  <-  "\r\n\r\n" を忘れずに
		ht_print("<html>\n");
		ht_print("<head>\n");
		ht_print("<title>Built in hello_bin:hello2</title>\n");
		ht_print("</head>\n");
		ht_print("<body>\n");
		ht_print("hello java<br />\n");
		ht_print("Built in app built_in_app.hello_bin:hello2 excuting<br />\n");
		ht_print("method="+method+"<br />\n");

		//ht_print("parm="+parm+"<br />\n");

		ht_print("<hr />\n");
		ht_print("------parm list-----<br />\n");

		//ブラウザーから渡されたパラーメータを一覧表示します。
		Enumeration<String> keys = parm_hash.keys();
		while(keys.hasMoreElements()) {
			String key = keys.nextElement();
			ht_print(key + "=" + parm_hash.get(key)+"<br />\n");
		}
		ht_print("<hr />\n");
		ht_print("</body>\n");
		ht_print("</html>\n");
		ht_flush();
	}
}
