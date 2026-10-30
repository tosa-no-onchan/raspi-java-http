#include <iostream>
#include <fstream>
#include <string>
#include <thread>
#include <mutex>
#include <atomic>
#include <chrono>
#include <unordered_map>
#include <unordered_set>
#include <vector>
#include <sstream>
#include <algorithm>
#include <filesystem>
#include <memory>
#include <csignal>
#include <cstring>
#include <cstdlib>
#include <cerrno>
#include <string_view>

// POSIX includes
#include <sys/socket.h>
#include <sys/stat.h>
#include <sys/wait.h>
#include <sys/select.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <fcntl.h>
#include <pwd.h>

#include <sys/ioctl.h>
#include <unistd.h> // close(), read(), write() などのシステムコール用

#include "constDef.hpp"
#include "com_lib.hpp"
#include "serverConfig.hpp"

using namespace std;
namespace fs = std::filesystem;

Defs defs;

// Configuration constants
//constexpr int SERVER_PORT = 8080;
//constexpr int SERVER_PORT = 80;

constexpr const char* SERVER_NAME = "SecureHTTP/1.1";
constexpr size_t MAX_REQUEST_SIZE = 8192;  // 8KB max request
constexpr size_t MAX_BODY_SIZE = 1024 * 1024;  // 1MB max body
constexpr size_t MAX_FILE_SIZE = 10 * 1024 * 1024;  // 10MB max file
//constexpr int MAX_CONCURRENT_THREADS = 100;
constexpr int REQUEST_TIMEOUT_SECONDS = 5;
constexpr int MAX_CONNECTIONS_PER_IP = 10;

// Global thread management
atomic<int> active_threads{0};
mutex connections_mutex;
unordered_map<string, int> ip_connections;

// MIME type mappings
const unordered_map<string, string> mime_types = {
    {".html", "text/html"},
    {".htm", "text/html"},
    {".css", "text/css"},
    {".js", "application/javascript"},
    {".json", "application/json"},
    {".png", "image/png"},
    {".jpg", "image/jpeg"},
    {".jpeg", "image/jpeg"},
    {".gif", "image/gif"},
    {".svg", "image/svg+xml"},
    {".ico", "image/x-icon"},
    {".txt", "text/plain"},
    {".pdf", "application/pdf"},
    {".xml", "application/xml"}
};

// HTTP status codes
const unordered_map<int, string> status_messages = {
    {200, "OK"},
    {400, "Bad Request"},
    {403, "Forbidden"},
    {404, "Not Found"},
    {405, "Method Not Allowed"},
    {413, "Payload Too Large"},
    {414, "URI Too Long"},
    {500, "Internal Server Error"},
    {503, "Service Unavailable"}
};

#define HttpBuff_a_LineSize 256
#define HttpBuff_i_buffSize 512
#define HttpReq_max_header 20
#define HttpReq_buf_Size 4096	// cgi からの受信バッファサイズ
#define HttpReq_str_Size 1024

/*
 * StrBuff
 *  rq_malloc() で使用します
 */
typedef struct _strBuff StrBuff;
struct _strBuff{
	int lng;	// 割り当て済データサイズ
	int mxlmg;	// 割り当て可能データサイズ
	int prev;	//直前の割り当てデータサイズ
	char *str;	//　割り当てデータ領域
};

/*
 * HttpBuff
 */
typedef struct _httpBuff HttpBuff;
struct _httpBuff{
	bool isCr;
	bool isEof;
	char a_line[HttpBuff_a_LineSize+1];	// getLine field
	char i_buff[HttpBuff_i_buffSize];	// read buffer
	int cur_p;	// current i_buff pointer
	int r_lng;	// remain i_buff data lng
};

/*
 * WorkBuff
 */
typedef struct _workBuff WorkBuff;
struct _workBuff{
	char wk[HttpReq_buf_Size/2];
	char o_buff[HttpReq_buf_Size/2];
};

struct HttpRequest {
	int sockfd;	// socket file descripter
    string client_ip;
    string method;
    string path;
    string script_name;
    string version;
    unordered_map<string, string> headers;
    string query_string;    // QUERY_STRING
    size_t content_length;
    std::string auth_user;
    std::string auth_pw;

    string body;
    bool valid = false;

	pid_t sub_pid;	// 子プロセスの pid or 0
	int pipe_send;
	int pipe_recv;
	HttpBuff hbuff;	// HttpBuff
	StrBuff sbuff;	// StrBuff
	char sbuff_data[HttpReq_str_Size];	// StrBuff data area
    char buff[HttpReq_buf_Size];	// free buff
};

struct HttpResponse {
    int status_code = 200;
    unordered_map<string, string> headers;
    string body;
    vector<uint8_t> binary_data;
    bool is_binary = false;
    // add by nishi 2026.10.13
    bool is_cgi = false;
};

// Utility functions
void log_error(const string& message) {
    cerr << "[ERROR] " << chrono::system_clock::now().time_since_epoch().count() 
         << ": " << message << endl;
}

void log_info(const string& message) {
    cout << "[INFO] " << chrono::system_clock::now().time_since_epoch().count() 
         << ": " << message << endl;
}

//#include <string>
//#include <vector>
//#include <iostream>

std::string base64_decode(const std::string& in) {
    std::string out;
    std::vector<int> T(256, -1);
    
    // Base64変換テーブルの初期化
    for (int i = 0; i < 64; i++) {
        T["ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"[i]] = i;
    }

    int val = 0, valb = -8;
    for (unsigned char c : in) {
        // スペースや改行、末尾のパディング '=' は無視する
        if (c == '=') break;
        if (T[c] == -1) continue; 

        val = (val << 6) + T[c];
        valb += 6;
        if (valb >= 0) {
            out.push_back(char((val >> valb) & 0xFF));
            valb -= 8;
        }
    }
    return out;
}

/*
 * int readBlock(int sockfd,HttpReq *httpReq)
 * update by nishi 2015.11.7
 * 注1) file upload など、データが大きいときに、ファイルが途中で途切れたり、サイズが一致しない場合は。
 * usleep() の値を大きくして下さい。 2015.11.7 by nishi
 *
 */
int readBlock(int sockfd,HttpRequest &httpReq){
	int nread;
	int rlng=0;
	if(httpReq.hbuff.r_lng>0){
		memcpy(httpReq.buff,&(httpReq.hbuff.i_buff[httpReq.hbuff.cur_p]), httpReq.hbuff.r_lng);
		rlng = httpReq.hbuff.r_lng;
		httpReq.hbuff.r_lng=0;
	}
	//受信可能なデータサイズのチェック
	ioctl(sockfd, FIONREAD, &nread);
	if(nread > 0){
		nread=read(sockfd,&(httpReq.buff[rlng]),sizeof(httpReq.buff)-rlng);
		if(nread>0){
			rlng+=nread;
		}
		return rlng;
	}
	else if(nread < 0){
		return rlng;
	}
	//usleep(100000);	// 100 m sec -> 0.1 sec
	//usleep(50000);	// 50 m sec -> 0.05 sec
	//usleep(25000);	// 25 m sec -> 0.025 sec
	//usleep(10000);	// 10 m sec -> 0.01 sec
	usleep(8000);	// 8 m sec -> 0.008 sec
	//usleep(5000);		// 5 m sec -> 0.005 sec

	ioctl(sockfd, FIONREAD, &nread);
	if(nread >0){
		nread=read(sockfd,&(httpReq.buff[rlng]),sizeof(httpReq.buff)-rlng);
		if(nread>0){
			rlng+=nread;
		}
	}
	//printf("readBlock() : #90 rlng=%d\n",rlng);
	//memdump(httpReq->buff,rlng);
	return rlng;
}

// URL decode function
string url_decode(const string& encoded) {
    string decoded;
    decoded.reserve(encoded.length());
    
    for (size_t i = 0; i < encoded.length(); ++i) {
        if (encoded[i] == '%' && i + 2 < encoded.length()) {
            try {
                int value = stoi(encoded.substr(i + 1, 2), nullptr, 16);
                if (value >= 0 && value <= 255) {
                    decoded += static_cast<char>(value);
                    i += 2;
                } else {
                    decoded += encoded[i];
                }
            } catch (...) {
                decoded += encoded[i];
            }
        } else if (encoded[i] == '+') {
            decoded += ' ';
        } else {
            decoded += encoded[i];
        }
    }
    return decoded;
}

