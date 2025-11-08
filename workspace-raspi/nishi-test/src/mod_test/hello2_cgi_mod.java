package mod_test;
/**
 * Raspberry Pi java http server
 * User java cgi module test program
 * hello_cgi_mod.java
 * http://192.168.1.x/class-mod/mod_test.test1_cgi_mod
 */


import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;

public class hello2_cgi_mod{
	public hello2_cgi_mod(){
	}
	public void cgi_go(DataOutputStream outs,String method,String parm){
		//System.out.println("hello2_cgi_mod:cgi_go :#1 called");
		//try{
			Date now = new Date();
			String http_hd_ok=
					"HTTP/1.1 200 OK\r\n"
					+"Date: "+now+"\r\n"
					+"Server: raspberry-pi\r\n"
					//+"Content-type: text/html\r\n\r\n" // 注）最後は \r\nが２個必要
					;
			//DataOutputStream outs = new DataOutputStream(sock.getOutputStream());
			//Writer out = new OutputStreamWriter(outs);

			// STDOUTのバックアップ& リダイレクト
			PrintStream sysOutBkup = System.out;
			System.setOut(new java.io.PrintStream(outs));

			System.out.print(http_hd_ok);
			System.out.print("Content-Type: text/html\r\n\r\n");

			System.out.print("<!DOCTYPE html>\n");
			System.out.print("<html lang=\"ja\">\n");
			System.out.print("<head>\n");
			System.out.print("<meta charset=\"shift_jis\">\n");
			System.out.print("<meta name=\"viewport\" content=\"width=device-width\">\n");
			System.out.print("<title>hello2_cgi_mod.class</title>\n");
			System.out.print("<style type=\"text/css\">\n");
			System.out.print("<!--\n");
			System.out.print("body{\n");
			System.out.print(" font-size:16px;\n");
			System.out.print("}\n");
			System.out.print("-->\n");
			System.out.print("</style>\n");
			System.out.print("</head>\n");
			System.out.print("<body>\n");
			System.out.print("hello java<br />\n");
			System.out.print("java hello2_cgi_mod.class excuting<br />\n");
			System.out.print("method="+method+"<br />\n");
			System.out.print("parm="+parm+"<br />\n");

			//for(int ii=0;ii<args.length;ii++){
			//	outs.writeBytes("args["+ii+"]="+args[ii]+"<br />\n");
			//}

			System.out.print("</body>\n");
			System.out.print("</html>\n");
			//outs.flush();
			System.setOut(sysOutBkup);

		//}
		//catch(IOException e){
		//	System.out.println("hello_cgi_mod:cgi_go :#90 error ="+e);
		//}
	}
}
