/**
 * arduino.serial_pi_mod.java
 * with c270_mjpg_stream
 */
package arduino;

import java.net.UnknownHostException;

import app_lib.socketClient;
import user_cgi_lib.CommandExec;
import user_cgi_lib.userCgiMod;

public class serial_pi_mod extends userCgiMod {
	// c270streamd file path
	String nd_pg_dir="../nishi-test";

	//mjpg stream url
	String url_s="http://192.168.1.180:8080";
	//String url_s="http://192.168.1.150:8080";
	
	// if you want use ajax,set ajax_use=true.
	boolean ajax_use=true;

	public serial_pi_mod() {
		// TODO 自動生成されたコンストラクター・スタブ
		super();
	}
	@Override
	public void start() {
		socketClient socli= new socketClient(4002);
		String ans_s="";
		String req_s="";
		String go_reload="no";
		boolean ctl_arduino=false;
		boolean ajax_f=false;
		// TODO 自動生成されたメソッド・スタブ
		System.out.println("serial_pi_mod::start():#1 parm="+parm);
		String go_start=parm_hash.get("go_start");
		String go_stop=parm_hash.get("go_stop");
		String go_con_start=parm_hash.get("go_con_start");
		String go_con_stop=parm_hash.get("go_con_stop");
		String func=parm_hash.get("func");
		String ajax_req = parm_hash.get("ajax_req");

		if(parm_hash.get("begin")!= null){
			if(func != null && func.equals("ajax")==true && ajax_req != null){
				if(ajax_req.equals("go_up")==true){
					req_s="res=,15";
					ctl_arduino=ajax_f=true;
				}
				else if(ajax_req.equals("go_down")==true){
					req_s="res=,-15";
					ctl_arduino=ajax_f=true;
				}
				else if(ajax_req.equals("go_left")==true){
					req_s="res=15,";
					ctl_arduino=ajax_f=true;
				}
				else if(ajax_req.equals("go_right")==true){
					req_s="res=-15,";
					ctl_arduino=ajax_f=true;
				}
				else if(ajax_req.equals("go_prog")==true){
					req_s="prog";
					ctl_arduino=ajax_f=true;
				}
				else if(ajax_req.equals("go_at")==true){
					req_s="at";
					ctl_arduino=ajax_f=true;
				}
				else if(ajax_req.equals("go_home")==true){
					req_s="home";
					ctl_arduino=ajax_f=true;
				}
				else if(ajax_req.equals("go_start")==true){
					go_start="start";
					ajax_f=true;
				}
				else if(ajax_req.equals("go_stop")==true){
					go_stop="stop";
					ajax_f=true;
				}
				else if(ajax_req.equals("go_con_start")==true){
					go_con_start="go_con_start";
					ajax_f=true;
				}
				else if(ajax_req.equals("go_con_stop")==true){
					go_con_stop="go_con_stop";
					ajax_f=true;
				}
			}
			if(parm_hash.get("go_up")!= null){
				req_s="res=,15";
				ctl_arduino=true;
			}
			else if(parm_hash.get("go_down")!= null){
				req_s="res=,-15";
				ctl_arduino=true;
			}
			else if(parm_hash.get("go_left")!= null){
				req_s="res=15,";
				ctl_arduino=true;
			}
			else if(parm_hash.get("go_right")!= null){
				req_s="res=-15,";
				ctl_arduino=true;
			}
			else if(parm_hash.get("go_prog")!= null){
				req_s="prog";
				ctl_arduino=true;
			}
			else if(parm_hash.get("go_at")!= null){
				req_s="at";
				ctl_arduino=true;
			}
			else if(parm_hash.get("go_home")!= null){
				req_s="home";
				ctl_arduino=true;
			}
			if(req_s != "" && ctl_arduino==true){
				try {
					ans_s = socli.sendReq(req_s);
				}
				catch (UnknownHostException e) {
					// TODO 自動生成された catch ブロック
					//e.printStackTrace();
					System.out.println("serial_pi_mod::start():#2 error "+e);
					ans_s=e.getMessage();
				}
			}
			else if(go_start != null && go_start.equals("") == false){
				start_mjpg_stream();
				try {
					java.lang.Thread.sleep(1200);
				}
				catch (InterruptedException e) {
					// TODO 自動生成された catch ブロック
					//e.printStackTrace();
				}
				ans_s="ビデオ Start";
				go_reload="yes";
			}
			else if(go_stop != null && go_stop.trim().equals("") == false){
				stop_mjpg_stream();
				ans_s="ビデオ Stop";
				go_reload="yes";
			}
			else if(go_con_start != null && go_con_start.equals("") == false){
				start_serial_pi_con();
				try {
					java.lang.Thread.sleep(1200);
				}
				catch (InterruptedException e) {
					// TODO 自動生成された catch ブロック
					//e.printStackTrace();
				}
				ans_s="serial_pi_con Start";
			}
			else if(go_con_stop != null && go_con_stop.equals("") == false){
				stop_serial_pi_con();
				ans_s="serial_pi_con Stop";
			}
		}
		if(ajax_f==true){
			disp_xml_resp(ans_s,go_reload);
		}
		else{
			//disp_serial_pi_mod(ans_s);
			disp_c270_mjpg_stream_cgi_mod_ajax(ans_s);
		}
	}
	/*
	 * start_mjpg_stream()
	 */
	public void start_mjpg_stream(){
		int rc;
		System.out.println("c270_mjpg_stream_cgi_mod::start_mjpg_stream() : #1 passed !");
		CommandExec cmde = new CommandExec();
		String cmd_s[] = {nd_pg_dir+"/c270streamd","start"};
		rc = cmde.command_exec_noresp(cmd_s);
		if(rc!=0){
			System.out.println("c270_mjpg_stream_cgi_mod::start_mjpg_stream() : #90 error "+cmde.errors);
		}
	}
	/*
	 * stop_mjpg_stream()
	 */
	public void stop_mjpg_stream(){
		int rc;
		System.out.println("c270_mjpg_stream_cgi_mod::stop_mjpg_stream() : #1 passed !");
		CommandExec cmde = new CommandExec();
		String cmd_s[] = {nd_pg_dir+"/c270streamd","stop"};
		rc = cmde.command_exec_noresp(cmd_s);
		if(rc!=0){
			System.out.println("c270_mjpg_stream_cgi_mod::stop_mjpg_stream() : #90 error "+cmde.errors);
		}
	}
	/*
	 * start_serial_pi_con()
	 */
	public void start_serial_pi_con(){
		int rc;
		System.out.println("c270_mjpg_stream_cgi_mod::start_serial_pi_con() : #1 passed !");
		CommandExec cmde = new CommandExec();
		String cmd_s[] = {nd_pg_dir+"/serpi_con","start"};
		rc = cmde.command_exec_noresp(cmd_s);
		if(rc!=0){
			System.out.println("c270_mjpg_stream_cgi_mod::start_serial_pi_con() : #90 error "+cmde.errors);
		}
	}
	/*
	 * stop_serial_pi_con()
	 */
	public void stop_serial_pi_con(){
		int rc;
		System.out.println("c270_mjpg_stream_cgi_mod::stop_serial_pi_con() : #1 passed !");
		CommandExec cmde = new CommandExec();
		String cmd_s[] = {nd_pg_dir+"/serpi_con","stop"};
		rc = cmde.command_exec_noresp(cmd_s);
		if(rc!=0){
			System.out.println("c270_mjpg_stream_cgi_mod::stop_serial_pi_con() : #90 error "+cmde.errors);
		}
	}
	/*
	 * disp_xml_resp(String ans_s)
	 * Ajax request & response
	 */
	public void disp_xml_resp(String ans_s,String go_start){
		String htmls;
		setHeader();	//ヘッダーは、自分で送って下さい。
		ht_print("Content-Type: text/html;charset=utf-8\r\n\r\n");
		//ht_print("Content-Type: text/html\r\n\r\n");
		htmls="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
+"<lists>\n"
+"<items>\n"
+"<msg>"+ans_s+"</msg>\n"
+"<reload_req>"+go_start+"</reload_req>\n"
+"</items>\n"
+"</lists>\n";
		ht_print(htmls);
	}
	/*
	 * disp_serial_pi_mod with c270_mjpg_stream_cgi_mod_ajax()
	 */
	public void disp_c270_mjpg_stream_cgi_mod_ajax(String msg){
		String htmls;
		setHeader();	//ヘッダーは、自分で送って下さい。
		ht_print("Content-Type: text/html\r\n\r\n");
		htmls="<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\""
+" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
+"<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"
+"<head>\n"
+"<title>arduino.serial_pi_mod.class with MJPEG-Streamer</title>\n"
+"<!-- Ajaxライブラリー jQuery -->\n"
+"<script type=\"text/javascript\" src =\"/js/jquery-1.10.2.min.js\" charset=\"utf-8\"></script>\n"
+"<!-- Ajaxライブラリー raspi-netosa-jquery.js -->\n"
+"<script type=\"text/javascript\" src =\"/js/raspi-netosa-jquery.js\" charset=\"utf-8\"></script>\n"
+"<!-- Ajaxライブラリー mjpg_stream.js -->\n"
+"<script type=\"text/javascript\" src =\"/js/mjpg_stream.js\" charset=\"utf-8\"></script>\n"
+"</head>\n"
+"<script type=\"text/javascript\">\n"
+"<!--\n"
+"jq$= jQuery.noConflict();\n"
+"\n"
+"jq$(function(){\n"
+"  jq$.ajaxSetup({\n"
+"    timeout:5000\n"
+"  });\n"
+"  startImageLayer(\'"+url_s+"');\n"
+"});\n"
+"\n"
+"function re_load(){\n"
+"  startImageLayer('"+url_s+"');\n"
+"}\n"
+"\n"
+"function FrontPage_Form1_Validator(theForm)\n"
+"{\n"
+"  return (true);\n"
+"}\n"
+"//-->\n"
+"	</script>\n";
		ht_print(htmls);

htmls="	<body>\n"
+"arduino.serial_pi_mod.class with MJPEG-Streamer<br />\n"
+"	<div id=\"webcam\" style=\"height:500px;\"><noscript><img src=\""+url_s+"/?action=snapshot\" /></noscript></div>\n"
+"\n"
+"<form method=\"POST\" action=\"/class-mod/arduino.serial_pi_mod\" onsubmit=\"return FrontPage_Form1_Validator(this)\" name=\"FrontPage_Form1\">\n"
+"  <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"24%\" style=\"margin: 20px 0 0 40px;\">\n"
+"    <tr>\n"
+"      <td align=\"center\" height=\"30\">\n";
		if(ajax_use == false){
			htmls+="        <input type=\"submit\" value=\"Streaming Start\" name=\"go_start\" />\n";
		}
		else{
			htmls+="        <input type=\"button\" value=\"start\" name=\"go_start\" onClick=\"reqAjax('/class-mod/arduino.serial_pi_mod','go_start')\" />\n";
		}
htmls+="      </td>\n"
+"      <td align=\"center\" height=\"30\">\n";
		if(ajax_use == false){
			htmls+="        <input type=\"submit\" value=\"Streaming Stop\" name=\"go_stop\" />\n";
		}
		else{
			htmls+="        <input type=\"button\" value=\"stop\" name=\"go_stop\" onClick=\"reqAjax('/class-mod/arduino.serial_pi_mod','go_stop')\" />\n";
		}
htmls+="      </td>\n"
+"    </tr>\n"
+"    <tr>\n"
+"      <td align=\"center\" height=\"30\">\n";
		if(ajax_use == false){
			htmls+="        <input type=\"submit\" value=\"up\" name=\"go_up\" />\n";
		}
		else{
			htmls+="        <input type=\"button\" value=\"up\" name=\"go_up\" onClick=\"reqAjax('/class-mod/arduino.serial_pi_mod','go_up')\" />\n";
		}
htmls+="      </td>\n"
+"      <td align=\"center\" height=\"30\">\n";
		if(ajax_use == false){
			htmls+="        <input type=\"submit\" value=\"down\" name=\"go_down\" />\n";
		}
		else{
			htmls+="        <input type=\"button\" value=\"down\" name=\"go_down\" onClick=\"reqAjax('/class-mod/arduino.serial_pi_mod','go_down')\" />\n";
		}
htmls+="      </td>\n"
+"    <tr>\n"
+"      <td align=\"center\" height=\"30\">\n";
		if(ajax_use == false){
			htmls+="        <input type=\"submit\" value=\"left\" name=\"go_left\" />\n";
		}
		else{
			htmls+="        <input type=\"button\" value=\"left\" name=\"go_left\" onClick=\"reqAjax('/class-mod/arduino.serial_pi_mod','go_left')\" />\n";
		}
htmls+="      </td>\n"
+"      <td align=\"center\" height=\"30\">\n";
		if(ajax_use == false){
			htmls+="        <input type=\"submit\" value=\"right\" name=\"go_right\" />\n";
		}
		else{
			htmls+="        <input type=\"button\" value=\"right\" name=\"go_right\" onClick=\"reqAjax('/class-mod/arduino.serial_pi_mod','go_right')\" />\n";
		}
htmls+="      </td>\n"
+"    </tr>\n"
+"    <tr>\n"
+"      <td align=\"center\" height=\"30\">\n";
		if(ajax_use == false){
			htmls+="        <input type=\"submit\" value=\"home\" name=\"go_home\" />\n";
		}
		else{
			htmls+="        <input type=\"button\" value=\"home\" name=\"go_home\" onClick=\"reqAjax('/class-mod/arduino.serial_pi_mod','go_home')\" />\n";
		}
htmls+="      </td>\n"
+"      <td align=\"center\" height=\"30\">\n"
+"      </td>\n"
+"    </tr>\n"
+"    <tr>\n"
+"      <td align=\"center\" height=\"30\">\n";
		if(ajax_use == false){
			htmls+="        <input type=\"submit\" value=\"at\" name=\"go_at\" />\n";
		}
		else{
			htmls+="        <input type=\"button\" value=\"at\" name=\"go_at\" onClick=\"reqAjax('/class-mod/arduino.serial_pi_mod','go_at')\" />\n";
		}
htmls+="      </td>\n"
+"      <td align=\"center\" height=\"30\">\n";
		if(ajax_use == false){
			htmls+="        <input type=\"submit\" value=\"prog\" name=\"go_prog\" />\n";
		}
		else{
			htmls+="        <input type=\"button\" value=\"prog\" name=\"go_prog\" onClick=\"reqAjax('/class-mod/arduino.serial_pi_mod','go_prog')\" />\n";
		}
htmls+="      </td>\n"
+"    </tr>\n"
+"    <tr>\n"
+"      <td align=\"center\" height=\"30\">\n";
		if(ajax_use == false){
			htmls+="        <input type=\"submit\" value=\"Con Start\" name=\"go_con_start\" />\n";
		}
		else{
			htmls+="        <input type=\"button\" value=\"Con start\" name=\"go_con_start\" onClick=\"reqAjax('/class-mod/arduino.serial_pi_mod','go_con_start')\" />\n";
		}
htmls+="      </td>\n"
+"      <td align=\"center\" height=\"30\">\n";
		if(ajax_use == false){
			htmls+="        <input type=\"submit\" value=\"Con Stop\" name=\"go_con_stop\" />\n";
		}
		else{
			htmls+="        <input type=\"button\" value=\"Con Stop\" name=\"go_con_stop\" onClick=\"reqAjax('/class-mod/arduino.serial_pi_mod','go_con_stop')\" />\n";
		}
htmls+="      </td>\n"
+"    </tr>\n"
+"  </table>\n"
+"  <input type=\"hidden\" name=\"begin\" value=\"1\" />\n"
+"</form>\n"
+"<div id=\"ans_msg\">"+msg+"</div>\n"
+"	</body>\n"
+"	</html>\n";
		ht_print(htmls);
	}
	/*
	 * disp_serial_pi_mod()
	 */
	public void disp_serial_pi_mod(String msg){
		// TODO 自動生成されたメソッド・スタブ
		setHeader();	//ヘッダーは、自分で送って下さい。
		ht_print("Content-Type: text/html\r\n\r\n");	// 最後の、"\r\n\r\n" を忘れずに。
		String htmls="<html>\n"
+"<head>\n"
+"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n"
+"<meta http-equiv=\"Content-Language\" content=\"ja\" />\n"
+"<title>arduino.serial_pi_mod.class</title>\n"
+"</head>\n"
+"<body>\n"
+"/class-mod/arduino.serial_pi_mod<br />\n"
+"Raspi to Arduino USB Serrial Conect test を実行してします。<br />\n"
+"<hr />\n"
+"<script Language=\"JavaScript\">\n"
+"<!--\n"
+"function FrontPage_Form1_Validator(theForm)\n"
+"{\n"
+"    //alert(\" passed #1 \");\n"
+"  return (true);\n"
+"}\n"
+"//-->\n"
+"</script>\n"
+"<form method=\"POST\" action=\"/class-mod/arduino.serial_pi_mod\" onsubmit=\"return FrontPage_Form1_Validator(this)\" name=\"FrontPage_Form1\">\n"
+"  <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"24%\" style=\"margin: 20px 0 0 40px;\">\n"
+"    <tr>\n"
+"      <td align=\"center\" height=\"30\">\n"
+"        <input type=\"submit\" value=\"left\" name=\"go_left\" />\n"
+"      </td>\n"
+"      <td align=\"center\" height=\"30\">\n"
+"        <input type=\"submit\" value=\"right\" name=\"go_right\" />\n"
+"      </td>\n"
+"    </tr>\n"
+"    <tr>\n"
+"      <td align=\"center\" height=\"30\">\n"
+"        <input type=\"submit\" value=\"at\" name=\"go_at\" />\n"
+"      </td>\n"
+"      <td align=\"center\" height=\"30\">\n"
+"        <input type=\"submit\" value=\"prog\" name=\"go_prog\" />\n"
+"      </td>\n"
+"    </tr>\n"
+"  </table>\n"
+"  <input type=\"hidden\" name=\"begin\" value=\"1\" />\n"
+"  </form>\n";
		ht_print(htmls);
		ht_print(msg+"<br />\n");
		ht_print("</body>\n");
		ht_print("</html>\n");		
	}
	/*
	 * disp_serial_pi_mod with c270_mjpg_stream_cgi_mod_ajax_old()
	 */
	public void disp_c270_mjpg_stream_cgi_mod_ajax_old(String msg){
		String htmls;
		setHeader();	//ヘッダーは、自分で送って下さい。
		ht_print("Content-Type: text/html\r\n\r\n");
		htmls="<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\""
+" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
+"<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"
+"<head>\n"
+"<title>arduino.serial_pi_mod.class with MJPEG-Streamer</title>\n"
+"<!-- Ajaxライブラリー jQuery -->\n"
+"<script type=\"text/javascript\" src =\"/js/jquery-1.10.2.min.js\" charset=\"utf-8\"></script>\n"
+"<!-- Ajaxライブラリー raspi-netosa-jquery.js -->\n"
+"<script type=\"text/javascript\" src =\"/js/raspi-netosa-jquery.js\" charset=\"utf-8\"></script>\n"
+"</head>\n"
+"<script type=\"text/javascript\">\n"
+"<!--\n"
+"jq$= jQuery.noConflict();\n"
+"\n"
+"jq$(function(){\n"
+"  jq$.ajaxSetup({\n"
+"    timeout:5000\n"
+"  });\n"
+"  //start_up();\n"
+"  startImageLayer(\""+url_s+"\");\n"
+"});\n"
+"\n"
+"  function re_load(){\n"
+"    startImageLayer(\""+url_s+"\");\n"
+"  }\n"
+"\n"
+"	/* Copyright (C) 2007 Richard Atterer, richard©atterer.net\n"
+"	   This program is free software; you can redistribute it and/or modify it\n"
+"	   under the terms of the GNU General Public License, version 2. See the file\n"
+"	   COPYING for details. */\n"
+"\n"
+"	var imageNr = 0; // Serial number of current image\n"
+"	var finished = new Array(); // References to img objects which have finished downloading\n"
+"	var paused = false;\n"
+"	var imageLayer_url;\n"
+"\n"
+"function startImageLayer(url_s){\n"
+"  imageLayer_url=url_s;\n"
+"  createImageLayer();\n"
+"}\n"
+"\n"
+"	function createImageLayer() {\n"
+"	  var img = new Image();\n"
+"	  img.style.position = \"absolute\";\n"
+"	  img.style.zIndex = -1;\n"
+"	  img.onload = imageOnload;\n"
+"	  img.onclick = imageOnclick;\n"
+"	  img.src = imageLayer_url+\"/?action=snapshot&n=\" + (++imageNr);\n"
+"	  var webcam = document.getElementById(\"webcam\");\n"
+"	  webcam.insertBefore(img, webcam.firstChild);\n"
+"	}\n"
+"\n"
+"	// Two layers are always present (except at the very beginning), to avoid flicker\n"
+"	function imageOnload() {\n"
+"	  this.style.zIndex = imageNr; // Image finished, bring to front!\n"
+"	  while (1 < finished.length) {\n"
+"	    var del = finished.shift(); // Delete old image(s) from document\n"
+"	    del.parentNode.removeChild(del);\n"
+"	  }\n"
+"	  finished.push(this);\n"
+"	  if (!paused) createImageLayer();\n"
+"	}\n"
+"\n"
+"	function imageOnclick() { // Clicking on the image will pause the stream\n"
+"	  paused = !paused;\n"
+"	  if (!paused) createImageLayer();\n"
+"	}\n"
+"\n"
+"function FrontPage_Form1_Validator(theForm)\n"
+"{\n"
+"  return (true);\n"
+"}\n"
+"//-->\n"
+"	</script>\n";
		ht_print(htmls);

htmls="	<body>\n"
+"arduino.serial_pi_mod.class with MJPEG-Streamer<br />\n"
+"	<div id=\"webcam\" style=\"height:500px;\"><noscript><img src=\""+url_s+"/?action=snapshot\" /></noscript></div>\n"
+"\n"
+"<form method=\"POST\" action=\"/class-mod/arduino.serial_pi_mod\" onsubmit=\"return FrontPage_Form1_Validator(this)\" name=\"FrontPage_Form1\">\n"
+"  <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"24%\" style=\"margin: 20px 0 0 40px;\">\n"
+"    <tr>\n"
+"      <td align=\"center\" height=\"30\">\n";
		if(ajax_use == false){
			htmls+="        <input type=\"submit\" value=\"Streaming Start\" name=\"go_start\" />\n";
		}
		else{
			htmls+="        <input type=\"button\" value=\"start\" name=\"go_start\" onClick=\"reqAjax('/class-mod/arduino.serial_pi_mod','go_start')\" />\n";
		}
htmls+="      </td>\n"
+"      <td align=\"center\" height=\"30\">\n";
		if(ajax_use == false){
			htmls+="        <input type=\"submit\" value=\"Streaming Stop\" name=\"go_stop\" />\n";
		}
		else{
			htmls+="        <input type=\"button\" value=\"stop\" name=\"go_stop\" onClick=\"reqAjax('/class-mod/arduino.serial_pi_mod','go_stop')\" />\n";
		}
htmls+="      </td>\n"
+"    </tr>\n"
+"    <tr>\n"
+"      <td align=\"center\" height=\"30\">\n";
		if(ajax_use == false){
			htmls+="        <input type=\"submit\" value=\"left\" name=\"go_left\" />\n";
		}
		else{
			htmls+="        <input type=\"button\" value=\"left\" name=\"go_left\" onClick=\"reqAjax('/class-mod/arduino.serial_pi_mod','go_left')\" />\n";
		}
htmls+="      </td>\n"
+"      <td align=\"center\" height=\"30\">\n";
		if(ajax_use == false){
			htmls+="        <input type=\"submit\" value=\"right\" name=\"go_right\" />\n";
		}
		else{
			htmls+="        <input type=\"button\" value=\"right\" name=\"go_right\" onClick=\"reqAjax('/class-mod/arduino.serial_pi_mod','go_right')\" />\n";
		}
htmls+="      </td>\n"
+"    </tr>\n"
+"    <tr>\n"
+"      <td align=\"center\" height=\"30\">\n";
		if(ajax_use == false){
			htmls+="        <input type=\"submit\" value=\"at\" name=\"go_at\" />\n";
		}
		else{
			htmls+="        <input type=\"button\" value=\"at\" name=\"go_at\" onClick=\"reqAjax('/class-mod/arduino.serial_pi_mod','go_at')\" />\n";
		}
htmls+="      </td>\n"
+"      <td align=\"center\" height=\"30\">\n";
		if(ajax_use == false){
			htmls+="        <input type=\"submit\" value=\"prog\" name=\"go_prog\" />\n";
		}
		else{
			htmls+="        <input type=\"button\" value=\"prog\" name=\"go_prog\" onClick=\"reqAjax('/class-mod/arduino.serial_pi_mod','go_prog')\" />\n";
		}
htmls+="      </td>\n"
+"    </tr>\n"
+"  </table>\n"
+"  <input type=\"hidden\" name=\"begin\" value=\"1\" />\n"
+"</form>\n"
+"<div id=\"ans_msg\">"+msg+"</div>\n"
+"	</body>\n"
+"	</html>\n";
		ht_print(htmls);
	}

}
