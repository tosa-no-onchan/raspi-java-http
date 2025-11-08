/*
 * CGILite.h
 *
 *  Created on: 2015/10/30
 *      Author: nishi
 */

#ifndef USR_CGI_LIB_CGILITE_HPP_
#define USR_CGI_LIB_CGILITE_HPP_

#include <stdio.h>      // printf
#include <stdlib.h>     // getenv

#include <string>
#include <iostream>
#include <tr1/unordered_map>

#include "HttpInputStreamCGI.hpp"
#include "Mylog.hpp"
#include "ComLib.hpp"

using namespace std;
using namespace tr1;

class CGILite {
private:
	bool debug;
	string env_req_method;	// "REQUEST_METHOD"  -> GET / POST
	string env_query_str;	//"QUERY_STRING"
	string env_cont_type;	//"CONTENT_TYPE"
	string env_cont_lng;		//"CONTENT_LENGTH"
	string env_cookie;		//"HTTP_COOKIE"
	string env_srv_lang;		//"SERVER_LANGUAGE"
	unsigned char *buf;
	ComLib comLib;
	Mylog mlog;
	void get_req();
	void post_req(HttpInputStreamCGI hin_r);
	void setParms();
	string parm;

public:
	CGILite();
	virtual ~CGILite();
    void in();
	unordered_map <string,string>parms;
};

#endif /* USR_CGI_LIB_CGILITE_HPP_ */
