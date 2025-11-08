package host_app_test;

import app_lib.Url_connect;
import user_cgi_lib.userCgiMod;

public class MyLed_app_mod extends userCgiMod{

	public MyLed_app_mod() {
		// TODO 自動生成されたコンストラクター・スタブ
		super();
	}

	@Override
	public void start() {
		// TODO 自動生成されたメソッド・スタブ
		// TODO 自動生成されたメソッド・スタブ
		setHeader();	//ヘッダーは、必ず自分で送って下さい。
		// ここからは、 html を Client マシン(ブラウザー)へ送っています。
		ht_print("Content-Type: text/html\r\n\r\n");	// 最後の、"\r\n\r\n" を忘れずに。

		//String	urlName="http://192.168.1.150/class-mod/mod_test.test1_cgi_mod";
		String	urlName="http://192.168.1.180/class-bin/MyLed_cgi";
		Url_connect urlc=new Url_connect();
		if(urlc.doPost(urlName, "parm=hello")==true){
			ht_print(urlc.htmls);
		}
		else{
			ht_print(urlc.errors);
		}
	}

}
