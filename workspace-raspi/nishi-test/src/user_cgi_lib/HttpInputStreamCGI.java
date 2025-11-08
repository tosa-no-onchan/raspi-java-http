/**
 * Raspberry Pi java http server
 * v1.9 update 2015.10.29
 * 
 * HttpInputStream Class or HttpInputStreamCGI Class for CGI
 * どちらも、 BufferedInputStream(又は DataInputStream) の OverRide 版です。
 * 処理内容は、どちらも同じですが、Class 名を別にしてあります。
 * 
 * 1.HttpInputStream Class for searver
 *   HttpInputStream <- BufferedInputStream(又は DataInputStream) <- sock.getInputStream()
 * Http 通信に対応した、DataInputStream を提供します。
 * 
 * 1) インスタンス作成方法
 *  InputStream in = sock.getInputStream();
 *  HttpInputStream hdin = new HttpInputStream(in);
 * 2)この中から、ログをファイル出力しないで下さい。動作が保証できません。 2015.2.20
 *  
 * 2.HttpInputStreamCGI Class for CGI
 *   HttpInputStreamCGI <- BufferedInputStream(又は DataInputStream) <- System.in
 * Http Sever から fork されたCGI側の受信処理を行います。
 * POST データの Http Server からの stdin を介した受け取りを行います。
 * 
 * 1) インスタンス作成方法
 *  HttpInputStreamCGI hdin = new HttpInputStreamCGI(System.in);
 * 2)この中から、ログをファイル出力しないで下さい。動作が保証できません。 2015.2.20
 * 3)但し、テスト表示 System.out.print() は、クライアントに表示されるので、ログ出力で行います。
 * 4)ログをファイル出力する場合は、BufferedInputStream の OverRide を止めて行います。2015.10.29
 * 5) java server からのコールは、setCgiUse(true)を行って下さい。
 * 6)C server からのコールは、setCgiUse(false)を行って下さい。
 */
