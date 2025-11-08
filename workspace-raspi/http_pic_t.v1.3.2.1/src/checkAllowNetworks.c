/*
 * checkAllowNetworks.c
 *
 *  Created on: 2014/05/10
 *      Author: nishi
 */
#include <pthread.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdbool.h>
#include <netinet/in.h>


#include "com_lib.h"
#include "tableDef.h"
#include "serverConfig.h"
#include "checkAllowNetworks.h"


AllowNet *allowNetPool;
int allowNetp_cnt;

/*
 * checkAllowNetworks_init("192.168.1.0/255.255.255.0,192.168.3.0/255.255.255.0");
 */
void checkAllowNetworks_init(char *ip_s){
	Msplit_list *msp;
	char *ipx;

	char *sp;

	char a_ip[50];
	char a_msk[50];
	int lng;

	allowNetPool=NULL;
	allowNetp_cnt=0;

	// ',' で分割します
	msp=msplit(ip_s, ",");

	// ネットワークのフィールドを処理します
	ipx=msplit_pop(msp);
	while(ipx != NULL){
		// ip とマスクに分割します
		sp=strstr_last(ipx,"/");
		lng=sp-ipx;
		strncpy(a_ip,ipx,lng);
		a_ip[lng]=0x00;
		lng=strlen(ipx)-lng-1;
		strncpy(a_msk,sp+1,lng);
		a_msk[lng]=0x00;

		//printf("checkAllowNetworks_init() :#2 a_ip=%s,a_msk=%s\n",a_ip,a_msk);
		checkAllowNetworks_add(a_ip,a_msk);
		ipx=msplit_pop(msp);
	}
	msplit_free(msp);
	//printf("checkAllowNetworks_init() :#90 end!!\n");
}
/*
 * void checkAllowNetworks_add(char *a_ip,char *a_msk)
 */
void checkAllowNetworks_add(char *a_ip,char *a_msk){
	AllowNet allowNet,*allowNetp;
	bool rc;
	int m_size;
	unsigned long int ip;

	//printf("checkAllowNetworks_add():#1 start\n");
	rc=checkAllowNetworks_a_to_ip(&ip,a_ip);
	if(rc==true){
		allowNet.ip=htonl(ip);		// network byte order
		//printf("checkAllowNetworks_add():#3 allowNet.ip=%x\n",allowNet.ip);
		rc=checkAllowNetworks_a_to_ip(&ip,a_msk);
		if(rc==true){
			allowNet.msk=htonl(ip);	// network byte order
			//printf("checkAllowNetworks_add():#4 allowNet.msk=%x\n",allowNet.msk);
			allowNet.net=allowNet.ip & allowNet.msk;
			m_size=sizeof(AllowNet) * (allowNetp_cnt+1);
			allowNetp=malloc(m_size);
			if(allowNetp_cnt > 0){
				memcpy(allowNetp,allowNetPool,sizeof(AllowNet)*allowNetp_cnt);
				memcpy(&allowNetp[allowNetp_cnt],&allowNet,sizeof(AllowNet));
				free(allowNetPool);
			}
			else{
				memcpy(allowNetp,&allowNet,sizeof(AllowNet));
			}
			allowNetPool=allowNetp;
			allowNetp_cnt++;
		}
	}
}
/*
 * bool checkAllowNetworks_a_to_ip(unsigned int *ip_r,char *ip_s)
 * convert ip string to ip binarry
 */
bool checkAllowNetworks_a_to_ip(unsigned long int *ip_r,char *ip_s){
	unsigned long int ip,ipx;
	int i;
	bool ok=true;

	char *p;
	char ip_wk[10];
	int lng;

	ip=0;
	for(i=0;i<4;i++){
		//printf("checkAllowNetworks_conv():#2 i=%d\n",i);
		p = strchr(ip_s, '.');
		//printf("checkAllowNetworks_conv():#3 p=%x\n",p);
		if(p != NULL){
			lng=p-ip_s;
			if(lng > 0){
				strncpy(ip_wk, ip_s,lng);
				ip_wk[lng]=0x00;
				ipx=atol(ip_wk);
				ip=ip * 256 + ipx;
				ip_s+=(lng+1);
			}
		}
		else if(strlen(ip_s)>0){
			ipx=atol(ip_s);
			ip=ip * 256 + ipx;
			ip_s=ip_s+strlen(ip_s);
		}
		else{
			ok=false;
			break;
		}
	}
	if(ok==true){
		*ip_r=ip;
	}
	return ok;
}

/*
 * void checkAllowNetworks_ip_to_a(char *ip_s,unsigned long int ip_b,bool thread_f)
 * convert ip binarry to ip string
 */
void checkAllowNetworks_ip_to_a(char *ip_s,unsigned long int ip_b,bool thread_f){
	unsigned int ip[4];
	int i;
	for(i=0;i<4;i++){
		ip[i]= ip_b % 255;
		ip_b = ip_b >> 8;
	}
	if(thread_f==true){
		if(pthread_mutex_lock(&mutex_mem)!=0){
			printf("checkAllowNetworks_ip_to_a() :#2 pthread_mutex_lock error \n");
			return;
		}
		sprintf(ip_s,"%d.%d.%d.%d",ip[3],ip[2],ip[1],ip[0]);
		if(pthread_mutex_unlock(&mutex_mem)!=0){
			printf("checkAllowNetworks_ip_to_a() :#3 pthread_mutex_unlock error\n");
		}
	}
	else{
		sprintf(ip_s,"%d.%d.%d.%d",ip[3],ip[2],ip[1],ip[0]);
	}
}

/*
 * checkAllowNetworks_chek_ip(unsigned long int ip_s)
 * ip_s : チェックIP(network byte order)
 */
bool checkAllowNetworks_chek_ip(unsigned long int ip_s){
	bool rc=false;
	unsigned long int net_r;
	AllowNet *allowNetPoolx=allowNetPool;
	int i=0;
	for(i=0;i<allowNetp_cnt;i++){
		net_r=allowNetPoolx->msk & ip_s;
		if(net_r == allowNetPoolx->net){
			rc=true;
			break;
		}
		allowNetPoolx++;
	}
	return rc;
}
/*
 * checkAllowNetworks_chek_ip_asc(char *ip_s)
 */
bool checkAllowNetworks_chek_ip_asc(char *ip_s){
	bool rc=false;
	unsigned long int ip_r,net_r;
	int i=0;
	if(checkAllowNetworks_a_to_ip(&ip_r,ip_s)==true){
		for(i=0;i<allowNetp_cnt;i++){
			net_r=allowNetPool[i].msk & ip_r;
			if(net_r == allowNetPool[i].net){
				rc=true;
				break;
			}
		}
	}
	else{
		printf("checkAllowNetworks_chek_ip():#3 ip convert error ip_s=%s\n",ip_s);
	}
	return rc;
}
