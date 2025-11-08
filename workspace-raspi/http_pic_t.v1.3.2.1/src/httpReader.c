/*
 * httpReader.c
 * Version 1.3.1
 * Update on: 2015/11/07
 *      Author: nishi
 * 注1)	file upload など、データが大きいときに、ファイルが途中で途切れたり、サイズが一致しない場合は。
 * readBlock() の中に、 usleep() の値を大きくして下さい。 2015.11.7 by nishi
 *
 */
#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <stddef.h>
#include <sys/ioctl.h>
#include <time.h>

#include "constDef.h"
#include "tableDef.h"
#include "serverConfig.h"
#include "httpReader.h"
#include "com_lib.h"

/*
 * char *rq_malloc(StrBuff *strBuff,size_t len)
 */
char *rq_malloc_old(StrBuff *strBuff,size_t len){
	//p=malloc(len);
	char *p=NULL;
	//printf("rq_malloc():#1 pstrBuff->str=0x%x , pstrBuff->lng=%d, len=%d\n",pstrBuff->str,pstrBuff->lng,len);
	if((strBuff->lng + len) < strBuff->mxlmg){
		p=&(strBuff->str[strBuff->lng]);
		strBuff->lng += len;
		strBuff->prev=len;
	}
	else{
		printf("rq_malloc() :#9 no free memory!!\n");
	}
	//printf("rq_malloc():#90 p=0x%x,strBuff->str=0x%x,strBuff->lng=%d,strBuff->mxlmg=%d\n",p,strBuff->str,strBuff->lng,strBuff->mxlmg);
	return p;
}
/*
 * char *rq_malloc(StrBuff *strBuff,size_t len)
 */
char *rq_malloc(StrBuff *strBuff,size_t len){
	//p=malloc(len);
	char *p=NULL;
	//printf("rq_malloc():#1 pstrBuff->str=0x%x , pstrBuff->lng=%d, len=%d\n",pstrBuff->str,pstrBuff->lng,len);
	if((strBuff->lng + len) < strBuff->mxlmg){
		p=(strBuff->str) + strBuff->lng;
		strBuff->lng += len;
		strBuff->prev=len;
	}
	else{
		printf("rq_malloc() :#9 no free memory!!\n");
	}
	//printf("rq_malloc():#90 p=0x%x,strBuff->str=0x%x,strBuff->lng=%d,strBuff->mxlmg=%d\n",p,strBuff->str,strBuff->lng,strBuff->mxlmg);
	return p;
}
/*
 * void rq_free_prev(StrBuff *strBuff)
 * 直前のrq_malloc() で割り当てた領域の開放
 */
void rq_free_prev(StrBuff *strBuff){
	if(strBuff->prev >0){
		strBuff->lng -= strBuff->prev;
		strBuff->prev=0;
	}
}
/*
 * char *rq_strcpy(StrBuff *strBuff,char *str)
 * ストリングのメモリーを確保してコピー
 */
char *rq_strcpy(StrBuff *strBuff,char *str){
	char *p=NULL;
	size_t len;
	if(str != NULL){
		len = strlen(str)+1;
		p=rq_malloc(strBuff,len);
		if(p == NULL){
			printf("rq_strcpy():99 rq_malloc() error\n");
		}
		else{
			strcpy(p, str);
		}
	}
	return p;
}
/*
 * char *rq_strncpy(StrBuff *strBuff,char *str,int lng)
 * ストリングのメモリーを確保してコピー
 */
char *rq_strncpy(StrBuff *strBuff,char *str,int lng){
	char *p;
	size_t len;
	len = lng+1;
	//printf("get_strncpy() :#1 lng=%d\n",lng);

	p=rq_malloc(strBuff,len);
	if(p == NULL){
		printf("rq_strncpy():99 rq_malloc() error\n");
	}
	else{
		strncpy(p,str,lng);
		*(p+lng)=0x00;
	}
	return p;
}
/*
 * char *rq_strcat(StrBuff *strBuff,char *str1,char *str2)
 * ストリングを結合して、新たなメモリーを確保
 *   str1+str2
 */
char *rq_strcat(StrBuff *strBuff,char *str1,char *str2){
	char *p;
	size_t len;
	len = strlen(str1)+strlen(str2)+1;
	p=rq_malloc(strBuff,len);
	if(p == NULL){
		printf("get_strcat():99 malloc() error\n");
	}
	else{
		strcpy(p, str1);
		strcat(p, str2);
	}
	return p;
}

/*
 * ssize_t read_act (int cnt,int fd, void *buf, int nbytes)
 */
