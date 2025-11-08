/*
 * serverConfig.h
 *
 *  Created on: 2014/03/16
 *      Author: nishi
 */

#include <pthread.h>

#ifndef SERVERCONFIG_H_
#define SERVERCONFIG_H_

void init_serverConfig(Defs *defs);
void serverConfig(Defs *defs);

#endif /* SERVERCONFIG_H_ */

#ifndef SERVERCONFIG_DEF
extern Defs defs;
extern pthread_mutex_t mutex_mem;
#endif


