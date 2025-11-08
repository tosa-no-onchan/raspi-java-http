package http_pi;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

public class hello_cgi_mod {
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
			outs.writeBytes("<html>\n");
			outs.writeBytes("<head>\n");
			outs.writeBytes("<title>hello_cgi_mod.class</title>\n");
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
