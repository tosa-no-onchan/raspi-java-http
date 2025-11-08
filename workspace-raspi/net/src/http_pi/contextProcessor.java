/**
 * Raspberry Pi java http server
 * v1.8 update 2015.2.28
 * contextProcessor.java
 *  コンテンツへのリクエストを処理します。
 */
package http_pi;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;

/**
 * @author nishi
 *
 */
public class contextProcessor {
	private http_comLib _http_comLib;
	private boolean _ka;
	private byte[] buff;
	/**
	 * contextProcessor(boolean ka,byte[] _buff,http_comLib m_http_comLib)
	 */
	public contextProcessor(boolean ka,byte[] _buff,http_comLib m_http_comLib){
		_ka=ka;
		buff=_buff;
		_http_comLib=m_http_comLib;
	}
	/*
	 * void set_ka(boolean ka)
	 */
	public void set_ka(boolean ka){
		_ka=ka;
		_http_comLib.set_ka(ka);
	}
	public void exec(Socket sock,HttpReader hrd,String indexname){
		File file;
		boolean flg=false;
		boolean img_f=false;
		boolean expir_f=false;

		String contentType;	//コンテントタイプ
		String	version = hrd.version;

		System.out.println("contentProcessor::exec :#1 indexname="+indexname);
		try {
			//OutputStream outs = new BufferedOutputStream(sock.getOutputStream());
			//BufferedOutputStream outs = new BufferedOutputStream(sock.getOutputStream());
			DataOutputStream outs = new DataOutputStream(sock.getOutputStream());
			//Writer out = new OutputStreamWriter(outs);
			
			if(indexname.equals("")){
				indexname="index.html";
				file = new File(Defs.doc_rootDir +"/"+ indexname);
				flg = file.canRead();
				if(flg != true){
					indexname="index.htm";
				}
			}
			contentType = "text/html";
			String adr = Defs.doc_rootDir +"/"+ indexname;
			System.out.println("contentProcessor::exec :#2 adr="+adr);

			//System.out.println("requestProcessor :#5-1 socket.getKeepAlive()="+sock.getKeepAlive());
			//System.out.println("requestProcessor :#5-1 socket.getSendBufferSize()="+sock.getSendBufferSize());
			if(adr.endsWith(".gif")){
				contentType = "image/gif";
				img_f=true;
				expir_f=true;
			}
			else if(adr.endsWith(".jpg")){
				contentType = "image/jpg";
				img_f=true;
				expir_f=true;
			}
			else if(adr.endsWith(".png")){
				contentType = "image/png";
				img_f=true;
				expir_f=true;
			}
			else if(adr.endsWith(".css")){
				contentType = "text/css";
				expir_f=true;
			}
			else if(adr.endsWith(".js")){
				contentType = "text/javascript";
				expir_f=true;
			}
			file = new File(adr);
			flg = file.canRead();
			Date now;

			if(flg){
				System.out.println("contentProcessor::exec :#3 output to client ad="+adr);
				System.out.println("contentProcessor::exec :#4 output file size="+file.length());
				
				if(version.startsWith("HTTP/")){	//MIME header read
					System.out.println("contentProcessor::exec :#5 output http response with contentType="+contentType);
					String hd_s=_http_comLib.mkHttpHeaderOK_file(hrd.method,img_f,expir_f,file,contentType);
					outs.writeBytes(hd_s);
					outs.flush();
				}
				// GET Method
				if(hrd.method.equals("GET")==true){
					put_file_bin(file,outs);
					outs.flush();
				}
			}
			else{	//ファイル未検出
				System.out.println("contentProcessor::exec :#7 file nothing");
				now = new Date();
				String hd_s="";
				if(version.startsWith("HTTP/")){	//MIME header read
					hd_s="HTTP/1.1 404 File Not Found\r\n"
						+"Date: "+now+"\r\n"
						+"Server: HTTP 1.1\r\n"
						+"Content-type: text/html\r\n\r\n";	// 注）最後は \r\nが２個必要
					outs.writeBytes(hd_s);
					outs.flush();
				}
				hd_s="<HTML>\r\n"
					+"<HEAD><TITLE>File Not Found</TITLE></HEAD>\r\n"
					+"<BODY>\r\n"
					+"<H1>HTTP Error 404: File Not Found</H1>\r\n"
					+"<BODY>\r\n";
				outs.writeBytes(hd_s);
				outs.flush();
			}
			//outs.close();
		}
		catch (IOException e) {
			System.out.println("contentProcessor::exec :#90 e="+e);
		}
	}
	/*
	 * void put_file_bin(String adr,DataOutputStream outs)
	 */
	private void put_file_bin(File file,DataOutputStream outs){
		try{
			BufferedInputStream in = new BufferedInputStream(
		            new FileInputStream(file));
		    int len;
		    while ((len = in.read(buff)) != -1) {
				//System.out.println("put_file_bin :#1 output len="+len);
		       outs.write(buff, 0, len);
		    }
		    in.close();
		}
		catch(IOException e){
		}
	}
}
