import java.io.IOException;

import user_cgi_lib.CGILite;
import user_cgi_lib.CommandExec;

/**
 * Raspberry Pi c light http server
 * v1.0
 * Shutdown_cgi.java
 * @author nishi
 */
public class Shutdown_cgi{
	/**
	 * 
	 */
	// 初期値は、static にしないと jni では、NG
	static String yid="raspi";
	static String ypass="raspi";
	public Shutdown_cgi() {
		// TODO 自動生成されたコンストラクター・スタブ
		super();
	}

	public void start() {
		CGILite cgi = new CGILite();	// use for GET,POST(non multipart/form-data)
		String yid="";
		String ypass="";

		//setHeader();	//ヘッダーは、自分で送って下さい。
		System.out.print("Content-Type: text/html\r\n\r\n");

		cgi.in();
		
		//if(true){
			//ブラウザーから渡されたパラーメータを一覧表示します
		//	for (String parm : parm_hash.keySet()) {
		//		ht_print(parm+"="+parm_hash.get(parm)+"<br />\n");
		//	}
		//}
		
		String begin=cgi.parms.get("begin");
		//System.out.println("Shutdown_cgi_mod::start() : #2 begin="+begin+"<br />");
		if(begin == null || begin.equals("1") == false){
			disp_Shutdown_cgi_mod("","","");
		}
		else{
			//System.out.println("Shutdown_cgi_mod::start() : #4 <br />");
			if(cgi.parms.get("yid") != null){
				yid=cgi.parms.get("yid");
			}
			if(cgi.parms.get("ypass") != null){
				ypass=cgi.parms.get("ypass");
			}
			//System.out.println("Shutdown_cgi_mod::start() : #5 yid="+yid+",ypass="+ypass+"<br />");
			if(yid.equals(Shutdown_cgi.yid) == false || ypass.equals(Shutdown_cgi.ypass) == false){
				String emsg="id or password missing!";
				disp_Shutdown_cgi_mod(yid,ypass,emsg);
			}
			else{
				shutdown_exec();
			}
		}
	}
	/*
	 * shutdown_exec()
	 */
	private void shutdown_exec(){
		CommandExec cmde = new CommandExec();
		String cmd_s[] = {"shutdown","-h","now"};
		int rc;
		String msg="shutdown ....";
		
		String htmls="<!DOCTYPE html>\n"
		+"<html lang=\"ja\">\n"
		+"<head>\n"
		+"<meta charset=\"utf-8\">\n"
		+"<meta name=\"viewport\" content=\"width=device-width\">\n"
		+"<title>Shutdown2_cgi_mod.class</title>\n"
		
		+"</head>\n"
		+"<body>\n"
		+"<p>Raspberry Pi Java Web Server</p>\n"
		+"Shutdown Raspberry Pi machine.<br />\n";
		if(msg!=""){
			htmls+="<p style=\"color:blue\">"+msg+"</p>\n";
		}
		htmls+="</form>\n"
		+"</body>\n"
		+"</html>\n";
		System.out.print(htmls);

		//rc = cmde.command_exec_noresp(cmd_s);
	}
	/*
	 * disp_Shutdown_cgi_mod()
	 */
	private void disp_Shutdown_cgi_mod(String yid,String ypass,String emsg){
		// TODO 自動生成されたメソッド・スタブ
		//System.out.println("Shutdown_cgi_mod::disp_Shutdown_cgi_mod() : #1 called!");

		String htmls="<!DOCTYPE html>\n"
		+"<html lang=\"ja\">\n"
		+"<head>\n"
		+"<meta charset=\"utf-8\">\n"
		+"<meta name=\"viewport\" content=\"width=device-width\">\n"
		+"<title>Shutdown2_cgi_mod.class</title>\n"
		
		+"</head>\n"
		+"<body>\n"
		+"<p>Raspberry Pi Java Web Server</p>\n"
		+"Shutdown Raspberry Pi machine.<br />\n";
		if(emsg!=""){
			htmls+="<p style=\"color:red\">"+emsg+"</p>\n";
		}
		htmls+="<script Language=\"JavaScript\">\n"
		+"<!--\n"
		+"function FrontPage_Form1_Validator(theForm)\n"
		+"{\n"
		+"    //alert(\" passed #1 \");\n"
		+"  if (theForm.yid.value == \"\")\n"
		+"  {\n"
		+"    alert(\"\\\"id\\\" フィールドに値を入力してください。\");\n"
		+"    theForm.yid.focus();\n"
		+"    return (false);\n"
		+"  }\n"
		+"  if (theForm.yid.value.length < 4)\n"
		+"  {\n"
		+"    alert(\"\\\"id\\\" フィールドには 4 文字以上の文字を入力してください。\");\n"
		+"    theForm.yid.focus();\n"
		+"    return (false);\n"
		+"  }\n"
		+"  if (theForm.yid.value.length > 12)\n"
		+"  {\n"
		+"    alert(\"\\\"id\\\" フィールドには 12 文字以下の文字を入力してください。\");\n"
		+"    theForm.yid.focus();\n"
		+"    return (false);\n"
		+"  }\n"
		+"  var checkOK = \"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789--_\";\n"
		+"  var checkStr = theForm.yid.value;\n"
		+"  var allValid = true;\n"
		+"  for (i = 0;  i < checkStr.length;  i++)\n"
		+"  {\n"
		+"    ch = checkStr.charAt(i);\n"
		+"    for (j = 0;  j < checkOK.length;  j++)\n"
		+"      if (ch == checkOK.charAt(j))\n"
		+"        break;\n"
		+"    if (j == checkOK.length)\n"
		+"    {\n"
		+"      allValid = false;\n"
		+"      break;\n"
		+"    }\n"
		+"  }\n"
		+"  if (!allValid)\n"
		+"  {\n"
		+"    alert(\"\\\"id\\\" フィールドには 英数字および \\\"-_\\\" を入力してください。\");\n"
		+"    theForm.yid.focus();\n"
		+"    return (false);\n"
		+"  }\n"
		+"  if (theForm.ypass.value == \"\")\n"
		+"  {\n"
		+"    alert(\"\\\"password\\\" フィールドに値を入力してください。\");\n"
		+"    theForm.ypass.focus();\n"
		+"    return (false);\n"
		+"  }\n"
		+"  if (theForm.ypass.value.length < 4)\n"
		+"  {\n"
		+"    alert(\"\\\"password\\\" フィールドには 4 文字以上の文字を入力してください。\");\n"
		+"    theForm.ypass.focus();\n"
		+"    return (false);\n"
		+"  }\n"
		+"  if (theForm.ypass.value.length > 16)\n"
		+"  {\n"
		+"    alert(\"\\\"password\\\" フィールドには 16 文字以下の文字を入力してください。\");\n"
		+"    theForm.ypass.focus();\n"
		+"    return (false);\n"
		+"  }\n"
		+"  var checkOK = \"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789--_\";\n"
		+"  var checkStr = theForm.ypass.value;\n"
		+"  var allValid = true;\n"
		+"  for (i = 0;  i < checkStr.length;  i++)\n"
		+"  {\n"
		+"    ch = checkStr.charAt(i);\n"
		+"    for (j = 0;  j < checkOK.length;  j++)\n"
		+"      if (ch == checkOK.charAt(j))\n"
		+"        break;\n"
		+"    if (j == checkOK.length)\n"
		+"    {\n"
		+"      allValid = false;\n"
		+"      break;\n"
		+"    }\n"
		+"  }\n"
		+"  if (!allValid)\n"
		+"  {\n"
		+"    alert(\"\\\"password\\\" フィールドには 英数字および \\\"-_\\\" を入力してください。\");\n"
		+"    theForm.ypass.focus();\n"
		+"    return (false);\n"
		+"  }\n"
		+"  return (true);\n"
		+"}\n"
		+"//-->\n"
		+"</script>\n"
		+"<form method=\"POST\" action=\"/class-bin/Shutdown_cgi\" onsubmit=\"return FrontPage_Form1_Validator(this)\" name=\"FrontPage_Form1\">\n"
		+"  <table border=\"0\" cellpadding=\"2\" cellspacing=\"0\" width=\"62%\">\n"
		+"    <tr>\n"
		+"      <td width=\"17%\" align=\"right\">id:</td>\n"
		+"      <td width=\"83%\">\n"
		+"        <!--webbot bot=\"Validation\" S-Display-Name=\"id\"\n"
		+"        S-Data-Type=\"String\" B-Allow-Letters=\"TRUE\" B-Allow-Digits=\"TRUE\"\n"
		+"        S-Allow-Other-Chars=\"-_\" B-Value-Required=\"TRUE\" I-Minimum-Length=\"4\"\n"
		+"        I-Maximum-Length=\"12\" -->\n"
		+"        <input type=\"text\" name=\"yid\" size=\"20\" value=\""+yid+"\" maxlength=\"12\">\n"
		+"        (半角英数字 4〜12文字)</td>\n"
		+"    </tr>\n"
		+"    <tr>\n"
		+"      <td width=\"17%\" align=\"right\">password:</td>\n"
		+"      <td width=\"83%\">\n"
		+"        <!--webbot bot=\"Validation\" S-Display-Name=\"password\"\n"
		+"        S-Data-Type=\"String\" B-Allow-Letters=\"TRUE\" B-Allow-Digits=\"TRUE\"\n"
		+"        S-Allow-Other-Chars=\"-_\" B-Value-Required=\"TRUE\" I-Minimum-Length=\"4\"\n"
		+"        I-Maximum-Length=\"16\" -->\n"
		+"        <input type=\"password\" name=\"ypass\" size=\"20\" value=\""+ypass+"\" maxlength=\"16\">\n"
		+"        (半角英数字 4〜16文字)</td>\n"
		+"    </tr>\n"
		+"  </table>\n"
		+"  <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"24%\" style=\"margin: 20px 0 0 40px;\">\n"
		+"    <tr>\n"
		+"      <td align=\"center\">\n"
		+"        <input type=\"submit\" value=\"Go shutdown\" name=\"go_shutdown\" />\n"
		+"      </td>\n"
		+"      <td align=\"center\">\n"
		+"        <input type=\"reset\" value=\"Reset\" name=\"B2\" />\n"
		+"      </td>\n"
		+"    </tr>\n"
		+"  </table>\n"
		+"  <input type=\"hidden\" name=\"begin\" value=\"1\" />\n"
		+"</form>\n"
		+"<p><a href=\"/\">Site Top</a></p>\n"
		+"</body>\n"
		+"</html>\n";
		System.out.print(htmls);
	}
	public static void main(String[] args) throws InterruptedException {
		Shutdown_cgi cgi=new Shutdown_cgi();
		cgi.start();
	}
}