ssize_t read_act (int cnt,int fd, void *buf, int nbytes){
	int r_lng;
	int nread;
	int i;
	r_lng=0;
	//受信可能なデータサイズのチェック
	for(i=0;i<cnt;i++){
		//受信可能なデータサイズのチェック
		ioctl(fd, FIONREAD, &nread);
		if(nread > 0){
			r_lng = read(fd,buf,nbytes);
			return r_lng;
		}
		else if(nread < 0){
			break;
		}
		//printf("read_act() : #2 %d read delay occured!!\n",fd);
		//usleep(100000);	// 100 m sec -> 0.1 sec
		//usleep(50000);	// 50 m sec -> 0.05 sec
		//usleep(25000);	// 25 m sec -> 0.025 sec
		//usleep(10000);	// 10 m sec -> 0.01 sec
		//usleep(8000);	// 8 m sec -> 0.008 sec
		usleep(5000);		// 5 m sec -> 0.005 sec
	}
	return r_lng;
}
/*
 * void httpReader(HttpReq *httpReq)
 */
void httpReader(HttpReq *httpReq){
	HttpBuff *httpBuff;
	StrBuff *strBuff;
	int lng;
	char *fname_s,*p,*brkb;
	int nread;
	int i;

	httpBuff=&(httpReq->hbuff);
	strBuff=&(httpReq->sbuff);

	int st=0;
	while(1){
		//lng=getLine(httpReq->sockfd,httpBuff);
		lng=readLine(httpReq->sockfd,httpBuff);
		if(lng ==0){
			if(st==0){
				printf("httpReader() : #3 %d,%d data nothing\n",httpReq->seq_i,httpReq->sockfd);
			}
			break;
		}
		else if(lng < 0){
			printf("httpReader() : #4 %d,%d read error occured!!\n",httpReq->seq_i,httpReq->sockfd);
			break;
		}
		//printf("%s\n",httpBuff->a_line);
		switch(st){
			case 0:
				//printf("%d,%d a_line=%s\n",httpReq->seq_i,httpReq->sockfd,httpBuff->a_line);
				if(lng>0){
					httpReq->req = rq_strcpy(strBuff,httpBuff->a_line);

					/* strtok分割 1回目の呼出し */
					httpReq->method = strtok_r(httpReq->req," ",&brkb);
				    /* strtok分割 2回目以降の呼出し */
					fname_s = strtok_r(NULL, " ",&brkb);
					httpReq->version = strtok_r(NULL," ",&brkb);


				    /* strtok分割 1回目の呼出し */
					httpReq->fname = strtok_r(fname_s,"?",&brkb);
				    /* strtok分割 2回目以降の呼出し */
					httpReq->parm = strtok_r(NULL,"?",&brkb);

					//printf("method=%s\n",httpReq->method);
					//printf("fname=%s\n",httpReq->fname);
					//printf("parm=%s\n",httpReq->parm);
					//printf("version=%s\n",httpReq->version);
					st=1;
				}
				break;
			case 1:
				if(httpReq->hd_c < HttpReq_max_header){
					httpReq->header[httpReq->hd_c] = rq_strcpy(strBuff,httpBuff->a_line);
					httpReq->hd_c++;
				}
				else{
					perror("httpReader() : #5 excueed max HttpReq_max_header");
				}
				break;
		}
		//lng=getLine(httpReq->sockfd,&httpBuff);
	}
	//printf("httpReader() %d : #99\n",httpReq->seq_i);
}
/*
 * int getLine(int sockfd,HttpBuff *httpBuff)
 *  read http-req,header information lines
 */
int getLine(int sockfd,HttpBuff *httpBuff){
	//printf("getLine() : #1 start\n");
	int lng;
	int r_lng;
	httpBuff->isCr=false;

	for(lng=0;lng < HttpBuff_a_LineSize;){
		r_lng = read(sockfd,&(httpBuff->a_line[lng]),1);
		// stream end
		if(r_lng == 0){
			break;
		}
		// stream I/O error
		else if(r_lng == -1){
			printf("httpReader::getLine() :#2 %d I/O error\n",sockfd);
			break;
		}
		else if(httpBuff->a_line[lng] == 0x0d){
		}
		else if(httpBuff->a_line[lng] == 0x0a){
			httpBuff->isCr=true;
			break;
		}
		else{
			lng++;
		}
	}
	httpBuff->a_line[lng] = 0x00;
	return lng;
}
/*
 * int readLine(int sockfd,HttpBuff *httpBuff)
 * update by nishi 201.4.5.6
 */
int readLine(int sockfd,HttpBuff *httpBuff){
	int lng;
	int i;
	for(lng=0;lng < HttpBuff_a_LineSize;){
		if(httpBuff->r_lng == 0){
			if(httpBuff->isEof==false){
				httpBuff->r_lng = read(sockfd,&(httpBuff->i_buff[0]),HttpBuff_i_buffSize);
				if(httpBuff->r_lng <= 0){
					httpBuff->isEof=true;
					break;
				}
			}
			httpBuff->cur_p = 0;
		}
		if(httpBuff->i_buff[httpBuff->cur_p]==0x0a){
			httpBuff->cur_p++;
			httpBuff->r_lng--;
			break;
		}
		else if(httpBuff->i_buff[httpBuff->cur_p]!=0x0d){
			httpBuff->a_line[lng]=httpBuff->i_buff[httpBuff->cur_p];
			lng++;
		}
		httpBuff->cur_p++;
		httpBuff->r_lng--;
	}
	httpBuff->a_line[lng] = 0x00;
	return lng;
}
/*
 * int readBlock(int sockfd,HttpReq *httpReq)
 * update by nishi 2015.11.7
 * 注1) file upload など、データが大きいときに、ファイルが途中で途切れたり、サイズが一致しない場合は。
 * usleep() の値を大きくして下さい。 2015.11.7 by nishi
 *
 */