// Sanitize path to prevent directory traversal
string sanitize_path(const string& path) {
    if (path.empty() || path[0] != '/') {
        return "";
    }
    bool cgi_f=false;
    
    // URL decode first
    string decoded = url_decode(path);
    
    // Reject paths with null bytes or other dangerous characters
    if (decoded.find('\0') != string::npos || 
        decoded.find("..") != string::npos ||
        decoded.find('\\') != string::npos) {
        return "";
    }

    cout << "sanitize_path() #2 path:" << path  << endl;

    // Convert to filesystem path
    fs::path requested_path = fs::path(defs.web_root) / decoded.substr(1);
    if (path.starts_with("/cgi-bin/")) {
        requested_path = fs::path(defs.cgi_root) / decoded.substr(9);
        cgi_f=true;
    }

    cout << "sanitize_path() #3 requested_path:" << requested_path  << endl;

    try {
        // Get canonical path to resolve any remaining .. or symlinks
        fs::path canonical;
        if(cgi_f){
            //canonical = fs::canonical(fs::path(defs.cgi_root));
            canonical = fs::absolute(fs::path(defs.cgi_root));
        }
        else{
            //canonical = fs::canonical(fs::path(defs.web_root));
            canonical = fs::absolute(fs::path(defs.web_root));
        }

        fs::path target;
        target = fs::absolute(requested_path);
        
        // Ensure the resolved path is within web root
        string canonical_str = canonical.string();
        string target_str = target.string();
        
        if (target_str.substr(0, canonical_str.length()) != canonical_str) {
            return "";  // Path traversal attempt
        }
        
        return target.string();
    } catch (const fs::filesystem_error&) {
        return "";
    }
}

// Check if file is forbidden (hidden/system files)
bool is_forbidden_file(const string& path) {
    fs::path p(path);
    string filename = p.filename().string();

    std::cout<< "is_forbidden_file():#1 path:"<< path <<" filename:"<< filename << std::endl;
    
    // Reject hidden files and dangerous extensions
    if (filename.empty() || filename[0] == '.' ||
        filename == "Thumbs.db" || filename == "desktop.ini" ||
        path.find("/.") != string::npos) {
        return true;
    }
    
    // Check for system/config file patterns
    static const unordered_set<string> forbidden_patterns = {
        ".htaccess", ".htpasswd", ".git", ".svn", ".env", 
        "web.config", ".DS_Store", "__pycache__"
    };
    
    for (const auto& pattern : forbidden_patterns) {
        if (filename.find(pattern) != string::npos) {
            return true;
        }
    }
    
    return false;
}

// get extension
string get_extension(const string& path){
    fs::path p(path);
    string ext = p.extension().string();
    transform(ext.begin(), ext.end(), ext.begin(), ::tolower);
    return ext;
}

// Get MIME type from file extension
string get_mime_type(const string& path) {
    fs::path p(path);
    string ext = p.extension().string();
    transform(ext.begin(), ext.end(), ext.begin(), ::tolower);
    
    auto it = mime_types.find(ext);
    return (it != mime_types.end()) ? it->second : "application/octet-stream";
}


// 画像ファイルを4096バイトずつ読み込んでソケットに送信する関数
bool send_image_file(int client_socket, const std::string& file_path) {

    string ext=get_extension(file_path);

    if(ext != ".jpg" && ext != ".png" && ext !="jpeg" && ext !="gif"){
        return false;
    }

    std::string mime_type = get_mime_type(file_path);

    // 1. ファイルを「バイナリモード」で開く
    std::ifstream file(file_path, std::ios::binary);
    if (!file.is_open()) {
        std::cerr << "エラー: ファイルが開けません: " << file_path << std::endl;
        return false;
    }

    // 2. まずはHTTPレスポンスヘッダを先に送信する（重要）
    // 画像として認識させるために Content-Type を正しく伝える必要があります
    std::string header = "HTTP/1.1 200 OK\r\n";
    header += "Content-Type: " + mime_type + "\r\n";
    header += "Connection: keep-alive\r\n"; // または close
    header += "\r\n"; // ヘッダの終わりを示す空行
    
    if (send(client_socket, header.c_str(), header.length(), 0) < 0) {
        return false;
    }

    // 3. 【本題】4096バイトのバッファを用意し、ループで読み込み＆送信
    const size_t BUFFER_SIZE = 4096;
    std::vector<char> buffer(BUFFER_SIZE);

    while (file.good()) {
        // ファイルから最大4096バイト読み込む
        file.read(buffer.data(), BUFFER_SIZE);
        std::streamsize bytes_read = file.gcount(); // 実際に読み込めたバイト数

        if (bytes_read > 0) {
            // 読み込めた分だけ、即座にソケットへ送信（out streamへの書き込み）
            std::streamsize bytes_sent = 0;
            while (bytes_sent < bytes_read) {
                ssize_t result = send(client_socket, buffer.data() + bytes_sent, bytes_read - bytes_sent, 0);
                if (result < 0) {
                    // 送信エラー（クライアントが途中で切断したなど）
                    return false;
                }
                bytes_sent += result;
            }
        }
    }

    return true;
}

// Read file with size limit
bool read_file_safe(const string& filepath, vector<uint8_t>& content) {
    std::cout<< "read_file_safe():# 1 filepath:" << filepath << std::endl;
    try {
        ifstream file(filepath, ios::binary);
        if (!file.is_open()) {
            return false;
        }
        
        // Get file size
        file.seekg(0, ios::end);
        size_t file_size = file.tellg();
        file.seekg(0, ios::beg);
        
        if (file_size > MAX_FILE_SIZE) {
            log_error("File too large: " + filepath);
            return false;
        }
        
        content.resize(file_size);
        file.read(reinterpret_cast<char*>(content.data()), file_size);

        std::cout<< "read_file_safe():# 9 passed!!" << std::endl;
        return file.good() || file.eof();
    } 
    catch (...) {
        return false;
    }
}

// 簡易的なヘッダー抽出関数 add by nishi 2025.12.30
std::string get_header_value(const std::string& request_head, const std::string& header_name) {
    std::string search_term = header_name + ": ";
    size_t start = request_head.find(search_term);
    if (start == std::string::npos) return "";

    start += search_term.length();
    size_t end = request_head.find("\r\n", start);
    return request_head.substr(start, end - start);
}


class Exec_proc{
    public:
    Exec_proc(){}

