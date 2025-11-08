/**
 * Raspberry Pi java http server
 * v1.0
 * taskTable Class
 * 接続毎の管理情報を保持します。
 */
package http_pi;

import java.util.Hashtable;

/**
 * @author nishi
 *
 */
public class taskTable {
	public Hashtable<String,String> http_hd;
	public Hashtable<String,String> req_line;
	public long t_timer;	// timeout system timer
	/**
	 * 
	 */
	public taskTable() {
		// TODO 自動生成されたコンストラクター・スタブ
		req_line = new Hashtable<String,String>();
		http_hd = new Hashtable<String,String>();
		// Defs.Timeout [sec]
		t_timer = System.currentTimeMillis()+Defs.Timeout*1000L;
	}
	public boolean isTimeout(){
		boolean rc=true;
		// time out
		if(t_timer < System.currentTimeMillis()){
			rc=false;
		}
		return rc;
	}

}
