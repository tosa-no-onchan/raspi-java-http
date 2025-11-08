/**
 * Raspberry Pi java http server
 * v1.8.1 update 2015.10.22
 * keepAliveProcessor.java
 *  keep alive probe packet response
 */

package http_pi;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class keepAliveProcessor {
	public keepAliveProcessor() {
		// TODO 自動生成されたコンストラクター・スタブ
	}
	public void exec(boolean ka,Socket sock){
		System.out.println("keepAliveProcessor::exec :#1");
		if(ka==true){
			try {
				OutputStream outs = sock.getOutputStream();
				outs.flush();
			}
			catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
	}
}
