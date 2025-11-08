/*
 * UtilLib.cpp
 *
 *  Created on: 2015/11/01
 *      Author: nishi
 */

#include "UtilLib.hpp"

using namespace std;

UtilLib::UtilLib() {
	// TODO 自動生成されたコンストラクター・スタブ

}

UtilLib::~UtilLib() {
	// TODO Auto-generated destructor stub
}

/*
 * バイト配列の比較
 */
bool UtilLib::compByteArry(unsigned char *a,int offs,unsigned char *b,int lng){
	bool rc=true;
	for(int i=0;i<lng;i++){
		if(a[offs+i]!=b[i]){
			rc=false;
			break;
		}
	}
	return rc;
}

