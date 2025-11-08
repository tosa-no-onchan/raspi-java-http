package user_cgi_lib;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class MyLog {
	static final String logf="./log.txt";

	public MyLog() {
		// TODO 自動生成されたコンストラクター・スタブ
	}
	public void put(boolean fg,String lg){
		if(fg){
			try {
				File file = new File(logf);
				Writer wt = new FileWriter(file,true);
				wt.write(lg+"\n");
				wt.close();
			}
			catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
			
		}
	}
}
