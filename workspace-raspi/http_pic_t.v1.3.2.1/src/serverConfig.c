/*
 * serverConfig.c
 *	v1.3.2
 *  update on: 2015/11/8
 *      Author: nishi
 */

#define SERVERCONFIG_DEF

#include <pthread.h>
#include <fcntl.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>


#include "tableDef.h"
#include "serverConfig.h"
#include "com_lib.h"
#include "checkAllowNetworks.h"

Defs defs;
pthread_mutex_t mutex_mem;

void init_serverConfig(Defs *defs){
	defs->version = "1.3.2.1";	// Version
	defs->update = "2015.11.11";	// Update
	defs->HTTP_PORT = 80;	//HTTP Server connection port no
	defs->conf_file = "server.conf";
	defs->d_conf_file = "/etc/http_pid/server.conf";	// conf_file for deamon
	defs->myhost = "www6.net-tosa.cxm";
	defs->myhostip ="192.168.1.150";
	defs->myuser="nishi";	// nishi / pi-nishi
	defs->timeout=120;	// [sec]
	defs->maxConnect=5;
	defs->maxThread_cnt= defs->maxConnect+1;	// Max Tread count = maxConnect+1
	defs->allow_networks="192.168.1.0/255.255.255.0";

	defs->doc_rootDir ="/home/not-use"; // "/home/nishi/www-raspi/html"

	defs->cgi_rootDir ="/home/not-use"; // "/home/nishi/www-raspi/cgi-bin"
	defs->ccgi_rootDir ="/home/not-use"; // "/home/nishi/www-raspi/ccgi-binc"	// add by nishi 2015.11.8

	defs->class_rootDir ="/home/not-use"; // "/home/nishi/workspace-raspi/nishi-test/bin"
	defs->class_modDir ="/home/not-use"; // "/home/nishi/workspace-raspi/nishi-test/bin"
	defs->tmp_dir ="/tmp/ras-pi-upload";	// file up-load working dir
		// Activated value
	defs->error_log="";
	defs->built_in="";		// built-in package name
	defs->class_path="";	// ".:/home/nishi/workspace-raspi/nishi-test2/bin:/usr/java/default/jre/lib/rt.jar"
}

void serverConfig(Defs *defs){
	//printf("serverConfig():#1 start\n");
	printf("--- %s define\n",defs->conf_file);

	FILE *fp = fopen(defs->conf_file,"r");
	size_t r_lng;
	char i_buff[256];
	char wk[128];
	if(fp != NULL){
		while(fgets(i_buff,sizeof(i_buff),fp) != NULL){
			rtrim(i_buff,0x0a);
			rtrim(i_buff,0x0d);
			rtrim(i_buff,' ');
			if(str_startsWith(i_buff,"http_port=")==0){
				strcpy(wk,i_buff+10);
				defs->HTTP_PORT = atoi(wk);
				printf("http_port=%d\n",defs->HTTP_PORT);
			}
			else if(str_startsWith(i_buff,"timeout=")==0){
				strcpy(wk,i_buff+8);
				defs->timeout = atoi(wk);
				printf("timeout=%d\n",defs->timeout);
			}
			else if(str_startsWith(i_buff,"maxconnect=")==0){
				strcpy(wk,i_buff+11);
				defs->maxConnect = atoi(wk);
				//defs->maxThread_cnt= (defs->maxConnect)+1;
				defs->maxThread_cnt= (defs->maxConnect);	// 試験用に+1しません。
				printf("maxconnect=%d\n",defs->maxConnect);
			}
			else if(str_startsWith(i_buff,"doc_rootdir=")==0){
				defs->doc_rootDir=get_strcpy(i_buff+12);
				printf("doc_rootdir=%s\n",defs->doc_rootDir);
			}
			else if(str_startsWith(i_buff,"cgi_rootdir=")==0){
				defs->cgi_rootDir=get_strcpy(i_buff+12);
				printf("cgi_rootdir=%s\n",defs->cgi_rootDir);
			}
			else if(str_startsWith(i_buff,"ccgi_rootdir=")==0){
				defs->ccgi_rootDir=get_strcpy(i_buff+13);
				printf("ccgi_rootdir=%s\n",defs->ccgi_rootDir);
			}
			else if(str_startsWith(i_buff,"allow_networks=")==0){
				defs->allow_networks=get_strcpy(i_buff+15);
				printf("allow_networks=%s\n",defs->allow_networks);
			}
			// class_rootdir= add by nishi 2015.10.27
			else if(str_startsWith(i_buff,"class_rootdir=")==0){
				defs->class_rootDir=get_strcpy(i_buff+14);
				printf("class_rootDir=%s\n",defs->class_rootDir);
			}
			// class_rootdir= add by nishi 2015.11.11
			else if(str_startsWith(i_buff,"class_moddir=")==0){
				defs->class_modDir=get_strcpy(i_buff+13);
				printf("class_modDir=%s\n",defs->class_modDir);
			}
			// class_path= add by nishi 2015.11.15
			else if(str_startsWith(i_buff,"class_path=")==0){
				defs->class_path=get_strcpy(i_buff+11);
				printf("class_path=%s\n",defs->class_path);
			}
		}
		fclose(fp);

		checkAllowNetworks_init(defs->allow_networks);
	}
	else{
		printf("serverConfig():#90 %s open error\n",defs->conf_file);

	}
}
