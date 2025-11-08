/*
 * hello2_cpp_cgi.cpp
 *
 *  Created on: 2015/10/31
 *      Author: nishi
 */
#include <stdio.h>      // printf
#include <stdlib.h>     // getenv
#include <iostream>	// cout
//#include <string>
#include <string.h>

#include "cgi_lib/CGI.hpp"
//#include "cgi_lib/Mylog.hpp"

using namespace std;
using namespace tr1;


int main(int argc , char *argv[]){

	CGI _cgi;
	//Mylog mylog;
	//bool debug=false;

	cout <<"Content-Type: text/html\r\n\r\n"<<endl;
	cout <<"<!DOCTYPE html>\n"
		<< "<html lang=\"ja\">\n"
		<< "<head>\n"
		<< "<meta charset=\"utf-8\">\n"
		<< "<meta name=\"viewport\" content=\"width=device-width\">\n"
		<< "<title>hello2_cpp_cgi</title>\n"
		<< "</head>\n"
		<< "<body>\n"
		<< "hello2 c++<br />\n"
		<< "hello2_cpp_cgi excuting<br />\n"
		<< "------ start ----------<br />\n"<<endl;

	_cgi.in();
	// ?xxx=xxx&yyy=xxx を表示します。
	//unordered_map<Key,T>::iterator itr;
	unordered_map<string,string>::iterator itr;
	for(itr=_cgi.parms.begin();itr!=_cgi.parms.end();++itr){
		cout << itr->first <<"=" << itr->second << "<br />\n";
	}
	cout <<"------ end ----------<br />\n"
		<< "</body>\n"
		<< "</html>\n"<<endl;
	fclose(stdout);
	//if(debug)mylog.putb("hello2_cpp_cgi:#99 end");
	return 0;
}