    // Execute PHP script safely
    bool execute_php(const string& script_path, HttpRequest& request) {

        bool first_f=true;
        //bool pipe_f=true;

        char o_buff[512];
        char wk[512];

        std::cout << "execute_php():#1 script_path:"<< script_path << " ,request.path:"<< request.path <<" ,request.script_name:" << request.script_name << std::endl;
        //request.
        // script_path:/home/xxxx/www/local-test/laravelapp/blog/public/index.php

        // Validate PHP file extension and location
        if (!script_path.ends_with(".php")) {
            std::cout << "execute_php():#2 script_path:"<< script_path << std::endl;
            return false;
        }

        //std::cout << "execute_php():#2 script_path.c_str():"<< script_path.c_str()<< std::endl;
        // execute_php():#2 script_path.c_str():/home/nishi/www/toyopet/html/zaiko/user/stock_disp.php
        // Ensure script is within web root
        try {
            //fs::path canonical_root = fs::canonical(defs.web_root);
            fs::path canonical_root = fs::absolute(defs.web_root);
            //fs::path canonical_script = fs::canonical(script_path);
            fs::path canonical_script = fs::absolute(script_path);
            
            if (canonical_script.string().substr(0, canonical_root.string().length()) != canonical_root.string()) {
                std::cout << "execute_php():#3 defs.web_root:"<< defs.web_root << " ,script_path:"<< script_path << std::endl;
                return false;
            }
        } 
        catch (...) {
            std::cout << "execute_php():#4 defs.web_root:"<< defs.web_root << " ,script_path:"<< script_path << std::endl;
            return false;
        }

        //std::cout << "execute_php():#3 script_path.c_str():"<< script_path.c_str()<< " ,request.method.c_str():" << request.method.c_str() << " ,request.path.c_str():" << request.path.c_str() <<std::endl;
        // execute_php():#3 script_path.c_str():/home/nishi/www/toyopet/html/zaiko/user/stock_disp.php 
        //  ,request.method.c_str():GET 
        //  ,request.path.c_str():/zaiko/user/stock_disp.php

        // get current dir. add by nishi 2025.12.30
        size_t slashPos = script_path.find_last_of('/');
        if( slashPos == std::string::npos){
            std::cout << "execute_php():#5 script_path:"<< script_path << std::endl;
            return false;
        }
        string cur_dir=script_path.substr(0,slashPos);
        string script=script_path.substr(slashPos+1);

        //std::cout << "execute_php():#4 cur_dir:"<< cur_dir << " ,script:" << script <<std::endl;

        // Create pipes for communication
        //int pipe_fd[2];

        int pipe_send[2];	// send to CGI pipe
        int pipe_recv[2];	// receive from CGI pipe
        pid_t fork_result;

        if (pipe(pipe_send) == -1) {
            log_error("Failed to create pipe_send: " + string(strerror(errno)));
            return false;
        }
        else if(pipe(pipe_recv)!=0){
            log_error("Failed to create pipe_rev: " + string(strerror(errno)));
            return false;
        }
        
        fork_result = fork();
        if (fork_result == (pid_t)-1) {
            log_error("Failed to fork: " + string(strerror(errno)));
            close(pipe_send[0]);
            close(pipe_send[1]);
            close(pipe_recv[0]);
            close(pipe_recv[1]);
            return false;
        }
        /*--------
        * 子プロセスです
        ---------*/
        if (fork_result == (pid_t)0) {
            // Child process
            // Set environment variables
            setenv("REQUEST_METHOD", request.method.c_str(), 1);    // PHP側では $_SERVER['REQUEST_METHOD'] になる
            setenv("SCRIPT_FILENAME", script_path.c_str(), 1);      // $_SERVER['SCRIPT_FILENAME'] になる
            setenv("REQUEST_URI", request.path.c_str(), 1);

            setenv("SCRIPT_NAME", request.script_name.c_str(), 1);      // $_SERVER['SCRIPT_NAME'] になる
            setenv("PHP_SELF", request.script_name.c_str(), 1);      // $_SERVER['PHP_SELF'] になる

            string app_url_s = "http://"+defs.http_host;
            setenv("APP_URL",app_url_s.c_str(),1);

            //HTTP_CONNECTION ： keep-alive
            //string connection = get_header_value(request_head, "Connection");
            string connection = request.headers["connection"];
            setenv("HTTP_CONNECTION", connection.c_str(), 1);

            //$PHP_AUTH_USER=(isset($_SERVER['PHP_AUTH_USER']))?$_SERVER['PHP_AUTH_USER']:'';
            //$PHP_AUTH_PW=(isset($_SERVER['PHP_AUTH_PW']))?$_SERVER['PHP_AUTH_PW']:'';
            //setenv("PHP_AUTH_USER", "", 1);
            //setenv("PHP_AUTH_PW", "", 1);

            // add by nishi 2025.12.30
            // C++側での記述例
            //setenv("REQUEST_METHOD", "GET", 1);      // PHP側では $_SERVER['REQUEST_METHOD'] になる
            //setenv("SCRIPT_FILENAME", "/path/to/script.php", 1); // $_SERVER['SCRIPT_FILENAME'] になる
            // "User-Agent:" の中身を取り出す add by nishi 2025.12.30
            //string user_agent = get_header_value(request_head, "User-Agent");
            string user_agent = request.headers["user-agent"];

            //std::cout << "parse_request() :#5 user_agent:" << request.user_agent << std::endl;
            //string  authenticate = get_header_value(raw_request,"WWW-authenticate");
            setenv("HTTP_USER_AGENT", user_agent.c_str(), 1);      // $_SERVER['HTTP_USER_AGENT'] になる

            //string accept_lang = get_header_value(request_head, "Accept-Language");
            string accept_lang = request.headers["accept-language"];
            setenv("HTTP_ACCEPT_LANGUAGE", accept_lang.c_str(), 1);      // $_SERVER['HTTP_ACCEPT'] になる

            //string accept = get_header_value(request_head, "Accept");
            string accept = request.headers["accept"];
            setenv("HTTP_ACCEPT", accept.c_str(), 1);      // $_SERVER['HTTP_ACCEPT'] になる

            //string accept_encoding = get_header_value(request_head, "Accept-Encoding");
            string accept_encoding = request.headers["accept-encoding"];
            setenv("HTTP_ACCEPT_ENCODING", accept_encoding.c_str(), 1);      // $_SERVER['HTTP_ACCEPT'] になる

            //string cookie = get_header_value(request_head, "Cookie");
            string cookie = request.headers["cookie"];
            setenv("HTTP_COOKIE", cookie.c_str(), 1);      // $_SERVER['HTTP_ACCEPT'] になる

            // add Content-Type by nishi 2026.10.12
            //string content_type = get_header_value(request_head, "Content-Type");
            string content_type = request.headers["content-type"];
            setenv("CONTENT_TYPE", content_type.c_str(), 1); 

            // QUERY_STRING が、ある
            if(request.query_string != ""){
                setenv("QUERY_STRING", request.query_string.c_str(), 1); 
            }
            // ⚠️ PHP CGIに必須の環境変数（これが無いと「Security Alert!」というエラーで動かない）
            setenv("REDIRECT_STATUS", "200", 1); 
            setenv("CONTENT_LENGTH", to_string(request.content_length).c_str(), 1);

            // PHPを実行するプロセスに環境変数を引き渡す
            setenv("HTTP_HOST", defs.http_host.c_str(), 1); // "192.168.1.170"
            setenv("SERVER_NAME", defs.server_name.c_str(), 1); // "192.168.1.170"
            std::string port_s = std::to_string(defs.server_port);
            setenv("SERVER_PORT", port_s.c_str(), 1); // 自作サーバーのポート番号: "80"

            // 認証がある!!
            if(request.headers["authorization"] !=""){
                // 🚀 PHP-CGIが認識できるように環境変数へセット！
                setenv("PHP_AUTH_USER", request.auth_user.c_str(), 1);
                setenv("PHP_AUTH_PW",   request.auth_pw.c_str(), 1);
            }

            // 環境変数をチェック
            //check_all_env();

            // execされるプロセスのstdin,stdout を pipe_send[0],pipe_recv[1] に接続
            close(0);  // stdin assign
            dup(pipe_send[0]);
            close(pipe_send[0]);
            close(pipe_send[1]);

            close(1);	// stdout assign
            dup(pipe_recv[1]);
            close(pipe_recv[0]);
            close(pipe_recv[1]);

            // change exec dir to current dir. add by nishi 2025.12.30
            if(chdir(cur_dir.c_str())!=0){
                //log_error("Failed to chdir !!");
                exit(1);  // execl failed
            }

            // Execute PHP
            //auto ex_rc = execl("/usr/bin/php", "php", "-f", script_path.c_str(), nullptr);
            string script_x = "./"+script;
            auto ex_rc = execl("/usr/bin/php-cgi", "php-cgi", script_x.c_str(), nullptr);
            //auto ex_rc = execl("/usr/bin/php-cgi", "php-cgi", script_path.c_str(), nullptr);
            // changed by nishi 2025.12.29
            //auto ex_rc = execl("/usr/bin/php", "php" , "-f", script.c_str(), nullptr);
            //auto ex_rc=execl(script.c_str(),0);
            if(ex_rc==-1){
                // send error response
                exit(1);  // execl failed
            }
            exit(0);
        }
        /*--------
        * 親プロセスです
        ---------*/
        else {
            #define _SILENT
            #ifndef _SILENT
                printf("execute_php() :#24 exec sub proc=%d,pipe_send[1]=%d,pipe_recv[0]=%d\n",fork_result,pipe_send[1],pipe_recv[0]);
            #endif
            // Parent process
            request.sub_pid=fork_result;
            //close(pipe_fd[1]);  // Close write end
            close(pipe_send[0]);
            close(pipe_recv[1]);

            request.pipe_send=pipe_send[1];
            request.pipe_recv=pipe_recv[0];

            int nread;
            //int result;
            //int fd;

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
            ssize_t sl=strlen(o_buff);
            //---- end ----

            //size_t sl=strlen(o_buff);
            //int lpc=0;

            // Read output with timeout
            //fd_set read_fds;
            //struct timeval timeout;
            //timeout.tv_sec = 5;  // 5 second timeout
            //timeout.tv_usec = 0;
            
            // GET method
            if(request.method == "GET"){
                #ifndef _SILENT
                    // cgiへの出力 を行いません
                    printf("execute_php() :#25 close pipe_send[1]!!\n");
                #endif
                // cgiへの入力をクローズ
                close(pipe_send[1]);
                request.pipe_send=0;
            }
            else{
                // cgi へリダイレクトする  httpReq->sockfd -> pipe_send[1]
                while(1){
                    //printf("cgiProcessor() :#30 readBlock() call!!\n");
                    nread=readBlock(request.sockfd,request);
                    //printf("cgiProcessor() :#31 readBlock() return!!\n");
                    if(nread > 0){
                        #ifndef _SILENT
                            printf("execute_php() :#32 request from Client nread=%d\n",nread);
                            //printf("execute_cgi() :#32.1 req-data=%.*s\n",nread,httpReq->buff);
                        #endif
                        if(write(pipe_send[1],request.buff,nread)!=nread){
                            printf("execute_php() :#31  write error\n");
                            break;
                        }
                        //printf("execute_cgi() :#33 write to cgi one record end\n");
                    }
                    else{
                        break;
                    }
                }
                #ifndef _SILENT
                    printf("execute_php() :#34 write to cgi all end\n");
                    printf("execute_php() :#35 close pipe_send[1]=%d\n",pipe_send[1]);
                #endif
                // cgiへの入力をクローズ
                close(pipe_send[1]);
                request.pipe_send=0;
            }
            // cgi からの出力を受け取る   httpReq->sockfd  <-  pipe_recv[0]
            while(1){
                // 注)read() は、データが上がってくるか。close されるまで待つ。
                // cgi側が、closeしないと、制御が戻ってこないので注意。
                // 将来は、上位で、全体のタイム監視機能を組み込む必要があります。
                nread=read(pipe_recv[0],request.buff,sizeof(request.buff));
                #ifndef _SILENT
                    printf("execute_php() :#46 read from cgi nread=%d\n",nread);
                    //printf("cgiProcessor() :#46.1 recv-data=%.*s\n",nread,httpReq->buff);
                #endif
                if(nread > 0){
                    // 最初は、 基本認証の STATUS があるかも知れないから、それを、処理する。
                    if(first_f==true){
                        #ifndef _SILENT
                            printf("execute_php() :#47 write http header\n");
                        #endif
                        // 1. buff を string に変換
                        std::string cgi_output(request.buff, nread);
                        std::string final_response = "";
                        // 2. "Status: " 行を探す
                        auto status_pos = cgi_output.find("Status: ");
                        if (status_pos != std::string::npos) {
                            // "Status: " の後ろにある文字列の行末（改行）を探す
                            auto line_end = cgi_output.find("\n", status_pos);
                            
                            if (line_end != std::string::npos) {
                                // "Status: 401 Unauthorized" などの部分を抽出
                                // "Status: " の文字数（8文字）分ずらしたところから、行末まで
                                std::string status_val = cgi_output.substr(status_pos + 8, line_end - (status_pos + 8));
                                
                                // 改行コード（\r）が残っていれば削る
                                if (!status_val.empty() && status_val.back() == '\r') {
                                    status_val.pop_back();
                                }
                                // 正しい HTTP ヘッダーを組み立てる
                                final_response += "HTTP/1.1 " + status_val + "\r\n";

                                //printf("execute_php() :#48  final_response:%s \n",final_response);
                                //std::cout << "execute_php():#48 final_response:"<< final_response <<std::endl;

                                // cgi_output から元の "Status: ... \n" の行を削除する
                                cgi_output.erase(status_pos, line_end - status_pos + 1);
                            }
                        } 
                        else {
                            // Status 行が無い通常のアクセスの場合は 200 OK を補う
                            final_response += "HTTP/1.1 200 OK\r\n";
                        }
                        // 3. 残りのヘッダーとボディを連結
                        final_response += cgi_output;
                        // 4. ブラウザへ送信
                        //send(client_socket, final_response.c_str(), final_response.length(), 0);
                        sl = final_response.length();
                        if(write(request.sockfd,final_response.c_str(), sl) != sl){
                        //if(write(request.sockfd,o_buff,sl) != sl){
                            printf("execute_php() :#48  write error \n");
                            break;
                        }
                        first_f=false;
                    }
                    else{
                        if(write(request.sockfd,request.buff,nread) != nread){
                            printf("execute_php() :#49  write error \n");
                            break;
                        }
                    }
                }
                else if(nread == 0){
                    break;
                }
                else{
                    printf("execute_php() :#50 pipe read error end!!\n");
                    break;
                }
            }
            #ifndef _SILENT
                printf("execute_php() :#51 read end,close pipe_recv[0]=%d\n",pipe_recv[0]);
            #endif
            // cgiからの出力をクローズ
            close(pipe_recv[0]);
            request.pipe_recv=0;

            // Wait for child process
            int status;
            waitpid(request.sub_pid, &status, 0);
            //return WIFEXITED(status) && WEXITSTATUS(status) == 0;
            //changed by nishi 2026.10.19
            if((WIFEXITED(status) && WEXITSTATUS(status)) == 0){
                return true;
            }
            return false;
        }
    }

