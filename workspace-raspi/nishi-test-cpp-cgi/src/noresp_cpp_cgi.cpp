/*
 * noresp_cpp_cgi.cpp
 *
 *  Created on: 2015/10/30
 *      Author: nishi
 */

#include <stdio.h>      // printf
#include <stdlib.h>     // getenv

//#include <iostream>	// cout
#include <string>

#include "cgi_lib/CGILite.hpp"


using namespace std;

int main(int argc , char *argv[]){
	char *env_req_method;	// "REQUEST_METHOD"  -> GET / POST
	char *env_query_str;		//"QUERY_STRING"
	char *env_cont_type;		//"CONTENT_TYPE"
	char *env_cont_lng;		//"CONTENT_LENGTH"
	//char *env_cookie;		//"HTTP_COOKIE"
	//char *env_srv_lang;		//"SERVER_LANGUAGE"

	while(1){
		sleep(30);
	}

	printf("Content-Type: text/html\r\n\r\n");
	printf("<!DOCTYPE html>\n");
	printf("<html lang=\"ja\">\n");
	printf("<head>\n");
	printf("<meta charset=\"utf-8\">\n");
	printf("<meta name=\"viewport\" content=\"width=device-width\">\n");
	printf("<title>hello_cpp_cgi</title>\n");
	printf("</head>\n");
	printf("<body>\n");
	printf("hello c++<br />\n");
	printf("hello_cpp_cgi excuting<br />\n");
	printf("----- start -----------<br />\n");

	// 環境変数 の値を取得
	if ((env_req_method = getenv("REQUEST_METHOD")) != NULL) {
		printf("REQUEST_METHOD=%s<br />\n",env_req_method);
		//cout << "REQUEST_METHOD=" << env_req_method;

		if(strcmp(env_req_method,"GET") == 0 ){	// GET method
			 /* 環境変数 REQUEST_METHOD の値を取得 */
			if ((env_query_str = getenv("QUERY_STRING")) != NULL) {
				printf("QUERY_STRING=%s<br />\n",env_query_str);
			}
		}
	}
	if ((env_cont_type = getenv("CONTENT_TYPE")) != NULL) {
		printf("CONTENT_TYPE=%s<br />\n",env_cont_type);
	}
	if ((env_cont_lng = getenv("CONTENT_LENGTH")) != NULL) {
		printf("CONTENT_LENGTH=%s<br />\n",env_cont_lng);
	}
	printf("----- end -----------<br />\n");

	printf("</body>\n");
	printf("</html>\n");
	fclose(stdout);
	return 0;
}
