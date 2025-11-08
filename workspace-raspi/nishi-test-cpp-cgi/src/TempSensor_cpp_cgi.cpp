/*
 * TempSensor_cpp_cgi.cpp
 *
 *  Created on: 2015/11/17
 *      Author: nishi
 */

#include "TempSensor_cpp_cgi.h"

using namespace std;


TempSensor_cpp_cgi::TempSensor_cpp_cgi() {
	// TODO 自動生成されたコンストラクター・スタブ

}

TempSensor_cpp_cgi::~TempSensor_cpp_cgi() {
	// TODO Auto-generated destructor stub
}

void TempSensor_cpp_cgi::start(){
	//CGILite _cgi;

	cout << "Content-Type: text/html\r\n\r\n"<<endl;

	cout << "<!DOCTYPE html>\n"
		<< "<html lang=\"ja\">\n"
		<< "<head>\n"
		<< "<meta charset=\"utf-8\">\n"
		<< "<meta name=\"viewport\" content=\"width=device-width\">\n"
		<< "<title>hello2_cpp_cgi</title>\n"
		<< "</head>\n"
		<< "<body>\n"
		<< "hello2 c++<br />\n"
		<< "hello2_cpp_cgi excuting<br />\n"
		<< "------ start ----------<br />\n" << endl;

	//_cgi.in();
	check_temp();

	cout << "------ end ----------<br />\n"
		<< "</body>\n"
		<< "</html>\n"<< endl;
}

void TempSensor_cpp_cgi::check_temp(){
	string sys_bus="/sys/bus/w1/devices";

	//CommandExec cmde = new CommandExec();
	//string cmd_s[] = {"ls",sys_bus};

    const int BUFSIZE = 300;
    char buf[ BUFSIZE+1 ];

    FILE * f = popen( "ls /sys/bus/w1/devices", "r" );
    if ( f == 0 ) {
		cout << "execute error<br />\n" << endl;
    	return;
    }

	//int rc;
	//rc = cmde.command_exec(cmd_s);
	//cout << "TempSensor_cgi::check_temp() : #1 rc=" << rc << endl;

	cout << "execute ok<br />\n" << endl;
	vector<string> result;
    while( fgets(buf, BUFSIZE, f) ) {
    	result.push_back(buf);
    	cout << buf << "<br />\n" << endl;
    }
    pclose( f );

	string x = comLib.rtrim(result[0]);

	//string cmd_s2[] = {"cat",sys_bus+"/"+cmde.result.get(0)+"/w1_slave"};

	string cmd_s2= "cat "+sys_bus+"/"+x+"/w1_slave";
    FILE * f2 = popen(cmd_s2.c_str(), "r" );
    if ( f2 == 0 ) {
		cout << "execute2 error<br />\n" << endl;
    	return;
    }
	vector<string> result2;
    while( fgets(buf, BUFSIZE, f2) ) {
    	result2.push_back(buf);
    	cout << buf << "<br />\n" << endl;
    }
    pclose(f2);

	string temp_line=comLib.rtrim(result2[1]);
	//cout << "temp_line="<<temp_line<<"<br />\n"<<endl;

	size_t p=temp_line.find("t=");
	string temp_val = temp_line.substr(p+2);
	//cout << "temp_val="<<temp_val<<"+++<br />\n"<<endl;

	float temp_f = atof(temp_val.c_str()) / 1000.0;
	cout << "Now temp is " << temp_f << " C<br />\n"<<endl;

	//for(int i=0;i<cmde.result.size();i++){
	//	string s = cmde.result.get(i);
	//	cout << s << "<br />\n" << endl;
	//}
	//string temp_line=cmde.result.get(1);
	//int i=temp_line.indexOf("t=");
	//if(i > 0){
	//	string temp=temp_line.substring(i+2);
	//if(temp != null){
	//		float temp_f=(float) (Float.parseFloat(temp)/1000.0);
	//		cout << "Now temp is " << temp_f << " C<br />\n"<<endl;
	//	}
	//}

}

int main(int argc , char *argv[]){
	TempSensor_cpp_cgi _prog;
	_prog.start();

	return 0;

}
