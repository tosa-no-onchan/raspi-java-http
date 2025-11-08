/*
 * CGILite.cpp
 *
 *  Created on: 2015/10/30
 *      Author: nishi
 */

#include "CGILite.hpp"

//#include <ext/hash_map>
//#include <unordered_map>

using namespace std;
using namespace tr1;

//using namespace __gnu_cxx;

CGILite::CGILite() {
	debug=false;
	env_req_method="";	// "REQUEST_METHOD"  -> GET / POST
	env_query_str="";	//"QUERY_STRING"
	env_cont_type="";	//"CONTENT_TYPE"
	env_cont_lng="";		//"CONTENT_LENGTH"
	env_cookie="";		//"HTTP_COOKIE"
	env_srv_lang="";		//"SERVER_LANGUAGE"
	parm="";
	buf = new unsigned char [1024];
	// TODO 自動生成されたコンストラクター・スタブ
	// 環境変数 の値を取得
	if (getenv("REQUEST_METHOD") != NULL) {
		env_req_method = getenv("REQUEST_METHOD");

		if(env_req_method =="GET"){	// GET method
			 /* 環境変数 REQUEST_METHOD の値を取得 */
			if (getenv("QUERY_STRING") != NULL) {
				env_query_str = getenv("QUERY_STRING");
				//printf("QUERY_STRING=%s<br />\n",env_query_str.c_str());
			}
		}
	}
	if (getenv("CONTENT_TYPE") != NULL) {
		env_cont_type = getenv("CONTENT_TYPE");
		//printf("CONTENT_TYPE=%s\n",env_cont_type.c_str());
	}
	if (getenv("CONTENT_LENGTH") != NULL) {
		env_cont_lng = getenv("CONTENT_LENGTH");
		//printf("CONTENT_LENGTH=%s\n",env_cont_lng.c_str());
	}
}

CGILite::~CGILite() {
	// TODO Auto-generated destructor stub
	//delete[] buf;
}

/*
 * in()
 */
void CGILite::in(){
	if(env_req_method=="GET"){
		get_req();
	}
	else if(env_req_method=="POST"){
		HttpInputStreamCGI hin;		// POST req
		post_req(hin);
		setParms();

	}
}
/*
 * get_req()
 */
void CGILite::get_req(){
	parm=env_query_str;
	setParms();
}

/*
 *  post_req(HttpInputStreamCGI hin_r)
 */
void CGILite::post_req(HttpInputStreamCGI hin_r){

	int n=0;
	string p_line="";

	string content_type;
	bool multipart_f=false;
	string boundary;
	string boundary1;
	string boundary2;

	vector<string> spl_d;

	if(debug)mlog.putb("CGILite::post_req() :#1 read POST data start!");

	if(env_cont_type != ""){
		if(env_cont_type.substr(0,19) =="multipart/form-data"){
			multipart_f=true;
			spl_d = comLib.split(env_cont_type,' ');
			if(spl_d.size() > 1){
				boundary = spl_d[1].substr(0,9);
				boundary1 = "--"+boundary;
				boundary2 = boundary1+"--";
				//mlog.put(DEBG,"boundary="+boundary+",length="+boundary.length());
				//mlog.put(DEBG,"boundary1="+boundary1+",length="+boundary1.length());
				//mlog.put(DEBG,"boundary2="+boundary2+",length="+boundary2.length());
			}
		}
	}
	// multipart ではありません
	if(!multipart_f){
		if(debug)mlog.putb("CGILite::post_req() :#2 start read POST data!!");
		//try {
			while (hin_r.ready() == true && n < 20000){
				if(debug)mlog.putb("CGILite::post_req() :#3 call hin_r.readline()");
				p_line=hin_r.readline();
				parm+=p_line;
				n++;
			}
		//}
		//catch (IOException e) {
			// TODO 自動生成された catch ブロック
			//mlog.put(DEBG,"CGILite::post_req() :#90 error ="+e+"<br />");
		//}
			if(debug)mlog.putb("CGILite::post_req() :#4 read end POST data!!");
	}
	else{
		if(debug)mlog.putb("CGILite::post_req() :#10 request is POST with multipart/form-data, stop processing!<br />");
		//int len;
		//try {
			// システム in バッファサイズを取得
			//len = System.in.available();
			//if(len <= 0){
			//	len=2048;
			//}
			//byte[] buf=new byte[len];
			// POST データを cgi の標準入力に転送
			while (hin_r.ready() == true && n < 20000){
				int data_l=hin_r.readBlock(buf,sizeof(buf));	// 注） sizeof は、動的割り当てにも問題ないか?
				if(data_l==0){
					break;
				}
				n++;
			}
		//}
		//catch (IOException e) {
			// TODO 自動生成された catch ブロック
		//	mlog.put(DEBG,"CGILite::post_req() :#91 error ="+e+"<br />");
		//}
	}
}
/*
 *  setParms()
 */
void CGILite::setParms(){
	//tr1::unordered_map<const char*, int> M;
	//std::tr1::unordered_map <string, int> map;
	//unordered_map <string, int> map;
	//map["a"] = 2;
	//cout << map["a"] << endl;

	if(debug)mlog.putb("CGILite::setParms() :#1 start!");

	vector<string> parm_l = comLib.split(parm, '&');

	parms.clear();
	for(unsigned int i=0;i< parm_l.size();i++){
		string parm = parm_l[i];
		vector<string>one_p = comLib.split(parm, '=');
		if(one_p.size()==2){
			parms[one_p[0]]=one_p[1];
		}
		else if(one_p.size()==1){
			parms[one_p[0]]="";
		}
	}
	if(debug)mlog.putb("CGILite::setParms() :#99 end!");
}