int readBlock(int sockfd,HttpReq *httpReq){
	int nread;
	int rlng=0;
	if(httpReq->hbuff.r_lng>0){
		memcpy(httpReq->buff,&(httpReq->hbuff.i_buff[httpReq->hbuff.cur_p]), httpReq->hbuff.r_lng);
		rlng = httpReq->hbuff.r_lng;
		httpReq->hbuff.r_lng=0;
	}
	//受信可能なデータサイズのチェック
	ioctl(httpReq->sockfd, FIONREAD, &nread);
	if(nread > 0){
		nread=read(httpReq->sockfd,&(httpReq->buff[rlng]),sizeof(httpReq->buff)-rlng);
		if(nread>0){
			rlng+=nread;
		}
		return rlng;
	}
	else if(nread < 0){
		return rlng;
	}
	//usleep(100000);	// 100 m sec -> 0.1 sec
	//usleep(50000);	// 50 m sec -> 0.05 sec
	//usleep(25000);	// 25 m sec -> 0.025 sec
	//usleep(10000);	// 10 m sec -> 0.01 sec
	usleep(8000);	// 8 m sec -> 0.008 sec
	//usleep(5000);		// 5 m sec -> 0.005 sec

	ioctl(httpReq->sockfd, FIONREAD, &nread);
	if(nread >0){
		nread=read(httpReq->sockfd,&(httpReq->buff[rlng]),sizeof(httpReq->buff)-rlng);
		if(nread>0){
			rlng+=nread;
		}
	}
	//printf("readBlock() : #90 rlng=%d\n",rlng);
	//memdump(httpReq->buff,rlng);
	return rlng;
}
/*
 * void init_httpReq(HttpReq *httpReq)
 * update by nishi 2014.5.6
 */
void init_httpReq(HttpReq *httpReq){
	httpReq->act_f=true;
	httpReq->req=NULL;
	httpReq->method=NULL;
	httpReq->fname=NULL;
	httpReq->parm=NULL;
	httpReq->version=NULL;
	httpReq->hd_c=0;
	httpReq->timer=(time_t)0;	// add by nishi 2015.11.8
	httpReq->sub_pid=0;		// add by nishi 2015.11.8
	httpReq->pipe_send=0;	// add by nishi 2015.11.9
	httpReq->pipe_recv=0;	// add by nishi 2015.11.9
	httpReq->hbuff.r_lng=0;	// add by nishi 2014.5.6
	httpReq->hbuff.isEof=false;	// add by nishi 2014.5.6
	httpReq->sbuff.lng=0;
	httpReq->sbuff.prev=0;
}
/*
 * void timer_httpReq(HttpReq *httpReq)
 */
void timer_httpReq(HttpReq *httpReq){
	//printf("timer_httpReq():#1 defs.timeout=%d\n",defs.timeout);
	time_t tm=time((time_t *)0);
	httpReq->timer=tm+(time_t)defs.timeout;
}
/*
 * void free_httpReq(HttpReq *phttpReq)
 */
void free_httpReq(HttpReq *phttpReq){
	// 例に入れてみます。 2014.3.23
	//if(phttpReq->req != NULL){
		//free(phttpReq->req);
		//phttpReq->req=NULL;
	//}
	/*
	if(phttpReq->method!=NULL){
		free(phttpReq->method);
	}
	if(phttpReq->version!=NULL){
		free(phttpReq->version);
	}
	if(phttpReq->fname!=NULL){
		free(phttpReq->fname);
	}
	if(phttpReq->parm!=NULL){
		free(phttpReq->parm);
	}
	*/
	int i;
	for(i=0;i < phttpReq->hd_c;i++){
		//free(phttpReq->header[i]);
		phttpReq->header[i]=NULL;
	}
}
/*
 * HttpReq *get_free_httpReq(HttpReq *phttpReq_pool)
 */
HttpReq *get_free_httpReq_old(HttpReq *phttpReq_pool){
	HttpReq * rc=NULL;
	int i;
	for(i=0;i<defs.maxThread_cnt;i++){
		if(phttpReq_pool->sockfd == -1){
			rc=phttpReq_pool;
			break;
		}
		phttpReq_pool++;
	}
	return rc;
}
/*
 * HttpReq *get_free_httpReq(HttpReq **phttpReq_pool)
 */
HttpReq *get_free_httpReq(HttpReq **phttpReq_pool){
	HttpReq * rc=NULL;
	int i;
	for(i=0;i<defs.maxThread_cnt;i++){
		if((*phttpReq_pool)->sockfd == -1){
			rc=*phttpReq_pool;
			break;
		}
		phttpReq_pool++;
	}
	return rc;
}

