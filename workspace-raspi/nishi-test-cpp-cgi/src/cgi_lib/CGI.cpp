/*
 * CGI.cpp
 *
 *  Created on: 2015/11/01
 *      Author: nishi
 */

#include "CGI.hpp"

#include "Mylog.hpp"

using namespace std;
using namespace tr1;


CGI::CGI() {
	err=true;
	debug=false;
	// TODO 自動生成されたコンストラクター・スタブ
	env_req_method="";	// "REQUEST_METHOD"  -> GET / POST
	env_query_str="";	//"QUERY_STRING"
	env_cont_type="";	//"CONTENT_TYPE"
	env_cont_lng="";		//"CONTENT_LENGTH"
	env_cookie="";		//"HTTP_COOKIE"
	env_srv_lang="";		//"SERVER_LANGUAGE"

	parm="";
	//p_data="";

	buf = new unsigned char [1024];

	Defs_tmp_dir ="/tmp/ras-pi-upload";	// file up-load working dir

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

CGI::~CGI() {
	// TODO Auto-generated destructor stub
	//delete[] buf;
}

/*
 * in()
 */
void CGI::in(){
	if(env_req_method=="GET"){
		get_req();
	}
	else if(env_req_method=="POST"){
		HttpInputStreamCGI hin;		// POST req
		post_req(hin);
		//parm=p_data;
		setParms();

	}
}
/*
 * get_req()
 */
void CGI::get_req(){
	parm=env_query_str;
	setParms();
}

/*
 *  post_req(HttpInputStreamCGI hin_r)
 */
void CGI::post_req(HttpInputStreamCGI hin_r){

	int n=0;
	string s;
	string p_line="";

	string content_type;
	bool multipart_f=false;

	vector<string> spl_d;

	if(debug)mlog.putb("CGI::post_req() :#1 read POST data start!");

	if(env_cont_type != ""){
		if(env_cont_type.substr(0,19) =="multipart/form-data"){
			multipart_f=true;
			spl_d = comLib.split(env_cont_type,' ');
			if(spl_d.size() > 1){
				if(debug)mlog.putb("CGI::post_req() :#2 spl_d[1]="+spl_d[1]);
				boundary = spl_d[1].substr(9);
				boundary1 = "--"+boundary;
				boundary2 = boundary1+"--";
				if(debug)mlog.putb("boundary="+boundary+",length="+comLib.ItoStr(boundary.length()));
				if(debug)mlog.putb("boundary1="+boundary1+",length="+comLib.ItoStr(boundary1.length()));
				if(debug)mlog.putb("boundary2="+boundary2+",length="+comLib.ItoStr(boundary2.length()));
			}
		}
	}

	//try{
	{
		//----- start
		if(debug)mlog.putb("CGI::post_req :#2 read POST data start!");
		n=0;
		ph=0;	// phase
				// 0 -> non muliti
				// 1 -> boundary wait
				// 2 --> Content-Disposition wait
		//din_r.debug=true;
		// read POST data lines
		string p_name="";
		string p_filename="";
		string p_val="";
		p_line="";
		if(multipart_f){
			ph=1;
		}
		while (hin_r.ready() == true && n < 20000){
			if(debug)mlog.putb("CGI::post_req :#3 passed!");
			p_line=hin_r.readline();
			// mlog.put(DEBG,"CGI::post_req() :#5 ph="+ph+",p_line="+p_line);
			if(debug)mlog.putb("CGI::post_req() :#5 ph="+comLib.ItoStr(ph)+",p_line="+p_line);
			switch(ph){
			case 0:	// non muliti part
				parm+=p_line;
				break;
			case 1:	// muliti part waiting boundary
				if(p_line == boundary1){
					ph=2;
					//mlog.put(DEBG,"HttpReaderCGI::read_din() :#6 boundary1 come >"+p_line);
				}
				else if(p_line== boundary2){
					//mlog.put(DEBG,"HttpReaderCGI::read_din() :#7 boundary2 come >"+p_line);
				}
				break;
			case 2:	// muliti part waiting Content-Disposition:
				// Content-Disposition: form-data; name="tno"  --> ph=3
				// Content-Disposition: form-data; name="simg"; filename=""  --> ph=10
				if(p_line.substr(0,20)== "Content-Disposition:"){
					s=p_line.substr(p_line.find("name=")+6);
					p_name=s.substr(0,s.find("\""));
					//mlog.put(DEBG,"HttpReaderCGI::read_din() :#8 p_name="+p_name);
					// filename= が続いていますか?
					if(p_line.find("filename=") != string::npos){
						s=p_line.substr(p_line.find("filename=")+10);
						p_filename=s.substr(0,s.find("\""));
						//mlog.put(DEBG,"HttpReaderCGI::read_din() :#9 p_filename="+p_filename);
						if(parm.length()==0){
							parm+=p_name+"="+p_filename;
						}
						else{
							parm+="&"+p_name+"="+p_filename;
						}
						// アップロードファイル名を伝達するためのパラメータ名を作成
						upload_parm_name=p_name+"_"+p_filename;
						//mlog.put(DEBG,"HttpReaderCGI::read_din() :#10 upload_parm_name="+upload_parm_name);
						ph=10;
					}
					else{
						ph=3;
					}
				}
				break;
			case 3:	// muliti part pass one line
				ph=4;
				break;
			case 4:	// muliti part get value
				p_val=p_line;
				//mlog.put(DEBG,"HttpReaderCGI::read_din() :#11 ph="+ph+",p_val="+p_val);
				if(parm.length()==0){
					parm+=p_name+"="+p_val;
				}
				else{
					parm+="&"+p_name+"="+p_val;
				}
				//ph=1;	// ここは、変 2015.2.18
				ph=5;	// 2015.22.18
				break;
			case 5:	// muliti part get next values 2015.2.18
				if(p_line == boundary1){
					ph=2;
					//mlog.put(DEBG,"HttpReaderCGI::read_din() :#12 boundary1 come >"+p_line);
				}
				else if(p_line == boundary2){
					ph=1;
					//mlog.put(DEBG,"HttpReaderCGI::read_din() :#13 boundary2 come >"+p_line);
				}
				else{
					p_val=p_line;
					//mlog.put(DEBG,"HttpReaderCGI::read_din() :#14 ph="+ph+",p_val="+p_val);
					//p_data+=0x0d+0x0a+p_val;
					parm+="\r\n"+p_val;
				}
				break;
			case 10:	// waiting for "Content-Type: application/octet-stream" line -> ph=11
						// waiting for "Content-Type: text/plain" line -> ph=11
						// or
						// waiting for "Content-Type: image/gif" line -> ph=12
						// waiting for "Content-Type: image/jpg" line -> ph=12
				//mlog.put(DEBG,"HttpReaderCGI::read_din() :#15 ph="+ph+",p_line="+p_line);
				if(p_line.find("application/octet-stream") != string::npos){
					ph=11;
				}
				else if(p_line.find("text/plain") != string::npos){
					ph=11;
				}
				// else if(p_line.indexOf("image/") >=0){
				else if(p_line.find("image/") != string::npos){
					ph=12;
				}
				else if(p_line == boundary1 || p_line == boundary2){
					ph=2;
				}
				break;
			case 11: // reading octet-stream , text/plain
				if(p_line == boundary1 || p_line == boundary2){
					ph=2;
				}
				break;
			case 12: // image/xxx reading
				//mlog.put(DEBG,"HttpReaderCGI::read_din() :#16 ph="+ph+",p_line="+p_line);
				// 1 brank line come
				if(p_line.length() == 0){
					//mlog.put(DEBG,"HttpReaderCGI::read_din() :#17 passed !!");
					// img ファイル受け取り
					p_val=upload_img(hin_r);
					if(parm.length()==0){
						parm+=upload_parm_name+"="+p_val;
					}
					else{
						parm+="&"+upload_parm_name+"="+p_val;
					}
				}
				break;
			}
			n++;
		}
	}
	//catch(IOException e){
	//	mlog.put(DEBG,"HttpReaderCGI::read_din() :#90 error ="+e);
	//}
	if(debug)mlog.putb("CGI::post_req() :#99 end!");

}

/*
 *  setParms()
 */
void CGI::setParms(){
	//tr1::unordered_map<const char*, int> M;
	//std::tr1::unordered_map <string, int> map;
	//unordered_map <string, int> map;
	//map["a"] = 2;
	//cout << map["a"] << endl;

	vector<string> parm_l = comLib.split(parm,'&');

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
}
/*
 * img ファイルの受け取りを行います。
 */
string CGI::upload_img(HttpInputStreamCGI hin_r){
	bool end_f=false;
	bool err_flg=false;
	int data_l=0;
	int j=0;
	size_t l;
	unsigned char buff[1024];

	if(debug)mlog.putb("CGI::upload_img() :#1 file upload start");


//	byte[] b_boundary1 = ("\r\n"+boundary1+"\r\n").getBytes();	// 先頭の \r\n は、バイナリーデータの終端です
	string s;
	s="\r\n"+boundary1+"\r\n";
	unsigned char b_boundary1[s.size()];
	for(l=0;l < s.size();l++){
		b_boundary1[l]= (unsigned char)s[l];	// 先頭の \r\n は、バイナリーデータの終端です
	}
//	byte[] b_boundary2 = ("\r\n"+boundary2+"\r\n").getBytes();	// 先頭の \r\n は、バイナリーデータの終端です
	s="\r\n"+boundary2+"\r\n";
	unsigned char b_boundary2[s.size()];
	for(l=0;l < s.size();l++){
		b_boundary2[l]= (unsigned char)s[l];	// 先頭の \r\n は、バイナリーデータの終端です
	}

	string tmp_f=make_upload_fname();

	// temp ディレクトリーのチェック&確保
	struct stat sb;
	if(stat(Defs_tmp_dir.c_str(),&sb)!=0){
		s="mkdir -p "+Defs_tmp_dir;
		if(debug)mlog.putb("CGI::upload_img() :#2 s="+s);
		system(s.c_str());
		if(stat(Defs_tmp_dir.c_str(),&sb)!=0){
			// error
			if(err)mlog.putb("CGI::upload_img() :#3 directory creating error!");
			err_flg=true;
		}
	}


	// File tmp_file=new File(Defs_tmp_dir+"/"+tmp_f);
	// OutputStream out = new FileOutputStream(tmp_file);

	s=Defs_tmp_dir+"/"+tmp_f;

	if(debug)mlog.putb("CGI::upload_img() :#4 s="+s);


	FILE *out_f;
	out_f= fopen(s.c_str(),"wb");
	if(out_f == NULL){
		if(err)mlog.putb("CGI::upload_img() :#4 create file error file-path="+s);
		err_flg=true;
	}

	//din_r.debug2=true;
	while(hin_r.ready() == true && j < 10000 && end_f==false){	// reading img loop
		//if(debug)mlog.putb("HttpReaderCGI::upload_img() :#3 j="+j);
		data_l=hin_r.readToBoundary(buff, 0, sizeof(buff), b_boundary1,b_boundary2);
		if(err_flg==false && data_l > 0){
			fwrite(buff,data_l,1,out_f);
		}
		//バウンダリーが来ました
		if(hin_r.check_Boundary_no()>0){
			break;
		}
		j++;
	}
	if(out_f!=NULL){
		fclose(out_f);
	}
	//tmp_file.setWritable(true,true);
	if(hin_r.check_Boundary_no() >0){
		ph=2;
	}
	else{
		ph=1;
	}
	//if(debug)mlog.putb("HttpReaderCGI::upload_img() :#10 file upload end");
	return tmp_f;
}
/*
 * アップロードファイル用の tmp名称を作成します。
 */
string CGI::make_upload_fname(){
	string temp_name;
	time_t cur_t;

	char buffer[30];
	char buffer2[30];

	struct timeval tv;

	gettimeofday(&tv,NULL);

	cur_t=tv.tv_sec;	// Seconds
	long msec = tv.tv_usec / 1000L;

	struct tm *now = localtime(&cur_t);

	strftime(buffer,30,"%y_%m_%d-%H_%M_%S",now);
	sprintf(buffer2,"%s_%ld",buffer,msec);
	temp_name="tmp-"+string(buffer2);


	// long currentTimeMillis = System.currentTimeMillis();
	// Date date = new Date(currentTimeMillis);
	// SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddHHmmss-SSS");
	// temp_name="tmp-"+simpleDateFormat.format(date);
	return temp_name;
}

