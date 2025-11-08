/**
 * Raspberry Pi java http server
 * v1.9 update 2015.10.30
 * HttpReaderCGI Class
 * Http Protocol Reader for CGI
 * Http Sever から fork されたCGI側の受信処理を行います。
 * POST データの Http Server からの stdin を介した受け取りを行います。
 */
package user_cgi_lib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author nishi
 *
 */
public class HttpReaderCGI {
	Map<String, String> env;
	private String Defs_tmp_dir ="/tmp/ras-pi-upload";	// file up-load working dir
	public String req;
	//public String host;
	public String content_type;
	public boolean multipart_f=false;
	public String boundary;
	public String boundary1;
	public String boundary2;

	public String filename;
	public String parm;
	public String p_data;
	
	//public Hashtable<String,String> http_hd;

	public boolean dump_f=false;
	private static boolean DEBG=false;

	private int ph;
	private MyLog mlog;

	//private HttpReaderBuff data_bf;

	private String svr_lang="";	// add by nishi 2015.10.30
	private boolean cgi_use=true;	// CGI mode. add by nishi 2015.10.30
	
	public HttpReaderCGI(Map<String, String> env){
		this.env=env;
		mlog = new MyLog();
		//SERVER_LANGUAGE add by nishi 2015.10.30
		if(env.get("SERVER_LANGUAGE")!= null){
			svr_lang=env.get("SERVER_LANGUAGE");	// C or Java
		}
		if(svr_lang.equals("C")==true){
			cgi_use=false; 	// set connection mode
		}
	}
	public void read(){
		HttpInputStreamCGI hin = new HttpInputStreamCGI(System.in);
		hin.setCgiUse(cgi_use); 	// set CGI mode or connection mode.
		req="";
		//host="";
		multipart_f=false;
		boundary="";
		boundary1="";
		boundary2="";
		filename="";
		parm="";
		p_data="";
		//http_hd = new Hashtable<String,String>();
		//data_bf = new HttpReaderBuff();

		//dump_f=true;
		if(dump_f==true){
			// client からの http 受信データ のダンプ出力を行います。デバッグに使用できます。
			// HTTP プロトコルのデータの解析に使用できます。
			dump_din(hin);
		}
		else{
			read_din(hin);
		}
	}
	/*
	 * dump_din()
	 * ダンプ表示 for debug
	 */
	private void dump_din(HttpInputStreamCGI hin_r){
		//HttpDataInputStream din_r = new HttpDataInputStream(in);
		try{
			//String in_s;
			String p_line="";
			int n=0;
			// ここでは、Httpヘッダーは、ありません。
			//req = din_r.readline();
			//System.out.println("HttpReaderCGI::dump_din() :#1 recve socket stream="+req+"<br />");
			//while((in_s=din_r.readline()).length() > 0){
			//	System.out.println(in_s+"<br />");
			//}
			// データ部分をダンプします。
			while (hin_r.ready() == true && n < 1000){
				p_line=hin_r.readline();
				System.out.println("HttpReaderCGI::dump_din() :#2 p_line="+p_line+"<br />");
				n++;
			}
		}
		catch(IOException e){
			System.out.println("HttpReaderCGI::read_din() :#90 error ="+e);
		}
	}
	/**
	 * read_din()
	 * read data through DataInputStream
	 */
	private void read_din(HttpInputStreamCGI din_r){
		String s;
		String upload_parm_name="";
		String[] spl_d;
		//HttpDataInputStream din_r = new HttpDataInputStream(in);

		if(env.get("CONTENT_TYPE") != null){
			content_type=env.get("CONTENT_TYPE");
			if(content_type.startsWith("multipart/form-data")==true){
				multipart_f=true;
				spl_d = content_type.split(" ");
				if(spl_d.length>1){
					boundary = spl_d[1].substring(9);
					boundary1 = "--"+boundary;
					boundary2 = boundary1+"--";
					mlog.put(DEBG,"boundary="+boundary+",length="+boundary.length());
					mlog.put(DEBG,"boundary1="+boundary1+",length="+boundary1.length());
					mlog.put(DEBG,"boundary2="+boundary2+",length="+boundary2.length());
				}
			}
		}
		try{
			//----- start 
			mlog.put(DEBG,"HttpReaderCGI::read_din() :#1 read POST data start!");
			int n=0;
			ph=0;	// phase 
					// 0 -> non muliti
					// 1 -> boundary wait
					// 2 --> Content-Disposition wait
			//din_r.debug=true;
			// read POST data lines
			String p_name="";
			String p_filename="";
			String p_val="";
			String p_line="";
			if(multipart_f){
				ph=1;
			}
			while (din_r.ready() == true && n < 20000){
				p_line=din_r.readline();
				mlog.put(DEBG,"HttpReaderCGI::read_din() :#5 ph="+ph+",p_line="+p_line);
				switch(ph){
				case 0:	// non muliti part
					p_data+=p_line;
					break;
				case 1:	// muliti part waiting boundary
					if(p_line.equals(boundary1)==true){
						ph=2;
						mlog.put(DEBG,"HttpReaderCGI::read_din() :#6 boundary1 come >"+p_line);
					}
					else if(p_line.equals(boundary2)==true){
						mlog.put(DEBG,"HttpReaderCGI::read_din() :#7 boundary2 come >"+p_line);
					}
					break;
				case 2:	// muliti part waiting Content-Disposition:
					// Content-Disposition: form-data; name="tno"  --> ph=3
					// Content-Disposition: form-data; name="simg"; filename=""  --> ph=10
					if(p_line.startsWith("Content-Disposition:") == true){
						s=p_line.substring(p_line.indexOf("name=")+6);
						p_name=s.substring(0,s.indexOf("\""));
						mlog.put(DEBG,"HttpReaderCGI::read_din() :#8 p_name="+p_name);
						// filename= が続いていますか?
						if(p_line.indexOf("filename=") >=0){
							s=p_line.substring(p_line.indexOf("filename=")+10);
							p_filename=s.substring(0,s.indexOf("\""));
							mlog.put(DEBG,"HttpReaderCGI::read_din() :#9 p_filename="+p_filename);
							if(p_data.length()==0){
								p_data+=p_name+"="+p_filename;
							}
							else{
								p_data+="&"+p_name+"="+p_filename;
							}
							// アップロードファイル名を伝達するためのパラメータ名を作成
							upload_parm_name=p_name+"_"+p_filename;
							mlog.put(DEBG,"HttpReaderCGI::read_din() :#10 upload_parm_name="+upload_parm_name);
							ph=10;
						}
						else{
							ph=3;
						}
					}
					break;
				case 3:	// muliti part pass one line
					ph=4;
					break;
				case 4:	// muliti part get value
					p_val=p_line;
					mlog.put(DEBG,"HttpReaderCGI::read_din() :#11 ph="+ph+",p_val="+p_val);
					if(p_data.length()==0){
						p_data+=p_name+"="+p_val;
					}
					else{
						p_data+="&"+p_name+"="+p_val;
					}
					//ph=1;	// ここは、変 2015.2.18
					ph=5;	// 2015.22.18
					break;
				case 5:	// muliti part get next values 2015.2.18
					if(p_line.equals(boundary1)==true){
						ph=2;
						mlog.put(DEBG,"HttpReaderCGI::read_din() :#12 boundary1 come >"+p_line);
					}
					else if(p_line.equals(boundary2)==true){
						ph=1;
						mlog.put(DEBG,"HttpReaderCGI::read_din() :#13 boundary2 come >"+p_line);
					}
					else{
						p_val=p_line;
						mlog.put(DEBG,"HttpReaderCGI::read_din() :#14 ph="+ph+",p_val="+p_val);
						p_data+=0x0d+0x0a+p_val;
					}
					break;
				case 10:	// waiting for "Content-Type: application/octet-stream" line -> ph=11
							// waiting for "Content-Type: text/plain" line -> ph=11
							// or
							// waiting for "Content-Type: image/gif" line -> ph=12
							// waiting for "Content-Type: image/jpg" line -> ph=12
					mlog.put(DEBG,"HttpReaderCGI::read_din() :#15 ph="+ph+",p_line="+p_line);
					if(p_line.indexOf("application/octet-stream") >=0){
						ph=11;
					}
					else if(p_line.indexOf("text/plain") >=0){
						ph=11;
					}
					else if(p_line.indexOf("image/") >=0){
						ph=12;
					}
					else if(p_line.equals(boundary1)==true || p_line.equals(boundary2)==true){
						ph=2;
					}
					break;
				case 11: // reading octet-stream , text/plain
					if(p_line.equals(boundary1)==true || p_line.equals(boundary2)==true){
						ph=2;
					}
					break;
				case 12: // image/xxx reading
					mlog.put(DEBG,"HttpReaderCGI::read_din() :#16 ph="+ph+",p_line="+p_line);
					// 1 brank line come
					if(p_line.length() == 0){
						mlog.put(DEBG,"HttpReaderCGI::read_din() :#17 passed !!");
						// img ファイル受け取り
						p_val=upload_img(din_r);
						if(p_data.length()==0){
							p_data+=upload_parm_name+"="+p_val;
						}
						else{
							p_data+="&"+upload_parm_name+"="+p_val;
						}
					}
					break;
				}
				n++;
			}
		}
		catch(IOException e){
			mlog.put(DEBG,"HttpReaderCGI::read_din() :#90 error ="+e);
		}
	}
	/*
	 * img ファイルの受け取りを行います。
	 */
	private String upload_img(HttpInputStreamCGI hin_r) throws IOException{
		boolean end_f=false;
		int data_l=0;
		int j=0;
		byte[] buff = new byte[1024];
		
		byte[] b_boundary1 = ("\r\n"+boundary1+"\r\n").getBytes();	// 先頭の \r\n は、バイナリーデータの終端です
		byte[] b_boundary2 = ("\r\n"+boundary2+"\r\n").getBytes();	// 先頭の \r\n は、バイナリーデータの終端です

		mlog.put(DEBG,"HttpReader::upload_img() :#1 file upload start");
		
		String tmp_f=make_upload_fname();

		File tmp_dir=new File(Defs_tmp_dir);
		if(tmp_dir.exists()==false){
			if(tmp_dir.mkdir()==false){
				mlog.put(DEBG,"HttpReaderCGI::upload_img() :#2 file upload dir="+Defs_tmp_dir+" alloc error");
			}
		}
		File tmp_file=new File(Defs_tmp_dir+"/"+tmp_f);
		OutputStream out = new FileOutputStream(tmp_file);
			
		//din_r.debug2=true;
		while(hin_r.ready() == true && j < 10000 && end_f==false){	// reading img loop
			mlog.put(DEBG,"HttpReaderCGI::upload_img() :#3 j="+j);
			data_l=hin_r.readToBoundary(buff, 0, buff.length, b_boundary1,b_boundary2);
			if(data_l > 0){
				out.write(buff,0,data_l);
			}
			//バウンダリーが来ました
			if(hin_r.check_Boundary_no()>0){
				break;
			}
			j++;
		}
		out.close();
		tmp_file.setWritable(true,true);
		if(hin_r.check_Boundary_no() >0){
			ph=2;
		}
		else{
			ph=1;
		}
		mlog.put(DEBG,"HttpReaderCGI::upload_img() :#10 file upload end");
		return tmp_f;
	}
	/*
	 * アップロードファイル用の tmp名称を作成します。
	 */
	private String make_upload_fname(){
		String temp_name;
		long currentTimeMillis = System.currentTimeMillis();
		Date date = new Date(currentTimeMillis);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddHHmmss-SSS");
		temp_name="tmp-"+simpleDateFormat.format(date);
		return temp_name;
	}
}
