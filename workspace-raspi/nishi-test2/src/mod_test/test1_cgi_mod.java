package mod_test;
import user_cgi_lib.userCgiMod2;

/**
 * @author nishi
 *  test1_cgi_mod
 *  program test1
 */
public class test1_cgi_mod extends userCgiMod2{
	/**
	 * Java User cgi_module サンプルです。
	 *  Java User cgi_module Base Class(user_cgi_lib.userCgiMod.java) を継承して、start() メソッドを
	 *  オーバーライトして、自分の Java User CGI モジュールを作成すると簡単です。
	 *  又、全てを自分で作成する事もできます。
	 *  その場合も、userCgiMod.java が参考になるでしょう。
	 */
	public test1_cgi_mod() {
		// TODO 自動生成されたコンストラクター・スタブ
		super();
	}
	/*
	 * ここから、Java user cgi module を、開始します。
	 *  test1_cgi_mod の開始
	 */
	public void start(){
		//setHeader();	//ヘッダーは、必ず自分で送って下さい。
		// ここからは、 html を Client マシン(ブラウザー)へ送っています。
		ht_print("Content-Type: text/html\r\n\r\n");	// 最後の、"\r\n\r\n" を忘れずに。

		String resp_s="<!DOCTYPE html>\n"
		+"<html lang=\"ja\">\n"
		+"<head>\n"
		+"<meta charset=\"utf-8\">\n"
		+"<meta name=\"viewport\" content=\"width=device-width\">\n"
		+"<title>mod_test.test1_cgi_mod.class</title>\n"
		+"<style type=\"text/css\">\n"
		+"<!--\n"
		+"body{\n"
		+" font-size:16px;\n"
		+"}\n"
		+"-->\n"
		+"</style>\n"
		
		+"</head>\n"
		+"<body>\n"
		+"/class-mod/mod_test.test1_cgi_mod<br />\n"
		+"java test1_cgi_mod.class を実行してします。<br />\n"
		//ht_print("method="+method+"<br />\n");
		//ht_print("parm="+parm+"<br />\n");
		+"<hr />\n"
		+"------parm list-----<br />\n";
		ht_print(resp_s);
		//ブラウザーから渡されたパラーメータを一覧表示します。
		for (String parm : parm_hash.keySet()) {
			ht_print(parm+"="+parm_hash.get(parm)+"<br />\n");
        }		
		resp_s="<hr />\n"
		+"</body>\n"
		+"</html>\n";
		ht_print(resp_s);
	}
}
