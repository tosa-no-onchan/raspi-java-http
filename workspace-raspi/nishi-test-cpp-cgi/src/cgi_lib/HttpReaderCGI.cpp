/*
 * HttpReader.cpp
 *
 *  Created on: 2015/11/01
 *      Author: nishi
 */

#include "HttpReaderCGI.hpp"

using namespace std;

HttpReaderCGI::HttpReaderCGI() {
	// TODO 自動生成されたコンストラクター・スタブ

	debug=false;
	// HttpReqCgi 初期化
	httpReqCgi.fd_i=0;	// fd stdin
	httpReqCgi.seq_i=0;	// sequence no
	httpReqCgi.hd_c=0;	// pool header records count

	// HttpBuff 初期化
	httpBuff.isCr=false;
	httpBuff.isEof=false;
	httpBuff.cur_p=0;	// current i_buff pointer
	httpBuff.r_lng=0;	// remain i_buff data lng
}

HttpReaderCGI::~HttpReaderCGI() {
	// TODO Auto-generated destructor stub
}
/*
 * int readLine()
 * update by nishi 201.4.5.6
 */
int HttpReaderCGI::readLine(){
	int lng;
	int i;
	if(debug)mylog.putb("HttpReaderCGI::readLine():1 start");
	for(lng=0;lng < HttpBuff_a_LineSize;){
		if(httpBuff.r_lng == 0){
			if(httpBuff.isEof==false){
				if(debug)mylog.putb("HttpReaderCGI::readLine():2 passed!");
				int nread;
				nread = read(httpReqCgi.fd_i,&(httpBuff.i_buff[0]),HttpBuff_i_buffSize);
				// データ OK
				if(nread > 0){
					httpBuff.r_lng = nread;
				}
				// データ無し or pipe クローズ
				else if(nread <= 0){
					httpBuff.r_lng=0;
					httpBuff.isEof=true;
					break;
				}
				if(debug)mylog.putb("HttpReaderCGI::readLine():3 passed!");
			}
			httpBuff.cur_p = 0;
			string s = comLib.ItoStr(httpBuff.r_lng);
			if(debug)mylog.putb("HttpReaderCGI::readLine():10 httpBuff.r_lng="+s);

		}
		if(httpBuff.i_buff[httpBuff.cur_p]==0x0a){
			httpBuff.cur_p++;
			httpBuff.r_lng--;
			break;
		}
		else if(httpBuff.i_buff[httpBuff.cur_p]!=0x0d){
			httpBuff.a_line[lng]=httpBuff.i_buff[httpBuff.cur_p];
			lng++;
		}
		httpBuff.cur_p++;
		httpBuff.r_lng--;
	}
	httpBuff.a_line[lng] = 0x00;

	if(debug){
		string s = comLib.ItoStr(httpBuff.r_lng);
		string s2 = comLib.ItoStr(lng);
		mylog.putb("HttpReaderCGI::readLine():99 httpBuff.r_lng="+s+",lng="+s2);
	}
	return lng;
}

/*
 * int readLine()
 * update by nishi 201.4.5.6
 */