package user_cgi_lib;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class HttpInputStreamCGI{
//public class HttpInputStreamCGI extends BufferedInputStream {
	private byte[] buff_b;
	private int rem_lng;	// remain data length
	private int read_lng;	// reading data length
	private int cur_pos;
	private boolean ready_f;
	private boolean inter_ready_f;
	private boolean lf;
	private int can_l;
	private int boundary_no;
	
	private BufferedInputStream bip;
	// 注1) デバッグで、ログ出力を行うときは、BufferedInputStream の継承を行わないでください。
	// デバッグが終われば、BufferedInputStream の継承 でOKです。
	MyLog mlog = new MyLog();


	// 回線のスピードにあわせて、lct を増減します。
	// lct は、 2 以上を指定します。1だと、リクエストデータの取り込みが完了せずに、
	// データ無しで戻る場合があります。。
	private int lct=2;
	// 回線のスピードにあわせて、wait_t を増減します。
	private long wait_t=50;		// 200 -> DataInputStream / 50 -> BufferedInputStream
	private boolean cgi_use=false;	// CGI use 受信
	public boolean debug=false;
	public boolean debug2=false;
	public boolean debug3=false;
	private String myClassName;
	
	util_lib _util_lib;
	
	public HttpInputStreamCGI(InputStream in) {
		bip = new BufferedInputStream(in);
		//super(in);
		// TODO 自動生成されたコンストラクター・スタブ
		initMy();
	}

	public HttpInputStreamCGI(InputStream in, int size) {
		//super(in, size);
		bip = new BufferedInputStream(in,size);
		// TODO 自動生成されたコンストラクター・スタブ
		initMy();
	}
	private void initMy(){
		myClassName=getClass().getSimpleName();
		buff_b = new byte[1024];

		rem_lng=0;	// remain data length
		read_lng=0;	// reading data length
		cur_pos=0;
		inter_ready_f=true;
		ready_f=true;
		
		can_l=0;
		_util_lib=new util_lib();
	}
	/*
	 * setCgiUse()
	 */
	public void setCgiUse(boolean cgi_use){
		this.cgi_use=cgi_use;
	}
	/*
	 * public int _read(byte[] buf,int offs,int buf_len)
	 */
	public int _read(byte[] buf,int offs,int buf_len) throws IOException{
		int r_lng=0;
		// A cgi which is called from java server needs to set cgi_use=true.
		if(cgi_use){
			//r_lng = super.read(buf,offs,buf_len);
			r_lng= bip.read(buf,offs,buf_len);
		}
		// A cgi which is called from c server needs to set cgi_use=false.
		else{
			// 回線のスピードにあわせて、_lct を増減します。
			// _lct は、 2 以上を指定します。1だと、リクエストデータの取り込みが完了せずに、
			// データ無しで戻る場合があります。
			for(int _lct=lct;_lct>0;_lct--){
				if(can_l <= 0){
					//can_l = super.available();
					can_l=bip.available();
				}
				if(can_l > 0){
					//r_lng = super.read(buf,offs,buf_len);
					r_lng = bip.read(buf,offs,buf_len);
					can_l-=r_lng;
					break;
				}
				else{
					try {
						Thread.sleep(wait_t);
					}
					catch (InterruptedException e) {}
				}
			}
		}
		mlog.put(debug3,myClassName+"::_read() :#99 r_lng="+r_lng);
		return r_lng;
	}
	public boolean ready(){
		return ready_f;
	}
	/*
	 * String readline()
	 *  read one line as String.
	 *  DataInputStream.readLine() と同じ処理です。
	 *  TEXT を1行毎に読み込みます。
	 *  行末の CR,LF は、取り除かれます。
	 *  
	 * 1) http の request line と header (message header) line を読み込めます。
	 *  
	 *  HttpInputStream hin_r = new HttpInputStream(sock.getInputStream());
	 *  String req = hin_r.readline();
	 *  if(req != ""){
	 *     .....
	 *   }
	 *   
	 *  String in_s="";
	 *  while((in_s=hin_r.readline()).length() > 0){
	 *    ....
	 *   }
	 *  
	 * 2) http の body 部分(POSTのデータ部分)も読み込めます。方法は、上記1) に続けて、
	 *  下記を行います。
	 *  int n=0;
	 *  while (hin_r.ready() == true && n < 200000){
	 *    p_line=din_r.readline();
	 *     ....
	 *    n++;
	 *  }
	 */
	public String readline() throws IOException{
		String one_line="";
		lf=false;
		mlog.put(debug,myClassName+"::readline() :#1 ");
		while(lf==false){
			if(rem_lng < 2){
				read_off();
			}
			if(rem_lng >1){
				if(buff_b[cur_pos] == 0x0d && buff_b[cur_pos+1] == 0x0a){
					lf=true;
					cur_pos+=2;
					rem_lng-=2;
					break;
				}
				else if(buff_b[cur_pos] == 0x0a || buff_b[cur_pos] == 0x0d){
					lf=true;
					cur_pos++;
					rem_lng--;
					break;
				}
				else if(rem_lng >0){
					one_line+=(char)buff_b[cur_pos];
					cur_pos++;
					rem_lng--;
				}
			}
			else if(ready_f==false){
				// update by nishi start 2015.10.24
				if(rem_lng >0){
					if(buff_b[cur_pos] == 0x0a || buff_b[cur_pos] == 0x0d){
						lf=true;
						cur_pos++;
						rem_lng--;
					}
					else{
						one_line+=(char)buff_b[cur_pos];
						lf=true;
						cur_pos++;
						rem_lng--;
					}
				}
				else{
					lf=true;
				}
				// update by nishi end 2015.10.24
			}
		}
		mlog.put(debug,myClassName+"::readline()() :#99 one_line="+one_line);
		return one_line;
	}
	/*
	 * void read_off()
	 */
	private void read_off() throws IOException{
		int off_pos=0;
		if(rem_lng > 0 && cur_pos > 0){
			for(int i=0;i<rem_lng;i++){
				buff_b[i]=buff_b[cur_pos+i];
			}
			off_pos=rem_lng;
		}
		cur_pos=0;
		if(inter_ready_f!=false){
			read_lng=_read(buff_b,off_pos,buff_b.length-off_pos);
			if(read_lng<=0){
				inter_ready_f=false;
				//ready_f=false;
			}
			else{
				rem_lng=read_lng+off_pos;
			}
		}
		else{
			ready_f=false;
		}
	}
	/*
	 * int readToBoundary(byte out_f[],int offs,int out_lng,byte[] bound1,byte[] bound2)
	 */
	public int readToBoundary(byte out_f[],int offs,int out_lng,byte[] bound1,byte[] bound2) throws IOException{
		int out_l=0;
		int bound1_l=bound1.length;
		int bound2_l=bound2.length;
		int bound_l=bound1_l;
		if(bound2_l>bound_l){
			bound_l=bound2_l;
		}
		lf=false;
		boundary_no=0;
		//System.out.println(myClassName+"::readToBoundary() :#1 cur_pos="+cur_pos+",rem_lng="+rem_lng);
		while(lf==false && out_lng > 0){
			//System.out.println(myClassName+"::readToBoundary() :#2 rem_lng="+rem_lng+",bound_l="+bound_l+",out_lng="+out_lng);
			if(rem_lng < bound_l && ready_f==true){
				//System.out.println(myClassName+"::readToBoundary() :#3 rem_lng="+rem_lng+",bound_l="+bound_l);
				read_off();
			}
			if(rem_lng >= bound1_l && _util_lib.compByteArry(buff_b,cur_pos,bound1,bound1_l)==true){
				//System.out.println(myClassName+"::readToBoundary() :#4 found Boundary1");
				//System.out.println(myClassName+"::readToBoundary() :#4 buf=\n"+_util_lib.BytetoHex(buff_b,cur_pos,bound1_l));
				//System.out.println(myClassName+"::readToBoundary() :#4 buf=");
				//_util_lib.printByte(buff_b,cur_pos,bound1_l);

				lf=true;
				cur_pos+=bound1_l;
				rem_lng-=bound1_l;
				boundary_no=1;
				break;
			}
			else if(rem_lng >= bound2_l && _util_lib.compByteArry(buff_b,cur_pos,bound2,bound2_l)==true){
				//System.out.println(myClassName+"::readToBoundary() :#5 found Boundary2");
				//System.out.println(myClassName+"::readToBoundary() :#5 buf=\n"+_util_lib.BytetoHex(buff_b,cur_pos,bound2_l));
				//System.out.println(myClassName+"::readToBoundary() :#4 buf=");
				//_util_lib.printByte(buff_b,cur_pos,bound2_l);

				lf=true;
				cur_pos+=bound2_l;
				rem_lng-=bound2_l;
				boundary_no=2;
				break;
			}
			else if(rem_lng > 0){
				out_f[offs] = buff_b[cur_pos];
				cur_pos++;
				rem_lng--;
				offs++;
				out_l++;
				out_lng--;
			}
			else if(ready_f==false){
				//System.out.println(myClassName+"::readToBoundary() :#10 cur_pos="+cur_pos+",rem_lng="+rem_lng);
				break;
			}
		}
		//System.out.println(myClassName+"::readline()() :#99 out_l="+out_l);		
		return out_l;
	}
	public int check_Boundary_no(){
		return boundary_no;
	}
	/*
	 * int readData(byte[] out_f,int out_lng)
	 * read Bytes Data until lf(line feed) appear.
	 *  読み込みデータに LF が表れるまで、あるいは、読み込みバッファサイズまで、
	 *  あるいは入力ストリームの終了まで byte[] で読み込みます。
	 *  http の body 部分(POST のデータ部分)を byte 読み込みます。
	 * 
	 * 1) http の body 部分(POSTのデータ部分)の読み込み。方法は、下記
	 *  int n=0;
	 *  int data_l;
	 *  byte[] buff= new byte[1024];
	 *  while (din_r.ready() == true && n < 200000){
	 *    data_l=din_r.readData(buff,1024);
	 *    if(data_l >= 0){  // cr,lf を含んでいます。
	 *       ....
	 *     }
	 *     ....
	 *    n++;
	 *  }
	 */
	public int readData_notuse(byte[] out_f,int out_lng) throws IOException{
		int out_l=0;
		int pos=0;
		int i;
		lf=false;
		mlog.put(debug2,myClassName+"::readData() :#1 cur_pos="+cur_pos+",buff_b[cur_pos]="+buff_b[cur_pos]+",rem_lng="+rem_lng);
		while(lf == false && out_lng > 0){
			if(rem_lng == 0){
				cur_pos=0;
				rem_lng=read_lng=_read(buff_b,0,buff_b.length);
				if(read_lng <= 0 ){
					ready_f=false;
					return out_l;
				}
			}
			i=readDataToLf(out_f,pos,out_lng);
			pos+=i;
			out_lng-=i;
			out_l+=i;
		}
		return out_l;
	}
	/*
	 * readDataToLf(byte[] out_f,int pos,int out_lng)
	 * read Bytes Data until lf(line feed) appear.
	 */
	private int readDataToLf(byte[] out_f,int pos,int out_lng){
		int out_l;
		int rem_l;
		if(out_lng <= rem_lng){
			rem_l= out_lng;
		}
		else{
			rem_l=rem_lng;
		}
		mlog.put(debug2,myClassName+"::readDataToLf() :#1 cur_pos="+cur_pos+",buff_b[cur_pos]="+buff_b[cur_pos]+",rem_lng="+rem_lng);
		for(out_l=0;lf==false && out_l < rem_l;out_l++){
			if(buff_b[cur_pos+out_l] == 0x0a){
				lf=true;
			}
		}
		// copy buff_b[cur_pos]  -> out_f[pos]  length=out_l
		System.arraycopy(buff_b, cur_pos, out_f, pos, out_l);
		cur_pos+=out_l;
		rem_lng-=out_l;
		if(rem_lng<0){
			rem_lng=0;
		}
		mlog.put(debug2,myClassName+"::readDataToLf() :#99 cur_pos="+cur_pos+",rem_lng="+rem_lng+",out_l="+out_l);
		return out_l;
	}

	/*
	 * int readBlock(byte[] out_f,int out_lng)
	 * read Bytes Data until full out_f or eod.
	 *  読み込みバッファサイズまで、
	 *  あるいは入力ストリームの終了まで byte[] で読み込みます。
	 *  http の body 部分(POST のデータ部分)を byte 読み込みます。
	 * 
	 * 1) http の body 部分(POSTのデータ部分)の読み込み。方法は、下記
	 *  int n=0;
	 *  int data_l;
	 *  byte[] buff= new byte[1024];
	 *  while (din_r.ready() == true && n < 200000){
	 *    data_l=din_r.readBlock(buff,1024);
	 *    if(data_l >= 0){
	 *       ....
	 *     }
	 *     ....
	 *    n++;
	 *  }
	 */
	public int readBlock(byte[] out_f,int out_lng) throws IOException{
		int out_l=0;	// actual read data length
		int rd_l=0;
		int pos=0;	// next read point of out_f[] 
		mlog.put(debug2,myClassName+"::readBlock() :#1 cur_pos="+cur_pos+",buff_b[cur_pos]="+buff_b[cur_pos]+",rem_lng="+rem_lng);
		while(out_lng > 0){
			// buff_b remain data length == 0
			if(rem_lng == 0){
				mlog.put(debug2,myClassName+"::readBlock() :#2 pos="+pos+",out_lng="+out_lng);
				// read execute
				rd_l=_read(out_f,pos,out_lng);
				if(rd_l <= 0 ){
					ready_f=false;
					return out_l;
				}
				out_l+=rd_l;
				out_lng-=rd_l;
				pos+=rd_l;
			}
			// require read size >= buff_b remain data length
			else if(out_lng >= rem_lng){
				mlog.put(debug2,myClassName+"::readBlock() :#3 pos="+pos+",out_lng="+out_lng+",rem_lng="+rem_lng);
				// copy buff_b[cur_pos]  -> out_f[pos]  length=rem_lng
				System.arraycopy(buff_b, cur_pos, out_f, pos, rem_lng);
				out_l+=rem_lng;
				out_lng-=rem_lng;
				pos+=rem_lng;
				cur_pos=0;
				rem_lng=0;
			}
			// require read size < buff_b remain data length
			else{
				mlog.put(debug2,myClassName+"::readBlock() :#4 pos="+pos+",out_lng="+out_lng+",rem_lng="+rem_lng);
				// copy buff_b[cur_pos]  -> out_f[pos]  length=out_lng
				System.arraycopy(buff_b, cur_pos, out_f, pos, out_lng);
				out_l+=out_lng;
				pos+=out_lng;
				cur_pos+=out_lng;
				rem_lng-=out_lng;
				out_lng=0;
			}
		}
		return out_l;
	}
}
