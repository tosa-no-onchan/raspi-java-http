/*
 * httpWriter.c
 *
 *  Created on: 2014/03/26
 *      Author: nishi
 */
#define HTTPWRITER_DEF

#include <pthread.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdbool.h>
#include <time.h>

#include "constDef.h"
#include "tableDef.h"
#include "serverConfig.h"
#include "httpReader.h"
#include "httpWriter.h"
#include "com_lib.h"

/*
 * int httpWriter(HttpReq *httpReq,char *contentType,bool img_f,int f_size,time_t mtm)
 */
int httpWriterOK(HttpReq *httpReq,char *contentType,bool img_f,int f_size,time_t *mtm){
	int rc=0;

	WorkBuff *workBuff=(WorkBuff *)(httpReq->buff);

	//printf("httpWriterOK() :#1 start %d,%d\n",httpReq->seq_i,httpReq->sockfd);


	// 現在に時刻を取得
	time_t ctm;
	ctm=time(NULL);
	ctime_r(&ctm,workBuff->wk);
	char *ctm_s=workBuff->wk;
	rtrim(ctm_s,0x0a);

	//printf("httpWriterOK() :#2 %d,%d ,ctm_s=%s\n",httpReq->seq_i,httpReq->sockfd,ctm_s);


	// ファイルのタイムスタンプを取得
	// Tue, 15 Nov 1994 08:12:31 GMT
	int lng=strlen(workBuff->wk);
	char *mtm_s=&(workBuff->wk[lng+1]);
	ctime_r(mtm,mtm_s);
	rtrim(mtm_s,0x0a);
	// ctime_r() はthread-safe か?

	//printf("httpWriterOK() :#3 %d,%d ,mtm_s=%s\n",httpReq->seq_i,httpReq->sockfd,mtm_s);
	//memdump(workBuff->wk,50);


	if(pthread_mutex_lock(&mutex_mem)!=0){
		printf("httpWriterOK() :#2 pthread_mutex_lock error \n");
		return -1;
	}
	//HTTP/1.1 200 OK\r\n
	//Date: Tue, 06 May 2014 10:49:25 GMT
	//Server: Apache
	//Last-Modified: Mon, 10 Dec 2012 08:04:30 GMT
	//ETag: "140008-11df2-4d07b03ca7f80"
	//Accept-Ranges: bytes
	//Content-Length: 73202
	//Keep-Alive: timeout=5, max=500
	//Connection: Keep-Alive
	//Content-Type: text/html


	// HTTP/1.1 200 OK\r\n
	// Date: "+now+"\r\n"
	// Server: raspberry-pi\r\n
	// Content-Length: nnn\r\n
	// Content-Type: xxxxx\r\n\r\n
	sprintf(workBuff->o_buff, "HTTP/1.1 200 OK\r\n" \
			"Date: %s" \
			"Server: raspberry-pi\r\n" \
			"Last-Modified: %s\r\n" \
			"Accept-Ranges: bytes\r\n" \
			"Content-Length: %d\r\n" \
			"Keep-Alive: timeout=5, max=500\r\n" \
			"Connection: Keep-Alive\r\n" \
			"Content-Type: %s\r\n\r\n" \
			,ctm_s,mtm_s,f_size,contentType);

	if(pthread_mutex_unlock(&mutex_mem)!=0){
		printf("httpWriterOK() :#3 pthread_mutex_unlock error\n");
		return -1;
	}
	size_t sl=strlen(workBuff->o_buff);
	if(write(httpReq->sockfd,workBuff->o_buff,sl) != sl){
		printf("httpWriter() :#4 error %d,%d\n",httpReq->seq_i,httpReq->sockfd);
		rc=-1;
	}
	//printf("httpWriterOK() :#90  end %d,%d lng=%d\n",httpReq->seq_i,httpReq->sockfd,strlen(workBuff->o_buff));
	return rc;
}

/*
 * int httpWriter(HttpReq *httpReq,char *contentType,bool img_f,int f_size,,time_t tm)
 */
int httpWriterOK_old(HttpReq *httpReq,char *contentType,bool img_f,int f_size,time_t tm){
	int rc=0;

	printf("httpWriter() :#1  HTTP OK put %d,%d\n",httpReq->seq_i,httpReq->sockfd);

	WorkBuff *workBuff=(WorkBuff *)(httpReq->buff);

	// HTTP/1.1 200 OK\r\n
	strcpy(workBuff->o_buff, http_ok);
	// Date: "+now+"\r\n"

	// Server: raspberry-pi\r\n
	strcat(workBuff->o_buff, http_server);

	if(img_f == true){
		// Accept-Ranges: bytes\r\n
		strcat(workBuff->o_buff, http_range);
		// Connection: Keep-Alive\r\n
		strcat(workBuff->o_buff, http_keep_alive);
	}
	// Content-Length: nnn\r\n
	if(f_size>0){
		sprintf(workBuff->wk,"Content-Length: %d\r\n",f_size);
		strcat(workBuff->o_buff, workBuff->wk);
	}
	// Content-Type: xxxxx\r\n\r\n
	if(contentType != NULL){
		sprintf(workBuff->wk,"Content-Type: %s\r\n\r\n",contentType);
		strcat(workBuff->o_buff, workBuff->wk);
	}
	if(write(httpReq->sockfd,workBuff->o_buff,strlen(workBuff->o_buff)) <=0){
		printf("httpWriter() :#3 error %d,%d\n",httpReq->seq_i,httpReq->sockfd);
		rc=-1;
	}
	printf("httpWriter() :#90  end %d,%d lng=%d\n",httpReq->seq_i,httpReq->sockfd,strlen(workBuff->o_buff));
	return rc;
}
/*
 * int httpWriterOKcgi(HttpReq *httpReq)
 */
int httpWriterOKcgi(HttpReq *httpReq){
	int rc=0;
	printf("httpWriterOKcgi() :#1  HTTP OK put %d,%d\n",httpReq->seq_i,httpReq->sockfd);

	char o_buff[256];
	char wk[128];

	// HTTP/1.1 200 OK\r\n
	strcpy(o_buff, http_ok);
	//Date: "+now+"\r\n";

	// Server: raspberry-pi\r\n
	strcat(o_buff, http_server);

	//Content-type: text/html\r\n\r\n" // 注）最後は \r\nが２個必要

	if(write(httpReq->sockfd,o_buff,strlen(o_buff)) <=0){
		printf("httpWriter() :#3 error %d,%d\n",httpReq->seq_i,httpReq->sockfd);
		rc=-1;
	}
	return rc;
}
