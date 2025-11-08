/*
 * classProcessor.c
 *
 *  Created on: 2015/11/11
 *      Author: nishi
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

#include <jni.h>

#include "constDef.h"
#include "tableDef.h"
#include "serverConfig.h"
#include "httpReader.h"
#include "httpWriter.h"
#include "com_lib.h"

#include "classProcessor.h"

/*
 * classProcessor(HttpReq *httpReq,char *index_name)
 *  java class のリクエスト処理
 */
void classProcessor(HttpReq *httpReq,char *index_name){
	StrBuff *strBuff;
	char *dir_s;	// rq_malloc use
	char *prog;	// rq_malloc use
	char *hd_s;
	char *p;
	char *java_class_path;
	struct stat  st;

	bool first_f=true;
	bool pipe_f=true;

	char o_buff[512];
	char wk[512];
	int rc;

#ifndef _SILENT
	printf("classProcessor() :#1 %d,%d start index_name=%s,httpReq->parm=%s\n",httpReq->seq_i,httpReq->sockfd ,index_name,httpReq->parm);
#endif
	strBuff=&(httpReq->sbuff);

	//WorkBuff *workBuff=(WorkBuff *)(httpReq->buff);

	// cgi ディレクトリー初期化
	dir_s=rq_strcpy(strBuff,"");
	//printf("classProcessor() :#2 dir_s=%s\n",dir_s);

	// cgiディレクトリー と cgi name の取得
	p=strstr_last(index_name, "/");	// 最後の "/" 位置を取得

	//printf("classProcessor() :#2.1 p=%s\n",p);

	// index_name は、パスを含んでいます
	if(p != NULL){
		prog=rq_strcpy(strBuff,p+1);	// cgi name を取得
		//printf("classProcessor() :#2.2 p=%s\n",p);
		//printf("classProcessor() :#2.3 index_name=%s , p-index_name+1=%d\n",index_name,p-index_name+1);
		dir_s=rq_strncpy(strBuff,index_name,p-index_name+1);	// cgiディレクトリー を取得
		//printf("classProcessor() :#2.4 dir_s=%s\n",dir_s);
	}
	else{
		prog=index_name;
	}

	p=rq_strcat(strBuff,defs.class_modDir,"/");
	dir_s=rq_strcat(strBuff,p,dir_s);

#ifndef _SILENT
	printf("classProcessor() :#3 dir_s=%s, prog=%s\n",dir_s,prog);
#endif
	// dir ファイル無し
	if (stat(dir_s, &st) != 0){
		printf("classProcessor():#4 dir not found!\n");
		return;
	}

	// (*JNIEnv)->FindClass が prog="mod_test.xxx_cgi_mod" では検索出来ないので
	// -> "mod_test/xxx_cgi_mod" に変換
	strreplace(prog,'.','/');

	// -Djava.class.path= を設定します
	p=rq_strcpy(strBuff,"-Djava.class.path=");
	java_class_path=rq_strcat(strBuff,p,defs.class_path);

	int pipe_send[2];	// send to CGI pipe
	int pipe_recv[2];	// receive from CGI pipe
	pid_t fork_result;

	if(pipe(pipe_send)!=0){
		printf("classProcessor():#5 Send Pipe create failure\n");
		pipe_f=false;
	}
	else if(pipe(pipe_recv)!=0){
		printf("classProcessor():#6 Recieve Pipe create failure\n");
		pipe_f=false;
	}

	// raspi で、 JavaVMが ARMv6 時に実行出来ないときの表示の後に、
	// ごみの表示されるを抑止出来ないか試してみます。 2015.11.21 OK
	fflush(stdout);

	if(pipe_f==true){
		fork_result=fork();
		if(fork_result == (pid_t)-1){
			printf("classProcessor():#6 Fork failure");
		}
		/*--------
		* 子プロセスです
		---------*/
		else if(fork_result == (pid_t)0){
			int i;
			char *env;
			int ex_rc;

			//printf("classProcessor() :#6 child start file_pipes[0]=%d,file_pipes[1]=%d\n",file_pipes[0],file_pipes[1]);

			// CGI環境変数をセットします
			//env.put("REQUEST_METHOD", hrd.method);
			env=rq_strcat(strBuff,env_req_method,httpReq->method);
			// setenv()もあるみたい。
			putenv(env);		// 注 putenv() 後も envの領域は有効でなといけないみたい。

			// GET  ->  QUERY_STRING
			//env.put("QUERY_STRING", parm);
			if(httpReq->parm != NULL){
				//env.put("QUERY_STRING", httpReq->parm);
				env=rq_strcat(strBuff,env_query_str,httpReq->parm);
				putenv(env);
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

			// java class call start
			//下記はてすとです。
			//prog="mod_test/simple_cgi_mod";
			//prog="mod_test/test1_cgi_mod";

			jint result;

			JNIEnv *jenvp,jenv;
			JavaVM *jvmp,jvm;
			JavaVMInitArgs vm_args;
			JavaVMOption options[4];

			//options[0].optionString = (char *)"-Djava.class.path=.:/home/nishi/workspace-raspi/nishi-test2/bin:/usr/java/jdk1.7.0_80/jre/lib/rt.jar";
			options[0].optionString = java_class_path;
			options[1].optionString = (char *)"-Djava.compiler=NONE";
			vm_args.version = JNI_VERSION_1_4;
			vm_args.options = options;
			vm_args.nOptions = 2;
			//vm_args.ignoreUnrecognized = JNI_FALSE;
			vm_args.ignoreUnrecognized = JNI_TRUE;


			/*
			 * JavaVMを初期化，起動する
			 * JNIインターフェースへのポインタを返す
			 */
			//int result = JNI_CreateJavaVM(&javavm, (void **)&jnienv, &vm_args);
			result=JNI_CreateJavaVM(&jvmp,(void **)&jenvp,&vm_args);
			if(result < 0){
				printf("classProcessor() :#7 JNI_CreateJavaVM error %d\n",result);
				exit(0);
			}
			jenv = *jenvp;
			jvm = *jvmp;

			/*
			 * クラスをさがす
			 */
			//jclass cls = jnienv->FindClass(prog);
			jclass cls = jenv->FindClass(jenvp, prog);
			if (cls == 0) {
				printf("classProcessor() :#8 java class not found %s",prog);
				exit(0);
			}
			/*
			 * Methodをさがす
			 * GetStaticMethodIDの引数については後述
			 */
			//jmethodID mid = jenv->GetMethodID(jenvp,cls, "cgi_go2", "(Ljava/lang/String;Ljava/lang/String;)V");
			jmethodID mid = jenv->GetMethodID(jenvp,cls, "cgi_go2", "()V");
			//jmethodID mid = jenv->GetStaticMethodID(jenvp, cls, "testmethod", "()V");
			if (mid == 0) {
				printf("classProcessor() :#9 not locate method cgi_go2 with signature ()V");
				exit(0);
			}
			/*
			 * インスタンス作成 -> これで実行されるみたい。
			 * パラメータもここで渡す必要がある。
			 */
			//jstring method =jenv->NewStringUTF(jenvp,"GET");
			//jstring parms =jenv->NewStringUTF(jenvp,"hello=java");
			//jobject obj = jenv->NewObject(jenvp,cls, mid, method,parms);
			jobject obj = jenv->NewObject(jenvp,cls, mid);

			//何らかの処理が終わって、終了するときにはついでにJavaVMも終わらす必要があります.
			result = jvm->DestroyJavaVM(jvmp);

			// ここの close(0),close(1)は、テストで入れてみます。 2015.11.20
			// raspi で、 JavaVMが ARMv6 時に実行出来ないときの表示の後に、
			// ごみの表示されるを抑止出来ないか試してみます。--> 結果NG
			// これを入れら、早くなったような気がします。錯覚か
			close(0);
			close(1);

			// java class call end
			exit(0);

		}
		/*--------
		* 親プロセスです
		---------*/
		else{
#ifndef _SILENT
			printf("classProcessor() :#24 exec sub proc=%d,pipe_send[1]=%d,pipe_recv[0]=%d\n",fork_result,pipe_send[1],pipe_recv[0]);
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
				printf("classProcessor() :#25 close pipe_send[1]!!\n");
#endif
				// cgiへの入力をクローズ
				close(pipe_send[1]);
				httpReq->pipe_send=0;
			}
			else{
				// cgi へリダイレクトする  httpReq->sockfd -> pipe_send[1]
				while(1){
					//printf("classProcessor() :#30 readBlock() call!!\n");
					nread=readBlock(httpReq->sockfd,httpReq);
					//printf("classProcessor() :#31 readBlock() return!!\n");
					if(nread > 0){
#ifndef _SILENT
						printf("cgiProcessor() :#32 request from Client nread=%d\n",nread);
						//printf("classProcessor() :#32.1 req-data=%.*s\n",nread,httpReq->buff);
#endif
						if(write(pipe_send[1],httpReq->buff,nread)!=nread){
							printf("cgiProcessor() :#31  write error\n");
							break;
						}
						//printf("classProcessor() :#33 write to cgi one record end\n");
					}
					else{
						break;
					}
				}
#ifndef _SILENT
				printf("classProcessor() :#34 write to cgi all end\n");
				printf("classProcessor() :#35 close pipe_send[1]=%d\n",pipe_send[1]);
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
				printf("classProcessor() :#46 read from cgi nread=%d\n",nread);
				//printf("classProcessor() :#46.1 recv-data=%.*s\n",nread,httpReq->buff);
#endif
				if(nread > 0){
					//memdump(httpReq->buff,nread);
					if(first_f==true){
#ifndef _SILENT
						printf("classProcessor() :#47 write http header\n");
#endif
						if(write(httpReq->sockfd,o_buff,sl) != sl){
							printf("classProcessor() :#48  write error \n");
							break;
						}
						// "\r\n"が無ければ、先頭に付加します。
						// これで、エラーがブラウザーに表示されます。
						if(0 > strstr_char(httpReq->buff,"\r\n",nread)){
#ifndef _SILENT
							printf("classProcessor() :#49 write http header end\n");
#endif
							if(write(httpReq->sockfd,"\r\n",2) != 2){
								printf("classProcessor() :#50  write error \n");
								break;
							}
						}
					}
					if(write(httpReq->sockfd,httpReq->buff,nread) != nread){
						printf("classProcessor() :#51  write error \n");
						break;
					}
					first_f=false;
				}
				else if(nread == 0){
					break;
				}
				else{
					printf("classProcessor() :#52 pipe read error end!!\n");
					break;
				}
			}
#ifndef _SILENT
			printf("classProcessor() :#53 read end,close pipe_recv[0]=%d\n",pipe_recv[0]);
#endif
			// cgiからの出力をクローズ
			close(pipe_recv[0]);
			httpReq->pipe_recv=0;
		}
	}
	else{
		printf("classProcessor():#80 Pipe create failure\n");
	}
#ifndef _SILENT
	printf("classProcessor() :#91 %d,%d end\n",httpReq->seq_i,httpReq->sockfd);
#endif
}


