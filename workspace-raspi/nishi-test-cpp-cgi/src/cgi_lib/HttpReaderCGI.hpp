/*
 * HttpReader.hpp
 *
 *  Created on: 2015/11/01
 *      Author: nishi
 */

#ifndef USR_CGI_LIB_HTTPREADERCGI_HPP_
#define USR_CGI_LIB_HTTPREADERCGI_HPP_

#include <stdio.h>
#include <stdlib.h>
#include <stddef.h>
#include <sys/ioctl.h>
#include <unistd.h>

#include <string>
#include <cstring>
#include "Mylog.hpp"
#include "ComLib.hpp"

using namespace std;

#define HttpBuff_a_LineSize 256
#define HttpBuff_i_buffSize 1024
#define HttpReq_max_header 20
//#define HttpReq_buf_Size 4096	// cgi からの受信バッファサイズ

/*
 * HttpBuff
 */
typedef struct _httpBuff HttpBuff;
struct _httpBuff{
	bool isCr;
	bool isEof;
	char a_line[HttpBuff_a_LineSize+1];	// getLine field
	char i_buff[HttpBuff_i_buffSize];	// read buffer
	int cur_p;	// current i_buff pointer
	int r_lng;	// remain i_buff data lng
};

/*
 * HttpReader.h
 * Version 1.1
 * Update on: 2014/03/26
 */
typedef struct _httpReqCgi HttpReqCgi;
struct _httpReqCgi{
	int fd_i;	// fd stdin
	int seq_i;	// sequence no
	int hd_c;	// pool header records count
	char *req;
	char *method;
	char *fname;
	char *parm;
	char *version;
	char *header[HttpReq_max_header];
};

class HttpReaderCGI {
public:
	HttpReaderCGI();
	virtual ~HttpReaderCGI();
	int readLine();
	int readLine_not_use();
	int readBlock(unsigned char *out_f,int out_lng);
	int readBlock_not_use(unsigned char *out_f,int out_lng);
	HttpReqCgi httpReqCgi;
	HttpBuff httpBuff;
private:
	int fd_i;	// input file descriptor
	bool debug;
	Mylog mylog;
	ComLib comLib;

};


#endif /* USR_CGI_LIB_HTTPREADERCGI_HPP_ */
