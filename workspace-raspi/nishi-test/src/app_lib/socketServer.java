/**
 * app_lib.socketServer
 * process socket communication server class
 * プロセスソケット通信 サーバークラス
 */
package app_lib;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author nishi
 *
 */
public abstract class socketServer {
	int portNo = 4002;
	ServerSocket servsock;
	Socket sock;
	/**
	 * 
	 */
	public socketServer() {
		// TODO 自動生成されたコンストラクター・スタブ
	}
	public socketServer(int portNo) {
		// TODO 自動生成されたコンストラクター・スタブ
		this.portNo=portNo;
	}
	/*
	 * getReq()
	 */
	public void getReq(){
		System.out.println("app_lib.socketServer::getReq() #1 start portNo="+portNo);
		try{
			servsock = new ServerSocket(portNo);
			while(true){
				sock = servsock.accept();		// check connection request from client
				System.out.println("app_lib.socketServer::getReq() #2 get accept");
				
				reqProcesser(sock);
				
				try {
					Thread.sleep(300);
				}
				catch (InterruptedException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
				//sock.close();	// 必要かどうか?
				System.out.println("app_lib.socketServer::getReq() #3 sock.close()");
			}
		}
		catch(IOException e){
			System.err.println("app_lib.socketServer::getReq() #90 error "+e);
		}		
	}
	/*
	 * reqProcesser(Socket sock)
	 */
	public abstract void reqProcesser(Socket sock);
}
