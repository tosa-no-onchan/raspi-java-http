/**
 * Raspberry Pi java http server
 * v1.7 update 2015.2.15
 * requestDispatch.java
 */
package http_pi;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author nishi
 *
 * TODO この生成された型コメントのテンプレートを変更するには次へジャンプ:
 * ウィンドウ - 設定 - Java - コード・スタイル - コード・テンプレート
 */
public class requestDispatch implements Runnable{
	private static LinkedList<Socket> queue = new LinkedList<Socket>();
	private static Pattern p1=Pattern.compile("\\.\\./");
	private boolean keep_alive_pro=false;	// process keep_alive
	private byte[] buff;
	
	private http_comLib _http_comLib;

	private contextProcessor cntp;
	private keepAliveProcessor kepp;
	
	public short _id;
	
	public requestDispatch(short id){
		buff = new byte[Defs.CIBL];
		_http_comLib = new http_comLib(true,buff);
		cntp = new contextProcessor(true,buff,_http_comLib);
		kepp = new keepAliveProcessor();
		this._id=id;
	}
		
	public static void reqProc(Socket s,int cnt){
		synchronized (queue){
			queue.addLast(s);
			System.out.println(">>>requestDispatch :#1 connection:"+cnt+" "+queue);
			queue.notifyAll();
		}
	}
	public void run(){
		while(true){
			Socket sock;	//接続ソケット
			synchronized(queue){
				while(queue.isEmpty()){
					try{
						queue.wait();
					}
					catch(InterruptedException e){
					}
				}
				sock = queue.removeFirst();
			}
			CheckAllowNetworks chk_ip = new CheckAllowNetworks(Defs.allow_networks);
			String yhost_ip =sock.getInetAddress().getHostAddress();
			System.out.println("requestDispatch("+_id+") :#2 yhost_ip="+yhost_ip);
			if(chk_ip.chek_ip(yhost_ip)==false){
				System.out.println("requestDispatch :#3 refuse yhost_ip="+yhost_ip);
			}
			else{
				int mkar=Defs.MKAR;	// Defs.MaxKeepAliveRequests
				int lp=0;
				long slise_tm=100;
				boolean time_out=false;
				keep_alive_pro=true;	// process keep_alive ON
				System.out.println("requestDispatch("+_id+") :#4 lp="+lp);
				requestLoop(Defs.KA,sock);
				lp++;
				for(;keep_alive_pro==true && Defs.KA!=0 && lp < mkar && time_out==false;lp++){
					System.out.println("requestDispatch("+_id+") :#5 lp="+lp);
					long wait_tm=(Defs.KAT+1)*1000;	// 秒  Defs.KeepAliveTimeout + 1
					try {
						//System.out.println("requestDispatch :#6 lp="+lp);
						int x;
						while(true){
							x = sock.getInputStream().available();
							//System.out.println("requestDispatch :#7 x="+x);
							if(x>0){
								break;
							}
							wait_tm-=slise_tm;
							if(wait_tm < 0){
								System.out.println("requestDispatch("+_id+") :#8 time out!!");
								time_out=true;
								break;
							}
							try {
								Thread.sleep(slise_tm);
							}
							catch (InterruptedException e) {}
						}
						if(x > 0){
							System.out.println("requestDispatch("+_id+") :#9 next request came!!");
							requestLoop(Defs.KA,sock);
						}
					}
					catch (IOException e1) {
						System.out.println("requestDispatch :#10 lp="+lp);
						// TODO 自動生成された catch ブロック
						e1.printStackTrace();
					}
				}
			} //else
			try{
				System.out.println("requestDispatch("+_id+") :#11 socket close!!");
				sock.close();
			}
			catch(IOException e){
				System.out.println("requestDispatch :#90 error ="+e);
			}
		} //while
	} //run
	
