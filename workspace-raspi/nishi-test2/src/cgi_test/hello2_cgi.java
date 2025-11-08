/**
 * Raspberry Pi C ligth http server
 * v1.3.2.1 update 2015.11.15
 * This is a java cgi program Sample.
 * cgi_test.hello2_cgi.java
 *  test for Raspberry Pi C light http server v1.3.2.1
 */
package cgi_test;

import java.util.Enumeration;

import user_cgi_lib.CGILite;
import user_cgi_lib.MyLog;

public class hello2_cgi {

	public static void main(String[] args) {
		MyLog mlog = new MyLog();

		CGILite cgi = new CGILite();	// use for GET,POST(non multipart/form-data)
		//CGI cgi = new CGI();	// use for GET,POST(multipart/form-data)

		boolean DEBG=false;
		
		mlog.put(DEBG,"hello2_cgi #1 passed");


		//ファイルアップロード等で大きなファイルの取り込みの際には、System.out へのログ表示を行わないで下さい。
		//親プロセスの STDINが一杯になって、データの受信が途中で、止まります。
		cgi.in();

		System.out.print("Content-Type: text/html\r\n\r\n");
		
		mlog.put(DEBG,"hello2_cgi #1 passed");


		cgi.pr_as("<!DOCTYPE html>\n"
			+"<html lang=\"ja\">\n"
			+"<head>\n"
			+"<meta charset=\"utf-8\">\n"
			+"<meta name=\"viewport\" content=\"width=device-width\">\n"
			+"<title>hello2_cgi.class for v1.7 & v1.8</title>\n"
			+"<style type=\"text/css\">\n"
			+"<!--\n"
			+"body{\n"
			+" font-size:16px;\n"
			+"}\n"
			+"-->\n"
			+"</style>\n"
			+"</head>\n"
			+"<body>\n"
			+"hello2 java for v1.7 & v1.8<br />\n"
			+"<br />\n"
			+"java hello2_cgi.class excuting<br />\n"
			+"use CGILite.class for accept GET,POST data<br />"
			+"--- parm ----<br />");

		for (String parm : cgi.parms.keySet()) {
            cgi.pr_kj(parm+"="+cgi.parms.get(parm)+"<br />\n");
        }		
		cgi.pr_as("---- parm end ----<br />\n"
        +"</body>\n"
		+"</html>\n");
		//try {
		//	System.in.close();
		//	System.err.close();
		//	System.out.close();
		//} catch (IOException e) {
			// TODO 自動生成された catch ブロック
		//	e.printStackTrace();
		//}
		//System.exit(0);
		mlog.put(DEBG,"hello2_cgi #99 passed");

	}
}
