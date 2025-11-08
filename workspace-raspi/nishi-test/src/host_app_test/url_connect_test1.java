package host_app_test;

import app_lib.Url_connect;

public class url_connect_test1 {

	public static void main(String[] args) {
		// TODO 自動生成されたメソッド・スタブ
		String	urlName="http://192.168.1.150/class-mod/mod_test.test1_cgi_mod";
		Url_connect urlc=new Url_connect();
		
		if(urlc.doPost(urlName, "parm=hello")==true){
			System.out.print(urlc.htmls);
		}
		else{
			System.out.print(urlc.errors);
		}
	}
}
