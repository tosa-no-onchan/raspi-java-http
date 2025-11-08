/*
 * Mylog.cpp
 *
 *  Created on: 2015/11/03
 *      Author: nishi
 */

#include "Mylog.hpp"

using namespace std;

Mylog::Mylog() {
	// TODO 自動生成されたコンストラクター・スタブ
	logf="./log.txt";
}

Mylog::~Mylog() {
	// TODO Auto-generated destructor stub
}

void Mylog::put(bool flg,string msg){
	FILE *fp;
	//char *s = new char[msg.size()+1];
	//s = msg.c_str();
	if(flg==true){
		fp=fopen(logf.c_str(),"a");
		if(fp!=NULL){
			fprintf(fp,"%s\n",msg.c_str());
			fclose(fp);
		}
	}
}
void Mylog::putb(string msg){
	FILE *fp;
	//char *s = new char[msg.size()+1];
	//s = msg.c_str();
	fp=fopen(logf.c_str(),"a");
	if(fp!=NULL){
		fprintf(fp,"%s\n",msg.c_str());
		fclose(fp);
	}
}
