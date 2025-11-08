/*
 * UtilLib.hpp
 *
 *  Created on: 2015/11/01
 *      Author: nishi
 */

#ifndef USR_CGI_LIB_UTILLIB_HPP_
#define USR_CGI_LIB_UTILLIB_HPP_

using namespace std;

class UtilLib {
public:
	UtilLib();
	virtual ~UtilLib();
	bool compByteArry(unsigned char *a,int offs,unsigned char *b,int lng);
	// static string StringtoHex(string in);
	// static string BytetoHex(unsigned char *b,int off,int lg);
	// void printByte(unsigned char *buf,int off,int lg);
};


#endif /* USR_CGI_LIB_UTILLIB_HPP_ */
