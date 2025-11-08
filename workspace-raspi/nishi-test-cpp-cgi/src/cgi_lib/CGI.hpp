/*
 * CGI.hpp
 *
 *  Created on: 2015/11/01
 *      Author: nishi
 */

#ifndef USR_CGI_LIB_CGI_HPP_
#define USR_CGI_LIB_CGI_HPP_

#include <stdio.h>      // printf
#include <stdlib.h>     // getenv

#include <string>
#include <iostream>
#include <sys/time.h>
#include <time.h>
//#include <ctime>
#include <tr1/unordered_map>
#include <sys/stat.h>
#include <cstdlib>

#include "HttpInputStreamCGI.hpp"
#include "ComLib.hpp"

using namespace std;
using namespace tr1;

class CGI {
private:
	string env_req_method;	// "REQUEST_METHOD"  -> GET / POST
	string env_query_str;	//"QUERY_STRING"
	string env_cont_type;	//"CONTENT_TYPE"
	string env_cont_lng;		//"CONTENT_LENGTH"
	string env_cookie;		//"HTTP_COOKIE"
	string env_srv_lang;		//"SERVER_LANGUAGE"
	unsigned char *buf;
	ComLib comLib;
	void get_req();
	void post_req(HttpInputStreamCGI hin_r);
	void setParms();
	string upload_img(HttpInputStreamCGI hin_r);
	string make_upload_fname();
	string parm;
	// string p_data;
	int ph;
	string boundary;
	string boundary1;
	string boundary2;
	string upload_parm_name;

	string Defs_tmp_dir;	// file up-load working dir

	Mylog mlog;

	bool err;	// エラー時のログ出力
	bool debug;	// テスト用のログ出力

public:
	CGI();
	virtual ~CGI();
    void in();
	unordered_map <string,string>parms;
};


#endif /* USR_CGI_LIB_CGI_HPP_ */
