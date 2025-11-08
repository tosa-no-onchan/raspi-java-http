/*
 * contextProcessor.c
 * Version 1.1
 * Update on: 2014/03/26
 *      Author: nishi
 */
#include <pthread.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
//#include <sys/socket.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
//#include <netinet/in.h>
//#include <signal.h>
#include <stdbool.h>

#include "constDef.h"
#include "tableDef.h"
#include "serverConfig.h"
#include "httpReader.h"
#include "httpWriter.h"
#include "com_lib.h"

void contextProcessor_nothing(HttpReq *httpReq){

	char hd_s[]="HTTP/1.1 404 File Not Found\r\n";
		//+"Date: "+now+"\r\n"
	char hd_s2[]="Server: HTTP 1.1\r\n";
	char hd_s3[]="Content-type: text/html\r\n\r\n";	// 注）最後は \r\nが２個必要

	write(httpReq->sockfd,hd_s,strlen(hd_s));
	write(httpReq->sockfd,hd_s2,strlen(hd_s3));
	write(httpReq->sockfd,hd_s3,strlen(hd_s3));

	char hd_s4[]="<HTML>\r\n";
	char hd_s5[]="<HEAD><TITLE>File Not Found</TITLE></HEAD>\r\n";
	char hd_s6[]="<BODY>\r\n";
	char hd_s7[]="<H1>HTTP Error 404: File Not Found</H1>\r\n";
	char hd_s8[]="<BODY>\r\n";
	write(httpReq->sockfd,hd_s4,strlen(hd_s4));
	write(httpReq->sockfd,hd_s5,strlen(hd_s5));
	write(httpReq->sockfd,hd_s6,strlen(hd_s6));
	write(httpReq->sockfd,hd_s7,strlen(hd_s7));
	write(httpReq->sockfd,hd_s8,strlen(hd_s8));
	//close(httpReq->sockfd);
}
/*
 * void put_file_bin(HttpReq *httpReq,char *file_ph,int o_fd)
 * 実際のコンテンツの出力
 */
void put_file_bin(HttpReq *httpReq,char *file_ph,int o_fd){
	int i_fd;
	int r_lng;
	i_fd=open(file_ph,O_RDONLY);
	//printf("contextProcessor::put_file_bin() : #1 i_fd=%d\n",i_fd);
	if(i_fd != -1){
		while(1){
			r_lng=read(i_fd,httpReq->buff,sizeof(httpReq->buff));
			if(r_lng>0){
				if(write(o_fd,httpReq->buff,r_lng) != r_lng){
					printf("contextProcessor::put_file_bin():#3 o_fd=%d write error\n",o_fd);
					break;
				}
				//最後 +1 の読み込みをなくしてみます。2014.3.30
				if(r_lng < sizeof(httpReq->buff)){
					break;
				}
			}
			else if(r_lng==0){
				break;
			}
			else{
				printf("contextProcessor::put_file_bin() : #4 read error\n");
				break;
			}
		}
		close(i_fd);
	}
	else{
		printf("contextProcessor::put_file_bin():#10 open error\n");
	}
}
/*
 * contextProcessor(HttpReq *httpReq,char *index_name)
 * コンテキストプロセッサー
 * html,imgの処理を行います
 */
