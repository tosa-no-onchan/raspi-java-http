package app_lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Url_connect {
	private URL url;
	public String htmls;
	public String errors;
	public String content_type;
	public Url_connect() {
		// TODO 自動生成されたコンストラクター・スタブ
	}
	public boolean doPost(String urls,String parm){
		boolean rc=true;
		htmls="";
		errors="";
		content_type="";
		try {
			url = new URL(urls);
			URLConnection uc = url.openConnection();
			uc.setDoOutput(true);	//POST可能にする
			uc.setRequestProperty("User-Agent", "Raspberry pi ap");	// ヘッダを設定
			uc.setRequestProperty("Accept-Language", "ja");	// ヘッダを設定
			OutputStream os = uc.getOutputStream();	//POST用のOutputStreamを取得
			PrintStream ps = new PrintStream(os);
			ps.print(parm);	//データをPOSTする
			ps.close();
			
			InputStream is = uc.getInputStream();	//POSTした結果を取得
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String s;
			while ((s = reader.readLine()) != null) {
				htmls+=s+"\n";
			}
			content_type=uc.getContentType();
			reader.close();
		}
		catch (MalformedURLException e) {
			System.out.println("Url_connect::doPost() :#9 error e="+e);
			errors="Url_connect::doPost() :#9 error "+e;
			rc=false;
		}
		// file not found
		catch (IOException e) {
			System.out.println("Url_connect::doPost() :#10 error e="+e);
			errors="Url_connect::doPost() :#10 error "+e;
			rc=false;
		}
		return rc;
	}
}
