/**
 * Raspberry Pi java http server v1.5
 * c270_mjpg_stream_cgi_mod.java
 * c270 mjpeg streamer
 */
import java.util.Enumeration;

import user_cgi_lib.CommandExec;
import user_cgi_lib.userCgiMod;

/**
 * @author nishi
 *
 */
public class c270_mjpg_stream_cgi_mod extends userCgiMod {
	// c270streamd file path
	//String nd_pg_dir="/home/pi-nishi/workspace-raspi/nishi-test";
	//String nd_pg_dir="/home/nishi/workspace-raspi/nishi-test";
	String nd_pg_dir="../nishi-test";

	//mjpg stream url
	//String url_s="http://192.168.1.180:8080";
	String url_s="http://192.168.1.150:8080";

	/**
	 * 
	 */
	public c270_mjpg_stream_cgi_mod() {
		// TODO 自動生成されたコンストラクター・スタブ
		super();
	}

	@Override
	public void start() {
		// TODO 自動生成されたメソッド・スタブ
		System.out.println("c270_mjpg_stream_cgi_mod::start() : #1 ");
		
		Enumeration<String> keys = parm_hash.keys();
		while(keys.hasMoreElements()) {
			String key = keys.nextElement();
			System.out.println(key + "=" + parm_hash.get(key));
			//ht_print(key + "=" + parm_hash.get(key)+"<br />\n");
		}
		
		String begin=parm_hash.get("begin");
		String go_start=parm_hash.get("go_start");
		String go_stop=parm_hash.get("go_stop");
		System.out.println("c270_mjpg_stream_cgi_mod::start() : #2 begin="+begin);
		if(begin == null || begin.equals("1") == false){
			System.out.println("c270_mjpg_stream_cgi_mod::start() : #3 ");
			disp_c270_mjpg_stream_cgi_mod();
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
			disp_c270_mjpg_stream_cgi_mod();
		}
		else if(go_stop != null && go_stop.trim().equals("") == false){
			stop_mjpg_stream();
			disp_c270_mjpg_stream_cgi_mod();
		}
	}
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
	 * disp_c270_mjpg_stream_cgi_mod()
	 */
	public void disp_c270_mjpg_stream_cgi_mod(){
		setHeader();	//ヘッダーは、自分で送って下さい。
		ht_print("Content-Type: text/html\r\n\r\n");
String htmls="<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\""
+" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
+"<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"
+"<head>\n"
+"<title>MJPEG-Streamer</title>\n"
+"</head>\n"
+"	<script type=\"text/javascript\">\n"
+"\n"
+"	/* Copyright (C) 2007 Richard Atterer, richard©atterer.net\n"
+"	   This program is free software; you can redistribute it and/or modify it\n"
+"	   under the terms of the GNU General Public License, version 2. See the file\n"
+"	   COPYING for details. */\n"
+"\n"
+"	var imageNr = 0; // Serial number of current image\n"
+"	var finished = new Array(); // References to img objects which have finished downloading\n"
+"	var paused = false;\n"
+"\n"
+"	function createImageLayer() {\n"
+"	  var img = new Image();\n"
+"	  img.style.position = \"absolute\";\n"
+"	  img.style.zIndex = -1;\n"
+"	  img.onload = imageOnload;\n"
+"	  img.onclick = imageOnclick;\n"
+"	  img.src = \""+url_s+"/?action=snapshot&n=\" + (++imageNr);\n"
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
+"	</script>\n";
		ht_print(htmls);

htmls="	<body onload=\"createImageLayer();\">\n"
+"MJPEG-Streamer / c270_mjpg_stream_cgi_mod<br />\n"
+"	<div id=\"webcam\" style=\"height:500px;\"><noscript><img src=\""+url_s+"/?action=snapshot\" /></noscript></div>\n"
+"\n"
+"<form method=\"POST\" action=\"/class-mod/c270_mjpg_stream_cgi_mod\" onsubmit=\"return FrontPage_Form1_Validator(this)\" name=\"FrontPage_Form1\">\n"
+"  <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"24%\" style=\"margin: 20px 0 0 40px;\">\n"
+"    <tr>\n"
+"      <td align=\"center\">\n"
+"        <input type=\"submit\" value=\"Streaming Start\" name=\"go_start\" />\n"
+"      </td>\n"
+"      <td align=\"center\">\n"
+"        <input type=\"submit\" value=\"Streaming Stop\" name=\"go_stop\" />\n"
+"      </td>\n"
+"    </tr>\n"
+"  </table>\n"
+"  <input type=\"hidden\" name=\"begin\" value=\"1\" />\n"
+"</form>\n"
+"	</body>\n"
+"	</html>\n";
		ht_print(htmls);
	}
}