    // Execute cgi script safely
    bool execute_cgi(const string& script_path, HttpRequest& request) {

        bool first_f=true;
        //bool pipe_f=true;
        bool rc=true;

        char o_buff[512];
        char wk[512];

        std::cout << "execute_cgi():#1 script_path.c_str():"<< script_path.c_str()<< std::endl;
        // Validate PHP file extension and location
        if (!script_path.ends_with(".cgi") && !script_path.ends_with(".pl")) {
            return false;
        }
        //std::cout << "execute_php():#2 script_path.c_str():"<< script_path.c_str()<< std::endl;
        // execute_php():#2 script_path.c_str():/home/nishi/www/toyopet/html/zaiko/user/stock_disp.php
        // Ensure script is within web root
        try {
            //fs::path canonical_root = fs::canonical(defs.cgi_root);
            fs::path canonical_root = fs::absolute(defs.cgi_root);
            //fs::path canonical_script = fs::canonical(script_path);
            fs::path canonical_script = fs::absolute(script_path);
            
            if (canonical_script.string().substr(0, canonical_root.string().length()) != canonical_root.string()) {
                return false;
            }
        } 
        catch (...) {
            return false;
        }

        //std::cout << "execute_php():#3 script_path.c_str():"<< script_path.c_str()<< " ,request.method.c_str():" << request.method.c_str() << " ,request.path.c_str():" << request.path.c_str() <<std::endl;
        // execute_php():#3 script_path.c_str():/home/nishi/www/toyopet/html/zaiko/user/stock_disp.php 
        //  ,request.method.c_str():GET 
        //  ,request.path.c_str():/zaiko/user/stock_disp.php

        // get current dir. add by nishi 2025.12.30
        size_t slashPos = script_path.find_last_of('/');
        if( slashPos == std::string::npos){
            return false;
        }
        string cur_dir=script_path.substr(0,slashPos);
        string script=script_path.substr(slashPos+1);

        std::cout << "execute_cgi():#4 cur_dir:"<< cur_dir << " ,script:" << script <<std::endl;

        // Create pipes for communication
        //int pipe_fd[2];

        int pipe_send[2];	// send to CGI pipe
        int pipe_recv[2];	// receive from CGI pipe
        pid_t fork_result;

        if (pipe(pipe_send) == -1) {
            log_error("Failed to create pipe_send: " + string(strerror(errno)));
            return false;
        }
        else if(pipe(pipe_recv)!=0){
            log_error("Failed to create pipe_rev: " + string(strerror(errno)));
            return false;
        }
        
        fork_result = fork();
        if (fork_result == (pid_t)-1) {
            log_error("Failed to fork: " + string(strerror(errno)));
            close(pipe_send[0]);
            close(pipe_send[1]);
            close(pipe_recv[0]);
            close(pipe_recv[1]);
            return false;
        }
        /*--------
        * 子プロセスです
        ---------*/
        if (fork_result == (pid_t)0) {
            // Child process
            
            // Set environment variables
            setenv("REQUEST_METHOD", request.method.c_str(), 1);    // PHP側では $_SERVER['REQUEST_METHOD'] になる
            setenv("SCRIPT_FILENAME", script_path.c_str(), 1);      // $_SERVER['SCRIPT_FILENAME'] になる
            setenv("REQUEST_URI", request.path.c_str(), 1);

            setenv("REMOTE_HOST", request.client_ip.c_str(), 1);

            //HTTP_CONNECTION ： keep-alive
            //string connection = get_header_value(request_head, "Connection");
            string connection = request.headers["connection"];
            setenv("HTTP_CONNECTION", connection.c_str(), 1);

            //$PHP_AUTH_USER=(isset($_SERVER['PHP_AUTH_USER']))?$_SERVER['PHP_AUTH_USER']:'';
            //$PHP_AUTH_PW=(isset($_SERVER['PHP_AUTH_PW']))?$_SERVER['PHP_AUTH_PW']:'';
            //setenv("PHP_AUTH_USER", "", 1);
            //setenv("PHP_AUTH_PW", "", 1);

            // add by nishi 2025.12.30
            // C++側での記述例
            //setenv("REQUEST_METHOD", "GET", 1);      // PHP側では $_SERVER['REQUEST_METHOD'] になる
            //setenv("SCRIPT_FILENAME", "/path/to/script.php", 1); // $_SERVER['SCRIPT_FILENAME'] になる
            // "User-Agent:" の中身を取り出す add by nishi 2025.12.30
            //string user_agent = get_header_value(request_head, "User-Agent");
            string user_agent = request.headers["user-agent"];

            //std::cout << "parse_request() :#5 user_agent:" << request.user_agent << std::endl;
            //string  authenticate = get_header_value(raw_request,"WWW-authenticate");
            setenv("HTTP_USER_AGENT", user_agent.c_str(), 1);      // $_SERVER['HTTP_USER_AGENT'] になる

            //string accept_lang = get_header_value(request_head, "Accept-Language");
            string accept_lang = request.headers["accept-language"];
            setenv("HTTP_ACCEPT_LANGUAGE", accept_lang.c_str(), 1);      // $_SERVER['HTTP_ACCEPT'] になる

            //string accept = get_header_value(request_head, "Accept");
            string accept = request.headers["accept"];
            setenv("HTTP_ACCEPT", accept.c_str(), 1);      // $_SERVER['HTTP_ACCEPT'] になる

            //string accept_encoding = get_header_value(request_head, "Accept-Encoding");
            string accept_encoding = request.headers["accept-encoding"];
            setenv("HTTP_ACCEPT_ENCODING", accept_encoding.c_str(), 1);      // $_SERVER['HTTP_ACCEPT'] になる

            //string cookie = get_header_value(request_head, "Cookie");
            string cookie = request.headers["cookie"];
            setenv("HTTP_COOKIE", cookie.c_str(), 1);      // $_SERVER['HTTP_ACCEPT'] になる

            // add Content-Type by nishi 2026.10.12
            //string content_type = get_header_value(request_head, "Content-Type");
            string content_type = request.headers["content-type"];
            setenv("CONTENT_TYPE", content_type.c_str(), 1); 

            // QUERY_STRING が、ある
            if(request.query_string != ""){
                setenv("QUERY_STRING", request.query_string.c_str(), 1); 
            }
            setenv("CONTENT_LENGTH", to_string(request.content_length).c_str(), 1);

            // execされるプロセスのstdin,stdout を pipe_send[0],pipe_recv[1] に接続
            // [1] 既存の stdin, stdout のパイプ接続
            close(0);  // stdin assign
            dup(pipe_send[0]);
            close(pipe_send[0]);
            close(pipe_send[1]);

            close(1);	// stdout assign
            dup(pipe_recv[1]);
            close(pipe_recv[0]);
            close(pipe_recv[1]);

            // 🚀 【ここを追加・修正！】標準エラー出力（STDERR / 2番）のハンドリング
            close(2);  // 一旦標準エラー出力を閉じる

            // server.conf で CGI ログが有効（ON）に設定されている場合
            if (defs.cgi_error_log == "on") {
                // 追記モード（O_APPEND）かつ、無ければ作成（O_CREAT）でログファイルを開く
                // 権限は 0644 (所有者読み書き、その他読み取り専用)
                int log_fd = open(defs.cgi_log_path.c_str(), O_WRONLY | O_CREAT | O_APPEND, 0644);
                
                if (log_fd != -1) {
                    // 開いたファイル記述子を 2番（STDERR）に複製して割り当てる
                    dup2(log_fd, 2);
                    close(log_fd); // dup2 した後は元の fd は閉じてOK
                } 
                else {
                    // 万が一ログファイルが開けなかった場合は /dev/null に捨てる
                    int null_fd = open("/dev/null", O_WRONLY);
                    dup2(null_fd, 2);
                    close(null_fd);
                }
            } 
            // server.conf で設定が OFF、または指定がない場合
            else {
                // エラーログを一切出さない（虚空へ捨てる）ように /dev/null へリダイレクト
                int null_fd = open("/dev/null", O_WRONLY);
                dup2(null_fd, 2);
                close(null_fd);
            }

            // 🚀 【ここが最重要！】
            // 子プロセス側ではブラウザのソケットを直接触らないので、ここで完全に閉じます。
            // これにより、OSの参照カウンターが正しく減少します。
            close(request.sockfd);

            // change exec dir to current dir. add by nishi 2025.12.30
            if(chdir(cur_dir.c_str())!=0){
                //log_error("Failed to chdir !!");
                exit(1);  // execl failed
            }
            // Execute CGI
            //execl("/usr/bin/php", "php", "-f", script_path.c_str(), nullptr);
            // changed by nishi 2025.12.29
            //auto ex_rc =execl("/usr/bin/perl", "perl" , "-f", script.c_str(), nullptr);
            string script_cur= "./"+script;
            auto ex_rc=execl(script_cur.c_str(),"",0);
            //auto ex_rc=execl(script_cur.c_str(),0);
            if(ex_rc==-1){
                //std::cout << "execute_cgi():#5 ex_rc:"<< ex_rc <<std::endl;
                // send error response
                exit(1);  // execl failed
            }
            //std::cout << "execute_cgi():#6 ex_rc:"<< ex_rc <<std::endl;
            exit(0);
        }
        /*--------
        * 親プロセスです
        ---------*/
        else {
            //#define _SILENT
            #ifndef _SILENT
                printf("execute_cgi() :#24 exec sub proc=%d,pipe_send[1]=%d,pipe_recv[0]=%d\n",fork_result,pipe_send[1],pipe_recv[0]);
            #endif

            // Parent process
            request.sub_pid=fork_result;
            //close(pipe_fd[1]);  // Close write end
            close(pipe_send[0]);
            close(pipe_recv[1]);

            request.pipe_send=pipe_send[1];
            request.pipe_recv=pipe_recv[0];

            int nread;
            //int result;
            //int fd;

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

            //Connection: close
            //strcat(o_buff, http_con_close);

            //Transfer-Encoding: chunked
            //+"Content-type: text/html\r\n\r\n" // 注）最後は \r\nが２個必要
            size_t sl=strlen(o_buff);
            //---- end ----

            //size_t sl=strlen(o_buff);
            //int lpc=0;

            // Read output with timeout
            //fd_set read_fds;
            //struct timeval timeout;
            //timeout.tv_sec = 5;  // 5 second timeout
            //timeout.tv_usec = 0;
            
            // GET method
            if(request.method == "GET"){
                #ifndef _SILENT
                    // cgiへの出力 を行いません
                    printf("execute_cgi() :#25 close pipe_send[1]!!\n");
                #endif
                // cgiへの入力をクローズ
                close(pipe_send[1]);
                request.pipe_send=0;
            }
            // POST method
            else{
                size_t remaining = request.content_length;
                // cgi へリダイレクトする  httpReq->sockfd -> pipe_send[1]
                while(remaining > 0){
                    //printf("cgiProcessor() :#30 readBlock() call!!\n");
                    nread=readBlock(request.sockfd,request);
                    #ifndef _SILENT
                        printf("execute_cgi() :#30 request from Client nread=%d\n",nread);
                        //printf("execute_cgi() :#32.1 req-data=%.*s\n",nread,httpReq->buff);
                    #endif
                    //printf("cgiProcessor() :#31 readBlock() return!!\n");
                    if(nread > 0){
                        #ifndef _SILENT
                            printf("execute_cgi() :#31 request from Client nread=%d\n",nread);
                            //printf("execute_cgi() :#32.1 req-data=%.*s\n",nread,httpReq->buff);
                        #endif
                        if(write(pipe_send[1],request.buff,nread)!=nread){
                            printf("execute_cgi() :#32  write error\n");
                            break;
                        }
                        #ifndef _SILENT
                            string buff = string(request.buff,nread);
                            std::cout << "execute_cgi() :#33 request from Client buff:"<< buff << std::endl;
                            //printf("execute_cgi() :#33 request from Client buff=%s\n",buff);
                        #endif

                        //printf("execute_cgi() :#33 write to cgi one record end\n");
                        remaining -= nread;
                    }
                    else{
                        break;
                    }
                }
                #ifndef _SILENT
                    printf("execute_cgi() :#34 write to cgi all end\n");
                    printf("execute_cgi() :#35 close pipe_send[1]=%d\n",pipe_send[1]);
                #endif
                // cgiへの入力をクローズ
                close(pipe_send[1]);
                request.pipe_send=0;
            }
            // cgi からの出力を受け取る   httpReq->sockfd  <-  pipe_recv[0]
            while(1){
                // 注)read() は、データが上がってくるか。close されるまで待つ。
                // cgi側が、closeしないと、制御が戻ってこないので注意。
                // 将来は、上位で、全体のタイム監視機能を組み込む必要があります。
                nread=read(pipe_recv[0],request.buff,sizeof(request.buff));
                #ifndef _SILENT
                    printf("execute_cgi() :#46 read from cgi nread=%d\n",nread);
                #endif
                if(nread > 0){
                    //memdump(httpReq->buff,nread);
                    if(first_f==true){
                        #ifndef _SILENT
                            printf("execute_cgi() :#47 write http header\n");
                        #endif

                        string cgi_output = string(request.buff,nread);
                        #ifndef _SILENT
                            //std::cout << "execute_cgi() :#47.1 cgi_output from Client buff:\n"<< cgi_output << std::endl;
                        #endif

                        // Perl が 「Status: 302」を出力しているか検索
                        if (cgi_output.find("Status: 302") != std::string::npos) {

                            #ifndef _SILENT
                                std::cout << "execute_cgi() :#48 [置換前ヘッダー]:\n" << cgi_output << std::endl;
                            #endif

                            // 「Status: 302 Moved」の部分を「HTTP/1.1 302 Found」に置換する
                            string target = "Status: 302 Moved";
                            size_t pos = cgi_output.find(target);
                            if (pos != string::npos) {
                                cgi_output.replace(pos, target.length(), "HTTP/1.1 302 Found");
                            }

                            #ifndef _SILENT
                                std::cout << "execute_cgi() :#48_1 [置換後ヘッダー]:\n" << cgi_output << std::endl;
                                //exit(0);
                            #endif

                            // 置換したヘッダー（Locationや空行もすべて含まれています）をそのままブラウザへ送信！
                            if (write(request.sockfd, cgi_output.c_str(), cgi_output.length()) != (ssize_t)cgi_output.length()) {
                                printf("execute_cgi() :#48_2 write error \n");
                                break;
                            }
                        } 
                        else if(write(request.sockfd,o_buff,sl) != (ssize_t)sl){
                            printf("execute_cgi() :#49  write error \n");
                            break;
                        }
                    }
                    if(write(request.sockfd,request.buff,nread) != nread){
                        printf("execute_cgi() :#50  write error \n");
                        break;
                    }
                    first_f=false;
                }
                else if(nread == 0){
                    // リクエストエラー
                    if(first_f==true){
                        rc=false;
                    }
                    break;
                }
                else{
                    printf("execute_cgi() :#51 pipe read error end!!\n");
                    break;
                }
            }
            #ifndef _SILENT
                printf("execute_cgi() :#52 read end,close pipe_recv[0]=%d\n",pipe_recv[0]);
            #endif
            // cgiからの出力をクローズ
            close(pipe_recv[0]);
            request.pipe_recv=0;

            //auto cnt = count_open_fds();
            //cout << "exec_cgi(): #80 cnt:" << cnt << endl;

            // 🚀 【重要】ここでブラウザとのソケットを強制的に閉じます！
            // これにより、ブラウザ側に「データがすべて送り終わった」ことが伝わり、パッと画面が描画されます。
            //close(request.sockfd);

            // Wait for child process
            int status;
            waitpid(request.sub_pid, &status, 0);
            if(WIFEXITED(status) && WEXITSTATUS(status) != 0){
                std::cout << "execute_cgi() :#99 waitpid() error"<< std::endl;
            };
            return rc;
        }
    }
};

