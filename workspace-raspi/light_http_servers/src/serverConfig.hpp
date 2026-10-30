#ifndef SERVERCONFIG_H_
#define SERVERCONFIG_H_

#include <string>

typedef struct _defs Defs;
struct _defs{
  int timeout;
  int maxconnect;
  std::string allow_networks;  //192.168.1.0/255.255.255.0
  std::string http_host="192.168.1.170";     // "192.168.1.170"
  std::string server_name="192.168.1.170";   // "192.168.1.170"
  int server_port;   //80
  std::string web_root;  //home/nishi/www/kyusyupost/html
  std::string cgi_root;  //home/nishi/netosa/www/cgi-bin
  std::string larable_index="";  // larable "index.php" へ、redirect する時指定する。
  std::string cgi_error_log ="on";
  std::string cgi_log_path ="./cgi_error.log";
};

//void init_serverConfig(Defs *defs);
int serverConfig(Defs &defs);

#endif /* SERVERCONFIG_H_ */
