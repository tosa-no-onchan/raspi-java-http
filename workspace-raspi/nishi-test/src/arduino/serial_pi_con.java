/**
 * arduino.serial_pi_con.java
 * Arduino と USB Serial 通信 で接続します
 * socket process 通信は、 socketServer を継承して行います。
 * Arduino USB Serial 通信は、 serial_pi.java を使います。
 */
package arduino;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import app_lib.socketServer;

/**
 * @author nishi
 *
 */
public class serial_pi_con extends socketServer {
	/**
	 * 
	 */
	serial_pi ser_pi;
	public serial_pi_con() {
		super();
		// TODO 自動生成されたコンストラクター・スタブ
		ser_pi = new serial_pi();
		ser_pi.isConnect();
	}
	public serial_pi_con(int portNo) {
		super(portNo);
		// TODO 自動生成されたコンストラクター・スタブ
		ser_pi = new serial_pi();
		ser_pi.isConnect();
	}
	
	@Override
	public void reqProcesser(Socket sock) {
		// TODO 自動生成されたメソッド・スタブ
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));

			String req_s = in.readLine().trim();
			System.out.println("serial_pi_con::reqProcesser() #1 req_s="+req_s);
			if(ser_pi.isConnect()==false){
				System.err.println("serial_pi_con::reqProcesser() #2 not connected to ardiono");
				out.write("ng\n");
			}
			else{
				ser_pi.set_redirecter(out);
				ser_pi.ser_send(req_s);
				long tlimit=System.currentTimeMillis()+5000L;
				while(ser_pi.rcv_cnt==0){
					if(System.currentTimeMillis()>tlimit){
						System.err.println("serial_pi_con::reqProcesser() #3 arduino receve time out");
						out.write("ng\n");
						out.flush();
						ser_pi.setConnectError();
						break;
					}
					// wait 1 second before continuing
					try {
						Thread.sleep(100);
					}
					catch (InterruptedException e) {
						// TODO 自動生成された catch ブロック
						e.printStackTrace();
					}
				}
				ser_pi.remove_redirecter();
			}
			//out.flush();
			out.close();
		}
		catch (IOException e1) {
			// TODO 自動生成された catch ブロック
			//e1.printStackTrace();
			System.err.println("serial_pi_con::reqProcesser() #90 error "+e1);
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		serial_pi_con spi_con = new serial_pi_con(4002);
		// socket からのリクエストを受信します
		spi_con.getReq();
	}
}
