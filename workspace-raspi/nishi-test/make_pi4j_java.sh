#!/bin/bash
# make_pi4j_java.sh
# sh ./make_pi4j_java.sh arduino/usb_serial_test1.java
#このコンパイルファイルは、pi4j を使った raspiマシンでのコンパイル用です。
javac -Xlint -cp /opt/pi4j-1.0-SNAPSHOT/lib/'*' -sourcepath ./src -d ./bin -s ./src src/$1
 
