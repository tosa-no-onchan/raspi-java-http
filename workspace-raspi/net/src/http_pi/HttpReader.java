/**
 * Raspberry Pi java http server
 * v1.8 update 2015.3.1
 * HttpReader Class
 * Http Protocol Reader for Http Server
 * http プロトコルに対応した Http Sever側の受信処理を行います。
 */
package http_pi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

/**
 * @author nishi
 *
 */
public class HttpReader {
	public String req;
	public String host;
	public String content_type;
	public Boolean multipart_f=false;
	public String boundary;
	public String boundary1;
	public String boundary2;
	public String method;
	public String filename;
	public String parm;
	public String version;
	public String p_data;
	
	public Hashtable<String,String> http_hd;
	public Hashtable<String,String> req_line;

	public boolean dump_f=false;
	private static boolean DEBG=false;
	private static boolean DEBG1=false;
	private static boolean DEBG2=false;
	private static boolean DEBG2b=false;
	private static boolean DEBG3=false;
	private int ph;

	//private HttpReaderBuff data_bf;
	
	public HttpReader(){
		
	}
	/*
	 * void read()
	 * read data through DataInputStream
	 */
	public void read(taskTable tskt,HttpInputStream hin){
		req="";
		host="";
		content_type="";
		multipart_f=false;
		boundary="";
		boundary1="";
		boundary2="";
		method="";
		filename="";
		parm="";
		version="";
		p_data="";
		req_line = new Hashtable<String,String>();
		http_hd = new Hashtable<String,String>();
		//data_bf = new HttpReaderBuff();

		//dump_f=true;
		if(dump_f){
			// client からの http 受信データ のダンプ出力を行います。デバッグに使用できます。
			// HTTP プロトコルのデータの解析に使用できます。
			dump_din(hin);
		}
		else{
			read_hd(tskt,hin);
		}
	}
	/*
	 * dump_din()
	 * ダンプ表示 for debug
	 */
	private void dump_din(HttpInputStream hin_r){
		//HttpDataInputStream din_r = new HttpDataInputStream(in);
		try{
			String in_s;
			String p_line="";
			int n=0;
			req = hin_r.readline();
			System.out.println("HttpReader::dump_din() :#1 recve socket stream="+req);
			while((in_s=hin_r.readline()).length() > 0){
				System.out.println(in_s);
			}
			while (hin_r.ready() == true && n < 1000){
				p_line=hin_r.readline();
				System.out.println("HttpReader::dump_din() :#2 p_line="+p_line);
				n++;
			}
		}
		catch(IOException e){
			System.out.println("HttpReader::read_din() :#90 error ="+e);
		}
	}
	/**
	 * read_hd()
	 * read http header
	 */
	private void read_hd(taskTable tskt,HttpInputStream hin_r){
		String in_s;
		String s;
		String[] spl_d,parm_d;
		//HttpDataInputStream din_r = new HttpDataInputStream(in);
		try{
			// get request lines(first rec)
			req = hin_r.readline();
			System.out.println("HttpReader::read_hd() :#1 recve start stream="+req+",lng="+req.length());
			http_hd.put("req", req);
			// リクエストが空行です
			if(req.length()<=0){
				// req.length()==0 の時は、keep-alive のプローブパケットの様です。
				// keep-alive's probe packet with no data
				//System.out.println("HttpReader::read_hd() :#2 recve nothing req=0x"+util_lib.StringtoHex(req));
				System.out.println("HttpReader::read_hd() :#2 recve nothing !!");
				return;
			}
			spl_d = req.split(" ");
			if(spl_d.length >= 1){
				method =spl_d[0];
			}
			if(spl_d.length >= 2){
				parm_d= spl_d[1].split("\\?");
				// set file name
				filename = parm_d[0];
				if(parm_d.length>=2){
					// set parameter
					parm=parm_d[1];
				}
			}
			if(spl_d.length >= 3){
				version = spl_d[2];}
			req_line.put("method", method);
			req_line.put("filename", filename);
			req_line.put("parm",parm);
			req_line.put("version", version);

			// get header lines
			while((in_s=hin_r.readline()).length() > 0){
				if(DEBG1) System.out.println("HttpReader::read_hd() :#3 in_s="+in_s);
				int pos= in_s.indexOf(":");
				if(pos > 0){
					String p_name=in_s.substring(0, pos+1);
					String p_val="";
					if(in_s.length() > pos+2){
						p_val=in_s.substring(pos+2);
					}
					http_hd.put(p_name, p_val);
					//System.out.println("HttpReader::read_hd() :#4 p_name="+p_name+",p_val="+p_val);
				}
				// Post 
				// Content-Type: application/x-www-form-urlencoded
				//if(in_s.startsWith("Content-Type:") == true){
				//	spl_d = in_s.split(" ");
				//	if(spl_d.length>=1){
				//		content_type=spl_d[1];
				//	}
				//	System.out.println("content_type="+content_type);
				//	if(content_type.startsWith("multipart/form-data")==true){
				//		multipart_f=true;
				//		if(spl_d.length>=2){
				//			s=spl_d[2];
				//			boundary = s.substring(9);
				//			boundary1 = "--"+boundary;
				//			boundary2 = boundary1+"--";
				//		}
				//		System.out.println("boundary="+boundary+",length="+boundary.length());
				//		System.out.println("boundary1="+boundary1+",length="+boundary1.length());
				//		System.out.println("boundary2="+boundary2+",length="+boundary2.length());
				//	}
				//}
			}
			if(http_hd.get("Host:") != null){
				host=http_hd.get("Host:");
			}
			if(DEBG)System.out.println("host="+host);
			s=http_hd.get("Content-Type:");
			if(s != null){
				if(s.startsWith("multipart/form-data")==true){
					multipart_f=true;
					spl_d = s.split(" ");
					if(spl_d.length>1){
						boundary = spl_d[1].substring(9);
						boundary1 = "--"+boundary;
						boundary2 = boundary1+"--";
						if(DEBG){
							System.out.println("boundary="+boundary+",length="+boundary.length());
							System.out.println("boundary1="+boundary1+",length="+boundary1.length());
							System.out.println("boundary2="+boundary2+",length="+boundary2.length());
						}
					}
				}
			}
			if(DEBG1) System.out.println("HttpReader::read_hd() :#10 recve end!!");
		}
		catch(IOException e){
			System.out.println("HttpReader::read_hd() :#90 error ="+e);
		}
	}
	/*
	 * void read_bdy(taskTable tskt,HttpInputStream hin_r)
	 */
	void read_bdy(taskTable tskt,HttpInputStream hin_r){
		String s;
		String upload_parm_name="";
		if(method.equals("POST")==true){
			try{
				System.out.println("HttpReader::read_bdy() :#1 read POST data start!");
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
				while (hin_r.ready() == true && n < 20000){
					p_line=hin_r.readline();
					if(DEBG2)System.out.println("HttpReader::read_bdy() :#2 ph="+ph+",p_line="+p_line);
					switch(ph){
					case 0:	// non muliti part
						p_data+=p_line;
						break;
					case 1:	// muliti part waiting boundary
						if(p_line.equals(boundary1)==true){
							ph=2;
							if(DEBG2)System.out.println("HttpReader::read_bdy() :#3 boundary1 come >"+p_line);
						}
						else if(p_line.equals(boundary2)==true){
							if(DEBG2)System.out.println("HttpReader::read_bdy() :#4 boundary2 come >"+p_line);
						}
						break;
					case 2:	// muliti part waiting Content-Disposition:
							// Content-Disposition: form-data; name="tno"  --> ph=3
							// Content-Disposition: form-data; name="simg"; filename=""  --> ph=10
						if(p_line.startsWith("Content-Disposition:") == true){
							s=p_line.substring(p_line.indexOf("name=")+6);
							p_name=s.substring(0,s.indexOf("\""));
							if(DEBG2)System.out.println("HttpReader::read_bdy() :#5 p_name="+p_name);
						
							if(p_line.indexOf("filename=") >=0){
								s=p_line.substring(p_line.indexOf("filename=")+10);
								p_filename=s.substring(0,s.indexOf("\""));
								if(DEBG2)System.out.println("HttpReader::read_bdy() :#6 p_filename="+p_filename);
								if(p_data.length()==0){
									p_data+=p_name+"="+p_filename;
								}
								else{
									p_data+="&"+p_name+"="+p_filename;
								}
								// アップロードファイル名を伝達するためのパラメータ名を作成
								upload_parm_name=p_name+"_"+p_filename;
								if(DEBG2)System.out.println("HttpReader::read_bdy() :#7 upload_parm_name="+upload_parm_name);
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
						if(DEBG2b) System.out.println("HttpReader::read_bdy() :#8 ph="+ph+",p_val="+p_val);
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
							if(DEBG2b) System.out.println("HttpReader::read_bdy() :#9 boundary1 come >"+p_line);
						}
						else if(p_line.equals(boundary2)==true){
							ph=1;
							if(DEBG2b) System.out.println("HttpReader::read_bdy() :#10 boundary2 come >"+p_line);
						}
						else{
							p_val=p_line;
							if(DEBG2b) System.out.println("HttpReader::read_bdy() :#11 ph="+ph+",p_val="+p_val);
							p_data+="%0D%0A"+p_val;
							//p_data+=0x0d+0x0a+p_val;
						}
						break;
					case 10:	// waiting for "Content-Type: application/octet-stream" line -> ph=11
								// waiting for "Content-Type: text/plain" line -> ph=11
								// or
								// waiting for "Content-Type: image/gif" line -> ph=12
								// waiting for "Content-Type: image/jpg" line -> ph=12
						if(DEBG2)System.out.println("HttpReader::read_bdy() :#12 ph="+ph+",p_line="+p_line);
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
						if(DEBG2)System.out.println("HttpReader::read_bdy() :#13 ph="+ph+",p_line="+p_line);
						// 1 brank line come
						if(p_line.length() == 0){
							if(DEBG2)System.out.println("HttpReader::read_bdy() :#14 passed !!");
							// img ファイル受け取り
							p_val=upload_img(hin_r);
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
				System.out.println("HttpReader::read_bdy() :#10 read POST data end!");
			}
			catch(IOException e){
				System.out.println("HttpReader::read_bdy() :#90 error ="+e);
			}
		}
	}
	/*
	 * img ファイルの受け取りを行います。
	 */
	private String upload_img(HttpInputStream hin_r) throws IOException{
		boolean end_f=false;
		int data_l=0;
		int j=0;
		byte[] buff = new byte[1024];
		
		byte[] b_boundary1 = ("\r\n"+boundary1+"\r\n").getBytes();	// 先頭の \r\n は、バイナリーデータの終端です
		byte[] b_boundary2 = ("\r\n"+boundary2+"\r\n").getBytes();	// 先頭の \r\n は、バイナリーデータの終端です

		System.out.println("HttpReader::upload_img() :#1 file upload start");
		
		String tmp_f=make_upload_fname();

		File tmp_dir=new File(Defs.tmp_dir);
		if(tmp_dir.exists()==false){
			if(tmp_dir.mkdir()==false){
				System.err.println("HttpReader::upload_img() :#2 file upload dir="+Defs.tmp_dir+" alloc error");
			}
		}
		File tmp_file=new File(Defs.tmp_dir+"/"+tmp_f);
		OutputStream out = new FileOutputStream(tmp_file);
			
		//din_r.debug2=true;
		while(hin_r.ready() == true && j < 10000 && end_f==false){	// reading img loop
			if(DEBG3)
				System.out.println("HttpReader::upload_img() :#3 j="+j);
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
		System.out.println("HttpReader::upload_img() :#10 file upload end");
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