class Process_proc{
    public:
    Process_proc(){}

    Exec_proc exec_proc;
    HttpResponse response;

    // Handle directory requests
    HttpResponse handle_directory(const string& dir_path,HttpRequest& request){
        //HttpResponse response;
        
        // Check for index files
        vector<string> index_files = {"index.html", "index.htm", "index.php"};
        response.is_cgi=false;

        std::cout << "handle_directory(): #2 request.path:"<< request.path << std::endl;

        string org_path = request.path;

        for (const string& index_file : index_files) {
            string index_path;
            if(request.path == ""){
                index_path = dir_path + "/" + index_file;
            }
            //else if(request.path == "/"){
            else if(request.path.ends_with("/") == true){
                index_path = dir_path + index_file;
            }
            else{
                break;
            }

            std::cout << "handle_directory(): #4 index_path:"<< index_path << std::endl;

            if (fs::exists(index_path) && fs::is_regular_file(index_path)) {
                if (index_file == "index.php") {
                    //HttpRequest dummy_request;
                    //dummy_request.method = "GET";
                    //request.path = org_path + index_file;
                    request.script_name = "/"+index_file;
                    
                    string php_output="";

                    std::cout << "handle_directory(): #5 index_path:"<< index_path << " ,request.method:" << request.method << " ,request.path:" << request.path << std::endl;
                    // handle_directory(): #5 index_path:/home/nishi/www/toyopet/html/ez/index.php 
                    //  ,dummy_request.method:GET 
                    //  ,dummy_request.path:/ez/index.php

                    //std::exit(0);

                    if (exec_proc.execute_php(index_path, request)) {
                        response.is_cgi=true;
                        response.body = php_output;
                        response.headers["Content-Type"] = "text/html";
                        response.is_cgi=true;

                        //std::exit(0);
                        return response;
                    }
                } 
                else {
                    std::cout << "handle_directory(): #6 index_path:"<< index_path << std::endl;
                    vector<uint8_t> content;
                    if (read_file_safe(index_path, content)) {
                        response.is_cgi=false;
                        response.binary_data = move(content);
                        response.is_binary = true;
                        response.headers["Content-Type"] = get_mime_type(index_path);

                        std::cout << "handle_directory(): #7 Content-Type:"<< response.headers["Content-Type"] << std::endl;
                        return response;
                    }
                }
            }
        }
        // No index file found - return 403 Forbidden
        response.status_code = 403;
        response.body = "<html><body><h1>403 Forbidden</h1><p>Directory listing is not allowed.</p></body></html>";
        response.headers["Content-Type"] = "text/html";
        return response;
    }

