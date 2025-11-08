/*
 * httpReader.h
 *
 *  Created on: 2014/03/15
 *      Author: nishi
 */

#ifndef HTTPREADER_H_
#define HTTPREADER_H_

char *rq_malloc(StrBuff *strBuff,size_t len);
void rq_free_prev(StrBuff *strBuff);
char *rq_strcpy(StrBuff *strBuff,char *str);
char *rq_strncpy(StrBuff *strBuff,char *str,int lng);
char *rq_strcat(StrBuff *strBuff,char *str1,char *str2);

ssize_t read_act (int cnt,int fd, void *buf, int nbytes);
void httpReader(HttpReq *httpReq);
void init_httpReq(HttpReq *phttpReq);
void timer_httpReq(HttpReq *httpReq);
void free_httpReq(HttpReq *phttpReq);
HttpReq *get_free_httpReq(HttpReq **phttpReq_pool);

#endif /* HTTPREADER_H_ */
