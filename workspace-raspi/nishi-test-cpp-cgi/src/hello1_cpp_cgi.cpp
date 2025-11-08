/*
 * hello1_cpp_cgi.cpp
 *
 *  Created on: 2015/10/31
 *      Author: nishi
 */
#include <stdio.h>      // printf
#include <stdlib.h>     // getenv

#include <iostream>	// cout

//#include <string>
#include <string.h>
#include "cgi_lib/CGILite.hpp"

using namespace std;
using namespace tr1;

int main(int argc , char *argv[]){

	CGILite _cgi;

	cout << "Content-Type: text/html\r\n\r\n"<<endl;
	cout << "<!DOCTYPE html>\n"
		<< "<html lang=\"ja\">\n"
		<< "<head>\n"
		<< "<meta charset=\"utf-8\">\n"
		<< "<meta name=\"viewport\" content=\"width=device-width\">\n"
		<< "<title>hello1_cpp_cgi</title>\n"
		<< "</head>\n"
		<< "<body>\n"
		<< "hello1 c++<br />\n"
		<< "hello1_cpp_cgi excuting<br />\n"
		<< "------ start ----------<br />\n"<<endl;
	_cgi.in();
	// ?xxx=xxx&yyy=xxx を表示します。
	unordered_map<string,string>::iterator itr;
	for(itr=_cgi.parms.begin();itr!=_cgi.parms.end();++itr){
		cout << itr->first <<"=" << itr->second << "<br />\n";
	}
	cout << "------ end ----------<br />\n"
		<< "</body>\n"
		<< "</html>\n"<<endl;
	fclose(stdout);
	return 0;
}




