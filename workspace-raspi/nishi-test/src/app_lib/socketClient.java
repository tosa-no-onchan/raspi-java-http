/**
 * app_lib.socketClient
 * process socket communication client class
 * プロセスソケット通信 クライアントクラス
 */
package app_lib;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author nishi
 *
 */
public class socketClient {
	String hostName ="localhost";
	//hostName ="192.168.1.125";
	int portNo = 4002;
	/**
	 * 
	 */
	public socketClient() {
		// TODO 自動生成されたコンストラクター・スタブ
	}
	public socketClient(int portNo) {
		// TODO 自動生成されたコンストラクター・スタブ
		this.portNo=portNo;
	}
	public String sendReq(String req_s) throws UnknownHostException{
		String ans_s="";
		Socket sock;
		System.out.println("app_lib.socketClient::sendReq() #1");
		try{
			//ソケット作成
			sock = new Socket(hostName,portNo);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
			out.write(req_s+"\n");
			out.flush();

			BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			ans_s=in.readLine().trim();
			System.out.println("app_lib.socketClient::sendReq() #2 ans_s="+ans_s);
			sock.close();
		}
		catch(IOException e){
			System.err.println("app_lib.socketClient::sendReq() #90 "+e);
			ans_s=e.getMessage();
		}
		//System.out.println("app_lib.socketClient::sendReq() #3 ans_s="+com_lib.StringtoHex(ans_s));
		return ans_s;
	}	
}
