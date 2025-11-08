/*
 * MyLed_cpp_cgi.cpp
 *
 *  Created on: 2015/11/20
 *      Author: nishi
 */
#include <iostream>
#include <unistd.h>
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include "cgi_lib/GPIOClass.hpp"
using namespace std;

int main() {

	cout << "Content-Type: text/html\r\n\r\n"<<endl;

	cout << "<!DOCTYPE html>\n"
		<< "<html lang=\"ja\">\n"
		<< "<head>\n"
		<< "<meta charset=\"utf-8\">\n"
		<< "<meta name=\"viewport\" content=\"width=device-width\">\n"
		<< "<title>MyLed_cpp_cgi</title>\n"
		<< "</head>\n"
		<< "<body>\n"
		<< "Led Blink c++ cgi<br />\n"
		<< "MyLed_cpp_cgi excuting<br />\n"
		<< "------ start ----------<br />\n" << endl;

	GPIOClass* gpio24 = new GPIOClass("24"); //create new GPIO object to be attached to  GPIO24

	gpio24->export_gpio(); //export GPIO24

	gpio24->setdir_gpio("out"); // GPIO24 set to out

	useconds_t period = 200*1000; // Sleep time in milliseconds
	useconds_t period2 = 100*1000; // Sleep time in milliseconds
	int lct=0;
	while (lct < 10) {
		// HIGH: Set GPIO port ON
    	gpio24->setval_gpio("1"); // turn LED ON

    	//java.lang.Thread.sleep(period);
    	usleep(period);

       // LOW: Set GPIO port OFF
    	gpio24->setval_gpio("0");

    	//java.lang.Thread.sleep(period2);
    	usleep(period2);
    	lct++;
    	cout << "led blink lct=" << lct << "<br />\n"<<endl;
    }
	cout << "------ end ----------<br />\n"
		<< "</body>\n"
		<< "</html>\n"<< endl;

	gpio24->unexport_gpio();

	return 0;
}



