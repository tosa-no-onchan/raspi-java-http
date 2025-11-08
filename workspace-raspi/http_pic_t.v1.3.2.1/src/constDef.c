/*
 * constDef.c
 *
 *  Created on: 2014/03/29
 *      Author: nishi
 */

/*
 * HTTP Header の定義
 */
char http_ok[] = "HTTP/1.1 200 OK\r\n";
char htttp_er_404[]="HTTP/1.1 404 internal server error\r\n";

char http_ctype_html[]="Content-type: text/html\r\n\r\n"; // 注）最後は \r\nが２個必要

// apache header sample
//Date: Tue, 06 May 2014 09:56:42 GMT
//Server: Apache
//Keep-Alive: timeout=5, max=500
//Connection: Keep-Alive
//Transfer-Encoding: chunked
//Content-Type: text/html

char http_server[] = "Server: raspberry-pi\r\n";
char http_date[] = "Date: ";
char http_range[] = "Accept-Ranges: bytes\r\n";
char http_keep_alive[] ="Keep-Alive: timeout=5, max=500\r\n";
char http_con_keep_alive[] ="Connection: Keep-Alive\r\n";

/*
 * ENV の定義
 */
char env_req_method[]="REQUEST_METHOD=";
char env_query_str[]="QUERY_STRING=";
char env_cont_type[]="CONTENT_TYPE=";
char env_cont_lng[]="CONTENT_LENGTH=";
char env_cookie[]="HTTP_COOKIE=";
char env_srv_lang[]="SERVER_LANGUAGE=C";
