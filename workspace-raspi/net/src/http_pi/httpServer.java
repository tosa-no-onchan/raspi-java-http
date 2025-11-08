/**
 * Raspberry Pi java http server
 * v1.9
 * httpServer.java
 * メインプログラムです。(main program)
 * httpserver.java
 *    |-- requestDispatch.java       --> using HttpReader
 *            |-- contextProcessor.java           |-- HttpReaderBuff
 *            |-- classProcessor.java             |-- HttpDataInputStream
 *            |-- cgiProcessor.java
 */
package http_pi;
import java.net.*;
import java.io.*;

//import javax.print.attribute.standard.Severity;

/**
 * @author Net Mall Tosa
 *
 * TODO この生成された型コメントのテンプレートを変更するには次へジャンプ:
 * ウィンドウ - 設定 - Java - コード・スタイル - コード・テンプレート
 */
public class httpServer {
	//メイン処理
	public static void main(String[] args) {
		boolean deamon_f=false;
		boolean keep_alive=true;
		if(args.length == 1){
			//throw new IllegalArgumentException("usage: >java httpServer <deamno>");
			if(args[0]=="deamno"){
				deamon_f=true;
			}
		}
		// server.conf ファイルの読み込み
		serverConfig srvconf = new serverConfig(deamon_f);
		srvconf.readConf();

		int numThreads = Defs.maxThread_cnt;		//スレッドループのMAXスレッド数
		int cnt = 0;			//接続カウンター
		try{
			System.out.println("httpServer Ver"+Defs.version+" update:"+Defs.update+" start!!");
			//サーバーソケット
			ServerSocket servsock = new ServerSocket(Defs.HTTP_PORT);
			System.out.println("httpServer :#2 passed!!");
			//5個のスレッド生成
			for(int i=0;i < numThreads;i++){
				requestDispatch req = new requestDispatch((short) i);
				Thread t = new Thread(req);
				t.start();
			}
			System.out.println("httpServer :#3 Connections on port"+ servsock.getLocalPort());
			
			while(true){
				try{
					Socket sock = servsock.accept();
					if(Defs.KA!=0){
						sock.setKeepAlive(true);
					}
					cnt++;
					//reqProc起動
					requestDispatch.reqProc(sock,cnt);
				}
				catch(IOException e){
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		catch(IOException e){
			System.err.println("httpServer :#99 io trouble Message="+e.getMessage());
		}
	}
}