    // Process HTTP request
    void process_request(HttpRequest& request, const string& client_ip) {
        log_info("Processing request from IP: " + client_ip);
        //exit(0);

        response.is_cgi=false;

        //HttpResponse response;
        if (!request.valid) {
            response.status_code = 400;
            response.body = "<html><body><h1>400 Bad Request</h1></body></html>";
            response.headers["Content-Type"] = "text/html";
            return;
        }
        
        // Sanitize path
        string safe_path = sanitize_path(request.path);

        std::cout << "process_request() :#2 request.path:"<< request.path <<" safe_path:" << safe_path << std::endl;

        if (safe_path.empty()) {
            //std::cout << "process_request() :#3 safe_path:" << safe_path << std::endl;
            response.status_code = 403;
            response.body = "<html><body><h1>403 Forbidden</h1><p>Invalid path.</p></body></html>";
            response.headers["Content-Type"] = "text/html";
            return;
        }
        // GET / HTTP/1.1 の時
        // Handle directory
        // これだと、 top だけ。 MT だと、top以下のディレクトリーも、対象にすべき。
        //if (request.path=="/" && fs::is_directory(safe_path)) {
        if (request.path.ends_with("/") == true && fs::is_directory(safe_path)) {
            //return handle_directory(safe_path, request.path);
            handle_directory(safe_path, request);
            return;
        }
        
        // Check for forbidden files
        if (is_forbidden_file(safe_path)) {
            //std::cout << "process_request() :#4 safe_path:" << safe_path << std::endl;
            response.status_code = 403;
            response.body = "<html><body><h1>403 Forbidden</h1><p>Access denied.</p></body></html>";
            response.headers["Content-Type"] = "text/html";
            return;
        }
        
        // 該当 file or directory が無い
        if (!fs::exists(safe_path)) {
            //std::cout << "process_request():#5 safe_path:" << safe_path << std::endl;
            //exit(0);

            // 拡張子が無い
            if(has_no_extension(safe_path)==true){
                // larable の redirect指定がある
                if(defs.larable_index !=""){
                    // larable の redirect を入れてみる
                    //string larable_index_path = defs.web_root+"/"+"index.php";
                    string larable_index_path = defs.web_root+"/"+defs.larable_index;

                    std::cout << "process_request():#4 larable_index_path:" << larable_index_path << std::endl;
                    if(fs::exists(larable_index_path)){
                        std::cout << "process_request():#5 larable_index_path is OK" << std::endl;
                        response.is_cgi=true;
                        request.query_string=request.path;
                        exec_proc.execute_php(larable_index_path, request);
                        return;
                    }
                }
            }
            response.status_code = 404;
            response.body = "<html><body><h1>404 Not Found</h1><p>The requested resource was not found.</p></body></html>";
            response.headers["Content-Type"] = "text/html";
            return;
        }
        
        
        // Handle PHP files
        if (safe_path.ends_with(".php")) {
            string php_output;

            std::cout << "process_request(): #5 safe_path:"<< safe_path << " ,request.method:" << request.method << " ,request.path:" << request.path << std::endl;
            // process_request(): #5 safe_path:/home/nishi/www/toyopet/html/ez/index.php 
            //  ,request.method:GET 
            //  ,request.path:/ez/index.php

            response.is_cgi=true;

            if (exec_proc.execute_php(safe_path, request)) {
                response.body = php_output;
                response.headers["Content-Type"] = "text/html";
            } 
            else {
                // error return でも、取れているみたい。
                //response.status_code = 500;
                //response.body = "<html><body><h1>500 Internal Server Error</h1><p>PHP execution failed.</p></body></html>";
                response.body = php_output;
                response.headers["Content-Type"] = "text/html";
            }
            return;
        }
        // add by nishi 2026.10.12
        // CGI
        else if(safe_path.ends_with(".cgi") || safe_path.ends_with(".pl")){
            std::cout << "process_request(): #6 safe_path:"<< safe_path << " ,request.method:" << request.method << " ,request.path:" << request.path << std::endl;
            if (request.path.starts_with("/cgi-bin/")) {
                string cgi_output="";
                response.is_cgi=true;

                if (exec_proc.execute_cgi(safe_path, request)) {
                    response.body = cgi_output;
                    response.headers["Content-Type"] = "text/html";
                } 
                else {
                    // error return でも、取れているみたい。
                    response.is_cgi=false;
                    response.status_code = 500;
                    response.body = "<html><body><h1>500 Internal Server Error</h1><p>CGI execution failed.</p></body></html>";
                    //response.body = cgi_output;
                    response.headers["Content-Type"] = "text/html";
                }
                return;
            }
        }
        
        // Handle static files

        std::string ext = get_extension(safe_path);
        std::cout << "process_request(): #8 ext:"<<ext<< std::endl;

        if(send_image_file(request.sockfd, safe_path)){
            response.is_cgi=true;
        }
        else{
            vector<uint8_t> content;
            if (read_file_safe(safe_path, content)) {
                response.binary_data = move(content);
                response.is_binary = true;
                response.headers["Content-Type"] = get_mime_type(safe_path);
            } 
            else {
                response.status_code = 500;
                response.body = "<html><body><h1>500 Internal Server Error</h1><p>Failed to read file.</p></body></html>";
                response.headers["Content-Type"] = "text/html";
            }
        }
        //return;
    }    

