/*
 * requestDispatch.h
 *
 *  Created on: 2014/03/15
 *      Author: nishi
 */
#ifndef REQUESTDISPATCH_H_
#define REQUESTDISPATCH_H_

typedef struct _disparch_param Dispatch_parm;
struct _disparch_param {
    int fd;
    int seq_i;
};

void *requestDispatch(void *arg);

#endif /* REQUESTDISPATCH_H_ */
