/*
 * checkAllowNetworks.h
 *
 *  Created on: 2014/05/10
 *      Author: nishi
 */
#include <stdbool.h>

#ifndef CHECKALLOWNETWORKS_H_
#define CHECKALLOWNETWORKS_H_

void checkAllowNetworks_init(char *ip_s);
void checkAllowNetworks_add(char *a_ip,char *a_msk);
bool checkAllowNetworks_a_to_ip(unsigned long int *ip_r,char *ip_s);
void checkAllowNetworks_ip_to_a(char *ip_s,unsigned long int ip_b,bool thread_f);
bool checkAllowNetworks_chek_ip(unsigned long int ip_s);
bool checkAllowNetworks_chek_ip_asc(char *ip_s);

typedef struct _allowNet AllowNet;
struct _allowNet{
	unsigned long int ip;	// ip 4byte, network byte order
	unsigned long int msk;	// mask 4byte, network byte order
	unsigned long int net;	// network 4byte, network byte order
};

#endif /* CHECKALLOWNETWORKS_H_ */