int HttpReaderCGI::readLine_not_use(){
	int lng;
	int i;
	if(debug)mylog.putb("HttpReaderCGI::readLine():1 start");
	for(lng=0;lng < HttpBuff_a_LineSize;){
		if(httpBuff.r_lng == 0){
			if(httpBuff.isEof==false){
				if(debug)mylog.putb("HttpReaderCGI::readLine():2 passed!");
				int i;
				int nread;
				for(i=0;i<=2;i++){
					//受信可能なデータサイズのチェック
					ioctl(httpReqCgi.fd_i, FIONREAD, &nread);
					if(nread > 0){
						break;
					}
					else{
						usleep(50000);	// 50 m sec -> 0.05 sec
					}
				}
				// データ OK
				if(nread > 0){
					httpBuff.r_lng = read(httpReqCgi.fd_i,&(httpBuff.i_buff[0]),HttpBuff_i_buffSize);
				}
				// データ無し or pipe クローズ
				else if(nread <= 0){
					httpBuff.isEof=true;
					break;
				}
				if(debug)mylog.putb("HttpReaderCGI::readLine():3 passed!");
			}
			httpBuff.cur_p = 0;
			string s = comLib.ItoStr(httpBuff.r_lng);
			if(debug)mylog.putb("HttpReaderCGI::readLine():10 httpBuff.r_lng="+s);

		}
		if(httpBuff.i_buff[httpBuff.cur_p]==0x0a){
			httpBuff.cur_p++;
			httpBuff.r_lng--;
			break;
		}
		else if(httpBuff.i_buff[httpBuff.cur_p]!=0x0d){
			httpBuff.a_line[lng]=httpBuff.i_buff[httpBuff.cur_p];
			lng++;
		}
		httpBuff.cur_p++;
		httpBuff.r_lng--;
	}
	httpBuff.a_line[lng] = 0x00;

	if(debug){
		string s = comLib.ItoStr(httpBuff.r_lng);
		string s2 = comLib.ItoStr(lng);
		mylog.putb("HttpReaderCGI::readLine():99 httpBuff.r_lng="+s+",lng="+s2);
	}
	return lng;
}
/*
 * int readBlock(unsigned char *out_f,int out_lng)
 * update by nishi 201.4.5.6
 */
int HttpReaderCGI::readBlock(unsigned char *out_f,int out_lng){
	int nread;
	int rlng=0;
	//読み込み残のデータがあります。
	if(httpBuff.r_lng > 0){
		//読み込み残のデータは、出力バッファより大きいです。
		if(httpBuff.r_lng > out_lng){
			memcpy(out_f,&(httpBuff.i_buff[httpBuff.cur_p]), httpBuff.r_lng);
			rlng=out_lng;
			httpBuff.cur_p += rlng;
			httpBuff.r_lng -= rlng;
			out_lng=0;
		}
		//読み込み残のデータは、出力バッファより小さいです。
		else{
			memcpy(out_f,&(httpBuff.i_buff[httpBuff.cur_p]), httpBuff.r_lng);
			rlng=httpBuff.r_lng;
			httpBuff.cur_p += rlng;
			httpBuff.r_lng -= rlng;
			out_lng -= rlng;
		}
	}
	if(out_lng > 0 && httpBuff.isEof == false){
		nread=read(httpReqCgi.fd_i,out_f+rlng,out_lng);
		if(nread > 0){
				rlng+=nread;
		}
		else{
			httpBuff.isEof=true;
		}
	}
	//printf("readBlock() : #90 rlng=%d\n",rlng);
	//memdump(httpReq->buff,rlng);
	return rlng;
}

/*
 * int readBlock(unsigned char *out_f,int out_lng)
 * update by nishi 201.4.5.6
 */
int HttpReaderCGI::readBlock_not_use(unsigned char *out_f,int out_lng){
	int nread;
	int rlng=0;
	//読み込み残のデータがあります。
	if(httpBuff.r_lng > 0){
		//読み込み残のデータは、出力バッファより大きいです。
		if(httpBuff.r_lng > out_lng){
			memcpy(out_f,&(httpBuff.i_buff[httpBuff.cur_p]), httpBuff.r_lng);
			rlng=out_lng;
			httpBuff.cur_p += rlng;
			httpBuff.r_lng -= rlng;
			out_lng=0;
		}
		//読み込み残のデータは、出力バッファより小さいです。
		else{
			memcpy(out_f,&(httpBuff.i_buff[httpBuff.cur_p]), httpBuff.r_lng);
			rlng=httpBuff.r_lng;
			httpBuff.cur_p += rlng;
			httpBuff.r_lng -= rlng;
			out_lng -= rlng;
		}
	}
	if(out_lng > 0 && httpBuff.isEof == false){
		//受信可能なデータサイズのチェック
		ioctl(httpReqCgi.fd_i, FIONREAD, &nread);
		if(nread > 0){
			nread=read(httpReqCgi.fd_i,out_f+rlng,out_lng);
			if(nread>0){
				rlng+=nread;
			}
		}
		else{
			httpBuff.isEof=true;
		}
	}
	//printf("readBlock() : #90 rlng=%d\n",rlng);
	//memdump(httpReq->buff,rlng);
	return rlng;
}
