/*
 * HttpInputStreamCGI.h
 *
 *  Created on: 2015/10/30
 *      Author: nishi
 */

#ifndef USR_CGI_LIB_HTTPINPUTSTREAMCGI_HPP_
#define USR_CGI_LIB_HTTPINPUTSTREAMCGI_HPP_

//#include <string.h>  //strlen
#include <string>  	//string
#include <sstream>


#include "Mylog.hpp"
#include "ComLib.hpp"
#include "HttpReaderCGI.hpp"
#include "UtilLib.hpp"

using namespace std;

class HttpInputStreamCGI {
public:
	HttpInputStreamCGI();
	virtual ~HttpInputStreamCGI();

	bool ready();

	string readline();
	void read_off();
	int readToBoundary(unsigned char *out_f,int offs,int out_lng,unsigned char *bound1,unsigned char *bound2);
	int readBlock(unsigned char *out_f,int out_lng);
	int check_Boundary_no();
	int readDataToLf(unsigned char *out_f,int pos,int out_lng);

private:
	//byte[] buff_b;
	unsigned char *buff_b;

	int rem_lng;	// remain data length
	int read_lng;	// reading data length
	int cur_pos;
	bool ready_f;
	bool inter_ready_f;
	bool lf;
	int can_l;
	int boundary_no;

	//BufferedInputStream bip;
	// 注1) デバッグで、ログ出力を行うときは、BufferedInputStream の継承を行わないでください。
	// デバッグが終われば、BufferedInputStream の継承 でOKです。
	//MyLog mlog = new MyLog();


	// 回線のスピードにあわせて、lct を増減します。
	// lct は、 2 以上を指定します。1だと、リクエストデータの取り込みが完了せずに、
	// データ無しで戻る場合があります。。
	int lct;		//=2;
	// 回線のスピードにあわせて、wait_t を増減します。
	long wait_t;	//=50;		// 200 -> DataInputStream / 50 -> BufferedInputStream
	bool cgi_use;	//=false;	// CGI use 受信

	bool debug;	//=false;
	bool debug2;	//=false;
	bool debug3;	//=false;

	string myClassName;

	UtilLib utilLib;
	HttpReaderCGI hrd;
	Mylog mlog;
	ComLib comLib;

	void initMy();
};

//} /* namespace std */

#endif /* USR_CGI_LIB_HTTPINPUTSTREAMCGI_HPP_ */
