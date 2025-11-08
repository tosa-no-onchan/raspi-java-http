/*
 * cgiProcessor.c
 * Version 1.3.1
 * Update on: 2015/11/07
 * Author: nishi
 * use poll
 */
#include <pthread.h>
#include <sys/types.h>
#include <sys/ioctl.h>
#include <sys/wait.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdbool.h>
#include <unistd.h>
#include <errno.h>
#include <signal.h>
#include <time.h>
#include <poll.h>

#include <netinet/in.h>
#include <sys/time.h>

#ifdef _GCC_OLD
// ioctl(..., I_SENDFD,.)
#include <stropts.h>
#endif

#include "constDef.h"
#include "tableDef.h"
#include "serverConfig.h"
#include "httpReader.h"
#include "httpWriter.h"
#include "com_lib.h"

#include "cgiProcessor.h"

/*
 * cgiProcessor(HttpReq *httpReq,char *index_name)
 *  perl cgi のリクエスト処理
 */
void cgiProcessor(HttpReq *httpReq,char *index_name){
	StrBuff *strBuff;
	char *dir_s;	// rq_malloc use
	char *prog;	// rq_malloc use
	char *hd_s;
	char *p;
	struct stat  st;

	bool first_f=true;
	bool pipe_f=true;

	char o_buff[512];
	char wk[512];
	int rc;

#ifndef _SILENT
	printf("cgiProcessor() :#1 %d,%d start index_name=%s,httpReq->parm=%s\n",httpReq->seq_i,httpReq->sockfd ,index_name,httpReq->parm);
#endif

	strBuff=&(httpReq->sbuff);

	//WorkBuff *workBuff=(WorkBuff *)(httpReq->buff);

	// cgi ディレクトリー初期化
	dir_s=rq_strcpy(strBuff,"");
	//printf("cgiProcessor() :#2 dir_s=%s\n",dir_s);

	// cgiディレクトリー と cgi name の取得
	p=strstr_last(index_name, "/");	// 最後の "/" 位置を取得

	//printf("cgiProcessor() :#2.1 p=%s\n",p);

	// index_name は、パスを含んでいます
	if(p != NULL){
		prog=rq_strcpy(strBuff,p+1);	// cgi name を取得
		//printf("cgiProcessor() :#2.2 p=%s\n",p);
		//printf("cgiProcessor() :#2.3 index_name=%s , p-index_name+1=%d\n",index_name,p-index_name+1);
		dir_s=rq_strncpy(strBuff,index_name,p-index_name+1);	// cgiディレクトリー を取得
		//printf("cgiProcessor() :#2.4 dir_s=%s\n",dir_s);
	}
	else{
		prog=index_name;
	}
	switch(httpReq->func){
	case 1:
		p=rq_strcat(strBuff,defs.cgi_rootDir,"/");
		break;
	case 2:
		p=rq_strcat(strBuff,defs.ccgi_rootDir,"/");
		break;
	case 3:
		p=rq_strcat(strBuff,defs.class_rootDir,"/");
		break;
	}
	dir_s=rq_strcat(strBuff,p,dir_s);

	//printf("cgiProcessor() :#3 dir_s=%s, prog=%s\n",dir_s,prog);

	// dir ファイル無し
	if (stat(dir_s, &st) != 0){
		printf("cgiProcessor():#4 dir not fined!\n");
		return;
	}

	int pipe_send[2];	// send to CGI pipe
	int pipe_recv[2];	// receive from CGI pipe
	pid_t fork_result;

	if(pipe(pipe_send)!=0){
		printf("cgiProcessor():#5 Send Pipe create failure\n");
		pipe_f=false;
	}
	else if(pipe(pipe_recv)!=0){
		printf("cgiProcessor():#6 Recieve Pipe create failure\n");
		pipe_f=false;
	}
	if(pipe_f==true){
		fork_result=fork();
		if(fork_result == (pid_t)-1){
			printf("cgiProcessor():#6 Fork failure");
		}
		/*--------
		* 子プロセスです
		---------*/
		else if(fork_result == (pid_t)0){
			int i;
			char *env;
			int ex_rc;

			//printf("cgiProcessor() :#6 child start file_pipes[0]=%d,file_pipes[1]=%d\n",file_pipes[0],file_pipes[1]);

			// CGI環境変数をセットします
			//env.put("REQUEST_METHOD", hrd.method);
			//env=get_strcat(env_req_method,httpReq->method);

			env=rq_strcat(strBuff,env_req_method,httpReq->method);
			// setenv()もあるみたい。
			putenv(env);		// 注 putenv() 後も envの領域は有効でなといけないみたい。

			// GET  ->  QUERY_STRING
			//env.put("QUERY_STRING", parm);
			if(httpReq->parm != NULL){
				//env=get_strcat(env_query_str,httpReq->parm);

				env=rq_strcat(strBuff,env_query_str,httpReq->parm);
				putenv(env);
				//p=getenv("QUERY_STRING");
				//if(p != NULL){
				//	printf("cgiProcessor():#6 QUERY_STRING=%s\n",p);
				//}
			}

			//ここは、改善すべきか?
			for(i=0;i < httpReq->hd_c;i++){
				if(str_startsWith(httpReq->header[i],"Content-Type:") == 0){
					//env.put("CONTENT_TYPE", hrd.http_hd.get("Content-Type:"));
					//env=get_strcat(env_cont_type,httpReq->header[i]+14);

					env=rq_strcat(strBuff,env_cont_type,httpReq->header[i]+14);
					//printf("cgiProcessor():#7 env=%s\n",env);
					putenv(env);
					//p=getenv("CONTENT_TYPE");
					//if(p != NULL){
					//	printf("cgiProcessor():#8 CONTENT_TYPE=%s\n",p);
					//}
				}
				else if(str_startsWith(httpReq->header[i],"Content-Length:") == 0){
					//env.put("CONTENT_LENGTH", hrd.http_hd.get("Content-Length:"));
					//env=get_strcpy("CONTENT_LENGTH=");
					//env=get_strcat(env_cont_lng,httpReq->header[i]+16);

					env=rq_strcat(strBuff,env_cont_lng,httpReq->header[i]+16);
					//printf("cgiProcessor():#9 env=%s\n",env);
					putenv(env);
					//p=getenv("CONTENT_LENGTH");
					//if(p != NULL){
					//	printf("cgiProcessor():#10 CONTENT_LENGTH=%s\n",p);
					//}
				}
				else if(str_startsWith(httpReq->header[i],"Cookie:")==0){
					//env.put("HTTP_COOKIE", hrd.http_hd.get("Cookie:"));
					//env=get_strcpy("HTTP_COOKIE=");
					//env=get_strcat(env_cookie,httpReq->header[i]+8);

					env=rq_strcat(strBuff,env_cookie,httpReq->header[i]+8);
					//printf("cgiProcessor():#11 env=%s\n",env);
					putenv(env);
				}
			}
			// execされるプロセスのstdin,stdout を pipe_send[0],pipe_recv[1] に接続
			close(0);	// stdin assign
			dup(pipe_send[0]);
			close(pipe_send[0]);
			close(pipe_send[1]);

			close(1);	// stdout assign
			dup(pipe_recv[1]);
			close(pipe_recv[0]);
			close(pipe_recv[1]);
			chdir(dir_s);


			switch(httpReq->func){
			case 1:
			case 2:
				ex_rc=execl(prog,0);
				break;
			case 3:
				ex_rc=execl("/usr/bin/java","-classpath .",prog,0);
				break;
			}
			// 通常 execl()が正常に為されると、ここには、制御は来ません。
			// ここに制御が来るのは、execl()がエラーの場合(rc=-1)です。
			//エラーコードは、errno に設定されます。
			//if(execl("/home/nishi/www-raspi/cgi-bin/hello.cgi",0)==-1){
			//if(execl("hello5.cgi",0)==-1){
			//if(execl("/usr/bin/java","-classpath .",prog,0)==-1){
			if(ex_rc==-1){
				// ここで、printf を行うと、http response として clientに送信されます。
				//printf("cgiProcessor() :#7 errno=%s\n",strerror(errno));
				// 注 sprintf は、スレッドで NG?
				sprintf(httpReq->buff,"errno=%s<br />\n",strerror(errno));

				// "HTTP/1.1 404 internal server error\r\n"
				strcpy(o_buff,htttp_er_404);

				// "Date: "+now+"\r\n"
				hd_s="Server: HTTP 1.1\r\n";
				// Server: raspberry-pi\r\n
				strcat(o_buff, http_server);
				// "Content-type: text/html\r\n\r\n" // 注）最後は \r\nが２個必要
				strcat(o_buff, http_ctype_html);

				write(httpReq->sockfd,o_buff,strlen(o_buff));
				write(httpReq->sockfd,httpReq->buff,strlen(httpReq->buff));
			}
			else{
				// cgi 出力
				//InputStream is = p.getInputStream();
			}
			exit(0);
		}
		/*--------
		* 親プロセスです
		---------*/
		else{
#ifndef _SILENT
			printf("cgiProcessor() :#24 exec sub proc=%d,pipe_send[1]=%d,pipe_recv[0]=%d\n",fork_result,pipe_send[1],pipe_recv[0]);
#endif
			httpReq->sub_pid=fork_result;
			close(pipe_send[0]);
			close(pipe_recv[1]);

			httpReq->pipe_send=pipe_send[1];
			httpReq->pipe_recv=pipe_recv[0];

			int nread;
			int result;
			int fd;

			// PUT Http Header OK
			// ----start ---
			// HTTP/1.1 200 OK\r\n
			strcpy(o_buff,http_ok);
			// Date: Tue, 06 May 2014 09:56:42 GMT
			// 現在に時刻を取得
			time_t ctm;
			ctm=time(NULL);
			ctime_r(&ctm,wk);
			char *ctm_s=wk;
			rtrim(ctm_s,0x0a);
			strcat(o_buff,http_date);
			strcat(o_buff,ctm_s);
			strcat(o_buff,"\r\n");

			// Server: raspberry-pi\r\n
			strcat(o_buff, http_server);
			//Keep-Alive: timeout=5, max=500
			strcat(o_buff, http_keep_alive);
			//Connection: Keep-Alive
			strcat(o_buff, http_con_keep_alive);
			//Transfer-Encoding: chunked
			//+"Content-type: text/html\r\n\r\n" // 注）最後は \r\nが２個必要
			size_t sl=strlen(o_buff);
			//---- end ----

			int lpc=0;

			// GET method
			if(strcmp(httpReq->method,"GET")==0){
#ifndef _SILENT
				// cgiへの出力 を行いません
				printf("cgiProcessor() :#25 close pipe_send[1]!!\n");
#endif
				// cgiへの入力をクローズ
				close(pipe_send[1]);
				httpReq->pipe_send=0;
			}
			else{
				// cgi へリダイレクトする  httpReq->sockfd -> pipe_send[1]
				while(1){
					//printf("cgiProcessor() :#30 readBlock() call!!\n");
					nread=readBlock(httpReq->sockfd,httpReq);
					//printf("cgiProcessor() :#31 readBlock() return!!\n");
					if(nread > 0){
#ifndef _SILENT
						printf("cgiProcessor() :#32 request from Client nread=%d\n",nread);
						//printf("cgiProcessor() :#32.1 req-data=%.*s\n",nread,httpReq->buff);
#endif
						if(write(pipe_send[1],httpReq->buff,nread)!=nread){
							printf("cgiProcessor() :#31  write error\n");
							break;
						}
						//printf("cgiProcessor() :#33 write to cgi one record end\n");
					}
					else{
						break;
					}
				}
#ifndef _SILENT
				printf("cgiProcessor() :#34 write to cgi all end\n");
				printf("cgiProcessor() :#35 close pipe_send[1]=%d\n",pipe_send[1]);
#endif
				// cgiへの入力をクローズ
				close(pipe_send[1]);
				httpReq->pipe_send=0;
			}
			// cgi からの出力を受け取る   httpReq->sockfd  <-  pipe_recv[0]
			while(1){
				// 注)read() は、データが上がってくるか。close されるまで待つ。
				// cgi側が、closeしないと、制御が戻ってこないので注意。
				// 将来は、上位で、全体のタイム監視機能を組み込む必要があります。
				nread=read(pipe_recv[0],httpReq->buff,sizeof(httpReq->buff));
#ifndef _SILENT
				printf("cgiProcessor() :#46 read from cgi nread=%d\n",nread);
				//printf("cgiProcessor() :#46.1 recv-data=%.*s\n",nread,httpReq->buff);
#endif
				if(nread > 0){
					//memdump(httpReq->buff,nread);
					if(first_f==true){
#ifndef _SILENT
						printf("cgiProcessor() :#47 write http header\n");
#endif
						if(write(httpReq->sockfd,o_buff,sl) != sl){
							printf("cgiProcessor() :#48  write error \n");
							break;
						}
					}
					if(write(httpReq->sockfd,httpReq->buff,nread) != nread){
						printf("cgiProcessor() :#49  write error \n");
						break;
					}
					first_f=false;
				}
				else if(nread == 0){
					break;
				}
				else{
					printf("cgiProcessor() :#50 pipe read error end!!\n");
					break;
				}
			}
#ifndef _SILENT
			printf("cgiProcessor() :#51 read end,close pipe_recv[0]=%d\n",pipe_recv[0]);
#endif
			// cgiからの出力をクローズ
			close(pipe_recv[0]);
			httpReq->pipe_recv=0;
		}
	}
	else{
		printf("cgiProcessor():#80 Pipe create failure\n");
	}
#ifndef _SILENT
	printf("cgiProcessor() :#91 %d,%d end\n",httpReq->seq_i,httpReq->sockfd);
#endif
}

