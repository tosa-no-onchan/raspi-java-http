/*
 * tableDef.h
 *
 *  Created on: 2014/03/29
 *      Author: nishi
 */
#include <stdbool.h>
#include <sys/types.h>

#ifndef TABLEDEF_H_
#define TABLEDEF_H_


//#define MAX_THREAD_CNT 6

typedef struct _defs Defs;
struct _defs{
	char *version;	// ="1.6"
	char *update;		// ="2014.2.2";
	int HTTP_PORT;	// =80;	//HTTP Server connection port no
	char *conf_file;	// ="server.conf";
	char *d_conf_file;	// = "/etc/http_pid/server.conf";	// conf_file for deamon
	char *myhost;		// ="www6.net-tosa.cxm";
	char *myhostip;	// ="192.168.1.150";
	char *myuser;	//="nishi";	// nishi / pi-nishi
	int timeout;	// timeout timer[sec]
	int maxConnect;	// =5
	int maxThread_cnt;	//=6;	// Max Tread count
	char *allow_networks;	//="192.168.1.0/255.255.255.0";
	char *doc_rootDir;	// ="/home/nishi/www-raspi/html";
	char *cgi_rootDir;	// ="/home/nishi/www-raspi/cgi-bin";
	char *ccgi_rootDir;	// ="/home/nishi/www-raspi/ccgi-bin";
	char *class_rootDir;	// ="/home/nishi/workspace-raspi/nishi-test/bin";
	char *class_modDir;	// ="/home/nishi/workspace-raspi/nishi-test/bin";
	char *tmp_dir;	// ="/tmp/ras-pi-upload";	// file up-load working dir
	// Activated value
	char *error_log;	//="";
	char *built_in;	//="";		// built-in package name
	char *class_path;	// java class path =".:/home/nishi/workspace-raspi/nishi-test2/bin:/usr/java/default/jre/lib/rt.jar"
};


#define HttpBuff_a_LineSize 256
#define HttpBuff_i_buffSize 512
#define HttpReq_max_header 20
#define HttpReq_buf_Size 4096	// cgi からの受信バッファサイズ
#define HttpReq_str_Size 1024

/*
 * StrBuff
 *  rq_malloc() で使用します
 */
typedef struct _strBuff StrBuff;
struct _strBuff{
	int lng;	// 割り当て済データサイズ
	int mxlmg;	// 割り当て可能データサイズ
	int prev;	//直前の割り当てデータサイズ
	char *str;	//　割り当てデータ領域
};

/*
 * HttpBuff
 */
typedef struct _httpBuff HttpBuff;
struct _httpBuff{
	bool isCr;
	bool isEof;
	char a_line[HttpBuff_a_LineSize+1];	// getLine field
	char i_buff[HttpBuff_i_buffSize];	// read buffer
	int cur_p;	// current i_buff pointer
	int r_lng;	// remain i_buff data lng
};

/*
 * WorkBuff
 */
typedef struct _workBuff WorkBuff;
struct _workBuff{
	char wk[HttpReq_buf_Size/2];
	char o_buff[HttpReq_buf_Size/2];
};

/*
 * HttpReader.h
 * Version 1.1
 * Update on: 2014/03/26
 */
typedef struct _httpReq HttpReq;
struct _httpReq{
	bool act_f;	// thread active flag.  true/false -> act / dead
	int server_sockfd;	// server socket fd or -1 = not use indicate
	int th_no;	// thread No
	int sockfd;	// socket file descripter
	int seq_i;	// sequence no
	int hd_c;	// pool header records count
	time_t timer;	// time over timer
	unsigned char func;	// 0/1/2/3/4 -> "docroot"/"cgi-bin"/"ccgi-bin"/"class-bin"/"class-mod"
	pid_t sub_pid;	// 子プロセスの pid or 0
	int pipe_send;
	int pipe_recv;
	char *req;
	char *method;
	char *fname;
	char *parm;
	char *version;
	char *header[HttpReq_max_header];
	HttpBuff hbuff;	// HttpBuff
	StrBuff sbuff;	// StrBuff
	char sbuff_data[HttpReq_str_Size];	// StrBuff data area
	char buff[HttpReq_buf_Size];	// free buff
};


#endif /* TABLEDEF_H_ */
