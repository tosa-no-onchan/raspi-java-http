### C++ Light Http Server  

Ubuntu 24.04  C++ Light Http Server  
make したら、sudo ./light_http_server で、すぐ試せます。  
lighttpd より、お手軽に試せます。  

PHP Laravel、 MT も、一通り動くと思います。  

### original
![GitHub repo size](https://img.shields.io/github/repo-size/aashishbishow/http_servers)
![GitHub license](https://img.shields.io/github/license/aashishbishow/http_servers)

上記、コードをベースに、PHP、Perl CGI での、Get、Post に対応しました。

オリジナル版は、PHP Get 処理しか入っていなかったので、  
仕方がないから、自分で機能追加しました。  
あと、折角 C++ を使っているので、  
Class モジュール構成にしました。  

Ubuntu 24.04 環境で、make できます。  
注) Windows 版は、入っていません。  

1. Build  
$ make  
src/xxx.o を削除するのは、  
$ make clean  

3. server.conf を、自分の環境にあわせてください。  

4. Run  
$ sudo ./light_http_server  

P.S.  
1. PHP を使う場合は、php-cgi をインストールしてください。  
$ sudo apt install php-cgi
  
2. Ubuntu 24.04 ローカル環境で、 MT の古い版を走らせて、動作確認しています。  
結構便利。  
また、 Laravel PHP の動作確認も、一応できます。


