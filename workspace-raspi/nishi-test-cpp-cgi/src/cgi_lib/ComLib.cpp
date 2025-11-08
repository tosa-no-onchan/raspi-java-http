/*
 * com_lib.cpp
 *
 *  Created on: 2015/10/31
 *      Author: nishi
 */

#include "ComLib.hpp"

using namespace std;

ComLib::ComLib() {
	// TODO 自動生成されたコンストラクター・スタブ

}

ComLib::~ComLib() {
	// TODO Auto-generated destructor stub
}

vector<string> ComLib::split(string str, char delimiter) {
  vector<string> internal;
  stringstream ss(str); // Turn the string into a stream.
  string tok;

  while(getline(ss, tok, delimiter)) {
    internal.push_back(tok);
  }

  return internal;
}


int ComLib::toInt(string s) {
	 int v; std::istringstream sin(s);
	 sin>>v;
	 return v;
}
string ComLib::ItoStr(int x){
	stringstream sout;
	sout<<x;
	return sout.str();
}

// rtrim
string ComLib::rtrim(string x){
	string t = " \t\n\r\f\v";
	x.erase(x.find_last_not_of(t.c_str()) + 1);
	return x;
}

// ltrim
string ComLib::ltrim(string x){
	string t = " \t\n\r\f\v";
    x.erase(0, x.find_first_not_of(t.c_str()));
    return x;
}
