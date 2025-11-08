/*
 * HttpInputStreamCGI.cpp
 *
 *  Created on: 2015/10/30
 *      Author: nishi
 */

#include "HttpInputStreamCGI.hpp"


using namespace std;

HttpInputStreamCGI::HttpInputStreamCGI() {
	// TODO 自動生成されたコンストラクター・スタブ
	initMy();

}


HttpInputStreamCGI::~HttpInputStreamCGI() {
	// TODO Auto-generated destructor stub
	//delete[] buff_b;
}


/*
 * void initMy()
 */
void HttpInputStreamCGI::initMy(){
	//myClassName=getClass().getSimpleName();

	//buff_b = new byte[1024];
	buff_b = new unsigned char [1024];


	rem_lng=0;	// remain data length
	read_lng=0;	// reading data length
	cur_pos=0;
	inter_ready_f=true;
	ready_f=true;

	can_l=0;
	//_util_lib=new util_lib();


	// 回線のスピードにあわせて、lct を増減します。
	// lct は、 2 以上を指定します。1だと、リクエストデータの取り込みが完了せずに、
	// データ無しで戻る場合があります。。
	lct=2;
	// 回線のスピードにあわせて、wait_t を増減します。
	wait_t=50;		// 200 -> DataInputStream / 50 -> BufferedInputStream
	cgi_use=false;	// CGI use 受信

	debug=false;
	debug2=false;
	debug3=false;
}

/*
 * bool ready()
 */
bool HttpInputStreamCGI::ready(){
	bool rc_f=false;
	if(hrd.httpBuff.isEof == false){
		rc_f=true;
	}
	return rc_f;
}


/*
 * String readline()
 *  read one line as String.
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
string HttpInputStreamCGI::readline(){
	string one_line="";
	int lng =hrd.readLine();

	if(lng > 0){
		one_line = hrd.httpBuff.a_line;
	}
	if(debug)mlog.putb("HttpInputStreamCGI::readline() :#99 one_line="+one_line);
	return one_line;
}

/*
 * void read_off()
 */
void HttpInputStreamCGI::read_off(){
	int off_pos=0;
	if(rem_lng > 0 && cur_pos > 0){
		for(int i=0;i<rem_lng;i++){
			buff_b[i]=buff_b[cur_pos+i];
		}
		off_pos=rem_lng;
	}
	cur_pos=0;
	if(inter_ready_f!=false){
		read_lng=readBlock(buff_b+off_pos,sizeof(buff_b)-off_pos);
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
int HttpInputStreamCGI::readToBoundary(unsigned char *out_f,int offs,int out_lng,unsigned char *bound1,unsigned char *bound2){
	int out_l=0;
	int bound1_l=sizeof(bound1);
	int bound2_l=sizeof(bound2);
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
		if(rem_lng >= bound1_l && utilLib.compByteArry(buff_b,cur_pos,bound1,bound1_l)==true){
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
		else if(rem_lng >= bound2_l && utilLib.compByteArry(buff_b,cur_pos,bound2,bound2_l)==true){
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

/*
 * int check_Boundary_no()
 */
int HttpInputStreamCGI::check_Boundary_no(){
	return boundary_no;
}


/*
 * readDataToLf(byte[] out_f,int pos,int out_lng)
 * read Bytes Data until lf(line feed) appear.
 */
int HttpInputStreamCGI::readDataToLf(unsigned char *out_f,int pos,int out_lng){
	int out_l;
	int rem_l;
	if(out_lng <= rem_lng){
		rem_l= out_lng;
	}
	else{
		rem_l=rem_lng;
	}
	//mlog.put(debug2,myClassName+"::readDataToLf() :#1 cur_pos="+cur_pos+",buff_b[cur_pos]="+buff_b[cur_pos]+",rem_lng="+rem_lng);
	for(out_l=0;lf==false && out_l < rem_l;out_l++){
		if(buff_b[cur_pos+out_l] == 0x0a){
			lf=true;
		}
	}
	// copy buff_b[cur_pos]  -> out_f[pos]  length=out_l
	//System.arraycopy(buff_b, cur_pos, out_f, pos, out_l);
	cur_pos+=out_l;
	rem_lng-=out_l;
	if(rem_lng<0){
		rem_lng=0;
	}
	//mlog.put(debug2,myClassName+"::readDataToLf() :#99 cur_pos="+cur_pos+",rem_lng="+rem_lng+",out_l="+out_l);
	return out_l;
}

/*
 * int readBlock(unsigned char *out_f,int out_lng)
 * read unsigned char Data until full out_f or eod.
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
int HttpInputStreamCGI::readBlock(unsigned char *out_f,int out_lng){
	int out_l=0;	// actual read data length
	out_l=hrd.readBlock(out_f,out_lng);
	return out_l;
}

