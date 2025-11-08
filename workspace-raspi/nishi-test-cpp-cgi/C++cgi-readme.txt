Welcom to Raspberry C++ cgi program.(C++cgi-readme.txt)

This program is written by C++ language for Raspberry Pi light http server Version-1.3.1.
You can execute this C++ cgi on Raspberry Pi light http server Version-1.3.1.

How to make
1] Download , copy and spread zip file.
  dowunload nishi-test-cpp-cgi.xxx.zip and unzuip to /home/your-id/workspace-raspi/
  $cd /home/your-id/workspace-raspi/
  unzip nishi-test-cpp-cgi.xxx.zip

  1) Making libcgi_lib.a
    $cd nishi-test-cpp-cgi/src/cgi_lib
    $make clean
    $make

  2) Making C++ CGI samples
    $cd nishi-test-cpp-cgi/
    $sh ./make_c++.sh hello_cpp_cgi    -->  src/hello_cpp_cgi.cpp compile
    $sh ./make_c++.sh hello1_cpp_cgi
    $sh ./make_c++.sh hello2_cpp_cgi
    
    Attention.
    If you want to run these C++ CGI samples under an apache,
    please deploy them to the cgi-bin directory.

2] edit server.conf file (http_pic_t.v1.3.1).
    /home/your-id/www-raspi
                   |-- /html                <--- doc_rootdir
                          |-- /images
                          |  index4.htm  <-- sampe html for C++ cgi access

    /home/your-id/workspace-raspi/nishi-test-cpp-cgi <--- cgi_rootdir (perl cgi and C++ cgi)


3] start http_pic_t server(v1.3.1)
$su -l pi
pi>$cd ~your-id/workspace-raspi/http_pic_t.v1.3.1
pi>$sudo ./httpServer_t

Then you can see index.htm by another pc browser(which is in a same network ex. 192.168.1.X).
http://your-Raspberry-Pi-ip/index4.htm

then you can see ~your-home/www-raspi/html/index4.htm index-file by your pc browser.  



4] C++ cgi program sample.

  src/hello1_cpp_cgi.cpp

  Making a C++ CGI program is very easy.
  It only uses CGILite.cpp or CGI.cpp class to accept a GET or POST http request.

---- start C++ code ----
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

---- end C++ code ----

Attention.
CGI.cpp and CGILite.cpp are built in basic function only.
Please custmize then as you like.
CGI.cpp dose not have a chekking of limit post-data-length
(like a muliti-part requests of file-upload).
Espesailly, CGILite.cpp dose not recieve muliti-part requests(like a file-upload).

