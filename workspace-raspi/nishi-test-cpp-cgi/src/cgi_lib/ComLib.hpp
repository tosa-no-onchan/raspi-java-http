/*
 * com_lib.hpp
 *
 *  Created on: 2015/10/31
 *      Author: nishi
 */

#ifndef USR_CGI_LIB_COMLIB_HPP_
#define USR_CGI_LIB_COMLIB_HPP_

#include <iostream>
#include <vector>
#include <string>
#include <sstream>

using namespace std;

class ComLib {
public:
	ComLib();
	virtual ~ComLib();
	vector<string> split(string str, char delimiter);

	int toInt(string s);
	string ItoStr(int x);
	string rtrim(string x);
	string ltrim(string x);
};

#endif /* USR_CGI_LIB_COMLIB_HPP_ */