	/*
	 * requestLoop(int defs_ka,Socket sock)
	 */
	private void requestLoop(int defs_ka,Socket sock){
		String indexname;	//インデックスファイル
		// sockt imput stream
		InputStream in;
		try {
			in = sock.getInputStream();
			boolean ka=true;	// keep-alive

			//BufferedReader rd = new BufferedReader(new InputStreamReader(in));
			HttpInputStream hdin = new HttpInputStream(in);

			//タスク情報テーブルの確保
			taskTable tskt = new taskTable();	// set Timeout timer
			HttpReader hrd = new HttpReader();

			// http リクエストの受信を行います。
			hrd.read(tskt,hdin);

			System.out.println("requestDispatch("+_id+")::requestLoop():#1 method="+hrd.method);
			System.out.println("requestDispatch("+_id+")::requestLoop():#2 filename="+hrd.filename);
			System.out.println("requestDispatch("+_id+")::requestLoop():#3 parm="+hrd.parm);

			// hacking ?
			Matcher m1 = p1.matcher(hrd.filename);
			if(m1.find()==true){
			}
			// built-in request ?
			else if(Defs.built_in != "" && hrd.filename.startsWith("/built-in/") == true){
				if(hrd.method.equals("GET") || hrd.method.equals("POST")){	// GET method or POST method
					System.out.println("requestDispatch("+_id+")::requestLoop():#4 passed");
					keep_alive_pro=false;	// process keep_alive Off
					if(hrd.method.equals("POST")){
						// POST body の受信を行います。
						hrd.read_bdy(tskt,hdin);
					}
					String cmd=hrd.filename.substring(10);
					classProcessor clasp = new classProcessor();
					clasp.built_in(sock,hrd.method,cmd,hrd);
				}
			}
			// dynamic class load request ?
			else if(hrd.filename.startsWith("/class-mod/") == true){
				if(hrd.method.equals("GET") || hrd.method.equals("POST")){	// GET method or POST method
					System.out.println("requestDispatch::requestLoop():#5 passed");
					keep_alive_pro=false;	// process keep_alive Off
					if(hrd.method.equals("POST")){
						// POST body の受信を行います。
						hrd.read_bdy(tskt,hdin);
					}
					String cmd=hrd.filename.substring(11);
					classProcessor clasp = new classProcessor();
					clasp.class_ld(sock,hrd.method,cmd,hrd);
				}
			}
			// java program request ?
			else if(hrd.filename.startsWith("/class-bin/") == true){
				if(hrd.method.equals("GET") || hrd.method.equals("POST")){	// GET method or POST method
					System.out.println("requestDispatch::requestLoop():#6 passed");
					String cmd=hrd.filename.substring(11);
					if(defs_ka!=1){
						ka=false;
						keep_alive_pro=false;	// process keep_alive Off
					}
					javaProcessor javap = new javaProcessor(ka,buff);
					if(Defs.class_interface==1){
						if(hrd.method.equals("POST")){
							// POST body の受信を行います。
							hrd.read_bdy(tskt,hdin);
						}
						// java program with command line parameter
						javap.exec(sock,hrd.method,cmd,hrd);
					}
					else{
						// java program with system-environment-variable
						javap.exec_sev(sock,cmd,hrd,hdin);
					}
				}
			}
			// cgi request ?
			else if(hrd.filename.startsWith("/cgi-bin/") == true){
				if(hrd.method.equals("GET") || hrd.method.equals("POST")){	// GET method or POST method
					System.out.println("requestDispatch::requestLoop():#7 passed");
					String cmd=hrd.filename.substring(9);
					if(defs_ka!=1){
						ka=false;
						keep_alive_pro=false;	// process keep_alive Off
					}
					cgiProcessor cgip=new cgiProcessor(ka,buff);
					cgip.exec(sock,cmd,hrd,hdin);
				}
			}
			//php script request
			else if(Defs.with_php==true && hrd.filename.endsWith(".php")== true){
				if(hrd.method.equals("GET") || hrd.method.equals("POST")){	// GET method or POST method
					System.out.println("requestDispatch::requestLoop():#8 passed");
					String cmd=hrd.filename;
					if(defs_ka!=1){
						ka=false;
						keep_alive_pro=false;	// process keep_alive Off
					}
					phpProcessor phpp = new phpProcessor(ka,buff);
					phpp.exec(sock,cmd,hrd,hdin);
				}
			}
			// content request
			else if(hrd.method.equals("GET") || hrd.method.equals("HEAD")){	// GET mothod or HEAD method
				System.out.println("requestDispatch::requestLoop():#9 passed");
				indexname = hrd.filename.substring(1,hrd.filename.length());
				if(defs_ka==0){
					ka=false;
					keep_alive_pro=false;	// process keep_alive Off
				}
				cntp.set_ka(ka);
				cntp.exec(sock, hrd, indexname);
			}
			//keep-alive probe packet
			else if(hrd.req.length()==0){
				System.out.println("requestDispatch::requestLoop():#10 received a keep alive probe packet.");
				if(defs_ka==0){
					ka=false;
					keep_alive_pro=false;	// process keep_alive Off
				}
				kepp.exec(ka,sock);
			}
			//else{
			//	System.out.println("requestDispatch::requestLoop():#11 passed lng="+hrd.req.length());
			//}
		}
		catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}
} //requestProcessor
