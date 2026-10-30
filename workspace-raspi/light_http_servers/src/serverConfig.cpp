/*
 * serverConfig.cpp
 *	v1.3.2
 *  update on: 2026/10/14
 *      Author: nishi
 */

#include <iostream>
#include <fstream>
#include <string>
#include <map>
#include <algorithm>

#include "serverConfig.hpp"

std::string filename = "server.conf";

// 文字列の前後の空白（スペース・タブ・改行）を削除する補助関数
std::string trim(const std::string& str) {
    size_t first = str.find_first_not_of(" \t\r\n");
    if (std::string::npos == first) return "";
    size_t last = str.find_last_not_of(" \t\r\n");
    return str.substr(first, (last - first + 1));
}

int serverConfig(Defs &defs){
    // -----------------------------------------
    // 2. ファイルからの読み込み (std::ifstream)
    // -----------------------------------------
    std::ifstream infile(filename); // 読み込み用に開く

    if (!infile) {
        std::cerr << "エラー: ファイルを開けませんでした（読み込み用）" << std::endl;
        return 1;
    }

    std::map<std::string, std::string> config;
    std::string line;

    // 1行ずつ読み込んでパース
    while (std::getline(infile, line)) {
        line = trim(line);

        // 空行、または '#' で始まるコメント行は無視
        if (line.empty() || line[0] == '#') {
            continue;
        }

        // '=' の位置を探す
        size_t delimiter_pos = line.find('=');
        if (delimiter_pos != std::string::npos) {
            std::string key = trim(line.substr(0, delimiter_pos));
            std::string value = trim(line.substr(delimiter_pos + 1));
            
            if (!key.empty()) {
                config[key] = value; // マップに格納
            }
        }
    }
    infile.close();    

    // ----------------------------------------------------
    // 2. マップから実際の変数に設定を代入する
    // ----------------------------------------------------
    // デフォルト値を決めておき、設定があれば上書き（stoi で数値変換）
    defs.server_port = config.count("server_port") ? std::stoi(config["server_port"]) : 80;
    defs.timeout = config.count("timeout") ? std::stoi(config["timeout"]) : 120;
    defs.maxconnect = config.count("maxconnect") ? std::stoi(config["maxconnect"]) : 1;

    defs.http_host = config["http_host"] != "" ? config["http_host"]: "192.168.1.170";
    defs.server_name = config["server_name"] != "" ? config["server_name"]: "192.168.1.170";
    defs.larable_index = config["larable_index"] != "" ? config["larable_index"] : "";

    defs.allow_networks = config["allow_networks"];
    defs.web_root    = config["web_root"];
    defs.cgi_root    = config["cgi_root"];
    defs.cgi_error_log = config["cgi_error_log"] != "" ? config["cgi_error_log"]: "";
    defs.cgi_log_path = config["cgi_log_path"] != "" ? config["cgi_log_path"]: "";

    //timeout=30
    //maxconnect=1
    //allow_networks=192.168.1.0/255.255.255.0
    //server_port=80
    //web_root=/home/nishi/www/kyusyupost/html
    //cgi_root=/home/nishi/netosa/www/cgi-bin

    // ----------------------------------------------------
    // 3. 読み込み結果の確認表示
    // ----------------------------------------------------
    std::cout << "--- 設定ファイルの読み込み完了 ---" << std::endl;
    std::cout << "server_port:    " << defs.server_port << std::endl;
    std::cout << "timeout:        " << defs.timeout << std::endl;
    std::cout << "maxconnect:     " << defs.maxconnect << std::endl;
    std::cout << "allow_networks: " << defs.allow_networks << std::endl;
    std::cout << "web_root:       " << defs.web_root << std::endl;
    std::cout << "cgi_root:       " << defs.cgi_root << std::endl;

    std::cout << "http_host:      " << defs.http_host<< std::endl;
    std::cout << "server_name:    " << defs.server_name<< std::endl;
    std::cout << "larable_index:  " << defs.larable_index<< std::endl;
    std::cout << "cgi_error_log:  " << defs.cgi_error_log<< std::endl;
    std::cout << "cgi_log_path:  " << defs.cgi_log_path<< std::endl;

    return 0;

}  