    // Send HTTP response
    bool send_response(int client_socket) {
        string status_message = "Unknown";
        auto it = status_messages.find(response.status_code);
        if (it != status_messages.end()) {
            status_message = it->second;
        }
        
        // Build response headers
        stringstream response_stream;
        response_stream << "HTTP/1.1 " << response.status_code << " " << status_message << "\r\n";
        response_stream << "Server: " << SERVER_NAME << "\r\n";
        response_stream << "Connection: close\r\n";
        
        // Add custom headers
        for (const auto& header : response.headers) {
            response_stream << header.first << ": " << header.second << "\r\n";
        }
        
        // Content length
        size_t content_length = response.is_binary ? response.binary_data.size() : response.body.length();
        response_stream << "Content-Length: " << content_length << "\r\n";
        
        response_stream << "\r\n";
        
        // Send headers
        string headers = response_stream.str();
        if (send(client_socket, headers.c_str(), headers.length(), MSG_NOSIGNAL) == -1) {
            return false;
        }
        
        // Send body
        if (content_length > 0) {
            if (response.is_binary) {
                if (send(client_socket, response.binary_data.data(), response.binary_data.size(), MSG_NOSIGNAL) == -1) {
                    return false;
                }
            } else {
                if (send(client_socket, response.body.c_str(), response.body.length(), MSG_NOSIGNAL) == -1) {
                    return false;
                }
            }
        }
        return true;
    }
};


class HeadAccept{
    public:
    HeadAccept(){
        init();
    }
    // 最大ヘッダーサイズ（悪意のある巨大ヘッダーによるメモリ枯渇攻撃を防ぐ）
    const size_t MAX_HEADER_SIZE = 8192; // 8KB

    //std::string header_str;
    char buffer[8192+1]; 
    //std::basic_string<char> header_str;
    size_t current_len;

    void init(){
        //header_str.reserve(MAX_HEADER_SIZE+1);
        current_len = 0;
        buffer[0] = '\0';
    }

    // ソケットからヘッダー部分（\r\n\r\nまで）だけを安全に読み込む関数
    int read_header_only(int client_socket) {
        char c;
        init(); // 読み込み前にカウンターをリセット

        // 1バイトずつソケットから読み込む
        while (current_len < MAX_HEADER_SIZE) {
            ssize_t n = recv(client_socket, &c, 1, 0);
            if (n > 0) {
                // 固定の箱に文字をそのまま入れる（new は起きない）
                buffer[current_len] = c;
                current_len++;

                // 💡 末尾4文字が "\r\n\r\n" になったかチェック
                if (current_len >= 4 && 
                    buffer[current_len - 4] == '\r' && buffer[current_len - 3] == '\n' &&
                    buffer[current_len - 2] == '\r' && buffer[current_len - 1] == '\n') {
                    break; // ヘッダーの終わり
                }
            } 
            else {
                // エラーまたは切断
                break;
            }
        }
        // 文字列の終端にヌル文字を安全に書き込む
        buffer[current_len] = '\0';
        return current_len; // 読み込めたサイズを返す
    }

    #if defined(USE_OLD_FUNC)
    // ソケットからヘッダー部分（\r\n\r\nまで）だけを安全に読み込む関数
    int read_header_only(int client_socket) {
        char c;
        // 1バイトずつソケットから読み込む
        while (header_str.length() < MAX_HEADER_SIZE) {
            // recvの第4引数に0を指定して1バイト読み込み
            ssize_t n = recv(client_socket, &c, 1, 0);
            if (n > 0) {
                header_str.push_back(c);
                // 💡 文字列の末尾4文字が "\r\n\r\n" になったかチェック
                if (header_str.length() >= 4 && 
                    header_str.compare(header_str.length() - 4, 4, "\r\n\r\n") == 0) {
                    break; // ヘッダーの終わりを検知したのでループを抜ける
                }
            } 
            else if (n == 0) {
                // ブラウザ側が接続を閉じた場合
                break;
            } 
            else {
                // エラーが発生した場合（タイムアウトや切断など）
                // 必要に応じてログ出力やエラー処理を行う
                break;
            }
        }
        return header_str.length();
    }
    #endif

    int read_header_only_byte(int client_socket, char buffer[]) {
        //header_str.clear();
        
        // ⭕ 自分で固定バッファ（スタック領域、またはmalloc）を確保
        // ここでは一番安全なスタック上のローカル配列を使います（スレッドごとに独立して安全）
        //char buffer[MAX_HEADER_SIZE]; 
        size_t total_bytes = 0;
        char c;

        // 1バイトずつソケットから読み込んで固定バッファに詰める
        while (total_bytes < MAX_HEADER_SIZE) {
            ssize_t n = recv(client_socket, &c, 1, 0);
            if (n > 0) {
                buffer[total_bytes] = c;
                total_bytes++;

                // 💡 末尾4文字が "\r\n\r\n" になったかチェック
                if (total_bytes >= 4 && 
                    buffer[total_bytes - 4] == '\r' && buffer[total_bytes - 3] == '\n' &&
                    buffer[total_bytes - 2] == '\r' && buffer[total_bytes - 1] == '\n') {
                    break; // ヘッダー終了
                }
            } else {
                break;
            }
        }

        // 最後に、完成したデータだけを std::string に1回だけコピーする
        // （一瞬で終わるため、スレッド終了時のASan誤検知ルートに引っかからなくなります）
        //if (total_bytes > 0) {
        //    header_str.assign(buffer, total_bytes);
        //}
        buffer[total_bytes]= '\0';
        return total_bytes;
    }

    // Read request with timeout
    string read_request_with_timeout(int client_socket) {
        string request;
        char buffer[1024];
        
        fd_set read_fds;
        struct timeval timeout;

        while (request.length() < MAX_REQUEST_SIZE) {
            FD_ZERO(&read_fds);
            FD_SET(client_socket, &read_fds);

            // 5秒でタイムアウトするみたい add by nishi 2025.12.29
            timeout.tv_sec = REQUEST_TIMEOUT_SECONDS;
            timeout.tv_usec = 0;
            // https://www.geekpage.jp/programming/winsock/select-with-timeout.php
            int select_result = select(client_socket + 1, &read_fds, nullptr, nullptr, &timeout);
            
            if (select_result <= 0) {
                break;  // Timeout or error
            }
            
            ssize_t bytes_received = recv(client_socket, buffer, sizeof(buffer) - 1, 0);
            if (bytes_received <= 0) {
                break;
            }
            
            buffer[bytes_received] = '\0';
            request += buffer;
            
            // Check if we have complete headers
            if (request.find("\r\n\r\n") != string::npos) {
                break;
            }
        }
        return request;
    }

