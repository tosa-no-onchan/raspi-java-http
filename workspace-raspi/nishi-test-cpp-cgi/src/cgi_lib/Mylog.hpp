/*
 * Mylog.hpp
 *
 *  Created on: 2015/11/03
 *      Author: nishi
 */

#ifndef MYLOG_HPP_
#define MYLOG_HPP_
#include <stdio.h>      // printf
#include <stdlib.h>     // getenv
#include <string>

using namespace std;

class Mylog {
private:
	string logf;
public:
	Mylog();
	virtual ~Mylog();
	void put(bool flg,string msg);
	void putb(string msg);
};

#endif /* MYLOG_HPP_ */
