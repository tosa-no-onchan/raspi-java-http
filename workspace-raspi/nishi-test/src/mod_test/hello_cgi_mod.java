package mod_test;
/**
 * Raspberry Pi java http server
 * User java cgi module test program
 * hello_cgi_mod.java
 * http://192.168.1.x/class-mod/mod_test.test1_cgi_mod
 */


import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

public class hello_cgi_mod{
	public hello_cgi_mod(){
	}
	public void cgi_go(DataOutputStream outs,String method,String parm){
		System.out.println("hello_cgi_mod:cgi_go :#1 called");
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

			outs.writeBytes("<!DOCTYPE html>\n");
			outs.writeBytes("<html lang=\"ja\">\n");
			outs.writeBytes("<head>\n");
			outs.writeBytes("<meta charset=\"shift_jis\">\n");
			outs.writeBytes("<meta name=\"viewport\" content=\"width=device-width\">\n");
			outs.writeBytes("<title>hello_cgi_mod.class</title>\n");
			outs.writeBytes("<style type=\"text/css\">\n");
			outs.writeBytes("<!--\n");
			outs.writeBytes("body{\n");
			outs.writeBytes(" font-size:16px;\n");
			outs.writeBytes("}\n");
			outs.writeBytes("-->\n");
			outs.writeBytes("</style>\n");
			outs.writeBytes("</head>\n");
			outs.writeBytes("<body>\n");
			outs.writeBytes("hello java<br />\n");
			outs.writeBytes("java hello_cgi.class excuting<br />\n");
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
			System.out.println("hello_cgi_mod:cgi_go :#90 error ="+e);
		}
	}
}
