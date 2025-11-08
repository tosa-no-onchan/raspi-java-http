/*
 * TempSensor_cpp_cgi.h
 *
 *  Created on: 2015/11/17
 *      Author: nishi
 */

#ifndef TEMPSENSOR_CPP_CGI_H_
#define TEMPSENSOR_CPP_CGI_H_


#include <stdio.h>      // printf
#include <stdlib.h>     // getenv


//#include <iostream>	// cout
//#include <string>

#include <string.h>

//#include "CGILite.hpp"
#include "cgi_lib/ComLib.hpp"

using namespace std;

class TempSensor_cpp_cgi {
private:
	ComLib comLib;

public:
	TempSensor_cpp_cgi();
	virtual ~TempSensor_cpp_cgi();
	void start();
	void check_temp();
};


#endif /* TEMPSENSOR_CPP_CGI_H_ */
