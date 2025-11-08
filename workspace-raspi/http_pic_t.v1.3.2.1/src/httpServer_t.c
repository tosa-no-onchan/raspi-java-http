/*
 * httpServer_t.c
 * http server thread with request pool and thread pool
 * Version 1.3.2
 * Update on: 2015/11/08
 *      Author: nishi
 *  how to run
 *    ./http_server_t&
 *  how to check
 *    netstat -tlp
 *  Author : Net-Mall Tosa
 * 注1)	file upload など、データが大きいときに、ファイルが途中で途切れたり、サイズが一致しない場合は。
 *  httpReader.c の readBlock() の中に、 usleep() の値を大きくして下さい。 2015.11.7 by nishi
 *
 */

#include <pthread.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <netinet/in.h>
#include <signal.h>
#include <errno.h>

#include <sys/ioctl.h>
#include <string.h>

#include "tableDef.h"
#include "serverConfig.h"
#include "requestDispatch.h"
#include "httpReader.h"


#define handle_error_en(en, msg) \
               do { errno = en; perror(msg); exit(EXIT_FAILURE); } while (0)

int main(){
	int on,i,res;
	int server_sockfd;
	int server_len;
	struct sockaddr_in server_address;

	pthread_t *threads;
	Dispatch_parm *dispatch_parm;

	int result;

	int seq_i=0;

	sigset_t set;

	//HttpReq *httpReq_pool[MAX_THREAD_CNT];
	HttpReq **httpReq_pool;
	HttpReq **phttpReq_pool;
	HttpReq *phttpReq;

	init_serverConfig(&defs);

	printf("Welcome Raspberry Pi light http server\n"\
			"httpServer_t ver=%s %s\n"\
			"written by C ,multi thread with thread pool.\n"\
			,defs.version,defs.update);

	serverConfig(&defs);

	if(0){
		sigemptyset(&set);
		sigaddset(&set,SIGQUIT);
		result = pthread_sigmask(SIG_BLOCK, &set, NULL);
		if (result != 0){
			handle_error_en(result, "pthread_sigmask");
		}
	}

	/*
	 * threads[] アロケート 2015.11.7
	 */
	threads = malloc(sizeof(pthread_t)*defs.maxThread_cnt);
	/*
	 * httpReq_pool をアロケート
	 */
	httpReq_pool=malloc(sizeof(HttpReq *)*(defs.maxThread_cnt));
	if(httpReq_pool == NULL){
		printf("httpServer_t():#2 httpReq_pool alloc failed\n");
		exit(EXIT_FAILURE);
	}

	/*
	 * 待機 HttpReq をアロケート
	 */
	phttpReq_pool=httpReq_pool;
	for (i=0;i<defs.maxThread_cnt;i++){
		*phttpReq_pool=malloc(sizeof(HttpReq));
		(*phttpReq_pool)->sockfd=-1;	// set not use indicate
		(*phttpReq_pool)->th_no=i;
		// StrBuff のアロケート
		(*phttpReq_pool)->sbuff.str=(*phttpReq_pool)->sbuff_data;
		//(*phttpReq_pool)->sbuff.str=malloc(HttpReq_str_Size);
		(*phttpReq_pool)->sbuff.mxlmg=HttpReq_str_Size;
		//printf("httpServer_t():#1-1 httpReq_pool[%d].sbuff.str=0x%x\n",i,(*phttpReq_pool)->sbuff.str);
		//printf("httpServer_t():#1-2 httpReq_pool[%d].sbuff.mxlmg=%d\n",i,(*phttpReq_pool)->sbuff.mxlmg);
		phttpReq_pool++;
	}

	/* this mutex is used to synchronize access to all common memory */
	if( pthread_mutex_init(&mutex_mem, NULL) != 0 ) {
		printf("httpServer_t():#3 could not initialize mutex\n");
		exit(EXIT_FAILURE);
	}

	// サーバーソケットを作成
	server_sockfd = socket(AF_INET,SOCK_STREAM,0);
	if (server_sockfd < 0 ) {
		fprintf(stderr, "httpServer_t():#4 socket create failed\n");
		exit(EXIT_FAILURE);
	}
	// ignore "socket already in use" errors
	// サーバーソケットは、プログラムでクローズしてもシステムには、しばらく残り続ける(TCP/IPの通信の保証の為)
	// ので、プロセスを再起動したときに、この設定が必要です。
	on = 1;
	if (setsockopt(server_sockfd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) {
		perror("httpServer_t():#5 setsockopt(SO_REUSEADDR) failed");
		exit(EXIT_FAILURE);
	}
	//printf("httpServer_t() :#6 socket_sockfd=%d\n",server_sockfd);

	server_address.sin_family= AF_INET;
	server_address.sin_addr.s_addr=htonl(INADDR_ANY);
	//server_address.sin_port = htons(9734);
	server_address.sin_port = htons(defs.HTTP_PORT);
	server_len = sizeof(server_address);

	if(bind(server_sockfd,(struct sockaddr *)&server_address,server_len) == -1){
		perror("httpServer():#7  socket bind error");
		exit(EXIT_FAILURE);
	}

	listen(server_sockfd,defs.maxConnect);
	// SIGCHLD -> サブプロセス、スレッドでエラーになった場合のシグナルを抑制します。
	// これを入れないと、cgiProcess.c で cgi を fork()するたびに、ゾンビが残る。
	signal(SIGCHLD,SIG_IGN);

	/* ignore SIGPIPE (send by OS if transmitting to closed TCP sockets) */
	signal(SIGPIPE, SIG_IGN);

	phttpReq_pool=httpReq_pool;
	//スレッドプールを作成
	for (i = 0; i < defs.maxThread_cnt; i++) {
		//printf("httpServer_t() :#10 i=%d\n",i);
		/*
		 * 新しいスレッドは、requestDispatch() から開始します。
		 */
		phttpReq=*phttpReq_pool;
		phttpReq->act_f=false;
		phttpReq->seq_i=0;
		phttpReq->server_sockfd=server_sockfd;
		phttpReq->timer=0;

		if(pthread_create(&threads[i], NULL, requestDispatch, (void*)phttpReq) != 0 ) {
			printf("httpServer_t():#10 Could not create thread.\n");
			exit(EXIT_FAILURE);
		}
		sleep(1);
		phttpReq_pool++;
	}

	// タイムオーバーの connection を監視します
	while(1){
		//sleep(10000);
		sleep(10);
		int i,j;
		//printf("httpServer_t():#20 wake up\n");

		phttpReq_pool=httpReq_pool;
		time_t tm=time((time_t *)0);
		for (i = 0; i < defs.maxThread_cnt; i++) {
			phttpReq=*phttpReq_pool;
			// タイムオーバーの接続があります。
			if(phttpReq->act_f==true && phttpReq->timer != (time_t)0 && tm > phttpReq->timer){
				printf("httpServer_t():#22 time out connection found no=%d\n",i);
				//該当 requestDispatch のキャンセル要求します。
				if(pthread_cancel(threads[i])!=0){
					printf("httpServer_t():#23 Could not cancel thread.\n");
				}
				else{
					printf("httpServer_t():#24 cancel thread request ok.\n");
					j=0;
					// requestDispatch を停止を待ちます。
					while(phttpReq->act_f==true && j<5){
						printf("httpServer_t():#25 waiting for thread stop.\n");
						sleep(1);
						j++;
					}
					printf("httpServer_t():#26 restart thread.\n");
					// 再起動します。
					phttpReq->act_f=false;
					phttpReq->seq_i=0;
					phttpReq->server_sockfd=server_sockfd;
					phttpReq->timer=0;
					phttpReq->sub_pid=0;
					if(pthread_create(&threads[i], NULL, requestDispatch, (void*)phttpReq) != 0 ) {
						printf("httpServer_t():#27 Could not create thread.\n");
					}
					sleep(1);
				}
			}
			phttpReq_pool++;
		}
	}
}
