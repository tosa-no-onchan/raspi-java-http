#!/bin/bash
#このコンパイルファイルは、piマシンでのコンパイル用です。
# $sh ./make_java.sh MyLed.java
# $sh ./make_java.sh mod_test/hello_cgi_mod.java
javac -Xlint -sourcepath ./src -d ./bin -s ./src src/$1
