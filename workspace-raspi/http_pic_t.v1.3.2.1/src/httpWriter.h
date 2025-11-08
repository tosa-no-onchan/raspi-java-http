/*
 * httpWriter.h
 *
 *  Created on: 2014/03/26
 *      Author: nishi
 */
#include <stdbool.h>

#ifndef HTTPWRITER_H_
#define HTTPWRITER_H_


int httpWriterOK(HttpReq *httpReq,char *contentType,bool img_f,int f_size,time_t *tm);
int httpWriterOKcgi(HttpReq *httpReq);


#endif /* HTTPWRITER_H_ */
