package http_pi;

public class util_lib {

	public util_lib() {
		// TODO 自動生成されたコンストラクター・スタブ
	}
	/*
	 * バイト配列の比較
	 */
	public boolean compByteArry(byte[] a,int offs,byte[] b,int lng){
		boolean rc=true;
		for(int i=0;i<lng;i++){
			if(a[offs+i]!=b[i]){
				rc=false;
				break;
			}
		}
		return rc;
	}
	/*

	 * 文字列→１６進コード変換
	 */
	public static String StringtoHex(String in){
		// 文字コードを１６進に変換します
		String h;
		String o="";
		byte[] b = in.getBytes();
		for (int i =0; i < b.length;i++){
			h = Integer.toHexString(b[i]).toUpperCase();
			int l = h.length();
			//System.out.println("\nform_lib:urlEncode() : #2 h="+h);
			if(l > 2){
				l -= 2;
				o += h.substring(l);
			}
			else if(l == 1){
				o += "0"+h;
			}
			else{
				o += h;
			}
		}
		return o;
		
	}
	/*
	 * バイト文字列→１６進コード変換
	 */
	public static String BytetoHex(byte[] b,int off,int lg){
		// 文字コードを１６進に変換します
		String h;
		String o="";
		//byte[] b = in.getBytes();
		for (int i =0; i < lg;i++){
			h = Integer.toHexString(b[i+off]).toUpperCase();
			int l = h.length();
			//System.out.println("\nform_lib:urlEncode() : #2 h="+h);
			if(l > 2){
				l -= 2;
				o += h.substring(l);
			}
			else if(l == 1){
				o += "0"+h;
			}
			else{
				o += h;
			}
		}
		return o;
	  }
	  /*
	   * printByte(byte[] buf,int lg) 
	   */
	  public void printByte(byte[] buf,int off,int lg){
		  for(int i=0;i<lg;i++){
			  System.out.print((char)buf[off+i]);
		  }
	  }	
}
