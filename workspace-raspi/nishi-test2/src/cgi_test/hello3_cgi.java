/**
 * Raspberry Pi C light http server
 * v1.3.2.1 update 2015.11.15
 * This is a java cgi program Sample.
 * cgi_test.hello3_cgi.java
 *  test for Raspberry Pi C light http server v1.3.2.1
 */
package cgi_test;

import user_cgi_lib.CGI;
/*
 * hello3_cgi.java
 * java program with system-environment-variables
 */
public class hello3_cgi {

	public static void main(String[] args) {

		//try {
		//	System.setOut(new java.io.PrintStream(new java.io.FileOutputStream("./log.txt", true)));
		//}catch (FileNotFoundException e) {
			// TODO 自動生成された catch ブロック
		//	e.printStackTrace();
		//} 
		
		CGI cgi = new CGI();			// use for GET,POST(multipart/form-data)
		//CGILite cgi = new CGILite();	// use for GET,POST(non multipart/form-data)

		//System.out.print("XXX") or cgi.pr_as(),cgi.pr(),cgi_pr_kj()
		System.out.print("Content-Type: text/html\r\n\r\n");

		//ファイルアップロード等で大きなファイルの取り込みの際には、System.out へのログ表示を行わないで下さい。
		//親プロセスの STDINが一杯になって、データの受信が途中で、止まります。
		cgi.in();

		cgi.pr_as(
		"<html>\n"
		+"<head>\n"
		+"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n"
		+"<meta http-equiv=\"Content-Language\" content=\"ja\">\n"
		+"<title>hello3_cgi.class for v1.7</title>\n"
		+"</head>\n"
		+"<body>\n"
		+"hello3 java for v1.7<br />\n"
		+"<br />"
		+"java hello3_cgi.class excuting<br />\n"
		+"use CGI.class for accept GET,POST data<br />"
		+"------<br />\n");
		
		//Map<String, String> env = System.getenv();

		for (String parm : cgi.parms.keySet()) {
            //System.out.print(parm+"="+cgi.parms.get(parm)+"<br />\n");
            cgi.pr_as(parm+"="+cgi.parms.get(parm)+"<br />\n");
        }		
		
		System.out.print("------<br />\n");
		for (String envName : cgi.env.keySet()) {
            System.out.format("%s=%s%n<br />\n",envName,cgi.env.get(envName));
        }		
		System.out.print("</body>\n");
		System.out.print("</html>\n");
		System.out.close();
	}
}
