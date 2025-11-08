package mod_test;
/**
 * simple_cgi_mod.java
 * C から JNI を使ってコールするテスト用の java クラスです。
 * call する c プログラムは、
 * nishi-test-c/java_class_call_test.c
 */

public class simple_cgi_mod {

	public simple_cgi_mod() {
		// TODO 自動生成されたコンストラクター・スタブ
	}
	public void cgi_go2(String method,String parm){
		//System.out.println("simple_cgi_mod:cgi_go2 :#1 called!!");
		//System.out.println("method="+method+",parm="+parm);
		start();
	}
	/*
	 * ここから、Java user cgi module を、開始します。
	 *  test1_cgi_mod の開始
	 */
	public void start(){
		//setHeader();	//ヘッダーは、必ず自分で送って下さい。
		// ここからは、 html を Client マシン(ブラウザー)へ送っています。
		System.out.println("Content-Type: text/html\r\n\r\n");	// 最後の、"\r\n\r\n" を忘れずに。

		System.out.println("<!DOCTYPE html>\n");
		System.out.println("<html lang=\"ja\">\n");
		System.out.println("<head>\n");
		System.out.println("<meta charset=\"utf-8\">\n");
		System.out.println("<meta name=\"viewport\" content=\"width=device-width\">\n");
		System.out.println("<title>mod_test.test1_cgi_mod.class</title>\n");
		System.out.println("<style type=\"text/css\">\n");
		System.out.println("<!--\n");
		System.out.println("body{\n");
		System.out.println(" font-size:16px;\n");
		System.out.println("}\n");
		System.out.println("-->\n");
		System.out.println("</style>\n");
		
		System.out.println("</head>\n");
		System.out.println("<body>\n");
		System.out.println("/class-mod/mod_test.test1_cgi_mod<br />\n");
		System.out.println("java test1_cgi_mod.class を実行してします。<br />\n");
		//System.out.println("method="+method+"<br />\n");
		//System.out.println("parm="+parm+"<br />\n");
		System.out.println("<hr />\n");
		System.out.println("------parm list-----<br />\n");

		//ブラウザーから渡されたパラーメータを一覧表示します。
		//Enumeration<String> keys = parm_hash.keys();
		//while(keys.hasMoreElements()) {
		//	String key = keys.nextElement();
		//	ht_print(key + "=" + parm_hash.get(key)+"<br />\n");
		//}
		System.out.println("<hr />\n");
		System.out.println("</body>\n");
		System.out.println("</html>\n");
	}}
