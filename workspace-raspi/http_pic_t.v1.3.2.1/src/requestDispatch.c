/*
 * requestDispatch.c
 *	Ver 1.3.1 update 2015.10.27
 *  Created on: 2014/03/15
 *      Author: nishi
 */
#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <string.h>
#include <netinet/in.h>
#include <signal.h>

#include "tableDef.h"
#include "serverConfig.h"
#include "requestDispatch.h"
#include "httpReader.h"
#include "contextProcessor.h"
#include "cgiProcessor.h"
#include "classProcessor.h"
#include "com_lib.h"
#include "checkAllowNetworks.h"

static int cleanup_pop_arg = 0;

/*
 * thread キャンセル処理ハンドラー
 */
void cleanup_handler(void *arg){
	HttpReq *httpReq=arg;
	printf("requestDispatch:cleanup_handler called. httpReq->th_no=%d \n",httpReq->th_no);
	// クライアントへの出力をクローズする
	shutdown(httpReq->sockfd,1);
	close(httpReq->sockfd);

	httpReq->act_f=false;
	httpReq->timer=0;

	// subプロセスの kill
	if(httpReq->sub_pid > 1){
		kill(httpReq->sub_pid,SIGKILL);
		httpReq->sub_pid=0;
	}
	// cgiへの送信 pipe クローズ
	if(httpReq->pipe_send !=0){
		close(httpReq->pipe_send);
		httpReq->pipe_send=0;
	}
	// cgi からの受信 pipe クローズ
	if(httpReq->pipe_recv !=0){
		close(httpReq->pipe_recv);
		httpReq->pipe_recv=0;
	}
}

void *requestDispatch(void *arg){
	int client_len;
	struct sockaddr_in client_address;
	client_len = sizeof(client_address);
	char wk[30];
	unsigned long int s_addr_x;

	HttpReq *httpReq=arg;

	// スレッドをデタッチします。
	// スレッドに割り当てられているリソースが終了時に回収可能であることをシステムに知らせます。
	if(pthread_detach(pthread_self()) !=0){
		printf("requestDispatch():#1 detach() error\n");
		pthread_exit(NULL);
	}
	//printf("requestDispatch():#2 called 0x%x,0x%x httpReq->sbuff.str=0x%x\n",httpReq->seq_i,httpReq->sockfd,httpReq->sbuff.str);

	if(pthread_setcancelstate(PTHREAD_CANCEL_ENABLE,NULL)!=0){
		printf("requestDispatch():#3 pthread_setcancelstate() exec error\n");
		exit(EXIT_FAILURE);
	}

	if(pthread_setcanceltype(PTHREAD_CANCEL_ASYNCHRONOUS,NULL)!=0){
		printf("requestDispatch():#4 pthread_setcanceltype() exec error\n");
		exit(EXIT_FAILURE);
	}

	pthread_cleanup_push(cleanup_handler,httpReq);

	while(1){
		init_httpReq(httpReq);
		httpReq->sockfd = accept(httpReq->server_sockfd,(struct sockaddr *)&client_address,&client_len);
#ifndef _SILENT
		printf("requestDispatch():#4 connected httpReq->th_no=%d\n",httpReq->th_no);
#endif
		// クライアントIP のチェック
		if(checkAllowNetworks_chek_ip(client_address.sin_addr.s_addr)==false){
			s_addr_x=ntohl(client_address.sin_addr.s_addr);
			checkAllowNetworks_ip_to_a(wk,s_addr_x,true);
#ifndef _SILENT
			printf("requestDispatch():#5 connect refuse,ip_addr=%s\n",wk);
#endif
		}
		else{
			timer_httpReq(httpReq);
			httpReader(httpReq);
			// cgi request ?
			if(httpReq->fname != NULL && httpReq->method != NULL){
				if(str_startsWith(httpReq->fname,"/cgi-bin/") == 0){
					if(strcmp(httpReq->method,"GET") == 0 || strcmp(httpReq->method,"POST")==0){	// GET method or POST method
						httpReq->func=1;	// "cgi-bin"
						cgiProcessor(httpReq,httpReq->fname+9);
					}
				}
				// C++ cgi request add by nishi 2015.11.8
				else if(str_startsWith(httpReq->fname,"/ccgi-bin/") == 0){
					if(strcmp(httpReq->method,"GET") == 0 || strcmp(httpReq->method,"POST")==0){	// GET method or POST method
						httpReq->func=2;	// "ccgi-bin"
						cgiProcessor(httpReq,httpReq->fname+10);
					}
				}
				// java cgi request add by nishi 2015.10.27
				else if(str_startsWith(httpReq->fname,"/class-bin/") == 0){
					if(strcmp(httpReq->method,"GET") == 0 || strcmp(httpReq->method,"POST")==0){	// GET method or POST method
						httpReq->func=3;	// "class-bin"
						cgiProcessor(httpReq,httpReq->fname+11);
					}
				}
#ifndef _REJECT_CLASSPROCESSOR
				// java class request add by nishi 2015.11.11
				else if(str_startsWith(httpReq->fname,"/class-mod/") == 0){
					if(strcmp(httpReq->method,"GET") == 0 || strcmp(httpReq->method,"POST")==0){	// GET method or POST method
						httpReq->func=4;	// "class-mod"
						classProcessor(httpReq,httpReq->fname+11);
					}
				}
#endif
				else if(strcmp(httpReq->method,"GET")==0){
					httpReq->func=0;	// "docroot"
					contextProcessor(httpReq,httpReq->fname+1);
				}
				else{
					printf("requestDispatch():#6 %d,%d undefined req comm httpReq.method=%s\n",httpReq->seq_i,httpReq->sockfd,httpReq->method);
				}
			}
			else{
				printf("requestDispatch():#7 %d,%d empty connection comm!!\n",httpReq->seq_i,httpReq->sockfd);
			}
		}

		//printf("requestDispatch() :#9 close sockfd=%d\n",httpReq.sockfd);

		// クライアントへの出力をクローズする
		shutdown(httpReq->sockfd,1);
		close(httpReq->sockfd);
		free_httpReq(httpReq);

		//printf("requestDispatch():#90  free socked %d %d,%d \n",httpReq->th_no,httpReq->seq_i,httpReq->sockfd);

	    httpReq->sockfd=-1;
	    httpReq->seq_i++;
	}
	pthread_cleanup_pop(cleanup_pop_arg);
	pthread_exit(NULL);
}



