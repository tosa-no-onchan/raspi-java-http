/**
 * Raspberry Pi java http server
 * v1.7 update 2015.2.17
 * java User cgi program Common Class
 * user_cgi_lib.CGI.java
 *  use this class for POST & enctype="multipart/form-data" data receive,such as perl CGI.pm.
 */
package user_cgi_lib;


//import http_pi.Defs;
//import http_pi.HttpDataInputStream;
//import http_pi.HttpReaderBuff;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CGI {
	public Map<String, String> env;
	public String method="";
	public String parm="";
	public Map<String,String> parms = new HashMap<String,String>();

	DataOutputStream outs;
	
	/*
	 * CGI()
	 */
	public CGI() {
		// TODO 自動生成されたコンストラクター・スタブ
		outs = new DataOutputStream(System.out);
		env = System.getenv();
	}
	/*
	 * in()
	 */
	public void in(){
		//REQUEST_METHOD
		if(env.get("REQUEST_METHOD")!= null){
			method=env.get("REQUEST_METHOD");
		}
		if(method.equals("GET")==true){
			get_req();
		}
		else if(method.equals("POST")==true){
			// POST req
			HttpReaderCGI hrdCGI = new HttpReaderCGI(env);
			hrdCGI.read();
			parm=hrdCGI.p_data;
			if(parm.equals("")==false){
				setParms();
			}
		}
	}
	/*
	 * get_req()
	 */
	private void get_req(){
		//QUERY_STRING
		if(env.get("QUERY_STRING")!= null){
			parm=env.get("QUERY_STRING");
			setParms();
		}
	}
	/*
	 * setParms()
	 *  parm をHashMapに変換します。-> Map<String,String> parms
	 *  1) multi-select の場合
	 *   parm1=neko&parm1=yagi&parm1=ushi -> parm1="neko"+0x00+"yagi"+0x00+"ushi"
	 */
	public void setParms(){
		String[] parmx = parm.split("&");
		for(int i=0;i< parmx.length;i++){
			String[] _parmx = parmx[i].split("=");
			if(_parmx.length>=2){
				//System.out.print(_parmx[0]+"="+_parmx[1]+"<br />\n");
				if(parms.containsKey(_parmx[0])){
					String s=parms.get(_parmx[0])+0x00+_parmx[1];
					parms.put(_parmx[0], s);
				}
				else{
					parms.put(_parmx[0], _parmx[1]);
				}
			}
			else{
				//System.out.print(parmx[i]+"<br />\n");
			}
		}
	}
	
	/*
	 * pr_as(String line)
	 *  ascii データ の Client への送信
	 */
	public void pr_as(String line){
		try {
			outs.writeBytes(line);
		}
		catch (IOException e) {
			System.out.println("CGI::pr_as() :#1 DataOutputStream writeBytes() error="+e+"<br />");
		}
		
	}
	/*
	 * pr_kj(String line)
	 *  Kanji データ の Client への送信
	 */
	public void pr_kj(String line){
		try {
			byte[] line_b = line.getBytes("utf-8");
			outs.write(line_b);
		}
		catch (IOException e) {
			System.out.println("CGI::pr_kj() :#1 DataOutputStream writeBytes() error="+e+"<br />");
		}
	}
	/*
	 * pr(String line)
	 *  String の Client への送信
	 *  半角、倍角自動判定します
	 */
	public void pr(String line){
		try{
			char[] chars = line.toCharArray();
			boolean kanj_f=false;
			for (int i = 0; i < chars.length; i++) {
				if (String.valueOf(chars[i]).getBytes().length >= 2) {
					// 倍角
					kanj_f=true;
					byte[] line_b =line.getBytes("utf-8");
					//System.out.println("CGI::pr() :#1 passed!!<br />");
					outs.write(line_b);
					break;
				}
			}
			if(kanj_f==false){
				//System.out.println("CGI::pr() :#2 passed!!<br />");
				outs.writeBytes(line);
			}
		}
		catch(IOException e){
			System.out.println("CGI::pr() :#3 DataOutputStream writeBytes() error="+e+"<br />");
		}
	}
}