    // Parse HTTP request with proper validation
    HttpRequest parse_request() {
    //HttpRequest parse_request(std::string header_str) {
        std::string header_str(buffer, current_len);
        HttpRequest request;
        istringstream stream(header_str);
        string line;
        // Parse request line
        if (!getline(stream, line) || line.empty()) {
            return request;
        }
        // Remove carriage return if present
        if (!line.empty() && line.back() == '\r') {
            line.pop_back();
        }
        istringstream request_line(line);
        if (!(request_line >> request.method >> request.path >> request.version)) {
            return request;
        }
        // Validate HTTP method
        static const unordered_set<string> allowed_methods = {"GET", "POST", "HEAD"};
        if (allowed_methods.find(request.method) == allowed_methods.end()) {
            return request;
        }
        // Validate HTTP version
        if (request.version != "HTTP/1.0" && request.version != "HTTP/1.1") {
            return request;
        }

        // "User-Agent:" の中身を取り出す add by nishi 2025.12.30
        //string user_agent = get_header_value(raw_request, "User-Agent");
        //std::cout << "parse_request() :#5 user_agent:" << request.user_agent << std::endl;
        //string  authenticate = get_header_value(raw_request,"WWW-authenticate");
        //std::cout << "parse_request() :#6 authenticate:" << authenticate << std::endl;

        std::cout << ">> parse_request() :#7 header_str:\n" << header_str << std::endl;

        // Parse headers
        request.content_length = 0;
        // whileの中は getline だけにして、途中の判定は内部で行う
        while (getline(stream, line)) {

            if (!line.empty() && line.back() == '\r') {
                line.pop_back();
            }

            // 2. '\r' を消した結果、空行（ヘッダーの終わり）になったらループを抜ける 🚀
            if (line.empty()) {
                break;
            }

            // 3. ここから下の解析処理はそのまま
            size_t colon_pos = line.find(':');
            if (colon_pos != string::npos) {
                string header_name = line.substr(0, colon_pos);
                string header_value = line.substr(colon_pos + 1);
                
                // Trim whitespace
                header_name.erase(0, header_name.find_first_not_of(" \t"));
                header_name.erase(header_name.find_last_not_of(" \t") + 1);
                header_value.erase(0, header_value.find_first_not_of(" \t"));
                header_value.erase(header_value.find_last_not_of(" \t") + 1);
                
                // Convert header name to lowercase for case-insensitive comparison
                transform(header_name.begin(), header_name.end(), header_name.begin(), ::tolower);
                request.headers[header_name] = header_value;
                if (header_name == "content-length") {
                    try {
                        request.content_length = stoull(header_value);
                        //if (content_length > MAX_BODY_SIZE) {
                        //    return request;  // Body too large
                        //}
                    } 
                    catch (...) {
                        return request;  // Invalid content-length
                    }
                }
            }
        }

        //#define TEST_DISPLAY_1
        #if defined(TEST_DISPLAY_1)
        // request.headers をチェックする
        std::cout << "parse_request() :#20 request.headers:" << std::endl;
        for (auto x:request.headers){
            std::cout << x.first << "->" << x.second << std::endl;
        }
        std::cout << "--- end ----" << std::endl;
        #endif

        // method GET
        if (request.method=="GET"){
            auto pos=request.path.find("?");
            if(pos != string::npos){
                // 先頭から '?' の直前までを安全に切り出す
                string cgi_path = request.path.substr(0, pos);
                // '?' の次の文字から末尾までを安全に切り出す
                // ※ pos + 1 が文字列の長さを超えないように substr の第2引数を省略する（自動で末尾までになる）
                string query_string = request.path.substr(pos + 1);
                request.query_string = query_string;
                request.path = cgi_path;
            }
        }

        // ※ヘッダー名は lowercase（小文字化）して格納されているため "authorization" で検索します
        if (request.headers.count("authorization") > 0) {
            std::string auth_val = request.headers["authorization"]; // 例: "Basic YWRtaW46cGFzc3dvcmQxMjM="

            // 先頭が "Basic " で始まっているかチェック
            if (auth_val.substr(0, 6) == "Basic ") {
                // 1. "Basic "（6文字）の後ろのエンコード文字列をシンプルに切り出し
                std::string encoded = auth_val.substr(6);

                // 2. Base64デコードを行う
                std::string decoded = base64_decode(encoded); // 例: "admin:password123"

                // 3. コロン ':' で分割して構造体に格納
                auto colon_pos = decoded.find(":");
                if (colon_pos != std::string::npos) {
                    request.auth_user = decoded.substr(0, colon_pos);
                    request.auth_pw   = decoded.substr(colon_pos + 1);
                    //std::cout << "parse_request() :#25 request.auth_user:" << request.auth_user <<", request.auth_pw:"<< request.auth_pw << std::endl;
                }
            }
        }

        std::cout << "parse_request() :#30 request.method:" << request.method <<", request.path:"<< request.path <<", request.query_string:" <<request.query_string<< " ,request.content_length:"<< request.content_length <<  std::endl;
        std::cout << "parse_request() :#99 passed!!" <<  std::endl;
        request.valid = true;
        return request;
    }
};

// Handle client connection
void handle_client(int client_socket, string client_ip) {
    // Increment active thread count
    active_threads++;

    HeadAccept headaccept;
    Process_proc process_proc;
    
    // Track connection per IP
    {
        lock_guard<mutex> lock(connections_mutex);
        ip_connections[client_ip]++;
    }
    
    try {
        // Read request with timeout
        //string raw_request = read_request_with_timeout(client_socket);
        int len = headaccept.read_header_only(client_socket);

        if (len ==0) {
            log_error("Empty or timeout request from " + client_ip);
        } 
        else {
            // 🚀 人工的に新しいスコープ { を開始
            {

                // 🚀 確定したサイズ（len）だけを安全に1回だけ string にコピーする！
                // 途中でサイズが変わるガタガタした動きをしないため、ASanに目を付けられません。
                //std::string request_head(headaccept.buffer, len);

                // Parse and process request
                HttpRequest request = headaccept.parse_request();
                request.client_ip = client_ip;
                //HttpRequest request = headaccept.parse_request(request_head);
                // add by nishi 2026.10.13
                request.sockfd=client_socket;

                //HttpResponse response;
                //response.is_cgi=false;

                process_proc.process_request(request, client_ip);
                
                if(process_proc.response.is_cgi == false){
                    // Send response
                    if (!process_proc.send_response(client_socket)) {
                        log_error("Failed to send response to " + client_ip);
                    }
                }
                
                log_info("Served " + request.method + " " + request.path + " to " + client_ip + 
                        " (Status: " + to_string(process_proc.response.status_code) + ")");
            } // 🚀 ここで右かっこ } を抜けた瞬間に、request_head は物理的にこの世から消滅（100%解放）！
        }
    } 
    catch (const exception& e) {
        log_error("Exception handling client " + client_ip + ": " + e.what());
    }
    
    // Cleanup
    close(client_socket);
    
    {
        lock_guard<mutex> lock(connections_mutex);
        ip_connections[client_ip]--;
        if (ip_connections[client_ip] <= 0) {
            ip_connections.erase(client_ip);
        }
    }
    
    active_threads--;
}

// Signal handler for SIGCHLD to prevent zombie processes
void sigchld_handler(int sig) {
    (void)sig;  // Suppress unused parameter warning
    while (waitpid(-1, nullptr, WNOHANG) > 0) {
        // Reap zombie processes
    }
}

// Main server function
int main() {
    serverConfig(defs);
    // Install SIGCHLD handler
    struct sigaction sa;
    sa.sa_handler = sigchld_handler;
    sigemptyset(&sa.sa_mask);
    sa.sa_flags = SA_RESTART | SA_NOCLDSTOP;
    if (sigaction(SIGCHLD, &sa, nullptr) == -1) {
        log_error("Failed to install SIGCHLD handler");
        return 1;
    }
    
    // Create socket
    int server_socket = socket(AF_INET, SOCK_STREAM, 0);
    if (server_socket == -1) {
        log_error("Failed to create socket: " + string(strerror(errno)));
        return 1;
    }
    
    // Set socket options
    int opt = 1;
    if (setsockopt(server_socket, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt)) == -1) {
        log_error("Failed to set socket options: " + string(strerror(errno)));
        close(server_socket);
        return 1;
    }
    
    // Bind socket
    struct sockaddr_in server_addr;
    memset(&server_addr, 0, sizeof(server_addr));
    server_addr.sin_family = AF_INET;
    server_addr.sin_addr.s_addr = INADDR_ANY;
    server_addr.sin_port = htons(defs.server_port);
    
    if (bind(server_socket, (struct sockaddr*)&server_addr, sizeof(server_addr)) == -1) {
        log_error("Failed to bind socket: " + string(strerror(errno)));
        close(server_socket);
        return 1;
    }
    
    // Listen for connections
    if (listen(server_socket, 128) == -1) {
        log_error("Failed to listen: " + string(strerror(errno)));
        close(server_socket);
        return 1;
    }
    
    log_info("Secure HTTP Server started on port " + to_string(defs.server_port));
    log_info("Web root: " + string(defs.web_root));
    log_info("Max threads: " + to_string(defs.maxconnect));
    
    // Main server loop
    while (true) {
        struct sockaddr_in client_addr;
        socklen_t client_addr_len = sizeof(client_addr);
        
        int client_socket = accept(server_socket, (struct sockaddr*)&client_addr, &client_addr_len);
        if (client_socket == -1) {
            if (errno == EINTR) {
                continue;  // Interrupted by signal, continue
            }
            log_error("Failed to accept connection: " + string(strerror(errno)));
            continue;
        }
        
        // Get client IP
        string client_ip = inet_ntoa(client_addr.sin_addr);
        
        // Check thread limit
        if (active_threads >= defs.maxconnect) {
            log_error("Thread limit reached, rejecting connection from " + client_ip);
            
            string error_response = "HTTP/1.1 503 Service Unavailable\r\n"
                                  "Content-Type: text/html\r\n"
                                  "Content-Length: 82\r\n"
                                  "Connection: close\r\n\r\n"
                                  "<html><body><h1>503 Service Unavailable</h1><p>Server busy.</p></body></html>";
            send(client_socket, error_response.c_str(), error_response.length(), MSG_NOSIGNAL);
            close(client_socket);
            continue;
        }
        
        // Check connections per IP
        {
            lock_guard<mutex> lock(connections_mutex);
            if (ip_connections[client_ip] >= MAX_CONNECTIONS_PER_IP) {
                log_error("Too many connections from " + client_ip);
                
                string error_response = "HTTP/1.1 429 Too Many Requests\r\n"
                                      "Content-Type: text/html\r\n"
                                      "Content-Length: 86\r\n"
                                      "Connection: close\r\n\r\n"
                                      "<html><body><h1>429 Too Many Requests</h1><p>Rate limited.</p></body></html>";
                send(client_socket, error_response.c_str(), error_response.length(), MSG_NOSIGNAL);
                close(client_socket);
                continue;
            }
        }
        // Handle client in new thread
        thread client_thread(handle_client, client_socket, client_ip);
        client_thread.detach();
        // ❌ ここで close(client_socket); をしていない
        //close(client_socket);
    }
    
    close(server_socket);
    return 0;
}