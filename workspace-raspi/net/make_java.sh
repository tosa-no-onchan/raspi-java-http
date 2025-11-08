#!/bin/bash
#このコンパイルファイルは、piマシンでのコンパイル用です。
# http_pi/httpServer.java のコンパイル方法
# $cd ~user-name/workspace-raspi/net
# how to build src/http_pi/httpServer.java
# $ sh ./make_java.sh http_pi/httpServer.java
javac -Xlint -sourcepath ./src -d ./bin -s ./src src/$1