void contextProcessor(HttpReq *httpReq,char *index_name){
	StrBuff *strBuff;
	char *contentType;	// const use
	char *version;	// const use

	char *m_indexname;	// rq_malloc use
	char *m_file_s;		// rq_malloc use
	char *m_file_s2;	// rq_malloc use
	char *m_dir;		// rq_malloc use

	struct stat  st;
	bool img_f;
	char *hd_s;

	char f_size_s[20];
	int rc;

	strBuff=&(httpReq->sbuff);

	//printf(">contextProcessor():#1 called %d,%d\n",httpReq->seq_i,httpReq->sockfd);

	//printf("contextProcessor():#1 index_name=%s\n",index_name);

	m_dir=rq_strcat(strBuff,defs.doc_rootDir,"/");
	m_file_s=rq_strcat(strBuff,m_dir,index_name);
	//ファイル無し
	if (stat(m_file_s, &st) != 0){
		m_indexname=rq_strcpy(strBuff,index_name);
		//printf("contextProcessor():#1 passed!\n");
	}
	else{
		//printf("contextProcessor():#2 m_file_s=%s\n",m_file_s);
		// ディレクトリーです
		if(S_ISDIR(st.st_mode) == 1){
			// 最後が "/" です
			if(str_endsWith(index_name, "/") == 0){
				//printf("contextProcessor():#3 passed!\n");
				strcpy(httpReq->buff,m_file_s);
				strcat(httpReq->buff,"index.html");
				//ファイル無し
				if (stat(httpReq->buff, &st) != 0){
					m_indexname=rq_strcat(strBuff,index_name,"index.htm");
				}
				else{
					m_indexname=rq_strcat(strBuff,index_name,"index.html");
				}
			}
			else{
				//printf("contextProcessor():#4 passed!\n");
				strcpy(httpReq->buff,m_file_s);
				strcat(httpReq->buff,"/index.html");
				//ファイル無し
				if (stat(httpReq->buff, &st) != 0){
					m_indexname=rq_strcat(strBuff,index_name,"/index.htm");
				}
				else{
					m_indexname=rq_strcat(strBuff,index_name,"/index.html");
				}
			}
		}
		else{
			//printf("contextProcessor():#5 passed!\n");
			m_indexname=rq_strcpy(strBuff,index_name);
		}
	}

	contentType = "text/html";
	version=httpReq->version;

	m_file_s2=rq_strcat(strBuff,m_dir,m_indexname);
#ifndef _SILENT
	//printf("contextProcessor() :#4 %d,file_s=%s\n",httpReq->seq_i,m_file_s2);
	if(str_endsWith(m_indexname,".html") == 0 || str_endsWith(m_indexname,".htm") == 0 ||
			str_endsWith(m_indexname,".css") == 0){
		printf("contextProcessor() :#4 %d,%d,file_s=%s\n",httpReq->seq_i,httpReq->sockfd,index_name);
	}
	else{
		printf("contextProcessor() :#4 %d,%d ..\n",httpReq->seq_i,httpReq->sockfd);
	}
#endif
	//ファイル無し
	if (stat(m_file_s2, &st) != 0){
#ifndef _SILENT
		printf("contextProcessor() :#5 file nothing file_s=%s\n",m_file_s2);
#endif
		contextProcessor_nothing(httpReq);
	}
	//ファイル有
	else{
		if(str_endsWith(m_indexname,".gif") == 0){
			contentType = "image/gif";
			img_f=true;
		}
		else if(str_endsWith(m_indexname,".jpg")==0){
			contentType = "image/jpg";
			img_f=true;
		}
		else if(str_endsWith(m_indexname,".css")==0){
			contentType = "text/css";
		}
		else if(str_endsWith(m_indexname,".js")==0){
			contentType = "text/javascript";
		}
		else if(str_endsWith(m_indexname,".png")==0){
			contentType = "image/png";
			img_f=true;
		}
		rc=0;
		if(str_startsWith(version,"HTTP/")==0){	//MIME header read
			// PUT Http Header OK
			rc=httpWriterOK(httpReq,contentType,img_f,st.st_size,&st.st_mtime);
		}
		if(rc==0){
			//printf("contextProcessor() :#10  call put_file_bin %d,%d\n",httpReq->seq_i,httpReq->sockfd);
			put_file_bin(httpReq,m_file_s2,httpReq->sockfd);
		}
	}
#ifndef _SILENT
	printf("<contextProcessor() :#90 end %d,%d\n",httpReq->seq_i,httpReq->sockfd);
#endif
}

